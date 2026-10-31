import java.util.jar.JarFile

def log = new File(basedir, "build.log").text
assert !log.contains("[ERROR]") : "build.log contains errors"
assert log.contains("Storing buildNumber: 6d36c746e82f00c5913954f9178f40224497b2f3")
assert log.contains("Storing scmBranch: master")

def scmRevision = new JarFile(new File(basedir, "target/buildnumber-maven-plugin-basic-it-1.0-SNAPSHOT.jar")).withCloseable { it.manifest.mainAttributes.getValue("SCM-Revision") }
assert scmRevision == "6d36c746e82f00c5913954f9178f40224497b2f3"
