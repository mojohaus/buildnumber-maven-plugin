package org.codehaus.mojo.build.it;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenExecution;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenRuntime.MavenRuntimeBuilder;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;
import org.codehaus.plexus.util.cli.Commandline;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( MavenJUnitTestRunner.class )
@MavenVersions( { "3.1.1" } )
public class BuildNumberMojoTest
{

    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime maven;

    public BuildNumberMojoTest( MavenRuntimeBuilder builder )
        throws Exception
    {
        this.maven = builder.withCliOptions( "-B" ).build();
    }

    @Test
    public void basicItTest()
        throws Exception
    {
        if ( ! isSvn18() ) {
            System.out.println( "Not Subversion 1.8 compatible. Skip test");
            return;
        }

        File projDir = resources.getBasedir( "basic-it" );

        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "clean" );
        result.assertErrorFreeLog();
        File testDir = result.getBasedir();
        FileUtils.copyDirectory( new File( testDir, "DotSvnDir" ), new File( testDir, ".svn" ) );
        result = mavenExec.execute( "clean", "verify" );
        result.assertLogText( "Storing buildNumber: 19665" );
        result.assertLogText( "Storing buildScmBranch: trunk" );

        File artifact = new File( testDir, "target/buildnumber-maven-plugin-basic-it-1.0-SNAPSHOT.jar" );
        JarFile jarFile = new JarFile( artifact );
        Attributes manifest = jarFile.getManifest().getMainAttributes();
        jarFile.close();
        String scmRev = manifest.getValue( "SCM-Revision" );
        Assert.assertEquals( "19665", scmRev );
    }

    @Test
    public void basicItClearcaseScmTest()
        throws Exception
    {
        File projDir = resources.getBasedir( "basic-it-clearcase-scm" );

        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "clean", "verify" );
        File testDir = result.getBasedir();
        result.assertLogText( "Storing buildNumber: foo" );
        result.assertLogText( "Storing buildScmBranch: UNKNOWN_BRANCH" );

        File artifact = new File( testDir, "target/buildnumber-maven-plugin-basic-it-clearcase-scm-1.0-SNAPSHOT.jar" );
        JarFile jarFile = new JarFile( artifact );
        Attributes manifest = jarFile.getManifest().getMainAttributes();
        jarFile.close();
        String scmRev = manifest.getValue( "SCM-Revision" );
        Assert.assertEquals( "foo", scmRev );
    }

    @Test
    public void basicItNoDevScmTest()
        throws Exception
    {
        if ( ! isSvn18() ) {
            System.out.println( "Not Subversion 1.8 compatible. Skip test");
            return;
        }

        File projDir = resources.getBasedir( "basic-it-no-devscm" );

        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "clean" );
        result.assertErrorFreeLog();
        File testDir = result.getBasedir();
        FileUtils.copyDirectory( new File( testDir, "DotSvnDir" ), new File( testDir, ".svn" ) );
        result = mavenExec.execute( "clean", "verify" );
        result.assertLogText( "Storing buildNumber: 19665" );
        result.assertLogText( "Storing buildScmBranch: trunk" );

        File artifact = new File( testDir, "target/buildnumber-maven-plugin-basic-it-no-devscm-1.0-SNAPSHOT.jar" );
        JarFile jarFile = new JarFile( artifact );
        Attributes manifest = jarFile.getManifest().getMainAttributes();
        jarFile.close();
        String scmRev = manifest.getValue( "SCM-Revision" );
        Assert.assertEquals( "19665", scmRev );
    }

    @Test
    public void basicItSvnJavaTest()
        throws Exception
    {
        if ( ! isSvn18() ) {
            System.out.println( "Not Subversion 1.8 compatible. Skip test");
            return;
        }

        File projDir = resources.getBasedir( "basic-it-svnjava" );

        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "clean" );
        result.assertErrorFreeLog();
        File testDir = result.getBasedir();
        FileUtils.copyDirectory( new File( testDir, "DotSvnDir" ), new File( testDir, ".svn" ) );
        result = mavenExec.execute( "clean", "verify" );
        result.assertLogText( "Storing buildNumber: 19665" );
        result.assertLogText( "Storing buildScmBranch: trunk" );

        File artifact = new File( testDir, "target/buildnumber-maven-plugin-basic-it-svnjava-1.0-SNAPSHOT.jar" );
        JarFile jarFile = new JarFile( artifact );
        Attributes manifest = jarFile.getManifest().getMainAttributes();
        jarFile.close();
        String scmRev = manifest.getValue( "SCM-Revision" );
        Assert.assertEquals( "19665", scmRev );
    }

    @Test
    public void createTimestampItTest()
        throws Exception
    {
        File projDir = resources.getBasedir( "create-timestamp-it" );

        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "clean", "verify" );
        File testDir = result.getBasedir();
        File artifact = new File( testDir, "target/buildnumber-maven-plugin-create-timestamp-it-1.0-SNAPSHOT.jar" );
        JarFile jarFile = new JarFile( artifact );
        Attributes manifest = jarFile.getManifest().getMainAttributes();
        jarFile.close();
        String timestamp = manifest.getValue( "Build-Time" );
        SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss" );
        Date theDate = format.parse( timestamp );

    }

    @Test
    @Ignore
    // svn local db corrupted
    public void failLocalChangeItTest()
        throws Exception
    {
        File projDir = resources.getBasedir( "failed-local-change" );

        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "clean" );
        result.assertErrorFreeLog();
        File basedir = result.getBasedir();
        File foo = new File( basedir, "foo.txt" );
        FileUtils.fileWrite( foo, "hello" );
        FileUtils.copyDirectory( new File( basedir, "DotSvnDir" ), new File( basedir, ".svn" ) );
        result = mavenExec.execute( "verify" );
        // this fail local dotSvnDir corrupted, not b/c we change local file
        result.assertLogText( "BUILD FAILURE" );
    }

    @Test
    @Ignore
    // git local database corrected
    public void gitBasicItMBUILDNUM66Test()
        throws Exception
    {
        File projDir = resources.getBasedir( "git-basic-it-MBUILDNUM-66" );

        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "clean" );
        result.assertErrorFreeLog();
        File basedir = result.getBasedir();
        File foo = new File( basedir, "foo.txt" );
        FileUtils.fileWrite( foo, "hello" );
        FileUtils.copyDirectory( new File( basedir, "dotGitDir" ), new File( basedir, ".git" ) );
        result = mavenExec.execute( "verify" );
        // this fail local dotSvnDir corrupted, not b/c we change local file

    }

    @Test
    public void helpItTest()
        throws Exception
    {
        File projDir = resources.getBasedir( "help-it" );

        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "validate" );
        result.assertErrorFreeLog();
        result.assertLogText( "buildnumber:create" );
        result.assertLogText( "This mojo discovers latest SCM revision and current timestamp" );
    }

    @Test
    @Ignore
    // this project needs a dotSvnDir
    public void mBuildNum5Test()
        throws Exception
    {
        File projDir = resources.getBasedir( "MBUILDNUM-5" );

        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "verify" );
        result.assertErrorFreeLog();
    }

    @Test
    public void mBuildNum83Test()
        throws Exception
    {
        File projDir = resources.getBasedir( "MBUILDNUM-83" );

        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "clean", "verify" );
        result.assertErrorFreeLog();

        File testDir = result.getBasedir();
        File artifact = new File( testDir, "target/buildnumber-maven-plugin-basic-it-svnjava-1.0-SNAPSHOT.jar" );
        JarFile jarFile = new JarFile( artifact );
        Attributes manifest = jarFile.getManifest().getMainAttributes();
        jarFile.close();
        String scmRev = manifest.getValue( "SCM-Revision" );
        Assert.assertEquals( "booom", scmRev ); // ??? biim?
    }

    @Test
    public void mBuildNum85Test()
        throws Exception
    {
        File projDir = resources.getBasedir( "MBUILDNUM-85" );

        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "clean", "verify" );
        result.assertLogText( "BUILD FAILURE" );
    }

    @Test
    public void mojo1372Test()
        throws Exception
    {
        File projDir = resources.getBasedir( "MOJO-1372" );

        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "clean", "verify" );
        result.assertErrorFreeLog();
    }

    @Test
    public void Mojo1668Test()
        throws Exception
    {
        if ( ! isSvn18() ) {
            System.out.println( "Not Subversion 1.8 compatible. Skip test");
            return;
        }

        File projDir = resources.getBasedir( "MOJO-1668" );

        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "clean" );
        result.assertErrorFreeLog();
        File testDir = result.getBasedir();
        FileUtils.copyDirectory( new File( testDir, "DotSvnDir" ), new File( testDir, ".svn" ) );
        result = mavenExec.execute( "clean", "verify" );

        File artifact = new File( testDir, "target/buildnumber-maven-plugin-MOJO-1668-1.0-SNAPSHOT.jar" );
        JarFile jarFile = new JarFile( artifact );
        Attributes manifest = jarFile.getManifest().getMainAttributes();
        jarFile.close();
        String buildDate = manifest.getValue( "Build-Date" );
        Assert.assertTrue( buildDate.length() > 0 );
    }

    @Test
    public void noRevisionItTest()
        throws Exception
    {
        File projDir = resources.getBasedir( "norevision-it" );

        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "clean", "verify" );
        result.assertErrorFreeLog();

        File testDir = result.getBasedir();
        File artifact = new File( testDir, "target/buildnumber-maven-plugin-norevision-it-1.0-SNAPSHOT.jar" );
        JarFile jarFile = new JarFile( artifact );
        Attributes manifest = jarFile.getManifest().getMainAttributes();
        jarFile.close();
        String scmRev = manifest.getValue( "SCM-Revision" );
        Assert.assertTrue( StringUtils.isBlank( scmRev ) );
    }

    @Test
    public void skipItTest()
        throws Exception
    {
        File projDir = resources.getBasedir( "skip-it" );

        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "clean", "verify" );
        result.assertErrorFreeLog();

        File testDir = result.getBasedir();
        File artifact = new File( testDir, "target/buildnumber-maven-plugin-skip-it-1.0-SNAPSHOT.jar" );
        JarFile jarFile = new JarFile( artifact );
        Attributes manifest = jarFile.getManifest().getMainAttributes();
        jarFile.close();
        String buildDate = manifest.getValue( "Build-Date" );
        Assert.assertTrue( StringUtils.isBlank( buildDate ) );
    }

    private static boolean isSvn18()
    {
        Commandline cl = new Commandline();
        cl.setExecutable( "svn" );

        StringStreamConsumer stdout = new StringStreamConsumer();
        StringStreamConsumer stderr = new StringStreamConsumer();

        try
        {
            CommandLineUtils.executeCommandLine( cl, stdout, stderr );
            return stdout.getOutput().contains( "svn, version 1.8." );
        }
        catch ( CommandLineException e )
        {
        }


        return false;
    }
}