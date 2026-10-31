import java.util.jar.JarFile

def log = new File(basedir, "build.log").text
assert !log.contains("[ERROR]") : "build.log contains errors"

def scmRevision = new JarFile(new File(basedir, "target/buildnumber-maven-plugin-norevision-it-1.0-SNAPSHOT.jar")).withCloseable { it.manifest.mainAttributes.getValue("SCM-Revision") }
assert !scmRevision?.trim() : "SCM-Revision should be blank but is " + scmRevision
