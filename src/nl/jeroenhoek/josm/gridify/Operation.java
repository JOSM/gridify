// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify;

import nl.jeroenhoek.josm.gridify.exception.GridifyException;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

import java.util.ArrayList;
import java.util.List;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Operation to perform.
 */
public enum Operation {
    /**
     * Generate a grid of connected straight lines.
     */
    LINES {
        @Override
        public List<Way> perform(List<List<Node>> columns) {
            List<Way> lines = new ArrayList<>();

            int rows = 0;
            for (List<Node> column : columns) {
                rows = column.size();
                Way line = new Way();
                for (Node node : column) {
                    line.addNode(node);
                }
                lines.add(line);
            }

            for (int i = 0; i < rows; i++) {
                Way line = new Way();
                for (List<Node> column : columns) {
                    line.addNode(column.get(i));
                }
                lines.add(line);
            }

            return lines;
        }

        @Override
        public String toString() {
            return tr("Lines");
        }
    },
    /**
     * Generate a grid of connected blocks.
     */
    BLOCKS {
        @Override
        public List<Way> perform(List<List<Node>> columns) throws GridifyException {
            List<Way> blocks = new ArrayList<>();
            if (columns.size() <= 1) return blocks;

            for (int col = 0; col < columns.size() - 1; col++) {
                List<Node> left = columns.get(col);
                List<Node> right = columns.get(col + 1);

                if (left.size() <= 1 || right.size() <= 1) continue;
                if (left.size() != right.size()) {
                    throw new GridifyException("Nodes per column varies. This should not be possible.");
                }

                for (int row = 0; row < left.size() - 1; row++) {
                    Way block = new Way();
                    block.addNode(left.get(row));
                    block.addNode(right.get(row));
                    block.addNode(right.get(row + 1));
                    block.addNode(left.get(row + 1));
                    block.addNode(left.get(row));
                    blocks.add(block);
                }
            }

            return blocks;
        }

        @Override
        public String toString() {
            return tr("Blocks");
        }
    };

    /**
     * Perform the operation.
     *
     * @param columns All the nodes of the grid, as a list of columns each containing an equal number of nodes.
     * @return A list of newly generated {@link Way} instances.
     * @throws GridifyException Thrown when an error occurs.
     */
    public abstract List<Way> perform(List<List<Node>> columns) throws GridifyException;
}
