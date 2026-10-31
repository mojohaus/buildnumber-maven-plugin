def log = new File(basedir, "build.log").text
assert !log.contains("[ERROR]") : "build.log contains errors"

for (path in ["target/file1.json", "target/xxx/file1.json", "target/generated/build-metadata/build.properties", "target/classes/build.properties"]) {
    assert new File(basedir, path).exists() : path + " is missing"
}
