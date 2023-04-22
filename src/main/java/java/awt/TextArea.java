package java.awt;

import org.mini.gui.GCallBack;
import org.mini.gui.GObject;
import org.mini.gui.GTextBox;

public class TextArea extends TextComponent {

    public TextArea() {
        this("");
    }
    public TextArea(String text) {
        GObject p = new GTextBox(GCallBack.getInstance().getApplication().getForm());
        setPeer(p);
        setText(text);
    }
}
