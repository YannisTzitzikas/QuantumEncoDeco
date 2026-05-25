package com.csd.dag.execution;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import com.csd.dag.api.StreamEnvironment;
import com.csd.dag.execution.operations.BatchSourceOp;
import com.csd.dag.execution.operations.Operator;
import com.csd.dag.execution.operations.SinkOp;
import com.csd.dag.schema.StreamVertex;

public class VirtualThreadGraphExecutor {

    // Capacity cap to prevent fast producers from overwhelming slow consumers
    private static final int MAX_QUEUED_BATCHES = 100;

    public void execute(StreamEnvironment graph) throws InterruptedException {
        // 1. Provision Queues: Every node gets an input queue
        Map<StreamVertex<?, ?>, BlockingQueue<BatchMessage<?>>> nodeInputQueues = new HashMap<>();
        for (StreamVertex<?, ?> node : graph.getNodes()) {
            nodeInputQueues.put(node, new ArrayBlockingQueue<>(MAX_QUEUED_BATCHES));
        }

        // 2. Prepare Virtual Thread Pool
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch completionLatch = new CountDownLatch(graph.getNodes().size());

            // 3. Launch a Virtual Thread worker for each node
            for (StreamVertex<?, ?> node : graph.getNodes()) {
                executor.submit(() -> {
                    try {
                        runNodeWorker(node, nodeInputQueues);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        completionLatch.countDown();
                    }
                });
            }

            // 4. Wait for the graph execution to finish cleanly via EOS propagation
            completionLatch.await();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void runNodeWorker(StreamVertex node, Map<StreamVertex<?, ?>, BlockingQueue<BatchMessage<?>>> queues) throws InterruptedException {
        Operator payload = node.getPayload();
        BlockingQueue<BatchMessage<?>> myInputQueue = queues.get(node);
        
        List<BlockingQueue<BatchMessage<?>>> downstreamQueues = new ArrayList<>();
        for (Object outNode : node.getOutgoingEdges()) {
            downstreamQueues.add(queues.get((StreamVertex<?, ?>) outNode));
        }

        // --- SOURCE NODE LOGIC ---
        if (payload instanceof BatchSourceOp sourceOp) {
            while (true) {
                List batch = sourceOp.processBatch(null);
                if (batch == null || batch.isEmpty()) {
                    broadcast(downstreamQueues, BatchMessage.eos());
                    break;
                }
                broadcast(downstreamQueues, BatchMessage.data(batch));
            }
            return;
        }

        // --- FILTER / MAP / SINK / MERGE NODE LOGIC ---
        int expectedEosCount = node.getIncomingEdges().size();
        int receivedEosCount = 0;

        while (receivedEosCount < expectedEosCount) {
            BatchMessage<?> msg = myInputQueue.take();

            if (msg.isEos()) {
                receivedEosCount++;
            } else {
                // Process the microbatch
                List resultBatch = payload.processBatch(msg.payload());

                // FIX: Propagate DATA only if not empty. 
                // BUT, if this is a filter, it MUST NOT block the pipeline.
                if (!resultBatch.isEmpty()) {
                    broadcast(downstreamQueues, BatchMessage.data(resultBatch));
                }
            }
        }

        // TERMINATION:
        // When we exit the loop, we have received EOS from all parents.
        // We MUST signal EOS to all children to trigger their termination.
        if (!(payload instanceof SinkOp)) {
            broadcast(downstreamQueues, BatchMessage.eos());
        }
    }

    private void broadcast(List<BlockingQueue<BatchMessage<?>>> queues, BatchMessage<?> msg) throws InterruptedException {
        for (BlockingQueue<BatchMessage<?>> queue : queues) {
            queue.put(msg); // Blocks if the downstream queue is full (Backpressure)
        }
    }
}