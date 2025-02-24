package java.awt;

import org.mini.gui.*;
import org.mini.gui.callback.GCallBack;

import java.awt.image.BufferedImage;

public class Canvas extends Component {
    BufferedImage bimg;

    public Canvas() {
        GObject peer = new GCanvas(GCallBack.getInstance().getApplication().getForm(), 0, 0, 1, 1) {
            public boolean paint(long vg) {
                if (bimg != null) {
                    //bimg.setRGB(bimg.getWidth() / 2, bimg.getHeight() / 2, 0xffffffff);
                    bimg.getImage().updateImage();
                    GToolkit.drawImage(vg, bimg.getImage(), getX(), getY(), getW(), getH(), false, 1.0f);
                    GForm.flush();
                }
                //GToolkit.drawRect(vg, getX(), getY(), getW(), getH(), GToolkit.getStyle().getHighColor(), false);
                return super.paint(vg);
            }
        };
        setPeer(peer);
    }

    @Override
    public void setParent(Container parent) {
        super.setParent(parent);
        setSize(parent.getWidth(), parent.getHeight());
        if (bimg != null) {

        }
        ;
    }

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
        getPeer().getParent().reAlign();
        bimg = null;
    }

    @Override
    public Graphics getGraphics() {
        if (bimg == null) {
            bimg = new BufferedImage((int) getPeer().getW(), (int) getPeer().getH(), BufferedImage.TYPE_INT_ARGB);
        }
        return bimg.getGraphics();
    }
}
