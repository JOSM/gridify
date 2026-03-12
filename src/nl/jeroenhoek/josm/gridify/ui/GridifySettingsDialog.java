// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify.ui;

import nl.jeroenhoek.josm.gridify.GridifySettings;
import nl.jeroenhoek.josm.gridify.InputData;
import nl.jeroenhoek.josm.gridify.Operation;
import nl.jeroenhoek.josm.gridify.ui.GridSizePanel.Nudge;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * The modal dialog presented to the user when executing the Gridify action.
 */
public class GridifySettingsDialog extends ExtendedDialog {
    private final InputData inputData;
    private final GridifySettings settings;

    private GridSizePanel gridSizePanel;
    private OperationChooser operationChooser;
    private SourceWayPanel sourceWayPanel;

    private JLabel gridCountLabel;
    private JLabel cellSizeLabel;
    private Runnable changeListener;

    public GridifySettingsDialog(InputData inputData, GridifySettings settings) {
        super(MainApplication.getMainFrame(), tr("Gridify preview"), tr("Gridify"), tr("Cancel"));
        this.inputData = inputData;
        this.settings = settings;

        final Insets insetsDefault = new Insets(0, 0, 10, 0);
        final Insets insetsIndent = new Insets(0, 30, 10, 0);
        final Border underline = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black);

        Preview preview = new Preview(inputData.getGridExtrema(), this);

        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.X_AXIS));

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;

        JLabel operationChooserLabel = new JLabel(tr("Output shape"));
        operationChooserLabel.setBorder(underline);
        controlPanel.add(operationChooserLabel, constraints);

        operationChooser = new OperationChooser(settings.getOperation(), this::fireChangeEvent);
        constraints.gridy = 1;
        constraints.insets = insetsIndent;
        controlPanel.add(operationChooser, constraints);

        JLabel gridSizePanelLabel = new JLabel(tr("Grid size"));
        gridSizePanelLabel.setBorder(underline);
        constraints.gridy = 2;
        constraints.insets = insetsDefault;
        controlPanel.add(gridSizePanelLabel, constraints);

        gridSizePanel = new GridSizePanel(
                (rows, columns) -> {
                    preview.updateRowsColumns(rows, columns);
                    fireChangeEvent();
                },
                settings.getNumRows(),
                settings.getNumColumns()
        );
        constraints.gridy = 3;
        constraints.insets = insetsIndent;
        controlPanel.add(gridSizePanel, constraints);

        if (inputData.getSourceWay().isPresent()) {
            JLabel wayLabel = new JLabel(tr("Source way"));
            wayLabel.setBorder(underline);
            constraints.gridy = 4;
            constraints.insets = insetsDefault;
            controlPanel.add(wayLabel, constraints);

            // Always check the 'delete source way' option when a new way is used as template.
            // It tends to have been drawn specifically to cut up.
            boolean deleteSourceWay = inputData.getSourceWay().get().isNew();

            sourceWayPanel = new SourceWayPanel(
                    settings.copyTagsFromSource(),
                    deleteSourceWay || settings.deleteSource(),
                    !inputData.getSourceWay().get().getReferrers().isEmpty(),
                    this::fireChangeEvent
            );
            constraints.gridy = 5;
            constraints.insets = insetsIndent;
            controlPanel.add(sourceWayPanel, constraints);
        }

        JLabel statsLabel = new JLabel(tr("Statistics"));
        constraints.gridy = 6;
        constraints.insets = insetsDefault;
        statsLabel.setBorder(underline);
        controlPanel.add(statsLabel, constraints);

        gridCountLabel = new JLabel();
        constraints.gridy = 7;
        constraints.insets = insetsDefault;
        controlPanel.add(gridCountLabel, constraints);

        cellSizeLabel = new JLabel();
        constraints.gridy = 8;
        constraints.insets = insetsDefault;
        controlPanel.add(cellSizeLabel, constraints);

        rootPanel.add(controlPanel);
        rootPanel.add(Box.createRigidArea(new Dimension(10, 10)));

        rootPanel.add(preview);

        setContent(rootPanel, false);
        setButtonIcons("ok", "cancel");
        setDefaultButton(1);

        setResizable(false);

        gridSizePanel.requestFocusInWindow();
    }

    /**
     * Updates the grid count display in the dialog.
     *
     * @param count The number of ways that will be generated.
     */
    public void setGridCount(int count) {
        gridCountLabel.setText(tr("Number of generated elements: {0}", count));
    }

    /**
     * Updates the cell size information in the dialog.
     *
     * @param cellSizeText The formatted text showing width, height, and area.
     */
    public void setCellSize(String cellSizeText) {
        cellSizeLabel.setText(cellSizeText);
    }

    /**
     * Registers a listener to be notified when any setting in the dialog changes.
     *
     * @param listener The listener to add.
     */
    public void addChangeListener(Runnable listener) {
        this.changeListener = listener;
    }

    private void fireChangeEvent() {
        if (changeListener != null) {
            changeListener.run();
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /**
     * Returns the selected row count from the spinner.
     *
     * @return The number of rows.
     */
    public int getRowCount() {
        return gridSizePanel == null
                ? settings.getNumRows()
                : gridSizePanel.getRowCount();
    }

    /**
     * Returns the selected column count from the spinner.
     *
     * @return The number of columns.
     */
    public int getColumnCount() {
        return gridSizePanel == null
                ? settings.getNumColumns()
                : gridSizePanel.getColumnCount();
    }

    /**
     * Increments or decrements the row count.
     *
     * @param direction The nudge direction.
     */
    public void nudgeRowCount(Nudge direction) {
        if (gridSizePanel != null) {
            gridSizePanel.nudgeRowCount(direction);
        }
    }

    /**
     * Increments or decrements the column count.
     *
     * @param direction The nudge direction.
     */
    public void nudgeColumnCount(Nudge direction) {
        if (gridSizePanel != null) {
            gridSizePanel.nudgeColumnCount(direction);
        }
    }

    /**
     * Returns the selected operation type.
     *
     * @return The selected {@link Operation}.
     */
    public Operation getOperation() {
        return operationChooser.getSelected();
    }

    /**
     * Whether tags should be copied from the source way.
     *
     * @return True if tags should be copied.
     */
    public boolean copyTags() {
        return inputData.getSourceWay().isPresent() && sourceWayPanel.copyTags();
    }

    /**
     * Whether the source way should be deleted.
     *
     * @return True if the source way should be deleted.
     */
    public boolean deleteSourceWay() {
        return inputData.getSourceWay().isPresent() && sourceWayPanel.deleteSourceWay();
    }
}
