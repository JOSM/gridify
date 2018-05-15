// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * The Gridify plugin turns four nodes into a user configured grid of shapes. Useful for splitting up areas into
 * {@code amenity=parking_space} etc.
 */
public class GridifyPlugin extends Plugin {
    public GridifyPlugin(PluginInformation info) {
        super(info);
        MainMenu.add(MainApplication.getMenu().moreToolsMenu, new GridifyAction());
    }
}
