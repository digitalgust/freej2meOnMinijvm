package java.awt.image;

import org.mini.gui.GImage;
import org.mini.gui.ImageMutable;

import javax.imageio.WritableRenderedImage;
import java.awt.*;
import java.nio.ByteBuffer;

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


    public BufferedImage(int width,
                         int height,
                         int imageType) {
//        if (imageType != TYPE_INT_ARGB) {
//            throw new RuntimeException("Not support BufferedImage type " + imageType);
//        }
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
        return null;
    }

    public void setRGB(int startX, int startY, int w, int h, int[] rgbData, int offset, int scanlength) {
        gimg.setPix(rgbData, 0, scanlength, 0, 0, w, h);
    }

    public void setRGB(int startX, int startY, int c) {
        gimg.setPix(startY, startX, c);
    }

    public int[] getRGB(int x, int y, int width, int height, int[] pixels, int offset, int scanlength) {
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
