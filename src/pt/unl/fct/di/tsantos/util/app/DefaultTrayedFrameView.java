/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.unl.fct.di.tsantos.util.app;

import javax.swing.ImageIcon;
import pt.unl.fct.di.tsantos.util.awt.JXTrayIcon;

/**
 *
 * @author tvcsantos
 */
public abstract class DefaultTrayedFrameView
        <T extends DefaultSingleFrameApplication>
        extends TrayedFrameView<T>
{
    public DefaultTrayedFrameView(T application) {
        super(application);
        if (getTheApplication().getFrameIcon() != null)
            getFrame().setIconImage(
                    getTheApplication().getFrameIcon().getImage());
    }

    @Override
    protected String getTrayApplicationName() {
        return getTheApplication().getShortName();
    }

    @Override
    protected JXTrayIcon createTrayIcon() {
        ImageIcon icon = getTheApplication().getTrayImageIcon();
        if (icon != null) return new JXTrayIcon(icon.getImage());
        return null;
    }
}
