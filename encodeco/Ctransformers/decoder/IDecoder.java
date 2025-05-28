package Ctransformers.decoder;

import java.io.IOException;

public interface IDecoder {
    void loadMappings(String mappingFilePath) throws IOException;
    void decodeFile(String encodedFilePath, String outputCsvPath) throws IOException;
}
