/*
 * Copyright 2008 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package pt.unl.fct.di.tsantos.util.awt;

import javax.swing.*;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JXTrayIcon extends TrayIcon {
    private JPopupMenu menu;
    private static JDialog dialog;
    static {
        dialog = new JDialog((Frame) null, "TrayDialog");
        dialog.setUndecorated(true);
        dialog.setAlwaysOnTop(true);
    }

    private static PopupMenuListener popupListener = new PopupMenuListener() {
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            dialog.setVisible(false);
        }

        public void popupMenuCanceled(PopupMenuEvent e) {
            dialog.setVisible(false);
        }
    };


    public JXTrayIcon(Image image) {
        super(image);
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                showJPopupMenu(e);
            }

            public void mouseReleased(MouseEvent e) {
                showJPopupMenu(e);
            }
        });
    }

    private void showJPopupMenu(MouseEvent e) {
        if (e.isPopupTrigger() && menu != null) {
            Dimension size = menu.getPreferredSize();
            dialog.setLocation(e.getX(), e.getY() - size.height);
            dialog.setVisible(true);
            menu.show(dialog.getContentPane(), 0, 0);
            // popup works only for focused windows
            dialog.toFront();
        }
    }

    public JPopupMenu getJPopupMenu() {
        return menu;
    }

    public void setJPopupMenu(JPopupMenu menu) {
        if (this.menu != null) {
            this.menu.removePopupMenuListener(popupListener);
        }
        this.menu = menu;
        menu.addPopupMenuListener(popupListener);
    }
}
