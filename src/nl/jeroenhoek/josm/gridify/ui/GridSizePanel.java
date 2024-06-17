// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;

import org.openstreetmap.josm.tools.GBC;

/**
 * UI widget that allows the user to set the grid division; i.e., how many rows and columns.
 */
public class GridSizePanel extends JPanel {
    private final PositiveSpinner spinnerRows;
    private final PositiveSpinner spinnerColumns;

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
        JButton flipButton = new JButton("×");
        flipButton.setPreferredSize(new JLabel(flipButton.getText()).getPreferredSize());
        flipButton.setPreferredSize(new Dimension(2 * flipButton.getPreferredSize().width, flipButton.getPreferredSize().height));
        flipButton.setMaximumSize(flipButton.getPreferredSize());

        add(spinnerRows);
        add(Box.createHorizontalStrut(10));
        add(flipButton);
        add(Box.createHorizontalStrut(10));
        add(spinnerColumns);

        spinnerRows.addChangeListener(e -> setRowCount((int) spinnerRows.getValue()));
        flipButton.addActionListener(e -> flipRowsColumns());
        spinnerColumns.addChangeListener(e -> setColumnCount((int) spinnerColumns.getValue()));
    }

    void flipRowsColumns() {
        int old_rows = this.rows;
        this.rows = this.columns;
        this.columns = old_rows;
        this.spinnerRows.setValue(this.rows);
        this.spinnerColumns.setValue(this.columns);
        changeCallback.changed(getRowCount(), getColumnCount());
    }

    void setRowCount(int rows) {
        if (rows < 1 || rows > 1000) return;
        this.rows = rows;
        this.spinnerRows.setValue(rows);
        changeCallback.changed(getRowCount(), getColumnCount());
    }

    void setColumnCount(int columns) {
        if (columns < 1 || columns > 1000) return;
        this.columns = columns;
        this.spinnerColumns.setValue(columns);
        changeCallback.changed(getRowCount(), getColumnCount());
    }

    void nudgeRowCount(Nudge direction) {
        if (direction == Nudge.INCREMENT || this.rows > 1) {
            this.rows += direction == Nudge.INCREMENT ? 1 : -1;
            this.spinnerRows.setValue(rows);
            this.spinnerRows.caretToEnd();
            changeCallback.changed(getRowCount(), getColumnCount());
        }
    }

    void nudgeColumnCount(Nudge direction) {
        if (direction == Nudge.INCREMENT || this.columns > 1) {
            this.columns += direction == Nudge.INCREMENT ? 1 : -1;
            this.spinnerColumns.setValue(columns);
            this.spinnerColumns.caretToEnd();
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
