package clsvis.model;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * Holds project configuration.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
@XmlType
@XmlRootElement
@XmlAccessorType( value = XmlAccessType.FIELD )
public final class ProjectConfig {

    /** Path where the config is stored on the file system. */
    @XmlTransient
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
}
