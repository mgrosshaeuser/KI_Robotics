package ki.robotics.server.robot;

import ki.robotics.server.Robot;
import ki.robotics.utility.map.Map;
import lejos.robotics.navigation.Pose;

import java.awt.geom.Point2D;


public abstract class RobotImplVirtualRobot implements Robot {
    protected Pose pose;
    protected int sensorHeadPosition = 0;
    protected final float bumper = 5f;
    protected Map map;


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
        return map.getFloorColorAt(new Point2D.Double(pose.getX(), pose.getY()));
    }


    @Override
    public double measureDistance() {
        return map.getDistanceToNearestObstacle(new Point2D.Double(pose.getX(), pose.getY()), 360 - pose.getHeading() - sensorHeadPosition);
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
    public int[] cameraGeneralQuery() {
        return map.getGeneralCameraQuery(pose.getX(), pose.getY(), 360 - pose.getHeading());
    }

    @Override
    public int[] cameraSignatureQuery(int signature) {
        return map.getCameraSignatureQuery(pose.getX(), pose.getY(), 360-pose.getHeading(), signature);
    }

    @Override
    public int[][] cameraAllSignaturesQuery() {
        int numberOfAvailableSignatures = 7;
        int[][] result = new int[numberOfAvailableSignatures][];
        for (int i = 0   ;   i < numberOfAvailableSignatures   ;   i++) {
            result[i] = map.getCameraSignatureQuery(pose.getX(), pose.getY(), 360 - pose.getHeading(), i);
        }
    return result;
    }

    @Override
    public int[] cameraColorCodeQuery(int color) {
        return map.getCameraColorCodeQuery(pose.getX(), pose.getY(), 360 - pose.getHeading(), color);
    }

    @Override
    public int cameraAngleQuery() {
        return map.getCameraAngleQuery(pose.getX(), pose.getY(), 360 - pose.getHeading());
    }


    public int getSensorHeadPosition() {
        return sensorHeadPosition;
    }

    public void setSensorHeadPosition(int position) { this.sensorHeadPosition = position; }

    protected abstract void translate(float dx, float dy);
    protected abstract void turnFull(int degrees);
    protected abstract void turnSensor(int position);
    public boolean setStayOnWhiteLine(boolean stayOnWhiteLine) { return false; }

}