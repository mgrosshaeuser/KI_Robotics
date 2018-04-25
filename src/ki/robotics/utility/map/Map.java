package ki.robotics.utility.map;

import java.awt.*;
import java.awt.geom.Point2D;

public interface Map {
    Polygon getOperatingRange();

    void paint(Graphics g, int scaleFactor, int xOffset, int yOffset);

    int getMinWidthForMapDisplay();

    int getMinHeightForMapDisplay();

    int getFloorColorAt(Point2D observationSpot);

    double getDistanceToNearestObstacle(Point2D position, double viewingDirection);

    //TODO Refactor.
    int[] getGeneralCameraQuery(float x, float y, float angle);

    //TODO Implement.
    int getCameraAngleQuery(float x, float y, float angle);

    //TODO Implement.
    int[] getCameraColorCodeQuery(float x, float y, float angle, int coloCode);

    //TODO REFACTOR!!!
    int[] getCameraSignatureQuery(float x, float y, float angle, int signature);
}
