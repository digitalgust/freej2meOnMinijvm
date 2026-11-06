package java.awt;

import org.mini.gui.*;
import org.mini.gui.callback.GCallBack;

import java.awt.image.BufferedImage;

public class Canvas extends Component {
    private final Object frameLock = new Object();
    private BufferedImage pendingFrame;
    private BufferedImage spareFrame;
    private long nextFrame;
    private long submittedFrame;

    public Canvas() {
        GObject peer = new GCanvas(GCallBack.getInstance().getApplication().getForm(), 0, 0, 1, 1) {
            private BufferedImage frontFrame;

            public boolean paint(long vg) {
                boolean hasNewFrame = false;
                synchronized (frameLock) {
                    if (pendingFrame != null) {
                        if (frontFrame != null) spareFrame = frontFrame;
                        frontFrame = pendingFrame;
                        pendingFrame = null;
                        hasNewFrame = true;
                    }
                }
                if (frontFrame != null) {
                    if (hasNewFrame) frontFrame.getImage().updateImage();
                    GToolkit.drawImage(vg, frontFrame.getImage(), getX(), getY(), getW(), getH(), false, 1.0f);
                }
                return super.paint(vg);
            }
        };
        setPeer(peer);
    }

    @Override
    public void setParent(Container parent) {
        super.setParent(parent);
        setSize(parent.getWidth(), parent.getHeight());
    }

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
        getPeer().getParent().reAlign();
    }

    @Override
    public void repaint() {
        int width = (int) getPeer().getW();
        int height = (int) getPeer().getH();
        if (width <= 0 || height <= 0) return;

        BufferedImage frame;
        long serial;
        synchronized (frameLock) {
            frame = spareFrame;
            spareFrame = null;
            serial = ++nextFrame;
        }
        if (frame == null || frame.getWidth() != width || frame.getHeight() != height) {
            frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }
        // This buffer is private until paint() completes. No lock spans paint or GPU upload.
        paint(frame.getGraphics());
        synchronized (frameLock) {
            if (serial < submittedFrame) {
                if (spareFrame == null) spareFrame = frame;
                return;
            }
            submittedFrame = serial;
            if (pendingFrame != null) spareFrame = pendingFrame;
            pendingFrame = frame;
        }
        GForm.flush();
    }
}
