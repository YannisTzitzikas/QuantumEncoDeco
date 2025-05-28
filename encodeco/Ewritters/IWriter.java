package Ewritters;

import java.util.List;

import Ctransformers.StatisticsCollector;
import Ctransformers.URITriple;
import Ctransformers.encode.IEncoder;

public interface IWriter {
    public int write(List<URITriple> triples, IEncoder<?> encoder, StatisticsCollector collector ,String outputFile);
}
