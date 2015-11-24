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
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.command.info.InfoItem;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

public abstract class AbstractScmMojo
    extends AbstractMojo
{
    /**
     * @since 1.0-beta-5
     */
    @Parameter( defaultValue = "${project.scm.connection}", alias = "readUrlScm", readonly = true )
    protected String scmConnectionUrl;

    /**
     * @since 1.0-beta-5
     */
    @Parameter( defaultValue = "${project.scm.developerConnection}", alias = "urlScm", readonly = true )
    protected String scmDeveloperConnectionUrl;

    /**
     * @since 1.4
     */
    @Parameter( defaultValue = "${project.scm.tag}", readonly = true )
    protected String scmTag;

    /**
     * The username that is used when connecting to the SCM system.
     *
     * @since 1.0-beta-1
     */
    @Parameter( property = "username" )
    protected String username;

    /**
     * The password that is used when connecting to the SCM system.
     *
     * @since 1.0-beta-1
     */
    @Parameter( property = "password" )
    protected String password;

    /**
     * Issue SCM actions at this local directory
     *
     * @since 1.0-beta-1
     */
    @Parameter( property = "maven.buildNumber.scmDirectory", defaultValue = "${basedir}" )
    protected File scmDirectory;

    /**
     * Max length of a revision id (GIT only)
     *
     * @since 1.1
     */
    @Parameter( property = "maven.buildNumber.shortRevisionLength", defaultValue = "0" )
    protected int shortRevisionLength = 0;

    /**
     * Setting this value allows the build to continue even in the event of an SCM failure. The value set will be used
     * as the revision string in the event of a failure to retrieve the revision it from the SCM.
     *
     * @since 1.0-beta-2
     */
    @Parameter( property = "maven.buildNumber.revisionOnScmFailure" )
    protected String revisionOnScmFailure;

    /**
     * whether to retrieve the revision for the last commit, or the last revision of the repository.
     *
     * @since 1.0-beta-2
     */
    @Parameter( property = "maven.buildNumber.useLastCommittedRevision", defaultValue = "false" )
    private boolean useLastCommittedRevision;

    /**
     * Whether to skip this execution.
     *
     * @since 1.3
     */
    @Parameter( property = "maven.buildNumber.skip", defaultValue = "false" )
    protected boolean skip;

    /**
     * Maven Settings
     *
     * @since 1.4
     */
    @Parameter( defaultValue = "${settings}", readonly = true )
    protected Settings settings;

    @Parameter( defaultValue = "${project}", required = true, readonly = true )
    protected MavenProject project;

    @Component
    protected ScmManager scmManager;

    /**
     * Maven Security Dispatcher
     *
     * @since 1.4
     */
    @Component( hint = "mng-4384" )
    private SecDispatcher securityDispatcher;

    /**
     * Load username password from settings.
     */
    private void loadInfosFromSettings( ScmProviderRepositoryWithHost repo )
    {
        if ( username == null || password == null )
        {
            String host = repo.getHost();

            int port = repo.getPort();

            if ( port > 0 )
            {
                host += ":" + port;
            }

            Server server = this.settings.getServer( host );

            if ( server != null )
            {
                setPasswordIfNotEmpty( repo, decrypt( server.getPassword(), host ) );

                setUserIfNotEmpty( repo, server.getUsername() );
            }
        }
    }

    private String decrypt( String str, String server )
    {
        try
        {
            return securityDispatcher.decrypt( str );
        }
        catch ( SecDispatcherException e )
        {
            getLog().warn( "Failed to decrypt password/passphrase for server " + server + ", using auth token as is" );
            return str;
        }
    }

    protected ScmRepository getScmRepository()
        throws ScmException
    {
        ScmRepository repository =
            scmManager.makeScmRepository( !StringUtils.isBlank( this.scmConnectionUrl ) ? scmConnectionUrl
                            : scmDeveloperConnectionUrl );

        ScmProviderRepository scmRepo = repository.getProviderRepository();

        if ( scmRepo instanceof ScmProviderRepositoryWithHost )
        {
            loadInfosFromSettings( (ScmProviderRepositoryWithHost) scmRepo );
        }

        setPasswordIfNotEmpty( scmRepo, password );

        setUserIfNotEmpty( scmRepo, username );

        return repository;
    }

    private void setPasswordIfNotEmpty( ScmProviderRepository repository, String password )
    {
        if ( !StringUtils.isEmpty( password ) )
        {
            repository.setPassword( password );
        }
    }

    private void setUserIfNotEmpty( ScmProviderRepository repository, String user )
    {
        if ( !StringUtils.isEmpty( user ) )
        {
            repository.setUser( user );
        }
    }

    protected void checkResult( ScmResult result )
        throws ScmException
    {
        if ( !result.isSuccess() )
        {
            // TODO: improve error handling
            getLog().error( "Provider message:" );

            getLog().error( result.getProviderMessage() );

            getLog().error( "Command output:" );

            getLog().error( result.getCommandOutput() );

            throw new ScmException( "Error!" );
        }
    }

    /**
     * Get info from scm.
     *
     * @param repository
     * @param fileSet
     * @return
     * @throws ScmException
     * @todo this should be rolled into org.apache.maven.scm.provider.ScmProvider and
     *       org.apache.maven.scm.provider.svn.SvnScmProvider
     */
    protected InfoScmResult info( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        CommandParameters commandParameters = new CommandParameters();

        // only for Git, we will make a test for shortRevisionLength parameter
        if ( GitScmProviderRepository.PROTOCOL_GIT.equals( scmManager.getProviderByRepository( repository ).getScmType() )
            && this.shortRevisionLength > 0 )
        {
            getLog().info( "ShortRevision tag detected. The value is '" + this.shortRevisionLength + "'." );
            if ( shortRevisionLength >= 0 && shortRevisionLength < 4 )
            {
                getLog().warn( "shortRevision parameter less then 4. ShortRevisionLength is relaying on 'git rev-parese --short=LENGTH' command, accordingly to Git rev-parse specification the LENGTH value is miminum 4. " );
            }
            commandParameters.setInt( CommandParameter.SCM_SHORT_REVISION_LENGTH, this.shortRevisionLength );
        }

        if ( !StringUtils.isBlank( scmTag ) && !"HEAD".equals( scmTag ) )
        {
            commandParameters.setScmVersion( CommandParameter.SCM_VERSION, new ScmTag( scmTag ) );
        }

        return scmManager.getProviderByRepository( repository ).info( repository.getProviderRepository(), fileSet,
                                                                      commandParameters );
    }

    protected String getScmRevision()
        throws ScmException
    {
        ScmRepository repository = getScmRepository();

        InfoScmResult scmResult = info( repository, new ScmFileSet( scmDirectory ) );

        if ( scmResult == null || scmResult.getInfoItems().isEmpty() )
        {
            return ( !StringUtils.isEmpty( revisionOnScmFailure ) ) ? revisionOnScmFailure : null;
        }

        checkResult( scmResult );

        InfoItem info = scmResult.getInfoItems().get( 0 );

        if ( useLastCommittedRevision )
        {
            return info.getLastChangedRevision();
        }

        return info.getRevision();
    }

}
