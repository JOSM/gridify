// License: GPL. For details, see LICENSE file.
package nl.jeroenhoek.josm.gridify.exception;

import java.util.function.Supplier;

/**
 * Thrown when there is a problem with the user input. {@link #getMessage()} returns a human-readable error message
 * intended for the user.
 */
public class UserInputException extends GridifyException {
    public UserInputException(String message) {
        super(message);
    }

    public static Supplier<UserInputException> error(String message) {
        return () -> new UserInputException(message);
    }
}
