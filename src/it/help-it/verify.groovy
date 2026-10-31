def log = new File(basedir, "build.log").text
assert !log.contains("[ERROR]") : "build.log contains errors"
assert log.contains("buildnumber:create")
assert log.contains("This mojo is designed to give you a build number")
assert log.contains("buildnumber:create-metadata")
assert log.contains("buildnumber:create-timestamp")
