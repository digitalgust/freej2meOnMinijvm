package java.awt;


import org.mini.gui.GContainer;

import java.util.ArrayList;
import java.util.List;

public class Container extends Component {
    List<Component> children = new ArrayList<>();

    public Insets getInsets() {
        return insets();
    }

    public Insets insets() {
        return new Insets(0, 0, 0, 0);
    }


    public Component add(Component comp) {
        return add(comp, 0);
    }

    public Component add(String name, Component comp) {

        comp.peer.setName(name);
        return add(comp, 0);
    }

    public Component add(Component comp, int index) {
        if (comp == null) return null;
        if (!children.contains(comp)) children.add(comp);
        ((GContainer) peer).add(index, comp.peer);
        comp.setParent(this);
        return comp;
    }
}
