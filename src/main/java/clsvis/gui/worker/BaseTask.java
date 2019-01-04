package clsvis.gui.worker;

import clsvis.Utils;
import clsvis.gui.MainFrame;
import java.beans.PropertyChangeEvent;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 * Base class for background tasks.<br>
 * This class introduces:
 * - logging functionality
 * - handling projects history
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public abstract class BaseTask<T, V> extends SwingWorker<T, V> {

    protected final Logger logger = Logger.getLogger( getClass().getName() );

    protected final MainFrame mainFrame;

    public BaseTask(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        addPropertyChangeListener();
    }

    private void addPropertyChangeListener() {
        addPropertyChangeListener( (PropertyChangeEvent event) -> {
            switch (event.getPropertyName()) {
                case "state":
                    onStateChange( (StateValue) event.getNewValue() );
                    break;
                case "progress":
                    onProgressGuiUpdate( (int) event.getNewValue() );
                    break;
            }
        } );
    }

    private void onStateChange(StateValue state) {
        switch (state) {
            case STARTED:
                beforeStartedGuiUpdate();
                break;
        }
    }

    protected void beforeStartedGuiUpdate() {
        mainFrame.startProgress();
    }

    protected void onProgressGuiUpdate(int progressPercent) {
        mainFrame.moveProgress( progressPercent );
    }

    @Override
    protected void done() {
        try {
            onSuccessGuiUpdate( get() );
        } catch (InterruptedException | ExecutionException e) {
            onFailureGuiUpdate( e );
        } finally {
            mainFrame.stopProgress();
        }
    }

    protected void onSuccessGuiUpdate(T result) {
    }

    protected void onFailureGuiUpdate(Exception cause) {
        logSevere( Utils.rootCauseAsString( cause ) );
        logger.throwing( "", "", cause );
    }

    protected void log(Level level, String message) {
        mainFrame.setStatusMessage( message );
        logger.log( level, message );
    }

    protected void logInfo(String message) {
        log( Level.INFO, message );
    }

    protected void logWarning(String message) {
        log( Level.WARNING, message );
    }

    protected void logSevere(String message) {
        log( Level.SEVERE, message );
    }
}
