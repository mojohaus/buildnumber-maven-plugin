# Agent guide for buildnumber-maven-plugin

This file is for automated agents and human contributors working on this repository. It records
what the code and the POMs do not make obvious.

## Layout

- One module with the goals `create` (`CreateMojo`), `create-metadata` (`CreateMetadataMojo`),
  `create-timestamp` (`CreateTimestampMojo`) and `hgchangeset` (`HgChangeSetMojo`), in
  `org.codehaus.mojo.build`. `AbstractScmMojo` holds the SCM handling they share; the metadata
  file formats are `PropertiesOutputFormat` and `JsonOutputFormat`.
- The site is built from `src/site` (`apt` and `fml` pages).

## Build gates

- Main code must use the Java 8 API: mojo-parent sets `maven.compiler.release` to 8.
- Spotless runs on JDK 17 and later only. It applies the formatting in `process-sources` on a local
  build and checks it when the `CI` environment variable is set. It formats Java with
  palantir-java-format and also reformats Markdown, so run `mvn spotless:apply` after editing the
  README or this file.
- The plugin compiles against Maven 3.9.16 (`mavenVersion`) and declares a literal
  `<prerequisites><maven>3.9.0</maven>`; keep the two apart, so raising `mavenVersion` does not
  raise the prerequisite. Building the plugin itself needs Maven 3.9 (`minimalMavenBuildVersion`).
- `.github/workflows/maven.yml` calls the shared `apache/maven-gh-actions-shared`
  `maven-verify.yml@v5` workflow. Branches in this repository build under the push event.

## Tests

- JUnit 5 only. The unit tests are `TestCreateMojo` and the output format tests in
  `src/test/java/org/codehaus/mojo/build`.
- The integration tests are JUnit 5 classes in the `it` package (`BuildNumberMojoTest`,
  `CreateMetadataMojoTest`) that extend `AbstractMavenIT`. They use Takari
  `takari-plugin-integration-testing` to run a Maven installation that the build unpacks to
  `target/maven-installation` (`maven.it.version`) against the fixture projects in
  `src/test/projects`, with `src/test/projects/settings.xml` pointing at the local repository.
- The `run-its` profile is active by default and sets up that Maven installation. Without it the
  `**/it/**` classes are excluded:

  ```shell
  mvn -B -P '!run-its' test
  mvn -B verify -Dtest=CreateMetadataMojoTest
  ```
- The SCM fixtures carry their repository metadata as `dotGitDir` and `dotSvnDir` directories, which
  the tests copy to `.git` and `.svn` in the working copy, so the fixtures are not nested
  repositories. `basicItTest` runs only when `svn` 1.8 or later is installed.
- Add an integration test only for what needs a real Maven build, such as the lifecycle binding or
  the manifest entries; prefer a unit test otherwise.

## Dependencies

- Deliberate pins in `.github/dependabot.yml`: Maven artifacts stay below 3.10.0, the Maven 3.9.x
  line being the plugin baseline, and plexus-xml below 4.0.0 because 4.x depends on the Maven 4
  XML API.

## Pull requests

- Pull requests are squash- or rebase-merged; merge commits are disabled. Release Drafter builds
  the release notes from the titles and labels, so the title should describe the change for a
  user.

