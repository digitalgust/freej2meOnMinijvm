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
        int[] whd = {0, 0, 0};
        byte[] b = GLUtil.image_parse_from_file_content(k, whd);

        BufferedImage img = new BufferedImage(whd[0], whd[1], BufferedImage.TYPE_INT_ARGB);
        img.getData().put(b);

        return img;
    }


}
