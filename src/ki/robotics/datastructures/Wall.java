package ki.robotics.datastructures;

import ki.robotics.rover.SimulatedRover;

import java.awt.*;


/**
 * Representation of a wall in a map.
 *
 * @version 1.0, 12/26/17
 */
public class Wall {
        private Point start;
        private Point end;

        private int color;

    /**
     * Constructor.
     * Creates a new Wall-instance from the string-representation of a line obeying the svg-xml-dialect.
     *
     * @param str_rep   The string-representation of a line.
     */
    public Wall(String[] str_rep) {
            this.color = Integer.parseInt(str_rep[0].substring(1));
            double x1 = Double.parseDouble(str_rep[1]);
            double x2 = Double.parseDouble(str_rep[2]);
            double y1 = Double.parseDouble(str_rep[3]);
            double y2 = Double.parseDouble(str_rep[4]);
            this.start = new Point((int) Math.round(x1), (int) Math.round(y1));
            this.end = new Point((int) Math.round(x2), (int) Math.round(y2));
        }



    /**
     * Returns the color of the wall as read from the map-file.
     *
     * @return  The color of the wall.
     */
    public int getColor() {
        return color;
    }



    /**
     * Returns the starting-point of this wall.
     *
     * @return  The starting-point of the wall.
     */
    public Point getStart() {
        return this.start;
    }



    /**
     * Returns the end-point of this wall.
     *
     * @return  The end-point of the wall.
     */
    public Point getEnd() {
        return this.end;
    }



    /**
     * Paints the wall in a given graphics-context using given scale-factor and offsets.
     *
     * @param g             The graphics-context to paint in.
     * @param scaleFactor   The scale-factor for adjusting length an position.
     * @param xOffset       The vertical offset.
     * @param yOffset       The horizontal offset.
     */
    public void paint(Graphics g, int scaleFactor, int xOffset, int yOffset) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawLine(
                start.x * scaleFactor + xOffset,
                start.y * scaleFactor + yOffset,
                end.x * scaleFactor + xOffset,
                end.y * scaleFactor + yOffset);
        }
}
