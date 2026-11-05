package java.awt;

import org.mini.gui.*;
import org.mini.gui.callback.GCallBack;

import java.awt.image.BufferedImage;

public class Canvas extends Component {
    volatile BufferedImage bimg;

    public Canvas() {
        GObject peer = new GCanvas(GCallBack.getInstance().getApplication().getForm(), 0, 0, 1, 1) {
            public boolean paint(long vg) {
                // MIDlet 在后台线程加载，首次绘制时缓冲区可能还未创建。
                // 即使暂时没有图像也要续请求下一帧，避免 UI 刷新在加载期间停掉。
                GForm.flush();
                BufferedImage image = bimg;
                if (image != null) {
                    //bimg.setRGB(bimg.getWidth() / 2, bimg.getHeight() / 2, 0xffffffff);
                    image.getImage().updateImage();
                    GToolkit.drawImage(vg, image.getImage(), getX(), getY(), getW(), getH(), false, 1.0f);
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
            // 首次生成图像或调整尺寸后，主动唤醒 UI 线程。
            GForm.flush();
        }
        return bimg.getGraphics();
    }
}
