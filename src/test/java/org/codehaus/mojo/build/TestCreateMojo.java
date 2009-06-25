package org.codehaus.mojo.build;

/**
 * The MIT License
 * 
 * Copyright (c) 2005 Learning Commons, University of Calgary
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.io.File;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import org.codehaus.plexus.PlexusTestCase;

public class TestCreateMojo
    extends PlexusTestCase
{

    protected void setUp()
        throws Exception
    {
        //without this, locale test fails intermittenly depending timezone 
        System.setProperty( "user.timezone", "UTC" );
        
        super.setUp();
    }

    public void testMessageFormat()
        throws Exception
    {
        CreateMojo mojo = new CreateMojo();
        mojo.setFormat( "At {1,time} on {1,date}, there was {2} on planet {0,number,integer}." );
        mojo.setItems( Arrays.asList( new Object[] { new Integer( 7 ), "timestamp", "a disturbance in the Force" } ) );

        Locale currentLocale = Locale.getDefault();
        try
        {
            Locale.setDefault( Locale.US );

            mojo.execute();

            String rev = mojo.getRevision();

            System.out.println( "rev = " + rev );

            assertTrue(
                        "Format didn't match.",
                        rev
                            .matches( "^At (\\d{1,2}:?){3} (AM|PM) on \\w{3} \\d{1,2}, \\d{4}, there was a disturbance in the Force on planet 7." ) );

        }
        finally
        {
            Locale.setDefault( currentLocale );
        }
    }

    /**
     * Test that dates are correctly formatted for different locales.
     */
    public void testLocale()
        throws Exception
    {
        Date date = new Date( 0 ); // the epoch
        CreateMojo mojo = new CreateMojo();

        mojo.setFormat( "{0,date}" );
        mojo.setItems( Arrays.asList( new Object[] { date } ) );

        mojo.execute();
        assertEquals( DateFormat.getDateInstance( DateFormat.DEFAULT ).format( date ), mojo.getRevision() );

        mojo.setLocale( "en" );
        mojo.execute();
        assertEquals( "Jan 1, 1970", mojo.getRevision() );

        mojo.setLocale( "fi" );
        mojo.execute();
        assertEquals( "1.1.1970", mojo.getRevision() );

        mojo.setLocale( "de" );
        mojo.execute();
        assertEquals( "01.01.1970", mojo.getRevision() );
    }

    public void testSequenceFormat()
        throws Exception
    {
        CreateMojo mojo = new CreateMojo();
        mojo.setBuildNumberPropertiesFileLocation( new File( getBasedir(), "target/buildNumber.properties" ) );
        mojo.setFormat( "{0,number}.{1,number}.{2,number}" );
        mojo.setItems( Arrays.asList( new Object[] { "buildNumber0", "buildNumber1", "buildNumber2" } ) );

        File file = new File( getBasedir(), "target/buildNumber.properties" );
        file.delete();

        mojo.execute();

        String rev = mojo.getRevision();

        System.out.println( "rev = " + rev );

        assertTrue( "Format didn't match.", rev.matches( "(\\d+\\.?){3}" ) );

        assertTrue( file.exists() );

        // for tests, we don't want this hanging around
        file.delete();

    }

    public void testFilterBranchFromScmUrl()
    {
    	CreateMojo mojo = new CreateMojo();
    	String scmUrlTrunk = "https://mifos.dev.java.net/svn/mifos/trunk";
    	assertEquals("trunk", mojo.filterBranchFromScmUrl(scmUrlTrunk));
    	String scmUrlBranch = "https://mifos.dev.java.net/svn/mifos/branches/v1.2.x";
    	assertEquals("branches/v1.2.x", mojo.filterBranchFromScmUrl(scmUrlBranch));
    	String scmUrlTag = "https://mifos.dev.java.net/svn/mifos/tags/v1.2.1";
    	assertEquals("tags/v1.2.1", mojo.filterBranchFromScmUrl(scmUrlTag));
    }
}
