package java.awt.event;

import java.util.EventListener;

public interface WindowStateListener extends EventListener {
    /**
     * Invoked when window state is changed.
     */
    public void windowStateChanged(WindowEvent e);
}
