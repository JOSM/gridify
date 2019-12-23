// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify.ui;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import java.awt.Font;

/**
 * UI widget that allows the user to set the grid division; i.e., how many rows and columns.
 */
public class GridSizePanel extends JPanel {
    private final JSpinner spinnerRows;
    private final JSpinner spinnerColumns;

    private final ChangeCallback changeCallback;

    private int rows;
    private int columns;

    public GridSizePanel(ChangeCallback changeCallback, int rows, int columns) {
        this.changeCallback = changeCallback;
        this.rows = rows;
        this.columns = columns;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        spinnerRows = new PositiveSpinner(rows, this::setRowCount);
        spinnerColumns = new PositiveSpinner(columns, this::setColumnCount);
        JLabel xLabel = new JLabel("×");
        xLabel.setFont(new Font("Monospaced", Font.PLAIN, 18));


        add(spinnerRows);
        add(Box.createHorizontalStrut(10));
        add(xLabel);
        add(Box.createHorizontalStrut(10));
        add(spinnerColumns);

        spinnerRows.addChangeListener(e -> setRowCount((int) spinnerRows.getValue()));
        spinnerColumns.addChangeListener(e -> setColumnCount((int) spinnerColumns.getValue()));
    }

    void setRowCount(int rows) {
        this.rows = rows;
        this.spinnerRows.setValue(rows);
        changeCallback.changed(getRowCount(), getColumnCount());
    }

    void setColumnCount(int columns) {
        this.columns = columns;
        this.spinnerColumns.setValue(columns);
        changeCallback.changed(getRowCount(), getColumnCount());
    }

    void nudgeRowCount(Nudge direction) {
        if (direction == Nudge.INCREMENT || this.rows > 1) {
            this.rows += direction == Nudge.INCREMENT ? 1 : -1;
            this.spinnerRows.setValue(rows);
            changeCallback.changed(getRowCount(), getColumnCount());
        }
    }

    void nudgeColumnCount(Nudge direction) {
        if (direction == Nudge.INCREMENT || this.columns > 1) {
            this.columns += direction == Nudge.INCREMENT ? 1 : -1;
            this.spinnerColumns.setValue(columns);
            changeCallback.changed(getRowCount(), getColumnCount());
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        return this.spinnerRows.requestFocusInWindow();
    }

    public int getRowCount() {
        return this.rows;
    }

    public int getColumnCount() {
        return this.columns;
    }

    @FunctionalInterface
    interface ChangeCallback {
        void changed(int rows, int columns);
    }

    public enum Nudge {
        INCREMENT,
        DECREMENT
    }
}
