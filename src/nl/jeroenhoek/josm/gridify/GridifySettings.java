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
        return numRowsSetting.get();
    }

    public void setNumRows(int numRows) {
        numRowsSetting.put(numRows);
    }

    public int getNumColumns() {
        return numColsSetting.get();
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
