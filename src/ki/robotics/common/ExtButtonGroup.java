package ki.robotics.common;

import javax.swing.*;

/**
 * Giving a ButtonGroup the capability to add more than one button at once.
 *
 * @version 1.0 01/02/18
 */
public class ExtButtonGroup extends ButtonGroup {
    /**
     * Adding an arbitrary number of buttons to a ButtonGroup.
     *
     * @param buttons   The buttons to be added.
     */
    public void addAll(AbstractButton ... buttons) {
        for (AbstractButton b : buttons) {
            add(b);
        }
    }
}
