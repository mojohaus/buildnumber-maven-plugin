import java.util.jar.JarFile

def log = new File(basedir, "build.log").text
assert !log.contains("[ERROR]") : "build.log contains errors"
assert log.contains("Storing buildNumber: 19665")
assert log.contains("Storing scmBranch: trunk")

def attributes = new JarFile(new File(basedir, "target/buildnumber-maven-plugin-basic-it-svnjava-1.0-SNAPSHOT.jar")).withCloseable {
    it.manifest.mainAttributes
}
assert attributes.getValue("SCM-Revision") == "19665"
assert attributes.getValue("Build-Date") ==~ /\d{4}-\d{2}-\d{2}/ : "Build-Date is " + attributes.getValue("Build-Date")
