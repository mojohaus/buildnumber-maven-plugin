package org.codehaus.mojo.build.it;

import java.io.File;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.runner.Description;

/**
 * Runs the projects of {@code src/test/projects} with the Maven distribution unpacked to
 * {@code target/maven-installation}. The Takari helpers are JUnit 4 classes without a Jupiter
 * counterpart, so this class drives them directly instead of through the JUnit 4 runner.
 */
abstract class AbstractMavenIT {

    TestResources resources;

    MavenRuntime maven;

    @BeforeEach
    void setUpMaven(TestInfo testInfo) throws Exception {
        final Description description = Description.createTestDescription(
                getClass(), testInfo.getTestMethod().get().getName());
        resources = new TestResources() {
            {
                starting(description);
            }
        };
        File mavenHome =
                new File("target/maven-installation", "apache-maven-" + System.getProperty("maven.it.version"));
        maven = MavenRuntime.builder(mavenHome, null).withCliOptions("-B").build();
    }
}
