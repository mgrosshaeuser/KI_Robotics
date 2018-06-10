package ki.robotics.utility.map;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public interface Map {
    String getMapKey();

    Polygon getOperatingRange();

    Line2D getBaseLine();

    void paint(Graphics g, int scaleFactor, int xOffset, int yOffset);

    int getMinWidthForMapDisplay();

    int getMinHeightForMapDisplay();

    int getFloorColorAt(Point2D observationSpot);

    double getDistanceToNearestObstacle(Point2D position, double viewingDirection);

    int[] getGeneralCameraQuery(double x, double y, double angle);

    int getCameraAngleQuery(double x, double y, double angle);

    int[] getCameraColorCodeQuery(double x, double y, double angle, int coloCode);

    int[] getCameraSignatureQuery(double x, double y, double angle, int signature);
}
