// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify;

import nl.jeroenhoek.josm.gridify.exception.GridifyException;
import nl.jeroenhoek.josm.gridify.exception.UserInputException;
import nl.jeroenhoek.josm.gridify.exception.UserCancelledException;
import nl.jeroenhoek.josm.gridify.ui.GridifySettingsDialog;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SelectCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.IntegerProperty;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.SubclassFilteredCollection;
import org.openstreetmap.josm.tools.Utils;

import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.*;

import static nl.jeroenhoek.josm.gridify.exception.UserInputException.error;
import static org.openstreetmap.josm.tools.I18n.set;
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
        DataSet dataSet = getLayerManager().getEditDataSet();

        Collection<Command> commands;
        try {
            commands = performGridifyAction(dataSet);
        } catch (UserInputException e) {
            // Tell the user what was wrong with their input.
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), e.getMessage());
            return;
        } catch (UserCancelledException e) {
            // That's fine.
            return;
        } catch (GridifyException e) {
            Logging.warn(e.getMessage());
            return;
        }

        UndoRedoHandler.getInstance().add(new SequenceCommand(DESCRIPTION, commands));
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

    Collection<Command> performGridifyAction(DataSet dataSet) throws GridifyException {
        if (dataSet == null) throw new GridifyException("Called with null data-set.");

        Collection<OsmPrimitive> selection = dataSet.getSelected();

        InputData inputData = inputDataFromSelection(selection)
                .orElseThrow(error(tr("Select four nodes, or a way consisting of four nodes.")));

        GridifySettings settings = new GridifySettings();

        GridifySettingsDialog dialog = new GridifySettingsDialog(inputData, settings);
        dialog.showDialog();

        // Only the OK button returns 1, the rest means 'Cancel' or a closed dialog window.
        if (dialog.getValue() != 1) {
            throw new UserCancelledException();
        }

        GridExtrema extrema = inputData.getGridExtrema();
        Collection<Command> commands = new ArrayList<>();

        // Read the user provided settings.
        int numRows = dialog.getRowCount();
        int numColumns = dialog.getColumnCount();
        Operation operation = dialog.getOperation();
        boolean deleteSourceWay = dialog.deleteSourceWay();
        boolean copyTags = dialog.copyTags();

        // Update settings properties now that we are about to commence the operation.
        // This way the user gets to keep the last settings they entered.
        settings.setNumRows(numRows);
        settings.setNumColumns(numColumns);
        settings.setDeleteSource(deleteSourceWay);
        settings.setCopyTagsFromSource(copyTags);
        settings.setOperation(operation);

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

    void addToDataSet(Collection<Command> commands, DataSet dataSet, List<? extends OsmPrimitive> primitives) {
        for (OsmPrimitive primitive : primitives) {
            commands.add(new AddCommand(dataSet, primitive));
        }
    }

    List<Node> combine(Node start, List<Node> between, Node stop) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(start);
        nodes.addAll(between);
        nodes.add(stop);
        return nodes;
    }

}
