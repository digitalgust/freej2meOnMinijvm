package java.awt;


import org.mini.gui.GToolkit;

import java.awt.event.WindowListener;

public class Window extends Container {
    transient WindowListener windowListener;

    public synchronized void addWindowListener(WindowListener l) {
        this.windowListener = l;
    }

    public void setVisible(boolean b) {
        this.peer.setVisible(b);
        GToolkit.showFrame(this.peer);
    }

    public void pack() {

    }
}
