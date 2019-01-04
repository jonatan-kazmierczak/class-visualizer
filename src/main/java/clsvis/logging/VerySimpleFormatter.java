package clsvis.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Very simple formatter of LogLecord.<br>
 * It produces entry of the form:
 * <tt>timestamp level message &lt;new_line&gt; optional_stack_trace</tt>.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public class VerySimpleFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        String stackTrace = "";
        // Block copied from java.util.logging.SimpleFormatter#format and adapted
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter( 0x1000 );
            try (PrintWriter pw = new PrintWriter( sw )) {
                record.getThrown().printStackTrace( pw );
                stackTrace = sw.toString();
            }
        }
        return String.format( "%s %s %s%n%s",
                new Timestamp( record.getMillis() ), record.getLevel(), formatMessage( record ), stackTrace );
    }
}
