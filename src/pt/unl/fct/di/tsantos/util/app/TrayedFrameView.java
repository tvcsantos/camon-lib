/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.unl.fct.di.tsantos.util.app;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.jdesktop.application.Application;
import pt.unl.fct.di.tsantos.util.awt.JXTrayIcon;

/**
 *
 * @author tvcsantos
 */
public abstract class TrayedFrameView<T extends Application>
        extends DefaultFrameView<T> {
    protected JXTrayIcon trayIcon;

    public TrayedFrameView(T application) {
        super(application);
        
        //UIManager.put("swing.boldMetal", Boolean.FALSE);
        //Schedule a job for the event-dispatching thread:
        //adding TrayIcon.
        getFrame().addWindowListener(new WindowListener() {

            public void windowOpened(WindowEvent e) { }
            public void windowClosing(WindowEvent e) { }
            public void windowClosed(WindowEvent e) { }

            public void windowIconified(WindowEvent e) {
                getFrame().setVisible(false);
                getFrame().setState(JFrame.ICONIFIED);
            }

            public void windowDeiconified(WindowEvent e) { }
            public void windowActivated(WindowEvent e) { }
            public void windowDeactivated(WindowEvent e) { }
        });

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowTray();
            }
        });
    }

    protected abstract String getTrayApplicationName();

    private void createAndShowTray() {
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            System.err.println("SystemTray is not supported");
            return;
        }

        trayIcon = createTrayIcon();
        if (trayIcon == null) {
            System.err.println("SystemTray is not supported. "
                    + "SystemTray must have an icon");
        }
        
        //final PopupMenu popup = new PopupMenu();
        final JPopupMenu popup = new JPopupMenu();
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a popup menu components
        JMenuItem showApplicationItem = new JMenuItem("Show " +
                getTrayApplicationName());
        JMenuItem aboutItem = new JMenuItem("About");
        JMenuItem exitItem = new JMenuItem("Exit");

        //Add components to popup menu
        popup.add(showApplicationItem);
        //popup.addSeparator();
        popup.add(aboutItem);
        popup.addSeparator();
        int added = addComponentsToTray(popup);
        if (added > 0) popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setJPopupMenu(popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip(getTrayApplicationName());

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("TrayIcon could not be added.");
            return;
        }

        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getFrame().setVisible(true);
                getFrame().setState(JFrame.NORMAL);
            }
        });

        showApplicationItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getFrame().setVisible(true);
                getFrame().setState(JFrame.NORMAL);
            }
        });

        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showAboutBox();
            }
        });

        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
                getApplication().exit();
            }
        });
    }

    protected int addComponentsToTray(JPopupMenu menu) {
        return 0;
    }

    protected abstract JXTrayIcon createTrayIcon();
}
