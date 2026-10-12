package org.codehaus.mojo.build.it;

import java.io.File;

import io.takari.maven.testing.executor.MavenExecution;
import io.takari.maven.testing.executor.MavenExecutionResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateMetadataMojoTest extends AbstractMavenIT {
    @Test
    void basicConfiguration() throws Exception {
        File projDir = resources.getBasedir("create-metadata-it");

        MavenExecution mavenExec = maven.forProject(projDir);
        MavenExecutionResult result = mavenExec.execute("clean", "test");
        result.assertErrorFreeLog();

        File testDir = result.getBasedir();
        assertTrue(new File(testDir, "target/file1.properties").exists());
        assertTrue(new File(testDir, "target/xxx/file1.properties").exists());
        assertTrue(new File(testDir, "target/generated/build-metadata/build.properties").exists());
        assertTrue(new File(testDir, "target/classes/build.properties").exists());
    }

    @Test
    void basicJsonConfiguration() throws Exception {
        File projDir = resources.getBasedir("create-metadata-json-it");

        MavenExecution mavenExec = maven.forProject(projDir);
        MavenExecutionResult result = mavenExec.execute("clean", "test");
        result.assertErrorFreeLog();

        File testDir = result.getBasedir();
        assertTrue(new File(testDir, "target/file1.json").exists());
        assertTrue(new File(testDir, "target/xxx/file1.json").exists());
        assertTrue(new File(testDir, "target/generated/build-metadata/build.properties").exists());
        assertTrue(new File(testDir, "target/classes/build.properties").exists());
    }

    @Test
    void basicConfigurationNoScm() throws Exception {
        File projDir = resources.getBasedir("create-metadata-it-no-scm");

        MavenExecution mavenExec = maven.forProject(projDir);
        MavenExecutionResult result = mavenExec.withCliOption("-e").execute("clean", "test");
        result.assertErrorFreeLog();

        File testDir = result.getBasedir();
        assertTrue(new File(testDir, "target/file1.properties").exists());
        assertTrue(new File(testDir, "target/xxx/file1.properties").exists());
        assertTrue(new File(testDir, "target/generated/build-metadata/build.properties").exists());
        assertTrue(new File(testDir, "target/classes/build.properties").exists());
    }
}
