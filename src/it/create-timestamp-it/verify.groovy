import java.text.SimpleDateFormat
import java.util.jar.JarFile

def timestamp = new JarFile(new File(basedir, "target/buildnumber-maven-plugin-create-timestamp-it-1.0-SNAPSHOT.jar")).withCloseable { it.manifest.mainAttributes.getValue("Build-Time") }
assert new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(timestamp) != null
