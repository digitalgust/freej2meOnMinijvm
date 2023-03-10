package java.awt;

import org.mini.gui.GCallBack;
import org.mini.gui.GObject;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventListener;

public class Component implements ImageObserver, MenuContainer,
        Serializable {
    transient GObject peer;
    Graphics gGraphics;
    private Container parent;
    java.util.List<ComponentListener> compListeners = new ArrayList<>();


    public GObject getPeer() {
        return peer;
    }

    public Container getParent() {
        return parent;
    }

    public void setParent(Container parent) {
        this.parent = parent;
    }

    public void setLocation(int x, int y) {
        peer.setLocation(x, y);
    }

    public void setSize(int width, int height) {
        peer.setSize(width, height);
        dispathComponentEvent();
    }

    protected void dispathComponentEvent() {
        for (int i = 0; i < compListeners.size(); i++) {
            ComponentListener l = compListeners.get(i);
            l.componentResized(new ComponentEvent(this, ComponentEvent.COMPONENT_RESIZED));
        }
    }

    public void move(int x, int y) {
        peer.move(x, y);
    }

    public int getWidth() {
        return (int) peer.getW();
    }


    public int getHeight() {
        return (int) peer.getH();
    }

    public Color getForeground() {
        float[] c = peer.getColor();
        return new Color(c[0], c[1], c[2], c[3]);
    }

    public void setForeground(Color c) {
        peer.setColor(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
    }

    public Color getBackground() {
        float[] c = peer.getBgColor();
        return new Color(c[0], c[1], c[2], c[3]);
    }

    public void setBackground(Color c) {
        peer.setBgColor(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
    }


    public Graphics getGraphics() {
        if (gGraphics == null) {
            gGraphics = new Graphics(this.peer, GCallBack.getInstance().getNvContext());
        }
        return gGraphics;
    }

    @Override
    public Font getFont() {
        return null;
    }

    @Override
    public void remove(MenuComponent comp) {

    }

    @Override
    public boolean postEvent(Event evt) {
        return false;
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        return false;
    }

    public boolean isFocusable() {
        return true;
    }

    public void setFocusable(boolean focusable) {

    }

    public synchronized void addKeyListener(KeyListener l) {
    }

    public synchronized void addMouseListener(MouseListener l) {
    }

    public synchronized void addComponentListener(ComponentListener l) {
        compListeners.add(l);
    }

    public void setVisible(boolean b) {
        peer.setVisible(b);
    }

    public boolean isVisible() {
        return peer.isVisible();
    }

    public void paint(Graphics g) {
    }


}
