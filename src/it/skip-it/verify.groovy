import java.util.jar.JarFile

def log = new File(basedir, "build.log").text
assert !log.contains("[ERROR]") : "build.log contains errors"

def buildDate = new JarFile(new File(basedir, "target/buildnumber-maven-plugin-skip-it-1.0-SNAPSHOT.jar")).withCloseable { it.manifest.mainAttributes.getValue("Build-Date") }
assert !buildDate?.trim() : "Build-Date should be blank but is " + buildDate
