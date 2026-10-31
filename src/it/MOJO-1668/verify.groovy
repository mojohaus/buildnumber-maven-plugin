import java.util.jar.JarFile

def buildDate = new JarFile(new File(basedir, "target/buildnumber-maven-plugin-MOJO-1668-1.0-SNAPSHOT.jar")).withCloseable { it.manifest.mainAttributes.getValue("Build-Date") }
assert buildDate.length() > 0
