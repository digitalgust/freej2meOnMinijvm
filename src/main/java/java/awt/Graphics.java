package java.awt;

import org.mini.gui.GGraphics;
import org.mini.gui.GObject;
import org.mini.nanovg.Nanovg;

import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;

public abstract class Graphics {
    protected int clipX;
    protected int clipY;
    protected int clipW;
    protected int clipH;
    protected int transX, transY;

    protected Color awtColor;
    protected Color backgroundColor;

    GGraphics gPeer;
    protected Font font;
    protected FontMetrics fontMetrics;

    public Graphics(GObject master, long context) {
        gPeer = new GGraphics(master, context);
    }

    public boolean drawImage(Image img, AffineTransform transform,
                             ImageObserver observer) {
        gPeer.drawImage(img.peer, (int) transform.getScaleX(), (int) transform.getScaleY(), Nanovg.NVG_ALIGN_LEFT | Nanovg.NVG_ALIGN_TOP);
        return true;
    }

    public boolean drawImage(Image img, int x, int y,
                             ImageObserver observer) {
        gPeer.drawImage(img.peer, x, y, Nanovg.NVG_ALIGN_LEFT | Nanovg.NVG_ALIGN_TOP);
        return true;
    }

    public boolean drawImage(Image img, int x, int y, int width, int height,
                             ImageObserver observer) {
        gPeer.drawImage(img.peer, x, y, width, height, Nanovg.NVG_ALIGN_LEFT | Nanovg.NVG_ALIGN_TOP);
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


    public void drawChars(char[] data, int offset, int length, int x, int y) {
        drawString(new String(data, offset, length), x, y);
    }

    public void drawString(String str, int x, int y) {
        x += transX;
        y += transY;
        gPeer.drawString(str, x, y, GGraphics.LEFT | GGraphics.BASELINE);
    }

    public void setColor(Color color) {
        if (color == null) return;
        awtColor = color;

        gPeer.setColor(color.getRed(), color.getGreen(), color.getBlue());
        gPeer.setAlpha(color.getAlpha());
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        x1 += transX;
        y1 += transY;
        x2 += transX;
        y2 += transY;
        gPeer.drawLine(x1, y1, x2, y2);
    }

    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        x += transX;
        y += transY;
        gPeer.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    public void drawRect(int x, int y, int w, int h) {
        x += transX;
        y += transY;
        gPeer.drawRect(x, y, w, h);
    }

    public void drawArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
        x += transX;
        y += transY;
        gPeer.drawArc(x, y, w, h, startAngle, arcAngle);
    }

    public void fillArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
        x += transX;
        y += transY;
        gPeer.fillArc(x, y, w, h, startAngle, arcAngle);
    }

    public void fillOval(int x, int y, int width, int height) {
        fillArc(x, y, width, height, 0, 360);
    }


    public void fillRect(int x, int y, int w, int h) {
        x += transX;
        y += transY;
        gPeer.fillRect(x, y, w, h);
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        x += transX;
        y += transY;
        gPeer.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    public void drawPolygon(int[] x, int[] y, int nPoints) {
        throw new RuntimeException("not implementation yet.");
    }

    public void fillPolygon(int[] x, int[] y, int nPoints) {
        throw new RuntimeException("not implementation yet.");
    }

    public void clearRect(int x, int y, int width, int height) {
        x += transX;
        y += transY;
        int c = gPeer.getColor();
        gPeer.setColor(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue());
        gPeer.fillRect(x, y, width, height);
        gPeer.setColor(c);
    }

    public void setBackground(Color color) {
        backgroundColor = color;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public synchronized void setClip(int x, int y, int w, int h) {
        if ((w <= 0) || (h <= 0)) {
            clipX = clipY = clipW = clipH = 0;
            return;
        }
        int imgW = (int) getPeer().getMaster().getW();
        int imgH = (int) getPeer().getMaster().getH();
        int x1 = x + transX;
        int y1 = y + transY;
        int x2 = x1 + w;
        int y2 = y1 + h;

        if (x1 < 0) x1 = 0;
        if (y1 < 0) y1 = 0;
        if (x1 > imgW) x1 = imgW;
        if (y1 > imgH) y1 = imgH;
        if (x2 < 0) x2 = 0;
        if (y2 < 0) y2 = 0;
        if (x2 > imgW) x2 = imgW;
        if (y2 > imgH) y2 = imgH;

        clipX = x1;
        clipY = y1;
        clipW = x2 - x1;
        clipH = y2 - y1;
    }


    public synchronized void clipRect(int x, int y, int w, int h) {
        if ((w <= 0) || (h <= 0)) {
            clipW = clipH = 0;
            return;
        }
        int x1 = x + transX;
        int y1 = y + transY;
        int x2 = x1 + w;
        int y2 = y1 + h;

        int cx1 = clipX;
        int cy1 = clipY;

        int cx2 = clipX + clipW;
        int cy2 = clipY + clipH;

        if (cx1 > x2 || cy1 > y2 || cx2 < x1 || cy2 < y1) {
            clipW = clipH = 0;
            return;
        }

        cx1 = cx1 > x1 ? cx1 : x1;
        cy1 = cy1 > y1 ? cy1 : y1;

        cx2 = cx2 > x2 ? x2 : cx2;
        cy2 = cy2 > y2 ? y2 : cy2;

        int cw = cx2 - cx1;
        int ch = cy2 - cy1;

        setClip(cx1 - transX, cy1 - transY, cw, ch);

    }


    public int getClipX() {
        return clipX - transX;
    }

    public int getClipY() {
        return clipY - transY;
    }

    public int getClipWidth() {
        return clipW;
    }

    public int getClipHeight() {
        return clipH;
    }

    public void setClip(Rectangle clip) {
        setClip(clip.x, clip.y, clip.width, clip.height);
    }

    public void setClip(Shape clip) {
        setClip(clip.getBounds());
    }

    public Rectangle getClipBounds(Rectangle r) {
        if (r == null) r = new Rectangle();
        r.x = getClipX();
        r.y = getClipY();
        r.width = getClipWidth();
        r.height = getClipHeight();
        return r;
    }


    public Rectangle getClipBounds() {
        Rectangle r = new Rectangle();
        return getClipBounds(r);
    }

    public synchronized void translate(int x, int y) {
        transX += x;
        transY += y;
    }

    public int getTranslateX() {
        return transX;
    }

    public int getTranslateY() {
        return transY;
    }

    public Color getColor() {
        return awtColor;
    }

    public void setStroke(Stroke style) {
    }

    public Stroke getStroke() {
        return null;
    }


    protected GGraphics getPeer() {
        return gPeer;
    }

}
