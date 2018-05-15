// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify.ui;

import nl.jeroenhoek.josm.gridify.Operation;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * UI widget for choosing the {@link Operation} to perform.
 */
public class OperationChooser extends JPanel {
    private Operation selected;

    public OperationChooser(Operation defaultOperation) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        ButtonGroup group = new ButtonGroup();
        selected = defaultOperation;

        for (Operation operation : Operation.values()) {
            JRadioButton button = new JRadioButton(operation.toString());
            button.addActionListener(e -> {
                if (button.isSelected()) {
                    selected = operation;
                }
            });
            if (operation == defaultOperation) {
                button.setSelected(true);
            }
            group.add(button);
            add(button);
        }
    }

    public Operation getSelected() {
        return selected;
    }
}
