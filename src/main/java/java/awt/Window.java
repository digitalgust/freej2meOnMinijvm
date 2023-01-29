package java.awt;


import org.mini.gui.GContainer;
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

    @Override
    public void setSize(int w, int h) {
        GContainer con = (GContainer) peer;
        con.setSize(w, h);
        for (int i = 0; i < children.size(); i++) {
            Component go = children.get(i);
            go.setSize(w, h);
        }
        dispathComponentEvent();
        peer.setLocation((peer.getForm().getW() - peer.getW()) * .5f, (peer.getForm().getH() - peer.getH()) * .5f);
        int debug = 1;
    }

    public void pack() {
        peer.setLocation((peer.getForm().getW() - peer.getW()) * .5f, (peer.getForm().getH() - peer.getH()) * .5f);
    }
}
