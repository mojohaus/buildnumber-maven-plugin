package org.codehaus.mojo.build;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

public class Utils
{
    private Utils()
    {
    }

    public static String createTimestamp( String timestampFormat )
    {
        Date now = Calendar.getInstance().getTime();

        if ( StringUtils.isBlank( timestampFormat ) )
        {
            return String.valueOf( now.getTime() );
        }
        else
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat( timestampFormat );
            return dateFormat.format( now );
        }
    }

}
