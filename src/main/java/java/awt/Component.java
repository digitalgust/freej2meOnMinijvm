package java.awt;

import org.mini.gui.GCallBack;
import org.mini.gui.GObject;

import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.image.ImageObserver;
import java.io.Serializable;

public class Component implements ImageObserver, MenuContainer,
        Serializable {
    transient GObject peer;
    Graphics gGraphics;

    int x;

    int y;

    int width;

    int height;

    public GObject getPeer() {
        return peer;
    }

    public void setLocation(int x, int y) {
        peer.setLocation(x, y);
    }

    public void setSize(int width, int height) {
        peer.setSize(width, height);
    }

    public void move(int x, int y) {
        peer.move(x, y);
    }

    public int getWidth() {
        return width;
    }


    public int getHeight() {
        return height;
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

    }

    public void setVisible(boolean b) {
        peer.setVisible(b);
    }

    public boolean isVisible() {
        return peer.isVisible();
    }
}
