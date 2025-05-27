package Ewritters;

import java.util.List;

import Ctransformers.URITriple;
import Ctransformers.encode.IEncoder;
import Ctransformers.encode.R2Encoder;

public class R2Writer implements IWriter {
    @Override
    public int write(List<URITriple> triples, IEncoder<?> encoder, String outputFile) {

        if(!(encoder instanceof R2Encoder))
        {
            throw new IllegalArgumentException("Encoder type missmatch.");
        }

        R2Encoder enco = (R2Encoder) encoder; 

        int totalMappings = encoder.getMappings().size();
        int bitsNeeded = bitCount(totalMappings);

        EWritter writer = new EWritter(outputFile);

        for (URITriple triple : triples) {
            Integer s = enco.getEncoded(triple.getSubject());
            Integer o = enco.getEncoded(triple.getObject());
            Integer p = enco.getEncoded(triple.getPredicate());

            if (s == null || o == null || p == null) {
                throw new IllegalStateException("Failed to encode one or more components.");
            }

            writer.write(getBinaryRepresentation(s, bitsNeeded) +
                            getBinaryRepresentation(p, bitsNeeded) +
                            getBinaryRepresentation(o, bitsNeeded) + "\n");
        }

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
