package org.codehaus.mojo.build;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public abstract class OutputFormat {
    static final OutputFormat DEFAULT_FORMAT = new PropertiesOutputFormat();

    private static final OutputFormat[] FORMATS = new OutputFormat[] {
            new JsonOutputFormat(),
            OutputFormat.DEFAULT_FORMAT
    };

    public static OutputFormat getOutputFormatFor(String fileName) {
        for(OutputFormat outputFormat : OutputFormat.FORMATS) {
            if( outputFormat.handles(fileName) ) {
                return outputFormat;
            }
        }

        return OutputFormat.DEFAULT_FORMAT;
    }

    public abstract boolean handles(String fileName);

    public abstract void write(Properties props, OutputStream out) throws IOException;
}
