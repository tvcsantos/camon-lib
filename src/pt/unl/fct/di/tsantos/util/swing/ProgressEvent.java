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
public class ProgressEvent extends EventObject {

    protected int progress;
    //protected int expected;

    public ProgressEvent(Object source) {
        super(source);
    }

    public ProgressEvent(Object source, int progress/*, int expected*/) {
        super(source);
        this.progress = progress;
        //this.expected = expected;
    }

    public int getProgress() {
        return progress;
    }

    /*public int getExpected() {
        return expected;
    }*/
}
