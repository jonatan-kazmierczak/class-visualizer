package clsvis.gui.model;

import clsvis.model.Class_;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.table.AbstractTableModel;

/**
 * Model for classes tables.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public class ClassesTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = { "Name", "Full Name" };

    private final ArrayList<Class_> classes_;

    /**
     * Constructor for keeping of history.
     */
    public ClassesTableModel() {
        classes_ = new ArrayList<>( 0x80 );
    }

    /**
     * Constructor for presentation of full list of classes.
     */
    public ClassesTableModel(Collection<Class_> classes_) {
        this.classes_ = new ArrayList<>( classes_ );
        this.classes_.sort( null );
    }

    @Override
    public int getRowCount() {
        return classes_.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return COLUMN_NAMES[ columnIndex ];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Class_ class_ = classes_.get( rowIndex );
        return new String[]{ class_.name, class_.fullTypeName }[ columnIndex ];
    }

    public void addRow(Class_ class_, int currentClassHistoryIndex) {
        // TODO reimplement using iterator
        for (int lastIndex = classes_.size() - 1; lastIndex >= currentClassHistoryIndex; lastIndex = classes_.size() - 1) {
            classes_.remove( lastIndex );
        }
        classes_.add( class_ );
    }

    public Class_ getRow(int idx) {
        return classes_.get( idx );
    }
}
