package ki.robotics.utility.svg_shapes;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


/**
 *
 * @version 1.0, 12/28/17
 */
public class Shape_Line extends Line2D.Double{
    private Point2D start;
    private Point2D end;
    private int stroke;
    private int fill;



    public Shape_Line(double x1, double y1, double x2, double y2) {
        this.start = new Point2D.Double(x1, y1);
        this.end = new Point2D.Double(x2, y2);
    }

    public Shape_Line(double x1, double y1, double x2, double y2, int stroke, int fill) {
        this(x1, y1, x2, y2);
        this.stroke = stroke;
        this.fill = fill;
    }


    @Override
    public double getX1() {
        return start.getX();
    }

    public int getX1AsInt() {
        return (int) Math.round(start.getX());
    }

    @Override
    public double getY1() {
        return start.getY();
    }

    public int getY1AsInt() {
        return (int) Math.round(start.getY());
    }

    @Override
    public Point2D getP1() {
        return start;
    }

    @Override
    public double getX2() {
        return end.getX();
    }

    public int getX2AsInt() {
        return (int) Math.round(end.getX());
    }

    @Override
    public double getY2() {
        return end.getY();
    }

    public int getY2AsInt() {
        return (int) Math.round(end.getY());
    }

    @Override
    public Point2D getP2() {
        return end;
    }

    @Override
    public void setLine(double x1, double y1, double x2, double y2) {
        this.start = new Point2D.Double(x1, y1);
        this.end = new Point2D.Double(x2, y2);
    }

    @Override
    public Rectangle2D getBounds2D() {
        double x = start.getX() < end.getX() ? start.getX() : end.getX();
        double y = start.getY() < end.getY() ? start.getY() : end.getY();
        double width = Math.abs((start.getX() - end.getX()));
        double height = Math.abs((start.getY() - end.getY()));
        return new Rectangle2D.Double(x, y, width, height);
    }


    public int getStroke() {
        return stroke;
    }

    public int getFill() {
        return  fill;
    }

    public double getLength() {
        double xSqr = Math.pow((start.getX() - end.getX()), 2);
        double ySqr = Math.pow((start.getY() - end.getY()), 2);
        return Math.sqrt(xSqr + ySqr);
    }


    @Override
    public String toString() {
        return "x1: " + start.getX() + ", y1: " + start.getY()+ ", x2: " + end.getX() + ", y2: " + end.getY();
    }
}
