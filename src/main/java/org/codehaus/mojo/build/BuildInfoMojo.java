package org.codehaus.mojo.build;

/**
 * The MIT License
 *
 * Copyright (c) 2015 Codehaus
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.scm.ScmException;
import org.codehaus.plexus.util.StringUtils;

/**
 * This mojo discovers latest SCM revision and current timestamp, then write them to a java property file together with
 * a set of user provided properties.
 */
@Mojo( name = "build-info", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresProject = true, threadSafe = true )
public class BuildInfoMojo
    extends AbstractScmMojo
{

    /**
     * Java property name to store the project name under the output file
     *
     * @since 1.4
     */
    @Parameter( defaultValue = "name" )
    private String applicationPropertyName;

    /**
     * Application name
     *
     * @since 1.4
     */
    @Parameter( defaultValue = "${project.name}" )
    private String applicationName;

    /**
     * Java property name to store the project version under the output file
     *
     * @since 1.4
     */
    @Parameter( defaultValue = "version" )
    private String versionPropertyName;

    /**
     * Version
     *
     * @since 1.4
     */
    @Parameter( defaultValue = "${project.version}" )
    private String version;

    /**
     * Java property name to store the discovered SCM revision value under the output file
     *
     * @since 1.4
     */
    @Parameter( defaultValue = "revision" )
    private String revisionPropertyName;

    /**
     * Java property name to store the discovered timestamp value under the output file
     *
     * @since 1.4
     */
    @Parameter( defaultValue = "timestamp" )
    private String timestampPropertyName;

    /**
     * java.text.SimpleDateFormat for the discover timestamp, if not given use default java timestamp format
     *
     * @since 1.4
     */
    @Parameter
    private String timestampFormat;

    /**
     * Output directory
     *
     * @since 1.4
     */
    @Parameter( defaultValue = "${project.build.directory}/generated/buildinfo", required = true )
    private File outputDirectory;

    /**
     * Output file name
     *
     * @since 1.4
     */
    @Parameter( defaultValue = "build.properties", required = true )
    private String outputName;

    /**
     * Add output directory to java resource
     *
     * @since 1.4
     */
    @Parameter( defaultValue = "false" )
    private boolean addOutputDirectoryToResource;

    /**
     * Install/Deploy to Maven repository
     *
     * @since 1.4
     */
    @Parameter( defaultValue = "false" )
    private boolean attach;

    /**
     * Additional output files
     *
     * @since 1.4
     */
    @Parameter
    private List<File> outputFiles = new ArrayList<File>();

    /**
     * Additional properties to write out
     *
     * @since 1.4
     */
    @Parameter
    private Map<String, String> properties = new HashMap<String, String>();

    /**
     * Maven ProjectHelper.
     */
    @Component
    private MavenProjectHelper projectHelper;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Properties props = new Properties();
        props.put( this.applicationPropertyName, applicationName );
        props.put( this.versionPropertyName, version );
        props.put( this.timestampPropertyName, Utils.createTimestamp( this.timestampFormat ) );
        props.put( this.revisionPropertyName, this.getRevision() );
        for ( String key : properties.keySet() )
        {
            props.put( key, properties.get( key ) );
        }

        File outputFile = new File( outputDirectory, outputName );
        outputFiles.add( outputFile );

        for ( File file : outputFiles )
        {
            file.getParentFile().mkdirs();
            OutputStream os = null;
            try
            {
                os = new FileOutputStream( file );
                props.store( os, "Created by build system. Do not modify" );
            }
            catch ( IOException e )
            {
                throw new MojoFailureException( "Unable to store output to " + file, e );
            }
        }

        if ( attach )
        {
            projectHelper.attachArtifact( this.project, "properties", "build", outputFile );
        }

        if ( this.addOutputDirectoryToResource )
        {
            Resource resource = new Resource();
            resource.setDirectory( outputDirectory.getAbsolutePath() );

            project.addResource( resource );
        }
    }

    public String getRevision()
        throws MojoExecutionException
    {
        try
        {
            return this.getScmRevision();
        }
        catch ( ScmException e )
        {
            if ( !StringUtils.isEmpty( revisionOnScmFailure ) )
            {
                getLog().warn( "Cannot get the revision information from the scm repository, proceeding with "
                                   + "revision of " + revisionOnScmFailure + " : \n" + e.getLocalizedMessage() );

                return revisionOnScmFailure;
            }

            throw new MojoExecutionException( "Cannot get the revision information from the scm repository : \n"
                + e.getLocalizedMessage(), e );

        }
    }

}
