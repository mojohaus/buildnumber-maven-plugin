package org.codehaus.mojo.build;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

public class Utils
{
    private Utils()
    {
    }

    public static String createTimestamp( String timestampFormat, String timeZoneId)
    {
        return createTimestamp( timestampFormat, timeZoneId, null);
    }

    public static String createTimestamp( String timestampFormat, String timeZoneId, Date now)
    {
        if ( null == now)
            now = Calendar.getInstance().getTime();

        if ( StringUtils.isBlank( timestampFormat ) )
        {
            return String.valueOf( now.getTime() );
        }
        else
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat( timestampFormat );
            dateFormat.setTimeZone(getTimeZone(timeZoneId));
            return dateFormat.format( now );
        }
    }
    private static TimeZone getTimeZone(String timeZoneId) {
        TimeZone timeZone = TimeZone.getDefault();
        if (StringUtils.isNotBlank(timeZoneId)) {
            timeZone =  TimeZone.getTimeZone(timeZoneId);
        }
        return timeZone;
    }
}
