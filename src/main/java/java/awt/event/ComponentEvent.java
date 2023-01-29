package java.awt.event;

import java.awt.*;
import java.awt.AWTEvent;
import java.lang.annotation.Native;

public class ComponentEvent extends AWTEvent {

    /**
     * The first number in the range of ids used for component events.
     */
    public static final int COMPONENT_FIRST = 100;

    /**
     * The last number in the range of ids used for component events.
     */
    public static final int COMPONENT_LAST = 103;

    /**
     * This event indicates that the component's position changed.
     */
    @Native
    public static final int COMPONENT_MOVED = COMPONENT_FIRST;

    /**
     * This event indicates that the component's size changed.
     */
    @Native
    public static final int COMPONENT_RESIZED = 1 + COMPONENT_FIRST;

    /**
     * This event indicates that the component was made visible.
     */
    @Native
    public static final int COMPONENT_SHOWN = 2 + COMPONENT_FIRST;

    /**
     * This event indicates that the component was rendered invisible.
     */
    @Native
    public static final int COMPONENT_HIDDEN = 3 + COMPONENT_FIRST;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = 8101406823902992965L;

    /**
     * Constructs a <code>ComponentEvent</code> object.
     * <p> This method throws an
     * <code>IllegalArgumentException</code> if <code>source</code>
     * is <code>null</code>.
     *
     * @param source The <code>Component</code> that originated the event
     * @param id     An integer indicating the type of event.
     *               For information on allowable values, see
     *               the class description for {@link ComponentEvent}
     * @throws IllegalArgumentException if <code>source</code> is null
     * @see #getComponent()
     * @see #getID()
     */
    public ComponentEvent(Component source, int id) {
        super(source, id);
    }

    /**
     * Returns the originator of the event.
     *
     * @return the <code>Component</code> object that originated
     * the event, or <code>null</code> if the object is not a
     * <code>Component</code>.
     */
    public Component getComponent() {
        return (source instanceof Component) ? (Component) source : null;
    }
}
