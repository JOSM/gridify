// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify.ui;

import nl.jeroenhoek.josm.gridify.GridExtrema;
import nl.jeroenhoek.josm.gridify.GridExtrema.Dimensions;
import nl.jeroenhoek.josm.gridify.ui.GridSizePanel.Nudge;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;

/**
 * UI widget that shows an abstracted live preview of the operation to be performed. Because shapes are rarely neatly
 * aligned to the cardinal directions of the compass, terms like 'rows' and 'columns' become rather abstract. So to
 * prevent confusion, we simply show the user how his grid will be divided.
 * <p>
 * The preview panel is updated whenever the values held by the {@link GridSizePanel} are changed.
 */
public class Preview extends JPanel {
    final int CANVAS_SIZE = 280;
    final int PADDING = 10;

    final Dimension size = new Dimension(CANVAS_SIZE + 2 * PADDING, CANVAS_SIZE + 2 * PADDING);

    Grid grid;

    public Preview(GridExtrema gridExtrema, GridifySettingsDialog settingsDialog) {
        Dimensions dimensions = gridExtrema.getDimensions();

        // Compute the multiplication factor and offsets that convert between EastNorth and local preview canvas
        // coordinates.
        double longestDistance = dimensions.longestOfWidthAndHeight();
        double fraction = CANVAS_SIZE / longestDistance;

        double offsetX = (longestDistance - (dimensions.getMaxX() - dimensions.getMinX())) / 2.0;
        double offsetY = (longestDistance - (dimensions.getMaxY() - dimensions.getMinY())) / 2.0;

        grid = new Grid(
                toCanvasCoordinates(dimensions, fraction, offsetX, offsetY, gridExtrema.getNodeOne()),
                toCanvasCoordinates(dimensions, fraction, offsetX, offsetY, gridExtrema.getNodeTwo()),
                toCanvasCoordinates(dimensions, fraction, offsetX, offsetY, gridExtrema.getNodeThree()),
                toCanvasCoordinates(dimensions, fraction, offsetX, offsetY, gridExtrema.getNodeFour())
        );
        grid.setRows(settingsDialog.getRowCount());
        grid.setColumns(settingsDialog.getColumnCount());

        // Allow users to adjust the row/column count by using the mouse wheel on top of the preview.
        // This is just an extra; the two spinners are the more visible way of adjusting the row and
        // column count.
        addMouseWheelListener(e -> {
            int ticks = e.getWheelRotation();
            int modifiers = e.getModifiersEx();
            // Only if shift is down (but not ctrl).
            boolean doOnlyRows = (modifiers & (SHIFT_DOWN_MASK | CTRL_DOWN_MASK)) == SHIFT_DOWN_MASK;
            // Only if ctrl is down (but not shift).
            boolean doOnlyColumns = (modifiers & (SHIFT_DOWN_MASK | CTRL_DOWN_MASK)) == CTRL_DOWN_MASK;
            if (ticks > 0) {
                for (int tick = 0; tick < ticks; tick++) {
                    if (doOnlyRows) {
                        settingsDialog.nudgeRowCount(Nudge.DECREMENT);
                    } else if (doOnlyColumns) {
                        settingsDialog.nudgeColumnCount(Nudge.DECREMENT);
                    } else {
                        settingsDialog.nudgeRowCount(Nudge.DECREMENT);
                        settingsDialog.nudgeColumnCount(Nudge.DECREMENT);
                    }
                }
            } else if (ticks < 0) {
                for (int tick = 0; tick > ticks; tick--) {
                    if (doOnlyRows) {
                        settingsDialog.nudgeRowCount(Nudge.INCREMENT);
                    } else if (doOnlyColumns) {
                        settingsDialog.nudgeColumnCount(Nudge.INCREMENT);
                    } else {
                        settingsDialog.nudgeRowCount(Nudge.INCREMENT);
                        settingsDialog.nudgeColumnCount(Nudge.INCREMENT);
                    }
                }
            }
        });
    }

    @Override
    public Dimension getMinimumSize() {
        return size;
    }

    @Override
    public Dimension getPreferredSize() {
        return size;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Use a BufferedImage to enable drawing anti-aliased lines.
        BufferedImage image = new BufferedImage(
                CANVAS_SIZE + 2 * PADDING,
                CANVAS_SIZE + 2 * PADDING,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D ig = image.createGraphics();

        ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background.
        ig.setColor(Color.BLACK);
        ig.fillRect(0, 0, 300, 300);

        // Draw the grid.
        ig.setColor(Color.RED);
        grid.withLines((start, stop) -> ig.drawLine(start.x, start.y, stop.x, stop.y));

        g.drawImage(image, 0, 0, this);
    }

    public void updateRowsColumns(int rows, int columns) {
        this.grid.setRows(rows);
        this.grid.setColumns(columns);
        repaint();
    }

    /**
     * Turn {@link EastNorth} coordinates into our local integer coordinates for the preview canvas.
     *
     * @param dimensions Bounding box used to determine the scaling factor.
     * @param fraction   Scaling factor.
     * @param offsetX    X offset (to centre the grid in the preview canvas).
     * @param offsetY    Y offset (to centre the grid in the preview canvas).
     * @param node       Node to convert.
     * @return Local preview canvas coordinates.
     */
    Coordinates toCanvasCoordinates(Dimensions dimensions,
                                    double fraction,
                                    double offsetX,
                                    double offsetY,
                                    Node node) {
        EastNorth eastNorth = node.getEastNorth();
        double x = eastNorth.getX();
        double y = eastNorth.getY();

        Coordinates coordinates = new Coordinates();

        x -= dimensions.getMinX();
        y -= dimensions.getMinY();

        x += offsetX;
        y += offsetY;

        x *= fraction;
        y *= fraction;

        coordinates.x = PADDING + (int) Math.round(x);
        // The y coordinates are 'upside down'.
        coordinates.y = (CANVAS_SIZE - (int) Math.round(y)) + PADDING;

        return coordinates;
    }

    /**
     * Simple integer coordinate pair.
     */
    public static class Coordinates {
        int x;
        int y;

        @Override
        public String toString() {
            return "x=" + x + ", y=" + y;
        }
    }

    /**
     * Representation of the grid the user wants to generate. The four coordinates specified in a clockwise order.
     */
    public static class Grid {
        Coordinates one;
        Coordinates two;
        Coordinates three;
        Coordinates four;

        int rows;
        int columns;

        public Grid(Coordinates one, Coordinates two, Coordinates three, Coordinates four) {
            this.one = one;
            this.two = two;
            this.three = three;
            this.four = four;
        }

        /**
         * Perform an operation on each line of the grid.
         *
         * @param consumer Consumer that receives two coordinate pairs for each line of the grid, both rows and columns.
         */
        void withLines(BiConsumer<Coordinates, Coordinates> consumer) {
            List<Coordinates> top = range(one, two, columns);
            List<Coordinates> bottom = range(four, three, columns);
            List<Coordinates> left = range(one, four, rows);
            List<Coordinates> right = range(two, three, rows);

            // All columns.
            for (int i = 0; i < top.size(); i++) {
                consumer.accept(top.get(i), bottom.get(i));
            }
            // All rows.
            for (int i = 0; i < left.size(); i++) {
                consumer.accept(left.get(i), right.get(i));
            }
        }

        /**
         * Generate a list of coordinates representing a number of points evenly distributed along a line.
         *
         * @param a Start of the line.
         * @param b End of the line.
         * @param n Number of point to add to the line.
         * @return A list of coordinates, including the start and end of the line.
         */
        private List<Coordinates> range(Coordinates a, Coordinates b, int n) {
            List<Coordinates> points = new ArrayList<>();
            points.add(a);

            if (n > 1) {
                float dx = (b.x - a.x) / (float) n;
                float dy = (b.y - a.y) / (float) n;

                for (int i = 1; i < n; i++) {
                    Coordinates between = new Coordinates();
                    between.x = a.x + Math.round(dx * i);
                    between.y = a.y + Math.round(dy * i);
                    points.add(between);
                }
            }

            points.add(b);
            return points;
        }

        public void setRows(int rows) {
            this.rows = rows;
        }

        public void setColumns(int columns) {
            this.columns = columns;
        }

        @Override
        public String toString() {
            return one + "|" + two + "|" + three + "|" + four;
        }
    }
}
