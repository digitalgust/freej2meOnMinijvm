package java.awt.image;

import org.mini.gl.GLMath;
import org.mini.gui.callback.GCallBack;

import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BitmapFont {
    static final int ARR_LEN = 8;
    static final int ARR_ID = 0, ARR_X = 1, ARR_Y = 2, ARR_W = 3, ARR_H = 4, ARR_XOFFSET = 5, ARR_YOFFSET = 6, ARR_XADVANCE = 7;

    static final String TAG_CHAR_COUNT = "chars count=";
    static final String TAG_COMMON = "common ";
    static final String TAG_PAGE_ID = "page id=";
    static final String TAG_FILE = "file=";
    static final String TAG_ID = "id=";

    short[][] charInfo;

    int lineHeight;
    int base;

    BufferedImage fontShap;

    public BitmapFont(String fntPathInJar) {
        load(fntPathInJar);
    }

    private void load(String fntPathInJar) {
        try {
            InputStream is = openRes(fntPathInJar);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            int index = 0;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(TAG_ID)) {
                    charInfo[index] = new short[ARR_LEN];
                    String[] ss = line.split(" ");
                    charInfo[index][ARR_ID] = (short) Integer.parseInt(ss[0].substring(ss[0].indexOf("=") + 1));
                    charInfo[index][ARR_X] = (short) Integer.parseInt(ss[1].substring(ss[1].indexOf("=") + 1));
                    charInfo[index][ARR_Y] = (short) Integer.parseInt(ss[2].substring(ss[2].indexOf("=") + 1));
                    charInfo[index][ARR_W] = (short) Integer.parseInt(ss[3].substring(ss[3].indexOf("=") + 1));
                    charInfo[index][ARR_H] = (short) Integer.parseInt(ss[4].substring(ss[4].indexOf("=") + 1));
                    charInfo[index][ARR_XOFFSET] = (short) Integer.parseInt(ss[5].substring(ss[5].indexOf("=") + 1));
                    charInfo[index][ARR_YOFFSET] = (short) Integer.parseInt(ss[6].substring(ss[6].indexOf("=") + 1));
                    charInfo[index][ARR_XADVANCE] = (short) Integer.parseInt(ss[7].substring(ss[7].indexOf("=") + 1));
                    index++;
                } else if (line.startsWith(TAG_CHAR_COUNT)) {
                    int count = Integer.parseInt(line.substring(line.indexOf('=') + 1));
                    charInfo = new short[count][];
                } else if (line.startsWith(TAG_COMMON)) {
                    int pos = line.indexOf("lineHeight=");
                    lineHeight = Integer.parseInt(line.substring(pos + 11, line.indexOf(' ', pos)));
                    pos = line.indexOf("base=");
                    base = Integer.parseInt(line.substring(pos + 5, line.indexOf(' ', pos)));
                } else if (line.startsWith(TAG_PAGE_ID)) {
                    int pos = line.indexOf(TAG_FILE) + TAG_FILE.length();
                    String bitmapFile = line.substring(pos);
                    bitmapFile = bitmapFile.replace("\"", "");
                    bitmapFile = fntPathInJar.substring(0, fntPathInJar.lastIndexOf('/')) + "/" + bitmapFile;
                    fontShap = ImageIO.read(openRes(bitmapFile));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // prefer the GUI callback loader; fall back to this class' loader so the
    // font also loads in headless contexts (GCallBack not initialized)
    private InputStream openRes(String path) {
        try {
            GCallBack cb = GCallBack.getInstance();
            if (cb != null) {
                InputStream is = cb.getResourceAsStream(path);
                if (is != null) return is;
            }
        } catch (Exception ignored) {
        }
        return BitmapFont.class.getResourceAsStream(path);
    }

    /**
     * binary search to find the char info
     *
     * @param ch
     * @return
     */
    public short[] findCharInfo(char ch) {
        int low = 0;
        int high = charInfo.length - 1;

        while (low <= high) {
            int mid = (low + high) >> 1;
            int midVal = charInfo[mid][ARR_ID] & 0xffff; //convert short to unsigned short

            if (midVal < ch)
                low = mid + 1;
            else if (midVal > ch)
                high = mid - 1;
            else
                return charInfo[mid]; // key found
        }
        return null;  // key not found.
    }

    public int charWidth(char ch) {
        if (ch == 'N') {
            int debug = 1;
        }
        short[] chinfo = findCharInfo(ch);
        if (chinfo != null) return chinfo[ARR_XADVANCE];
        return 12;
    }


    /**
     * draw char without scale and translate.
     * M00=1.0f M11=1.0f
     *
     * @param canvas target image
     * @param ch     character
     * @param x      pen position x (left edge of the advance)
     * @param y      baseline y, matching java.awt.Graphics.drawString semantics
     */
    public int drawChar(BufferedImage canvas, char ch, int x, int y, int rgb, int clipX, int clipY, int clipW, int clipH) {

        short[] chinfo = findCharInfo(ch);
        if (chinfo != null) {
            int cx = x + chinfo[ARR_XOFFSET];
            int cy = y - base + chinfo[ARR_YOFFSET]; // yoffset is relative to line top
            int cw = chinfo[ARR_W];
            int chh = chinfo[ARR_H];

            int tx1 = cx;
            int ty1 = cy;
            int rx1 = clipX;
            int ry1 = clipY;
            int tx2 = tx1 + cw;
            int ty2 = ty1 + chh;
            int rx2 = rx1 + clipW;
            int ry2 = ry1 + clipH;
            if (tx1 < rx1) tx1 = rx1;
            if (ty1 < ry1) ty1 = ry1;
            if (tx2 > rx2) tx2 = rx2;
            if (ty2 > ry2) ty2 = ry2;
            tx2 -= tx1;
            ty2 -= ty1;

//    static public native int img_draw(byte[] imgCanvas, int canvasWidth,
//                                      byte[] img, int imgWidth,
//                                      int clipX, int clipY, int clipW, int clipH,
//                                      float transformM00, float transformM01, float transformM02, float transformM10, float transformM11, float transformM12,
//                                      float alpha,
//                                      boolean bitmapFont, int fontRGB);

            GLMath.img_draw(canvas.getData().array(), canvas.getWidth(),
                    fontShap.getData().array(), fontShap.getWidth(),
                    tx1, ty1, tx2, ty2,
                    1.0f, 0.0f, cx - chinfo[ARR_X], 0.0f, 1.0f, cy - chinfo[ARR_Y],
                    1.0f,
                    true, rgb
            );
            return chinfo[ARR_XADVANCE];
        }
        return 0;
    }

    public int stringWidth(String str) {
        if (str == null) return 0;
        int w = 0;
        for (int i = 0; i < str.length(); i++) {
            w += charWidth(str.charAt(i));
        }
        return w;
    }


    public int charsWidth(char[] str) {
        if (str == null) return 0;
        int w = 0;
        for (int i = 0; i < str.length; i++) {
            w += charWidth(str[i]);
        }
        return w;
    }

    public int charHeight(char ch) {
        return getHeight();
    }


    // user-facing line box: glyph box is 12 rows (lineHeight 13 has 1px built-in
    // spacing), vertically centered: 18 = 12 + TOP_PAD 3 + BOTTOM_PAD 3, per S60 look
    static final int TOP_PAD = 3;
    static final int BOTTOM_PAD = 3;

    public int getHeight() {
        return lineHeight - 1 + TOP_PAD + BOTTOM_PAD;
    }

    public int getAscent() {
        return base + TOP_PAD;
    }

    public int getDescent() {
        return getHeight() - getAscent();
    }
}
