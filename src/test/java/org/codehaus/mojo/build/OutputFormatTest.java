package org.codehaus.mojo.build;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by conni on 11/10/16.
 */
public class OutputFormatTest {

    @Test
    public void defaultIsPropertiesFormat() {
        OutputFormat outputFormat = OutputFormat.getOutputFormatFor("illegal");

        assertTrue(outputFormat instanceof PropertiesOutputFormat);
    }

    @Test
    public void jsonForForDotJson() {
        OutputFormat outputFormat = OutputFormat.getOutputFormatFor("file.json");

        assertTrue(outputFormat instanceof JsonOutputFormat);
    }

    @Test
    public void propertiesForForDotProperties() {
        OutputFormat outputFormat = OutputFormat.getOutputFormatFor("file.properties");

        assertTrue(outputFormat instanceof PropertiesOutputFormat);
    }
}
