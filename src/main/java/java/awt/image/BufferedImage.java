package java.awt.image;

import org.mini.gl.GLMath;
import org.mini.gui.GImage;
import org.mini.gui.ImageMutable;

import javax.imageio.WritableRenderedImage;
import java.awt.*;
import java.nio.ByteBuffer;


/**
 * BufferedImage bytes array dependence ImageMutable
 * <p>
 * ImageMutable is ABGR format
 */
public class BufferedImage extends java.awt.Image implements WritableRenderedImage {
    public static final int TYPE_CUSTOM = 0;
    public static final int TYPE_INT_RGB = 1;
    public static final int TYPE_INT_ARGB = 2;
    public static final int TYPE_INT_ARGB_PRE = 3;
    public static final int TYPE_INT_BGR = 4;
    public static final int TYPE_3BYTE_BGR = 5;
    public static final int TYPE_4BYTE_ABGR = 6;
    public static final int TYPE_4BYTE_ABGR_PRE = 7;
    public static final int TYPE_USHORT_565_RGB = 8;
    public static final int TYPE_USHORT_555_RGB = 9;
    public static final int TYPE_BYTE_GRAY = 10;
    public static final int TYPE_USHORT_GRAY = 11;
    public static final int TYPE_BYTE_BINARY = 12;
    public static final int TYPE_BYTE_INDEXED = 13;

    //
    private static final int DCM_RED_MASK = 0x00ff0000;
    private static final int DCM_GREEN_MASK = 0x0000ff00;
    private static final int DCM_BLUE_MASK = 0x000000ff;
    private static final int DCM_ALPHA_MASK = 0xff000000;
    private static final int DCM_565_RED_MASK = 0xf800;
    private static final int DCM_565_GRN_MASK = 0x07E0;
    private static final int DCM_565_BLU_MASK = 0x001F;
    private static final int DCM_555_RED_MASK = 0x7C00;
    private static final int DCM_555_GRN_MASK = 0x03E0;
    private static final int DCM_555_BLU_MASK = 0x001F;
    private static final int DCM_BGR_RED_MASK = 0x0000ff;
    private static final int DCM_BGR_GRN_MASK = 0x00ff00;
    private static final int DCM_BGR_BLU_MASK = 0xff0000;


    ImageMutable gimg;
    Graphics2D graphics2D;
    int imageType;

    static final byte BYTE_PER_PIXEL = 4;


    public BufferedImage(int width,
                         int height,
                         int imageType) {
//        if (imageType != TYPE_INT_ARGB) {
//            throw new RuntimeException("Not support BufferedImage type " + imageType);
//        }
        this.imageType = imageType;
        gimg = GImage.createImageMutable(width, height);
    }

    public Graphics2D createGraphics() {
        if (graphics2D == null) {
            graphics2D = new BufferedImageGraphics(this);
        }
        return graphics2D;
    }

    public int getWidth() {
        return (int) gimg.getWidth();
    }

    public int getHeight() {
        return (int) gimg.getHeight();
    }

    @Override
    public int getWidth(ImageObserver observer) {
        return (int) gimg.getWidth();
    }

    @Override
    public int getHeight(ImageObserver observer) {
        return (int) gimg.getHeight();
    }

    @Override
    public ImageProducer getSource() {
        return null;
    }

    @Override
    public Graphics getGraphics() {
        return createGraphics();
    }

    @Override
    public Object getProperty(String name, ImageObserver observer) {
        return "";
    }

    @Override
    public void flush() {

    }

    public BufferedImage getSubimage(int x, int y, int width, int height) {
        if (x == 0 && y == 0 && width == getWidth() && height == getHeight()) {
            return this;
        }
        BufferedImage nimg = new BufferedImage(width, height, TYPE_INT_ARGB);
//        Graphics g2d = nimg.getGraphics();
//        g2d.drawImage(this, -x, -y, null);

        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + width > this.getWidth()) width = this.getWidth() - x;
        if (y + height > this.getHeight()) height = this.getHeight() - y;
        byte[] src = gimg.getData().array();
        byte[] dst = nimg.getData().array();

        int len = width * BYTE_PER_PIXEL;
        for (int srcY = y, imax = srcY + height, dstY = 0; srcY < imax; srcY++, dstY++) {
            int srcRowStartBytes = (srcY * getWidth() + x) * BYTE_PER_PIXEL;
            int dstRowStartBytes = (dstY * width) * BYTE_PER_PIXEL;
            System.arraycopy(src, srcRowStartBytes, dst, dstRowStartBytes, len);
        }
        return nimg;
    }

    public void setRGB(int startX, int startY, int w, int h, int[] argbArray, int offset, int scanlength) {

        int imgW = gimg.getWidth();

        for (int y = startY, ymax = startY + h; y < ymax; y++) {
            for (int x = startX, xmax = startX + w; x < xmax; x++) {
                int pixel = argbArray[offset + (y - startY) * scanlength + (x - startX)];
                GLMath.img_fill(getData().array(), y * imgW + x, 1, pixel);
            }
        }

        //this method is rgba format
    }

    public void setRGB(int startX, int startY, int c) {
        //argb ->abgr
        int nc = (0xff000000 & c);//a
        nc |= (c >> 16) & 0xff;//r
        nc |= (c) & 0x0000ff00;//g
        nc |= (c & 0xff) << 16;//b
        gimg.setPix(startY, startX, nc);
    }

    public int[] getRGB(int x, int y, int width, int height, int[] pixels, int offset, int scanlength) {
        int tgtY = offset / scanlength;
        int tgtX = offset % scanlength;
        int tgtW = scanlength;
        int tgtH = pixels.length / scanlength;
        for (int j = y; j < height && j < gimg.getHeight() && tgtY < tgtH; j++, tgtY++) {
            int dx = tgtX;
            int tgtRowStart = tgtY * scanlength;
            for (int i = x; i < width && i < gimg.getWidth() && dx < tgtW; i++, dx++) {
                pixels[tgtRowStart + dx] = gimg.getPix(j, i);
            }
        }
        return pixels;
    }

    public int getRGB(int x, int y) {
        return gimg.getPix(y, x);
    }

    public ByteBuffer getData() {
        return gimg.getData();
    }

    public ImageMutable getImage() {
        return gimg;
    }
}
