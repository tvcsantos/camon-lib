/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.unl.fct.di.tsantos.util.exceptions;

/**
 *
 * @author tvcsantos
 */
public class UnsupportedOSException extends Exception {

    public UnsupportedOSException(Throwable cause) {
        super(cause);
    }

    public UnsupportedOSException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedOSException(String message) {
        super(message);
    }

    public UnsupportedOSException() {
        super();
    }

}
