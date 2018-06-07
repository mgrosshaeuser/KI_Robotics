package ki.robotics.utility.crisp;

import java.util.ArrayList;

public interface InstructionSequence {
    InstructionSequence perform(ArrayList<String> instructions);

    InstructionSequence append(InstructionSequence seq);

    InstructionSequence botTravelForward(double distance);

    InstructionSequence botTravelBackward(double distance);

    InstructionSequence botTurnLeft(double angle);

    InstructionSequence botTurnLeft();

    InstructionSequence botTurnRight(double angle);

    InstructionSequence botTurnRight();

    InstructionSequence botEnableLineFollower();

    InstructionSequence botDisableLineFollower();

    InstructionSequence sensorTurnLeft(double degree);

    InstructionSequence sensorTurnLeft();

    InstructionSequence sensorTurnRight(double degree);

    InstructionSequence sensorTurnRight();

    InstructionSequence sensorReset();

    InstructionSequence measureColor();

    InstructionSequence measureSingleDistance();

    InstructionSequence measureAllDistances();

    InstructionSequence cameraGeneralQuery();

    InstructionSequence camAngleQuery();

    InstructionSequence camQuerySignature(int signature);

    InstructionSequence camQueryAllSignatures();

    InstructionSequence camQueryColorCode(int code);

    InstructionSequence shutdown();

    InstructionSequence disconnect();

    @Override
    String toString();
}
