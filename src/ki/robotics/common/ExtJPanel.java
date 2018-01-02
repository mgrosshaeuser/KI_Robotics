package ki.robotics.common;

import javax.swing.*;
import java.awt.*;

/**
 * Giving a JPanel the capability to add more than one component at once.
 *
 * @version 1.0 01/02/18
 */
public class ExtJPanel extends JPanel {
    /**
     * Adding an arbitrary number of components to a JPanel.
     *
     * @param components    Components to be added.
     */
    public void addAll(Component ... components) {
        for (Component c : components) {
            add(c);
        }
    }
}
