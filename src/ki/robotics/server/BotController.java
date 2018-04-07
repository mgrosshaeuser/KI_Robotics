package ki.robotics.server;

import ki.robotics.robot.Robot;
import ki.robotics.robot.RoverSimulation;
import ki.robotics.robot.Sojourner;
import ki.robotics.utility.crisp.Message;

import java.io.PrintWriter;
import java.util.ArrayList;

import static ki.robotics.utility.crisp.CRISP.*;


class BotController {
    private BotServer server;
    private PrintWriter out;
    private final Robot robot;




    public BotController(boolean isSimulation, BotServer server) {
        this.server = server;
        this.out = null;
        this.robot = isSimulation ? new RoverSimulation() : Sojourner.getInstance();
    }


    public void registerOutputStream(PrintWriter out) {
        this.out = out;
    }




    public void handleRequest(String transmission) {
        ArrayList<Message> requests = Message.decodeTransmission(transmission);
        while (! requests.isEmpty()) {
            processRequest(requests.remove(0));
        }
        out.println(new Message<>(END_OF_INSTRUCTION_SEQUENCE));
    }



    private void processRequest(Message request) {
        switch (request.getMessageGroup()) {
            case BOT_INSTRUCTION:
                processBotInstructions(request);
                break;
            case SENSOR_INSTRUCTION:
                processSensorInstruction(request);
                break;
            case CAMERA_INSTRUCTION:
                processCameraInstruction(request);
                break;
            default:
                processOtherInstruction(request);
                break;
        }
    }


    private void processBotInstructions(Message instruction) {
        switch (instruction.getMnemonic()) {
            case BOT_RETURN_POSE:
                //TODO Implementation
                out.println(new Message<>(BOT_RETURN_POSE, 0,0,0));
                break;
            case BOT_LINE_FOLLOWING_ENABLED:
                robot.setStayOnWhiteLine(true);
                out.println(new Message<>(BOT_LINE_FOLLOWING_ENABLED));
                break;
            case BOT_LINE_FOLLOWING_DISABLED:
                robot.setStayOnWhiteLine(false);
                out.println(new Message<>(BOT_LINE_FOLLOWING_DISABLED));
                break;
            case BOT_TRAVEL_FORWARD:
                botTravelForwardWithCollisionAvoidance(instruction);
                break;
            case BOT_TRAVEL_BACKWARD:
                double travelledBackward = robot.botTravelBackward((double)instruction.getParameter());
                out.println(new Message<>(instruction.getMnemonic(), travelledBackward));
                break;
            case BOT_TURN_LEFT:
                robot.botTurnLeft((double)instruction.getParameter());
                out.println(instruction);
                break;
            case BOT_TURN_RIGHT:
                robot.botTurnRight((double)instruction.getParameter());
                out.println(instruction);
                break;
            default:
                out.println(UNSUPPORTED_INSTRUCTION);
                robot.handleUnsupportedInstruction(instruction);
                break;
        }
    }

    private void botTravelForwardWithCollisionAvoidance(Message instruction) {
        double travelledForward = robot.botTravelForward((double)instruction.getParameter());
        if (travelledForward < -8) { //bumper
            out.println(new Message<>(BOT_U_TURN));
            out.println(new Message<>(instruction.getMnemonic(), -travelledForward ));
        }else if (travelledForward < 0 ) {
            out.println(new Message<>(instruction.getMnemonic(), travelledForward));
        } else {
            out.println(new Message<>(instruction.getMnemonic(), travelledForward));
        }
    }


    /**
     * Handles instructions regarding the sensors and the sensor-head.
     * Sensor-readout is directly send over the output-stream.
     *
     * @param instruction   Instruction to be performed.
     */
    private void processSensorInstruction(Message instruction) {
        switch (instruction.getMnemonic()) {
            case SENSOR_TURN_LEFT:
                robot.sensorHeadTurnLeft((double)instruction.getParameter());
                out.println(instruction);
                break;
            case SENSOR_TURN_RIGHT:
                robot.sensorHeadTurnRight((double)instruction.getParameter());
                out.println(instruction);
                break;
            case SENSOR_RESET:
                robot.sensorHeadReset();
                out.println(instruction);
                break;
            case SENSOR_MEASURE_COLOR:
                int color = robot.measureColor();
                out.println(new Message<>(SENSOR_MEASURE_COLOR, color));
                break;
            case SENSOR_SINGLE_DISTANCE_SCAN:
                double distance = robot.measureDistance();
                out.println(new Message<>(SENSOR_SINGLE_DISTANCE_SCAN, distance));
                break;
            case SENSOR_THREE_WAY_SCAN:
                double[] tws = robot.ultrasonicThreeWayScan();
                out.println(new Message<>(SENSOR_THREE_WAY_SCAN, tws[0], tws[1], tws[2]));
                break;
            default:
                out.println(UNSUPPORTED_INSTRUCTION);
                robot.handleUnsupportedInstruction(instruction);
                break;
        }
    }


    /**
     * Handles instructions regarding the camera.
     * Readout is directly send over the output-stream.
     *
     * @param instruction   Instruction to be performed.
     */
    private void processCameraInstruction(Message instruction) {
        switch (instruction.getMnemonic()) {
            case CAMERA_GENERAL_QUERY:
                out.println(new Message<>(CAMERA_GENERAL_QUERY, robot.cameraGeneralQuery()));
                break;
            case CAMERA_SINGLE_SIGNATURE_QUERY:
                int[] singleSignatureResult = robot.cameraSignatureQuery((int)instruction.getParameter());
                out.println(new Message<>(CAMERA_SINGLE_SIGNATURE_QUERY, singleSignatureResult));
                break;
            case CAMERA_ALL_SIGNATURES_QUERY:
                int[][] allSignatures = robot.cameraAllSignaturesQuery();
                for (int[] signature : allSignatures) {
                    out.println(new Message<>(CAMERA_SINGLE_SIGNATURE_QUERY, signature));
                }
                break;
            case CAMERA_COLOR_CODE_QUERY:
                int colorCode = (int)instruction.getParameter();
                int[] colorCodeResponse = robot.cameraColorCodeQuery(colorCode);
                int[] colorCodeResult = new int[colorCodeResponse.length + 1];
                colorCodeResult[0] = colorCode;
                System.arraycopy(colorCodeResponse, 0, colorCodeResult, 1, colorCodeResponse.length);
                out.println(new Message<>(CAMERA_COLOR_CODE_QUERY, colorCodeResult));
                break;
            case CAMERA_ANGLE_QUERY:
                int angleResult = robot.cameraAngleQuery();
                out.println(new Message<>(CAMERA_ANGLE_QUERY, angleResult));
                break;
            default:
                out.println(UNSUPPORTED_INSTRUCTION);
                robot.handleUnsupportedInstruction(instruction);
                break;
        }
    }


    /**
     * Handles all other instructions.
     * Feedback is directly send over the output-stream.
     *
     * @param instruction   Instruction to be performed.
     */
    private void processOtherInstruction(Message instruction) {
        switch(instruction.getMnemonic()) {
            case SHUTDOWN:
                out.println(DISCONNECT);
                server.disconnect();
                robot.shutdown();
            case DISCONNECT:
                out.println(DISCONNECT);
                server.disconnect();
                robot.disconnect();
            default:
                out.println(UNSUPPORTED_INSTRUCTION);
                robot.handleUnsupportedInstruction(instruction);
                break;
        }
    }
}
