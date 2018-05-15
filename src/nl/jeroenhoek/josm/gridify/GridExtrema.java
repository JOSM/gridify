// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.tools.Geometry;

/**
 * Four nodes representing the area selected by the user. The order of the nodes describes the resulting polygon in a
 * clockwise manner.
 */
public final class GridExtrema {
    Node one;
    Node two;
    Node three;
    Node four;

    private GridExtrema(Node one, Node two, Node three, Node four) {
        this.one = one;
        this.two = two;
        this.three = three;
        this.four = four;
    }

    /**
     * Static constructor for {@link GridExtrema}. This method figures out the correct order of the four nodes to
     * form a clockwise way starting at node {@code a}.
     *
     * @param a A node.
     * @param b Another node.
     * @param c Yet another node.
     * @param d Again, another node.
     * @return A {@link GridExtrema} instance.
     */
    public static GridExtrema from(Node a, Node b, Node c, Node d) {
        EastNorth enForA = a.getEastNorth();
        EastNorth enForB = b.getEastNorth();
        EastNorth enForC = c.getEastNorth();
        EastNorth enForD = d.getEastNorth();

        double a1 = Geometry.getCornerAngle(enForB, enForA, enForC);
        double a2 = Geometry.getCornerAngle(enForB, enForA, enForD);
        double a3 = Geometry.getCornerAngle(enForC, enForA, enForD);

        // Figure out the correct order of the four nodes to form a clockwise way starting at
        // node 'a'. The two nodes that form the biggest angle when connected with starting node
        // 'a' (1) in the middle are by necessity nodes 2 and 4. The direction of the angle tells
        // us which is which.
        if (Math.abs(a1) > Math.abs(a2) && Math.abs(a1) > Math.abs(a3)) {
            if (a1 >= 0) {
                return new GridExtrema(a, b, d, c);
            } else {
                return new GridExtrema(a, c, d, b);
            }
        } else if (Math.abs(a2) > Math.abs(a1) && Math.abs(a2) > Math.abs(a3)) {
            if (a2 >= 0) {
                return new GridExtrema(a, b, c, d);
            } else {
                return new GridExtrema(a, d, c, b);
            }
        } else {
            if (a3 >= 0) {
                return new GridExtrema(a, c, b, d);
            } else {
                return new GridExtrema(a, d, b, c);
            }
        }
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

    public Dimensions getDimensions() {
        Dimensions dimensions = new Dimensions(this.one.getEastNorth());

        dimensions.recomputeBounds(this.two.getEastNorth());
        dimensions.recomputeBounds(this.three.getEastNorth());
        dimensions.recomputeBounds(this.four.getEastNorth());

        return dimensions;
    }

    /**
     * Value class containing the minimum and maximum {@link EastNorth} coordinates of the nodes passed to it. This
     * effectively gives us a bounding box for a set of nodes.
     */
    public static class Dimensions {
        double minY;
        double maxY;
        double minX;
        double maxX;

        /**
         * Create a new {@link Dimensions} instance.
         *
         * @param eastNorth Use this coordinate pair as the base value.
         */
        public Dimensions(EastNorth eastNorth) {
            minY = eastNorth.getY();
            maxY = minY;
            minX = eastNorth.getX();
            maxX = minX;
        }

        /**
         * Add a coordinate pair to this class and recompute the bounds.
         *
         * @param eastNorth Coordinate pair to add.
         */
        public void recomputeBounds(EastNorth eastNorth) {
            minY = Math.min(minY, eastNorth.getY());
            maxY = Math.max(maxY, eastNorth.getY());
            minX = Math.min(minX, eastNorth.getX());
            maxX = Math.max(maxX, eastNorth.getX());
        }

        /**
         * Compute which is greater; the width or the height of the bounding box represented by this class, and
         * return it.
         *
         * @return The greater value of the width and height.
         */
        public double longestOfWidthAndHeight() {
            return maxX - minX > maxY - minY
                    ? maxX - minX
                    : maxY - minY;
        }

        public double getMinY() {
            return minY;
        }

        public double getMaxY() {
            return maxY;
        }

        public double getMinX() {
            return minX;
        }

        public double getMaxX() {
            return maxX;
        }

        @Override
        public String toString() {
            return "(x: " + minX + "/" + maxX + ", y: " + minY + "/" + maxY + ")";
        }
    }
}
