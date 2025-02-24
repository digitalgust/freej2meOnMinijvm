package java.awt;

import org.mini.gui.GButton;
import org.mini.gui.callback.GCallBack;
import org.mini.gui.GLabel;
import org.mini.gui.GObject;
import org.mini.gui.event.GActionListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.awt.event.ActionEvent.ACTION_PERFORMED;

public class Label extends Component {

    public Label() {
        this("");
    }

    public Label(String lab) {
        GObject p = new GLabel(GCallBack.getInstance().getApplication().getForm());
        setPeer(p);
        p.setText(lab);
    }

    public synchronized void addActionListener(ActionListener l) {
        if (l == null) {
            return;
        }
        ((GButton) getPeer()).setActionListener(new GActionListener() {
            @Override
            public void action(GObject gobj) {
                l.actionPerformed(new ActionEvent(Label.this, ACTION_PERFORMED, getPeer().getText()));
            }
        });
    }
}
