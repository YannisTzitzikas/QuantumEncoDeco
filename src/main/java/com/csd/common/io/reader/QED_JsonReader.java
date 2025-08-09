package com.csd.common.io.reader;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

/**
 * Reads a JSON configuration and converts it to a canonical, format-agnostic structure:
 * - JsonObject -> Map<String, Object> (LinkedHashMap to preserve order)
 * - JsonArray  -> List<Object>
 * - JsonPrimitive (number) -> BigDecimal
 * - JsonPrimitive (boolean) -> Boolean
 * - JsonPrimitive (string) -> String
 * - JsonNull -> null
 *
 * Designed to be independent of validation and business logic.
 */
public final class QED_JsonReader implements IReader {

    @Override
    public Map<String, Object> read(File file) throws Exception {
        return read(() -> {
            try {
                return new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
                );
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
    
    @Override
    public Map<String, Object> read(Path filePath) throws Exception {
        return read(() -> {
            try {
                return Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Override
    public Map<String, Object> read(String fileStr) throws Exception {
        return read(() -> new StringReader(fileStr));
    }

    @Override
    public Map<String, Object> read(Supplier<Reader> readerSupplier) throws Exception {
        try (Reader r = readerSupplier.get()) {
            JsonReader jr = new JsonReader(r);
            JsonElement root = JsonParser.parseReader(jr);

            if (!root.isJsonObject()) {
                throw new IllegalArgumentException("Top-level JSON must be an object");
            }
            return toMap(root.getAsJsonObject());
        }
    }

    // ---- Convers  ion helpers ----

    private static Map<String, Object> toMap(JsonObject obj) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
            out.put(e.getKey(), toJava(e.getValue()));
        }
        return out;
    }

    private static List<Object> toList(JsonArray arr) {
        List<Object> out = new ArrayList<>(arr.size());
        for (JsonElement el : arr) {
            out.add(toJava(el));
        }
        return out;
    }

    private static Object toJava(JsonElement el) {
        if (el == null || el.isJsonNull()) return null;

        if (el.isJsonObject()) return toMap(el.getAsJsonObject());
        if (el.isJsonArray()) return toList(el.getAsJsonArray());

        if (el.isJsonPrimitive()) {
            JsonPrimitive p = el.getAsJsonPrimitive();
            if (p.isBoolean()) return p.getAsBoolean();
            if (p.isString()) return p.getAsString();
            if (p.isNumber()) {
                try {
                    return p.getAsBigDecimal();
                } catch (NumberFormatException ex) {
                    return p.getAsString(); // fallback to string
                }
            }
        }

        return el.toString(); // defensive fallback
    }
}
