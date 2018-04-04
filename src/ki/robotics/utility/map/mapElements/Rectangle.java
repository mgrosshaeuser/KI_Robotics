package ki.robotics.utility.map.mapElements;

import java.awt.*;
import java.awt.geom.Rectangle2D;


/**
 * Representation of rectangular map-elements matching a SVG-"rect"-elements.
 */
public class Rectangle extends Rectangle2D.Double {
    private int stroke;
    private int fill;



    /**
     * Constructs and initializes a Rectangle with the specified coordinates and dimensions.
     *
     * @param x the X coordinate of the upper-left corner of the newly constructed Rectangle
     * @param y the Y coordinate of the upper-left corner of the newly constructed Rectangle
     * @param width the width of the newly constructed Rectangle
     * @param height the height of the newly constructed Rectangle
     */
    public Rectangle(double x, double y, double width, double height) {
        super(x, y, width, height);
    }



    /**
     * Returns the stroke-color of this shape as integer, suitable for the
     * constructor java.awt.Color(int rgb).
     *
     * @return the stroke-color as integer (rgb)
     */
    public int getStroke() {
        return stroke;
    }



    /**
     * Sets the stroke-color of this shape to be the specified rgb-value.
     * In case of a value outside the rgb-color-space, black is the default color.
     *
     * @param stroke the specified stroke-color
     */
    public void setStroke(int stroke) {
        int rgbColorSpaceUpperLimit = 255255256;
        if (stroke >= 0  &&  stroke  < rgbColorSpaceUpperLimit) {
            this.stroke = stroke;
        } else {
            this.stroke = Color.BLACK.getRGB();
        }
    }



    /**
     * Returns the fill-color of this shape as integer, suitable for the
     * constructor java.awt.Color(int rgb).
     *
     * @return the fill-color as integer (rgb)
     */
    public int getFill() {
        return fill;
    }



    /**
     * Sets the fill-color of this shape to be the specified rgb-value.
     * In case of a value outside the rgb-color-space, black is the default color.
     *
     * @param fill the specified fill-color
     */
    public void setFill(int fill) {
        int rgbColorSpaceUpperLimit = 255255256;
        if (fill >= 0  &&  fill  < rgbColorSpaceUpperLimit) {
            this.fill = fill;
        } else {
            this.fill = Color.BLACK.getRGB();
        }
    }



    /**
     * Paints this shape in the given graphics context using the given scale-factor and offsets.
     *
     * @param g             The graphics context to paint in.
     * @param scaleFactor   The factor for size-adjustment.
     * @param xOffset       The vertical offset.
     * @param yOffset       The horizontal offset.
     */
    public void paint(Graphics g, int scaleFactor, int xOffset, int yOffset) {
        Graphics2D g2d = (Graphics2D) g;

        int x = (int)Math.round(this.getX()) * scaleFactor + xOffset;
        int y = (int)Math.round(this.getY()) * scaleFactor + yOffset;
        int width = (int)Math.round(this.width) * scaleFactor;
        int height = (int)Math.round(this.height) * scaleFactor;

        g2d.setColor(new Color(this.fill));
        g2d.fillRect(x, y, width, height);

        g2d.setColor(new Color(this.stroke));
        g2d.drawRect(x, y, width, height);
    }
}

