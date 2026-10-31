import java.util.jar.JarFile

def log = new File(basedir, "build.log").text
assert log.contains("Storing buildNumber: foo")
assert log.contains("Storing scmBranch: UNKNOWN_BRANCH")

def scmRevision = new JarFile(new File(basedir, "target/buildnumber-maven-plugin-basic-it-clearcase-scm-1.0-SNAPSHOT.jar")).withCloseable { it.manifest.mainAttributes.getValue("SCM-Revision") }
assert scmRevision == "foo"
