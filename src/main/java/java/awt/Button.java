package java.awt;

import org.mini.gui.GButton;
import org.mini.gui.GCallBack;
import org.mini.gui.GObject;
import org.mini.gui.event.GActionListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.awt.event.ActionEvent.ACTION_PERFORMED;

public class Button extends Component {
    java.util.List<ActionListener> actionListeners = new java.util.ArrayList<>();

    public Button() {
        this("");
    }

    public Button(String lab) {
        GObject p = new GButton(GCallBack.getInstance().getApplication().getForm());
        setPeer(p);
        p.setText(lab);
    }

    public synchronized void addActionListener(ActionListener l) {
        if (l == null) {
            return;
        }
        actionListeners.add(l);
        ((GButton) getPeer()).setActionListener(new GActionListener() {
            @Override
            public void action(GObject gobj) {
                ActionEvent e = new ActionEvent(Button.this, ACTION_PERFORMED, getPeer().getText());
                for (ActionListener listener : actionListeners) {
                    listener.actionPerformed(e);
                }
            }
        });
    }

    public synchronized void removeActionListener(ActionListener l) {
        if (l == null) {
            return;
        }
        ((GButton) getPeer()).setActionListener(null);
    }
}
