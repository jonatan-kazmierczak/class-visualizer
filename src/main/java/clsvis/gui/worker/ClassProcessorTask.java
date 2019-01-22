package clsvis.gui.worker;

import clsvis.ProjectConfigIO;
import clsvis.Utils;
import clsvis.gui.ConstantValues;
import clsvis.gui.MainFrame;
import clsvis.model.ProjectConfig;
import clsvis.process.importer.BaseProjectImporter;
import clsvis.process.importer.CompiledClassImporter;
import clsvis.process.importer.ImportProgressListener;
import java.io.IOException;

/**
 * Class processor, responsible for the following tasks (executed sequentially):
 * - unmarshal projectData from XML file
 * - add jars to classpath
 * - import classes
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public class ClassProcessorTask extends BaseTask<Void, Void> implements ImportProgressListener {

    private final ProjectConfig config;
    private final ProjectConfig mainProjectConfig;
    private final BaseProjectImporter projectImporter;
    private boolean somethingToBeDone;
    private boolean refreshUI;
    private String projectTitle;
    private String errorMessage;

    public ClassProcessorTask(MainFrame mainFrame, ProjectConfig config) {
        super( mainFrame );
        this.config = config;
        mainProjectConfig = mainFrame.getProjectConfig();
        projectImporter = mainFrame.getProjectImporter();
    }

    @Override
    protected Void doInBackground() throws Exception {
        if (config.isPathToBeUsedForProjectLoad()) {
            loadProjectConfig();
            projectTitle = config.path.toString();
        } else if (config.isPathSet()) {
            // Modification of previously saved project
            projectTitle = config.path.toString() + " (modified)";
        } else {
            // New project
            projectTitle = ConstantValues.NEW_PROJECT_TITLE;
        }

        if (!config.classPaths.isEmpty()) {
            somethingToBeDone = true;
            addToClasspath();
        }
        if (!config.importPaths.isEmpty()) {
            somethingToBeDone = true;
            importClasses();
        }
        if (!somethingToBeDone) {
            logWarning( "Nothing to be done" );
        }
        return null;
    }

    /** Sets the given error message only if {@link #errorMessage} is null. */
    private void setErrorMessage(String errorMessage) {
        if (this.errorMessage == null) {
            this.errorMessage = errorMessage;
        }
    }

    private void loadProjectConfig() throws IOException {
        logInfo( "Opening project: " + config.path );
        setErrorMessage( "Error during opening project: " );

        ProjectConfig newConfig = ProjectConfigIO.load( config.path );
        config.setContent( config.path, newConfig );
        mainProjectConfig.setContent( config.path, newConfig );
    }

    private void addToClasspath() {
        logInfo( "Adding elements to the class path" );
        setErrorMessage( "Error during adding elements to the classpath: " );

        int classPathsCountBefore = projectImporter.getClassPaths().size();
        projectImporter.addClassPaths( config.classPaths );
        int finalClassPathsCount = projectImporter.getClassPaths().size();
        int delta = finalClassPathsCount - classPathsCountBefore;

        if (delta > 0) {
            mainProjectConfig.addContent( config );
            logInfo(
                    String.format( "%d new element(s) added to class path; total count: %d", delta, finalClassPathsCount )
            );
        } else {
            logWarning( "No new elements added to class path" );
        }
    }

    private void importClasses() {
        logInfo( "Loading classes" );
        setErrorMessage( "Error during project import: " );

        CompiledClassImporter classImporter = projectImporter.getClassImporter();
        classImporter.setImportProgressListener( this );

        int startClassesCount = classImporter.getImportedClasses().size();
        projectImporter.importProject( config.importPaths );

        int endClassesCount = classImporter.getImportedClasses().size();
        int failedClassesCount = classImporter.getNotImportedClassesCount();
        int delta = endClassesCount - startClassesCount;

        if (delta > 0) {
            mainProjectConfig.addContent( config );
            refreshUI = true;
            String msg = failedClassesCount == 0
                    ? String.format(
                            "Project imported successfully: %d classes.", delta )
                    : String.format( "Project imported with some problems: %d classes (%d classes not imported). "
                            + "HINT: Add missing dependencies with 'File -> Add Required Libraries'",
                            delta, failedClassesCount );
            logInfo( msg );
        } else {
            logWarning( "No classes imported" );
        }

        projectImporter.cleanupAfterImport();
    }

    @Override
    protected void onSuccessGuiUpdate(Void result) {
        if (refreshUI) {
            mainFrame.setTitle( projectTitle );
            mainFrame.showClasses();
        }
        mainFrame.stopProgress();
    }

    @Override
    protected void onFailureGuiUpdate(Exception cause) {
        logSevere( errorMessage + Utils.rootCauseAsString( cause ) );
        logger.throwing( "", "", cause );
        config.path = null;
        projectImporter.cleanupAfterImport();
        mainFrame.stopProgress();
    }

    /**
     * Progress listener implementation.
     */
    @Override
    public void importProgress(int importedCount, int totalCount) {
        setProgress( importedCount * 100 / totalCount );
    }
}
