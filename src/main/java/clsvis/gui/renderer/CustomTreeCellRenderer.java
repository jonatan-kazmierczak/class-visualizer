package clsvis.gui.renderer;

import java.awt.Color;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Customized renderer to not show default icons.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public final class CustomTreeCellRenderer extends DefaultTreeCellRenderer {

    private final static Color TREE_SELECTION_BG_COLOR = new Color( 0x87CEFA ); // LightSkyBlue

    public CustomTreeCellRenderer() {
        setLeafIcon( null );
        setOpenIcon( null );
        setClosedIcon( null );
        setTextSelectionColor( Color.BLACK );
        setTextNonSelectionColor( Color.BLACK );
        setBackgroundSelectionColor( TREE_SELECTION_BG_COLOR );
    }
}
