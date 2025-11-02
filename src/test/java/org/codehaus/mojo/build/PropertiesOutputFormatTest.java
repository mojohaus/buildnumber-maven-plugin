package org.codehaus.mojo.build;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by conni on 11/10/16.
 */
class PropertiesOutputFormatTest {

    private OutputFormat outputFormat = new PropertiesOutputFormat();

    private Properties properties = new Properties();

    @BeforeEach
    void before() {
        properties.put("key0", "value0");
        properties.put("key1", "value1");
    }

    @Test
    void handlesDotProperties() {
        assertTrue(outputFormat.handles("file.properties"));
    }

    @Test
    void doesNotHandleNonProperties() {
        assertFalse(outputFormat.handles("file.other"));
    }

    @Test
    void writesProperties() throws Exception {
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
