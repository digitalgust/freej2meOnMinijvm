package javax.imageio;

import org.mini.glwrap.GLUtil;

import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ImageIO {


    public static boolean write(RenderedImage im,
                                String formatName,
                                File output) throws IOException {
        if (output == null) {
            throw new IllegalArgumentException("output == null!");
        }
        ImageOutputStream stream = null;

        return true;
    }


    public static BufferedImage read(InputStream input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input == null!");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] k = new byte[1024];
        int r;
        while ((r = input.read(k)) > 0) {
            baos.write(k, 0, r);
        }
        k = baos.toByteArray();
        if (k.length == 0) {
            throw new IOException("image stream is empty");
        }
        int[] whd = {0, 0, 0};
        byte[] b = GLUtil.image_parse_from_file_content(k, whd);
        if (b == null) {
            throw new IOException("image decode failed");
        }

        int channels = whd[2];
        if (whd[0] <= 0 || whd[1] <= 0) {
            throw new IOException("invalid image size: " + whd[0] + "x" + whd[1]);
        }
        BufferedImage img = new BufferedImage(whd[0], whd[1], BufferedImage.TYPE_INT_ARGB);
        if (channels == 4) {//rgba
            img.getData().put(b);
        } else if (channels == 3) {//rgb
            for (int i = 0; i < b.length; i += 3) {
                img.getData().put(b[i + 0]);
                img.getData().put(b[i + 1]);
                img.getData().put(b[i + 2]);
                img.getData().put((byte) 0xff);//insert alpha
            }
        } else if (channels == 2) {//gray and alpha
            for (int i = 0; i < b.length; i += 2) {
                byte gray = b[i];
                img.getData().put(gray);
                img.getData().put(gray);
                img.getData().put(gray);
                img.getData().put(b[i + 1]);
            }
        } else if (channels == 1) {//gray
            for (int i = 0; i < b.length; i++) {
                byte gray = b[i];
                img.getData().put(gray);
                img.getData().put(gray);
                img.getData().put(gray);
                img.getData().put((byte) 0xff);
            }
        } else {
            throw new IOException("unknown image channel count: " + channels);
        }

        return img;
    }


}
