package Ewritters.encoding;

import java.util.List;

import Ctransformers.StatisticsCollector;
import Ctransformers.URITriple;
import Ctransformers.encode.IEncoder;

public interface IEncodeWriter {
    public int write(List<URITriple> triples, IEncoder<?> encoder, StatisticsCollector collector ,String outputFile);
}
