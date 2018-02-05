package ki.robotics.utility.map;

import ki.robotics.utility.svg.svg_Line;

import java.awt.*;
import java.awt.geom.Line2D;


/**
 * Representation of a wall in a map.
 *
 * @version 1.1, 12/28/17
 */
class Wall{
    private final svg_Line line;


    /**
     * Constructor.
     *
     * @param x1        x-Coordinate of the starting-point of the wall.
     * @param y1        y-Coordinate of the starting-point of the wall.
     * @param x2        x-Coordinate of the end-point of the wall.
     * @param y2        x-Coordinate of the end-point of the wall.
     * @param stroke    Color of the stroke as integer-RGB-value.
     * @param fill      Color of the fill as integer-RGB-value.
     */
    private Wall(double x1, double y1, double x2, double y2, int stroke, int fill) {
        this.line = new svg_Line(x1, y1, x2, y2, stroke, fill);
    }



    /**
     * Constructor.
     *
     * @param line      An instance of svg_Line from the svg.
     */
    public Wall(svg_Line line) {
        this(line.getX1(), line.getY1(), line.getX2(), line.getY2(), line.getStroke(), line.getFill());
    }



    /**
     * Returns the starting-point of this wall in integer-coordinates.
     *
     * @return  The starting-point of the wall.
     */
    public Point getStart() {
        return new Point(
                (int) Math.round(line.getP1().getX()),
                (int) Math.round(line.getP1().getY())
        );
    }


    /**
     * Returns the end-point of this wall in integer-coordinates.
     *
     * @return  The end-point of the wall.
     */
    public Point getEnd() {
        return new Point(
                (int) Math.round(line.getP2().getX()),
                (int) Math.round(line.getP2().getY())
        );
    }


    public Line2D.Double getAsLine2D() {
        return new Line2D.Double(this.getStart(), this.getEnd());
    }

    /**
     * Paints the wall in a given graphics-context using given scale-factor and offsets.
     *
     * @param g             The graphics-context to paint in.
     * @param scaleFactor   The scale-factor for adjusting length and position.
     * @param xOffset       The vertical offset.
     * @param yOffset       The horizontal offset.
     */
    public void paint(Graphics g, int scaleFactor, int xOffset, int yOffset) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(line.getStroke()));
        g2d.drawLine(
                line.getX1AsInt() * scaleFactor + xOffset,
                line.getY1AsInt() * scaleFactor + yOffset,
                line.getX2AsInt() * scaleFactor + xOffset,
                line.getY2AsInt() * scaleFactor + yOffset
        );
    }

}
