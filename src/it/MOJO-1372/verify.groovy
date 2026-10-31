def log = new File(basedir, "build.log").text
assert !log.contains("[ERROR]") : "build.log contains errors"
