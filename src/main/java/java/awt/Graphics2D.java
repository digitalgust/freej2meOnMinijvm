package java.awt;

import org.mini.gui.GObject;

import java.awt.geom.AffineTransform;

public abstract class Graphics2D extends Graphics {
    protected AffineTransform transform = new AffineTransform();

    public Graphics2D(GObject master, long context) {
        super(master, context);
    }


    public AffineTransform getTransform() {
        return transform;
    }
}
