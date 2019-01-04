package clsvis.gui.renderer;

import clsvis.gui.ConstantValues;
import clsvis.gui.model.ClassesTableModel;
import clsvis.model.Class_;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Customized renderer using element color as background color and customized selection color.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public class ClassesTableCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Component component = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
        if (value instanceof Integer) {
            component.setComponentOrientation( ComponentOrientation.RIGHT_TO_LEFT );
        }

        ClassesTableModel model = (ClassesTableModel) table.getModel();
        Class_ class_ = model.getRow( table.convertRowIndexToModel( row ) );

        if (isSelected) {
            component.setForeground( Color.WHITE );
            component.setBackground( Color.BLUE );
        } else {
            component.setForeground( Color.BLACK );
            component.setBackground( value instanceof String
                    ? ConstantValues.backgroundColorMap.get( class_.kind ) : Color.WHITE );
        }
        return component;
    }
}
