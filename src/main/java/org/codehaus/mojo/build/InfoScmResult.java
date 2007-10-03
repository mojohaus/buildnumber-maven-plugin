package org.codehaus.mojo.build;

/**
 * The MIT License
 *
 * Copyright (c) 2005 Learning Commons, University of Calgary
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmResult;
import org.codehaus.plexus.util.StringInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 *
 * @author <a href="mailto:woodj@ucalgary.ca">Julian Wood</a>
 * @version $Id$
 */
public class InfoScmResult
    extends ScmResult
{

    public InfoScmResult( String commandLine, String commandOutput )
    {
        this( commandLine, null, commandOutput, true );
    }

    /**
     * Available if we need to error.
     *
     * @param commandLine       the line that produced this output
     * @param providerMessage   any result/error message
     * @param commandOutput     the actual output of the command
     * @param success           did it fail?
     */
    public InfoScmResult( String commandLine, String providerMessage, String commandOutput, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, success );
    }

    /**
     * Get the revision number from svn.
     * @return                  the int svn rev, as a string
     * @throws ScmException    if we couldn't parse the 'svn --xml info' result
     */
    public String getRevision() throws ScmException
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse( new StringInputStream( getCommandOutput() ) );

            Node entryNode = document.getDocumentElement().getElementsByTagName( "entry" ).item( 0 );
            Node node = entryNode.getAttributes().getNamedItem( "revision" );

            return node.getNodeValue();
        }
        catch ( ParserConfigurationException e )
        {
            throw new ScmException( "Couldn't locate xml parser.", e );
        }
        catch ( SAXException e )
        {
            throw new ScmException( "Couldn't parse XML.", e );
        }
        catch ( IOException e )
        {
            throw new ScmException( "Couldn't parse XML.", e );
        }

    }
}

