package java.awt;

import org.mini.gui.GCallBack;
import org.mini.gui.GContainer;
import org.mini.gui.GObject;
import sun.misc.GC;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class Component implements ImageObserver, MenuContainer,
        Serializable {

    private String name;
    private transient GObject peer;
    Graphics gGraphics;
    private Container parent;
    java.util.List<ComponentListener> compListeners = new ArrayList<>();
    java.util.List<KeyListener> keyListeners = new ArrayList<>();
    java.util.List<MouseListener> mouseListeners = new ArrayList<>();
    java.util.List<MouseMotionListener> mouseMotionListeners = new ArrayList<>();


    public GObject getPeer() {
        return peer;
    }

    public void setPeer(GObject peer) {
        this.peer = peer;
        this.peer.setAttachment(this);
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

    public void setPreferredSize(Dimension preferredSize) {
        if (preferredSize != null) {
            setSize(preferredSize.width, preferredSize.height);
        }
    }

    public void setBounds(int x, int y, int width, int height) {
        setLocation(x, y);
        setSize(width, height);
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
            gGraphics = new Graphics(this.peer, GCallBack.getInstance().getNvContext()) {
            };
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

    public List<KeyListener> getKeyListeners() {
        return keyListeners;
    }

    public synchronized void addKeyListener(KeyListener l) {
        keyListeners.add(l);

    }

    public synchronized void removeKeyListener(KeyListener l) {
        keyListeners.remove(l);
    }

    public synchronized void addMouseListener(MouseListener l) {
        mouseListeners.add(l);
    }

    public List<MouseListener> getMouseListeners() {
        return mouseListeners;
    }


    public List<MouseMotionListener> getMouseMotionListeners() {
        return mouseMotionListeners;
    }

    public synchronized void addMouseMotionListener(MouseMotionListener l) {
        mouseMotionListeners.add(l);
    }

    public synchronized void removeMouseListener(MouseListener l) {
        mouseListeners.remove(l);
    }

    public synchronized void removeMouseMotionListener(MouseMotionListener l) {
        mouseMotionListeners.remove(l);
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

    public void repaint() {
    }

    public void requestFocus() {
        GContainer p = peer.getParent();
        if (p != null) {
            peer.getParent().setCurrent(peer);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if(peer!=null){
            peer.setName(name);
        }
    }
}
