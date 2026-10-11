package org.codehaus.mojo.build;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by conni on 11/10/16.
 */
class OutputFormatTest {

    @Test
    void defaultIsPropertiesFormat() {
        OutputFormat outputFormat = OutputFormat.getOutputFormatFor("illegal");

        assertTrue(outputFormat instanceof PropertiesOutputFormat);
    }

    @Test
    void jsonForForDotJson() {
        OutputFormat outputFormat = OutputFormat.getOutputFormatFor("file.json");

        assertTrue(outputFormat instanceof JsonOutputFormat);
    }

    @Test
    void propertiesForForDotProperties() {
        OutputFormat outputFormat = OutputFormat.getOutputFormatFor("file.properties");

        assertTrue(outputFormat instanceof PropertiesOutputFormat);
    }
}
