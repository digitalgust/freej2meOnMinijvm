package java.awt;


import java.awt.event.WindowListener;

public class Window extends Container {
    transient WindowListener windowListener;

    public synchronized void addWindowListener(WindowListener l) {
    }

    public void pack() {

    }
}
