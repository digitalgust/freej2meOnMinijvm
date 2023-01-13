package java.awt;

import org.mini.gui.GCallBack;
import org.mini.gui.GFrame;


public class Frame extends Window implements MenuContainer {


    public Frame(String title) {
        peer = new GFrame(GCallBack.getInstance().getApplication().getForm(), title, 0, 0, 300, 200);
    }

    public synchronized void setIconImage(Image image) {
        if (peer != null) {
        }
    }
}
