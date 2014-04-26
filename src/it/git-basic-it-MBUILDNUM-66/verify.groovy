/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.*;
import java.util.*;
import java.util.jar.*;
import org.codehaus.plexus.util.*;

File target = new File( basedir, "target" );
assert target.exists();
assert target.isDirectory();

File artifact = new File ( target, "buildnumber-maven-plugin-basic-it-1.0-SNAPSHOT.jar" );
assert artifact.exists();
assert !artifact.isDirectory();

JarFile jar = new JarFile( artifact );

Attributes manifest = jar.getManifest().getMainAttributes();

String scmRev = manifest.get( new Attributes.Name( "SCM-Revision" ) );
if ( scmRev == null || scmRev.length() < 1 )
{
    System.err.println( "No manifest entry SCM-Revision" );
    return false;    
}  
// assert we can parse it as long
if(!"ee58acb27b6636a497c1185f80cd15f76134113f".equals(scmRev)) 
{
    System.err.println("Bad revision retrieved: "+scmRev);
    return false;
}

File buildLog = new File( basedir, "build.log" );
assert buildLog.exists();
assert !buildLog.isDirectory();
assert buildLog.text.contains("Storing buildScmBranch: master");

return true;