package org.codehaus.mojo.build;

/**
 * The MIT License
 *
 * Copyright (c) 2005 Learning Commons, University of Calgary
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnexe.command.SvnCommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Command to get info from svn.
 *
 * @author <a href="mailto:woodj@ucalgary.ca">Julian Wood</a>
 * @version $Id$
 */
public class SvnInfoCommand
    extends AbstractCommand
    implements SvnCommand
{

    protected InfoScmResult executeInfoCommand( ScmProviderRepository repo, ScmFileSet fileSet )
        throws ScmException
    {
        Commandline cl = null;
        try
        {
            cl = createCommandline( (SvnScmProviderRepository) repo, fileSet.getBasedir() );
        }
        catch ( Exception e )
        {
            throw new ScmException( "Error while executing command.", e );
        }

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        SvnInfoConsumer stdin = new SvnInfoConsumer( getLogger() );

        getLogger().debug( "Executing: " + cl );
        getLogger().debug( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );

        Process process = null;

        try
        {
            process = cl.execute();
            // for some reason this gets executed twice
            // System.out.println( "process = " + process );

            BufferedReader br = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
            String line;
            while ( ( line = br.readLine() ) != null )
            {
                stdin.consumeLine( line );
            }

            br = new BufferedReader( new InputStreamReader( process.getErrorStream() ) );
            while ( ( line = br.readLine() ) != null )
            {
                stderr.consumeLine( line );
            }

            process.waitFor();

        }
        catch ( CommandLineException e )
        {
            throw new ScmException( "Error while executing command.", e );
        }
        catch ( IOException e )
        {
            throw new ScmException( "Error while executing command.", e );
        }
        catch ( InterruptedException e )
        {
            throw new ScmException( "Error while executing command.", e );
        }

        if ( process != null && process.exitValue() != 0 )
        {
            process.destroy();
            return new InfoScmResult( cl.toString(), "The svn command failed.", stderr.getOutput(), false );
        }

        process.destroy();

        return new InfoScmResult( cl.toString(), stdin.getCommandOutput() );

    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------


    private static Commandline createCommandline( SvnScmProviderRepository repository, File workingDirectory )
        throws Exception
    {
        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine( workingDirectory, repository );

        // for tests
        String svnexe = System.getProperty( "pathToSvnExecutable" );
        if ( svnexe != null )
        {
            cl.setExecutable( svnexe );
        }

        cl.createArgument().setValue( "info" );
        cl.createArgument().setValue( "--xml" );
        return cl;
    }

    protected ScmResult executeCommand( ScmProviderRepository scmProviderRepository, ScmFileSet scmFileSet,
                                        CommandParameters commandParameters )
        throws ScmException
    {
        return executeInfoCommand( scmProviderRepository, scmFileSet );
    }
}
