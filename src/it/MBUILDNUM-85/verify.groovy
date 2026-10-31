def log = new File(basedir, "build.log").text
assert log.contains("BUILD FAILURE")
