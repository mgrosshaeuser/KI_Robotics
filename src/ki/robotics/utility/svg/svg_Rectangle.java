package ki.robotics.utility.svg;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;



/**
 * Representation of a SVG-rect-Element.
 *
 * @version 1.0, 12/28/17
 */
public class svg_Rectangle extends Rectangle2D.Double {
    private Point2D.Double topLeft;
    private double width;
    private double height;
    private int stroke;
    private int fill;


    public svg_Rectangle(double x1, double y1, double width, double height) {
        this.topLeft = new Point2D.Double(x1,y1);
        this.width = width;
        this.height = height;
    }

    public svg_Rectangle(double x1, double y1, double width, double height, int stroke, int fill) {
        this(x1, y1, width, height);
        this.stroke = stroke;
        this.fill = fill;
    }


    @Override
    public double getX() { return topLeft.getX(); }

    public int getXAsInt() {
        return (int) Math.round(topLeft.getX());
    }

    @Override
    public double getY() { return topLeft.getY(); }

    public int getYAsInt() {
        return (int) Math.round(topLeft.getY());
    }

    @Override
    public void setRect(double x, double y, double w, double h) {
        this.topLeft = new Point2D.Double(x, y);
    }

    @Override
    public double getWidth() { return this.width; }

    @Override
    public double getHeight() { return this.height; }

    public int getWidthAsInt() { return (int) Math.round(width); }

    public int getHeightAsInt() { return (int) Math.round(height); }


    public Point2D getTopLeft() {
        return this.topLeft;
    }

    public int getStroke() {
        return stroke;
    }

    public int getFill() {
        return fill;
    }
}
