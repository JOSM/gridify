// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify.ui;

import org.openstreetmap.josm.data.osm.Way;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * UI widget that shows options relating to the {@link Way} the user selected.
 */
public class SourceWayPanel extends JPanel {
    private final JCheckBox copyTagsButton;
    private final JCheckBox deleteSourceWayButton;

    public SourceWayPanel(boolean copyTags, boolean deleteSourceWay, boolean disableDeleteSourceWay) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        copyTagsButton = new JCheckBox(tr("Copy tags from source way"));
        copyTagsButton.setSelected(copyTags);

        add(copyTagsButton);

        deleteSourceWayButton = new JCheckBox(tr("Delete source way"));
        if (disableDeleteSourceWay) {
            deleteSourceWayButton.setEnabled(false);
            deleteSourceWayButton.setToolTipText(tr("Disabled since source way is part of relation"));
        } else {
            deleteSourceWayButton.setSelected(deleteSourceWay);
        }

        add(deleteSourceWayButton);
    }

    /**
     * Should the tags from the source way be reused?
     *
     * @return True, if the tags should be copied to the newly generated ways.
     */
    public boolean copyTags() {
        return copyTagsButton.isSelected();
    }

    /**
     * Should the source way be deleted?
     *
     * @return True if the user wants the source to be deleted.
     */
    public boolean deleteSourceWay() {
        return deleteSourceWayButton.isSelected();
    }
}
