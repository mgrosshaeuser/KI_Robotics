package ki.robotics.datastructures;

import ki.robotics.utility.SVGParser;
import ki.robotics.utility.svg_shapes.Shape_Line;
import ki.robotics.utility.svg_shapes.Shape_Rectangle;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.ArrayList;



/**
 * Map-Representation including methods for parsing the map from an SVG-File, painting the map within a
 * graphical context and calculating distances within the map.
 *
 * @version 1.2, 12/28/17
 */
public class Map {

    private static final double EPSILON = 0.00001;

    private ArrayList<Wall> map;
    private ArrayList<FloorTile> floor;
    private SVGParser svgParser;




    /**
     * Constructor
     *
     * @param file  The SVG-File containing the information about walls.
     */
    public Map(File file) {
        this.svgParser = new SVGParser(file);
        loadMapElements();
    }



    /**
     * Transforms svg-elements (lines, rectangles) into map-elements (walls and floor-tiles).
     */
    private void loadMapElements() {
        ArrayList<Shape_Line> lines = svgParser.getLines();
        this.map = new ArrayList<>();
        for (Shape_Line l : lines) {
            map.add(new Wall(l));
        }

        ArrayList<Shape_Rectangle> rectangles = svgParser.getRectangles();
        this.floor = new ArrayList<>();
        for (Shape_Rectangle r : rectangles) {
            floor.add(new FloorTile(r));
        }
    }



    /**
     * Paints the map within a graphical-context (e.g. on a JPanel).
     *
     * @param g             The graphical context.
     * @param scaleFactor   Scale Factor for resizing the map according to the window-dimensions.
     * @param xOffset       X-Axis-Offset for horizontal centering of the map.
     * @param yOffset       Y-Axis-Offset for vertical centering of the map.
     */
    public void paint(Graphics g, int scaleFactor, int xOffset, int yOffset) {
        Graphics2D g2d = (Graphics2D) g;
        for (FloorTile f : floor) {
            f.paint(g2d, scaleFactor, xOffset, yOffset);
        }
        for (Wall w : map) {
            w.paint(g2d, scaleFactor, xOffset, yOffset);
        }
    }



    /**
     * Returns the minimum required width needed to display the map.
     *
     * @return  Minimum width for displaying the map.
     */
    public int getRequiredMinWidth() {
        return (int) Math.ceil(svgParser.graphicWidth);
    }



    /**
     * Returns the minimum required height needed to display the map.
     *
     * @return  Minimum height for displaying the map.
     */
    public int getRequiredMinHeight() {
        return  (int) Math.ceil(svgParser.graphicHeight);
    }



    /**
     * Returns the floor-color at a given point in the map.
     *
     * @param x     x-Coordinate of the point in question.
     * @param y     y-Coordinate of the point in question.
     * @return      The floor-color at point (x,y)
     */
    public int getColorAtPosition(int x, int y) {
        for (FloorTile f : floor) {
            if (f.containsPoint(x, y)) {
                return f.getColor();
            }
        }
        return -1;
    }



    /**
     * Finds the closest obstacle (e.g. wall) from given observation-coordinates (e.g. the location of
     * the robot) and an angle (e.g. the direction in which the distance-sensor points).
     *
     * @param x         X-Coordinate of the observer.
     * @param y         Y-Coordinate of the observer.
     * @param angle     Direction of view.
     * @return          Distance to the closest obstacle.
     */
    public double getDistanceToObstacle(float x, float y, float angle) {
        // Maximum distance possible within the map-boundaries.
        double maxLine = Math.sqrt(Math.pow(svgParser.graphicHeight, 2) + Math.pow(svgParser.graphicWidth,2));

        double nearestObstacle = Double.MAX_VALUE;
        for (Wall w : map) {
            Line2D wall = new Line2D.Double(w.getStart(), w.getEnd());

            Point2D sensorBeamStart = new Point2D.Double(x, y);
            Point2D sensorBeamEnd = new Point2D.Double(
                    Math.round(Math.cos(Math.toRadians(angle)) * maxLine) + sensorBeamStart.getX(),
                    Math.round(Math.sin(Math.toRadians(angle)) * maxLine) + sensorBeamStart.getY()
            );

            Line2D sensorBeam = new Line2D.Double(sensorBeamStart, sensorBeamEnd);

            if (sensorBeam.intersectsLine(wall)) {
                Point2D intersectionPoint = getIntersectionPoint(wall, sensorBeam);
                if (wall.ptSegDist(intersectionPoint) < EPSILON) {
                    double distance = sensorBeamStart.distance(intersectionPoint);
                    nearestObstacle = distance < nearestObstacle ? distance : nearestObstacle;
                }
            }
        }
        return nearestObstacle;
    }



    /**
     * Finds the point of intersection between two given lines.
     * ATTENTION: This Method does not perform a pre-check whether the lines intersect at all!
     *
     * @param wall      A line that, in the given context, represents a wall of the map.
     * @param beam      A line that, in the given context, represents a sensor-beam from the robot.
     * @return          The point of intersection between the two lines.
     */
    private Point2D getIntersectionPoint(Line2D wall, Line2D beam) {
        double mWall = calculateSlope(wall);
        double mBeam = calculateSlope(beam);

        double bWall = wall.getY1() - mWall * wall.getX1();
        double bBeam = beam.getY1() - mBeam * beam.getX1();

        double x, y;

        if (Math.abs(mWall- Double.MAX_VALUE) < EPSILON) {
            x = wall.getX1();
            y = mBeam * x + bBeam;
        } else if (Math.abs(mBeam - Double.MAX_VALUE) < EPSILON) {
            x = beam.getX1();
            y = mWall * x + bWall;
        } else {
            x = -(bWall - bBeam) / (mWall - mBeam);
            y = mWall * x + bWall;
        }

        return new Point2D.Double(x,y);
    }



    /**
     * Returns the slope of the given line or Double.MAX_VALUE if the line is parallel to the y-axis
     * (which means slope approaching infinity).
     *
     * @return  The slope of the given line or Double.NaN
     */
    private double calculateSlope(Line2D line) {
        if (Math.abs(line.getX1() - line.getX2()) < EPSILON) {
            return Double.MAX_VALUE;
        } else {
            return (line.getY2() - line.getY1()) / (line.getX2() - line.getX1());
        }
    }

}
