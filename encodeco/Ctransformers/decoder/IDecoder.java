package Ctransformers.decoder;

import java.io.IOException;

import Ewritters.triple.ITripleWriter;

public interface IDecoder {
    void loadMappings(String mappingFilePath) throws IOException;
    void decodeFile(String encodedFilePath, String outputCsvPath) throws IOException;
    void decodeFile(String encodedFilePath, ITripleWriter writer) throws IOException;
}
