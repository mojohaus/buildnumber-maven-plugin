// Needs a Subversion client 1.8 or later; the build is skipped without one.
def output = new StringBuilder()
try {
    def process = ['svn', '--version'].execute()
    process.consumeProcessOutput(output, new StringBuilder())
    process.waitFor()
} catch (IOException e) {
    return false
}
def matcher = output =~ /svn, version (\d+)\.(\d+)\./
return matcher.find() && (matcher.group(1).toInteger() > 1 || matcher.group(2).toInteger() >= 8)
