package java.awt;

import org.mini.gui.GGraphics;
import org.mini.gui.GObject;
import org.mini.gui.ImageMutable;
import org.mini.nanovg.Nanovg;

import javax.microedition.lcdui.game.Layer;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;

public class Graphics extends GGraphics {
    protected Font font;
    protected FontMetrics fontMetrics;

    public Graphics(GObject master, long context) {
        super(master, context);
    }

    public boolean drawImage(Image img, AffineTransform transform,
                             ImageObserver observer) {
        drawImage(img.peer, (int) transform.getScaleX(), (int) transform.getScaleY(), Nanovg.NVG_ALIGN_LEFT | Nanovg.NVG_ALIGN_TOP);
        return true;
    }

    public boolean drawImage(Image img, int x, int y,
                             ImageObserver observer) {
        drawImage(img.peer, x, y, Nanovg.NVG_ALIGN_LEFT | Nanovg.NVG_ALIGN_TOP);
        return true;
    }

    public boolean drawImage(Image img, int x, int y, int width, int height,
                             ImageObserver observer) {
        drawImage(img.peer, x, y, width, height, Nanovg.NVG_ALIGN_LEFT | Nanovg.NVG_ALIGN_TOP);
        return true;
    }

    public void setFont(Font awtFont) {
        this.font = awtFont;
    }

    public Font getFont() {
        return font;
    }

    public FontMetrics getFontMetrics() {
        if (fontMetrics == null) {
            fontMetrics = new FontMetrics(font);
        }
        return fontMetrics;
    }


    public void drawString(String str, int x, int y) {
        drawString(str, x, y, GGraphics.LEFT | GGraphics.BASELINE);
    }

    public Rectangle getClipBounds(Rectangle r) {
        if (r == null) r = new Rectangle();
        r.x = super.getClipX();
        r.y = super.getClipY();
        r.width = super.getClipWidth();
        r.height = super.getClipHeight();
        return r;
    }


    public Rectangle getClipBounds() {
        Rectangle r = new Rectangle();
        return getClipBounds(r);
    }

    public void setColor(Color color) {
        setColor(color.getRed(), color.getGreen(), color.getBlue());
        setAlpha(color.getAlpha());
    }

    public void drawPolygon(int[] x, int[] y, int nPoints) {
    }

    public void fillPolygon(int[] x, int[] y, int nPoints) {
    }

    public void clearRect(int x, int y, int width, int height) {
    }

    public void setBackground(Color color) {
    }
}
