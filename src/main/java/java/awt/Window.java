package java.awt;


import org.mini.gui.GCallBack;
import org.mini.gui.GContainer;
import org.mini.gui.GFrame;
import org.mini.gui.GToolkit;

import java.awt.event.WindowListener;

public class Window extends Container {
    transient WindowListener windowListener;

    public synchronized void addWindowListener(WindowListener l) {
        this.windowListener = l;
    }

    public void setVisible(boolean b) {
        this.getPeer().setVisible(b);
        if (b) GToolkit.showFrame(this.getPeer());
    }

    @Override
    public void setSize(int w, int h) {
        GContainer peer = (GContainer) getPeer();
        peer.setSize(w, h);
        for (int i = 0; i < children.size(); i++) {
            Component go = children.get(i);
            go.setSize(w, h);
        }
        dispathComponentEvent();
        peer.setLocation((peer.getForm().getW() - peer.getW()) * .5f, (peer.getForm().getH() - peer.getH()) * .5f);
        int debug = 1;
    }

    public void pack() {
        getPeer().setLocation((getPeer().getForm().getW() - getPeer().getW()) * .5f, (getPeer().getForm().getH() - getPeer().getH()) * .5f);
    }

    public void dispose() {
        GCallBack.getInstance().getApplication().getForm().remove(getPeer());
    }
}
