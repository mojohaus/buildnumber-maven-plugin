def log = new File(basedir, "build.log").text
assert log.contains("proceeding with revision of NOREVISION") : "revisionOnScmFailure was not applied"
assert log.contains("Storing buildNumber: NOREVISION")
assert log.contains("BUILD SUCCESS")
