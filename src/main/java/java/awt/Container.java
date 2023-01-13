package java.awt;


import org.mini.gui.GContainer;

public class Container extends Component {

    public Insets getInsets() {
        return insets();
    }

    public Insets insets() {
        return new Insets(0, 0, 0, 0);
    }


    public Component add(Component comp) {
        ((GContainer) peer).add(comp.peer);
        return comp;
    }

    public Component add(String name, Component comp) {
        ((GContainer) peer).add(comp.peer);
        comp.peer.setName(name);
        return comp;
    }

    public Component add(Component comp, int index) {
        ((GContainer) peer).add(index, comp.peer);
        return comp;
    }
}
