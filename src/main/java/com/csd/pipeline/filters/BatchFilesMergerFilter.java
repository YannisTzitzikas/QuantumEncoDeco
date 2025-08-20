package com.csd.pipeline.filters;

import com.csd.core.event.EventBus;
import com.csd.core.model.Message;
import com.csd.core.pipeline.AbstractFilter;
import com.csd.core.pipeline.BarrierPolicy;
import com.csd.core.pipeline.InputPort;
import com.csd.core.pipeline.OutputPort;
import com.csd.core.pipeline.PortBindings;
import com.csd.events.UniqueEntityEvent;
import com.csd.events.UniquePredicateEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BatchFilesMergerFilter extends AbstractFilter {

    public static final InputPort<Path> IN = new InputPort<>("batch-dir");
    public static final OutputPort<Path> OUT = new OutputPort<>("mappings-file-path");

    private static final Pattern BATCH_PATTERN = Pattern.compile("batch_\\d+");

    public BatchFilesMergerFilter(PortBindings bindings, EventBus bus) {
        super(Arrays.asList(IN), Arrays.asList(OUT), bindings, new BarrierPolicy(), bus);
    }

    @Override
    protected void process(Batch in, Emitter out) throws Exception {
        Message<Path> msg = in.pop(IN);
        if (msg.getKind() == Message.MessageKind.EOS) {
            out.emit(OUT, Message.eos());
            return;
        }

        Path batchDir = msg.getPayload().getParent();

        // Collect all batch files matching the pattern
        List<Path> batchFiles;
        try (java.util.stream.Stream<Path> stream = Files.list(batchDir)) {
            batchFiles = stream
                    .filter(p -> BATCH_PATTERN.matcher(p.getFileName().toString()).matches())
                    .sorted() // optional: ensures deterministic order
                    .collect(Collectors.toList());
        }

        if (batchFiles.isEmpty()) {
            throw new IllegalStateException("No batch files found in " + batchDir);
        }

        // Prepare readers for each batch
        class BatchReader {
            final java.io.BufferedReader br;
            String current;

            BatchReader(Path file, int index) throws java.io.IOException {
                this.br = Files.newBufferedReader(file);
                advance();
            }

            boolean advance() throws java.io.IOException {
                current = br.readLine();
                return current != null;
            }

            void close() throws java.io.IOException {
                br.close();
            }
        }

        List<BatchReader> readers = new ArrayList<>();
        for (int i = 0; i < batchFiles.size(); i++) {
            readers.add(new BatchReader(batchFiles.get(i), i));
        }

        // Min-heap ordered by current string
        java.util.PriorityQueue<BatchReader> heap = new java.util.PriorityQueue<>(
                Comparator.<BatchReader, String>comparing(r -> r.current));

        for (BatchReader r : readers) {
            if (r.current != null)
                heap.add(r);
        }

        // Output file
        Path outputFile = Paths.get("results").resolve("merged_mappings.txt");
        try (java.io.BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            String lastWritten = null;
            int id = 0;

            while (!heap.isEmpty()) {
                BatchReader r = heap.poll();
                String s = r.current;

                // Split into value and role
                int sepIndex = s.lastIndexOf(' ');
                if (sepIndex == -1) {
                    throw new IllegalStateException("Invalid batch line, no role found: " + s);
                }
                String componentValue = s.substring(0, sepIndex);
                String roleStr = s.substring(sepIndex + 1);

                // Deduplicate by component value only
                if (lastWritten == null || !lastWritten.equals(componentValue)) {
                    writer.write(componentValue);
                    writer.write(' ');
                    writer.write(Integer.toString(id++));
                    writer.newLine();
                    lastWritten = componentValue;

                    // Publish event based on role
                    if (!"PREDICATE".equals(roleStr)) {
                        eventBus.get().publish(new UniqueEntityEvent());
                    } else {
                        eventBus.get().publish(new UniquePredicateEvent());
                    }
                }

                if (r.advance()) {
                    heap.add(r);
                }
            }

        } finally {
            for (BatchReader r : readers) {
                try {
                    r.close();
                } catch (Exception ignore) {
                }
            }
        }

        // Emit the path to the merged file
        out.emit(OUT, Message.data(outputFile));
        onStop();
    }

}