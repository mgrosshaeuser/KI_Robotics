package ki.robotics.utility.map.mapElements;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;


/**
 * Representation of linear map-elements matching a SVG-"line"-elements.
 */
public class Line extends Line2D.Double{
    private static final double EPSILON = 0.00001;

    private int stroke = Color.BLACK.getRGB();


    /**
     * Constructs and initializes a Line with the specified coordinates.
     *
     * @param startPoint the start-point of the newly constructed Line
     * @param endPoint the end-point of the newly constructed Line
     */
    public Line(Point2D startPoint, Point2D endPoint) {
        super(startPoint, endPoint);
    }



    /**
     * Constructs and initializes a Line with the specified origin-coordinates, direction and length.
     *
     * @param origin the start-point of the newly constructed Line
     * @param viewingDirection the direction of the newly constructed Line
     * @param length the length of the newly constructed Line
     */
    public Line(Point2D origin, double viewingDirection, double length) {
        double endX = Math.round(Math.cos(Math.toRadians(viewingDirection)) * length) + origin.getX();
        double endY = Math.round(Math.sin(Math.toRadians(viewingDirection)) * length) + origin.getY();
        Point2D endpoint = new Point2D.Double(endX, endY);
        this.setLine(origin, endpoint);
    }



    /**
     * Returns the stroke-color of this shape as integer, suitable for the
     * constructor java.awt.Color(int rgb)
     *
     * @return the stroke-color as integer (rgb)
     */
    public int getStroke() {
        return this.stroke;
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
     * Returns the length of this Line in double precision.
     *
     * @return the length of this Line
     */
    public double getLength() {
        double deltaX = this.getX2()-this.getX1();
        double deltaY = this.getY2()-this.getY1();
        return Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
    }



    /**
     * Returns the slope of this Line in double precision.
     *
     * @return the slope of this Line
     */
    public double getSlope() {
        boolean lineIsParallelToYAxis = Math.abs(this.getX1() - this.getX2()) < EPSILON;
        if (lineIsParallelToYAxis) {
            return java.lang.Double.MAX_VALUE;
        } else {
            return (this.getY2() - this.getY1()) / (this.getX2() - this.getX1());
        }
    }



    /**
     * Returns the angle between this Line and the specified in double precision degrees.
     *
     * @param line the specified Line
     * @return the angle between this Line and the specified
     */
    public double getAngleTo(Line line) {
        double crossProduct = getCrossProductWith(line);
        double angleInRadians = Math.acos(crossProduct / (this.getLength() * line.getLength()));
        return Math.toDegrees(angleInRadians);
    }



    /**
     * Returns the cross-product (vector-product) of this Line with the specified in double precision.
     *
     * @param line the specified Line
     * @return the cross-product of this Line and the specified
     */
    public double getCrossProductWith(Line line) {
        double lineOneDeltaX = this.getX2()-this.getX1();
        double lineOneDeltaY = this.getY2()-this.getY1();
        double lineTwoDeltaX = line.getX2()-line.getX1();
        double lineTwoDeltaY = line.getY2()-line.getY1();
        return (lineOneDeltaX * lineTwoDeltaX) + (lineOneDeltaY * lineTwoDeltaY);
    }



    /**
     * Returns the intersection-point between this Line and the specified. If the lines do not
     * intersect, an UnsupportedOperationException is thrown.
     *
     * @param line the specified Line
     * @return the intersection-point between this Line and the specified
     * @throws UnsupportedOperationException in case the lines do not intersect
     */
    public Point2D getIntersectionPointWith(Line line) throws UnsupportedOperationException{
        if (! this.intersectsLine(line)) {
            throw new UnsupportedOperationException("No intersection-point with given line");
        }

        double slopeOne = this.getSlope();
        double slopeTwo = line.getSlope();

        double xOffsetOne = this.getY1() - slopeOne * this.getX1();
        double xOffsetTwo = line.getY1() - slopeTwo * line.getX1();

        double x, y;

        if (Math.abs(slopeOne - java.lang.Double.MAX_VALUE) < EPSILON) {
            x = this.getX1();
            y = slopeTwo * x + xOffsetTwo;
        } else if (Math.abs(slopeTwo - java.lang.Double.MAX_VALUE) < EPSILON) {
            x = line.getX1();
            y = slopeOne * x + xOffsetOne;
        } else {
            x = -(xOffsetOne - xOffsetTwo) / (slopeOne - slopeTwo);
            y = slopeOne * x + xOffsetOne;
        }

        return new Point2D.Double(x,y);
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
        int startX = (int)Math.round(this.getX1()) * scaleFactor + xOffset;
        int startY = (int)Math.round(this.getY1()) * scaleFactor + yOffset;
        int endX = (int)Math.round(this.getX2()) * scaleFactor + xOffset;
        int endY = (int)Math.round(this.getY2()) * scaleFactor + yOffset;

        g2d.setColor(new Color(stroke));
        g2d.drawLine(startX, startY, endX, endY);
    }
}
