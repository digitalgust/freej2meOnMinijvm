package java.awt.font;


public final class TextAttribute {

    public static final TextAttribute UNDERLINE = new TextAttribute("underline");

    public static final Integer UNDERLINE_ON = new Integer((byte) 0);

    public static final TextAttribute STRIKETHROUGH = new TextAttribute("strikethrough");
    private final String name;

    protected TextAttribute(String name) {
        this.name = name;
    }
}
