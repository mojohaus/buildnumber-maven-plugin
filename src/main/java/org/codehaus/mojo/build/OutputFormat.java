package org.codehaus.mojo.build;

/**
 * The MIT License
 *
 * Copyright (c) 2015 Learning Commons, University of Calgary
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public abstract class OutputFormat {
    static final OutputFormat DEFAULT_FORMAT = new PropertiesOutputFormat();

    private static final OutputFormat[] FORMATS =
            new OutputFormat[] {new JsonOutputFormat(), OutputFormat.DEFAULT_FORMAT};

    public static OutputFormat getOutputFormatFor(String fileName) {
        for (OutputFormat outputFormat : OutputFormat.FORMATS) {
            if (outputFormat.handles(fileName)) {
                return outputFormat;
            }
        }

        return OutputFormat.DEFAULT_FORMAT;
    }

    public abstract boolean handles(String fileName);

    public abstract void write(Properties props, OutputStream out) throws IOException;
}
