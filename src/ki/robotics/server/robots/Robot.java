package ki.robotics.server.robots;

import ki.robotics.server.communication.ServerComController;
import ki.robotics.utility.crisp.Message;
import lejos.robotics.navigation.Pose;


/**
 * Interface prescribing the basic operations of a robot.
 */
public interface Robot {
    /**
     * Registers the communication-controller handling communication with this robot.
     *
     * @param comController An instance of ServerComController
     */
    void registerComController(ServerComController comController);


    /**
     * Moves the robot forward over the given distance. The return value is supposed to be the distance which the
     * robot actually traveled, which might be less than the given distance if an obstacle prohibits further movement.
     *
     * @param distance  The travel-distance
     * @return  The actual travel-distance
     */
    double botTravelForward(double distance);


    /**
     * Moves the robot backward over the given distance. The return value is supposed to be the distance which the
     * robot actually traveled, which might be less than the given distance if an obstacle prohibits further movement.
     *
     * @param distance  The travel-distance
     * @return  The actual travel-distance
     */
    double botTravelBackward(double distance);


    /**
     * Turns the robot over the given degrees to the left.
     *
     * @param degree    Degrees to turn robot.
     * @return  Boolean value indicating success
     */
    boolean botTurnLeft(double degree);


    /**
     * Turns the robot over the given degrees to the right.
     *
     * @param degree    Degrees to turn robot.
     * @return  Boolean value indicating success
     */
    boolean botTurnRight(double degree);


    /**
     * Turns the sensor-head to the given position (in degrees) to the left.
     *
     * @param position   Position to which to turn sensor-head.
     * @return  Boolean value indicating success
     */
    boolean sensorHeadTurnLeft(double position);


    /**
     * Turns the sensor-head to the given position (in degrees) to the right.
     *
     * @param position   Position to which to turn sensor-head.
     * @return  Boolean value indicating success
     */
    boolean sensorHeadTurnRight(double position);


    /**
     * Resets sensor-head to 0° (ahead)
     *
     * @return  Boolean value indicating success
     */
    boolean sensorHeadReset();


    /**
     * Measures the color at the current location.
     *
     * @return  The color-value as rgb-value (of type int)
     */
    int measureColor();


    /**
     * Measures the distance in direction the sensor-head is currently facing.
     *
     * @return  The measured distance
     */
    double measureDistance();


    /**
     * Performs a three distance-measurements at 90° to the left, ahead and 90° to the right.
     *
     * @return Array with the three distances (of type double)
     */
    double[] ultrasonicThreeWayScan();


    /**
     * Returns the pose of the robot (or what the robot thinks his pose is)
     *
     * @return  The (perceived) pose of the robot
     */
    Pose getPose();




    int[] cameraGeneralQuery();
    int[] cameraSignatureQuery(int signature);
    int[][] cameraAllSignaturesQuery();
    int[] cameraColorCodeQuery(int color);
    int cameraAngleQuery();




    /**
     * Shutdown of the server.
     *
     * @return  A boolean value indicating success
     */
    boolean shutdown();


    /**
     * Close current connection to a client.
     *
     * @return  A boolean value indicating success
     */
    boolean disconnect();


    /**
     * Handles instructions unknown to the robot.
     *
     * @param instruction   An (unknown) instruction
     * @return  A boolean value indicating success
     */
    boolean handleUnsupportedInstruction(Message instruction);


    /**
     * Enables (true) or disables (false) line-following for automatic correction of track deviation.
     *
     * @param stayOnWhiteLine   Boolean value enabling (true) or disabling (false) line-following
     * @return  A boolean value indicating success
     */
    boolean setStayOnWhiteLine(boolean stayOnWhiteLine);
}