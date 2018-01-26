package ki.robotics.client.MCL;

/**
 * A simple 'book-keeping'-class for the sensor-feedback from the robot.
 *
 * @version 1.0 01/02/18
 */
public class SensorModel {

    private float distanceToLeft;
    private float distanceToCenter;
    private float distanceToRight;
    private int color;
    private float sensorHeadPosition;

    public float[] getAllDistances() {
        return new float[]{distanceToLeft, distanceToCenter, distanceToRight};
    }

    public float getDistanceToLeft() {
        return distanceToLeft;
    }

    public void setDistanceToLeft(float distanceToLeft) {
        this.distanceToLeft = distanceToLeft;
    }

    public float getDistanceToCenter() {
        return distanceToCenter;
    }

    public void setDistanceToCenter(float distanceToCenter) {
        this.distanceToCenter = distanceToCenter;
    }

    public float getDistanceToRight() {
        return distanceToRight;
    }

    public void setDistanceToRight(float distanceToRight) {
        this.distanceToRight = distanceToRight;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getSensorHeadPosition() {
        return sensorHeadPosition;
    }

    public void setSensorHeadPosition(float sensorHeadPosition) {
        this.sensorHeadPosition = sensorHeadPosition;
    }
}
