package Ewritters;

import java.util.List;

import Ctransformers.URITriple;
import Ctransformers.encode.IEncoder;

public interface IWriter {
    int write(List<URITriple> triples, IEncoder<?> encoder, String outputFile);
}
