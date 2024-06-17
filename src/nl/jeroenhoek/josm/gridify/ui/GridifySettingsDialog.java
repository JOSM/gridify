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

import static org.openstreetmap.josm.tools.I18n.set;
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

        operationChooser = new OperationChooser(settings.getOperation());
        constraints.gridy = 1;
        constraints.insets = insetsIndent;
        controlPanel.add(operationChooser, constraints);

        JLabel gridSizePanelLabel = new JLabel(tr("Grid size"));
        gridSizePanelLabel.setBorder(underline);
        constraints.gridy = 2;
        constraints.insets = insetsDefault;
        controlPanel.add(gridSizePanelLabel, constraints);

        gridSizePanel = new GridSizePanel(
                preview::updateRowsColumns,
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
                    !inputData.getSourceWay().get().getReferrers().isEmpty()
            );
            constraints.gridy = 5;
            constraints.insets = insetsIndent;
            controlPanel.add(sourceWayPanel, constraints);
        }

        rootPanel.add(controlPanel);
        rootPanel.add(Box.createRigidArea(new Dimension(10, 10)));

        rootPanel.add(preview);

        setContent(rootPanel, false);
        setButtonIcons("ok", "cancel");
        setDefaultButton(1);

        setResizable(false);

        gridSizePanel.requestFocusInWindow();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public int getRowCount() {
        return gridSizePanel == null
                ? settings.getNumRows()
                : gridSizePanel.getRowCount();
    }

    public int getColumnCount() {
        return gridSizePanel == null
                ? settings.getNumColumns()
                : gridSizePanel.getColumnCount();
    }

    public void nudgeRowCount(Nudge direction) {
        if (gridSizePanel != null) {
            gridSizePanel.nudgeRowCount(direction);
        }
    }

    public void nudgeColumnCount(Nudge direction) {
        if (gridSizePanel != null) {
            gridSizePanel.nudgeColumnCount(direction);
        }
    }

    public Operation getOperation() {
        return operationChooser.getSelected();
    }

    public boolean copyTags() {
        return inputData.getSourceWay().isPresent() && sourceWayPanel.copyTags();
    }

    public boolean deleteSourceWay() {
        return inputData.getSourceWay().isPresent() && sourceWayPanel.deleteSourceWay();
    }
}
