/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.unl.fct.di.tsantos.util.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.TaskMonitor;
import org.jdesktop.application.View;

/**
 *
 * @author tvcsantos
 */
public abstract class ProgressBar {

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons;
    private int busyIconIndex = 0;
    
    private JLabel statusMessageLabel;
    private JProgressBar progressBar;
    private JLabel statusAnimationLabel;
    
    protected abstract String initMessageTimout();
    protected abstract String initBusyAnimationRate();
    protected abstract String initBusyIcons(int i);
    protected abstract String initIdleIcon();
    
    protected void messageTimerActionPerformed(ActionEvent e) {
        if (statusMessageLabel != null)
            statusMessageLabel.setText("");
        else progressBar.setString("");
    }
    
    public ProgressBar(View view, JProgressBar progressBar,
             JLabel statusAnimationLabel, 
            int animationSize) {
        this(view, null, progressBar, statusAnimationLabel, animationSize);
    }

    public ProgressBar(View view,
             JLabel statusMessageLabel,
             JProgressBar progressBar,
             JLabel statusAnimationLabel, 
            int animationSize) {
        this.statusMessageLabel = statusMessageLabel;
        this.progressBar = progressBar;
        this.statusAnimationLabel = statusAnimationLabel;
        
        if (this.statusMessageLabel == null) {
            this.progressBar.setStringPainted(true);
            this.progressBar.setString("");
        }
                
        busyIcons = new Icon[animationSize];
        // status bar initialization - message timeout, 
        // idle icon and busy animation, etc
        ResourceMap resourceMap = view.getResourceMap();
        int messageTimeout =
                resourceMap.getInteger(initMessageTimout());
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                messageTimerActionPerformed(e);
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = 
                resourceMap.getInteger(initBusyAnimationRate());
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon(initBusyIcons(i));
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                ProgressBar.this.statusAnimationLabel.setIcon(
                        busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon(initIdleIcon());
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(true);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(
                view.getApplication().getContext());
        taskMonitor.addPropertyChangeListener(
                new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    taskMonitorPropertyChandedStarted(evt);                    
                } else if ("done".equals(propertyName)) {
                    taskMonitorPropertyChandedDone(evt);
                } else if ("message".equals(propertyName)) {
                    taskMonitorPropertyChandedMessage(evt);
                } else if ("progress".equals(propertyName)) {
                    taskMonitorPropertyChandedProgress(evt);
                } else taskMonitorPropertyChandedOther(evt);
            }
        });
    }
    
    protected void taskMonitorPropertyChandedStarted(
            java.beans.PropertyChangeEvent evt) {
        if (!busyIconTimer.isRunning()) {
            statusAnimationLabel.setIcon(busyIcons[0]);
            busyIconIndex = 0;
            busyIconTimer.start();
        }
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
    }
    
    protected void taskMonitorPropertyChandedDone(
            java.beans.PropertyChangeEvent evt) {
        busyIconTimer.stop();
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
    }
    
    protected void taskMonitorPropertyChandedMessage(
            java.beans.PropertyChangeEvent evt) {
        String text = (String) (evt.getNewValue());
        if (statusMessageLabel != null)
            statusMessageLabel.setText((text == null) ? "" : text);
        else progressBar.setString((text == null) ? "" : text);
        messageTimer.restart();
    }
    
    protected void taskMonitorPropertyChandedProgress(
            java.beans.PropertyChangeEvent evt) {
        int value = (Integer) (evt.getNewValue());
        progressBar.setVisible(true);
        progressBar.setIndeterminate(false);
        progressBar.setValue(value);
    }
    
    protected void taskMonitorPropertyChandedOther(
            java.beans.PropertyChangeEvent evt) {}
}
