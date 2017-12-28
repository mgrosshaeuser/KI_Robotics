package ki.robotics.rover;

import ki.robotics.datastructures.Instruction;
import ki.robotics.utility.InstructionSetTranscoder;

import java.io.PrintWriter;
import java.util.ArrayList;


/**
 * A Controller translating the Instruction-class mnemonics into method-calls on an implementation of
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
    public BotController(boolean isSimulation) {
        this.out = null;
        this.jobQueue = new ArrayList<>();
        this.transcoder = new InstructionSetTranscoder();
        this.robot = isSimulation ? new SimulatedRover() : Sojourner.getInstance();
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

        out.println("SFIN");

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
            case "BTRF":
                status = robot.botTravelForward(parameter);
                out.println(instruction);
                break;
            case "BTRB":
                status = robot.botTravelBackward(parameter);
                out.print(instruction);
                break;
            case "BTNL":
                status = robot.botTurnLeft(parameter);
                out.println(instruction);
                break;
            case "BTNR":
                status = robot.botTurnRight(parameter);
                out.println(instruction);
                break;
            case "STNL":
                status = robot.sensorHeadTurnLeft(parameter);
                out.println(instruction);
                break;
            case "STNR":
                status = robot.sensorHeadTurnRight(parameter);
                out.println(instruction);
                break;
            case "SRST":
                status = robot.sensorHeadReset();
                out.println(instruction);
                break;
            case "MCLR":
                status = true;
                int color = robot.measureColor();
                out.println(new Instruction(instruction.getMnemonic(), color));
                break;
            case "MDST":
                status = true;
                double distance = robot.measureDistance();
                out.println(new Instruction(instruction.getMnemonic(), distance));
                break;
            case "USTW":
                //TODO Implementation
                break;
            case "POSE":
                //TODO Implementation
                break;
            case "SHTD":
                status = robot.shutdown();
                out.println("DCNT");
                break;
            case "DCNT":
                status = robot.disconnect();
                out.println("DCNT");
                break;
            default:
                status = robot.handleUnsupportedInstruction(instruction);
                out.println("IERR");
                break;
        }
        return status;
    }
}
