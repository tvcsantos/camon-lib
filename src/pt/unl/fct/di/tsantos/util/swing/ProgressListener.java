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
public interface ProgressListener extends EventListener {
    public void progressStart(ProgressEvent pe);

    public void progressUpdate(ProgressEvent pe);

    public void progressFinish(ProgressEvent pe);

    public void progressInterrupt(ProgressEvent pe);
}
