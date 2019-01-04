package clsvis.gui.worker;

import clsvis.Utils;
import clsvis.gui.ConstantValues;
import clsvis.gui.MainFrame;
import clsvis.model.ProjectConfig;
import java.io.File;
import javax.xml.bind.JAXB;

/**
 * Saves project file.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public class SaveProjectTask extends BaseTask<Void, Void> {

    private final ProjectConfig projectConfig;

    public SaveProjectTask(MainFrame mainFrame) {
        super( mainFrame );
        projectConfig = mainFrame.getProjectConfig();
    }

    public SaveProjectTask(MainFrame mainFrame, File path) {
        this( mainFrame );
        projectConfig.path = path.getName().endsWith( ConstantValues.PROJECT_FILE_EXTENSION )
                ? path
                : new File( path.getAbsolutePath() + '.' + ConstantValues.PROJECT_FILE_EXTENSION );
    }

    @Override
    protected Void doInBackground() {
        JAXB.marshal( projectConfig, projectConfig.path );
        return null;
    }

    @Override
    protected void onSuccessGuiUpdate(Void result) {
        logInfo( "Project saved successfully: " + projectConfig.path );
        mainFrame.setTitle( projectConfig.path.toString() );
        super.onSuccessGuiUpdate( result );
    }

    @Override
    protected void onFailureGuiUpdate(Exception cause) {
        logSevere( "Project saving failed: " + Utils.rootCauseAsString( cause ) );
        logger.throwing( "", "", cause );
        projectConfig.path = null;
    }
}
