package org.codehaus.mojo.build;

/**
 * The MIT License
 * 
 * Copyright (c) 2005 Learning Commons, University of Calgary
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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.CommandParameters;
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
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.StringUtils;

/**
 * This mojo is designed to give you a build number. So when you might make 100 builds of version
 * 1.0-SNAPSHOT, you can differentiate between them all. The build number is based on the revision
 * number retrieved from scm. It only works with subversion, currently. This mojo can also check to
 * make sure that you have checked everything into scm, before issuing the build number. That
 * behaviour can be suppressed, and then the latest local build number is used. Build numbers are
 * not reflected in your artifact's filename (automatically), but can be added to the metadata. You
 * can access the build number in your pom with ${buildNumber}. You can also access ${timestamp} and
 * the scm branch of the build (if applicable) in ${buildScmBranch}
 * 
 * @author <a href="mailto:woodj@ucalgary.ca">Julian Wood</a>
 * @version $Id$
 * @goal create
 * @requiresProject
 * @description create a timestamp and a build number from scm or an integer sequence
 */
public class CreateMojo
    extends AbstractMojo
{

    public final String DEFAULT_BRANCH_NAME = "UNKNOWN_BRANCH";
    
    /**
     * @parameter expression="${project.scm.developerConnection}"
     * @readonly
     */
    private String urlScm;
    
    /**
     * @parameter expression="${project.scm.connection}"
     * @since 1.0-beta-5
     * @readonly
     */
    private String readUrlScm;    

    /**
     * The username that is used when connecting to the SCM system.
     * 
     * @parameter expression="${username}"
     * @since 1.0-beta-1
     */
    private String username;

    /**
     * The password that is used when connecting to the SCM system.
     * 
     * @parameter expression="${password}"
     * @since 1.0-beta-1
     */
    private String password;

    /**
     * Local directory to be used to issue SCM actions
     * 
     * @parameter expression="${maven.buildNumber.scmDirectory}" default-value="${basedir}
     * @since 1.0-beta-
     */
    private File scmDirectory;



    /**
     * You can rename the buildNumber property name to another property name if desired.
     * 
     * @parameter expression="${maven.buildNumber.buildNumberPropertyName}"
     *            default-value="buildNumber"
     * @since 1.0-beta-1
     */
    private String buildNumberPropertyName;

    /**
     * You can rename the timestamp property name to another property name if desired.
     * 
     * @parameter expression="${maven.buildNumber.timestampPropertyName}" default-value="timestamp"
     * @since 1.0-beta-1
     */
    private String timestampPropertyName;

    /**
     * If this is made true, we check for modified files, and if there are any, we fail the build.
     * Note that this used to be inverted (skipCheck), but needed to be changed to allow releases to
     * work. This corresponds to 'svn status'.
     * 
     * @parameter expression="${maven.buildNumber.doCheck}" default-value="false"
     * @since 1.0-beta-1
     */
    private boolean doCheck;

    /**
     * If this is made true, then the revision will be updated to the latest in the repo, otherwise
     * it will remain what it is locally. Note that this used to be inverted (skipUpdate), but
     * needed to be changed to allow releases to work. This corresponds to 'svn update'.
     * 
     * Note that these expressions (doCheck, doUpdate, etc) are the first thing evaluated. If there
     * is no matching expression, we get the default-value. If there is (ie
     * -Dmaven.buildNumber.doCheck=false), we get that value. The configuration, however, gets the
     * last say, through use of the getters/setters below. So if <doCheck>true</doCheck>, then
     * normally that's the final value of the param in question. However, this mojo reverses that
     * behaviour, such that the command line parameters get the last say.
     * 
     * @parameter expression="${maven.buildNumber.doUpdate}" default-value="false"
     * @since 1.0-beta-1
     */
    private boolean doUpdate;

    /**
     * Specify a message as specified by java.text.MessageFormat. This triggers "items"
     * configuration to be read
     * 
     * @parameter
     * @since 1.0-beta-1
     */
    private String format;

    /**
     * Properties file to be created when "format" is not null and item has "buildNumber". See Usage
     * for details
     * 
     * @parameter default-value="${basedir}/buildNumber.properties";
     * @since 1.0-beta-2
     */
    private File buildNumberPropertiesFileLocation;

    /**
     * Specify the corresponding items for the format message, as specified by
     * java.text.MessageFormat. Special item values are "timestamp" and "buildNumber/d*".
     * 
     * @parameter
     * @since 1.0-beta-1
     */
    private List items;

    /**
     * The locale used for date and time formatting. The locale name should be in the format defined
     * in {@link Locale#toString()}. The default locale is the platform default returned by
     * {@link Locale#getDefault()}.
     * 
     * @parameter expression="${maven.buildNumber.locale}"
     * @since 1.0-beta-2
     */
    private String locale;

    /**
     * whether to retrieve the revision for the last commit, or the last revision of the repository.
     * 
     * @parameter expression="${maven.buildNumber.useLastCommittedRevision}" default-value="false"
     * @since 1.0-beta-2
     */
    private boolean useLastCommittedRevision;

    /**
     * Apply this java.text.MessageFormat to the timestamp only (as opposed to the
     * <code>format</code> parameter).
     * 
     * @parameter
     * @since 1.0-beta-2
     */
    private String timestampFormat;

    /**
     * Setting this value allows the build to continue even in the event of an SCM failure.  The value set will be
     * used as the revision string in the event of a failure to retrieve the revision it from the SCM.
     * 
     * @parameter
     * @since 1.0-beta-2
     */
    private String revisionOnScmFailure;
    
    /**
     * Selects alternative SCM provider implementations. Each map key denotes the original provider type as given in the
     * SCM URL like "cvs" or "svn", the map value specifies the provider type of the desired implementation to use
     * instead. In other words, this map configures a substitition mapping for SCM providers.
     * 
     * @parameter
     * @since 1.0-beta-3
     */
    private Map<String, String> providerImplementations;
    
    /**
     * @component
     */
    private ScmManager scmManager;

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @readonly
     */
    private MavenProject project;

    /**
     * Contains the full list of projects in the reactor.
     *
     * @parameter expression="${reactorProjects}"
     * @readonly
     * @since 1.0-beta-3
     */
    private List reactorProjects;
    
    /**
     * If set to true, will get the scm revision once for all modules of a multi-module project 
     * instead of fetching once for each module.
     * 
     * @parameter default-value="false"
     * @since 1.0-beta-3
     * 
     */
    private boolean getRevisionOnlyOnce;

    /**
     * You can rename the buildScmBranch property name to another property name if desired.
     * 
     * @parameter expression="${maven.buildNumber.scmBranchPropertyName}"
     *            default-value="scmBranch"
     * @since 1.0-beta-4
     */
    private String scmBranchPropertyName;
    
    
    /**
     * @parameter expression="${session}"
     * @readonly
     * @required
     */
    protected MavenSession session;       

    private ScmLogDispatcher logger;

    private String revision;


    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( providerImplementations != null )
        {
            for ( Entry<String, String> entry : providerImplementations.entrySet() )
            {
                String providerType = entry.getKey();
                String providerImplementation = entry.getValue();
                getLog().info(
                               "Change the default '" + providerType + "' provider implementation to '"
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
                    else if ( s.startsWith( "buildNumber" ) )
                    {
                        // check for properties file
                        File propertiesFile = this.buildNumberPropertiesFileLocation;

                        // create if not exists
                        if ( !propertiesFile.exists() )
                        {
                            try
                            {
                                if (!propertiesFile.getParentFile().exists())
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
                        try
                        {
                            // get the number for the buildNumber specified
                            properties.load( new FileInputStream( propertiesFile ) );
                            buildNumberString = properties.getProperty( s );
                            if ( buildNumberString == null )
                            {
                                buildNumberString = "0";
                            }
                            int buildNumber = Integer.valueOf( buildNumberString ).intValue();

                            // store the increment
                            properties.setProperty( s, String.valueOf( ++buildNumber ) );
                            properties.store( new FileOutputStream( propertiesFile ),
                                              "maven.buildNumber.plugin properties file" );

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
            if ( this.getRevisionOnlyOnce && revision != null)
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
                getLog().info( "Checking for local modifications: skipped." );
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
                        getLog().info( "Updated: " + file );
                    }
                    if ( changedFiles.size() == 0 )
                    {
                        getLog().info( "No files needed updating." );
                    }
                }
                else
                {
                    getLog().info( "Updating project files from SCM: skipped." );
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

            getLog().info(
                           MessageFormat.format( "Storing buildNumber: {0} at timestamp: {1}", new Object[] {
                               revision,
                               timestamp } ) );
            if ( revision != null )
            {
                project.getProperties().put( buildNumberPropertyName, revision );
            }
            project.getProperties().put( timestampPropertyName, timestamp );
            
            String scmBranch = getScmBranch();
            getLog().info("Storing buildScmBranch: " + scmBranch);
            project.getProperties().put( scmBranchPropertyName, scmBranch );

            // Add the revision and timestamp properties to each project in the reactor
            if ( getRevisionOnlyOnce && reactorProjects != null )
            {
                Iterator projIter = reactorProjects.iterator();
                while ( projIter.hasNext() )
                {
                    MavenProject nextProj = (MavenProject) projIter.next();
                    if ( revision != null )
                    {
                        nextProj.getProperties().put( this.buildNumberPropertyName, revision );
                    }
                    nextProj.getProperties().put( this.timestampPropertyName, timestamp );
                    nextProj.getProperties().put(  this.scmBranchPropertyName, scmBranch );
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
        getLog().info( "Verifying there are no local modifications ..." );

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

            
            if (result == null )
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

        if (result == null)
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
        String scmUrl;
        try
        {
            ScmRepository repository = getScmRepository();
            InfoScmResult scmResult = info( repository, new ScmFileSet( scmDirectory ) );
            if ( scmResult == null || !scmResult.isSuccess() )
            {
                getLog().debug( "Cannot get the branch information from the scm repository : "
                                    + (scmResult == null ? "" : scmResult.getCommandOutput() ) );
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
            InfoItem info = scmResult.getInfoItems().get( 0 );
            scmUrl = info.getURL();
        }
        catch ( ScmException e )
        {
        	 if ( !StringUtils.isEmpty( revisionOnScmFailure ) )
             {
                 getLog().warn(
                                "Cannot get the branch information from the scm repository, proceeding with "
                                    + DEFAULT_BRANCH_NAME+ " : \n" + e.getLocalizedMessage() );

                 setDoCheck( false );
                 setDoUpdate( false );

                 return DEFAULT_BRANCH_NAME;
             }
            throw new MojoExecutionException( "Cannot get the branch information from the scm repository : \n" +
                e.getLocalizedMessage(), e );
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
            scmBranch = scmUrl.replaceFirst( ".*((branches|tags)[^/]*).*?", "$1" );
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

        if ( format != null )
        {
            return revision;
        }

        try
        {
            ScmRepository repository = getScmRepository();

            InfoScmResult scmResult = info( repository, new ScmFileSet( scmDirectory ) );

            if (scmResult == null || scmResult.getInfoItems().isEmpty() ) 
            {
                return (!StringUtils.isEmpty( revisionOnScmFailure )) ? revisionOnScmFailure : null;
            }            
            
            checkResult( scmResult );
            
            
            InfoItem info = scmResult.getInfoItems().get( 0 );
            
            if ( useLastCommittedRevision )
            {
                return info.getLastChangedRevision();
            }
            
            return info.getRevision();
        }
        catch ( ScmException e )
        {
            if ( !StringUtils.isEmpty( revisionOnScmFailure ) )
            {
                getLog().warn(
                               "Cannot get the revision information from the scm repository, proceeding with "
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
     * Get info from svn.
     * 
     * @param repository
     * @param fileSet
     * @return
     * @throws ScmException
     * @todo this should be rolled into org.apache.maven.scm.provider.ScmProvider and
     *       org.apache.maven.scm.provider.svn.SvnScmProvider
     */
    public InfoScmResult info( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return scmManager.getProviderByRepository( repository ).info( repository.getProviderRepository(), fileSet, new CommandParameters() );
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

    private ScmRepository getScmRepository()
        throws ScmException
    {
        ScmRepository repository;

        repository = scmManager.makeScmRepository( StringUtils.isBlank( urlScm ) ? readUrlScm : urlScm  );

        ScmProviderRepository scmRepo = repository.getProviderRepository();

        if ( !StringUtils.isEmpty( username ) )
        {
            scmRepo.setUser( username );
        }
        
        if ( !StringUtils.isEmpty( password ) )
        {
            scmRepo.setPassword( password );
        }

        return repository;
    }

    private void checkResult( ScmResult result )
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

    //////////////////////////////////////////////////////////////////////////////////////////////
    // setters to help with test
    public void setScmManager( ScmManager scmManager )
    {
        this.scmManager = scmManager;
    }

    public void setUrlScm( String urlScm )
    {
        this.urlScm = urlScm;
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
        String doCheckSystemProperty = System.getProperty( "maven.buildNumber.doCheck" );
        if ( doCheckSystemProperty != null )
        {
            // well, this gets the final say
            this.doCheck = Boolean.valueOf( doCheckSystemProperty ).booleanValue();
        }
        else
        {
            this.doCheck = doCheck;
        }
    }

    public void setDoUpdate( boolean doUpdate )
    {
        String doUpdateSystemProperty = System.getProperty( "maven.buildNumber.doUpdate" );
        if ( doUpdateSystemProperty != null )
        {
            // well, this gets the final say
            this.doUpdate = Boolean.valueOf( doUpdateSystemProperty ).booleanValue();
        }
        else
        {
            this.doUpdate = doUpdate;
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

    void setItems( List items )
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
        
}
