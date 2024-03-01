package org.codehaus.mojo.build;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

public class JsonOutputFormat extends OutputFormat {
    @Override
    public boolean handles(String fileName) {
        return fileName.endsWith(".json");
    }

    @Override
    public void write(Properties props, OutputStream out) throws IOException {
        Gson gson = new Gson();
        try (JsonWriter jsonWriter = gson.newJsonWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            jsonWriter.beginObject();
            for (Object key : props.keySet()) {
                jsonWriter.name((String) key);
                jsonWriter.value(props.getProperty((String) key));
            }
            jsonWriter.endObject();
            jsonWriter.flush();
        }
    }
}
