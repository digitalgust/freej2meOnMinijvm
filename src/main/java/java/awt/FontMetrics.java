package java.awt;

import org.mini.gui.GCallBack;
import org.mini.gui.GToolkit;

public class FontMetrics {
    Font font;

    public FontMetrics(Font font) {
        this.font = font;
    }

    public int stringWidth(String str) {
        float[] b = GToolkit.getTextBoundle(GCallBack.getInstance().getNvContext(), str, 1000f, font.size);
        return (int) (b[2] - b[0]);
    }

    public int getAscent() {
        return 0;
    }

    public int getHeight() {
        return 0;
    }
}
