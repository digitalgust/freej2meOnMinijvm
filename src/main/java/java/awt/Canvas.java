package java.awt;

import org.mini.gui.GCallBack;
import org.mini.gui.GCanvas;

public class Canvas extends Component {

    public Canvas() {
        peer = new GCanvas(GCallBack.getInstance().getApplication().getForm());
    }
}
