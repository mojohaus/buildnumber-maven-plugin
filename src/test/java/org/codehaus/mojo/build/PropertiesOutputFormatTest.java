package org.codehaus.mojo.build;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by conni on 11/10/16.
 */
public class PropertiesOutputFormatTest {

    private OutputFormat outputFormat = new PropertiesOutputFormat();

    private Properties properties = new Properties();

    @Before
    public void before() {
        properties.put("key0", "value0");
        properties.put("key1", "value1");
    }

    @Test
    public void handlesDotProperties() {
        assertTrue(outputFormat.handles("file.properties"));
    }

    @Test
    public void doesNotHandleNonProperties() {
        assertFalse(outputFormat.handles("file.other"));
    }

    @Test
    public void writesProperties() throws IOException {
        byte[] serialized = writeProperties();

        Properties deserializedProperties = new Properties();
        deserializedProperties.load(new ByteArrayInputStream(serialized));

        assertThat(deserializedProperties, is(properties));
    }

    private byte[] writeProperties() throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        outputFormat.write(properties, bytesOut);
        return bytesOut.toByteArray();
    }
}