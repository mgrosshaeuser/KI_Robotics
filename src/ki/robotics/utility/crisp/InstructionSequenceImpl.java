package ki.robotics.utility.crisp;

import java.util.ArrayList;

import static ki.robotics.utility.crisp.CRISP.*;

public class InstructionSequenceImpl implements InstructionSequence {
    private StringBuilder sequence = new StringBuilder();


    @Override
    public InstructionSequence perform(ArrayList<String> instructions) {
        for (String s : instructions) {
            if (sequence.length() != 0) { sequence.append(", "); }
            sequence.append(s);
        }
        return this;
    }


    @Override
    public InstructionSequence append(InstructionSequence seq) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(seq);
        return this;
    }


    @Override
    public InstructionSequence botTravelForward(double distance) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(BOT_TRAVEL_FORWARD).append(" ").append(distance);
        return this;
    }


    @Override
    public InstructionSequence botTravelBackward(double distance) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(BOT_TRAVEL_BACKWARD).append(" ").append(distance);
        return this;
    }


    @Override
    public InstructionSequence botTurnLeft(double angle) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(BOT_TURN_LEFT).append(" ").append(angle);
        return this;
    }

    @Override
    public InstructionSequence botTurnLeft() {
        return this.botTurnLeft(90);
    }


    @Override
    public InstructionSequence botTurnRight(double angle) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(BOT_TURN_RIGHT).append(" ").append(angle);
        return this;
    }

    @Override
    public InstructionSequence botTurnRight() {
        return this.botTurnRight(90);
    }


    @Override
    public InstructionSequence botEnableLineFollower() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(BOT_LINE_FOLLOWING_ENABLED);
        return this;
    }


    @Override
    public InstructionSequence botDisableLineFollower() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(BOT_LINE_FOLLOWING_DISABLED);
        return this;
    }


    @Override
    public InstructionSequence sensorTurnLeft(double degree) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SENSOR_TURN_LEFT).append(" ").append(degree);
        return this;
    }

    @Override
    public InstructionSequence sensorTurnLeft() {
        return this.sensorTurnLeft(90);
    }


    @Override
    public InstructionSequence sensorTurnRight(double degree) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SENSOR_TURN_RIGHT).append(" ").append(degree);
        return this;
    }

    @Override
    public InstructionSequence sensorTurnRight() {
        return this.sensorTurnRight(90);
    }


    @Override
    public InstructionSequence sensorReset() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SENSOR_RESET);
        return this;
    }


    @Override
    public InstructionSequence measureColor() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SENSOR_MEASURE_COLOR);
        return this;
    }


    @Override
    public InstructionSequence measureSingleDistance() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SENSOR_SINGLE_DISTANCE_SCAN);
        return this;
    }


    @Override
    public InstructionSequence measureAllDistances() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SENSOR_THREE_WAY_SCAN);
        return this;
    }


    @Override
    public InstructionSequence cameraGeneralQuery() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(CAMERA_GENERAL_QUERY);
        return this;
    }


    @Override
    public InstructionSequence camAngleQuery() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(CAMERA_ANGLE_QUERY);
        return this;
    }


    @Override
    public InstructionSequence camQuerySignature(int signature) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(CAMERA_SINGLE_SIGNATURE_QUERY).append(" ").append(signature);
        return this;
    }


    @Override
    public InstructionSequence camQueryAllSignatures() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(CAMERA_ALL_SIGNATURES_QUERY);
        return this;
    }


    @Override
    public InstructionSequence camQueryColorCode(int code) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(CAMERA_COLOR_CODE_QUERY).append(" ").append(code);
        return this;
    }


    @Override
    public InstructionSequence shutdown() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SHUTDOWN);
        return this;
    }

    @Override
    public InstructionSequence disconnect() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(DISCONNECT);
        return this;
    }




    @Override
    public String toString() {
        return sequence.toString();
    }
}
