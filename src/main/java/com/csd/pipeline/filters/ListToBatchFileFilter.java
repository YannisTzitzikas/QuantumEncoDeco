package com.csd.pipeline.filters;

import com.csd.core.event.EventBus;
import com.csd.core.model.Message;
import com.csd.core.model.uri.TripleComponent;
import com.csd.core.pipeline.AbstractFilter;
import com.csd.core.pipeline.InputPort;
import com.csd.core.pipeline.OutputPort;
import com.csd.core.pipeline.PortBindings;
import com.csd.core.pipeline.StreamPolicy;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class ListToBatchFileFilter extends AbstractFilter {

    public static final InputPort<Set<TripleComponent>> IN =
        new InputPort<>("List");
    public static final OutputPort<Path> OUT =
        new OutputPort<>("path");

    private final AtomicInteger batchCounter = new AtomicInteger(0);
    private final Path tempDir;

    public ListToBatchFileFilter(PortBindings bindings, EventBus bus) throws Exception {
        super(Arrays.asList(IN), Arrays.asList(OUT), bindings, new StreamPolicy(), bus);
        this.tempDir = Files.createTempDirectory("batch_files");
        logger.info("Created temp directory: {}", tempDir);
    }

    @Override
    protected void process(Batch in, Emitter out) throws Exception {
        Message<Set<TripleComponent>> msg = in.pop(IN);
        if (msg.getKind() == Message.MessageKind.EOS) {
            out.emit(OUT, Message.eos());
            return;
        }

        Set<TripleComponent> set = msg.getPayload();
        Comparator<TripleComponent> comparator = Comparator.comparing(TripleComponent::getValue);
        
        SortedSet<TripleComponent> sortedSet = new TreeSet<>(comparator);
        sortedSet.addAll(set);


        int batchNum = batchCounter.getAndIncrement();
        Path batchFile = tempDir.resolve("batch_" + batchNum);

        try (BufferedWriter writer = Files.newBufferedWriter(batchFile)) {
            for (TripleComponent entry : sortedSet) {
                writer.write(entry.getValue() + " " + entry.getRole().toString());
                writer.newLine();
            }
        }

        logger.info("Written batch {} with {} entries to {}", batchNum, set.size(), batchFile);
        out.emit(OUT, Message.data(batchFile));
    }

    @Override
    protected void onStop() {
        try {
            Files.deleteIfExists(tempDir);
        } catch (Exception e) {
            logger.warn("Failed to cleanup temp directory: {}", e.getMessage());
        }
        super.onStop();
    }
}