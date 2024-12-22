

package java.awt.event;

public abstract class KeyAdapter implements KeyListener {
    /**
     * Invoked when a key has been typed.
     * This event occurs when a key press is followed by a key release.
     */
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Invoked when a key has been pressed.
     */
    public void keyPressed(KeyEvent e) {
    }

    /**
     * Invoked when a key has been released.
     */
    public void keyReleased(KeyEvent e) {
    }
}
