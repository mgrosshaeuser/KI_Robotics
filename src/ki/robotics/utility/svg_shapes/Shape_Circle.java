package ki.robotics.utility.svg_shapes;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;



/**
 * Representation of a SVG-circle-Element.
 *
 * @version 1.0, 12/28/17
 */
public class Shape_Circle extends Ellipse2D.Double {
    private Point2D.Double center;
    private double diameter;
    private int stroke;
    private int fill;



    /**
     * Constructor.
     *
     * @param cx        x-Coordinate for the center of the circle.
     * @param cy        y-Coordinate for the center of the circle.
     * @param diameter  Diameter of the circle.
     */
    public Shape_Circle(double cx, double cy, double diameter) {
        this.center = new Point2D.Double(cx, cy);
        this.diameter = diameter;
    }



    /**
     * Constrcutor.
     *
     * @param cx        x-Coordinate for the center of the circle.
     * @param cy        y-Coordinate for the center of the circle.
     * @param diameter  Diameter of the circle.
     * @param stroke    Stroke-color of the circle as integer-RGB-value.
     * @param fill      Fill-color of the circle as integer-RGB-value.
     */
    public Shape_Circle(double cx, double cy, double diameter, int stroke, int fill) {
        this(cx, cy, diameter);
        this.stroke = stroke;
        this.fill = fill;
    }



    /**
     * Returns the x-coordinate of the center of the circle.
     *
     * @return  x-Coordinate of the center.
     */
    @Override
    public double getX() {
        return center.getX();
    }



    /**
     * Returns the y-coordinate of the center of the circle.
     *
     * @return  y-Coordinate of the center.
     */
    @Override
    public double getY() {
        return center.getY();
    }



    /**
     * Returns the width of the circle which is identical to its diameter.
     *
     * @return  Width (= diameter) of the circle.
     */
    @Override
    public double getWidth() {
        return diameter;
    }



    /**
     * Returns the height of the circle which is identical to its diameter.
     *
     * @return Height (= diameter) of the circle.
     */

    @Override
    public double getHeight() {
        return diameter;
    }



    /**
     * Tests whether the point is empty, which means either no center or a diameter of zero or less..
     *
     * @return  Circle has a center and diameter > 0
     */
    @Override
    public boolean isEmpty() {
        return (center == null || diameter <= 0);
    }



    /**
     * Fits the circle as incircle into the center of given rectangle-coordinates.
     *
     * @param x     x-Coordinate of the upper left vertex of a rectangular frame.
     * @param y     y-Coordinate of the upper left vertex of a rectangular frame.
     * @param w     Width of a rectangular frame.
     * @param h     Height of a rectangular frame.
     */
    @Override
    public void setFrame(double x, double y, double w, double h) {
        this.center = new Point2D.Double(x + w/2, y + h/2);
        this.diameter = w < h ? w : h;
    }


    /**
     * Returns the smallest possible Rectangle2D to enclose the circle.
     *
     * @return  The rectangle enclosing the circle.
     */
    @Override
    public Rectangle2D getBounds2D() {
        double radius = diameter / 2;
        return new Rectangle2D.Double(center.getX()-radius, center.getY()-radius, diameter, diameter);
    }



    /**
     * Returns the center of the circle.
     *
     * @return  The center of the circle.
     */
    public Point2D getCenter() {
        return center;
    }



    /**
     * Returns the diameter of the circle.
     *
     * @return  The diameter of the circle.
     */
    public double getDiameter() {
        return diameter;
    }



    /**
     * Returns the x-coordinate of the center of the circle as integer-value.
     *
     * @return  The center-x-coordinate as integer.
     */
    public int getXAsInt() {
        return (int) Math.round(center.getX());
    }



    /**
     * Returns the y-coordinate of the center of the circle as integer-value.
     *
     * @return  The center-y-coordinate as integer.
     */
    public int getYAsInt() {
        return (int) Math.round(center.getY());
    }



    /**
     * Returns the diameter of the circle as integer-value.
     *
     * @return  The circle-diameter as integer.
     */
    public int getDiameterAsInt() {
        return (int) Math.round(diameter);
    }
}
