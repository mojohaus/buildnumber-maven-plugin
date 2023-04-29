package org.codehaus.mojo.build.it;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenExecution;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenRuntime.MavenRuntimeBuilder;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;

import java.io.File;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( MavenJUnitTestRunner.class )
@MavenVersions( { "3.5.4" } )
public class CreateMetadataMojoTest
{
    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime maven;

    public CreateMetadataMojoTest( MavenRuntimeBuilder builder )
        throws Exception
    {
        this.maven = builder.withCliOptions( "-B" ).build();
    }

    @Test
    public void testBasicConfiguration()
        throws Exception
    {
        File projDir = resources.getBasedir( "create-metadata-it" );

        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "clean", "test" );
        result.assertErrorFreeLog();

        File testDir = result.getBasedir();
        Assert.assertTrue( new File( testDir, "target/file1.properties" ).exists() );
        Assert.assertTrue( new File( testDir, "target/xxx/file1.properties" ).exists() );
        Assert.assertTrue( new File( testDir, "target/generated/build-metadata/build.properties" ).exists() );
        Assert.assertTrue( new File( testDir, "target/classes/build.properties" ).exists() );

    }

    @Test
    public void testBasicJsonConfiguration()
        throws Exception
    {
        File projDir = resources.getBasedir( "create-metadata-json-it" );

        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "clean", "test" );
        result.assertErrorFreeLog();

        File testDir = result.getBasedir();
        Assert.assertTrue( new File( testDir, "target/file1.json" ).exists() );
        Assert.assertTrue( new File( testDir, "target/xxx/file1.json" ).exists() );
        Assert.assertTrue( new File( testDir, "target/generated/build-metadata/build.properties" ).exists() );
        Assert.assertTrue( new File( testDir, "target/classes/build.properties" ).exists() );

    }
}
