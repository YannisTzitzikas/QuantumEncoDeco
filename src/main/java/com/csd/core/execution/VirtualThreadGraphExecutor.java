package com.csd.core.execution;

import java.util.*;
import java.util.concurrent.*;
import com.csd.core.api.StreamEnvironment;
import com.csd.core.execution.operations.*;
import com.csd.core.schema.StreamVertex;

public class VirtualThreadGraphExecutor {

    private static final int MAX_QUEUED_BATCHES = 100;

    public void execute(StreamEnvironment graph) throws InterruptedException {
        // FIX: Mapping is now Edge-based (From -> To) instead of Node-based!
        // This isolates communication channels completely.
        Map<String, BlockingQueue<BatchMessage<?>>> channels = new HashMap<>();
        
        for (StreamVertex<?, ?> node : graph.getNodes()) {
            for (Object outNode : node.getOutgoingEdges()) {
                StreamVertex<?, ?> target = (StreamVertex<?, ?>) outNode;
                String edgeKey = node.getId() + "->" + target.getId();
                channels.put(edgeKey, new ArrayBlockingQueue<>(MAX_QUEUED_BATCHES));
            }
        }

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch completionLatch = new CountDownLatch(graph.getNodes().size());

            for (StreamVertex<?, ?> node : graph.getNodes()) {
                executor.submit(() -> {
                    try {
                        runNodeWorker(node, channels);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        completionLatch.countDown();
                    }
                });
            }

            completionLatch.await();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void runNodeWorker(StreamVertex node, Map<String, BlockingQueue<BatchMessage<?>>> channels) throws InterruptedException {
        Operator payload = node.getPayload();
        
        // Locate all inbound channels explicitly
        List<BlockingQueue<BatchMessage<?>>> inboundQueues = new ArrayList<>();
        for (Object inNode : node.getIncomingEdges()) {
            StreamVertex<?, ?> source = (StreamVertex<?, ?>) inNode;
            inboundQueues.add(channels.get(source.getId() + "->" + node.getId()));
        }

        // Locate all outbound channels explicitly
        List<BlockingQueue<BatchMessage<?>>> outboundQueues = new ArrayList<>();
        for (Object outNode : node.getOutgoingEdges()) {
            StreamVertex<?, ?> target = (StreamVertex<?, ?>) outNode;
            outboundQueues.add(channels.get(node.getId() + "->" + target.getId()));
        }

        // --- SOURCE NODE LOGIC ---
        if (payload instanceof BatchSourceOp sourceOp) {
            while (true) {
                List batch = sourceOp.processBatch(null);
                if (batch == null || batch.isEmpty()) {
                    broadcast(outboundQueues, BatchMessage.eos());
                    break;
                }
                broadcast(outboundQueues, BatchMessage.data(batch));
            }
            return;
        }

        // --- FILTER / MAP / SINK / MERGE NODE LOGIC ---
        // Active inbound channels we need to poll data from
        List<BlockingQueue<BatchMessage<?>>> activeInputs = new CopyOnWriteArrayList<>(inboundQueues);

        while (!activeInputs.isEmpty()) {
            // Fair round-robin polling across isolated input streams to prevent starvation
            for (BlockingQueue<BatchMessage<?>> queue : activeInputs) {
                // Use poll with a tiny timeout to avoid thread spinning while remaining non-blocking
                BatchMessage<?> msg = queue.poll(10, TimeUnit.MILLISECONDS);
                if (msg == null) continue;

                if (msg.isEos()) {
                    // This parent is done. Drop the isolated queue.
                    activeInputs.remove(queue);
                } else {
                    List resultBatch = payload.processBatch(msg.payload());
                    if (resultBatch != null && !resultBatch.isEmpty()) {
                        broadcast(outboundQueues, BatchMessage.data(resultBatch));
                    }
                }
            }
        }

        // When all active inputs are fully exhausted, safely broadcast downstream EOS
        if (!(payload instanceof SinkOp)) {
            broadcast(outboundQueues, BatchMessage.eos());
        }
    }

    private void broadcast(List<BlockingQueue<BatchMessage<?>>> queues, BatchMessage<?> msg) throws InterruptedException {
        for (BlockingQueue<BatchMessage<?>> queue : queues) {
            queue.put(msg); 
        }
    }
}