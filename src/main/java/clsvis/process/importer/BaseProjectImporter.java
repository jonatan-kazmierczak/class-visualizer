package clsvis.process.importer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Base importing functionality:<br/>
 * Imports all classes included in provided path: jar file or directory.
 *
 * @author Jonatan Kazmierczak [Jonatan.Kazmierczak (at) gmail (dot) com]
 */
public class BaseProjectImporter {

    private static final Logger logger = Logger.getLogger( BaseProjectImporter.class.getName() );

    public static final String jarFileName = ".jar";
    private static final String classFileSuffix = ".class";

    protected final CompiledClassImporter classImporter = new CompiledClassImporter();
    protected Collection<File> classPaths = new LinkedHashSet<>();
    protected final List<String> classNames = new ArrayList<>( 0x4000 );

    /**
     * Import project pointed by the given path.
     */
    public void importProject(Collection<File> paths) {
        classNames.clear();

        try {
            Runtime runtime = Runtime.getRuntime();

            logger.log( Level.FINE, "Used memory before import: {0} MB",
                    (runtime.totalMemory() - runtime.freeMemory()) >> 20 );
            findClassNames( paths );
            logger.log( Level.CONFIG, "Project size: {0} top-level classes", classNames.size() );

            if (!classNames.isEmpty()) {
                // Adding imported projects paths on the beginning of classpaths
                Collection<File> originalClassPaths = classPaths;
                classPaths = new LinkedHashSet<>(
                        ((originalClassPaths.size() + paths.size()) * 4 + 2) / 3 );
                classPaths.addAll( paths );
                classPaths.addAll( originalClassPaths );

                initClassLoader();
                runClassesImport();
                logger.log( Level.FINE, "Used memory after import: {0} MB",
                        (runtime.totalMemory() - runtime.freeMemory()) >> 20 );
            }
        } catch (IOException e) {
            logThrowable( e );
            throw new ImportException( e );
        } catch (ImportException e) {
            // Already logged
            throw e;
        } catch (RuntimeException | Error e) {
            logThrowable( e );
            throw e;
        }
    }

    /**
     * Does some memory cleanup after import.
     */
    public void cleanupAfterImport() {
        try {
            // in case of exception with paths, classLoader can be null here
            URLClassLoader classLoader = classImporter.getClassLoader();
            if (classLoader != null) {
                classLoader.close();
            }
        } catch (IOException e) {
            logThrowable( e );
        }
        classImporter.setClassLoader( null );
        classImporter.setImportProgressListener( null );
        classNames.clear();
        //System.gc();
        // TODO: try to improve releasing of refs to loaded classes
    }

    /**
     * Searches for classes in the given mainClassesDir and its subdirectories. It fills-in {@link #classNames}.
     */
    private void findClassNamesInDirectory(File classesDir, Collection<String> subDirNames) {
        File currPath = classesDir;
        for (String subDirName : subDirNames) {
            currPath = new File( currPath, subDirName );
        }
        String currPathName = currPath.getPath();
        if (currPathName.startsWith( "." )) {
            return; // ignore . .. .svn and others
        }
        if (currPath.isDirectory()) {
            // Process directory entries
            for (String dirEntryName : currPath.list()) {
                // process sub-entries
                ArrayList<String> newSubDirNames = new ArrayList<>( subDirNames );
                newSubDirNames.add( dirEntryName );
                findClassNamesInDirectory( classesDir, newSubDirNames );
            }
        } else // Process file
        if (isTopLevelClass( currPathName )) {
            // load top level class
            StringBuilder classNameSB = new StringBuilder( 0x80 );
            for (String subDirName : subDirNames) {
                classNameSB.append( subDirName ).append( '.' );
            }
            classNameSB.setLength( classNameSB.length() - 7 ); // Remove suffix ".class."
            //System.out.println(classNameSB);
            classNames.add( classNameSB.toString() );
        }
    }

    /**
     * Searches for classes in the given jarFile. It fills-in {@link #classNames}.
     */
    private void findClassNamesInJarFile(File jarFile) throws IOException {
        try (ZipFile zipFile = new ZipFile( jarFile )) {
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                String entryName = zipEntries.nextElement().getName();
                if (isTopLevelClass( entryName )) {
                    classNames.add( entryName.replace( '/', '.' ).substring( 0, entryName.length() - 6 ) ); // Remove ".class"
                }
            }
        }
    }

    /**
     * Searches for classes on the given paths (jar files or directories).
     */
    protected void findClassNames(Collection<File> paths) throws IOException {
        for (File path : paths) {
            findClassNames( path );
        }
    }

    /**
     * Searches for classes on the given path (jar file or directory).
     */
    protected void findClassNames(File path) throws IOException {
        if (path == null) {
            throw new IOException( "No path available" );
        }
        checkPathExists( path );

        if (path.isDirectory()) {
            findClassNamesInDirectory( path, Collections.EMPTY_LIST );
        } else if (path.getName().endsWith( jarFileName )) {
            findClassNamesInJarFile( path );
        } else {
            throw new IOException( String.format( "Unsupported type of path '%s'", path.getName() ) );
        }
    }

    private static boolean isTopLevelClass(String entry) {
        return entry.endsWith( classFileSuffix ) && !entry.contains( "$" );
    }

    /**
     * Adds the given paths to the class paths.
     */
    public void addClassPaths(Collection<File> paths) {
        //logger.log(Level.CONFIG, "{0} elements to add to class path", paths.length);
        for (File path : paths) {
            findJarsInDirectory( path );
        }
    }

    /**
     * Adds all jars found in the given currPath and its subdirs to the class paths.
     */
    private void findJarsInDirectory(File currPath) {
        String currPathName = currPath.getPath();
        if (currPathName.startsWith( "." )) {
            return; // ignore . .. .svn .git and others
        }
        if (currPath.isDirectory()) {
            // Process directory entries
            File[] dirEntries = currPath.listFiles();
            // Revers sort - to have greater lib versions before lower on class path
            Arrays.sort( dirEntries, Collections.reverseOrder() );
            for (File dirEntry : dirEntries) {
                findJarsInDirectory( dirEntry );
            }
        } else // Process files
        if (currPathName.endsWith( jarFileName )) {
            try {
                logger.log( Level.FINEST, "Adding to class path: {0}", currPath );
                checkPathExists( currPath );
                classPaths.add( currPath );
            } catch (FileNotFoundException ex) {
                logger.log( Level.WARNING, "Problem during adding element to class path: {0}", ex.toString() );
                logThrowable( ex );
            }
        }
    }

    /**
     * Initializes class loader according to {@link #classPaths}.
     */
    protected void initClassLoader() {
        Collection<URL> classpathURLs = new ArrayList<>( classPaths.size() );
        for (File classPath : classPaths) {
            try {
                if (!classPath.exists()) {
                    logger.warning( String.format( "Classpath entry '%s' doesn't exist", classPath.getPath() ) );
                }
                classpathURLs.add( classPath.toURI().toURL() );
            } catch (MalformedURLException ex) {
                //System.err.println(ex);
                logger.log( Level.WARNING,
                        String.format( "Problem during processing classpath entry '%s':", classPath.getPath() ), ex );
            }
        }
        URLClassLoader classLoader = new URLClassLoader( classpathURLs.toArray( new URL[classpathURLs.size()] ) );
        classImporter.setClassLoader( classLoader );
    }

    /**
     * Runs actual import.
     */
    protected void runClassesImport() {
        classImporter.importClasses( classNames );
    }

    /**
     * Checks existence of the given path.
     * If not exists, exception is thrown.
     */
    protected static void checkPathExists(File path) throws FileNotFoundException {
        if (!path.exists()) {
            throw new FileNotFoundException( path.getPath() );
        }
    }

    /**
     * Logs throwable.
     */
    protected static void logThrowable(Throwable t) {
        logger.throwing( "", "", t );
    }

    /**
     * Returns reference to class importer hold by instance of this class.
     */
    public CompiledClassImporter getClassImporter() {
        return classImporter;
    }

    /**
     * Returns class names to import collected by {@link #collectClassNames(java.io.File, java.util.Collection)}.
     */
    public Collection<String> getClassNames() {
        return classNames;
    }

    /**
     * Returns class paths collected by {@link #importProjectInternal(java.io.File)}.
     */
    public Collection<File> getClassPaths() {
        return classPaths;
    }
}
