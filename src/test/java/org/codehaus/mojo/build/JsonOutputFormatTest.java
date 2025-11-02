package org.codehaus.mojo.build;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
    void writesJson() throws Exception {
        String s = writePropertiesToString();

        Map<String, Object> map = gson.fromJson(s, Map.class);
        assertThat(map.size(), is(2));
        assertThat(map.get("key0"), is((Object) "value0"));
        assertThat(map.get("key1"), is((Object) "value1"));
    }

    private String writePropertiesToString() throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        outputFormat.write(properties, bytesOut);
        return new String(bytesOut.toByteArray(), "UTF-8");
    }
}
