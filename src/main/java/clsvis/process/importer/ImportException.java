package clsvis.process.importer;

/**
 * Exception for reporting problems during import.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public class ImportException extends RuntimeException {

    public ImportException(Throwable cause) {
        super( cause );
    }
}
