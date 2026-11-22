# MojoHaus BuildNumber Maven Plugin

[![The MIT License](https://img.shields.io/github/license/mojohaus/buildnumber-maven-plugin.svg?label=License)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/buildnumber-maven-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/org.codehaus.mojo/buildnumber-maven-plugin)
[![Build Status](https://github.com/mojohaus/buildnumber-maven-plugin/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/mojohaus/buildnumber-maven-plugin/actions/workflows/maven.yml?query=branch%3Amaster)

The BuildNumber Maven Plugin gives each build a unique identifier, taken from the SCM revision, a
sequence or a timestamp, and exposes it as a property, for example for a jar manifest or a file
name. It also writes build metadata files. It requires Maven 3.9.0 or later and Java 8 or later.

The [plugin site](https://www.mojohaus.org/buildnumber-maven-plugin/) lists the goals and has a
[usage page](https://www.mojohaus.org/buildnumber-maven-plugin/usage.html). Contributor and agent
notes are in [AGENTS.md](AGENTS.md).

## Releasing

* Make sure `gpg-agent` is running.
* subversion `svn` is also needed for running tests
* Make sure all tests pass `mvn clean verify -Prun-its`
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
```

