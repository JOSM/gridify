// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Shortcut;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * The Gridify action opens a modal dialog where the user can configure the operation of this plugin, and execute it.
 */
public class GridifyAction extends JosmAction {
    static final String DESCRIPTION = tr("Generate a grid of ways from four nodes.");

    public GridifyAction() {
        super(
                tr("Gridify"),
                "gridify",
                DESCRIPTION,
                Shortcut.registerShortcut(
                        "tools:gridify",
                        tr("Tool: {0}", DESCRIPTION),
                        KeyEvent.VK_Y,
                        Shortcut.ALT_SHIFT
                ),
                true
        );
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        JOptionPane.showMessageDialog(MainApplication.getMainFrame(), "Test.");
    }

    @Override
    protected void updateEnabledState() {
        // Keep this action disabled when there is no data set to work with.
        DataSet dataSet = getLayerManager().getEditDataSet();
        setEnabled(dataSet != null && !dataSet.selectionEmpty());
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        // Only enable this action when there is a selection to work with.
        updateEnabledStateOnModifiableSelection(selection);
    }
}
