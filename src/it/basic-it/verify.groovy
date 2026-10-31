import java.util.jar.JarFile

def log = new File(basedir, "build.log").text
assert !log.contains("[ERROR]") : "build.log contains errors"
assert log.contains("Storing buildNumber: 14069")
assert log.contains("Storing scmBranch: trunk")

def scmRevision = new JarFile(new File(basedir, "target/buildnumber-maven-plugin-basic-it-1.0-SNAPSHOT.jar")).withCloseable { it.manifest.mainAttributes.getValue("SCM-Revision") }
assert scmRevision == "14069"
