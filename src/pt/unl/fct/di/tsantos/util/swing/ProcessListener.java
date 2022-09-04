/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.unl.fct.di.tsantos.util.swing;

import java.util.EventListener;

/**
 *
 * @author tvcsantos
 */
public interface ProcessListener extends EventListener {
    public void processStart(ProcessEvent pe);

    public void processUpdate(ProcessEvent pe);

    public void processFinish(ProcessEvent pe);

    public void processInterrupt(ProcessEvent pe);
}
