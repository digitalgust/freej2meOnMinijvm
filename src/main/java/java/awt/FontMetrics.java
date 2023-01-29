package java.awt;

public class FontMetrics {
    Font font;

    public FontMetrics(Font font) {
        this.font = font;
    }

    public int stringWidth(String str) {
        return (int) font.bitmapfont.stringWidth(str);
    }

    public int getAscent() {
        return 0;
    }

    public int getHeight() {
        return 12;
    }
}
