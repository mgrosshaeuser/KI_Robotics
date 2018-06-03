package ki.robotics.utility.map;

import ki.robotics.utility.map.mapElements.Circle;
import ki.robotics.utility.map.mapElements.Rectangle;
import ki.robotics.utility.map.mapElements.Line;
import ki.robotics.utility.pixyCam.PixyCam;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;


/**
 * Representation of a map.
 */
public class MapImpl implements Map {
    private static final double EPSILON = 0.00001;

    private double width;
    private double height;

    private Polygon operatingRange;

    private ArrayList<Line> walls;
    private ArrayList<Rectangle> floorTiles;
    private ArrayList<Circle> landmarks;

    private String mapKey;



    /**
     * Constructs and initializes a Map with the specified walls, floor-tiles and landmarks.
     *
     * @param mapKey the key (or name) to identify this Map
     * @param walls the walls of the newly constructed Map
     * @param floorTiles the floor-tiles of the newly constructed Map
     * @param landmarks the landmarks of the newly constructed Map
     */
    MapImpl(String mapKey, ArrayList<Line> walls, ArrayList<Rectangle> floorTiles, ArrayList<Circle> landmarks) {
        this.mapKey = mapKey;
        this.walls = walls;
        this.floorTiles = floorTiles;
        this.landmarks = landmarks;
        updateLandmarkWallAreaOccupancy();
    }



    /**
     * One-time setting of the map-width. If the width of this Map is already set, an
     * UnsupportedOperationException is thrown.
     *
     * @param width the width of this Map
     * @exception UnsupportedOperationException in case the width is already set
     */
    void setWidth(double width) throws UnsupportedOperationException{
        if (this.width == 0) {
            this.width = width;
        } else {
            throw new UnsupportedOperationException("Illegal Modification of Map-Property (Width)");
        }
    }



    /**
     * One-time setting of the map-height. If the height of this Map is already set, an
     * UnsupportedOperationException is thrown.
     *
     * @param height the height of this Map
     * @exception UnsupportedOperationException in case the height is already set
     */
    void setHeight(double height) {
        if (this.height == 0) {
            this.height = height;
        } else {
            throw new UnsupportedOperationException("Illegal Modification of Map-Property (Height)");
        }
    }


    /**
     * Returns the map-key (of type String) of this Map.
     *
     * @return The map-key of this Map
     */
    @Override
    public String getMapKey() { return this.mapKey; }


    /**
     * Sets the map-key (of type String) of this Map
     *
     * @param mapKey The new map-key for this Map
     */
    void setMapKey(String mapKey) { this.mapKey = mapKey; }


    /**
     * Returns the operating-range within this Map as polygon.
     *
     * @return the operating range as polygon
     */
    @Override
    public Polygon getOperatingRange() {
        return operatingRange;
    }



    /**
     * Sets the operating-range within this Map as polygon.
     *
     * @param operatingRange the operating-range within this Map
     */
    void setOperatingRange(Polygon operatingRange) {
        this.operatingRange = operatingRange;
    }






    /**
     * Paints the walls within a graphical-context (e.g. on a JPanel).
     *
     * @param g             The graphical context.
     * @param scaleFactor   Scale Factor for resizing the walls according to the window-dimensions.
     * @param xOffset       X-Axis-Offset for horizontal centering of the walls.
     * @param yOffset       Y-Axis-Offset for vertical centering of the walls.
     */
    @Override
    public void paint(Graphics g, int scaleFactor, int xOffset, int yOffset) {
        Graphics2D g2d = (Graphics2D) g;
        for (Rectangle f : floorTiles) {
            f.paint(g2d, scaleFactor, xOffset, yOffset);
        }
        for (Circle l : landmarks) {
            l.paint(g2d, scaleFactor, xOffset, yOffset);
        }
        for (Line w : walls) {
            w.paint(g2d, scaleFactor, xOffset, yOffset);
        }
    }



    /**
     * Returns the minimum width, required to display the walls.
     *
     * @return  Minimum width for displaying the walls.
     */
    @Override
    public int getMinWidthForMapDisplay() {
        return (int) Math.ceil(width);
    }



    /**
     * Returns the minimum height, required to display the walls.
     *
     * @return  Minimum height for displaying the walls.
     */
    @Override
    public int getMinHeightForMapDisplay() {
        return  (int) Math.ceil(height);
    }



    /**
     * Returns the color of the floor-tile at the specified observation-spot or -1 in case there is no
     * floor-tile at the specified spot.
     *
     * @param observationSpot the specified observation-point
     * @return the color at the specified spot
     */
    @Override
    public int getFloorColorAt(Point2D observationSpot) {
        for (Rectangle f : floorTiles) {
            Rectangle2D bounds = f.getBounds2D();
            if (bounds.contains(observationSpot)) {
                return f.getFill();
            }
        }
        return -1;
    }



    /**
     * Returns the distance to the nearest obstacle from a specified position in a specified direction.
     *
     * @param position the specified origin of the distance-measurement
     * @param viewingDirection the specified direction of measurement
     * @return the distance to the nearest obstacle
     */
    @Override
    public double getDistanceToNearestObstacle(Point2D position, double viewingDirection) {
        Line sensorBeam = getLongestPossibleLineInMap(position, viewingDirection);
        double distanceToNearestObstacle = Double.MAX_VALUE;
        for (Line wall : walls) {
            try {
                Point2D intersectionPoint = wall.getIntersectionPointWith(sensorBeam);
                boolean intersectionPointIsWithinWallBoundaries = wall.ptSegDist(intersectionPoint) < EPSILON;
                if (intersectionPointIsWithinWallBoundaries) {
                    double distance = position.distance(intersectionPoint);
                    boolean currentObstacleIsCloserThanAnyOtherYet = distance < distanceToNearestObstacle;
                    if (currentObstacleIsCloserThanAnyOtherYet) {
                        distanceToNearestObstacle = distance;
                    }
                }
            } catch (UnsupportedOperationException e) {
                // Exception is thrown if lines do not intersect. This is an implicit 'continue' for the loop.
            }
        }
        return distanceToNearestObstacle;
    }






    //TODO Refactor.
    @Override
    public int[] getGeneralCameraQuery(double x, double y, double angle) {
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


    //TODO Implement.
    @Override
    public int getCameraAngleQuery(double x, double y, double angle) {
        return 0;
    }

    //TODO Implement.
    @Override
    public int[] getCameraColorCodeQuery(double x, double y, double angle, int coloCode) {
        return new int[]{0,0,0,0,0,0};
    }


    //TODO REFACTOR!!!
    @Override
    public int[] getCameraSignatureQuery(double x, double y, double angle, int signature) {
        double imageFullAngle = 75;
        double imageHalfAngle = imageFullAngle / 2;
        int pixelInFullAngle = 255;
        String signatureString = "M" + signature;
        Point2D.Double origin = new Point2D.Double(x, y);
        Line cameraCenterOfFocus = getLongestPossibleLineInMap(origin, angle);

        landmark_loop:
        for (Circle landmark : landmarks) {
            if (! landmark.getId().equals(signatureString)) {
                continue;
            }
            Line landmarkVector = new Line (new Point2D.Double(x, y), new Point2D.Double(landmark.getCenterX(), landmark.getCenterY()));
            double angleToCameraCenterOfFocus = landmarkVector.getAngleTo(cameraCenterOfFocus);
            if (angleToCameraCenterOfFocus >= imageHalfAngle) {
                continue;
            }

            for (Line wall : walls) {
                if (wall.intersectsLine(landmarkVector)) {
                    continue landmark_loop;
                }
            }

            Line cameraLeftOuterRim = getLongestPossibleLineInMap(origin, angle - imageHalfAngle);
            Line cameraRightOuterRim = getLongestPossibleLineInMap(origin, angle + imageHalfAngle);
            double angleToLeftOuterRim = landmarkVector.getAngleTo(cameraLeftOuterRim);
            double angleToRightOuterRim = landmarkVector.getAngleTo(cameraRightOuterRim);

            angleToCameraCenterOfFocus = (angleToLeftOuterRim < angleToRightOuterRim) ? angleToCameraCenterOfFocus : -angleToCameraCenterOfFocus;
            int xCoordinateOfLandmarkCenter = PixyCam.angleDegreeToPixel(angleToCameraCenterOfFocus);

            Line toWallP1 = new Line(origin, landmark.getOccupiedWallArea().getP1());
            Line toWallP2 = new Line(origin, landmark.getOccupiedWallArea().getP2());
            double perceivedAngle = toWallP1.getAngleTo(toWallP2);
            double angleBetweenCameraCenterOfFocusAndP1 = cameraCenterOfFocus.getAngleTo(toWallP1);
            double angleBetweenCameraCenterOfFocusAndP2 = cameraCenterOfFocus.getAngleTo(toWallP2);
            if (angleBetweenCameraCenterOfFocusAndP1 > imageHalfAngle) {
                perceivedAngle -= (angleBetweenCameraCenterOfFocusAndP1 - imageHalfAngle);
            }
            if (angleBetweenCameraCenterOfFocusAndP2 > imageHalfAngle) {
                perceivedAngle -= (angleBetweenCameraCenterOfFocusAndP2 - imageHalfAngle);
            }

            int absoluteWidth = (int) Math.round(perceivedAngle / imageFullAngle * pixelInFullAngle);

            return new int[]{1,xCoordinateOfLandmarkCenter,0,absoluteWidth,0};
        }
        return new int[]{0,0,0,0,0};
    }



    /**
     * Returns the longest possible Line in this Map, extending from the specified position in the specified direction.
     *
     * @param position the origin of the newly constructed Line
     * @param viewingDirection the direction of the newly constructed Line
     * @return the newly constructed Line
     */
    private Line getLongestPossibleLineInMap(Point2D position, double viewingDirection) {
        double maxPossibleDistanceInMap = Math.sqrt(Math.pow(height, 2) + Math.pow(width, 2));
        return new Line(position, viewingDirection, maxPossibleDistanceInMap);
    }



    /**
     * Checks for intersections of walls and landmarks.
     * For each intersection updateWallAreaOccupancyForLandmarkAndWallCombination is called to update
     * landmarks regarding their occupancy of wall-area.
     */
    private void updateLandmarkWallAreaOccupancy() {
        for (Circle landmark : landmarks) {
            for (Line wall : walls) {
                if (wall.intersects(landmark.getBounds())) {
                    updateWallAreaOccupancyForLandmarkAndWallCombination(landmark, wall);
                }
            }
        }
    }



    /**
     * Provides distinct handling of intersection depending on the orientation of the wall.
     *
     * @param landmark the landmark intersecting with the wall
     * @param wall the wall intersecting with the landmark
     */
    private void updateWallAreaOccupancyForLandmarkAndWallCombination(Circle landmark, Line wall) {
        boolean wallIsParallelToAxisOfAbscissae = Math.abs(wall.getSlope() - Double.MAX_VALUE) < EPSILON ;
        if (wallIsParallelToAxisOfAbscissae) {
            updateWallAreaOccupancyForLandmarksOnWallsParallelToAxisOfAbscissae(landmark, wall);
        } else {
            updateWallAreaOccupancyForLandmarksOnWallsParallelToAxisOfOrdinates(landmark, wall);
        }
    }



    /**
     * Handles landmark-wall-intersection on walls parallel to the axis of abscissae.
     *
     * @param landmark the landmark intersecting with the wall
     * @param wall the wall intersecting with the landmark
     */
    private void updateWallAreaOccupancyForLandmarksOnWallsParallelToAxisOfAbscissae(Circle landmark, Line wall) {
        double radiusOfLandmark = landmark.getDiameter() / 2;
        double landmarkY = landmark.getCenterY();
        boolean wallRunsFromSouthToNorth = wall.getY1() < wall.getY2();
        if (wallRunsFromSouthToNorth) {
            Point2D.Double upperRim = new Point2D.Double(wall.getX1(), landmarkY - radiusOfLandmark);
            Point2D.Double lowerRim = new Point2D.Double(wall.getX2(), landmarkY +  radiusOfLandmark);
            landmark.setOccupiedWallArea(upperRim, lowerRim);
        } else {
            Point2D.Double upperRim = new Point2D.Double(wall.getX1(), landmarkY + radiusOfLandmark);
            Point2D.Double lowerRim = new Point2D.Double(wall.getX2(), landmarkY -  radiusOfLandmark);
            landmark.setOccupiedWallArea(upperRim, lowerRim);
        }
    }



    /**
     * Handles landmark-wall-intersection on walls parallel to the axis of ordinates.
     *
     * @param landmark the landmark intersecting with the wall
     * @param wall the wall intersecting with the landmark
     */
    private void updateWallAreaOccupancyForLandmarksOnWallsParallelToAxisOfOrdinates(Circle landmark, Line wall) {
        double radiusOfLandmark = landmark.getDiameter() / 2;
        double landmarkX = landmark.getCenterX();
        boolean wallRunsFromWestToEast = wall.getX1() < wall.getX2();
        if (wallRunsFromWestToEast) {
            Point2D.Double leftRim = new Point2D.Double(landmarkX - radiusOfLandmark, wall.getY1());
            Point2D.Double rightRim = new Point2D.Double(landmarkX + radiusOfLandmark, wall.getY1());
            landmark.setOccupiedWallArea(leftRim, rightRim);
        } else {
            Point2D.Double leftRim = new Point2D.Double(landmarkX + radiusOfLandmark, wall.getY1());
            Point2D.Double rightRim = new Point2D.Double(landmarkX - radiusOfLandmark, wall.getY1());
            landmark.setOccupiedWallArea(leftRim, rightRim);
        }
    }
}
