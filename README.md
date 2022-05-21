# MojoHaus BuildNumber Maven Plugin

This is the [buildnumber-maven-plugin](http://www.mojohaus.org/buildnumber-maven-plugin/).
 
[![The MIT License](https://img.shields.io/github/license/mojohaus/buildnumber-maven-plugin.svg?label=License)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/buildnumber-maven-plugin.svg?label=Maven%20Central)](https://search.maven.org/artifact/org.codehaus.mojo/buildnumber-maven-plugin)
[![Build Status](https://github.com/mojohaus/buildnumber-maven-plugin/workflows/GitHub%20CI/badge.svg?branch=master)](https://github.com/mojohaus/buildnumber-maven-plugin/actions/workflows/maven.yml?query=branch%3Amaster)

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
