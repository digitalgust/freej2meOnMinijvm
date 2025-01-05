package java.awt.image;

import org.mini.gl.GLMath;
import org.mini.gui.GGraphics;
import org.mini.gui.GObject;

import java.awt.*;
import java.awt.geom.AffineTransform;

class BufferedImageGraphics extends Graphics2D {
    static final int CELL_BYTES = 4;

    //draw image
    BufferedImage bimg;
    int imgW, imgH;

    int curColor;

    static ThreadLocal<AffineTransform> transform = new ThreadLocal() {
        protected AffineTransform initialValue() {
            return new AffineTransform();
        }
    };
    static ThreadLocal<java.awt.Point> tmpPoint = new ThreadLocal() {
        protected Point initialValue() {
            return new java.awt.Point();
        }
    };
    static ThreadLocal<IntList> cachedPoints = new ThreadLocal() {
        protected IntList initialValue() {
            return new IntList();
        }
    };


    public BufferedImageGraphics(GObject master, long context) {
        super(master, context);
    }

    public BufferedImageGraphics(BufferedImage bimg) {
        super(null, 0);
        this.bimg = bimg;
        imgW = bimg.getWidth();
        imgH = bimg.getHeight();
        setClip(0, 0, imgW, imgH);
        transX = transY = 0;
    }

    public synchronized void fillRect(int x, int y, int w, int h) {
        x += transX;
        y += transY;
        if (x + w < 0 || y + h < 0 || x > imgW || y > imgH) {
            return;
        }
        int cx1 = clipX > x ? clipX : x;
        int cy1 = clipY > y ? clipY : y;
        int cx2 = clipX + clipW > x + w ? x + w : clipX + clipW;
        int cy2 = clipY + clipH > y + h ? y + h : clipY + clipH;
        int cw = cx2 - cx1;
        int ch = cy2 - cy1;

        for (int i = cy1; i < cy2; i++) {
            GLMath.img_fill(bimg.getData().array(), cx1 + i * imgW, cw, curColor);
        }
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        fillRect(x, y, width, height);
    }

    public synchronized void fillArc(int left, int top, int width, int height, int startAngle, int arcAngle) {
        startAngle = ((startAngle % 360) + 360) % 360;
        arcAngle = arcAngle > 360 ? 360 : arcAngle;// range: 0-360
        arcAngle = arcAngle < -360 ? -360 : arcAngle;
        if (arcAngle < 0) {
            int tmp = startAngle + arcAngle;
            startAngle = (tmp + 360) % 360;
            arcAngle = -arcAngle;
        }
        int endAngle = startAngle + arcAngle;
        if (endAngle > 360) {
            //分两段画
            drawArcImpl(left, top, width, height, startAngle, 360, true);
            drawArcImpl(left, top, width, height, 0, endAngle % 360, true);
        } else {
            drawArcImpl(left, top, width, height, startAngle, arcAngle, true);
        }
    }


    /**
     * OpenAI generate
     *
     * @param left
     * @param top
     * @param width
     * @param height
     * @param startAngle
     * @param arcAngle
     */
    public synchronized void drawArc(int left, int top, int width, int height, int startAngle, int arcAngle) {
        startAngle = ((startAngle % 360) + 360) % 360;
        arcAngle = arcAngle > 360 ? 360 : arcAngle;// range: 0-360
        arcAngle = arcAngle < -360 ? -360 : arcAngle;
        if (arcAngle < 0) {
            int tmp = startAngle + arcAngle;
            startAngle = (tmp + 360) % 360;
            arcAngle = -arcAngle;
        }
        int endAngle = startAngle + arcAngle;
        if (endAngle > 360) {
            //分两段画
            drawArcImpl(left, top, width, height, startAngle, 360, false);
            drawArcImpl(left, top, width, height, 0, endAngle % 360, false);
        } else {
            drawArcImpl(left, top, width, height, startAngle, arcAngle, false);
        }
    }

    private void drawArcImpl(int left, int top, int width, int height, int startAngle, int arcAngle, boolean fill) {
        int endAngle = startAngle + arcAngle;

        int a = width / 2;
        int b = height / 2;
        int x0 = left + width / 2;
        int y0 = top + height / 2;
        int x = 0, y = b;
        int d1 = b * b - a * a * b + a * a / 4;
        int dx = 2 * b * b * x;
        int dy = 2 * a * a * y;
        float startRadian = (float) Math.toRadians(startAngle);// 起始角度
        float endRadian = (float) Math.toRadians(endAngle); // 终止角度
        while (dx < dy) {
            drawPixImpl(x0, y0, x, y, startRadian, endRadian, fill);
            if (d1 < 0) {
                x++;
                dx += 2 * b * b;
                d1 += dx + b * b;
            } else {
                x++;
                y--;
                dx += 2 * b * b;
                dy -= 2 * a * a;
                d1 += dx - dy + b * b;
            }
        }
        int d2 = (int) (b * b * (x + 0.5) * (x + 0.5) + a * a * (y - 1) * (y - 1) - a * a * b * b);
        while (y >= 0) {
            drawPixImpl(x0, y0, x, y, startRadian, endRadian, fill);

            if (d2 > 0) {
                y--;
                dy -= 2 * a * a;
                d2 += a * a - dy;
            } else {
                y--;
                x++;
                dx += 2 * b * b;
                dy -= 2 * a * a;
                d2 += dx - dy + a * a;
            }
        }
    }

    private void drawPixImpl(int x0, int y0, int x, int y, float startRadian, float endRadian, boolean fill) {
        float radian;
        radian = (float) Math.atan2(y, x);
        if (radian >= startRadian && radian <= endRadian) {
            drawLine(x0 + x, y0 - y, fill ? x0 : (x0 + x), fill ? y0 : (y0 - y));
        }
        radian = (float) Math.atan2(y, -x);
        if (radian >= startRadian && radian <= endRadian) {
            drawLine(x0 - x, y0 - y, fill ? x0 : (x0 - x), fill ? y0 : (y0 - y));
        }
        radian = (float) (Math.atan2(-y, x) + Math.PI * 2);
        if (radian >= startRadian && radian <= endRadian) {
            drawLine(x0 + x, y0 + y, fill ? x0 : (x0 + x), fill ? y0 : (y0 + y));
        }
        radian = (float) (Math.atan2(-y, -x) + Math.PI * 2);
        if (radian >= startRadian && radian <= endRadian) {
            drawLine(x0 - x, y0 + y, fill ? x0 : (x0 - x), fill ? y0 : (y0 + y));
        }
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        x1 += transX;
        y1 += transY;
        x2 += transX;
        y2 += transY;
        if (x1 == x2) {// 垂直
            if (x1 < clipX || x1 > clipX + clipW) {
                return;
            }
            if (y1 > y2) {
                int tmp = y1;
                y1 = y2;
                y2 = tmp;
            }
            if (y1 < clipY) {
                y1 = clipY;
            }
            if (y2 > clipY + clipH) {
                y2 = clipY + clipH;
            }
            for (int i = y1; i <= y2; i++) {
                GLMath.img_fill(bimg.getData().array(), i * bimg.getWidth() + x1, 1, curColor);
            }
        } else if (y1 == y2) { // 水平
            if (y1 < clipY || y1 > clipY + clipH) {
                return;
            }
            if (x1 > x2) {
                int tmp = x1;
                x1 = x2;
                x2 = tmp;
            }
            if (x1 < clipX) {
                x1 = clipX;
            }
            if (x2 > clipX + clipW) {
                x2 = clipX + clipW;
            }
            GLMath.img_fill(bimg.getData().array(), y1 * bimg.getWidth() + x1, x2 - x1 + 1, curColor);
        } else {// 斜线
            int dy = Math.abs(y2 - y1);
            int dx = Math.abs(x2 - x1);
            int cx2 = clipX + clipW;
            int cy2 = clipY + clipH;
            if (dx > dy) {
                int sx, ex;

                if (x1 > x2) {
                    sx = x2;
                    ex = x1;
                } else {
                    sx = x1;
                    ex = x2;
                }
                if (sx < clipX) {
                    sx = clipX;
                }
                if (ex > cx2) {
                    ex = cx2;
                }
                for (int i = sx; i <= ex; i++) {
                    int ty = (y1 + ((y2 - y1) * (i - x1) / (x2 - x1)));
                    if (ty < clipY || ty > cy2) continue;
                    GLMath.img_fill(bimg.getData().array(), ty * imgW + i, 1, curColor);
                }
            } else {
                int sy, ey;
                if (y1 > y2) {
                    sy = y2;
                    ey = y1;
                } else {
                    sy = y1;
                    ey = y2;
                }
                if (sy < clipY) {
                    sy = clipY;
                }
                if (ey > cy2) {
                    ey = cy2;
                }
                for (int i = sy; i <= ey; i++) {
                    int tx = (x1 + ((x2 - x1) * (i - y1) / (y2 - y1)));
                    if (tx < clipX || tx > cx2) continue;
                    GLMath.img_fill(bimg.getData().array(), i * imgW + tx, 1, curColor);
                }
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

    public void drawString(String str, int x, int y) {
        drawString(str, x, y, GGraphics.BASELINE | GGraphics.LEFT);
    }

    public void drawChars(char[] data, int offset, int length, int x, int y) {
        drawChars(data, offset, length, x, y, GGraphics.BASELINE | GGraphics.LEFT);
    }

    private void drawString(String str, int x, int y, int anchor) {
        if (str == null) return;
        drawSubstring(str, 0, str.length(), x, y, anchor);
    }

    private void drawSubstring(String str, int offset, int length, int x, int y, int anchor) {
        x += transX;
        y += transY;
        if (str == null) return;
        if (offset < 0) offset = 0;
        int end = offset + length;
        if (end > str.length()) end = str.length();

        int w = font.getBitmapfont().stringWidth(str);
        int h = font.getBitmapfont().getHeight();

        if ((anchor & GGraphics.TOP) != 0) {
        } else if ((anchor & GGraphics.VCENTER) != 0) {
            y -= h / 2;
        } else if ((anchor & GGraphics.BASELINE) != 0) {
            y -= h;
        } else {//bottom
            y -= h;
        }

        if ((anchor & GGraphics.LEFT) != 0) {
        } else if ((anchor & GGraphics.HCENTER) != 0) {
            x -= w / 2;
        } else {//RIGHT
            x -= w;
        }


        int pos = 0;
        for (int i = offset; i < end; i++) {
            pos += font.getBitmapfont().drawChar(bimg, str.charAt(i), x + pos, y, curColor, clipX, clipY, clipW, clipH);
        }
    }

    private synchronized void drawChar(char character, int x, int y, int anchor) {
        x += transX;
        y += transY;
        int w = font.getBitmapfont().charWidth(character);
        int h = font.getBitmapfont().getHeight();

        if ((anchor & GGraphics.TOP) != 0) {
        } else if ((anchor & GGraphics.VCENTER) != 0) {
            y -= h / 2;
        } else if ((anchor & GGraphics.BASELINE) != 0) {
            y -= h;
        } else {//bottom
            y -= h;
        }

        if ((anchor & GGraphics.LEFT) != 0) {
        } else if ((anchor & GGraphics.HCENTER) != 0) {
            x -= w / 2;
        } else {//RIGHT
            x -= w;
        }
        font.getBitmapfont().drawChar(bimg, character, x, y, curColor, clipX, clipY, clipW, clipH);
    }

    public synchronized void drawChars(char[] data, int offset, int length, int x, int y, int anchor) {
        x += transX;
        y += transY;
        if (data == null) return;
        if (offset < 0) offset = 0;
        int end = offset + length;
        if (end > data.length) end = data.length;

        int w = font.getBitmapfont().charsWidth(data);
        int h = font.getBitmapfont().getHeight();

        if ((anchor & GGraphics.TOP) != 0) {
        } else if ((anchor & GGraphics.VCENTER) != 0) {
            y -= h / 2;
        } else if ((anchor & GGraphics.BASELINE) != 0) {
            y -= h;
        } else {//bottom
            y -= h;
        }

        if ((anchor & GGraphics.LEFT) != 0) {
        } else if ((anchor & GGraphics.HCENTER) != 0) {
            x -= w / 2;
        } else {//RIGHT
            x -= w;
        }


        int pos = 0;
        for (int i = offset; i < end; i++) {
            pos += font.getBitmapfont().drawChar(bimg, data[i], x + pos, y, curColor, clipX, clipY, clipW, clipH);
        }
    }

    public void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        drawLine(x1, y1, x2, y2);
        drawLine(x1, y1, x3, y3);
        drawLine(x2, y2, x3, y3);
    }

    static ThreadLocal<int[][]> triangle = new ThreadLocal() {
        public int[][] initialValue() {
            int[][] t = new int[2][3];
            return t;
        }
    };

    /**
     * 画平顶实心三角形
     */
    private void fillTopLineTriangle(int x0, int y0, int x1, int y1, int x2, int y2) {
        int ys = y0;
        if (y0 < clipY) {
            ys = clipY;
        }
        int ye = y2;
        if (y2 > clipY + clipH) {
            ye = clipY + clipH;
        }
        for (int y = ys; y <= ye; y++) {
            int xs, xe;
            xs = (int) ((y - y0) * (x2 - x0) / (y2 - y0) + x0 + 0.5f);
            xe = (int) ((y - y1) * (x2 - x1) / (y2 - y1) + x1 + 0.5f);
            if (xs > xe) {
                int tmp = xs;
                xs = xe;
                xe = tmp;
            }
            if (xe < clipX || xs >= clipX + clipW) {
                continue;
            }
            xe += 1;
            //drawLine(xs, y, xe, y);
            if (xs < clipX) {
                xs = clipX;
            }
            if (xe > clipX + clipW) {
                xe = clipX + clipW;
            }
            GLMath.img_fill(bimg.getData().array(), y * bimg.getWidth() + xs, xe - xs, curColor);
        }
    }

    /**
     * 画平底实心三角形
     */
    private void fillBottomLineTriangle(int x0, int y0, int x1, int y1, int x2, int y2) {
        int ys = y0;
        if (y0 < clipY) {
            ys = clipY;
        }
        int ye = y1;
        if (y1 > clipY + clipH) {
            ye = clipY + clipH;
        }
        for (int y = ys; y <= ye; y++) {
            int xs, xe;
            xs = (int) ((y - y0) * (x1 - x0) / (y1 - y0) + x0 + 0.5f);
            xe = (int) ((y - y0) * (x2 - x0) / (y2 - y0) + x0 + 0.5f);

            if (xs > xe) {
                int tmp = xs;
                xs = xe;
                xe = tmp;
            }
            if (xe < clipX || xs >= clipX + clipW) {
                continue;
            }
            xe += 1;
            //drawLine(xs, y, xe, y);
            if (xs < clipX) {
                xs = clipX;
            }
            if (xe > clipX + clipW) {
                xe = clipX + clipW;
            }
            GLMath.img_fill(bimg.getData().array(), y * bimg.getWidth() + xs, xe - xs, curColor);

        }
    }

    public void fillTriangle(int x0, int y0, int x1, int y1, int x2, int y2) {
        if (y0 == y1) {
            fillTopLineTriangle(x0, y0, x1, y1, x2, y2);
        } else if (y1 == y2) {
            fillBottomLineTriangle(x0, y0, x1, y1, x2, y2);
        } else {
            int xtop = 0, ytop = 0, xmiddle = 0, ymiddle = 0, xbottom = 0, ybottom = 0;
            xtop = x0;
            ytop = y0;
            xmiddle = x1;
            ymiddle = y1;
            xbottom = x2;
            ybottom = y2;
            int xl; // 长边在ymiddle时的x，来决定长边是在左边还是右边
            xl = (int) ((ymiddle - ytop) * (xbottom - xtop) / (ybottom - ytop) + xtop + 0.5);

            if (xl <= xmiddle) // 左三角形
            {
                // 画平底
                fillBottomLineTriangle(xtop, ytop, xl, ymiddle, xmiddle, ymiddle);

                // 画平顶
                fillTopLineTriangle(xl, ymiddle, xmiddle, ymiddle, xbottom, ybottom);
            } else // 右三角形
            {
                // 画平底
                fillBottomLineTriangle(xtop, ytop, xmiddle, ymiddle, xl, ymiddle);

                // 画平顶
                fillTopLineTriangle(xmiddle, ymiddle, xl, ymiddle, xbottom, ybottom);
            }
        }
    }

    private synchronized void fillTriangle(int[] x, int[] y) {
        //排序，确保y值最小的点为第一个点，y值最大的点为第三个点
        int minI = 0;
        int maxI = 0;
        x[0] += transX;
        y[0] += transY;
        for (int i = 1; i < 3; i++) {
            x[i] += transX;
            y[i] += transY;
            if (y[i] < y[minI]) {
                minI = i;
            }
            if (y[i] > y[maxI]) {
                maxI = i;
            }
        }
        if (minI == maxI) {//三点共线
            return;
        }
        if (minI != 0) {
            int t = x[0];
            x[0] = x[minI];
            x[minI] = t;
            t = y[0];
            y[0] = y[minI];
            y[minI] = t;
            if (maxI == 0) maxI = minI;
        }
        if (maxI != 2) {
            int t = x[2];
            x[2] = x[maxI];
            x[maxI] = t;
            t = y[2];
            y[2] = y[maxI];
            y[maxI] = t;
        }
        try {
            fillTriangle(x[0], y[0], x[1], y[1], x[2], y[2]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * openai generate
     *
     * @param x
     * @param y
     * @param n
     */
    public void fillPolygon(int[] x, int[] y, int n) {
        if (n < x.length || n < y.length) return;

        if (n == 3) {
            fillTriangle(x, y);
            return;
        }
        int i;
        int ymin = Integer.MAX_VALUE;
        int ymax = Integer.MIN_VALUE;
        boolean yGreatThanClipMin = false;
        boolean yLessThanClipMax = false;
        boolean xGreatThanClipMin = false;
        boolean xLessThanClipMax = false;
        for (i = 0; i < n; i++) {
            x[i] += transX;
            y[i] += transY;
            if (y[i] < ymin) {
                ymin = y[i];
            }
            if (y[i] > ymax) {
                ymax = y[i];
            }
            yLessThanClipMax = yLessThanClipMax || (y[i] < clipY + clipH);
            yGreatThanClipMin = yGreatThanClipMin || (y[i] > clipY);
            xLessThanClipMax = xLessThanClipMax || (x[i] < clipX + clipW);
            xGreatThanClipMin = xGreatThanClipMin || (x[i] > clipX);
        }
        //如果矩形区域超出屏幕，则不绘制
        if (!yGreatThanClipMin || !yLessThanClipMax || !xGreatThanClipMin || !xLessThanClipMax) {
            return;
        }

        IntList[] edgeList = new IntList[ymax - ymin + 1];
        for (i = 0; i < ymax - ymin + 1; i++) {
            edgeList[i] = new IntList();
        }
        for (i = 0; i < n; i++) {
            int x1 = x[i];
            int y1 = y[i];
            int x2 = x[(i + 1) % n];
            int y2 = y[(i + 1) % n];
            if (y1 == y2) {
                edgeList[y1 - ymin].add(x1);
                edgeList[y2 - ymin].add(x2);
                continue;
            }
            if (y1 > y2) {
                int temp;
                temp = x1;
                x1 = x2;
                x2 = temp;
                temp = y1;
                y1 = y2;
                y2 = temp;
            }
            float dx = (float) (x2 - x1) / (float) (y2 - y1);
            float xi = x1;
            for (int yi = y1; yi < y2; yi++) {
                int ixi = Math.round(xi);
                edgeList[yi - ymin].add(ixi);
                xi += dx;
            }
        }
        for (i = ymin; i <= ymax; i++) {
            //Collections.sort(edgeList[i - ymin]);
            //Arrays.sort(edgeList[i-ymin].getArray());
            edgeList[i - ymin].sort();
            for (int j = 0; j < edgeList[i - ymin].size(); j += 2) {
                drawLine(edgeList[i - ymin].get(j), i, edgeList[i - ymin].get(j + 1), i);
            }
        }
    }

    public void drawRegion(Image src, int x_src, int y_src, int width, int height, int transform, int x_dest, int y_dest, int anchor) {
        if (src == null || width == 0 || height == 0) {
            System.out.println("drawRegion src image is null");
            return;
        }
        final int imgw = src.getWidth(null);
        final int imgh = src.getHeight(null);
        x_src = x_src < 0 ? 0 : x_src;
        x_src = x_src > imgw ? imgw : x_src;
        y_src = y_src < 0 ? 0 : y_src;
        y_src = y_src > imgh ? imgh : y_src;

        if (x_src + width > imgw) {
            width = imgw - x_src;
        }
        if (y_src + height > imgh) {
            height = imgh - y_src;
        }

        x_dest += transX;
        y_dest += transY;


        float winX, winY, winW, winH;
        winX = x_dest;
        winY = y_dest;
        winW = width;
        winH = height;
        float ix = 0f, iy = 0f, iw = imgw;
        float px = 0f, py = 0f, pw = 0f, ph = 0f;
        float rot = 0f;
        switch (transform) {
            case GGraphics.TRANS_NONE:
                px = x_dest - x_src;
                py = y_dest - y_src;
                pw = imgw;
                ph = imgh;
                ix = px;
                iy = py;
                winW = width;
                winH = height;
                break;
            case GGraphics.TRANS_ROT90:
                px = x_dest - (imgh - y_src - height);
                py = y_dest - x_src;
                pw = imgh;
                ph = imgw;
                rot = 90f;
                ix = px + imgh;
                iy = py;
                winW = height;
                winH = width;
                break;
            case GGraphics.TRANS_ROT180:
                px = x_dest - (imgw - x_src - width);
                py = y_dest - (imgh - y_src - height);
                pw = imgw;
                ph = imgh;
                rot = 180f;
                ix = px + imgw;
                iy = py + imgh;
                winW = width;
                winH = height;
                break;
            case GGraphics.TRANS_ROT270:
                px = x_dest - y_src;
                py = y_dest - (imgw - x_src - width);
                pw = imgh;
                ph = imgw;
                rot = 270f;
                ix = px;
                iy = py + imgw;
                winW = height;
                winH = width;
                break;
            case GGraphics.TRANS_MIRROR:
                px = x_dest - (imgw - x_src - width);
                py = y_dest - y_src;
                pw = imgw;
                ph = imgh;
                ix = px + imgw;
                iy = py;
                iw = -imgw;
                winW = width;
                winH = height;
                break;
            case GGraphics.TRANS_MIRROR_ROT90:
                px = x_dest - (imgh - y_src - height);
                py = y_dest - (imgw - x_src - width);
                pw = imgh;
                ph = imgw;
                rot = 90f;
                ix = px + imgh;
                iy = py + imgw;
                iw = -imgw;
                winW = height;
                winH = width;
                break;
            case GGraphics.TRANS_MIRROR_ROT180:
                px = x_dest - x_src;
                py = y_dest - (imgh - y_src - height);
                pw = imgw;
                ph = imgh;
                rot = 180f;
                iw = -imgw;
                ix = px;
                iy = py + imgh;
                winW = width;
                winH = height;
                break;
            case GGraphics.TRANS_MIRROR_ROT270:
                px = x_dest - y_src;
                py = y_dest - x_src;
                pw = imgh;
                ph = imgw;
                rot = 270f;
                iw = -imgw;
                ix = px;
                iy = py;
                winW = height;
                winH = width;
                break;
            default:
                throw new IllegalArgumentException("IllegalArgumentException");
        }

        if ((anchor & GGraphics.RIGHT) != 0) {
            px -= winW;
            ix -= winW;
            winX -= winW;
        } else if ((anchor & GGraphics.HCENTER) != 0) {
            px -= winW / 2;
            ix -= winW / 2;
            winX -= winW / 2;
        }
        if ((anchor & GGraphics.BOTTOM) != 0) {
            py -= winH;
            iy -= winH;
            winY -= winH;
        } else if ((anchor & GGraphics.VCENTER) != 0) {
            py -= winH / 2;
            iy -= winH / 2;
            winY -= winH / 2;
        }

        throw new RuntimeException("not implementation yet.");
    }

    public int getARGB() {
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
        throw new RuntimeException("not implementation yet.");
    }

    public synchronized void translate(int x, int y) {
        super.translate(x, y);
        getTransform().translate(x, y);
    }

    public synchronized void setClip(int x, int y, int w, int h) {
        if ((w <= 0) || (h <= 0)) {
            clipX = clipY = clipW = clipH = 0;
            return;
        }
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


    public void setGrayScale(int value) {

    }

    @Override
    public synchronized boolean drawImage(Image img, AffineTransform transform,
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
                    (float) transform.getTranslateX() + transX,
                    (float) transform.getShearY(),
                    (float) transform.getScaleY(),
                    (float) transform.getTranslateY() + transY,
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
            AffineTransform af = transform.get();
            af.setToIdentity();
            af.translate(x, y);
            drawImage(img, af, observer);
        }
        return true;
    }

    @Override
    public synchronized boolean drawImage(Image img, int x, int y, int width, int height,
                                          ImageObserver observer) {
        if (img instanceof BufferedImage) {
            BufferedImage cimg = ((BufferedImage) img);
            AffineTransform af = transform.get();
            af.setToIdentity();
            af.translate(x, y);
            af.scale((double) width / cimg.getWidth(), (double) height / cimg.getHeight());
            drawImage(img, af, observer);
        }
        return true;
    }

    public synchronized boolean drawImage(Image img,
                                          int dx1,
                                          int dy1,
                                          int dx2,
                                          int dy2,
                                          int sx1,
                                          int sy1,
                                          int sx2,
                                          int sy2,
                                          ImageObserver observer) {
        if (img instanceof BufferedImage) {
            BufferedImage cimg = ((BufferedImage) img);
            int srcW = cimg.getWidth();
            byte[] dst = bimg.getData().array();
            byte[] src = cimg.getData().array();
            GLMath.img_draw(dst, imgW,
                    src, srcW,
                    dx1, dy1, dx2 - dx1, dy2 - dy1,
                    (float) (dx2 - dx1) / (sx2 - sx1),
                    (float) 0f,
                    (float) -sx1,
                    (float) 0f,
                    (float) (dy2 - dy1) / (sy2 - sy1),
                    (float) -sy1,
                    1.0f,
                    false, 0);
        }
        return true;
    }

    public void setColor(Color color) {
        super.setColor(color);
        curColor = color.getRGB();
    }
}
