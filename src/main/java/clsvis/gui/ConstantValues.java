package clsvis.gui;

import clsvis.model.ElementKind;
import java.awt.Color;
import java.awt.Cursor;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Constant values shared by other classes.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public interface ConstantValues {

    /** Map of elements background colors. */
    Map<ElementKind, Color> backgroundColorMap = Arrays.stream( ElementKind.values() )
            .collect( Collectors.toMap( ek -> ek, ek -> new Color( ek.colorNum ) ) );

    String projectsHistoryFileName = "projectsHistory.session.xml";

    String applicationTitle = "Class Visualizer";

    String NEW_PROJECT_TITLE = "new project";

    String PROJECT_FILE_EXTENSION = "xml";

    Cursor WAIT_CURSOR = new Cursor( Cursor.WAIT_CURSOR );
}
