package nl.jeroenhoek.josm.gridify;

import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.EnumProperty;
import org.openstreetmap.josm.data.preferences.IntegerProperty;

import static nl.jeroenhoek.josm.gridify.ui.PositiveSpinner.SPINNER_MAX_VALUE;
import static nl.jeroenhoek.josm.gridify.ui.PositiveSpinner.SPINNER_MIN_VALUE;

/**
 * All settings used by Gridify.
 */
public class GridifySettings {
    private final static int DEFAULT_ROW_COUNT = 2;
    private final static int DEFAULT_COL_COUNT = 4;

    final IntegerProperty numRowsSetting = new IntegerProperty("gridify.num_rows", DEFAULT_ROW_COUNT);
    final IntegerProperty numColsSetting = new IntegerProperty("gridify.num_cols", DEFAULT_COL_COUNT);
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

        // Limit the number of rows to permissible values
        if (rows < SPINNER_MIN_VALUE || rows > SPINNER_MAX_VALUE) {
            setNumRows(DEFAULT_ROW_COUNT);
            return DEFAULT_ROW_COUNT;
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

        // Limit the number of columns to permissible values
        if (columns < SPINNER_MIN_VALUE || columns > SPINNER_MAX_VALUE) {
            setNumColumns(DEFAULT_COL_COUNT);
            return DEFAULT_COL_COUNT;
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
