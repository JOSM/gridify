// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify;

import nl.jeroenhoek.josm.gridify.exception.GridifyException;
import nl.jeroenhoek.josm.gridify.exception.UserCancelledException;
import nl.jeroenhoek.josm.gridify.exception.UserInputException;
import nl.jeroenhoek.josm.gridify.ui.GridifySettingsDialog;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SelectCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.SystemOfMeasurement;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.SubclassFilteredCollection;
import org.openstreetmap.josm.tools.Utils;

import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import static nl.jeroenhoek.josm.gridify.exception.UserInputException.error;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * The Gridify action opens a modal dialog where the user can configure the operation of this plugin, and execute it.
 */
public class GridifyAction extends JosmAction {
    static final String DESCRIPTION = tr("Generate a grid of ways from four nodes.");

    private final List<Command> currentPreviewCommands = new ArrayList<>();

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
        DataSet dataSet = getLayerManager().getEditDataSet();

        try {
            performGridifyAction(dataSet);
        } catch (UserInputException e) {
            // Tell the user what was wrong with their input.
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), e.getMessage());
        } catch (UserCancelledException e) {
            // That's fine.
        } catch (GridifyException e) {
            Logging.warn(e.getMessage());
        }

        MainApplication.getMap().repaint();
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

    /**
     * Core logic of the Gridify action.
     *
     * @param dataSet The current dataset.
     * @throws GridifyException Thrown if an error occurs during the operation.
     */
    void performGridifyAction(DataSet dataSet) throws GridifyException {
        if (dataSet == null) throw new GridifyException("Called with null data-set.");

        Collection<OsmPrimitive> selection = dataSet.getSelected();

        InputData inputData = inputDataFromSelection(selection)
                .orElseThrow(error(tr("Select four nodes, or a way consisting of four nodes.")));

        GridifySettings settings = new GridifySettings();

        GridifySettingsDialog dialog = new GridifySettingsDialog(inputData, settings);

        dialog.addChangeListener(() -> updatePreview(dialog, dataSet, inputData));

        // Trigger initial calculation
        updatePreview(dialog, dataSet, inputData);

        dialog.showDialog();

        // Only the OK button returns 1, the rest means 'Cancel' or a closed dialog window.
        if (dialog.getValue() != 1) {
            // Undo any remaining preview changes
            ListIterator<Command> it = currentPreviewCommands.listIterator(currentPreviewCommands.size());
            while (it.hasPrevious()) {
                it.previous().undoCommand();
            }
            currentPreviewCommands.clear();

            throw new UserCancelledException();
        }

        // On OK, we want to make a single sequence command appear in the undo history.
        // The commands are already executed, so undo them before adding UndoRedoHandler, because it executes them again.
        List<Command> finalCommands = new ArrayList<>(currentPreviewCommands);

        ListIterator<Command> it = finalCommands.listIterator(finalCommands.size());
        while (it.hasPrevious()) {
            it.previous().undoCommand();
        }

        currentPreviewCommands.clear();
        UndoRedoHandler.getInstance().add(new SequenceCommand(tr("Create a grid of {0} elements", getCreateWayCount(finalCommands)), finalCommands));

        // Update settings properties now that we are about to commence the operation.
        // This way the user gets to keep the last settings they entered.
        settings.setNumRows(dialog.getRowCount());
        settings.setNumColumns(dialog.getColumnCount());
        settings.setDeleteSource(dialog.deleteSourceWay());
        settings.setCopyTagsFromSource(dialog.copyTags());
        settings.setOperation(dialog.getOperation());
    }

    /**
     * Updates the preview by recalculating and applying grid commands.
     *
     * @param dialog    The settings dialog
     * @param dataSet   The JOSM data set
     * @param inputData The input data (selected nodes/way)
     */
    private void updatePreview(GridifySettingsDialog dialog, DataSet dataSet, InputData inputData) {
        // 1. Undo previous preview
        ListIterator<Command> it = currentPreviewCommands.listIterator(currentPreviewCommands.size());
        while (it.hasPrevious()) {
            it.previous().undoCommand();
        }
        currentPreviewCommands.clear();

        // 2. Calculate new grid
        Collection<Command> newCommands;
        try {
            newCommands = calculateGridCommands(dataSet, inputData,
                    dialog.getRowCount(),
                    dialog.getColumnCount(),
                    dialog.getOperation(),
                    dialog.copyTags(),
                    dialog.deleteSourceWay());
        } catch (GridifyException e) {
            Logging.warn(e.getMessage());
            return;
        }

        // 3. Apply new commands (but don't add to UndoRedoHandler yet)
        for (Command cmd : newCommands) {
            cmd.executeCommand();
        }
        currentPreviewCommands.addAll(newCommands);

        // 4. Update the UI count and cell size
        updateDialogInfo(dialog, newCommands, inputData);

        MainApplication.getMap().repaint();
    }

    /**
     * Calculates the JOSM commands needed to generate the grid.
     *
     * @param dataSet         The dataset to operate on.
     * @param inputData       The user-selected input data (nodes/way).
     * @param numRows         Number of rows in the grid.
     * @param numColumns      Number of columns in the grid.
     * @param operation       The operation type (blocks or lines).
     * @param copyTags        Whether to copy tags from the source way.
     * @param deleteSourceWay Whether to delete the source way after the operation.
     * @return A collection of commands that, when executed, generate the grid.
     * @throws GridifyException Thrown if there is an error in the grid calculation.
     */
    Collection<Command> calculateGridCommands(DataSet dataSet, InputData inputData, int numRows, int numColumns,
                                              Operation operation, boolean copyTags, boolean deleteSourceWay) throws GridifyException {
        GridExtrema extrema = inputData.getGridExtrema();
        Collection<Command> commands = new ArrayList<>();

        List<Node> nodesTopBetween = nodesBetween(extrema.one, extrema.two, numColumns - 1);
        addToDataSet(commands, dataSet, nodesTopBetween);
        List<Node> nodesTop = combine(extrema.one, nodesTopBetween, extrema.two);

        List<Node> nodesBottomBetween = nodesBetween(extrema.four, extrema.three, numColumns - 1);
        addToDataSet(commands, dataSet, nodesBottomBetween);
        List<Node> nodesBottom = combine(extrema.four, nodesBottomBetween, extrema.three);

        List<List<Node>> columns = new ArrayList<>(numColumns + 1);

        for (int i = 0; i < numColumns + 1; i++) {
            Node start = nodesTop.get(i);
            Node stop = nodesBottom.get(i);
            List<Node> nodesBetween = nodesBetween(start, stop, numRows - 1);
            addToDataSet(commands, dataSet, nodesBetween);
            List<Node> column = combine(start, nodesBetween, stop);
            columns.add(column);
        }

        List<Way> ways = operation.perform(columns);
        addToDataSet(commands, dataSet, ways);
        if (!inputData.getTags().isEmpty() && copyTags) {
            commands.add(new ChangePropertyCommand(dataSet, ways, inputData.getTags()));
        }

        if (deleteSourceWay) {
            inputData.getSourceWay().ifPresent(way -> commands.add(new DeleteCommand(dataSet, way)));
        }

        commands.add(new SelectCommand(dataSet, new ArrayList<>(ways)));

        return commands;
    }

    private void updateDialogInfo(GridifySettingsDialog dialog, Collection<Command> commands, InputData inputData) {
        long wayCount = getCreateWayCount(commands);
        dialog.setGridCount((int) wayCount);

        GridExtrema extrema = inputData.getGridExtrema();
        GridExtrema.CellDimensions dims = extrema.getAverageCellDimensions(dialog.getRowCount(), dialog.getColumnCount());

        SystemOfMeasurement som = SystemOfMeasurement.getSystemOfMeasurement();
        String info = tr("Average cell size: {0} \u00d7 {1} (Area: {2})",
                som.getDistText(dims.width),
                som.getDistText(dims.height),
                som.getAreaText(dims.area));
        dialog.setCellSize(info);
    }

    private static long getCreateWayCount(Collection<Command> commands) {
        return commands.stream()
                .filter(c -> c instanceof AddCommand)
                .map(c -> (AddCommand) c)
                .flatMap(c -> c.getParticipatingPrimitives().stream())
                .filter(p -> p instanceof Way)
                .count();
    }

    Optional<InputData> inputDataFromSelection(Collection<OsmPrimitive> selection) {
        // Four nodes?
        if (selection.size() == 4) {
            SubclassFilteredCollection<OsmPrimitive, Node> nodes = Utils.filteredCollection(selection, Node.class);
            if (nodes.size() == 4) {
                Iterator<Node> it = nodes.iterator();
                GridExtrema extrema = GridExtrema.from(it.next(), it.next(), it.next(), it.next());
                return Optional.of(new InputData(extrema));
            }
        }

        // One way consisting of four nodes?
        if (selection.size() == 1) {
            SubclassFilteredCollection<OsmPrimitive, Way> ways = Utils.filteredCollection(selection, Way.class);
            if (ways.size() == 1) {
                Way way = ways.iterator().next();
                List<Node> nodes = way.getNodes();
                // In closed ways consisting of four nodes the first node is repeated as the last node,
                // so #getNodes returns 5 nodes for such closed ways.
                if (nodes.size() == (way.isClosed() ? 5 : 4)) {
                    GridExtrema extrema = GridExtrema.from(nodes.get(0), nodes.get(1), nodes.get(2), nodes.get(3));
                    TagMap tags = way.getKeys();
                    return Optional.of(new InputData(extrema, way, tags));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Interpolates a specified number of nodes between two existing nodes.
     *
     * @param start The starting node.
     * @param stop  The ending node.
     * @param n     The number of nodes to create between start and stop.
     * @return A list of newly created nodes.
     */
    @SuppressWarnings("MixedMutabilityReturnType")
    List<Node> nodesBetween(Node start, Node stop, int n) {
        if (n < 1) return Collections.emptyList();

        EastNorth startCoords = start.getEastNorth();
        EastNorth stopCoords = stop.getEastNorth();

        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double fraction = (i + 1.0) / (n + 1.0);
            EastNorth intCoords = startCoords.interpolate(stopCoords, fraction);
            nodes.add(new Node(intCoords));
        }

        return nodes;
    }

    /**
     * Helper to add primitives to a collection of AddCommands.
     *
     * @param commands   The collection to add commands to.
     * @param dataSet    The JOSM data set.
     * @param primitives The primitives to be added.
     */
    void addToDataSet(Collection<Command> commands, DataSet dataSet, List<? extends OsmPrimitive> primitives) {
        for (OsmPrimitive primitive : primitives) {
            commands.add(new AddCommand(dataSet, primitive));
        }
    }

    /**
     * Combines a start node, a list of intermediate nodes, and a stop node into a single list.
     *
     * @param start   The start node.
     * @param between The list of nodes in between.
     * @param stop    The stop node.
     * @return A combined list of nodes.
     */
    List<Node> combine(Node start, List<Node> between, Node stop) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(start);
        nodes.addAll(between);
        nodes.add(stop);
        return nodes;
    }

}
