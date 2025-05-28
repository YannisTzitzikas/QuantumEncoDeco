package Ewritters;

import java.util.List;

import Ctransformers.StatisticsCollector;
import Ctransformers.TripleComponent;
import Ctransformers.URITriple;
import Ctransformers.encode.IEncoder;
import Ctransformers.encode.R2Encoder;

public class R2Writer implements IWriter {
    @Override
    public int write(List<URITriple> triples, IEncoder<?> encoder, StatisticsCollector collector ,String outputFile) {

        if(!(encoder instanceof R2Encoder))
        {
            throw new IllegalArgumentException("Encoder type missmatch.");
        }

        R2Encoder enco = (R2Encoder) encoder; 

        int totalMappingsEntities = encoder.getMappings(TripleComponent.SUBJECT).size();
        int totalMappingsPredicates = encoder.getMappings(TripleComponent.PREDICATE).size();

        int bitsNeededForEntities = bitCount(totalMappingsEntities);
        int bitsNeededForPredicates = bitCount(totalMappingsPredicates);
        
        collector.setBitsPerEntity(bitsNeededForEntities);
        collector.setBitsPerPredicate(bitsNeededForPredicates);
        
        EWritter writer = new EWritter(outputFile);

        for (URITriple triple : triples) {
            Integer s = enco.getEncoded(triple.getSubject(), TripleComponent.SUBJECT);
            Integer o = enco.getEncoded(triple.getObject(),  TripleComponent.OBJECT);
            Integer p = enco.getEncoded(triple.getPredicate(),  TripleComponent.PREDICATE);

            if (s == null || o == null || p == null) {
                throw new IllegalStateException("Failed to encode one or more components.");
            }

            String encodedLine = getBinaryRepresentation(s, bitsNeededForEntities) +
                                 getBinaryRepresentation(p, bitsNeededForPredicates) +
                                 getBinaryRepresentation(o, bitsNeededForEntities) + "\n";

            writer.write(encodedLine);
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
