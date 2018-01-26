package ki.robotics.server;

import ki.robotics.utility.crisp.Instruction;
import ki.robotics.robot.Robot;
import ki.robotics.robot.RoverSimulation;
import ki.robotics.robot.Sojourner;
import ki.robotics.utility.crisp.InstructionSetTranscoder;

import java.io.PrintWriter;
import java.util.ArrayList;

import static ki.robotics.utility.crisp.CRISP.*;

/**
 * A GUIComController translating the Instruction-class mnemonics into method-calls on an implementation of
 * the Robot-interface.
 *
 * @version 1.0, 12/26/17
 */
public class BotController {
    private PrintWriter out;
    private ArrayList<Instruction> jobQueue;
    private InstructionSetTranscoder transcoder;
    private Robot robot;



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
     * 'SFIN' signals the client the completion of an instruction-sequence.
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

        out.println(INSTRUCTION_SEQUENCE_FINISHED);

        return stayConnected;
    }



    /**
     * Processes an Instruction by mapping the mnemonic to a method-invocation on the Robot.
     * Feedback, such as sensor-readout is directly send over the output-stream.
     *
     * @param instruction   The instruction to be performed.
     * @return              The status resulting from the processing of the Instruction; Decision about
     *                      keeping or terminating the connection.
     */
    private boolean processInstruction(Instruction instruction) {
        boolean status = false;
        double parameter = instruction.getParameter();

        switch(instruction.getMnemonic()) {
            case BOT_TRAVEL_FORWARD:
                status = true;
                out.println(new Instruction(instruction.getMnemonic(), robot.botTravelForward(parameter)));
                break;
            case BOT_TRAVEL_BACKWARD:
                status = true;
                out.println(new Instruction(instruction.getMnemonic(), robot.botTravelBackward(parameter)));
                break;
            case BOT_TURN_LEFT:
                status = robot.botTurnLeft(parameter);
                out.println(instruction);
                break;
            case BOT_TURN_RIGHT:
                status = robot.botTurnRight(parameter);
                out.println(instruction);
                break;
            case SENSOR_TURN_LEFT:
                status = robot.sensorHeadTurnLeft(parameter);
                out.println(instruction);
                break;
            case SENSOR_TURN_RIGHT:
                status = robot.sensorHeadTurnRight(parameter);
                out.println(instruction);
                break;
            case SENSOR_RESET:
                status = robot.sensorHeadReset();
                out.println(instruction);
                break;
            case MEASURE_COLOR:
                status = true;
                int color = robot.measureColor();
                out.println(instruction.getMnemonic() + " " + color);
                break;
            case N_WAY_SCAN:
                status = true;
                double distance = robot.measureDistance();
                out.println(new Instruction(instruction.getMnemonic(), distance));
                break;
            case THREE_WAY_SCAN:
                status = true;
                double[] tws = robot.ultrasonicThreeWayScan();
                out.println(new Instruction(THREE_WAY_SCAN_LEFT, tws[0]));
                out.println(new Instruction(THREE_WAY_SCAN_CENTER, tws[1]));
                out.println(new Instruction(THREE_WAY_SCAN_RIGHT, tws[2]));
                out.println(THREE_WAY_SCAN);
                break;
            case RETURN_POSE:
                //TODO Implementation
                break;
            case CAMERA_GENERAL_QUERY:
                status = true;
                out.println(CAMERA_GENERAL_QUERY + " " + robot.cameraGeneralQuery());
                break;
            case CAMERA_SINGLE_SIGNATURE_QUERY:
                status = true;
                out.println(CAMERA_SINGLE_SIGNATURE_QUERY + " " + robot.cameraSignatureQuery((int) Math.round(parameter)));
                break;
            case CAMERA_ALL_SIGNATURES_QUERY:
                status =true;
                String[] signatures = robot.cameraAllSignaturesQuery();
                for (int i = 0  ;  i < signatures.length  ;  i++) {
                    out.println("CSG" + (i+1) + " " + signatures[i]);
                }
                break;
            case CAMERA_COLOR_CODE_QUERY:
                status = true;
                out.println(CAMERA_COLOR_CODE_QUERY + " " + robot.cameraColorCodeQuery((int) Math.round(parameter)));
                break;
            case CAMERA_ANGLE_QUERY:
                status = true;
                out.println(CAMERA_ANGLE_QUERY + " " + robot.cameraAngleQuery());
                break;
            case BOT_SHUTDOWN:
                status = robot.shutdown();
                out.println(BOT_DISCONNECT);
                break;
            case BOT_DISCONNECT:
                status = robot.disconnect();
                out.println(BOT_DISCONNECT);
                break;
            default:
                status = robot.handleUnsupportedInstruction(instruction);
                out.println(UNSUPPORTED_INSTRUCTION);
                break;
        }
        return status;
    }
}
