package Ewritters.triple;

import java.io.IOException;

public interface ITripleWriter extends AutoCloseable {
    void write(String subject, String predicate, String object) throws IOException;
    void close() throws IOException;
}