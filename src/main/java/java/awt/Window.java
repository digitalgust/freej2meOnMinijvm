package java.awt;


import org.mini.gui.*;
import org.mini.gui.callback.GCallBack;
import org.mini.gui.event.GStateChangeListener;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

public class Window extends Container {

    static float HIGHT_ADD = GFrame.TITLE_HEIGHT + GFrame.PAD * 2;
    static float WIDTH_ADD = GFrame.PAD * 2;

    java.util.List<WindowListener> windowListeners = new ArrayList<>();

    public Window() {
        super();
        GFrame peer = new GFrame(GCallBack.getInstance().getApplication().getForm(), "", 0, 0, 300 + WIDTH_ADD, 200 + HIGHT_ADD);
//        GObject peer = new GPanel(GCallBack.getInstance().getApplication().getForm(), 0, 0, 300, 200);
        setPeer(peer);
    }

    public synchronized void addWindowListener(WindowListener l) {
        this.windowListeners.add(l);
        getPeer().setStateChangeListener(new GStateChangeListener() {
            @Override
            public void onStateChange(GObject gObject) {
                if (gObject.getParent() == null) {
                    for (WindowListener wl : windowListeners) {
                        wl.windowClosing(new WindowEvent(Window.this, WindowEvent.WINDOW_CLOSING));
                        wl.windowClosed(new WindowEvent(Window.this, WindowEvent.WINDOW_CLOSED));
                    }
                }
            }
        });
    }

    public void setVisible(boolean b) {
        this.getPeer().setVisible(b);
        GForm gform = GCallBack.getInstance().getApplication().getForm();
        gform.add(this.getPeer());
        gform.setCurrent(this.getPeer());
        gform.flushNow();
    }

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
        GContainer peer = (GContainer) getPeer();
        peer.setSize(w + WIDTH_ADD, h + HIGHT_ADD);
        for (int i = 0; i < children.size(); i++) {
            Component go = children.get(i);
            go.setSize(w, h);
        }
        dispathComponentEvent();
        peer.flushNow();
    }

    public void pack() {
        getPeer().setLocation((getPeer().getForm().getW() - getPeer().getW()) * .5f, (getPeer().getForm().getH() - getPeer().getH()) * .5f);
        getPeer().flushNow();
    }

    public void dispose() {
        GCallBack.getInstance().getApplication().getForm().remove(getPeer());
    }
}
