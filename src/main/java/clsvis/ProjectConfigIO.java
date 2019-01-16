package clsvis;

import clsvis.model.ProjectConfig;
import clsvis.model.ProjectConfig.PersistentFields;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class to load and save {@link ProjectConfig}.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public interface ProjectConfigIO {

    static void save(ProjectConfig projectConfig) {
        try {
            Files.write( projectConfig.path.toPath(),
                    Arrays.asList(
                            PersistentFields.classPaths.name() + '=' + projectConfig.classPaths,
                            PersistentFields.importPaths.name() + '=' + projectConfig.importPaths
                    ),
                    StandardCharsets.UTF_8
            );
        } catch (IOException ex) {
            throw new UncheckedIOException( ex );
        }
    }

    static ProjectConfig load(File path) {
        try (Stream<String> fileLines = Files.lines( path.toPath(), StandardCharsets.UTF_8 )) {
            Map<PersistentFields, List<File>> fieldsWithValues
                    = fileLines
                    .map( line -> line.split( "[=\\[\\]]+" ) )
                    .collect( Collectors.toMap(
                            vals -> PersistentFields.valueOf( vals[ 0 ] ),
                            vals -> vals.length < 2
                                    ? Collections.emptyList()
                                    : Arrays.stream( vals[ 1 ].split( ", " ) )
                                            .filter( fileName -> !fileName.isEmpty() )
                                            .map( fileName -> new File( fileName ) )
                                            .collect( Collectors.toList() )
                    ) );
            return new ProjectConfig(
                    path,
                    fieldsWithValues.getOrDefault( PersistentFields.classPaths, Collections.emptyList() ),
                    fieldsWithValues.getOrDefault( PersistentFields.importPaths, Collections.emptyList() )
            );
        } catch (IOException ex) {
            throw new UncheckedIOException( ex );
        }
    }
}
