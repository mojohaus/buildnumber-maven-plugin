title: Frequently Asked Questions

<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

# Frequently Asked Questions

**General**

1. [Can the plugin be used on a project without an SCM connection?](#missing-scm)

## General

<a id="missing-scm"></a>

### Can the plugin be used on a project without an SCM connection?

Yes, set the plugin parameter `<revisionOnScmFailure>` to prevent
the plugin from throwing an exception if SCM information is not found.

The other option is to use `-Dmaven.buildNumber.ignoreEmptyScmUrl=true -Dmaven.buildNumber.scmProvider=git/svn/`
Default is git.
