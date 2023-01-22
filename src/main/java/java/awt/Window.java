package java.awt;


import org.mini.gui.GContainer;
import org.mini.gui.GFrame;
import org.mini.gui.GObject;
import org.mini.gui.GToolkit;

import java.awt.event.WindowListener;
import java.util.List;

public class Window extends Container {
    transient WindowListener windowListener;

    public synchronized void addWindowListener(WindowListener l) {
        this.windowListener = l;
    }

    public void setVisible(boolean b) {
        this.peer.setVisible(b);
        GToolkit.showFrame(this.peer);
    }

    @Override
    public void setSize(int w, int h) {
        GContainer con = (GContainer) peer;
        con.setSize(w + 4, h + GFrame.TITLE_HEIGHT + GFrame.PAD * 2);
        for (int i = 0; i < children.size(); i++) {
            Component go = children.get(i);
            go.setSize(w, h);
        }
    }

    public void pack() {

    }
}
