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
- The integration tests are maven-invoker-plugin projects in `src/it`, one directory per fixture with
  an `invoker.properties` (goals), a `verify.groovy` (plain Groovy asserts on `build.log` and the
  built jar or files) and, where needed, a `prebuild.groovy` or `selector.groovy`. Fixture POMs
  take the plugin version as `@project.version@`. `src/it/settings.xml` points at the local
  repository.
- The `run-its` profile activates when `src/it` exists. It installs the plugin into
  `target/local-repo` and runs the fixtures with the Maven that runs the build, so the Maven 4
  CI job tests the plugin on Maven 4. Skip the ITs or run one:

  ```shell
  mvn -B -P '!run-its' test
  mvn -B verify -Dinvoker.test=create-metadata-it
  ```
- The SCM fixtures carry their repository metadata as `dotGitDir` and `dotSvnDir` directories, which
  `prebuild.groovy` copies to `.git` and `.svn` in the cloned project under `target/it`, so the
  fixtures are not nested repositories. `basic-it`, `basic-it-no-devscm` and `basic-it-svnjava`
  have a `selector.groovy` that skips them unless `svn` 1.8 or later is installed.
- `failed-local-change`, `MOJO-1372`, `MOJO-1668`, `norevision-it` (their svn server is gone) and
  `MBUILDNUM-5` (needs a `dotSvnDir`) are disabled by a `selector.groovy` that returns `false`.
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

