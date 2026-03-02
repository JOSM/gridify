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

    /**
     * Number of rows to generate.
     *
     * @return The number of rows.
     */
    public int getNumRows() {
        int rows = numRowsSetting.get();

        // Limit number of rows to permissible values.
        if (rows < 1 || rows > 1000) {
            setNumRows(2);
            return 2;
        }

        return rows;
    }

    /**
     * Set the number of rows to generate.
     *
     * @param numRows The number of rows.
     */
    public void setNumRows(int numRows) {
        numRowsSetting.put(numRows);
    }

    /**
     * Number of columns to generate.
     *
     * @return The number of columns.
     */
    public int getNumColumns() {
        int columns = numColsSetting.get();

        // Limit number of columns to permissible values.
        if (columns < 1 || columns > 1000) {
            setNumColumns(4);
            return 4;
        }

        return columns;
    }

    /**
     * Set the number of columns to generate.
     *
     * @param numColumns The number of columns.
     */
    public void setNumColumns(int numColumns) {
        numColsSetting.put(numColumns);
    }

    /**
     * Whether to copy tags from the source way.
     *
     * @return True if tags should be copied.
     */
    public boolean copyTagsFromSource() {
        return copyTagsFromSource.get();
    }

    /**
     * Set whether to copy tags from the source way.
     *
     * @param enabled True if tags should be copied.
     */
    public void setCopyTagsFromSource(boolean enabled) {
        copyTagsFromSource.put(enabled);
    }

    /**
     * Whether to delete the source way.
     *
     * @return True if the source way should be deleted.
     */
    public boolean deleteSource() {
        return deleteSource.get();
    }

    /**
     * Set whether to delete the source way.
     *
     * @param enabled True if the source way should be deleted.
     */
    public void setDeleteSource(boolean enabled) {
        deleteSource.put(enabled);
    }

    /**
     * The operation to perform (lines or blocks).
     *
     * @return The operation.
     */
    public Operation getOperation() {
        return operationSetting.get();
    }

    /**
     * Set the operation to perform.
     *
     * @param operation The operation.
     */
    public void setOperation(Operation operation) {
        operationSetting.put(operation);
    }
}
