package ki.robotics.server;

import ki.robotics.robot.Robot;
import ki.robotics.robot.RoverSimulation;
import ki.robotics.robot.Sojourner;
import ki.robotics.utility.crisp.Message;

import java.io.PrintWriter;
import java.util.ArrayList;

import static ki.robotics.utility.crisp.CRISP.*;

/**
 * A GUIComController translating the Instruction-class mnemonics into method-calls on an implementation of
 * the Robot-interface.
 *
 * @version 1.0, 12/26/17
 */
class BotController {
    private PrintWriter out;
    private ArrayList<Message> jobQueue;
    private final Robot robot;



    /**
     * Constructor
     * Depending on the boolean isSimulation-value, either an instance of a simulated robot is used or the
     * singleton-instance for accessing the physical robot is acquired.
     *
     * @param isSimulation  Determines about simulation-mode or real-mode
     */
    public BotController(boolean isSimulation, BotServer botServer) {
        this.out = null;
        this.jobQueue = new ArrayList<>();
        this.robot = isSimulation ? new RoverSimulation(botServer) : Sojourner.getInstance();
    }


    /**
     * For the robot to send feedback an output-stream has to be registered.
     *
     * @param out   The output-stream to give feedback to the client.
     */
    public void registerOutputStream(PrintWriter out) {
        this.out = out;
        this.jobQueue = new ArrayList<>();
    }



    /**
     * Manages the interpretation and execution of a client-request by using an InstructionSetDecode-instance
     * to split and interpret the Instructions from an instruction-sequence, appending them to a job-queue and
     * reporting to the clients once all jobs in the job-queue are processed.
     * 'EOSQ' signals the client the completion of an instruction-sequence.
     *
     * @param instructionSequence   The instruction-sequence to process.
     * @return                      boolean value, used to tell the server to close or keep the connection.
     */
    public boolean handleRequest(String instructionSequence) {
        ArrayList<Message> instructions = Message.decodeTransmission(instructionSequence);
        jobQueue.addAll(instructions);
        boolean stayConnected = true;

        while (stayConnected  &&  ! jobQueue.isEmpty()) {
            Message instruction = jobQueue.remove(0);
            stayConnected = processInstruction(instruction);
        }

        out.println(END_OF_INSTRUCTION_SEQUENCE);

        return stayConnected;
    }



    /**
     * Routes the instruction for processing by a specialized handler-method.
     *
     * @param instruction   The instruction to be performed.
     * @return              The status resulting from the processing of the Instruction; Decision about
     *                      keeping or terminating the connection.
     */
    private boolean processInstruction(Message instruction) {
        boolean status;

        switch (instruction.getMessageGroup()) {
            case BOT_INSTRUCTION:
                status = processBotInstruction(instruction);
                break;
            case SENSOR_INSTRUCTION:
                status = processSensorInstruction(instruction);
                break;
            case CAMERA_INSTRUCTION:
                status = processCameraInstruction(instruction);
                break;
            default:
                status = processOtherInstruction(instruction);
        }

        return status;
    }


    /**
     * Handles instructions regarding the movement of the robot.
     * Feedback, is directly send over the output-stream.
     *
     * @param instruction   Instruction to be performed.
     * @return              A status.
     */
    private boolean processBotInstruction(Message instruction) {
        switch (instruction.getMnemonic()) {
            case BOT_RETURN_POSE:
                //TODO Implementation
                return true;
            case BOT_LINE_FOLLOWING_ENABLED:
                out.println(new Message<>(BOT_LINE_FOLLOWING_ENABLED));
                return robot.setStayOnWhiteLine(true);
            case BOT_LINE_FOLLOWING_DISABLED:
                out.println(new Message<>(BOT_LINE_FOLLOWING_DISABLED));
                robot.setStayOnWhiteLine(false);
                return true;
        }

        double parameter;
        try {
            parameter = (double) instruction.getParameter();
        } catch (ClassCastException e) {
            parameter = (int) instruction.getParameter();
        }
        switch (instruction.getMnemonic()) {
            case BOT_TRAVEL_FORWARD:
                double travelledForward = robot.botTravelForward(parameter);
                if (travelledForward < -8) { //bumper
                    out.println(new Message<>(BOT_U_TURN));
                    out.println(new Message<>(instruction.getMnemonic(), -travelledForward ));
                }else if (travelledForward < 0 ) {
                    out.println(new Message<>(instruction.getMnemonic(), travelledForward));
                } else {
                    out.println(new Message<>(instruction.getMnemonic(), travelledForward));
                }
                return true;
            case BOT_TRAVEL_BACKWARD:
                double travelledBackward = robot.botTravelBackward(parameter);
                out.println(new Message<>(instruction.getMnemonic(), travelledBackward));
                return true;
            case BOT_TURN_LEFT:
                out.println(instruction);
                return robot.botTurnLeft(parameter);
            case BOT_TURN_RIGHT:
                out.println(instruction);
                return robot.botTurnRight(parameter);
            default:
                out.println(new Message<>(UNSUPPORTED_INSTRUCTION));
                return true;
        }
    }


    /**
     * Handles instructions regarding the sensors and the sensor-head.
     * Sensor-readout is directly send over the output-stream.
     *
     * @param instruction   Instruction to be performed.
     * @return              A status.
     */
    private boolean processSensorInstruction(Message instruction) {
        switch (instruction.getMnemonic()) {
            case SENSOR_TURN_LEFT:
                out.println(instruction);
                return robot.sensorHeadTurnLeft((double)instruction.getParameter());
            case SENSOR_TURN_RIGHT:
                out.println(instruction);
                return robot.sensorHeadTurnRight((double)instruction.getParameter());
            case SENSOR_RESET:
                out.println(instruction);
                return robot.sensorHeadReset();
            case SENSOR_MEASURE_COLOR:
                int color = robot.measureColor();
                out.println(new Message<>(SENSOR_MEASURE_COLOR, color));
                return true;
            case SENSOR_SINGLE_DISTANCE_SCAN:
                double distance = robot.measureDistance();
                out.println(new Message<>(SENSOR_SINGLE_DISTANCE_SCAN, distance));
                return true;
            case SENSOR_THREE_WAY_SCAN:
                double[] tws = robot.ultrasonicThreeWayScan();
                out.println(new Message<>(SENSOR_THREE_WAY_SCAN, tws[0], tws[1], tws[2]));
                return true;
            default:
                out.println(new Message<>(UNSUPPORTED_INSTRUCTION));
                return true;
        }
    }


    /**
     * Handles instructions regarding the camera.
     * Readout is directly send over the output-stream.
     *
     * @param instruction   Instruction to be performed.
     * @return              A status.
     */
    private boolean processCameraInstruction(Message instruction) {
        switch (instruction.getMnemonic()) {
            case CAMERA_GENERAL_QUERY:
                out.println(new Message<>(CAMERA_GENERAL_QUERY, robot.cameraGeneralQuery()));
                return  true;
            case CAMERA_SINGLE_SIGNATURE_QUERY:
                int[] singleSignatureResult = robot.cameraSignatureQuery((int)instruction.getParameter());
                out.println(new Message<>(CAMERA_SINGLE_SIGNATURE_QUERY, singleSignatureResult));
                return true;
            case CAMERA_ALL_SIGNATURES_QUERY:
                int[][] allSignatures = robot.cameraAllSignaturesQuery();
                for (int[] signature : allSignatures) {
                    out.println(new Message<>(CAMERA_SINGLE_SIGNATURE_QUERY, signature));
                }
                return true;
            case CAMERA_COLOR_CODE_QUERY:
                int colorCode = (int)instruction.getParameter();
                int[] colorCodeResponse = robot.cameraColorCodeQuery(colorCode);
                int[] colorCodeResult = new int[colorCodeResponse.length + 1];
                colorCodeResult[0] = colorCode;
                System.arraycopy(colorCodeResponse, 0, colorCodeResult, 1, colorCodeResponse.length);
                out.println(new Message<>(CAMERA_COLOR_CODE_QUERY, colorCodeResult));
                return true;
            case CAMERA_ANGLE_QUERY:
                int angleResult = robot.cameraAngleQuery();
                out.println(new Message<>(CAMERA_ANGLE_QUERY, angleResult));
                return true;
        }
        return true;
    }


    /**
     * Handles all other instructions.
     * Feedback is directly send over the output-stream.
     *
     * @param instruction   Instruction to be performed.
     * @return              A status.
     */
    private boolean processOtherInstruction(Message instruction) {
        switch(instruction.getMnemonic()) {
            case SHUTDOWN:
                out.println(DISCONNECT);
                return robot.shutdown();
            case DISCONNECT:
                out.println(DISCONNECT);
                return robot.disconnect();
            default:
                out.println(UNSUPPORTED_INSTRUCTION);
                return robot.handleUnsupportedInstruction(instruction);

        }
    }
}
