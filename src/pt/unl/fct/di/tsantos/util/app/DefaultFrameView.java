/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.unl.fct.di.tsantos.util.app;

import org.jdesktop.application.Application;
import org.jdesktop.application.FrameView;

/**
 *
 * @author tvcsantos
 */
public abstract class DefaultFrameView<T extends Application>
        extends FrameView {
    
    public DefaultFrameView(T application) {
        super(application);
    }

    public final T getTheApplication() {
        return (T)getApplication();
    }

    protected abstract void showAboutBox();
}
