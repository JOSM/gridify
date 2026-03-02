// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify.exception;

import java.util.function.Supplier;

/**
 * Thrown when there is a problem with the user input. {@link #getMessage()} returns a human-readable error message
 * intended for the user.
 */
public class UserInputException extends GridifyException {
    /**
     * Constructs a UserInputException with a message.
     *
     * @param message The error message.
     */
    public UserInputException(String message) {
        super(message);
    }

    /**
     * Returns a supplier that creates a UserInputException.
     *
     * @param message The error message.
     * @return A supplier of UserInputException.
     */
    public static Supplier<UserInputException> error(String message) {
        return () -> new UserInputException(message);
    }
}
