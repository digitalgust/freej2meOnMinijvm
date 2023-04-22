package java.awt;


import org.mini.gui.GContainer;

import java.util.ArrayList;
import java.util.List;

public class Container extends Component {

    LayoutManager layoutMgr;
    List<Component> children = new ArrayList<>();

    public Insets getInsets() {
        return insets();
    }

    public Insets insets() {
        return new Insets(0, 0, 0, 0);
    }

    public List<Component> getChildren() {
        return children;
    }

    public Component add(Component comp) {
        return add(comp, 0);
    }

    public Component add(String name, Component comp) {

        comp.getPeer().setName(name);
        return add(comp, 0);
    }

    public Component add(Component comp, int index) {
        if (comp == null) return null;
        if (!children.contains(comp)) children.add(comp);
        ((GContainer) getPeer()).add(index, comp.getPeer());
        comp.setParent(this);
        return comp;
    }

    public void setLayout(LayoutManager mgr) {
        layoutMgr = mgr;
    }
}
