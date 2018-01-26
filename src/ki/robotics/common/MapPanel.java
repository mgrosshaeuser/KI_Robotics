package ki.robotics.common;

import ki.robotics.utility.map.Map;

import javax.swing.*;
import java.awt.*;

/**
 * Abstract class for the common operations regarding the display of a Map.
 *
 * @version 1.0 01/02/18
 */
public abstract class MapPanel extends JPanel{
    private int scaleFactor = 1;
    private int xOffset = 0;
    private int yOffset = 0;
    private boolean isModifiable;
    private final JFrame parent;
    private Map map;

    /**
     * Constructor.
     */
    protected MapPanel(JFrame parent, Map map) {
        this.parent = parent;
        this.map = map;
        this.isModifiable = true;

    }

    public void setNewMap(Map map) {
        this.map = map;
        repaint();
    }

    public int getScaleFactor() {   return scaleFactor; }
    public int getxOffset()     {   return xOffset;     }
    public int getyOffset()     {   return yOffset;     }

    public boolean isModifiable() { return isModifiable; }
    public void setModifiable(boolean modifiable) { isModifiable = modifiable;  }

    /**
     * Initiates recalculations of the visual parameters and paints the map.
     *
     * @param g     The graphical context.
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        updateVisualParameters();
        this.setBackground(Color.LIGHT_GRAY);
        if (map != null) {
            map.paint(g, scaleFactor, xOffset, yOffset);
        }
    }


    /**
     * (Re)Calculates the visual parameters, including
     * - the scale-factor to use the available space at its best
     * - the x- and y-offsets to center the simulation in the window.
     */

    private void updateVisualParameters() {
        if (map == null) {
            return;
        }
        int maxX = map.getRequiredMinWidth();
        int maxY = map.getRequiredMinHeight();

        double width = this.getVisibleRect().getWidth();
        double height = this.getVisibleRect().getHeight();

        double scaleX = width / maxX;
        double scaleY = height / maxY;

        double limitingFactor = scaleX > scaleY ? scaleY : scaleX;

        scaleFactor = (int) Math.abs(Math.floor(limitingFactor));

        xOffset = ((int) Math.abs(width) - (maxX * scaleFactor)) / 2;
        yOffset = ((int) Math.abs(height) - (maxY * scaleFactor)) / 2;
    }
}
