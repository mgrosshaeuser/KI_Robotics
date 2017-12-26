package ki.robotics.rover;

import ki.robotics.datastructures.Instruction;
import lejos.robotics.navigation.Pose;


/**
 * Interface prescribing the basic operations of a robot.
 */
public interface Robot {
    //TODO Comments, comments, commments ... oh dear!

    public boolean botTravelForward(double distance);
    public boolean botTravelBackward(double distance);
    public boolean botTurnLeft(double degree);
    public boolean botTurnRight(double degree);
    public boolean sensorHeadTurnLeft(double position);
    public boolean sensorHeadTurnRight(double position);
    public boolean sensorHeadReset();
    public int measureColor();
    public double measureDistance();
    public double[] ultrasonicThreeWayScan();
    public Pose getPose();
    public boolean shutdown();
    public boolean disconnect();
    public boolean handleUnsupportedInstruction(Instruction instruction);
}