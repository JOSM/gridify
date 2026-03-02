// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify.exception;

/**
 * Base class for exceptions thrown within the Gridify plugin.
 */
public class GridifyException extends Exception {
    /**
     * Constructs a GridifyException.
     */
    public GridifyException() {
        super();
    }

    /**
     * Constructs a GridifyException with a message.
     *
     * @param message The error message.
     */
    public GridifyException(String message) {
        super(message);
    }
}
