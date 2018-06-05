package ki.robotics.server.robots.simulation;

import ki.robotics.server.Robot;
import ki.robotics.utility.crisp.Message;
import lejos.robotics.navigation.Pose;

import java.awt.geom.Point2D;

/**
 * Implementation of the Robot-Interface through the RobotImplVirtualRobot-class.
 * Simulates and displays a robot.
 *
 * @version 1.0 01/02/18
 */
public class RobotImplSimulation implements Robot {
    private static final int ANIMATION_INTER_FRAME_TIME = 50;

    private final SimulationController simulationController;

    /**
     * Constructor.
     */
    public RobotImplSimulation() {
        this.simulationController = new SimulationController();
    }


    @Override
    public double botTravelForward(double distance) {
        float bumper = 5f;
        int temp = simulationController.getSensorHeadPosition();
        if (distance > 0) {
            turnSensor(0);
            distance = (measureDistance() >= distance + bumper) ? distance : measureDistance() - bumper;
        } else {
            turnSensor(180);
            distance = (measureDistance() >= Math.abs(distance) + bumper) ? distance : -(measureDistance() - bumper);
        }

        float dx = (float) (Math.cos(Math.toRadians(simulationController.getPose().getHeading())) * distance);
        float dy = (float) (Math.sin(Math.toRadians(simulationController.getPose().getHeading())) * distance) * -1;

        translate(dx, dy);
        turnSensor(temp);
        return distance;
    }

    /**
     * Translates the simulated robot over given distances in x- and y-direction.
     *
     * @param dx    Translation-distance along the x-axis.
     * @param dy    Translation-distance along the y-axis.
     */
    private void translate(float dx, float dy) {
        float distance = (float) Math.sqrt(Math.pow(dx,2) + Math.pow(dy,2));
        float xStep = dx / distance;
        float yStep = dy / distance;
        for (int i = 0  ;  i < distance  ;  i++) {
            simulationController.getPose().translate(xStep, yStep);
            simulationController.repaintWindow();
            pause(ANIMATION_INTER_FRAME_TIME);
        }
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
        Point2D measuringSpot = new Point2D.Double(simulationController.getPose().getX(), simulationController.getPose().getY());
        return simulationController.getMap().getFloorColorAt(measuringSpot);
    }


    @Override
    public double measureDistance() {
        Point2D currentLocation = new Point2D.Double(simulationController.getPose().getX(), simulationController.getPose().getY());
        double viewingDirection = 360 - simulationController.getPose().getHeading() - simulationController.getSensorHeadPosition();
        return simulationController.getMap().getDistanceToNearestObstacle(currentLocation, viewingDirection);
    }


    @Override
    public double[] ultrasonicThreeWayScan() {
        int temp = simulationController.getSensorHeadPosition();
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
        return simulationController.getPose();
    }

    @Override
    public int[] cameraGeneralQuery() {
        double x = simulationController.getPose().getX();
        double y = simulationController.getPose().getY();
        double angle = 360 - simulationController.getPose().getHeading();
        return simulationController.getMap().getGeneralCameraQuery(x, y, angle);
    }

    @Override
    public int[] cameraSignatureQuery(int signature) {
        double x = simulationController.getPose().getX();
        double y = simulationController.getPose().getY();
        double angle = 360 - simulationController.getPose().getHeading();
        return simulationController.getMap().getCameraSignatureQuery(x, y, angle, signature);
    }

    @Override
    public int[][] cameraAllSignaturesQuery() {
        int numberOfAvailableSignatures = 7;
        int[][] result = new int[numberOfAvailableSignatures][];
        for (int i = 0   ;   i < numberOfAvailableSignatures   ;   i++) {
            double x = simulationController.getPose().getX();
            double y = simulationController.getPose().getY();
            double angle = 360 - simulationController.getPose().getHeading();
            result[i] = simulationController.getMap().getCameraSignatureQuery(x, y, angle, i);
        }
        return result;
    }

    @Override
    public int[] cameraColorCodeQuery(int color) {
        double x = simulationController.getPose().getX();
        double y = simulationController.getPose().getY();
        double angle = 360 - simulationController.getPose().getHeading();
        return simulationController.getMap().getCameraColorCodeQuery(x, y, angle, color);
    }

    @Override
    public int cameraAngleQuery() {
        double x = simulationController.getPose().getX();
        double y = simulationController.getPose().getY();
        double angle = 360 - simulationController.getPose().getHeading();
        return simulationController.getMap().getCameraAngleQuery(x, y, angle);
    }



    /**
     * Turns the heading over the given degrees.
     *
     * @param degrees   Degrees to turn the heading.
     */
    private void turnFull(int degrees) {
        if (degrees > 0) {
            for (int i = 0  ;  i < degrees  ;  i++) {
                simulationController.getPose().setHeading((Math.round(simulationController.getPose().getHeading() + 1)) % 360);
                pause(ANIMATION_INTER_FRAME_TIME / 10);
            }
        } else {
            int diff = Math.round(simulationController.getPose().getHeading()) - Math.abs(degrees);
            if (diff >= 0) {
                for (int i = 0  ;  i > degrees  ;  i--) {
                    simulationController.getPose().setHeading(Math.round(simulationController.getPose().getHeading() - 1));
                    pause (ANIMATION_INTER_FRAME_TIME / 10);
                }
            } else {
                while (simulationController.getPose().getHeading() > 0) {
                    simulationController.getPose().setHeading(Math.round(simulationController.getPose().getHeading() -1));
                    pause (ANIMATION_INTER_FRAME_TIME / 10);
                }
                simulationController.getPose().setHeading(0);
                for (int i = 0  ;  i >= diff  ;  i--) {
                    simulationController.getPose().setHeading(360 + i);
                    pause (ANIMATION_INTER_FRAME_TIME/10);
                }
            }
        }
    }


    /**
     * Turns the position of the sensor-head carrying the distance-sensor.
     *
     * @param position  The new position of the sensor-head.
     */
    private void turnSensor(int position) {
        int sensorHeadPosition = simulationController.getSensorHeadPosition();
        if (position > sensorHeadPosition) {
            while ( position > sensorHeadPosition) {
                simulationController.setSensorHeadPosition(++sensorHeadPosition);
                pause(ANIMATION_INTER_FRAME_TIME / 20);
            }
        } else {
            while (position < sensorHeadPosition) {
                simulationController.setSensorHeadPosition(--sensorHeadPosition);
                pause(ANIMATION_INTER_FRAME_TIME / 20);
            }
        }
    }


    /**
     * Stops the current (gui-)thread to make the movement of the simulated robot observable.
     *
     * @param ms    Delay between two updates of the display.
     */
    private void pause(int ms) {
        simulationController.repaintWindow();
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean shutdown() {
        return false;
    }

    @Override
    public boolean disconnect() {
        return false;
    }

    @Override
    public boolean handleUnsupportedInstruction(Message instruction) {
        System.out.println("Unknows Instruction: >>" + instruction.getMnemonic());
        return true;
    }

    @Override
    public boolean setStayOnWhiteLine(boolean stayOnWhiteLine) {
        //no color sensor in simulation
        return true;
    }





}
