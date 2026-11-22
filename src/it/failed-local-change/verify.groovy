try {
    def log = new File(basedir, "build.log").text
    assert log.contains("BUILD FAILURE")
    assert log.contains("because you have local modifications")
    assert log.contains("[foo.txt:modified]") : "foo.txt should be the only local modification"
    assert !log.contains("[pom.xml:modified]")
} finally {
    // Git writes its objects read-only, and on Windows the clean of the next build cannot delete them
    def git = new File(basedir, ".git")
    git.eachFileRecurse { it.setWritable(true) }
    git.deleteDir()
}
