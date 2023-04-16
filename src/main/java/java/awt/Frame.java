package java.awt;

import org.mini.gui.GCallBack;
import org.mini.gui.GFrame;
import org.mini.gui.GObject;
import org.mini.gui.GPanel;


public class Frame extends Window implements MenuContainer {


    public Frame(String title) {
//        peer = new GFrame(GCallBack.getInstance().getApplication().getForm(), title, 0, 0, 300, 200);
        GObject peer = new GPanel(GCallBack.getInstance().getApplication().getForm(), 0, 0, 300, 200){

            public void keyEventGlfw(int key, int scanCode, int action, int mods) {
            }

            public void keyEventGlfm(int key, int action, int mods) {
            }
        };
        peer.setName(title);
        setPeer(peer);
    }

    public synchronized void setIconImage(Image image) {
        if (getPeer() != null) {
        }
    }
}
