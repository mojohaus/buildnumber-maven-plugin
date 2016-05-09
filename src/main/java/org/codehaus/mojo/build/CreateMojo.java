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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.info.InfoItem;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.command.update.UpdateScmResultWithRevision;
import org.apache.maven.scm.log.ScmLogDispatcher;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.git.gitexe.command.branch.GitBranchCommand;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgScmProvider;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

/**
 * This mojo is designed to give you a build number. So when you might make 100 builds of version 1.0-SNAPSHOT, you can
 * differentiate between them all.
 * <p>
 * The build number is based on the revision number retrieved from SCM. It is known to work with Subversion, GIT, and
 * Mercurial.
 * <p>
 * This mojo can also check to make sure that you have checked everything into SCM, before issuing the build number.
 * That behaviour can be suppressed, and then the latest local build number is used.
 * <p>
 * Build numbers are not automatically reflected in your artifact's filename, but can be added to the metadata. You can
 * access the build number in your pom with ${buildNumber}. You can also access ${timestamp} and the SCM branch of the
 * build (if applicable) in ${SCMBranch}
 * <p>
 * Note that there are several <code><strong>doFoo</strong></code> parameters. These parameters (doCheck, doUpdate, etc)
 * are the first thing evaluated. If there is no matching expression, we get the default-value. If there is (ie
 * <code>-Dmaven.buildNumber.doUpdate=false</code>), we get that value. So if the XML contains
 * <tt>&lt;doCheck&gt;true&lt;/doCheck&gt;</tt>, then normally that's the final value of the param in question. However,
 * this mojo reverses that behaviour, such that the command line parameters get the last say.
 *
 * @author <a href="mailto:woodj@ucalgary.ca">Julian Wood</a>
 * @version $Id$
 */
@Mojo( name = "create", defaultPhase = LifecyclePhase.INITIALIZE, requiresProject = true, threadSafe = true )
public class CreateMojo
    extends AbstractScmMojo
{
    private static final String DEFAULT_BRANCH_NAME = "UNKNOWN_BRANCH";

    /**
     * You can rename the buildNumber property name to another property name if desired.
     *
     * @since 1.0-beta-1
     */
    @Parameter( property = "maven.buildNumber.buildNumberPropertyName", defaultValue = "buildNumber" )
    private String buildNumberPropertyName;

    /**
     * You can rename the timestamp property name to another property name if desired.
     *
     * @since 1.0-beta-1
     */
    @Parameter( property = "maven.buildNumber.timestampPropertyName", defaultValue = "timestamp" )
    private String timestampPropertyName;

    /**
     * If this is made true, we check for modified files, and if there are any, we fail the build. Note that this used
     * to be inverted (skipCheck), but needed to be changed to allow releases to work. This corresponds to 'svn status'.
     *
     * @since 1.0-beta-1
     */
    @Parameter( property = "maven.buildNumber.doCheck", defaultValue = "false" )
    private boolean doCheck;

    /**
     * If this is made true, then the revision will be updated to the latest in the repo, otherwise it will remain what
     * it is locally. Note that this used to be inverted (skipUpdate), but needed to be changed to allow releases to
     * work. This corresponds to 'svn update'.
     *
     * @since 1.0-beta-1
     */
    @Parameter( property = "maven.buildNumber.doUpdate", defaultValue = "false" )
    private boolean doUpdate;

    /**
     * Specify a message as specified by java.text.MessageFormat. This triggers "items" configuration to be read
     *
     * @since 1.0-beta-1
     */
    @Parameter( property = "maven.buildNumber.format" )
    private String format;

    /**
     * Properties file to be created when "format" is not null and item has "buildNumber". See Usage for details
     *
     * @since 1.0-beta-2
     */
    @Parameter( defaultValue = "${basedir}/buildNumber.properties" )
    private File buildNumberPropertiesFileLocation;

    /**
     * Specify the corresponding items for the format message, as specified by java.text.MessageFormat. Special item
     * values are "scmVersion", "timestamp" and "buildNumber[digits]", where [digits] are optional digits added to the
     * end of the number to select a property.
     *
     * @since 1.0-beta-1
     */
    @Parameter
    private List<?> items;

    /**
     * The locale used for date and time formatting. The locale name should be in the format defined in
     * {@link Locale#toString()}. The default locale is the platform default returned by {@link Locale#getDefault()}.
     *
     * @since 1.0-beta-2
     */
    @Parameter( property = "maven.buildNumber.locale" )
    private String locale;

    /**
     * whether to retrieve the revision for the last commit, or the last revision of the repository.
     *
     * @since 1.0-beta-2
     */
    @Parameter( property = "maven.buildNumber.useLastCommittedRevision", defaultValue = "false" )
    private boolean useLastCommittedRevision;

    /**
     * Apply this java.text.MessageFormat to the timestamp only (as opposed to the <code>format</code> parameter).
     *
     * @since 1.0-beta-2
     */
    @Parameter( property = "maven.buildNumber.timestampFormat" )
    private String timestampFormat;

    /**
     * Selects alternative SCM provider implementations. Each map key denotes the original provider type as given in the
     * SCM URL like "cvs" or "svn", the map value specifies the provider type of the desired implementation to use
     * instead. In other words, this map configures a substitution mapping for SCM providers.
     *
     * @since 1.0-beta-3
     */
    @Parameter
    private Map<String, String> providerImplementations;

    /**
     * If set to true, will get the scm revision once for all modules of a multi-module project instead of fetching once
     * for each module.
     *
     * @since 1.0-beta-3
     */
    @Parameter( property = "maven.buildNumber.getRevisionOnlyOnce", defaultValue = "false" )
    private boolean getRevisionOnlyOnce;

    /**
     * You can rename the buildScmBranch property name to another property name if desired.
     *
     * @since 1.0-beta-4
     */
    @Parameter( property = "maven.buildNumber.scmBranchPropertyName", defaultValue = "scmBranch" )
    private String scmBranchPropertyName;


    // ////////////////////////////////////// internal maven components ///////////////////////////////////


    /**
     * Contains the full list of projects in the reactor.
     *
     * @since 1.0-beta-3
     */
    @Parameter( defaultValue = "${reactorProjects}", readonly = true, required = true )
    private List<MavenProject> reactorProjects;

    @Parameter( defaultValue = "${session}", readonly = true, required = true )
    private MavenSession session;


    // ////////////////////////////////////// internal variables ///////////////////////////////////

    private ScmLogDispatcher logger;

    private String revision;

    private boolean useScm;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( skip )
        {
            getLog().info( "Skipping execution." );
            return;
        }

        if ( providerImplementations != null )
        {
            for ( Entry<String, String> entry : providerImplementations.entrySet() )
            {
                String providerType = entry.getKey();
                String providerImplementation = entry.getValue();
                getLog().info( "Change the default '" + providerType + "' provider implementation to '"
                                   + providerImplementation + "'." );
                scmManager.setScmProviderImplementation( providerType, providerImplementation );
            }
        }
        Date now = Calendar.getInstance().getTime();
        if ( format != null )
        {
            if ( items == null )
            {
                throw new MojoExecutionException(
                                                  " if you set a format, you must provide at least one item, please check documentation " );
            }
            // needs to be an array
            // look for special values
            Object[] itemAry = new Object[items.size()];
            for ( int i = 0; i < items.size(); i++ )
            {
                Object item = items.get( i );
                if ( item instanceof String )
                {
                    String s = (String) item;
                    if ( s.equals( "timestamp" ) )
                    {
                        itemAry[i] = now;
                    }
                    else if ( s.startsWith( "scmVersion" ) )
                    {
                        useScm = true;
                        itemAry[i] = getRevision();
                    }
                    else if ( s.startsWith( "buildNumber" ) )
                    {
                        // check for properties file
                        File propertiesFile = this.buildNumberPropertiesFileLocation;

                        // create if not exists
                        if ( !propertiesFile.exists() )
                        {
                            try
                            {
                                if ( !propertiesFile.getParentFile().exists() )
                                {
                                    propertiesFile.getParentFile().mkdirs();
                                }
                                propertiesFile.createNewFile();
                            }
                            catch ( IOException e )
                            {
                                throw new MojoExecutionException( "Couldn't create properties file: " + propertiesFile,
                                                                  e );
                            }
                        }

                        Properties properties = new Properties();
                        String buildNumberString = null;
                        FileInputStream inputStream = null;
                        FileOutputStream outputStream = null;
                        try
                        {
                            // get the number for the buildNumber specified
                            inputStream = new FileInputStream( propertiesFile );
                            properties.load( inputStream );
                            buildNumberString = properties.getProperty( s );
                            if ( buildNumberString == null )
                            {
                                buildNumberString = "0";
                            }
                            int buildNumber = parseInt( buildNumberString );

                            // store the increment
                            properties.setProperty( s, String.valueOf( ++buildNumber ) );
                            outputStream = new FileOutputStream( propertiesFile );
                            properties.store( outputStream, "maven.buildNumber.plugin properties file" );

                            // use in the message (format)
                            itemAry[i] = new Integer( buildNumber );
                        }
                        catch ( NumberFormatException e )
                        {
                            throw new MojoExecutionException(
                                                              "Couldn't parse buildNumber in properties file to an Integer: "
                                                                  + buildNumberString );
                        }
                        catch ( IOException e )
                        {
                            throw new MojoExecutionException( "Couldn't load properties file: " + propertiesFile, e );
                        }
                        finally
                        {
                            IOUtil.close( inputStream );
                            IOUtil.close( outputStream );
                        }
                    }
                    else
                    {
                        itemAry[i] = item;
                    }
                }
                else
                {
                    itemAry[i] = item;
                }
            }

            revision = format( itemAry );
        }
        else
        {
            // Check if the plugin has already run.
            revision = project.getProperties().getProperty( this.buildNumberPropertyName );
            if ( this.getRevisionOnlyOnce && revision != null )
            {
                getLog().debug( "Revision available from previous execution" );
                return;
            }

            if ( doCheck )
            {
                // we fail if there are local mods
                checkForLocalModifications();
            }
            else
            {
                getLog().debug( "Checking for local modifications: skipped." );
            }
            if ( session.getSettings().isOffline() )
            {
                getLog().info( "maven is executed in offline mode, Updating project files from SCM: skipped." );
            }
            else
            {
                if ( doUpdate )
                {
                    // we update your local repo
                    // even after you commit, your revision stays the same until you update, thus this
                    // action
                    List<ScmFile> changedFiles = update();
                    for ( ScmFile file : changedFiles )
                    {
                        getLog().debug( "Updated: " + file );
                    }
                    if ( changedFiles.size() == 0 )
                    {
                        getLog().debug( "No files needed updating." );
                    }
                }
                else
                {
                    getLog().debug( "Updating project files from SCM: skipped." );
                }
            }
            revision = getRevision();
        }

        if ( project != null )
        {
            String timestamp = String.valueOf( now.getTime() );
            if ( timestampFormat != null )
            {
                timestamp = MessageFormat.format( timestampFormat, new Object[] { now } );
            }

            getLog().info( MessageFormat.format( "Storing buildNumber: {0} at timestamp: {1}", new Object[] { revision,
                               timestamp } ) );
            if ( revision != null )
            {
                project.getProperties().put( buildNumberPropertyName, revision );
            }
            project.getProperties().put( timestampPropertyName, timestamp );

            String scmBranch = getScmBranch();
            getLog().info( "Storing buildScmBranch: " + scmBranch );
            project.getProperties().put( scmBranchPropertyName, scmBranch );

            // Add the revision and timestamp properties to each project in the reactor
            if ( getRevisionOnlyOnce && reactorProjects != null )
            {
                Iterator<MavenProject> projIter = reactorProjects.iterator();
                while ( projIter.hasNext() )
                {
                    MavenProject nextProj = (MavenProject) projIter.next();
                    if ( revision != null )
                    {
                        nextProj.getProperties().put( this.buildNumberPropertyName, revision );
                    }
                    nextProj.getProperties().put( this.timestampPropertyName, timestamp );
                    nextProj.getProperties().put( this.scmBranchPropertyName, scmBranch );
                }
            }
        }
    }

    /**
     * Formats the given argument using the configured format template and locale.
     *
     * @param arguments arguments to be formatted @ @return formatted result
     */
    private String format( Object[] arguments )
    {
        Locale l = Locale.getDefault();
        if ( locale != null )
        {
            String[] parts = locale.split( "_", 3 );
            if ( parts.length <= 1 )
            {
                l = new Locale( locale );
            }
            else if ( parts.length == 2 )
            {
                l = new Locale( parts[0], parts[1] );
            }
            else
            {
                l = new Locale( parts[0], parts[1], parts[2] );
            }
        }

        return new MessageFormat( format, l ).format( arguments );
    }

    private void checkForLocalModifications()
        throws MojoExecutionException
    {
        getLog().debug( "Verifying there are no local modifications ..." );

        List<ScmFile> changedFiles;

        try
        {
            changedFiles = getStatus();
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "An error has occurred while checking scm status.", e );
        }

        if ( !changedFiles.isEmpty() )
        {
            StringBuilder message = new StringBuilder();

            String ls = System.getProperty( "line.separator" );

            for ( ScmFile file : changedFiles )
            {
                message.append( file.toString() );

                message.append( ls );
            }

            throw new MojoExecutionException(
                                              "Cannot create the build number because you have local modifications : \n"
                                                  + message );
        }

    }

    public List<ScmFile> update()
        throws MojoExecutionException
    {
        try
        {
            ScmRepository repository = getScmRepository();

            ScmProvider scmProvider = scmManager.getProviderByRepository( repository );

            UpdateScmResult result = scmProvider.update( repository, new ScmFileSet( scmDirectory ) );

            if ( result == null )
            {
                return Collections.emptyList();
            }

            checkResult( result );

            if ( result instanceof UpdateScmResultWithRevision )
            {
                String revision = ( (UpdateScmResultWithRevision) result ).getRevision();
                getLog().info( "Got a revision during update: " + revision );
                this.revision = revision;
            }

            return result.getUpdatedFiles();
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "Couldn't update project. " + e.getMessage(), e );
        }

    }

    public List<ScmFile> getStatus()
        throws ScmException
    {

        ScmRepository repository = getScmRepository();

        ScmProvider scmProvider = scmManager.getProviderByRepository( repository );

        StatusScmResult result = scmProvider.status( repository, new ScmFileSet( scmDirectory ) );

        if ( result == null )
        {
            return Collections.emptyList();
        }

        checkResult( result );

        return result.getChangedFiles();

    }

    /**
     * Get the branch info for this revision from the repository. For svn, it is in svn info.
     *
     * @return
     * @throws MojoExecutionException
     * @throws MojoExecutionException
     */
    public String getScmBranch()
        throws MojoExecutionException
    {
        try
        {
            ScmRepository repository = getScmRepository();
            ScmProvider provider = scmManager.getProviderByRepository( repository );
            /* git branch can be obtained directly by a command */
            if ( GitScmProviderRepository.PROTOCOL_GIT.equals( provider.getScmType() ) )
            {
                ScmFileSet fileSet = new ScmFileSet( scmDirectory );
                return GitBranchCommand.getCurrentBranch( getLogger(),
                                                          (GitScmProviderRepository) repository.getProviderRepository(),
                                                          fileSet );
            } else if ( provider instanceof HgScmProvider ) {
                /* hg branch can be obtained directly by a command */
                HgOutputConsumer consumer = new HgOutputConsumer( getLogger() );
 		        ScmResult result = HgUtils.execute( consumer, logger, scmDirectory, new String[] { "id", "-b" } );
		        checkResult( result );
		        if (StringUtils.isNotEmpty(consumer.getOutput())) {
		        	return consumer.getOutput();	
		        }
	         }
        }
        catch ( ScmException e )
        {
            getLog().warn( "Cannot get the branch information from the git repository: \n" + e.getLocalizedMessage() );
        }

        return getScmBranchFromUrl();
    }

    private String getScmBranchFromUrl()
        throws MojoExecutionException
    {
        String scmUrl = null;
        try
        {
            ScmRepository repository = getScmRepository();
            InfoScmResult scmResult = info( repository, new ScmFileSet( scmDirectory ) );
            if ( scmResult == null || !scmResult.isSuccess() )
            {
                getLog().debug( "Cannot get the branch information from the scm repository : "
                                    + ( scmResult == null ? "" : scmResult.getCommandOutput() ) );
                return DEFAULT_BRANCH_NAME;
            }
            if ( scmResult.getInfoItems().isEmpty() )
            {
                if ( !StringUtils.isEmpty( revisionOnScmFailure ) )
                {
                    setDoCheck( false );
                    setDoUpdate( false );

                    return DEFAULT_BRANCH_NAME;
                }
            }
            if ( !scmResult.getInfoItems().isEmpty() )
            {
                InfoItem info = scmResult.getInfoItems().get( 0 );
                scmUrl = info.getURL();
            }
        }
        catch ( ScmException e )
        {
            if ( !StringUtils.isEmpty( revisionOnScmFailure ) )
            {
                getLog().warn( "Cannot get the branch information from the scm repository, proceeding with "
                                   + DEFAULT_BRANCH_NAME + " : \n" + e.getLocalizedMessage() );

                setDoCheck( false );
                setDoUpdate( false );

                return DEFAULT_BRANCH_NAME;
            }
            throw new MojoExecutionException( "Cannot get the branch information from the scm repository : \n"
                + e.getLocalizedMessage(), e );
        }

        return filterBranchFromScmUrl( scmUrl );
    }

    protected String filterBranchFromScmUrl( String scmUrl )
    {
        String scmBranch = "UNKNOWN";

        if ( StringUtils.contains( scmUrl, "/trunk" ) )
        {
            scmBranch = "trunk";
        }
        else if ( StringUtils.contains( scmUrl, "/branches" ) || StringUtils.contains( scmUrl, "/tags" ) )
        {
            scmBranch = scmUrl.replaceFirst( ".*((branches|tags)/[^/]*).*", "$1" );
        }
        return scmBranch;
    }

    /**
     * Get the revision info from the repository. For svn, it is svn info
     *
     * @return
     * @throws MojoExecutionException
     */
    public String getRevision()
        throws MojoExecutionException
    {

        if ( format != null && !useScm )
        {
            return revision;
        }
        useScm = false;

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

                setDoCheck( false );
                setDoUpdate( false );

                return revisionOnScmFailure;
            }

            throw new MojoExecutionException( "Cannot get the revision information from the scm repository : \n"
                + e.getLocalizedMessage(), e );

        }

    }

    /**
     * @return
     * @todo normally this would be handled in AbstractScmProvider
     */
    private ScmLogger getLogger()
    {
        if ( logger == null )
        {
            logger = new ScmLogDispatcher();
        }
        return logger;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////
    // setters to help with test
    public void setScmManager( ScmManager scmManager )
    {
        this.scmManager = scmManager;
    }

    public void setUrlScm( String urlScm )
    {
        this.scmConnectionUrl = urlScm;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public void setDoCheck( boolean doCheck )
    {
        this.doCheck = getBooleanProperty( "maven.buildNumber.doCheck", doCheck );
    }

    public void setDoUpdate( boolean doUpdate )
    {
        this.doUpdate = getBooleanProperty( "maven.buildNumber.doUpdate", doUpdate );
    }

    private boolean getBooleanProperty( String key, boolean defaultValue ) {
        String systemProperty = System.getProperty( key );
        if (systemProperty == null)
        {
            return defaultValue;
        }
        else
        {
            return parseBoolean( systemProperty );
        }
    }

    void setFormat( String format )
    {
        this.format = format;
    }

    void setLocale( String locale )
    {
        this.locale = locale;
    }

    void setItems( List<?> items )
    {
        this.items = items;
    }

    public void setBuildNumberPropertiesFileLocation( File buildNumberPropertiesFileLocation )
    {
        this.buildNumberPropertiesFileLocation = buildNumberPropertiesFileLocation;
    }

    public void setScmDirectory( File scmDirectory )
    {
        this.scmDirectory = scmDirectory;
    }

    public void setRevisionOnScmFailure( String revisionOnScmFailure )
    {
        this.revisionOnScmFailure = revisionOnScmFailure;
    }

    public void setShortRevisionLength( int shortRevision )
    {
        this.shortRevisionLength = shortRevision;
    }


}
