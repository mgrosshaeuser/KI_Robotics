package ki.robotics.server.robots.simulation;

import ki.robotics.server.robots.Robot;
import ki.robotics.server.communication.ServerComController;
import ki.robotics.utility.crisp.Message;
import lejos.robotics.navigation.Pose;

import java.awt.geom.Point2D;

/**
 * Implementation of the Robot-Interface for simulating a robot.
 */
public class RobotImplSimulation implements Robot {
    private static final int ANIMATION_INTER_FRAME_TIME = 50;

    private final SimulationController simulationController;
    private ServerComController comController;

    /**
     * Constructor.
     */
    public RobotImplSimulation() {
        this.simulationController = new SimulationController();
    }


    //////////////////////////////////////////////////////////////////////////////
    //                                                                          //
    //          Implementation of the methods from the Robot-interface          //
    //                                                                          //
    //////////////////////////////////////////////////////////////////////////////

    @Override
    public void registerComController(ServerComController comController) {
        this.comController = comController;
    }

    @Override
    public double botTravelForward(double distance) {
        distance = determinePossibleTravelDistance(distance);
        float heading = simulationController.getModel().getPose().getHeading();
        float dx = (float) (Math.cos(Math.toRadians(heading)) * distance);
        float dy = (float) (Math.sin(Math.toRadians(heading)) * distance) * -1;
        translate(dx, dy);
        return distance;
    }

    @Override
    public double botTravelBackward(double distance) {
        return -(botTravelForward(distance * -1));
    }

    @Override
    public boolean botTurnLeft(double degree) {
        turnRobot((int) Math.round(degree));
        return true;
    }

    @Override
    public boolean botTurnRight(double degree) {
        turnRobot((int) Math.round(degree) * -1);
        return true;
    }

    @Override
    public boolean sensorHeadTurnLeft(double position) {
        turnSensorHead((int) Math.round(position));
        return true;
    }

    @Override
    public boolean sensorHeadTurnRight(double position) {
        turnSensorHead((int) Math.round(position) * -1);
        return true;
    }

    @Override
    public boolean sensorHeadReset() {
        turnSensorHead(0);
        return true;
    }

    @Override
    public int measureColor() {
        Pose pose = simulationController.getModel().getPose();
        Point2D measuringSpot = new Point2D.Double(pose.getX(), pose.getY());
        return simulationController.getModel().getMap().getFloorColorAt(measuringSpot);
    }

    @Override
    public double measureDistance() {
        Pose pose = simulationController.getModel().getPose();
        Point2D currentLocation = new Point2D.Double(pose.getX(), pose.getY());
        double viewingDirection = 360 - pose.getHeading() - simulationController.getModel().getSensorHeadPosition();
        return simulationController.getModel().getMap().getDistanceToNearestObstacle(currentLocation, viewingDirection);
    }

    @Override
    public double[] ultrasonicThreeWayScan() {
        int temp = simulationController.getModel().getSensorHeadPosition();
        turnSensorHead(90);
        double a = measureDistance();
        turnSensorHead(0);
        double b = measureDistance();
        turnSensorHead(-90);
        double c = measureDistance();
        turnSensorHead(temp);
        return new double[]{a, b, c};
    }

    @Override
    public Pose getPose() {
        return simulationController.getModel().getPose();
    }

    @Override
    public int[] cameraGeneralQuery() {
        double queryParameter[] = getCoordinatesAndAngleForCameraQuery();
        return simulationController.getModel().getMap().getGeneralCameraQuery(queryParameter[0], queryParameter[1], queryParameter[2]);
    }

    @Override
    public int[] cameraSignatureQuery(int signature) {
        double queryParameter[] = getCoordinatesAndAngleForCameraQuery();
        return simulationController.getModel().getMap().getCameraSignatureQuery(queryParameter[0], queryParameter[1], queryParameter[2], signature);
    }

    @Override
    public int[][] cameraAllSignaturesQuery() {
        int numberOfAvailableSignatures = 7;
        int[][] result = new int[numberOfAvailableSignatures][];
        for (int i = 0   ;   i < numberOfAvailableSignatures   ;   i++) {
            double queryParameter[] = getCoordinatesAndAngleForCameraQuery();
            result[i] = simulationController.getModel().getMap().getCameraSignatureQuery(queryParameter[0], queryParameter[1], queryParameter[2], i);
        }
        return result;
    }

    @Override
    public int[] cameraColorCodeQuery(int color) {
        double queryParameter[] = getCoordinatesAndAngleForCameraQuery();
        return simulationController.getModel().getMap().getCameraColorCodeQuery(queryParameter[0], queryParameter[1], queryParameter[2], color);
    }

    @Override
    public int cameraAngleQuery() {
        double queryParameter[] = getCoordinatesAndAngleForCameraQuery();
        return simulationController.getModel().getMap().getCameraAngleQuery(queryParameter[0], queryParameter[1], queryParameter[2]);
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

    //////////////////////////////////////////////////////////////////////////////





    /**
     * Returns the coordinates and the angle of view used by all camera queries
     *
     * @return Coordinates and angle of view as double[]
     */
    private double[] getCoordinatesAndAngleForCameraQuery() {
        Pose pose = simulationController.getModel().getPose();
        double x = pose.getX();
        double y = pose.getY();
        double angle = 360 - pose.getHeading();
        return new double[]{x, y, angle};
    }


    /**
     * Determines the possible fraction of the requested travel distance that can be traveled without hitting
     * an obstacle.
     *
     * @param requestedTravelDistance   the requested travel distance
     * @return  the possible travel distance without hitting an obstacle
     */
    private double determinePossibleTravelDistance(double requestedTravelDistance) {
        int temp = simulationController.getModel().getSensorHeadPosition();
        double possibleTravelDistance;
        float bumper = 5f;
        if (requestedTravelDistance > 0) {
            turnSensorHead(0);
            possibleTravelDistance = (measureDistance() >= requestedTravelDistance + bumper) ? requestedTravelDistance : measureDistance() - bumper;
        } else {
            turnSensorHead(180);
            possibleTravelDistance = (measureDistance() >= Math.abs(requestedTravelDistance) + bumper) ? requestedTravelDistance : -(measureDistance() - bumper);
        }
        turnSensorHead(temp);
        return possibleTravelDistance;
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
            simulationController.getModel().getPose().translate(xStep, yStep);
            simulationController.repaintWindow();
            pause(ANIMATION_INTER_FRAME_TIME);
        }
    }


    /**
     * Turns the heading over the given degrees stepwise. After each step the animation is paused for a fraction
     * of ANIMATION_INTER_FRAME_TIME
     *
     * @param degrees   Degrees to turn the heading.
     */
    private void turnRobot(int degrees) {
        Pose pose = simulationController.getModel().getPose();
        if (degrees > 0) {
            for (int i = 0  ;  i < degrees  ;  i++) {
                float heading = pose.getHeading();
                pose.setHeading((Math.round(heading + 1)) % 360);
                pause(ANIMATION_INTER_FRAME_TIME / 10);
            }
        } else {
            int diff = Math.round(pose.getHeading()) - Math.abs(degrees);
            if (diff >= 0) {
                for (int i = 0  ;  i > degrees  ;  i--) {
                    float heading = pose.getHeading();
                    pose.setHeading(Math.round(heading - 1));
                    pause (ANIMATION_INTER_FRAME_TIME / 10);
                }
            } else {
                while (pose.getHeading() > 0) {
                    float heading = pose.getHeading();
                    pose.setHeading(Math.round(heading -1));
                    pause (ANIMATION_INTER_FRAME_TIME / 10);
                }
                pose.setHeading(0);
                for (int i = 0  ;  i >= diff  ;  i--) {
                    pose.setHeading(360 + i);
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
    private void turnSensorHead(int position) {
        int sensorHeadPosition = simulationController.getModel().getSensorHeadPosition();
        if (position > sensorHeadPosition) {
            while ( position > sensorHeadPosition) {
                simulationController.getModel().setSensorHeadPosition(++sensorHeadPosition);
                pause(ANIMATION_INTER_FRAME_TIME / 20);
            }
        } else {
            while (position < sensorHeadPosition) {
                simulationController.getModel().setSensorHeadPosition(--sensorHeadPosition);
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
}
