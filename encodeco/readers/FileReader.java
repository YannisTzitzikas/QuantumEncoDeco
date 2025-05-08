package readers;

import java.io.File;
import java.io.IOException;

public interface FileReader<T> {
    T parse(File file) throws IOException;
    void write(T data, File outputFile) throws IOException;
}