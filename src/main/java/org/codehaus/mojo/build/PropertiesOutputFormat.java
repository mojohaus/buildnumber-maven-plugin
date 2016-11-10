package org.codehaus.mojo.build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class PropertiesOutputFormat extends OutputFormat {
    @Override
    public boolean handles(String fileName) {
        return fileName.endsWith(".properties");
    }

    @Override
    public void write(Properties props, OutputStream out) throws IOException {
        props.store( out, "Created by build system. Do not modify" );
    }
}
