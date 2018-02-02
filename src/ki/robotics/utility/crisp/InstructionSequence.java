package ki.robotics.utility.crisp;

import java.util.ArrayList;

import static ki.robotics.utility.crisp.CRISP.*;

public class InstructionSequence {
    private StringBuilder sequence = new StringBuilder();


    public InstructionSequence perform(ArrayList<String> instructions) {
        for (String s : instructions) {
            if (sequence.length() != 0) { sequence.append(", "); }
            sequence.append(s);
        }
        return this;
    }


    public InstructionSequence botTravelForward(int distance) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(BOT_TRAVEL_FORWARD).append(" ").append(distance);
        return this;
    }


    public InstructionSequence botTravelBackward(int distance) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(BOT_TRAVEL_BACKWARD).append(" ").append(distance);
        return this;
    }


    public InstructionSequence botTurnLeft(int angle) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(BOT_TURN_LEFT).append(" ").append(angle);
        return this;
    }

    public InstructionSequence botTurnLeft() {
        return this.botTurnLeft(90);
    }


    public InstructionSequence botTurnRight(int angle) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(BOT_TURN_RIGHT).append(" ").append(angle);
        return this;
    }

    public InstructionSequence botTurnRight() {
        return this.botTurnRight(90);
    }


    public InstructionSequence botEnableLineFollower() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(BOT_LINE_FOLLOWING_ENABLED);
        return this;
    }


    public InstructionSequence botDisableLineFollower() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(BOT_LINE_FOLLOWING_DISABLED);
        return this;
    }


    public InstructionSequence sensorTurnLeft(int degree) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SENSOR_TURN_LEFT).append(" ").append(degree);
        return this;
    }

    public InstructionSequence sensorTurnLeft() {
        return this.sensorTurnLeft(90);
    }


    public InstructionSequence sensorTurnRight(int degree) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SENSOR_TURN_RIGHT).append(" ").append(degree);
        return this;
    }

    public InstructionSequence sensorTurnRight() {
        return this.sensorTurnRight(90);
    }


    public InstructionSequence sensorReset() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SENSOR_RESET);
        return this;
    }


    public InstructionSequence measureColor() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SENSOR_MEASURE_COLOR);
        return this;
    }


    public InstructionSequence measureSingleDistance() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SENSOR_SINGLE_DISTANCE_SCAN);
        return this;
    }


    public InstructionSequence measureAllDistances() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SENSOR_THREE_WAY_SCAN);
        return this;
    }


    public InstructionSequence cameraGeneralQuery() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(CAMERA_GENERAL_QUERY);
        return this;
    }


    public InstructionSequence camAngleQuery() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(CAMERA_ANGLE_QUERY);
        return this;
    }


    public InstructionSequence camQuerySignature(int signature) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(CAMERA_SINGLE_SIGNATURE_QUERY).append(" ").append(signature);
        return this;
    }


    public InstructionSequence camQueryAllSignatures() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(CAMERA_ALL_SIGNATURES_QUERY);
        return this;
    }


    public InstructionSequence camQueryColorCode(int code) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(CAMERA_COLOR_CODE_QUERY).append(" ").append(code);
        return this;
    }


    public InstructionSequence shutdown() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SHUTDOWN);
        return this;
    }

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
