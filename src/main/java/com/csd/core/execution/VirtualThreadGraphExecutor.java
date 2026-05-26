package com.csd.core.execution;

import java.util.*;
import java.util.concurrent.*;

import com.csd.core.api.StreamEnvironment;
import com.csd.core.execution.operations.*;
import com.csd.core.schema.StreamVertex;

public class VirtualThreadGraphExecutor {

    // Queue capacities
    private static final int INITIAL_WINDOW_SIZE = 128;
    private static final int MIN_WINDOW_SIZE = 32;
    private static final int MAX_WINDOW_SIZE = 512;

    // Latency-oriented microbatch sizes
    private static final int MIN_BATCH_SIZE = 256;
    private static final int MAX_BATCH_SIZE = 65_536;

    public void execute(StreamEnvironment graph) throws InterruptedException {

        Map<String, BlockingQueue<BatchMessage<?>>> channels = new HashMap<>();

        for (StreamVertex<?, ?> node : graph.getNodes()) {
            for (Object outNode : node.getOutgoingEdges()) {

                StreamVertex<?, ?> target = (StreamVertex<?, ?>) outNode;

                String edgeKey = node.getId() + "->" + target.getId();

                channels.put(
                    edgeKey,
                    new AdaptiveBlockingQueue<>(
                        INITIAL_WINDOW_SIZE,
                        MIN_WINDOW_SIZE,
                        MAX_WINDOW_SIZE
                    )
                );
            }
        }

        try (ExecutorService executor =
                Executors.newVirtualThreadPerTaskExecutor()) {

            CountDownLatch latch =
                    new CountDownLatch(graph.getNodes().size());

            for (StreamVertex<?, ?> node : graph.getNodes()) {

                executor.submit(() -> {

                    try {
                        runNodeWorker(node, channels);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void runNodeWorker(
            StreamVertex<?, ?> node,
            Map<String, BlockingQueue<BatchMessage<?>>> channels)
            throws InterruptedException {

        Operator payload = node.getPayload();

        List<BlockingQueue<BatchMessage<?>>> inbound = new ArrayList<>();
        List<BlockingQueue<BatchMessage<?>>> outbound = new ArrayList<>();

        for (Object in : node.getIncomingEdges()) {
            StreamVertex<?, ?> parent = (StreamVertex<?, ?>) in;
            inbound.add(channels.get(parent.getId() + "->" + node.getId()));
        }

        for (Object out : node.getOutgoingEdges()) {
            StreamVertex<?, ?> child = (StreamVertex<?, ?>) out;
            outbound.add(channels.get(node.getId() + "->" + child.getId()));
        }

        // SOURCE NODES
        if (payload instanceof BatchSourceOp source) {

            while (true) {

                int targetBatchSize =
                        computeTargetBatchSize(outbound);

                List<?> batch =
                        source.processBatchDirect(targetBatchSize);

                if (batch == null || batch.isEmpty()) {
                    break;
                }

                broadcast(outbound, BatchMessage.data(batch));
            }

            broadcast(outbound, BatchMessage.eos());
            return;
        }

        // INTERMEDIATE + SINK NODES

        List<BlockingQueue<BatchMessage<?>>> active =
                new CopyOnWriteArrayList<>(inbound);

        while (!active.isEmpty()) {

            for (BlockingQueue<BatchMessage<?>> queue : active) {

                BatchMessage<?> msg =
                        queue.poll(1, TimeUnit.MILLISECONDS);

                if (msg == null) {
                    continue;
                }

                if (msg.isEos()) {
                    active.remove(queue);
                    continue;
                }

                List<?> result =
                        payload.processBatch(msg.payload());

                if (result != null && !result.isEmpty()) {
                    broadcast(outbound, BatchMessage.data(result));
                }
            }
        }

        broadcast(outbound, BatchMessage.eos());
    }

    private int computeTargetBatchSize(
            List<BlockingQueue<BatchMessage<?>>> queues) {

        double fill = getMaxFillRatio(queues);

        // inverse pressure scaling
        double pressure = 1.0 - fill;

        return (int)(
                MIN_BATCH_SIZE +
                ((MAX_BATCH_SIZE - MIN_BATCH_SIZE) * pressure)
        );
    }

    private double getMaxFillRatio(
            List<BlockingQueue<BatchMessage<?>>> queues) {

        double max = 0.0;

        for (BlockingQueue<BatchMessage<?>> q : queues) {

            if (q instanceof AdaptiveBlockingQueue<?> adaptive) {
                max = Math.max(max, adaptive.getFillRatio());
            } else {
                int total = q.size() + q.remainingCapacity();

                if (total > 0) {
                    max = Math.max(max, (double) q.size() / total);
                }
            }
        }

        return max;
    }

    private void broadcast(
            List<BlockingQueue<BatchMessage<?>>> queues,
            BatchMessage<?> msg)
            throws InterruptedException {

        for (BlockingQueue<BatchMessage<?>> queue : queues) {
            queue.put(msg);
        }
    }
}