package org.codehaus.mojo.build;

import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.hg.command.HgConsumer;

class HgOutputConsumer
        extends HgConsumer
    {

        private String output;

        HgOutputConsumer( ScmLogger logger )
        {
            super( logger );
        }

        @Override
		public void doConsume( ScmFileStatus status, String line )
        {
            output = line;
        }

        String getOutput()
        {
            return output;
        }
    }

