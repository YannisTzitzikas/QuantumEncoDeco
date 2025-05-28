package Ewritters;

import java.util.List;

import Ctransformers.StatisticsCollector;
import Ctransformers.TripleComponent;
import Ctransformers.URITriple;
import Ctransformers.encode.IEncoder;
import Ctransformers.encode.R1Encoder;

public class R1Writer implements IWriter {
    @Override
    public int write(List<URITriple> triples, IEncoder<?> encoder, StatisticsCollector collector ,String outputFile) {

        if(!(encoder instanceof R1Encoder))
        {
            throw new IllegalArgumentException("Encoder type missmatch.");
        }

        R1Encoder enco = (R1Encoder) encoder; 

        int totalMappings = encoder.getMappings(TripleComponent.OBJECT).size();
        int bitsNeeded = bitCount(totalMappings);

        collector.setBitsPerEntity(bitsNeeded);
        collector.setBitsPerPredicate(bitsNeeded);

        EWritter writer = new EWritter(outputFile);

        for (URITriple triple : triples) {
            Integer s = enco.getEncoded(triple.getSubject(), TripleComponent.SUBJECT);
            Integer o = enco.getEncoded(triple.getObject(),  TripleComponent.PREDICATE);
            Integer p = enco.getEncoded(triple.getPredicate(),  TripleComponent.OBJECT);

            if (s == null || o == null || p == null) {
                throw new IllegalStateException("Failed to encode one or more components.");
            }

            writer.write(getBinaryRepresentation(s, bitsNeeded) +
                            getBinaryRepresentation(p, bitsNeeded) +
                            getBinaryRepresentation(o, bitsNeeded) + "\n");
        }

        writer.close();

        return triples.size();
    }

    private int bitCount(int count) {
        if (count == 0) {
            return 1; 
        }
        return 32 - Integer.numberOfLeadingZeros(count);
    }

    private String getBinaryRepresentation(int number, int bitsNeeded) {
        String binaryString = Integer.toBinaryString(number);
        return String.format("%" + bitsNeeded + "s", binaryString).replace(' ', '0');
    }
}
