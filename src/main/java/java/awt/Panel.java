package java.awt;

import org.mini.gui.GCallBack;
import org.mini.gui.GObject;
import org.mini.gui.GPanel;

public class Panel extends Container {

    public Panel() {
        GObject peer = new GPanel(GCallBack.getInstance().getApplication().getForm(), 0, 0, 300, 200);
        setPeer(peer);
    }
}
