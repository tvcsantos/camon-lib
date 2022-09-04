/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.unl.fct.di.tsantos.util.exceptions;

/**
 *
 * @author tvcsantos
 */
public class NotExecutableException extends Exception {

    public NotExecutableException(Throwable cause) {
        super(cause);
    }

    public NotExecutableException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotExecutableException(String message) {
        super(message);
    }

    public NotExecutableException() {
        super();
    }

}
