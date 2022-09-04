/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.unl.fct.di.tsantos.util.swing;

import java.util.EventObject;

/**
 *
 * @author tvcsantos
 */
public class ProcessEvent extends EventObject {

    protected String message;
    protected Object argument;
    protected boolean subProcess;

    public ProcessEvent(Object source) {
        this(source, false);
    }

    public ProcessEvent(Object source, boolean subProcess) {
        this(source, subProcess, null, null);
    }
    
    public ProcessEvent(Object source, String message) {
        this(source, false, message, null);
    }

    public ProcessEvent(Object source, Object argument) {
        this(source, false, null, argument);
    }

    public ProcessEvent(Object source, boolean subProcess, String message) {
        this(source, subProcess, message, null);
    }

    public ProcessEvent(Object source, boolean subProcess, Object argument) {
        this(source, subProcess, null, argument);
    }

    public ProcessEvent(Object source, String message, Object argument) {
        this(source, false, message, argument);
    }
    
    public ProcessEvent(Object source, boolean subProcess, String message,
            Object argument) {
        super(source);
        this.subProcess = subProcess;
        this.message = message;
        this.argument = argument;
    }
    
    public String getMessage() {
        return message;
    }

    public Object getArgument() {
        return argument;
    }
    
    public int getID() {
        return source.hashCode();
    }    
}
