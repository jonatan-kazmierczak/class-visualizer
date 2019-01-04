package clsvis.logging;

import java.io.ByteArrayOutputStream;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

/**
 * Logging StreamHandler which sends output to the assigned JTextArea.
 *
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public final class GUIHandler extends StreamHandler {

    private static final int MAX_LINES = 0x200;

    private static JTextArea textArea;

    private final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream( 0x2000 );

    public GUIHandler() {
        setOutputStream( outputBuffer );
    }

    @Override
    public synchronized void publish(LogRecord record) {
        super.publish( record );
        flush();
        if (textArea != null) {
            SwingUtilities.invokeLater( () -> {
                // Clean the overflowing part of the log
                int lineCount = textArea.getLineCount();
                if (lineCount > MAX_LINES) {
                    try {
                        textArea.replaceRange( "...\n", 0, textArea.getLineEndOffset( lineCount - MAX_LINES - 1 ) );
                    } catch (BadLocationException ignored) {
                    }
                }
                // Show new log entry
                String logMsg = outputBuffer.toString();
                outputBuffer.reset(); // Has to be done immediately after previous line to avoid deleting unread data
                textArea.append( logMsg );
            } );
        }
    }

    /**
     * Sets reference of the logging component to the provided one.
     */
    public static void setTextArea(JTextArea textArea) {
        GUIHandler.textArea = textArea;
    }
}
