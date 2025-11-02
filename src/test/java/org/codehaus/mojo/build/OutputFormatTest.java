package org.codehaus.mojo.build;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Created by conni on 11/10/16.
 */
class OutputFormatTest {

    @Test
    void defaultIsPropertiesFormat() {
        OutputFormat outputFormat = OutputFormat.getOutputFormatFor("illegal");

        assertInstanceOf(PropertiesOutputFormat.class, outputFormat);
    }

    @Test
    void jsonForForDotJson() {
        OutputFormat outputFormat = OutputFormat.getOutputFormatFor("file.json");

        assertInstanceOf(JsonOutputFormat.class, outputFormat);
    }

    @Test
    void propertiesForForDotProperties() {
        OutputFormat outputFormat = OutputFormat.getOutputFormatFor("file.properties");

        assertInstanceOf(PropertiesOutputFormat.class, outputFormat);
    }
}
