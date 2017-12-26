package ki.robotics.datastructures;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Map-Representation including methods for parsing the map from an SVG-File, painting the map within a
 * graphical context and calculating distances within the map.
 *
 * @version 1.0, 12/26/17
 */
public class Map {

    public static final double EPSILON = 0.000001;

    private ArrayList<Wall> map = new ArrayList<>();
    private int requieredMinWidht;
    private int requieredMinHeight;



    /**
     * Constructor
     *
     * @param file  The SVG-File containing the information about walls.
     */
    public Map(File file) {
        this.map = parseSVGFile(file);
        requieredMinWidht = calculateRequiredMinWidth();
        requieredMinHeight = calculateRequiredMinHeight();
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
        g2d.setColor(Color.LIGHT_GRAY);
        for (Wall w : map) {
            w.paint(g2d, scaleFactor, xOffset, yOffset);
        }
    }



    /**
     * Returns the minimum required width necessary to display the map.
     *
     * @return  Minimum width for displaying the map.
     */
    public int getRequieredMinWidht() {
        return this.requieredMinWidht;
    }



    /**
     * Returns the minimum required height necessary to display the map.
     *
     * @return  Minimum height for displaying the map.
     */
    public int getRequieredMinHeight() {
        return  this.requieredMinHeight;
    }



    /**
     * Calculates the minimum required width necessary to display the map, as the difference between the
     * greatest and smallest value on the x-axis. The calculation is performed once when a map-file is
     * loaded and the result is stored locally to speed up access.
     *
     * @return  Minimum width for displaying the map.
     */
    private int calculateRequiredMinWidth() {
        int maxX = 0;
        for (Wall w : map) {
            maxX = w.getStart().x > maxX ? w.getStart().x : maxX;
            maxX = w.getEnd().x > maxX ? w.getEnd().x : maxX;
        }
        return maxX;
    }



    /**
     * Calculates the minimum required height necessary to display the map, as the difference between the
     * greatest and smallest value on the y-axis. The calculation is performed once when a map-file is
     * loaded and the result is stored locally to speed up access.
     *
     * @return  Minimum width for displaying the map.
     */
    private int calculateRequiredMinHeight() {
        int maxY = 0;
        for (Wall w : map) {
            maxY = w.getStart().y > maxY ? w.getStart().y : maxY;
            maxY = w.getEnd().y > maxY ? w.getEnd().y : maxY;
        }
        return maxY;
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
        double maxLine = Math.sqrt(Math.pow(requieredMinHeight, 2) + Math.pow(requieredMinWidht,2));

        double nearestObstacle = Double.MAX_VALUE;
        for (Wall w : map) {
            Line2D wall = new Line2D.Double(w.getStart(), w.getEnd());

            Point2D sensorBeamStart = new Point2D.Double(x, y);
            Point2D sensorBeamEnd = new Point2D.Double(
                    Math.cos(Math.toRadians(angle)) * maxLine,
                    Math.sin(Math.toRadians(angle)) * maxLine
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
     * @param wall          A line that, in the given context, represents a wall of the map.
     * @param sensorBeam    A line that, in the given context, represents a sensor-beam from the robot.
     * @return              The point of intersection between the two lines.
     */
    private Point2D getIntersectionPoint(Line2D wall, Line2D sensorBeam) {
        double mWall, mBeam;
        double x, y;

        if (Math.abs(wall.getX1() - wall.getX2()) < EPSILON) {
            mWall = Double.MAX_VALUE;
        } else {
            mWall = (wall.getY1() - wall.getY2()) / (wall.getX1() - wall.getX2());
        }
        if (Math.abs(sensorBeam.getX1() - sensorBeam.getX2()) < EPSILON) {
            mBeam = Double.MAX_VALUE;
        } else {
            mBeam = (sensorBeam.getY1() - sensorBeam.getY2()) / (sensorBeam.getX1() - sensorBeam.getX2());
        }

        double bWall = wall.getY1() - mWall * wall.getX1();
        double bBeam = sensorBeam.getY1() - mBeam * sensorBeam.getX1();

        if (Math.abs(Double.MAX_VALUE - mWall) < EPSILON) {
            x = wall.getX1();
            y = mBeam * x + bBeam;
        } else if (Math.abs((Double.MAX_VALUE - mBeam)) < EPSILON) {
            x = sensorBeam.getX1();
            y = mWall * x + bWall;
        } else {
            x = -(bWall - bBeam) / (mWall - mBeam);
            y = mWall * x + bWall;
        }

        return new Point2D.Double(x,y);
    }


    /**
     * Parses the information from a given SVG-File.
     * At the moment, only line-elements of the svg-xml-dialect are processed.
     *
     * @param file  The SVG-File to be parsed.
     * @return      The line-elements from the SVG-File as List of Wall-instances.
     */
    private ArrayList<Wall> parseSVGFile(File file) {
        Pattern pattern = Pattern.compile("\"(.+?)\"");

        ArrayList<Wall> walls = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (! line.startsWith("<line")) {
                    continue;
                }
                Matcher matcher = pattern.matcher(line);

                String[] tmp = new String[5];
                int i = 0;
                while (matcher.find()) {
                    tmp[i] = matcher.group(1);
                    i++;
                }
                walls.add(new Wall(tmp));

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return walls;
    }
}
