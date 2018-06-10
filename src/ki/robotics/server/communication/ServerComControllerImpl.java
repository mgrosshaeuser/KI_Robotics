package ki.robotics.server.communication;

import ki.robotics.server.ServerFactory;
import ki.robotics.server.robots.Robot;
import ki.robotics.utility.crisp.Message;

import java.io.PrintWriter;
import java.util.ArrayList;

import static ki.robotics.utility.crisp.CRISP.*;


/**
 * Communication-controller for:
 * - forwarding instructions to the robot (or a simulation)
 * - sending back sensor-feedback
 */
public class ServerComControllerImpl implements ServerComController {
    private ServerCommunicator communicator;
    private PrintWriter out;
    private Robot robot;
    private RequestHandler requestHandler;


    /**
     * Constructor.
     *
     * @param communicator  A server-side communicator
     * @param robot A robot
     */
    public ServerComControllerImpl(ServerCommunicator communicator, Robot robot) {
        this.communicator = communicator;
        this.out = null;
        this.robot = robot;
    }


    /**
     * Registers the output stream for sending responses.
     *
     * @param out The output stream for sending responses.
     */
    void registerOutputStream(PrintWriter out) {
        this.out = out;
        if (this.requestHandler != null) {
            requestHandler.registerNewOutputStream(out);
        }
    }


    /**
     *  Allows the robot to terminate the connection.
     */
    @Override
    public void disconnect() {
        communicator.disconnect();
    }


    /**
     * Allows the robot to shutdown the communication-server.
     */
    @Override
    public void shutdown() {
        communicator.shutdown();
    }


    /**
     * Lazy initialization of request-handler and forwarding of transmission to request-handler.
     *
     * @param transmission  The transmission from a client
     */
    @Override
    public void handleRequest(String transmission) {
        if (this.requestHandler == null) {
            this.requestHandler = new RequestHandler(out, robot);
        }
        requestHandler.processTransmission(transmission);
        out.println(ServerFactory.createMeassage(END_OF_INSTRUCTION_SEQUENCE));
    }





    private class RequestHandler {
        private PrintWriter out;
        private final Robot robot;


        /**
         * Constructor
         *
         * @param out       A PrintWriter for sending feedback
         * @param robot     A Robot to perform the instructions
         */
        private RequestHandler(PrintWriter out, Robot robot) {
            this.out = out;
            this.robot = robot;
        }


        /**
         * Registers a new PrintWriter for re-established communication.
         *
         * @param out   A new PrintWriter
         */
        private void registerNewOutputStream(PrintWriter out) {
            this.out = out;
        }


        /**
         * Decodes the transmission (of type String) and routes the received instructions.
         *
         * @param transmission  The transmission from a client
         */
        private void processTransmission(String transmission) {
            ArrayList<Message> requests = ServerFactory.createMessageListFromTransmission(transmission);
            while (! requests.isEmpty()) {
                processRequest(requests.remove(0));
            }
        }


        /**
         * Conditional routing of the request, depending on the instruction-category.
         *
         * @param request   A single request.
         */
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


        /**
         * Handles instructions regarding robot-motion.
         * Acknowledgement is directly send over the output-stream.
         *
         * @param instruction   An instruction regarding robot-motion
         */
        private void processBotInstructions(Message instruction) {
            switch (instruction.getMnemonic()) {
                case BOT_RETURN_POSE:
                    //TODO Implementation
                    out.println(ServerFactory.createMessage(BOT_RETURN_POSE, 0,0,0));
                    break;
                case BOT_LINE_FOLLOWING_ENABLED:
                    robot.setStayOnWhiteLine(true);
                    out.println(ServerFactory.createMeassage(BOT_LINE_FOLLOWING_ENABLED));
                    break;
                case BOT_LINE_FOLLOWING_DISABLED:
                    robot.setStayOnWhiteLine(false);
                    out.println(ServerFactory.createMeassage(BOT_LINE_FOLLOWING_DISABLED));
                    break;
                case BOT_TRAVEL_FORWARD:
                    botTravelForwardWithCollisionAvoidance(instruction);
                    break;
                case BOT_TRAVEL_BACKWARD:
                    double travelledBackward = robot.botTravelBackward((double)instruction.getParameter());
                    out.println(ServerFactory.createMessage(instruction.getMnemonic(), travelledBackward));
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


        /**
         * Forward motion of the robot including collision avoidance.
         * Acknowledgement (including traveled distance) is directly send over the output-stream.
         *
         * @param instruction   Forward-motion-instruction.
         */
        private void botTravelForwardWithCollisionAvoidance(Message instruction) {
            double travelledForward = robot.botTravelForward((double)instruction.getParameter());
            if (travelledForward < -8) { //bumper
                out.println(ServerFactory.createMeassage(BOT_U_TURN));
                out.println(ServerFactory.createMessage(instruction.getMnemonic(), -travelledForward ));
            }else if (travelledForward < 0 ) {
                out.println(ServerFactory.createMessage(instruction.getMnemonic(), travelledForward));
            } else {
                out.println(ServerFactory.createMessage(instruction.getMnemonic(), travelledForward));
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
                    out.println(ServerFactory.createMessage(SENSOR_MEASURE_COLOR, color));
                    break;
                case SENSOR_SINGLE_DISTANCE_SCAN:
                    double distance = robot.measureDistance();
                    out.println(ServerFactory.createMessage(SENSOR_SINGLE_DISTANCE_SCAN, distance));
                    break;
                case SENSOR_THREE_WAY_SCAN:
                    double[] tws = robot.ultrasonicThreeWayScan();
                    out.println(ServerFactory.createMessage(SENSOR_THREE_WAY_SCAN, tws[0], tws[1], tws[2]));
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
                    out.println(ServerFactory.createMessage(CAMERA_GENERAL_QUERY, robot.cameraGeneralQuery()));
                    break;
                case CAMERA_SINGLE_SIGNATURE_QUERY:
                    int[] singleSignatureResult = robot.cameraSignatureQuery((int)instruction.getParameter());
                    out.println(ServerFactory.createMessage(CAMERA_SINGLE_SIGNATURE_QUERY, singleSignatureResult));
                    break;
                case CAMERA_ALL_SIGNATURES_QUERY:
                    int[][] allSignatures = robot.cameraAllSignaturesQuery();
                    for (int[] signature : allSignatures) {
                        out.println(ServerFactory.createMessage(CAMERA_SINGLE_SIGNATURE_QUERY, signature));
                    }
                    break;
                case CAMERA_COLOR_CODE_QUERY:
                    int colorCode = (int)instruction.getParameter();
                    int[] colorCodeResponse = robot.cameraColorCodeQuery(colorCode);
                    int[] colorCodeResult = new int[colorCodeResponse.length + 1];
                    colorCodeResult[0] = colorCode;
                    System.arraycopy(colorCodeResponse, 0, colorCodeResult, 1, colorCodeResponse.length);
                    out.println(ServerFactory.createMessage(CAMERA_COLOR_CODE_QUERY, colorCodeResult));
                    break;
                case CAMERA_ANGLE_QUERY:
                    int angleResult = robot.cameraAngleQuery();
                    out.println(ServerFactory.createMessage(CAMERA_ANGLE_QUERY, angleResult));
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
                    communicator.disconnect();
                    robot.shutdown();
                    break;
                case DISCONNECT:
                    out.println(DISCONNECT);
                    communicator.disconnect();
                    robot.disconnect();
                    break;
                default:
                    out.println(UNSUPPORTED_INSTRUCTION);
                    robot.handleUnsupportedInstruction(instruction);
                    break;
            }
        }

    }


}
