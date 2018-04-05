package ki.robotics.client;

import ki.robotics.client.MCL.Configuration;
import ki.robotics.client.MCL.MCL_Display;
import ki.robotics.client.MCL.MCL_Provider;
import ki.robotics.client.MCL.SensorModel;
import ki.robotics.utility.crisp.InstructionSequence;
import ki.robotics.utility.crisp.Message;
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
    private final SensorModel roverModel;
    private Configuration configuration;
    private Thread t;



    /**
     * Constructor.
     */
    public GUIComController() {
        this.window = new MCL_Display(this);
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
            if (configuration.isWithCamera() ){
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

        InstructionSequence n = new InstructionSequence().perform(scans);

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
        ArrayList<Message> responses = Message.decodeTransmission(botResponse);
        for (Message response : responses) {
            switch (response.getMessageGroup()) {
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
            if (configuration.stopWhenDone() && !configuration.isWithCamera() && mclProvider.isLocalizationDone()) {
                mclProvider.badParticlesFinalKill();
                window.repaint();
                stop();
            }
            window.repaint();
        }
    }


    /**
     * Handles responses regarding movement of the robot.
     *
     * @param response   The instruction-response.
     */
    private void handleBotResponse(Message response) {
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
        switch (response.getMnemonic()) {
            case BOT_TRAVEL_FORWARD:
                mclProvider.translateParticle((double)response.getParameter());
                return;
            case BOT_TRAVEL_BACKWARD:
                mclProvider.translateParticle((double)response.getParameter() * -1);
                return;
            case BOT_TURN_LEFT:
                mclProvider.turnFull(Math.abs((int)response.getParameter()));
                return;
            case BOT_TURN_RIGHT:
                mclProvider.turnFull(Math.abs((int)response.getParameter()) * -1);
                return;
        }
    }


    /**
     * Handles responses regarding the sensors and turning of the sensor-head.
     *
     * @param response  The instruction-response.
     */
    private void handleSensorResponse(Message response) {
        switch (response.getMnemonic()) {
            case SENSOR_TURN_LEFT:
                roverModel.setSensorHeadPosition((int)response.getParameter());
                break;
            case SENSOR_TURN_RIGHT:
                roverModel.setSensorHeadPosition((int)response.getParameter() * -1);
                break;
            case SENSOR_MEASURE_COLOR:
                roverModel.setColor((int)response.getParameter());
                break;
            case SENSOR_THREE_WAY_SCAN:
                roverModel.setDistanceToLeft((double) response.getParameters()[0]);
                roverModel.setDistanceToCenter((double) response.getParameters()[1]);
                roverModel.setDistanceToRight((double) response.getParameters()[2]);
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
    private  void handleCameraResponse(Message response) {
        switch (response.getMnemonic()) {
            case CAMERA_GENERAL_QUERY:
                roverModel.setGeneralQuery(new DTOGeneralQuery(response.getParameters()));
                break;
            case CAMERA_ANGLE_QUERY:
                roverModel.setAngleQuery(new DTOAngleQuery((int) response.getParameter()));
                break;
            case CAMERA_COLOR_CODE_QUERY:
                roverModel.setColorCodeQuery(new DTOColorCodeQuery(response.getParameters()));
                break;
            case CAMERA_SINGLE_SIGNATURE_QUERY:
                roverModel.setSignatureQuery(new DTOSignatureQuery(response.getParameters()));
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
    private void handleOtherResponse(Message response) {
        switch (response.getMnemonic()) {
            case END_OF_INSTRUCTION_SEQUENCE:
                break;
        }
    }


    /**
     * Updates the sensor-model for left, front or right distance from a single distance measurement, depending
     * on the current orientation of the sensor-head.
     *
     * @param response   The instruction-response.
     */
    private void measureDistance1D(Message response) {
        double angle = roverModel.getSensorHeadPosition();
        boolean measurementLeft = angle > 45;
        boolean measurementRight = angle < -45;

        double param = (double)response.getParameters()[0];
//        double param = ((Instruction.SingleFloatInstruction)instruction).getParameters();

        if (measurementLeft) {
            roverModel.setDistanceToLeft((float) param);
        } else if (measurementRight) {
            roverModel.setDistanceToRight((float)param);
        } else {
            roverModel.setDistanceToCenter((float)param);
        }
    }
}
