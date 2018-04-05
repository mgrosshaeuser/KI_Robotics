package ki.robotics.utility.crisp;

import java.util.ArrayList;

import static ki.robotics.utility.crisp.CRISP.*;

public class ResponseSequence {
    private StringBuilder sequence = new StringBuilder();


    public ResponseSequence perform(ArrayList<String> instructions) {
        for (String s : instructions) {
            if (sequence.length() != 0) { sequence.append(", "); }
            sequence.append(s);
        }
        return this;
    }


    public ResponseSequence append(ResponseSequence seq) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(seq);
        return this;
    }


    public ResponseSequence botTravelForward(int distance) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(BOT_TRAVEL_FORWARD).append(" ").append(distance);
        return this;
    }


    public ResponseSequence botTravelBackward(int distance) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(BOT_TRAVEL_BACKWARD).append(" ").append(distance);
        return this;
    }


    public ResponseSequence botTurnLeft(int angle) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(BOT_TURN_LEFT).append(" ").append(angle);
        return this;
    }

    public ResponseSequence botTurnLeft() {
        return this.botTurnLeft(90);
    }


    public ResponseSequence botTurnRight(int angle) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(BOT_TURN_RIGHT).append(" ").append(angle);
        return this;
    }

    public ResponseSequence botTurnRight() {
        return this.botTurnRight(90);
    }

    public ResponseSequence botReturnPose() {
        return this;
    }


    public ResponseSequence botEnableLineFollower() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(BOT_LINE_FOLLOWING_ENABLED);
        return this;
    }


    public ResponseSequence botDisableLineFollower() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(BOT_LINE_FOLLOWING_DISABLED);
        return this;
    }


    public ResponseSequence sensorTurnLeft(int degree) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SENSOR_TURN_LEFT).append(" ").append(degree);
        return this;
    }

    public ResponseSequence sensorTurnLeft() {
        return this.sensorTurnLeft(90);
    }


    public ResponseSequence sensorTurnRight(int degree) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SENSOR_TURN_RIGHT).append(" ").append(degree);
        return this;
    }

    public ResponseSequence sensorTurnRight() {
        return this.sensorTurnRight(90);
    }


    public ResponseSequence sensorReset() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SENSOR_RESET);
        return this;
    }


    public ResponseSequence measureColor() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SENSOR_MEASURE_COLOR);
        return this;
    }


    public ResponseSequence measureSingleDistance() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SENSOR_SINGLE_DISTANCE_SCAN);
        return this;
    }


    public ResponseSequence measureAllDistances() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SENSOR_THREE_WAY_SCAN);
        return this;
    }


    public ResponseSequence cameraGeneralQuery() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(CAMERA_GENERAL_QUERY);
        return this;
    }


    public ResponseSequence camAngleQuery() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(CAMERA_ANGLE_QUERY);
        return this;
    }


    public ResponseSequence camQuerySignature(int signature) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(CAMERA_SINGLE_SIGNATURE_QUERY).append(" ").append(signature);
        return this;
    }


    public ResponseSequence camQueryAllSignatures() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(CAMERA_ALL_SIGNATURES_QUERY);
        return this;
    }


    public ResponseSequence camQueryColorCode(int code) {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(CAMERA_COLOR_CODE_QUERY).append(" ").append(code);
        return this;
    }


    public ResponseSequence shutdown() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(SHUTDOWN);
        return this;
    }

    public ResponseSequence disconnect() {
        if (sequence.length() != 0) { sequence.append(", "); }
        sequence.append(DISCONNECT);
        return this;
    }




    @Override
    public String toString() {
        return sequence.toString();
    }
}