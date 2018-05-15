// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify.exception;

/**
 * Base class for exceptions thrown within the Gridify plugin.
 */
public class GridifyException extends Exception {
    public GridifyException() {
        super();
    }

    public GridifyException(String message) {
        super(message);
    }
}
