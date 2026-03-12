// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify;

import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Four nodes representing the area selected by the user.
 * The nodes are normalized into TL, TR, BR, BL order.
 */
public final class GridExtrema {
    Node one;   // TL
    Node two;   // TR
    Node three; // BR
    Node four;  // BL

    private GridExtrema(Node one, Node two, Node three, Node four) {
        this.one = one;
        this.two = two;
        this.three = three;
        this.four = four;
    }

    /**
     * Creates a GridExtrema with normalized corner ordering: TL, TR, BR, BL.
     */
    public static GridExtrema from(Node a, Node b, Node c, Node d) {
        List<Node> nodes = Arrays.asList(a, b, c, d);

        // calculate centroid
        double cx = 0, cy = 0;
        for (Node n : nodes) {
            EastNorth en = n.getEastNorth();
            cx += en.east();
            cy += en.north();
        }
        cx /= 4.0;
        cy /= 4.0;

        // sort CCW around centroid by angle
        final double finalCy = cy;
        final double finalCx = cx;
        nodes.sort(Comparator.comparingDouble(n -> {
            EastNorth en = n.getEastNorth();
            return Math.atan2(en.north() - finalCy, en.east() - finalCx);
        }));

        // find index of top-left (max north)
        int tlIndex = 0;
        double maxNorth = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < 4; i++) {
            double north = nodes.get(i).getEastNorth().north();
            if (north > maxNorth) {
                maxNorth = north;
                tlIndex = i;
            }
        }

        // nodes are CCW; map to TL, TR, BR, BL
        // If index i is TL:
        // i-1 (or i+3) is TR
        // i-2 (or i+2) is BR
        // i-3 (or i+1) is BL
        Node tl = nodes.get(tlIndex);
        Node tr = nodes.get((tlIndex + 3) % 4); // previous in CCW is TR
        Node br = nodes.get((tlIndex + 2) % 4); // opposite is BR
        Node bl = nodes.get((tlIndex + 1) % 4); // next in CCW is BL

        return new GridExtrema(tl, tr, br, bl);
    }

    public Node getNodeOne() {
        return one;
    }

    public Node getNodeTwo() {
        return two;
    }

    public Node getNodeThree() {
        return three;
    }

    public Node getNodeFour() {
        return four;
    }

    /**
     * Holds calculated cell dimensions.
     */
    public static class CellDimensions {
        /**
         * The average width of the cell.
         */
        public final double width;
        /**
         * The average height of the cell.
         */
        public final double height;
        /**
         * The area of the cell.
         */
        public final double area;

        /**
         * Initializes the cell dimensions.
         *
         * @param width  The average width.
         * @param height The average height.
         * @param area   The area.
         */
        public CellDimensions(double width, double height, double area) {
            this.width = width;
            this.height = height;
            this.area = area;
        }
    }

    /**
     * Calculates the average dimensions of a single cell in the grid using meters.
     *
     * @param numRows Total number of rows.
     * @param numCols Total number of columns.
     * @return The {@link CellDimensions} of a single cell.
     */
    public CellDimensions getAverageCellDimensions(int numRows, int numCols) {
        Projection proj = ProjectionRegistry.getProjection();

        // convert to LatLon to measure real-world meters
        LatLon l1 = proj.eastNorth2latlon(one.getEastNorth());   // TL
        LatLon l2 = proj.eastNorth2latlon(two.getEastNorth());   // TR
        LatLon l3 = proj.eastNorth2latlon(three.getEastNorth()); // BR
        LatLon l4 = proj.eastNorth2latlon(four.getEastNorth());  // BL

        // calculate average width (top edge + bottom edge)
        double widthTop = l1.greatCircleDistance(l2);
        double widthBottom = l4.greatCircleDistance(l3);
        double avgTotalWidth = (widthTop + widthBottom) / 2.0;

        // calculate average height (left edge + right edge)
        double heightLeft = l1.greatCircleDistance(l4);
        double heightRight = l2.greatCircleDistance(l3);
        double avgTotalHeight = (heightLeft + heightRight) / 2.0;

        double cellWidth = avgTotalWidth / numCols;
        double cellHeight = avgTotalHeight / numRows;

        return new CellDimensions(cellWidth, cellHeight, cellWidth * cellHeight);
    }

    /**
     * Returns the bounding box of the four nodes.
     *
     * @return The {@link ProjectionBounds} of the grid extrema.
     */
    public ProjectionBounds getDimensions() {
        ProjectionBounds bounds = new ProjectionBounds(one.getEastNorth());
        bounds.extend(two.getEastNorth());
        bounds.extend(three.getEastNorth());
        bounds.extend(four.getEastNorth());
        return bounds;
    }
}
