package java.awt;

import org.mini.gui.GCallBack;
import org.mini.gui.GFrame;
import org.mini.gui.GObject;
import org.mini.gui.GPanel;


public class Frame extends Window implements MenuContainer {

    public Frame() {
        this("");
    }

    public Frame(String title) {
        super();
        setTitle(title);
    }

    public synchronized void setIconImage(Image image) {
        if (getPeer() != null) {
        }
    }

    public void setResizable(boolean resizable) {
    }

    public void setTitle(String title) {
        ((GFrame) getPeer()).setTitle(title);
    }

    public String getTitle() {
        return ((GFrame) getPeer()).getTitle();
    }

    public int getHeight() {
        return (int) ((GFrame) getPeer()).getView().getH();
    }

    public int getWidth() {
        return (int) ((GFrame) getPeer()).getView().getW();
    }

}
