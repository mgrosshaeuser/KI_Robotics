package ki.robotics.utility.map;

import ki.robotics.utility.svg.svg_Rectangle;

import java.awt.*;


/**
 * Representation of a floor tile in a map.
 *
 * @version 1.0 12/28/17
 */
public class FloorTile {

   private svg_Rectangle rectangle;


    /**
     *Constructor.
     *
     * @param x1        x-Coordinate of the upper left vertex.
     * @param y1        y-Coordinate of the upper left vertex.
     * @param width     Width of the rectangular floor-tile.
     * @param height    Height of the rectangular floor-tile.
     * @param stroke    RGB-Color of the stroke as integer-value.
     * @param fill      RGB-Color of the fill as integer-value.
     */
    public FloorTile(double x1, double y1, double width, double height, int stroke, int fill) {
        this.rectangle = new svg_Rectangle(x1, y1, width, height, stroke, fill);
    }



    /**
     * Constructor.
     *
     * @param r     An instance of svg_Rectangle from the svg
     */
    public FloorTile(svg_Rectangle r) {
        this(r.getXAsInt(), r.getYAsInt(), r.getWidthAsInt(), r.getHeightAsInt(), r.getStroke(), r.getFill());
    }



    /**
     * Allows checking whether a given point lies within the floor-tile.
     * @param x     x-Coordinate of the point to be checked.
     * @param y     y-Coordinate of the point to be checked.
     * @return      true, in case the point lies within the floor-tile, otherwise false.
     */
    public boolean containsPoint(int x, int y) {
        return rectangle.contains(x, y);
    }



    /**
     * Returns the color of the floor-tile, which is the fill of the associated rectangle.
     *
     * @return  the floor-color.
     */
    public int getColor() {
        return rectangle.getFill();
    }



    /**
     * Paints the floor-tile in a given graphics-context using given scale-factor and offsets.
     *
     * @param g             The graphics-context to paint in.
     * @param scaleFactor   The scale-factor for adjusting position and size.
     * @param xOffset       The vertical offset.
     * @param yOffset       The horizontal offset.
     */
    public void paint(Graphics g, int scaleFactor, int xOffset, int yOffset) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(rectangle.getFill()));
        g2d.fillRect(
                rectangle.getXAsInt() * scaleFactor + xOffset,
                rectangle.getYAsInt() * scaleFactor + yOffset,
                rectangle.getWidthAsInt() * scaleFactor,
                rectangle.getHeightAsInt() * scaleFactor
        );
    }
}

