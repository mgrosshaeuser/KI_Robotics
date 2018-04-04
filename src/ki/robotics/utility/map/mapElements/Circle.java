package ki.robotics.utility.map.mapElements;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;


/**
 * Representation of circular map-elements matching a SVG-"circle"-elements.
 */
public class Circle extends Ellipse2D.Double {
    private String id;
    private int stroke = Color.BLACK.getRGB();
    private int fill = Color.BLACK.getRGB();
    private Line2D occupiedWallArea = new Line2D.Double();



    /**
     * Constructs and initializes a Circle with the specified center and radius.
     *
     * @param center the center of the newly constructed Circle
     * @param radius the radius of the newly constructed Circle
     */
    public Circle(Point2D center, double radius) {
        super(center.getX() - radius, center.getY() - radius, radius * 2, radius * 2);
    }


    /**
     * Returns the id of this shape as String.
     *
     * @return the id of the circle
     */
    public String getId() {
        return this.id;
    }



    /**
     * Sets the id of this shape to be the specified String.
     *
     * @param id the specified id
     */
    public void setId(String id) {
        this.id = id;
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
     * Returns the diameter of this shape in double precision.
     *
     * @return the diameter of this shape.
     */
    public double getDiameter() {
        return this.getWidth() * 2;
    }



    /**
     * Returns a Line2D representing the part of a Line, covered by this Circle. If no Line is covered or
     * the attribute is not initialized, null is returned.
     *
     * @return a line-segment covered by this circle or null.
     */
    public Line2D getOccupiedWallArea() {
        return occupiedWallArea;
    }



    /**
     * Sets the line-segment covered by this circle, ranging from P1 to P2.
     *
     * @param P1 the start of the covered line-segment.
     * @param P2 the end of the covered line-segment.
     */
    public void setOccupiedWallArea(Point2D.Double P1, Point2D.Double P2) {
        this.occupiedWallArea = new Line2D.Double(P1, P2);
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
        int radius = (int)Math.round(this.width) / 2;

        int x = ((int)Math.round(this.getCenterX()) - radius) * scaleFactor + xOffset;
        int y = ((int)Math.round(this.getCenterY()) - radius) * scaleFactor + yOffset;
        int diameter = radius * 2 * scaleFactor;

        g2d.setColor(new Color(this.fill));
        g2d.fillOval(x, y, diameter, diameter);

        g2d.setColor(new Color(this.stroke));
        g2d.drawOval(x, y, diameter, diameter);

        x = ((int)Math.round(this.getCenterX()) - radius) * scaleFactor + xOffset + (radius * scaleFactor);
        y = ((int)Math.round(this.getCenterY()) - radius) * scaleFactor + yOffset + (radius * scaleFactor);
        g2d.setColor(Color.BLACK);
        g2d.drawString(this.getId(),x, y);
    }

}
