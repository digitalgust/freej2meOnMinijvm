package java.awt;

import org.mini.gui.GCallBack;
import org.mini.gui.GCanvas;
import org.mini.gui.GToolkit;

import java.awt.image.BufferedImage;

public class Canvas extends Component {
    BufferedImage bimg;

    public Canvas() {
        peer = new GCanvas(GCallBack.getInstance().getApplication().getForm(), 0, 0, 1, 1) {
            public boolean paint(long vg) {
                if (bimg != null) {
                    bimg.getImage().updateImage();
                    GToolkit.drawImage(vg, bimg.getImage(), getX(), getY(), getW(), getH(), false, 1.0f);
                }
                return true;
            }
        };
    }

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
        if (bimg != null) {
            bimg = null;
        }
    }

    @Override
    public Graphics getGraphics() {
        if (bimg == null) {
            bimg = new BufferedImage((int) peer.getW(), (int) peer.getH(), BufferedImage.TYPE_INT_ARGB);
        }
        return bimg.getGraphics();
    }
}
