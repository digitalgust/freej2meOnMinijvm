package java.util;

public class EventObject {
    protected transient Object  source;

    public EventObject(Object source) {
        if (source == null)
            throw new IllegalArgumentException("null source");

        this.source = source;
    }

    public Object getSource() {
        return source;
    }
}
