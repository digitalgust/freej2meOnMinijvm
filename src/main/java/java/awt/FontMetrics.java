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
        return font.bitmapfont.getHeight();
    }

    public int getHeight() {
        return font.bitmapfont.getHeight();
    }
}
