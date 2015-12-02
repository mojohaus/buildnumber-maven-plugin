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

import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * This mojo is designed to give you a timestamp available through one or more properties. Only a single timestamp is
 * created for each execution of the mojo. This timestamp can be format into one or more strings which are then saved to
 * properties.
 *
 * @author pgier
 * @version $Id$
 * @since 1.0-beta-5
 * @description create a timestamp property
 */
@Mojo( name = "create-timestamp", defaultPhase = LifecyclePhase.INITIALIZE, requiresProject = true, threadSafe = true )
public class CreateTimestampMojo
    extends AbstractMojo
{
    /**
     * Whether to skip this execution.
     *
     * @since 1.3
     */
    @Parameter( property = "maven.buildNumber.skip", defaultValue = "false" )
    private boolean skip;

    /**
     * The maven project.
     */
    @Parameter( defaultValue = "${project}", required = true, readonly = true )
    private MavenProject project;

    /**
     * Contains the full list of projects in the reactor.
     */
    @Parameter( defaultValue = "${reactorProjects}", required = true, readonly = true )
    private List<MavenProject> reactorProjects;

    /**
     * You can rename the timestamp property name to another property name if desired.
     */
    @Parameter( property = "maven.buildNumber.timestampPropertyName", defaultValue = "timestamp" )
    private String timestampPropertyName;

    /**
     * Apply this java.text.SimpleDateFormat to the timestamp. By default, no formatting is done but the raw number
     * value (milliseconds since January 1, 1970, 00:00:00 GMT) is used.
     */
    @Parameter( property = "maven.buildNumber.timestampFormat", defaultValue = "" )
    private String timestampFormat;

    /**
     * The timezone of the generated timestamp.
     * If blank will default to {@link TimeZone#getDefault()}
     */
    @Parameter(property = "maven.buildNumber.timestampTimeZone", defaultValue = "")
    private String timezone;

    public void execute()
    {
        if ( skip )
        {
            getLog().info( "Skipping execution." );
            return;
        }

        String timestampString = project.getProperties().getProperty( timestampPropertyName );

        // Check if the plugin has already run in the current build.
        if ( timestampString != null )
        {
            getLog().debug( "Using previously created timestamp." );
            return;
        }

        timestampString = Utils.createTimestamp( timestampFormat, timezone );

        getLog().debug( "Storing timestamp property: " + timestampPropertyName + " " + timestampString );

        for ( MavenProject project : reactorProjects )
        {
            project.getProperties().setProperty( timestampPropertyName, timestampString );
        }
    }
}
