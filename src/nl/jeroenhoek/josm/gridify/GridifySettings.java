package nl.jeroenhoek.josm.gridify;

import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.EnumProperty;
import org.openstreetmap.josm.data.preferences.IntegerProperty;

/**
 * All settings used by Gridify.
 */
public class GridifySettings {
    final IntegerProperty numRowsSetting = new IntegerProperty("gridify.num_rows", 2);
    final IntegerProperty numColsSetting = new IntegerProperty("gridify.num_cols", 4);
    final BooleanProperty copyTagsFromSource = new BooleanProperty("gridify.copy_tags_from_source", true);
    final BooleanProperty deleteSource = new BooleanProperty("gridify.delete_source", true);

    final EnumProperty<Operation> operationSetting = new EnumProperty<>(
            "gridify.operation", Operation.class, Operation.BLOCKS
    );

    public int getNumRows() {
        int rows = numRowsSetting.get();

        // Limit number of rows to permissible values.
        if (rows < 1 || rows > 1000) {
            setNumRows(2);
            return 2;
        }

        return rows;
    }

    public void setNumRows(int numRows) {
        numRowsSetting.put(numRows);
    }

    public int getNumColumns() {
        int columns = numColsSetting.get();

        // Limit number of columns to permissible values.
        if (columns < 1 || columns > 1000) {
            setNumColumns(4);
            return 4;
        }

        return columns;
    }

    public void setNumColumns(int numColumns) {
        numColsSetting.put(numColumns);
    }

    public boolean copyTagsFromSource() {
        return copyTagsFromSource.get();
    }

    public void setCopyTagsFromSource(boolean enabled) {
        copyTagsFromSource.put(enabled);
    }

    public boolean deleteSource() {
        return deleteSource.get();
    }

    public void setDeleteSource(boolean enabled) {
        deleteSource.put(enabled);
    }

    public Operation getOperation() {
        return operationSetting.get();
    }

    public void setOperation(Operation operation) {
        operationSetting.put(operation);
    }
}
