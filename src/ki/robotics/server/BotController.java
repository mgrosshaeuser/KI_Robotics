package ki.robotics.server;

import ki.robotics.utility.crisp.Instruction;
import ki.robotics.robot.Robot;
import ki.robotics.robot.RoverSimulation;
import ki.robotics.robot.Sojourner;
import ki.robotics.utility.crisp.InstructionSetTranscoder;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import static ki.robotics.utility.crisp.CRISP.*;

/**
 * A GUIComController translating the Instruction-class mnemonics into method-calls on an implementation of
 * the Robot-interface.
 *
 * @version 1.0, 12/26/17
 */
class BotController {
    private PrintWriter out;
    private ArrayList<Instruction> jobQueue;
    private InstructionSetTranscoder transcoder;
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
        this.transcoder = new InstructionSetTranscoder();
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
        this.transcoder = new InstructionSetTranscoder();
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
        ArrayList<Instruction> commands= transcoder.decodeRequest(instructionSequence);
        jobQueue.addAll(commands);
        boolean stayConnected = true;

        while (stayConnected  &&  ! jobQueue.isEmpty()) {
            Instruction instruction = jobQueue.remove(0);
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
    private boolean processInstruction(Instruction instruction) {
        boolean status;

        switch (instruction.getInstructionGroup()) {
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
    private boolean processBotInstruction(Instruction instruction) {
        if (instruction.getMnemonic().equals(BOT_RETURN_POSE)) {
            //TODO Implementation
            return true;
        }

        double parameter = ((Instruction.SingleFloatInstruction)instruction).getParameter();

        switch (instruction.getMnemonic()) {
            case BOT_TRAVEL_FORWARD:
                double travelledForward = robot.botTravelForward(parameter);
                out.println(new Instruction.SingleFloatInstruction(BOT_INSTRUCTION, instruction.getMnemonic(), travelledForward));
                return true;
            case BOT_TRAVEL_BACKWARD:
                double travelledBackward = robot.botTravelBackward(parameter);
                out.println(new Instruction.SingleFloatInstruction(BOT_INSTRUCTION,instruction.getMnemonic(), travelledBackward));
                return true;
            case BOT_TURN_LEFT:
                out.println(instruction);
                return robot.botTurnLeft(parameter);
            case BOT_TURN_RIGHT:
                out.println(instruction);
                return robot.botTurnRight(parameter);
            default:
                out.println(new Instruction(OTHER_INSTRUCTION, UNSUPPORTED_INSTRUCTION));
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
    private boolean processSensorInstruction(Instruction instruction) {
        switch (instruction.getMnemonic()) {
            case SENSOR_TURN_LEFT:
                out.println(instruction);
                return robot.sensorHeadTurnLeft(((Instruction.SingleIntInstruction)instruction).getParameter());
            case SENSOR_TURN_RIGHT:
                out.println(instruction);
                return robot.sensorHeadTurnRight(((Instruction.SingleIntInstruction)instruction).getParameter());
            case SENSOR_RESET:
                out.println(instruction);
                return robot.sensorHeadReset();
            case SENSOR_MEASURE_COLOR:
                int color = robot.measureColor();
                out.println(new Instruction.SingleIntInstruction(SENSOR_INSTRUCTION, SENSOR_MEASURE_COLOR, color));
                return true;
            case SENSOR_SINGLE_DISTANCE_SCAN:
                double distance = robot.measureDistance();
                out.println(new Instruction.SingleFloatInstruction(SENSOR_INSTRUCTION, SENSOR_SINGLE_DISTANCE_SCAN, distance));
                return true;
            case SENSOR_THREE_WAY_SCAN:
                double[] tws = robot.ultrasonicThreeWayScan();
                out.println(new Instruction.SingleFloatInstruction(SENSOR_INSTRUCTION, THREE_WAY_SCAN_LEFT, tws[0]));
                out.println(new Instruction.SingleFloatInstruction(SENSOR_INSTRUCTION, THREE_WAY_SCAN_CENTER, tws[1]));
                out.println(new Instruction.SingleFloatInstruction(SENSOR_INSTRUCTION, THREE_WAY_SCAN_RIGHT, tws[2]));
                out.println(SENSOR_THREE_WAY_SCAN);
                return true;
            case STAY_ON_WHITE_LINE:
                robot.setStayOnWhiteLine(true);
            default:
                out.println(new Instruction(OTHER_INSTRUCTION, UNSUPPORTED_INSTRUCTION));
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
    private boolean processCameraInstruction(Instruction instruction) {
        switch (instruction.getMnemonic()) {
            case CAMERA_GENERAL_QUERY:
                out.println(new Instruction.MultiIntInstruction(CAMERA_INSTRUCTION, CAMERA_GENERAL_QUERY, robot.cameraGeneralQuery()));
                return  true;
            case CAMERA_SINGLE_SIGNATURE_QUERY:
                int signature = ((Instruction.MultiIntInstruction)instruction).getParameters()[0];
                int[] singleSignatureResult = robot.cameraSignatureQuery(signature);
                out.println(new Instruction.MultiIntInstruction(CAMERA_INSTRUCTION, CAMERA_SINGLE_SIGNATURE_QUERY, singleSignatureResult));
                return true;
            case CAMERA_ALL_SIGNATURES_QUERY:
                int[][] allSignatures = robot.cameraAllSignaturesQuery();
                for (int i = 0  ;  i < allSignatures.length  ;  i++) {
                    out.println(new Instruction.MultiIntInstruction(CAMERA_INSTRUCTION, CAMERA_SIGNATURE_BASE + i, allSignatures[i]));
                }
                return true;
            case CAMERA_COLOR_CODE_QUERY:
                int colorCode = ((Instruction.MultiIntInstruction)instruction).getParameters()[0];
                int[] colorCodeResponse = robot.cameraColorCodeQuery(colorCode);
                int[] colorCodeResult = new int[colorCodeResponse.length + 1];
                colorCodeResult[0] = colorCode;
                System.arraycopy(colorCodeResponse, 0, colorCodeResult, 1, colorCodeResponse.length);
                out.println(new Instruction.MultiIntInstruction(CAMERA_INSTRUCTION, CAMERA_COLOR_CODE_QUERY, colorCodeResult));
                return true;
            case CAMERA_ANGLE_QUERY:
                int angleResult = robot.cameraAngleQuery();
                out.println(new Instruction.SingleIntInstruction(CAMERA_INSTRUCTION, CAMERA_ANGLE_QUERY, angleResult));
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
    private boolean processOtherInstruction(Instruction instruction) {
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
