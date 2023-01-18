package java.awt.event;

import java.util.EventListener;

public interface ComponentListener extends EventListener {
    /**
     * Invoked when the component's size changes.
     */
    public void componentResized(ComponentEvent e);

    /**
     * Invoked when the component's position changes.
     */
    public void componentMoved(ComponentEvent e);

    /**
     * Invoked when the component has been made visible.
     */
    public void componentShown(ComponentEvent e);

    /**
     * Invoked when the component has been made invisible.
     */
    public void componentHidden(ComponentEvent e);
}

