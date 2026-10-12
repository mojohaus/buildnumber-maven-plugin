package org.codehaus.mojo.build;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by conni on 11/10/16.
 */
class JsonOutputFormatTest {

    private OutputFormat outputFormat = new JsonOutputFormat();

    private Properties properties = new Properties();

    private Gson gson = new Gson();

    @BeforeEach
    void before() {
        properties.put("key0", "value0");
        properties.put("key1", "value1");
    }

    @Test
    void handlesDotJson() {
        assertTrue(outputFormat.handles("file.json"));
    }

    @Test
    void doesNotHandleNonJson() {
        assertFalse(outputFormat.handles("file.other"));
    }

    @Test
    void writesJson() throws IOException {
        String s = writePropertiesToString();

        Map<String, Object> map = gson.fromJson(s, Map.class);
        assertEquals(2, map.size());
        assertEquals("value0", map.get("key0"));
        assertEquals("value1", map.get("key1"));
    }

    private String writePropertiesToString() throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        outputFormat.write(properties, bytesOut);
        return new String(bytesOut.toByteArray(), "UTF-8");
    }
}
