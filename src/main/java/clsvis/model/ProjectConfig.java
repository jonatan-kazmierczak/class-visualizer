package clsvis.model;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;

/**
 * Holds project configuration.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public final class ProjectConfig {

    public enum PersistentFields { classPaths, importPaths }


    /** Path where the config is stored on the file system. */
    public File path;

    public Collection<File> classPaths = new LinkedHashSet<>();

    public Collection<File> importPaths = new LinkedHashSet<>();

    public ProjectConfig() {
    }

    public ProjectConfig(File path) {
        this.path = path;
    }

    public ProjectConfig(File path, Collection<File> classPaths, Collection<File> importPaths) {
        this.path = path;
        this.classPaths.addAll( classPaths );
        this.importPaths.addAll( importPaths );
    }

    public ProjectConfig(File path, File[] classPaths, File... importPaths) {
        this( path, Arrays.asList( classPaths ), Arrays.asList( importPaths ) );
    }

    public void setContent(File path, ProjectConfig config) {
        this.path = path;
        classPaths = config.classPaths;
        importPaths = config.importPaths;
    }

    public void addContent(ProjectConfig config) {
        classPaths.addAll( config.classPaths );
        importPaths.addAll( config.importPaths );
    }

    public boolean isPathSet() {
        return path != null;
    }

    public boolean isPathToBeUsedForProjectLoad() {
        return path != null && classPaths.isEmpty() && importPaths.isEmpty();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode( this.path );
        hash = 17 * hash + Objects.hashCode( this.classPaths );
        hash = 17 * hash + Objects.hashCode( this.importPaths );
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProjectConfig other = (ProjectConfig) obj;
        if (!Objects.equals( this.path, other.path )) {
            return false;
        }
        if (!Objects.equals( this.classPaths, other.classPaths )) {
            return false;
        }
        if (!Objects.equals( this.importPaths, other.importPaths )) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ProjectConfig{" + "path=" + path + ", classPaths=" + classPaths + ", importPaths=" + importPaths + '}';
    }
}
