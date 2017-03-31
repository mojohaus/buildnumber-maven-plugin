# MojoHaus BuildNumber Maven Plugin

This is the [buildnumber-maven-plugin](http://www.mojohaus.org/buildnumber-maven-plugin/).
 
[![The MIT License](https://img.shields.io/github/license/mojohaus/buildnumber-maven-plugin.svg?label=License)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/buildnumber-maven-plugin.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.codehaus.mojo%22%20AND%20a%3A%22buildnumber-maven-plugin%22)
[![Build Status](https://travis-ci.org/mojohaus/buildnumber-maven-plugin.svg?branch=master)](https://travis-ci.org/mojohaus/buildnumber-maven-plugin)

## Releasing

* Make sure `gpg-agent` is running.
* Make sure all tests pass `mvn clean verify -Prun-its`
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
```
