package ki.robotics.client;

import ki.robotics.client.MCL.Configuration;
import ki.robotics.client.MCL.MCL_Display;
import ki.robotics.client.MCL.MCL_Provider;
import ki.robotics.client.MCL.SensorModel;
import ki.robotics.utility.crisp.Instruction;
import ki.robotics.utility.crisp.InstructionSequence;
import ki.robotics.utility.crisp.InstructionSetTranscoder;
import ki.robotics.utility.pixyCam.DTOAngleQuery;
import ki.robotics.utility.pixyCam.DTOColorCodeQuery;
import ki.robotics.utility.pixyCam.DTOGeneralQuery;
import ki.robotics.utility.pixyCam.DTOSignatureQuery;

import java.util.ArrayList;

import static ki.robotics.utility.crisp.CRISP.*;

/**
 * Monte-Carlo-Localization including a GUI for presentation of results.
 *
 * @version 1.2, 01/02/18
 */
public class GUIComController implements ComController {
    private final MCL_Display window;
    private MCL_Provider mclProvider;
    private final InstructionSetTranscoder transcoder;
    private final SensorModel roverModel;
    private Configuration configuration;
    private Thread t;



    /**
     * Constructor.
     */
    public GUIComController() {
        this.window = new MCL_Display(this);
        this.transcoder = new InstructionSetTranscoder();
        this.roverModel = new SensorModel();
    }


    /**
     * Stars a new thread to handle the communication with the server (robot).
     *
     * @param configuration A run-configuration, chosen by the user.
     */
    @Override
    public void start(Configuration configuration) {
        this.mclProvider = window.getMclProvider();
        this.configuration = configuration;
        t = new Thread(new Communicator(Main.HOST, Main.PORT, this));
        t.setDaemon(true);
        t.start();
    }


    /**
     * Stops the communications-thread.
     */
    @Override
    public void stop() {
        if (t != null) {
            Communicator.running = false;
            t= null;
        }
    }


    /**
     * Returns the initial request, supposed to be sent to the robot right after the connection is established.
     *
     * @return  The initial request to the robot.
     */
    @Override
    public String getInitialRequest() {
        InstructionSequence sequence = new InstructionSequence();
        if (configuration.isOneDimensional()) {
            sequence.sensorReset().botEnableLineFollower().perform(configuration.getSensingInstructions());
        } else {
            sequence.sensorReset().botDisableLineFollower().perform(configuration.getSensingInstructions());
        }
        return sequence.toString();
    }


    /**
     * Formulates the next request based on the responses that arrived so far.
     * Called from the server every time a previous instruction sequence was completed.
     *
     * @return  Ongoing instructions-requests.
     */
    @Override
    public String getNextRequest() {
        int bumper = 18; //additional 10cm for Soujourner delta ultra sonic sensor to axis

        InstructionSequence sequence = new InstructionSequence();
        if (configuration.isOneDimensional()) {
            return getNextInstructionSequenceForOneDimension().toString();
        } else {
            if (configuration.isWithCamera()  &&  mclProvider.isLocalizationDone()) {
                return getNextRequestWithCamera(bumper).toString();
            } else {
                return getNextRequestForTwoDimensions(bumper).toString();
            }
        }
    }


    /**
     * Formulates the next request (InstructionSequence) for the one-dimensional map.
     *
     * @return  InstructionSequence for the next request.
     */
    private InstructionSequence getNextInstructionSequenceForOneDimension() {
        int stepSize = configuration.getStepSize();
        ArrayList<String > scans = configuration.getSensingInstructions();
        return new InstructionSequence().botTravelForward(stepSize).perform(scans);
    }


    /**
     * Formulates the next request (InstructionSequence) for the two-dimensional map.
     *
     * @return  InstructionSequence for the next request.
     */
    private InstructionSequence getNextRequestForTwoDimensions(int bumper) {
        int stepSize = configuration.getStepSize();
        ArrayList<String > scans = configuration.getSensingInstructions();
        double center = roverModel.getDistanceToCenter(), left = roverModel.getDistanceToLeft(), right = roverModel.getDistanceToRight();

        int randomNumberSoBoDoesntStutterInFrontOfWall = 4;

        if(center > bumper + randomNumberSoBoDoesntStutterInFrontOfWall){
            return new InstructionSequence().botTravelForward(stepSize).perform(scans);
        }else if(right > left  &&  right > bumper){
            return new InstructionSequence().botTurnRight(90).botTravelForward(stepSize).perform(scans);
        }else if (left > right  &&  left > bumper){
            return new InstructionSequence().botTurnLeft(90).botTravelForward(stepSize).perform(scans);
        } else {
            return new InstructionSequence().botTurnRight(180).botTravelForward(stepSize).perform(scans);
        }
    }


    /**
     * Formulates the next request (InstructionSequence) for the two-dimensional map with camera usage.
     *
     * @return  InstructionSequence for the next request.
     */
    private InstructionSequence getNextRequestWithCamera(int bumper) {
        if (mclProvider.getEstimatedBotPoseDeviation() <= configuration.getAcceptableTolerance()) {
            mclProvider.badParticlesFinalKill();
            window.repaint();
            return new InstructionSequence().disconnect();
        }
        int stepSize = configuration.getStepSize();
        ArrayList<String > scans = configuration.getSensingInstructions();
        double center = roverModel.getDistanceToCenter(), left = roverModel.getDistanceToLeft(), right = roverModel.getDistanceToRight();

        int randomNumberSoBoDoesntStutterInFrontOfWall = 4;

        InstructionSequence n = new InstructionSequence().perform(scans).botTurnRight(60).perform(scans).botTurnRight(60).perform(scans).botTurnRight(60).perform(scans).botTurnRight(60).perform(scans).botTurnRight(60).perform(scans).botTurnRight(60).perform(scans);

        if(center > bumper + randomNumberSoBoDoesntStutterInFrontOfWall){
            return new InstructionSequence().botTravelForward(stepSize).append(n);
        }else if(right > left  &&  right > bumper){
            return new InstructionSequence().botTurnRight(90).botTravelForward(stepSize).append(n);
        }else if (left > right  &&  left > bumper){
            return new InstructionSequence().botTurnLeft(90).botTravelForward(stepSize).append(n);
        } else {
            return new InstructionSequence().botTurnLeft(180).botTravelForward(stepSize).append(n);
        }
    }



    /**
     * Routes the response from the robot to response-handling methods and repaints the display.
     *
     * @param botResponse  A single response from the robot.
     */
    @Override
    public void handleResponse(String botResponse) {
        Instruction response = transcoder.decodeInstruction(botResponse);
        switch (response.getInstructionGroup()) {
            case BOT_INSTRUCTION:
                handleBotResponse(response);
                break;
            case SENSOR_INSTRUCTION:
                handleSensorResponse(response);
                break;
            case CAMERA_INSTRUCTION:
                handleCameraResponse(response);
                break;
            default:
                handleOtherResponse(response);
                break;
        }
        if (configuration.stopWhenDone()  && ! configuration.isWithCamera()  &&  mclProvider.isLocalizationDone()) {
            mclProvider.badParticlesFinalKill();
            window.repaint();
            stop();
        }
        window.repaint();
    }


    /**
     * Handles responses regarding movement of the robot.
     *
     * @param response   The instruction-response.
     */
    private void handleBotResponse(Instruction response) {
        // Robot-responses without parameter.
        switch (response.getMnemonic()) {
            case BOT_RETURN_POSE:
                return;
            case BOT_LINE_FOLLOWING_ENABLED:
                return;
            case BOT_LINE_FOLLOWING_DISABLED:
                return;
            case BOT_U_TURN:
                ((Configuration.ConfigOneD)configuration).flipDirection();
                mclProvider.turnFull(180);
                return;
        }

        // Robot-responses with one floating-point parameter
        double parameter = ((Instruction.SingleFloatInstruction)response).getParameter();
        switch (response.getMnemonic()) {
            case BOT_TRAVEL_FORWARD:
                mclProvider.translateParticle((float)parameter);
                return;
            case BOT_TRAVEL_BACKWARD:
                mclProvider.translateParticle((float)(-parameter));
                return;
            case BOT_TURN_LEFT:
                mclProvider.turnFull((int) Math.abs(parameter));
                return;
            case BOT_TURN_RIGHT:
                mclProvider.turnFull((int) Math.abs(parameter) * -1);
                return;
        }
    }


    /**
     * Handles responses regarding the sensors and turning of the sensor-head.
     *
     * @param response  The instruction-response.
     */
    private void handleSensorResponse(Instruction response) {
        switch (response.getMnemonic()) {
            case SENSOR_TURN_LEFT:
                int leftAngle = ((Instruction.SingleIntInstruction)response).getParameter();
                roverModel.setSensorHeadPosition(leftAngle);
                break;
            case SENSOR_TURN_RIGHT:
                int rightAngle = ((Instruction.SingleIntInstruction)response).getParameter();
                roverModel.setSensorHeadPosition(-rightAngle);
                break;
            case SENSOR_MEASURE_COLOR:
                int color = ((Instruction.SingleIntInstruction)response).getParameter();
                roverModel.setColor(color);
                break;
            case THREE_WAY_SCAN_LEFT:
                double distanceLeft = ((Instruction.SingleFloatInstruction)response).getParameter();
                roverModel.setDistanceToLeft((float) distanceLeft);
                break;
            case THREE_WAY_SCAN_CENTER:
                double distanceCenter = ((Instruction.SingleFloatInstruction)response).getParameter();
                roverModel.setDistanceToCenter((float) distanceCenter);
                break;
            case THREE_WAY_SCAN_RIGHT:
                double distanceRight = ((Instruction.SingleFloatInstruction)response).getParameter();
                roverModel.setDistanceToRight((float) distanceRight);
                break;
            case SENSOR_THREE_WAY_SCAN:
                mclProvider.recalculateParticleWeight(roverModel);
                break;
            case SENSOR_RESET:
                roverModel.setSensorHeadPosition(0);
                break;
            case SENSOR_SINGLE_DISTANCE_SCAN:
                measureDistance1D(response);
                mclProvider.recalculateParticleWeight(roverModel);
                break;
        }
    }


    /**
     * Handles responses regarding the camera.
     *
     * @param response  The instruction-response.
     */
    private  void handleCameraResponse(Instruction response) {
        switch (response.getMnemonic()) {
            case CAMERA_GENERAL_QUERY:
                int[] generalQuery = ((Instruction.MultiIntInstruction)response).getParameters();
                roverModel.setGeneralQuery(new DTOGeneralQuery(generalQuery));
                break;
            case CAMERA_ANGLE_QUERY:
                int angleQuery = ((Instruction.SingleIntInstruction)response).getParameter();
                roverModel.setAngleQuery(new DTOAngleQuery(angleQuery));
                break;
            case CAMERA_COLOR_CODE_QUERY:
                int[] colorCodeQuery = ((Instruction.MultiIntInstruction)response).getParameters();
                roverModel.setColorCodeQuery(new DTOColorCodeQuery(colorCodeQuery));
                break;
            case CAMERA_SINGLE_SIGNATURE_QUERY:
                int [] signatureQuery = ((Instruction.MultiIntInstruction)response).getParameters();
                roverModel.setUnspecifiedSignatureQuery(new DTOSignatureQuery(signatureQuery));
                break;
            case CAMERA_SIGNATURE_1:
                int [] signatureQuery1 = ((Instruction.MultiIntInstruction)response).getParameters();
                roverModel.setSignatureQuery1(new DTOSignatureQuery(signatureQuery1));
                break;
            case CAMERA_SIGNATURE_2:
                int [] signatureQuery2 = ((Instruction.MultiIntInstruction)response).getParameters();
                roverModel.setSignatureQuery2(new DTOSignatureQuery(signatureQuery2));
                break;
            case CAMERA_SIGNATURE_3:
                int [] signatureQuery3 = ((Instruction.MultiIntInstruction)response).getParameters();
                roverModel.setSignatureQuery3(new DTOSignatureQuery(signatureQuery3));
                break;
            case CAMERA_SIGNATURE_4:
                int [] signatureQuery4 = ((Instruction.MultiIntInstruction)response).getParameters();
                roverModel.setSignatureQuery4(new DTOSignatureQuery(signatureQuery4));
                break;
            case CAMERA_SIGNATURE_5:
                int [] signatureQuery5 = ((Instruction.MultiIntInstruction)response).getParameters();
                roverModel.setSignatureQuery5(new DTOSignatureQuery(signatureQuery5));
                break;
            case CAMERA_SIGNATURE_6:
                int [] signatureQuery6 = ((Instruction.MultiIntInstruction)response).getParameters();
                roverModel.setSignatureQuery6(new DTOSignatureQuery(signatureQuery6));
                break;
            case CAMERA_SIGNATURE_7:
                int [] signatureQuery7 = ((Instruction.MultiIntInstruction)response).getParameters();
                roverModel.setSignatureQuery7(new DTOSignatureQuery(signatureQuery7));
                break;
        }
        if (mclProvider.isLocalizationDone()) {
            mclProvider.recalculateParticleWeight(roverModel);
        }
    }


    /**
     * Handles all other responses.
     *
     * @param response  The instruction-response.
     */
    private void handleOtherResponse(Instruction response) {
        switch (response.getMnemonic()) {
            case END_OF_INSTRUCTION_SEQUENCE:
                break;
        }
    }


    /**
     * Updates the sensor-model for left, front or right distance from a single distance measurement, depending
     * on the current orientation of the sensor-head.
     *
     * @param instruction   The instruction-response.
     */
    private void measureDistance1D(Instruction instruction) {
        float angle = roverModel.getSensorHeadPosition();
        boolean measurementLeft = angle > 45;
        boolean measurementRight = angle < -45;

        double param = ((Instruction.SingleFloatInstruction)instruction).getParameter();

        if (measurementLeft) {
            roverModel.setDistanceToLeft((float) param);
        } else if (measurementRight) {
            roverModel.setDistanceToRight((float)param);
        } else {
            roverModel.setDistanceToCenter((float)param);
        }
    }
}
