// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.text.ParseException;
import java.util.Objects;

/**
 * A {@link JSpinner} limited to a range of 1 to 1000. This class also adds a mouse wheel listener allowing users to
 * scroll the value up or down.
 */
public class PositiveSpinner extends JSpinner {

    private final JFormattedTextField field;
    private final ValueChanged changeCallback;
    private Integer lastValue;

    public PositiveSpinner(int defaultValue, ValueChanged changeCallback) {
        super(new SpinnerNumberModel(defaultValue, 1, 1000, 1));
        this.changeCallback = changeCallback;
        DefaultEditor editor = (DefaultEditor) getEditor();
        field = editor.getTextField();
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

        // Select the input for easy editing when the field receives focus.
        field.addFocusListener(new FocusListener());

        // Let our parent know when the field value changes for live updates.
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                callbackIfChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                callbackIfChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                callbackIfChanged();
            }
        });

        this.lastValue = (Integer) getValue();
    }

    void callbackIfChanged() {
        Integer newValue;
        try {
            newValue = Integer.parseInt(field.getText());
        } catch (NumberFormatException e) {
            // Ignore.
            return;
        }

        if (!Objects.equals(newValue, lastValue)) {
            SwingUtilities.invokeLater(() -> {
                this.lastValue = newValue;
                changeCallback.onChange(newValue);
            });
        }
    }

    @Override
    public void setValue(Object value) {
        // No need to update the value if nothing changes. This prevents the caret being placed at an awkward position.
        if (Objects.equals(value, lastValue)) return;

        super.setValue(value);
    }

    public void caretToEnd() {
        field.setCaretPosition(field.getText().length());
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public boolean requestFocusInWindow() {
        return this.field.requestFocusInWindow();
    }

    /**
     * This should work on most platforms.
     */
    static class FocusListener implements java.awt.event.FocusListener {
        // Thanks to mKorbel (https://stackoverflow.com/users/714968/mkorbel) for this instructive snippet:
        // https://stackoverflow.com/questions/20971050/jspinner-autoselect-onfocus/20971713#20971713

        @Override
        public void focusGained(FocusEvent e) {
            focus(e);
        }

        @Override
        public void focusLost(FocusEvent e) {
            focus(e);
        }

        private void focus(FocusEvent e) {
            final Component component = e.getComponent();
            if (component instanceof JFormattedTextField) {
                JFormattedTextField field = (JFormattedTextField) component;
                SwingUtilities.invokeLater(() -> {
                    field.setText(field.getText());
                    field.selectAll();
                });
            }
        }
    }

    interface ValueChanged {
        void onChange(int value);
    }
}
