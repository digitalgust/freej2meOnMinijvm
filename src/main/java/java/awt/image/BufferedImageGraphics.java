package java.awt.image;

import org.mini.gl.GLMath;
import org.mini.gui.GObject;

import java.awt.*;
import java.awt.geom.AffineTransform;

class BufferedImageGraphics extends Graphics2D {
    static final int CELL_BYTES = 4;

    //draw image
    BufferedImage bimg;
    byte[] bimgArr;
    int imgW, imgH;

    //draw string
    final static int FONT_MAX_DOT = 72;
    byte[] charBitmap = new byte[FONT_MAX_DOT * FONT_MAX_DOT];
    int[] widthAndHeight = {0, 0};

    public BufferedImageGraphics(GObject master, long context) {
        super(master, context);
    }

    public BufferedImageGraphics(BufferedImage bimg) {
        super(null, 0);
        this.bimg = bimg;
        bimgArr = bimg.getData().array();
        imgW = bimg.getWidth();
        imgH = bimg.getHeight();
        setClip(0, 0, imgW, imgH);
    }


    public void fillRect(int x, int y, int w, int h) {
        if (x + w < 0 || y + h < 0 || x > imgW || h > imgH) {
            return;
        }
        if (x + w > imgW) {
            w = imgW - x;
        }
        if (y + h > imgH) {
            h = imgH - y;
        }

        for (int i = y; i < y + h; i++) {
            GLMath.img_fill(bimgArr, x + i * imgW, w, curColor);
        }
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        fillRect(x, y, width, height);
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
                int ty = y1 + (y2 - y1) * i / (x2 - x1);
                GLMath.img_fill(bimgArr, ty * imgW + i, 1, curColor);
            }
        } else {
            for (int i = y1; i < y2; i++) {
                int tx = x1 + (x2 - x1) * i / (y2 - y1);
                GLMath.img_fill(bimgArr, i * imgW + tx, 1, curColor);
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
        drawRect(x, y, width, height);
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

        int w = font.getBitmapfont().stringWidth(str);
        int h = font.getBitmapfont().getHeight();

        if ((anchor & TOP) != 0) {
        } else if ((anchor & VCENTER) != 0) {
            y -= h / 2;
        } else if ((anchor & BASELINE) != 0) {
            y -= h;
        } else {//bottom
            y -= h;
        }

        if ((anchor & LEFT) != 0) {
        } else if ((anchor & HCENTER) != 0) {
            x -= w / 2;
        } else {//RIGHT
            x -= w;
        }


        int pos = 0;
        for (int i = offset; i < end; i++) {
            pos += font.getBitmapfont().drawChar(bimg, str.charAt(i), x + pos, y, curColor);
        }
    }

    public synchronized void drawChar(char character, int x, int y, int anchor) {
        int w = font.getBitmapfont().charWidth(character);
        int h = font.getBitmapfont().getHeight();

        if ((anchor & TOP) != 0) {
        } else if ((anchor & VCENTER) != 0) {
            y -= h / 2;
        } else if ((anchor & BASELINE) != 0) {
            y -= h;
        } else {//bottom
            y -= h;
        }

        if ((anchor & LEFT) != 0) {
        } else if ((anchor & HCENTER) != 0) {
            x -= w / 2;
        } else {//RIGHT
            x -= w;
        }
        font.getBitmapfont().drawChar(bimg, character, x, y, curColor);
    }

    public void drawChars(char[] data, int offset, int length, int x, int y, int anchor) {
        if (data == null) return;
        if (offset < 0) offset = 0;
        int end = offset + length;
        if (end > data.length) end = data.length;

        int w = font.getBitmapfont().charsWidth(data);
        int h = font.getBitmapfont().getHeight();

        if ((anchor & TOP) != 0) {
        } else if ((anchor & VCENTER) != 0) {
            y -= h / 2;
        } else if ((anchor & BASELINE) != 0) {
            y -= h;
        } else {//bottom
            y -= h;
        }

        if ((anchor & LEFT) != 0) {
        } else if ((anchor & HCENTER) != 0) {
            x -= w / 2;
        } else {//RIGHT
            x -= w;
        }


        int pos = 0;
        for (int i = offset; i < end; i++) {
            pos += font.getBitmapfont().drawChar(bimg, data[i], x + pos, y, curColor);
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

    @Override
    public boolean drawImage(Image img, AffineTransform transform,
                             ImageObserver observer) {
        if (img instanceof BufferedImage) {
            BufferedImage cimg = ((BufferedImage) img);
            int srcW = cimg.getWidth();
            byte[] dst = bimg.getData().array();
            byte[] src = cimg.getData().array();

            GLMath.img_draw(dst, imgW,
                    src, srcW,
                    clipX, clipY, clipW, clipH,
                    (float) transform.getScaleX(),
                    (float) transform.getShearX(),
                    (float) transform.getTranslateX(),
                    (float) transform.getShearY(),
                    (float) transform.getScaleY(),
                    (float) transform.getTranslateY(),
                    1.0f,
                    false, 0);
        } else {
            int debug = 1;
        }
        return true;
    }

    @Override
    public boolean drawImage(Image img, int x, int y,
                             ImageObserver observer) {
        if (img instanceof BufferedImage) {
            AffineTransform af = new AffineTransform();
            af.translate(x, y);
            drawImage(img, af, observer);
        }
        return true;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height,
                             ImageObserver observer) {
        if (img instanceof BufferedImage) {
            BufferedImage cimg = ((BufferedImage) img);
            AffineTransform af = new AffineTransform();
            af.translate(x, y);
            af.scale((double) width / cimg.getWidth(), (double) height / cimg.getHeight());
            drawImage(img, af, observer);
        }
        return true;
    }

}
