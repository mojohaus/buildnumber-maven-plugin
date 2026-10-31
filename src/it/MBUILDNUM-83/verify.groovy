import java.util.jar.JarFile

def log = new File(basedir, "build.log").text
assert !log.contains("[ERROR]") : "build.log contains errors"

def scmRevision = new JarFile(new File(basedir, "target/buildnumber-maven-plugin-basic-it-svnjava-1.0-SNAPSHOT.jar")).withCloseable { it.manifest.mainAttributes.getValue("SCM-Revision") }
assert scmRevision == "booom"
