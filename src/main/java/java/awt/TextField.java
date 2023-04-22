package java.awt;

import org.mini.gui.GCallBack;
import org.mini.gui.GObject;
import org.mini.gui.GTextField;

public class TextField extends TextComponent {

    public TextField() {
        this("");
    }

    public TextField(String text) {
        GObject p = new GTextField(GCallBack.getInstance().getApplication().getForm());
        setPeer(p);
        setText(text);
    }
}
