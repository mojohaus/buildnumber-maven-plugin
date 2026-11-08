package org.codehaus.mojo.build;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The plugin bundles one provider for subversion, so that the provider in use does not depend on discovery order
 * (#261). Users who want the svnjava provider add it as a plugin dependency.
 */
class BundledScmProvidersTest {

    @Test
    void shouldBundleTheSvnexeProvider() throws Exception {
        assertNotNull(Class.forName("org.apache.maven.scm.provider.svn.svnexe.SvnExeScmProvider"));
    }

    @Test
    void shouldNotBundleTheSvnjavaProvider() {
        assertThrows(
                ClassNotFoundException.class,
                () -> Class.forName("org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider"));
    }
}
