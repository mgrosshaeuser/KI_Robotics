package ki.robotics.robot;

import ki.robotics.utility.crisp.Message;
import lejos.robotics.navigation.Pose;


/**
 * Interface prescribing the basic operations of a robot.
 */
public interface Robot {
    double botTravelForward(double distance);
    double botTravelBackward(double distance);
    boolean botTurnLeft(double degree);
    boolean botTurnRight(double degree);
    boolean sensorHeadTurnLeft(double position);
    boolean sensorHeadTurnRight(double position);
    boolean sensorHeadReset();
    int measureColor();
    double measureDistance();
    double[] ultrasonicThreeWayScan();
    Pose getPose();
    int[] cameraGeneralQuery();
    int[] cameraSignatureQuery(int signature);
    int[][] cameraAllSignaturesQuery();
    int[] cameraColorCodeQuery(int color);
    int cameraAngleQuery();
    boolean shutdown();
    boolean disconnect();
    boolean handleUnsupportedInstruction(Message instruction);
    boolean setStayOnWhiteLine(boolean stayOnWhiteLine);
}