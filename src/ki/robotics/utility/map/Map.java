package ki.robotics.utility.map;

import ki.robotics.utility.pixyCam.PixyCam;
import ki.robotics.utility.svg.SVGParser;
import ki.robotics.utility.svg.svg_Circle;
import ki.robotics.utility.svg.svg_Line;
import ki.robotics.utility.svg.svg_Rectangle;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.*;
import java.lang.reflect.Array;
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
    private ArrayList<Landmark> landmarks;
    private final SVGParser svgParser;

    private final Polygon boundaries;


    /**
     * Constructor
     *
     * @param file  The SVG-File containing the information about walls.
     */
    public Map(File file, Polygon boundaries) {
        this.boundaries = boundaries;
        this.svgParser = new SVGParser(file);
        loadMapElements();
    }





    public Polygon getMapBoundaries() {
        return boundaries;
    }






    /**
     * Transforms svg-elements (lines, rectangles) into map-elements (walls and floor-tiles).
     */
    private void loadMapElements() {
        ArrayList<svg_Line> lines = svgParser.getLines();
        this.map = new ArrayList<>();
        for (svg_Line l : lines) {
            map.add(new Wall(l));
        }

        ArrayList<svg_Rectangle> rectangles = svgParser.getRectangles();
        this.floor = new ArrayList<>();
        for (svg_Rectangle r : rectangles) {
            floor.add(new FloorTile(r));
        }

        ArrayList<svg_Circle> circles = svgParser.getCircles();
        this.landmarks = new ArrayList<>();
        for (svg_Circle c : circles) {
            landmarks.add(new Landmark(c));
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
        for (Landmark l : landmarks) {
            l.paint(g2d, scaleFactor, xOffset, yOffset);
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
     * @param originX         X-Coordinate of the observer.
     * @param originY         Y-Coordinate of the observer.
     * @param angle     Direction of view.
     * @return          Distance to the closest obstacle.
     */
    public double getDistanceToObstacle(float originX, float originY, float angle) {
        // Maximum distance possible within the map-boundaries.
        double maxLineLength = Math.sqrt(Math.pow(svgParser.graphicHeight, 2) + Math.pow(svgParser.graphicWidth,2));
        Line2D.Double sensorBeam = makeLine(new Point2D.Double(originX, originY), angle, maxLineLength);


        double nearestObstacle = Double.MAX_VALUE;
        for (Wall w : map) {
            Line2D wall = new Line2D.Double(w.getStart(), w.getEnd());

            if (sensorBeam.intersectsLine(wall)) {
                Point2D intersectionPoint = getIntersectionPoint(wall, sensorBeam);
                if (wall.ptSegDist(intersectionPoint) < EPSILON) {
                    double distance = sensorBeam.getP1().distance(intersectionPoint);
                    nearestObstacle = distance < nearestObstacle ? distance : nearestObstacle;
                }
            }
        }
        return nearestObstacle;
    }



    public int[] getGeneralCameraQuery(float x, float y, float angle) {
        int numberOfAvailableSignatures = 7;
        int byteHoldingSignatureSizeInformation = 3;
        int signature = 1;
        int[] signatureA, signatureB;
        signatureA = getCameraSignatureQuery(x, y, angle, signature);
        for (int i = 2  ;  i <= numberOfAvailableSignatures  ;  i++) {
            signatureB = getCameraSignatureQuery(x, y, angle, i);
            if (signatureA[byteHoldingSignatureSizeInformation] < signatureB [byteHoldingSignatureSizeInformation]) {
                signatureA = signatureB;
                signature = i;
            }
        }
        if (signatureA[0] == 0) {
            return new int[]{0, 0, 0, 0, 0};
        } else {
            return new int[]{
                    signature, signatureA[1], 0, signatureA[3], 0
            };
        }
    }


    public int getCameraAngleQuery(float x, float y, float angle) {
        return 0;
    }


    public int[] getCameraSignatureQuery(float x, float y, float angle, int signature) {
        double imageHalfAngle = 32.5;
        String signatureString = "M" + signature;
        Point2D.Double origin = new Point2D.Double(x, y);
        // Maximum distance possible within the map-boundaries.
        double maxLineLength = Math.sqrt(Math.pow(svgParser.graphicHeight, 2) + Math.pow(svgParser.graphicWidth,2));
        Line2D.Double cameraCenterOfFocus = makeLine(origin, angle, maxLineLength);

        landmark_loop:
        for (Landmark l : landmarks) {
            if (! l.getId().equals(signatureString)) {
                continue;
            }
            Line2D.Double landmarkVector = new Line2D.Double(x, y, l.getCenter().x, l.getCenter().y);
            double angleToSensorBeam = angleBetween2Lines(landmarkVector, cameraCenterOfFocus);
            if (angleToSensorBeam >= imageHalfAngle) {
                continue;
            }

            for (Wall w : map) {
                Line2D.Double wall = new Line2D.Double(w.getStart(), w.getEnd());
                if (wall.intersectsLine(landmarkVector)) {
                    continue landmark_loop;
                }
            }

            Line2D.Double cameraLeftOuterRim = makeLine(origin, angle - imageHalfAngle, maxLineLength);
            Line2D.Double cameraRightOuterRim = makeLine(origin, angle + imageHalfAngle, maxLineLength);
            double angleToLeftOuterRim = angleBetween2Lines(cameraLeftOuterRim, landmarkVector);
            double angleToRightOuterRim = angleBetween2Lines(cameraRightOuterRim, landmarkVector);

            angleToSensorBeam = (angleToLeftOuterRim < angleToRightOuterRim) ? angleToSensorBeam : -angleToSensorBeam;
            int xCoordinateOfLandmarkCenter = PixyCam.angleDegreeToPixel(angleToSensorBeam);


            Line2D.Double imaginaryViewVector = makeLine(origin, angle, calculateLengthOfLine(landmarkVector));
            Line2D.Double imaginaryViewPane = makeLine(imaginaryViewVector.getP2(), angle+90, maxLineLength);
            Line2D.Double imaginaryScreen = new Line2D.Double(
                    getIntersectionPoint(cameraLeftOuterRim, imaginaryViewPane),
                    getIntersectionPoint(cameraRightOuterRim, imaginaryViewPane)
            );
            double lengthOfImaginaryScreen = calculateLengthOfLine(imaginaryScreen);
            double widthOfLandMark= l.getBound().getWidth();

            double relativeWidth = widthOfLandMark / lengthOfImaginaryScreen;
            int absoluteWidth = (int) Math.round(relativeWidth * 255);

            return new int[]{1,xCoordinateOfLandmarkCenter,0,absoluteWidth,0};
        }
        return new int[]{0,0,0,0,0};
    }

    public int[] getCameraColorCodeQuery(float x, float y, float angle, int coloCode) {
        return new int[]{0,0,0,0,0,0};
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

    private double angleBetween2Lines(Line2D line1, Line2D line2) {
        double line1abs = calculateLengthOfLine(line1);
        double line2abs = calculateLengthOfLine(line2);
        double vectorProduct = calculateVectorProductOfTwoLine(line1, line2);
        double angleInRadians = Math.acos(vectorProduct / (line1abs * line2abs));
        return Math.toDegrees(angleInRadians);
    }

    private double calculateLengthOfLine(Line2D line) {
        double lineDX = line.getX2()-line.getX1();
        double lineDY = line.getY2()-line.getY1();
        return Math.sqrt(Math.pow(lineDX, 2) + Math.pow(lineDY, 2));
    }

    private double calculateVectorProductOfTwoLine(Line2D line1, Line2D line2) {
        double line1DX = line1.getX2()-line1.getX1();
        double line1DY = line1.getY2()-line1.getY1();
        double line2DX = line2.getX2()-line2.getX1();
        double line2DY = line2.getY2()-line2.getY1();
        return (line1DX * line2DX) + (line1DY * line2DY);
    }

    private Line2D.Double makeLine(Point2D origin, double angle, double length) {
        return new Line2D.Double(
                origin.getX(),
                origin.getY(),
                Math.round(Math.cos(Math.toRadians(angle)) * length) + origin.getX(),
                Math.round(Math.sin(Math.toRadians(angle)) * length) + origin.getY()
        );
    }
}
