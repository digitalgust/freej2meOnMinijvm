package java.awt.image;

import org.mini.gui.GCallBack;
import org.mini.gui.GCanvas;

import javax.imageio.WritableRenderedImage;
import java.awt.*;

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


    GCanvas canvas;
    Graphics2D graphics;


    public BufferedImage(int width,
                         int height,
                         int imageType) {
        canvas = new GCanvas(GCallBack.getInstance().getApplication().getForm(), 0, 0, width, height);
    }

    public Graphics2D createGraphics() {
        if (graphics == null) {
            graphics = new Graphics2D(canvas, GCallBack.getInstance().getNvContext());
        }
        return graphics;
    }

    public int getWidth() {
        return (int) canvas.getW();
    }

    public int getHeight() {
        return (int) canvas.getH();
    }

    @Override
    public int getWidth(ImageObserver observer) {
        return (int) canvas.getW();
    }

    @Override
    public int getHeight(ImageObserver observer) {
        return (int) canvas.getH();
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
        return null;
    }

    @Override
    public void flush() {

    }

    public BufferedImage getSubimage(int x, int y, int width, int height) {
        return null;
    }

    public void setRGB(int i, int i1, int width, int height, int[] rgbData, int offset, int scanlength) {

    }

    public void setRGB(int i, int i1, int c) {

    }

    public int[] getRGB(int x, int y, int width, int height, int[] pixels, int offset, int scanlength) {
        return pixels;
    }

    public int getRGB(int i, int i1) {
        return 0xffffffff;
    }
}
