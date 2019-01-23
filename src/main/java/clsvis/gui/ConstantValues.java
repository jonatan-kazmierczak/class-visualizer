package clsvis.gui;

import clsvis.model.ElementKind;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Constant values shared by other classes.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public interface ConstantValues {

    /** Map of elements background colors. */
    Map<ElementKind, Color> backgroundColorMap = Collections.unmodifiableMap(
            Arrays.stream( ElementKind.values() )
            .collect( Collectors.toMap( ek -> ek, ek -> new Color( ek.colorNum ) ) ) );

    String APPLICATION_TITLE = "Class Visualizer";
    String APPLICATION_VERSION = "0.11.0";

    String NEW_PROJECT_TITLE = "new project";

    String PROJECT_FILE_EXTENSION = "clsvis";
}
