package java.awt;

import org.mini.gui.GCallBack;
import org.mini.gui.GFrame;
import org.mini.gui.GPanel;


public class Frame extends Window implements MenuContainer {


    public Frame(String title) {
//        peer = new GFrame(GCallBack.getInstance().getApplication().getForm(), title, 0, 0, 300, 200);
        peer = new GPanel(GCallBack.getInstance().getApplication().getForm(), 0, 0, 300, 200);
        peer.setName("LCD_FRAME");
    }

    public synchronized void setIconImage(Image image) {
        if (peer != null) {
        }
    }
}
