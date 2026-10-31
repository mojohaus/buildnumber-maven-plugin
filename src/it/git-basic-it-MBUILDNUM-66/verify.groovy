import java.util.jar.JarFile

def log = new File(basedir, "build.log").text
assert !log.contains("[ERROR]") : "build.log contains errors"
assert log.contains("Storing scmBranch: master")

def scmRevision = new JarFile(new File(basedir, "target/buildnumber-maven-plugin-basic-it-1.0-SNAPSHOT.jar")).withCloseable { it.manifest.mainAttributes.getValue("SCM-Revision") }
assert scmRevision == "ee58acb27b6636a497c1185f80cd15f76134113f"
