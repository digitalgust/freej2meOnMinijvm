package java.awt;

import org.mini.gui.GTextObject;

public class TextComponent extends Component {

    public void setText(String t) {
        getPeer().setText(t);
    }

    public String getText() {
        return getPeer().getText();
    }

    public void setCaretPosition(int position) {
        ((GTextObject)getPeer()).setCaretIndex(position);
    }
}
