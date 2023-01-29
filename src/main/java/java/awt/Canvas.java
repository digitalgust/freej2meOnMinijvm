package java.awt;

import org.mini.gui.*;

import java.awt.image.BufferedImage;

public class Canvas extends Component {
    BufferedImage bimg;

    public Canvas() {
        peer = new GCanvas(GCallBack.getInstance().getApplication().getForm(), 0, 0, 1, 1) {
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
    }

    @Override
    public void setParent(Container parent) {
        super.setParent(parent);
        setSize(parent.getWidth(), parent.getHeight());
        if(bimg != null){

        };
    }

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
        peer.getParent().reAlign();
    }

    @Override
    public Graphics getGraphics() {
        if (bimg == null) {
            bimg = new BufferedImage((int) peer.getW(), (int) peer.getH(), BufferedImage.TYPE_INT_ARGB);
        }
        return bimg.getGraphics();
    }
}
