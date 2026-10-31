import java.nio.file.Files
import java.nio.file.StandardCopyOption

// The fixture carries its repository metadata as dotSvnDir, so that it is not a nested repository.
def source = new File(basedir, 'dotSvnDir').toPath()
def target = new File(basedir, '.svn').toPath()
Files.walk(source).withCloseable { paths ->
    paths.each { path ->
        def destination = target.resolve(source.relativize(path).toString())
        if (Files.isDirectory(path)) {
            Files.createDirectories(destination)
        } else {
            Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING)
        }
    }
}

new File(basedir, "foo.txt").text = "hello"
true
