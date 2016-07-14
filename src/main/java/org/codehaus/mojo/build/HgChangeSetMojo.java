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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.log.ScmLogDispatcher;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * Goal which sets project properties for changeSet and changeSetDate from the current Mercurial repository.
 *
 * @author Tomas Pollak
 * @since 1.0-beta-4
 */
@Mojo( name = "hgchangeset", defaultPhase = LifecyclePhase.INITIALIZE, requiresProject = true, threadSafe = true )
public class HgChangeSetMojo
    extends AbstractMojo
{
    /**
     * Whether to skip this execution.
     *
     * @since 1.3
     */
    @Parameter( property = "maven.buildNumber.skip", defaultValue = "false" )
    private boolean skip;

    private ScmLogDispatcher logger = new ScmLogDispatcher();

    /**
     * The maven project.
     */
    @Parameter( defaultValue = "${project}", required = true, readonly = true )
    private MavenProject project;

    /**
     * Local directory to be used to issue SCM actions
     *
     * @since 1.0
     */
    @Parameter( property = "maven.changeSet.scmDirectory", defaultValue = "${basedir}" )
    private File scmDirectory;

    private void checkResult( ScmResult result )
        throws MojoExecutionException
    {
        if ( !result.isSuccess() )
        {
            getLog().debug( "Provider message:" );
            getLog().debug( result.getProviderMessage() == null ? "" : result.getProviderMessage() );
            getLog().debug( "Command output:" );
            getLog().debug( result.getCommandOutput() == null ? "" : result.getCommandOutput() );
            throw new MojoExecutionException( "Command failed."
                + StringUtils.defaultString( result.getProviderMessage() ) );
        }
    }

    public void execute()
        throws MojoExecutionException
    {
        if ( skip )
        {
            getLog().info( "Skipping execution." );
            return;
        }

        try
        {
            String previousChangeSet = getChangeSetProperty();
            String previousChangeSetDate = getChangeSetDateProperty();
            String previousLastChangeSetInFolder = getLastChangeSetInFolderProperty();
            String previousLastChangeSetDateInFolder = getLastChangeSetDateInFolderProperty();
            if ( previousChangeSet == null || previousChangeSetDate == null
                    || previousLastChangeSetInFolder == null || previousLastChangeSetDateInFolder == null)
            {
                String changeSet = getChangeSet();
                String changeSetDate = getChangeSetDate();
                String lastChangeSetInFolder = getLastChangeSetInFolder();
                String lastChangeSetDateInFolder = getLastChangeSetDateInFolder();
                getLog().info( "Setting Mercurial Changeset: " + changeSet );
                getLog().info( "Setting Mercurial Changeset Date: " + changeSetDate );
                getLog().info( "Setting Mercurial Last Changeset in Folder: " + lastChangeSetInFolder );
                getLog().info( "Setting Mercurial Last Changeset Date in Folder: " + lastChangeSetDateInFolder );
                setChangeSetProperty( changeSet );
                setChangeSetDateProperty( changeSetDate );
                setLastChangeSetInFolderProperty( lastChangeSetInFolder );
                setLastChangeSetDateInFolderProperty( lastChangeSetDateInFolder );
            }
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "SCM Exception", e );
        }
    }

    protected String getHgCommandOutput(String[] command)
            throws ScmException, MojoExecutionException
    {
        HgOutputConsumer consumer = new HgOutputConsumer( logger );
        ScmResult result = HgUtils.execute( consumer, logger, scmDirectory, command );
        checkResult( result );
        return consumer.getOutput();
    }

    protected String getChangeSet()
            throws ScmException, MojoExecutionException
    {
        return getHgCommandOutput(new String[] { "id", "-i" });
    }

    protected String getChangeSetDate()
            throws ScmException, MojoExecutionException
    {
        return getHgCommandOutput(new String[] { "log", "-r", ".", "--template",
                "\"{date|isodate}\"" } );
    }

    protected String getLastChangeSetInFolder()
            throws ScmException, MojoExecutionException
    {
        return getHgCommandOutput(new String[] { "log", "-l1", "--template", "\"{node|short}\"", "." });
    }

    protected String getLastChangeSetDateInFolder()
            throws ScmException, MojoExecutionException
    {
        return getHgCommandOutput(new String[] { "log", "-l1", "--template", "\"{date|isodate}\"", "." });
    }


    protected String getChangeSetDateProperty()
    {
        return getProperty( "changeSetDate" );
    }

    protected String getChangeSetProperty()
    {
        return getProperty( "changeSet" );
    }

    protected String getLastChangeSetDateInFolderProperty()
    {
        return getProperty( "lastChangeSetDateInFolder" );
    }

    protected String getLastChangeSetInFolderProperty()
    {
        return getProperty( "lastChangeSetInFolder" );
    }

    protected String getProperty( String property )
    {
        return project.getProperties().getProperty( property );
    }

    private void setChangeSetDateProperty( String changeSetDate )
    {
        setProperty( "changeSetDate", changeSetDate );
    }

    private void setChangeSetProperty( String changeSet )
    {
        setProperty( "changeSet", changeSet );
    }

    private void setLastChangeSetDateInFolderProperty( String changeSetDate )
    {
        setProperty( "lastChangeSetDateInFolder", changeSetDate );
    }

    private void setLastChangeSetInFolderProperty( String changeSet )
    {
        setProperty( "lastChangeSetInFolder", changeSet );
    }

    private void setProperty( String property, String value )
    {
        if ( value != null )
        {
            project.getProperties().put( property, value );
        }
    }


}
