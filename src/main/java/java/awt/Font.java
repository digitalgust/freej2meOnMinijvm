package java.awt;

import java.awt.image.BitmapFont;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class Font {

    public static final int PLAIN = 0;

    public static final int BOLD = 1;

    public static final int ITALIC = 2;

    public static final int ROMAN_BASELINE = 0;

    public static final int CENTER_BASELINE = 1;

    public static final int HANGING_BASELINE = 2;


    public static final int TRUETYPE_FONT = 0;

    public static final String DIALOG = "Dialog";
    public static final String DIALOG_INPUT = "DialogInput";
    public static final String SANS_SERIF = "SansSerif";
    public static final String SERIF = "Serif";
    public static final String MONOSPACED = "Monospaced";

    protected String name;

    protected int style;

    protected int size;

    protected float pointSize;

    private Hashtable fRequestedAttributes = new Hashtable();

    boolean createdFont = true;
    BitmapFont bitmapfont;
    static Map<String, BitmapFont> fonts = new HashMap<>();


    public Font(String name, int style, int size) {
        this.name = name;
        this.style = (style & ~0x03) == 0 ? style : 0;
        this.size = size;
        this.pointSize = size;
        bitmapfont = fonts.get(name);
        if (bitmapfont == null) {
            bitmapfont = new BitmapFont("/12txt.fnt");
            fonts.put(name, bitmapfont);
        }
        if (bitmapfont == null) throw new RuntimeException("Create font error " + name);
    }

    private Font(Map attributes, boolean created) {
        this(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 14);
        this.createdFont = created;
        fRequestedAttributes.putAll(attributes);
    }

    public Map getAttributes() {
        return (Map) fRequestedAttributes;
    }

    public Font deriveFont(int style, float size) {
        Hashtable newAttributes = new Hashtable();
        this.style = style;
        this.size = (int) size;
        return new Font(newAttributes, createdFont);
    }

    public Font deriveFont(Map attributes) {
        if (attributes == null || attributes.size() == 0) {
            return this;
        }

        Hashtable newAttrs = new Hashtable(getAttributes());

        return new Font(newAttrs, createdFont);
    }

    public int getSize() {
        return size;
    }

    public BitmapFont getBitmapfont() {
        return bitmapfont;
    }
}
