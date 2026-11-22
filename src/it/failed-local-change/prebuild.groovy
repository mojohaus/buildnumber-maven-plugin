// Commit the project as it is, then modify one tracked file, so that the working copy has exactly one local modification.
def git = { String... args ->
    def command = ['git', '-c', 'user.name=IT', '-c', 'user.email=it@example.org', '-c', 'commit.gpgsign=false'] + args.toList()
    def process = command.execute(null, basedir)
    def output = new StringBuilder()
    process.consumeProcessOutput(output, output)
    assert process.waitFor() == 0 : "git ${args.join(' ')} failed: ${output}"
}

git('init', '-q')
git('add', 'pom.xml', 'foo.txt')
git('commit', '-q', '-m', 'Initial commit')

new File(basedir, "foo.txt").text = "modified at " + System.nanoTime()
true
