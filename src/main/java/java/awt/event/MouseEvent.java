package java.awt.event;

public class MouseEvent {

    public static final int NOBUTTON = 0;

    public static final int BUTTON1 = 1;

    public static final int BUTTON2 = 2;

    public static final int BUTTON3 = 3;

    int x;

    int y;

    int clickCount;

    int button;


    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public int getButton() {
        return button;
    }

    public int getClickCount() {
        return clickCount;
    }
}
