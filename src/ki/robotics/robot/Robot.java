package ki.robotics.robot;

import ki.robotics.utility.crisp.Instruction;
import lejos.robotics.navigation.Pose;


/**
 * Interface prescribing the basic operations of a robot.
 */
public interface Robot {
    //TODO Comments, comments, commments ... oh dear!

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
    boolean shutdown();
    boolean disconnect();
    boolean handleUnsupportedInstruction(Instruction instruction);
}