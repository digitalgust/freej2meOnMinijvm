package java.awt;

import org.mini.gui.GImage;

import java.awt.image.*;

public abstract class Image {
    GImage peer;

    public abstract int getWidth(ImageObserver observer);

    public abstract int getHeight(ImageObserver observer);

    public abstract ImageProducer getSource();

    public abstract Graphics getGraphics();

    public abstract Object getProperty(String name, ImageObserver observer);

    public static final Object UndefinedProperty = new Object();

    public Image getScaledInstance(int width, int height, int hints) {

        //return Toolkit.getDefaultToolkit().createImage(prod);
        return null;
    }

    public static final int SCALE_DEFAULT = 1;

    public static final int SCALE_FAST = 2;

    public static final int SCALE_SMOOTH = 4;

    public static final int SCALE_REPLICATE = 8;

    public static final int SCALE_AREA_AVERAGING = 16;

    public abstract void flush();
}
