package java.awt;

public class TextComponent extends Component {

    public void setText(String t) {
        getPeer().setText(t);
    }

    public String getText() {
        return getPeer().getText();
    }
}
