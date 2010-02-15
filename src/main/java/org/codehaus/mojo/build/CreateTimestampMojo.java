package org.codehaus.mojo.build;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

/**
 * This mojo is designed to give you a timestamp available through one or more properties.
 * Only a single timestamp is created for each execution of the mojo.  This timestamp can
 * be format into one or more strings which are then saved to properties. 
 * 
 * @author pgier
 * @version $Id$
 * @goal create-timestamp
 * @since 1.0-beta-5
 * @requiresProject
 * @description create a timestamp property
 */
public class CreateTimestampMojo
    extends AbstractMojo
{

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @readonly
     */
    private MavenProject project;

    /**
     * Contains the full list of projects in the reactor.
     *
     * @parameter expression="${reactorProjects}"
     * @readonly
     */
    private List reactorProjects;

    /**
     * You can rename the timestamp property name to another property name if desired.
     * 
     * @parameter expression="${maven.buildNumber.timestampPropertyName}" default-value="timestamp"
     */
    private String timestampPropertyName;
    
    /**
     * Apply this java.text.SimpleDateFormat to the timestamp.
     * 
     * @parameter expression="${maven.buildNumber.timestampFormat}" default-value=""
     */
    private String timestampFormat;

    public void execute()
    {
        String timestampString = project.getProperties().getProperty( timestampPropertyName );
        
        // Check if the plugin has already run in the current build.
        if ( timestampString != null )
        {
            getLog().debug( "Using previously created timestamp." );
            return;
        }
        
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        
        if ( timestampFormat == null || timestampFormat.equals( "" ))
        {
            timestampString = String.valueOf( now.getTime() );
        }
        else
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat( timestampFormat );
            timestampString = dateFormat.format( now );            
        }
        
        getLog().debug( "Storing timestamp property: " + timestampPropertyName + " " + timestampString );
        
        Iterator projIter = reactorProjects.iterator();
        while ( projIter.hasNext() )
        {
            MavenProject nextProj = (MavenProject) projIter.next();
            nextProj.getProperties().setProperty( this.timestampPropertyName, timestampString );
        }
        
    }
     
}
