package java.awt.image;

import org.mini.gui.GObject;
import org.mini.reflect.ReflectArray;
import org.mini.reflect.vm.RefNative;

import java.awt.*;

class BufferedImageGraphics extends Graphics2D {
    static final int CELL_BYTES = 4;

    BufferedImage img;
    long imgArrAddr;
    byte[] int2byte = {0, 0, 0, 0};
    long byteAddr;
    int imgW, imgH;

    public BufferedImageGraphics(GObject master, long context) {
        super(master, context);
    }

    public BufferedImageGraphics(BufferedImage img) {
        super(null, 0);
        this.img = img;
        imgArrAddr = ReflectArray.getBodyPtr(img.getData().array());
        byteAddr = ReflectArray.getBodyPtr(int2byte);
        imgW = img.getWidth();
        imgH = img.getHeight();
    }


    public void fillRect(int x, int y, int w, int h) {
        if (x + w < 0 || y + h < 0 || x >= w || h >= h) {
            return;
        }
        if (x + w > imgW) {
            w = imgW - x;
        }
        if (y + h > imgH) {
            h = imgH - y;
        }

        int2byte[0] = b;
        int2byte[1] = g;
        int2byte[2] = r;
        int2byte[3] = a;

        for (int i = y; i < y + h; i++) {
            RefNative.heap_fill(imgArrAddr + (i * w + x) * CELL_BYTES, w * CELL_BYTES, byteAddr, CELL_BYTES);
        }
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {

    }

    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    }


    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {

    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        if (x1 < 0) x1 = 0;
        if (x1 > imgW) x1 = imgW;
        if (x2 < 0) x2 = 0;
        if (x2 > imgW) x2 = imgW;
        if (y1 < 0) y1 = 0;
        if (y1 > imgH) y1 = imgW;
        if (y2 < 0) y2 = 0;
        if (y2 > imgH) y2 = imgW;

        if (y1 > y2) {
            int i = y2;
            y2 = y1;
            y1 = i;
        }
        if (x1 > x2) {
            int i = x2;
            x2 = x1;
            x1 = i;
        }
        int dy = y2 - y1;
        int dx = x2 - x1;
        if (dx > dy) {
            for (int i = x1; i < x2; i++) {
                int ty = (y2 - y1) * i / (x2 - x1);
                img.getImage().setPix(i, ty, curColor);
            }
        } else {
            for (int i = y1; i < y2; i++) {
                int tx = (x2 - x1) * i / (y2 - y1);
                img.getImage().setPix(tx, i, curColor);
            }
        }
    }

    public void drawRect(int x, int y, int w, int h) {
        drawLine(x, y, x + w, y);
        drawLine(x, y + h, x + w, y + h);
        drawLine(x, y, x, y + h);
        drawLine(x + w, y, x + w, y + h);
    }

    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {

    }


    public void drawString(String str, int x, int y, int anchor) {
        if (str == null) return;
        drawSubstring(str, 0, str.length(), x, y, anchor);
    }

    public void drawSubstring(String str, int offset, int length, int x, int y, int anchor) {
        if (str == null) return;
        if (offset < 0) offset = 0;
        int end = offset + length;
        if (end > str.length()) end = str.length();

        int size = font.getSize();
        for (int i = offset; i < end; i++) {
            drawChar(str.charAt(i), x + size * i, y, anchor);
        }
    }

    public void drawChar(char character, int x, int y, int anchor) {
        int size = font.getSize();
        drawRect(x, y, size, size);
    }

    public void drawChars(char[] data, int offset, int length, int x, int y, int anchor) {
        if (data == null) return;
        if (offset < 0) offset = 0;
        int end = offset + length;
        if (end > data.length) end = data.length;

        int size = font.getSize();
        for (int i = offset; i < end; i++) {
            drawChar(data[i], x + size * i, y, anchor);
        }
    }

    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        drawLine(x1, y1, x2, y2);
        drawLine(x1, y1, x3, y3);
        drawLine(x2, y2, x3, y3);
    }

    public void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
    }

    public void drawImage(Image img, int x, int y, int anchor) {
        if (img == null) return;
        drawImage(img, x, y, img.getWidth(null), img.getHeight(null), anchor);
    }

    public void drawImage(Image img, int x, int y, int w, int h, int anchor) {
        if (img == null) return;
    }

    public void drawRegion(Image src, int x_src, int y_src, int width, int height, int transform, int x_dest, int y_dest, int anchor) {
        if (src == null || width == 0 || height == 0) {
            return;
        }
        final int imgw = src.getWidth(null);
        final int imgh = src.getHeight(null);
        if (x_src < 0 || y_src < 0 || x_src + width > imgw || y_src + height > imgh) {
            return;
        }

        x_dest += 0;
        y_dest += 0;


        float clipX, clipY, clipW, clipH;
        clipX = x_dest;
        clipY = y_dest;
        clipW = width;
        clipH = height;
        float ix = 0f, iy = 0f, iw = imgw;
        float px = 0f, py = 0f, pw = 0f, ph = 0f;
        float rot = 0f;
        switch (transform) {
            case TRANS_NONE:
                px = x_dest - x_src;
                py = y_dest - y_src;
                pw = imgw;
                ph = imgh;
                ix = px;
                iy = py;
                clipW = width;
                clipH = height;
                break;
            case TRANS_ROT90:
                px = x_dest - (imgh - y_src - height);
                py = y_dest - x_src;
                pw = imgh;
                ph = imgw;
                rot = 90f;
                ix = px + imgh;
                iy = py;
                clipW = height;
                clipH = width;
                break;
            case TRANS_ROT180:
                px = x_dest - (imgw - x_src - width);
                py = y_dest - (imgh - y_src - height);
                pw = imgw;
                ph = imgh;
                rot = 180f;
                ix = px + imgw;
                iy = py + imgh;
                clipW = width;
                clipH = height;
                break;
            case TRANS_ROT270:
                px = x_dest - y_src;
                py = y_dest - (imgw - x_src - width);
                pw = imgh;
                ph = imgw;
                rot = 270f;
                ix = px;
                iy = py + imgw;
                clipW = height;
                clipH = width;
                break;
            case TRANS_MIRROR:
                px = x_dest - (imgw - x_src - width);
                py = y_dest - y_src;
                pw = imgw;
                ph = imgh;
                ix = px + imgw;
                iy = py;
                iw = -imgw;
                clipW = width;
                clipH = height;
                break;
            case TRANS_MIRROR_ROT90:
                px = x_dest - (imgh - y_src - height);
                py = y_dest - (imgw - x_src - width);
                pw = imgh;
                ph = imgw;
                rot = 90f;
                ix = px + imgh;
                iy = py + imgw;
                iw = -imgw;
                clipW = height;
                clipH = width;
                break;
            case TRANS_MIRROR_ROT180:
                px = x_dest - x_src;
                py = y_dest - (imgh - y_src - height);
                pw = imgw;
                ph = imgh;
                rot = 180f;
                iw = -imgw;
                ix = px;
                iy = py + imgh;
                clipW = width;
                clipH = height;
                break;
            case TRANS_MIRROR_ROT270:
                px = x_dest - y_src;
                py = y_dest - x_src;
                pw = imgh;
                ph = imgw;
                rot = 270f;
                iw = -imgw;
                ix = px;
                iy = py;
                clipW = height;
                clipH = width;
                break;
            default:
                throw new IllegalArgumentException("IllegalArgumentException");
        }

        if ((anchor & RIGHT) != 0) {
            px -= clipW;
            ix -= clipW;
            clipX -= clipW;
        } else if ((anchor & HCENTER) != 0) {
            px -= clipW / 2;
            ix -= clipW / 2;
            clipX -= clipW / 2;
        }
        if ((anchor & BOTTOM) != 0) {
            py -= clipH;
            iy -= clipH;
            clipY -= clipH;
        } else if ((anchor & VCENTER) != 0) {
            py -= clipH / 2;
            iy -= clipH / 2;
            clipY -= clipH / 2;
        }


    }


    /**
     * Notice: the rgbData is ABGR format
     * IMPORTANT : This mehod maybe copy large mount of data when the area is big, so it's slow sometimes.
     *
     * @param rgbData
     * @param offset
     * @param scanlength
     * @param x
     * @param y
     * @param width
     * @param height
     * @param processAlpha
     */
    public void drawRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height, boolean processAlpha) {
        //img.setPix(rgbData, offset, scanlength, 0, 0, width, height);
    }

    public int getColor() {
        return curColor;
    }

    /**
     * not implementation
     *
     * @param x_src
     * @param y_src
     * @param width
     * @param height
     * @param x_dest
     * @param y_dest
     * @param anchor
     */
    public void copyArea(int x_src, int y_src, int width, int height, int x_dest, int y_dest, int anchor) {
    }

    public void clipRect(int x, int y, int width, int height) {
        clipX = clipX > x ? clipX : x;
        clipY = clipY > x ? clipY : y;
        clipW = clipX + clipW < clipX + width ? clipW : width;
        clipH = clipY + clipH < clipY + height ? clipH : height;
        setClip(clipX, clipY, clipW, clipH);
    }

    public void translate(int x, int y) {
    }

    public void setClip(int x, int y, int w, int h) {
        clipX = x;
        clipY = y;
        clipW = w;
        clipH = h;
    }

    public int getClipX() {
        return clipX;
    }

    public int getClipY() {
        return clipY;
    }

    public int getClipWidth() {
        return clipW;
    }

    public int getClipHeight() {
        return clipH;
    }

    public void setStrokeStyle(int style) {
        strokeStyle = style;
    }

    public int getStrokeStyle() {
        return strokeStyle;
    }

    public void setGrayScale(int value) {

    }
}
