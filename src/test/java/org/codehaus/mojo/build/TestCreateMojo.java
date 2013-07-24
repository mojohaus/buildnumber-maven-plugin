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
import java.util.List;
import java.util.Locale;

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.blame.BlameScmRequest;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.command.branch.BranchScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.command.mkdir.MkdirScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.unedit.UnEditScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;
import org.codehaus.plexus.PlexusTestCase;

public class TestCreateMojo
    extends PlexusTestCase
{

    protected void setUp()
        throws Exception
    {
        // without this, locale test fails intermittenly depending timezone
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

            assertTrue( "Format didn't match.",
                        rev.matches( "^At (\\d{1,2}:?){3} (AM|PM) on \\w{3} \\d{1,2}, \\d{4}, there was a disturbance in the Force on planet 7." ) );

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
        assertEquals( "trunk", mojo.filterBranchFromScmUrl( scmUrlTrunk ) );
        String scmUrlBranch = "https://mifos.dev.java.net/svn/mifos/branches/v1.2.x";
        assertEquals( "branches/v1.2.x", mojo.filterBranchFromScmUrl( scmUrlBranch ) );
        String scmUrlTag = "https://mifos.dev.java.net/svn/mifos/tags/v1.2.1";
        assertEquals( "tags/v1.2.1", mojo.filterBranchFromScmUrl( scmUrlTag ) );
    }

    public void testSpecialItemScmVersion()
        throws Exception
    {
        CreateMojo mojo = new CreateMojo();
        mojo.setBuildNumberPropertiesFileLocation( new File( getBasedir(), "target/buildNumber.properties" ) );
        mojo.setFormat( "{0}-{1}-{2}" );
        mojo.setItems( Arrays.asList( "buildNumber0", "scmVersion", "buildNumber0" ) );
        File file = new File( getBasedir(), "target/buildNumber.properties" );
        file.delete();
        mojo.setRevisionOnScmFailure( "scmrevision" );
        mojo.setScmManager( new ScmManager()
        {

            public ScmRepository makeScmRepository( String string )
                throws ScmRepositoryException, NoSuchScmProviderException
            {
                throw new ScmRepositoryException( "No SCM for testing." );
            }

            public ScmRepository makeProviderScmRepository( String string, File file )
                throws ScmRepositoryException, UnknownRepositoryStructure, NoSuchScmProviderException
            {
                throw new ScmRepositoryException( "No SCM for testing." );
            }

            public List<String> validateScmRepository( String string )
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public ScmProvider getProviderByUrl( String string )
                throws ScmRepositoryException, NoSuchScmProviderException
            {
                throw new ScmRepositoryException( "No SCM for testing." );
            }

            public ScmProvider getProviderByType( String string )
                throws NoSuchScmProviderException
            {
                throw new NoSuchScmProviderException( "No SCM for testing." );
            }

            public ScmProvider getProviderByRepository( ScmRepository sr )
                throws NoSuchScmProviderException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public void setScmProvider( String string, ScmProvider sp )
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public void setScmProviderImplementation( String string, String string1 )
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public AddScmResult add( ScmRepository sr, ScmFileSet sfs )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public AddScmResult add( ScmRepository sr, ScmFileSet sfs, String string )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public BranchScmResult branch( ScmRepository sr, ScmFileSet sfs, String string )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public BranchScmResult branch( ScmRepository sr, ScmFileSet sfs, String string, String string1 )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public ChangeLogScmResult changeLog( ScmRepository sr, ScmFileSet sfs, Date date, Date date1, int i,
                                                 ScmBranch sb )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public ChangeLogScmResult changeLog( ScmRepository sr, ScmFileSet sfs, Date date, Date date1, int i,
                                                 ScmBranch sb, String string )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public ChangeLogScmResult changeLog( ChangeLogScmRequest changeLogScmRequest )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public ChangeLogScmResult changeLog( ScmRepository sr, ScmFileSet sfs, ScmVersion sv, ScmVersion sv1 )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public ChangeLogScmResult changeLog( ScmRepository sr, ScmFileSet sfs, ScmVersion sv, ScmVersion sv1,
                                                 String string )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public CheckInScmResult checkIn( ScmRepository sr, ScmFileSet sfs, String string )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public CheckInScmResult checkIn( ScmRepository sr, ScmFileSet sfs, ScmVersion sv, String string )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public CheckOutScmResult checkOut( ScmRepository sr, ScmFileSet sfs )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public CheckOutScmResult checkOut( ScmRepository sr, ScmFileSet sfs, ScmVersion sv )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public CheckOutScmResult checkOut( ScmRepository sr, ScmFileSet sfs, boolean bln )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public CheckOutScmResult checkOut( ScmRepository sr, ScmFileSet sfs, ScmVersion sv, boolean bln )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public DiffScmResult diff( ScmRepository sr, ScmFileSet sfs, ScmVersion sv, ScmVersion sv1 )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public EditScmResult edit( ScmRepository sr, ScmFileSet sfs )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public ExportScmResult export( ScmRepository sr, ScmFileSet sfs )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public ExportScmResult export( ScmRepository sr, ScmFileSet sfs, ScmVersion sv )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public ExportScmResult export( ScmRepository sr, ScmFileSet sfs, String string )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public ExportScmResult export( ScmRepository sr, ScmFileSet sfs, ScmVersion sv, String string )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public ListScmResult list( ScmRepository sr, ScmFileSet sfs, boolean bln, ScmVersion sv )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public MkdirScmResult mkdir( ScmRepository sr, ScmFileSet sfs, String string, boolean bln )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public RemoveScmResult remove( ScmRepository sr, ScmFileSet sfs, String string )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public StatusScmResult status( ScmRepository sr, ScmFileSet sfs )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public TagScmResult tag( ScmRepository sr, ScmFileSet sfs, String string )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public TagScmResult tag( ScmRepository sr, ScmFileSet sfs, String string, String string1 )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public UnEditScmResult unedit( ScmRepository sr, ScmFileSet sfs )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public UpdateScmResult update( ScmRepository sr, ScmFileSet sfs )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public UpdateScmResult update( ScmRepository sr, ScmFileSet sfs, ScmVersion sv )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public UpdateScmResult update( ScmRepository sr, ScmFileSet sfs, boolean bln )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public UpdateScmResult update( ScmRepository sr, ScmFileSet sfs, ScmVersion sv, boolean bln )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public UpdateScmResult update( ScmRepository sr, ScmFileSet sfs, String string )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public UpdateScmResult update( ScmRepository sr, ScmFileSet sfs, ScmVersion sv, String string )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public UpdateScmResult update( ScmRepository sr, ScmFileSet sfs, Date date )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public UpdateScmResult update( ScmRepository sr, ScmFileSet sfs, ScmVersion sv, Date date )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public UpdateScmResult update( ScmRepository sr, ScmFileSet sfs, Date date, String string )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public UpdateScmResult update( ScmRepository sr, ScmFileSet sfs, ScmVersion sv, Date date, String string )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public BlameScmResult blame( ScmRepository sr, ScmFileSet sfs, String string )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

            public BlameScmResult blame( BlameScmRequest blameScmRequest )
                throws ScmException
            {
                throw new UnsupportedOperationException( "Not supported yet." );
            }

        } );
        mojo.setUrlScm( "http://nonexistent" );

        mojo.execute();
        String rev = mojo.getRevision();

        System.out.println( "rev = " + rev );
        assertEquals( "1-scmrevision-2", rev );

        // String result = mojo.getRevision() + "-" +
    }

}
