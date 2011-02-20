package org.codehaus.mojo.build;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.log.ScmLogDispatcher;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgConsumer;
import org.codehaus.plexus.util.StringUtils;

/**
 * Goal which sets project properties for changeSet and changeSetDate from the
 * current Mercurial repository.
 * 
 * @author Tomas Pollak
 * @goal hgchangeset
 * @requiresProject
 * @since 1.0-beta-4
 */
public class HgChangeSetMojo
    extends AbstractMojo
{

    private ScmLogDispatcher logger = new ScmLogDispatcher();

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @readonly
     */
    private MavenProject project;

    /**
     * Local directory to be used to issue SCM actions
     * 
     * @parameter expression="${maven.changeSet.scmDirectory}" default-value="${basedir}
     * @since 1.0
     */
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
        try
        {
            String previousChangeSet = getChangeSetProperty();
            String previousChangeSetDate = getChangeSetDateProperty();
            if ( previousChangeSet == null || previousChangeSetDate == null )
            {
                String changeSet = getChangeSet();
                String changeSetDate = getChangeSetDate();
                getLog().info( "Setting Mercurial Changeset: " + changeSet );
                getLog().info( "Setting Mercurial Changeset Date: " + changeSetDate );
                setChangeSetProperty( changeSet );
                setChangeSetDateProperty( changeSetDate );
            }
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "SCM Exception", e );
        }
    }

    protected String getChangeSet()
        throws ScmException, MojoExecutionException
    {
        HgOutputConsumer consumer = new HgOutputConsumer( logger );
        ScmResult result = HgUtils.execute( consumer, logger, scmDirectory, new String[] { "id", "-i" } );
        checkResult( result );
        return consumer.getOutput();
    }

    protected String getChangeSetDate()
        throws ScmException, MojoExecutionException
    {
        HgOutputConsumer consumer = new HgOutputConsumer( logger );
        ScmResult result =
            HgUtils.execute( consumer, logger, scmDirectory, new String[] { "log", "-r", ".",
                "--template", "\"{date|isodate}\"" } );
        checkResult( result );
        return consumer.getOutput();
    }

    protected String getChangeSetDateProperty()
    {
        return getProperty( "changeSetDate" );
    }

    protected String getChangeSetProperty()
    {
        return getProperty( "changeSet" );
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

    private void setProperty( String property, String value )
    {
        if ( value != null )
        {
            project.getProperties().put( property, value );
        }
    }

    private static class HgOutputConsumer
        extends HgConsumer
    {

        private String output;

        private HgOutputConsumer( ScmLogger logger )
        {
            super( logger );
        }

        public void doConsume( ScmFileStatus status, String line )
        {
            output = line;
        }

        private String getOutput()
        {
            return output;
        }
    }    
}
