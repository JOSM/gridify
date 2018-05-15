// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify.ui;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.Dimension;
import java.awt.Font;

/**
 * A {@link JSpinner} limited to a range of 1 to 1000. This class also adds a mouse wheel listener allowing users to
 * scroll the value up or down.
 */
public class PositiveSpinner extends JSpinner {
    public PositiveSpinner(int defaultValue) {
        super(new SpinnerNumberModel(defaultValue, 1, 1000, 1));
        JFormattedTextField field = ((DefaultEditor) getEditor()).getTextField();
        field.setColumns(3);
        field.setFont(new Font("Monospaced", Font.PLAIN, 18));

        // Kind of weird that the default JSpinner doesn't do this.
        addMouseWheelListener(e -> {
            int ticks = e.getWheelRotation();
            Object newValue = null;
            if (ticks > 0) {
                for (int tick = 0; tick < ticks; tick++) {
                    Object previous = getPreviousValue();
                    if (previous == null) break;
                    newValue = previous;
                }
            } else if (ticks < 0) {
                for (int tick = 0; tick > ticks; tick--) {
                    Object next = getNextValue();
                    if (next == null) break;
                    newValue = next;
                }
            }

            if (newValue != null) {
                setValue(newValue);
            }
        });
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
}
