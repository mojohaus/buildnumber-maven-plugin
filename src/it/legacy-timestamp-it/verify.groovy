import java.text.SimpleDateFormat
import java.util.jar.JarFile

def log = new File(basedir, "build.log").text
assert log.contains("Please update your POM")

def timestamp = new JarFile(new File(basedir, "target/buildnumber-maven-plugin-legacy-timestamp-it-1.0-SNAPSHOT.jar")).withCloseable { it.manifest.mainAttributes.getValue("Build-Time") }
assert new SimpleDateFormat("yyyy-MM-dd").parse(timestamp) != null
