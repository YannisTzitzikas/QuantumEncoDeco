package Ewritters.triple;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class CSVTripleWriter implements ITripleWriter {
    private final BufferedWriter writer;
    private boolean headerWritten = false;

    public CSVTripleWriter(OutputStream out) {
        this.writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
    }

    @Override
    public void write(String subject, String predicate, String object) throws IOException {
        if (!headerWritten) {
            writer.write("Subject,Predicate,Object\n");
            headerWritten = true;
        }
        writer.write(escapeCsv(subject) + "," + 
                    escapeCsv(predicate) + "," + 
                    escapeCsv(object) + "\n");
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}