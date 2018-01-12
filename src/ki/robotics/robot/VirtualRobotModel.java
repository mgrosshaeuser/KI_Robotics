package ki.robotics.robot;

import ki.robotics.utility.map.Map;
import lejos.robotics.navigation.Pose;

public abstract class VirtualRobotModel implements Robot {

    Pose pose;
    int sensorHeadPosition = 0;
    float bumper = 5f;
    Map map;


    @Override
    public double botTravelForward(double distance) {
        int temp = sensorHeadPosition;
        if (distance > 0) {
            turnSensor(0);
            distance = (measureDistance() >= distance + bumper) ? distance : measureDistance() - bumper;
        } else {
            turnSensor(180);
            distance = (measureDistance() >= Math.abs(distance) + bumper) ? distance : -(measureDistance() - bumper);
        }

        float dx = (float) (Math.cos(Math.toRadians(pose.getHeading())) * distance);
        float dy = (float) (Math.sin(Math.toRadians(pose.getHeading())) * distance) * -1;

        translate(dx, dy);
        turnSensor(temp);
        return distance;
    }


    @Override
    public double botTravelBackward(double distance) {
        return -(botTravelForward(distance * -1));
    }


    @Override
    public boolean botTurnLeft(double degree) {
        turnFull((int) Math.round(degree));
        return true;
    }


    @Override
    public boolean botTurnRight(double degree) {
        turnFull((int) Math.round(degree) * -1);
        return true;
    }


    @Override
    public boolean sensorHeadTurnLeft(double position) {
        turnSensor((int) Math.round(position));
        return true;
    }


    @Override
    public boolean sensorHeadTurnRight(double position) {
        turnSensor((int) Math.round(position) * -1);
        return true;
    }


    @Override
    public boolean sensorHeadReset() {
        turnSensor(0);
        return true;
    }


    @Override
    public int measureColor() {
        return map.getColorAtPosition(Math.round(pose.getX()), Math.round(pose.getY()));
    }


    @Override
    public double measureDistance() {
        return map.getDistanceToObstacle(pose.getX(), pose.getY(), 360 - pose.getHeading() - sensorHeadPosition);
    }


    @Override
    public double[] ultrasonicThreeWayScan() {
        int temp = sensorHeadPosition;
        turnSensor(90);
        double a = measureDistance();
        turnSensor(0);
        double b = measureDistance();
        turnSensor(-90);
        double c = measureDistance();
        turnSensor(temp);
        return new double[]{a, b, c};
    }


    @Override
    public Pose getPose() {
        return pose;
    }

    @Override
    public byte[] cameraGeneralQuery() {
        return new byte[]{0};
    }

    @Override
    public byte[][] cameraSignatureQuery() {
        return new byte[][]{{0}};
    }

    @Override
    public byte[] cameraColorCodeQuery(int color) {
        return new byte[]{0};
    }

    @Override
    public byte[] cameraAngleQuery() {
        return new byte[]{0};
    }

    public byte[] queryCamera() {
        return new byte[]{0};
    }

    public int getSensorHeadPosition() {
        return sensorHeadPosition;
    }

    public void setSensorHeadPosition(int position) { this.sensorHeadPosition = position; }

    abstract void translate(float dx, float dy);
    abstract void turnFull(int degrees);
    abstract void turnSensor(int position);


}
