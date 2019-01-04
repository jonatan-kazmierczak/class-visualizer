package clsvis.process.importer;

/**
 * Listener receiving notifications about import progress.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public interface ImportProgressListener {

    /**
     * Notification about import progress: importedCount out of totalCount.
     */
    public void importProgress(int importedCount, int totalCount);
}
