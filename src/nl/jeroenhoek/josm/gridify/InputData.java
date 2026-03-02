// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify;

import org.openstreetmap.josm.data.osm.Way;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Sanitized representation of the primitives selected by the user.
 */
public class InputData {
    private final GridExtrema extrema;
    private final Way sourceWay;
    private final Map<String, String> tags;

    /**
     * Constructs InputData from a {@link GridExtrema} instance.
     *
     * @param extrema The grid extrema.
     */
    public InputData(GridExtrema extrema) {
        this.extrema = extrema;
        this.tags = new HashMap<>();
        this.sourceWay = null;
    }

    /**
     * Constructs InputData with a source way and its tags.
     *
     * @param extrema   The grid extrema.
     * @param sourceWay The source way.
     * @param tags      The tags from the source way.
     */
    public InputData(GridExtrema extrema, Way sourceWay, Map<String, String> tags) {
        this.extrema = extrema;
        this.sourceWay = sourceWay;
        this.tags = tags;
    }

    /**
     * Get the computed {@link GridExtrema} for this user input.
     *
     * @return A {@link GridExtrema} instance. @{code null} is never returned.
     */
    public GridExtrema getGridExtrema() {
        return extrema;
    }

    /**
     * The tags present on the way the user selected. If that has no tags, or no way was selected, this method
     * returns an empty {@link Map}.
     *
     * @return A map of tags. @{code null} is never returned, but the {@link Map} may be empty.
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * The source way the user selected, if present.
     *
     * @return An optional containing the source {@link Way} if the user selected one.
     */
    public Optional<Way> getSourceWay() {
        return Optional.ofNullable(sourceWay);
    }
}
