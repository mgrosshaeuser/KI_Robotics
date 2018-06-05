package ki.robotics.client.communication;

import ki.robotics.client.ClientFactory;
import ki.robotics.client.GUI.GuiConfiguration;
import ki.robotics.client.GUI.GuiController;
import ki.robotics.client.MCL.LocalizationProvider;
import ki.robotics.client.Main;
import ki.robotics.client.SensorModel;
import ki.robotics.utility.crisp.InstructionSequence;
import ki.robotics.utility.crisp.Message;
import ki.robotics.utility.pixyCam.DTOAngleQuery;
import ki.robotics.utility.pixyCam.DTOColorCodeQuery;
import ki.robotics.utility.pixyCam.DTOGeneralQuery;
import ki.robotics.utility.pixyCam.DTOSignatureQuery;

import java.util.ArrayList;

import static ki.robotics.utility.crisp.CRISP.*;

/**
 * Communication-Controller for:
 *  - generating instructions based on the sensor-model
 *  - interpreting responses from the robot (updating sensor-model and localization-provider).
 */
public class ClientComControllerImplGUI implements ClientComController {
    private GuiController guiController;
    private LocalizationProvider localizationProvider;
    private final SensorModel roverModel;
    private GuiConfiguration guiConfiguration;
    private Thread communicationThread;
    private boolean isStoped;



    /**
     * Constructor.
     */
    public ClientComControllerImplGUI() {
        this.guiController = ClientFactory.createNewGuiController(this);
        this.roverModel = ClientFactory.createNewSensorModel();
        this.isStoped = true;
    }



    /**
     * Performs pre-start assignments and creates a new thread to handle the communication.
     */
    @Override
    public void start() {
        this.isStoped = false;
        this.guiConfiguration = guiController.getUserSettings();
        this.localizationProvider = this.guiConfiguration.getLocalizationProvider();
        communicationThread = new Thread(new ClientCommunicator(Main.HOST, Main.PORT, this));
        communicationThread.setDaemon(true);
        communicationThread.start();
    }


    /**
     * Stops the communications-thread.
     */
    @Override
    public void stop() {
        if (communicationThread != null) {
            ClientCommunicator.running = false;
            this.isStoped = true;
            communicationThread = null;
        }
    }


    /**
     * Allows the communication-thread to ask about the communication-status (stopped or ongoing).
     *
     * @return  boolean value to indicate stopped (true) or ongoing (false)
     */
    @Override
    public boolean isStopped() {
        return isStoped;
    }


    /**
     * Returns the initial request, supposed to be sent to the robot right after the connection is established.
     *
     * @return  The initial request to the robot.
     */
    @Override
    public String getInitialRequest() {
        InstructionSequence sequence = new InstructionSequence();
        InstructionSequence scans = provideSensingInstructionsFrom(guiConfiguration);
        if (guiConfiguration.isOneDimensional()) {
            sequence.sensorReset().botEnableLineFollower().append(scans);
        } else {
            sequence.sensorReset().botDisableLineFollower().append(scans);
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
        if (guiConfiguration.isPaused())
            return null;

        int bumper = 18; //additional 10cm for Soujourner delta ultra sonic sensor to axis

        if (guiConfiguration.isOneDimensional()) {
            return getNextInstructionSequenceForOneDimension().toString();
        } else {
            if (guiConfiguration.isWithCamera() ){
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
        int stepSize = guiConfiguration.getStepSize();
        return new InstructionSequence().botTravelForward(stepSize).append(provideSensingInstructionsFrom(guiConfiguration));
    }


    /**
     * Formulates the next request (InstructionSequence) for the two-dimensional map.
     *
     * @return  InstructionSequence for the next request.
     */
    private InstructionSequence getNextRequestForTwoDimensions(int bumper) {
        int stepSize = guiConfiguration.getStepSize();
        InstructionSequence scans = provideSensingInstructionsFrom(guiConfiguration);
        double center = roverModel.getDistanceToCenter(), left = roverModel.getDistanceToLeft(), right = roverModel.getDistanceToRight();

        int randomNumberSoBoDoesntStutterInFrontOfWall = 4;

        if(center > bumper + randomNumberSoBoDoesntStutterInFrontOfWall){
            return new InstructionSequence().botTravelForward(stepSize).append(scans);
        }else if(right > left  &&  right > bumper){
            return new InstructionSequence().botTurnRight().botTravelForward(stepSize).append(scans);
        }else if (left > right  &&  left > bumper){
            return new InstructionSequence().botTurnLeft().botTravelForward(stepSize).append(scans);
        } else {
            return new InstructionSequence().botTurnRight(180).botTravelForward(stepSize).append(scans);
        }
    }


    /**
     * Formulates the next request (InstructionSequence) for the two-dimensional map with camera usage.
     *
     * @return  InstructionSequence for the next request.
     */
    private InstructionSequence getNextRequestWithCamera(int bumper) {
        if (localizationProvider.getSpreadingAroundEstimatedBotPose() <= guiConfiguration.getAcceptableSpreading()) {
            localizationProvider.badParticlesFinalKill();
            guiController.repaintWindow();
            return new InstructionSequence().disconnect();
        }

        int stepSize = guiConfiguration.getStepSize();
        InstructionSequence scans = provideSensingInstructionsFrom(guiConfiguration);
        double center = roverModel.getDistanceToCenter(), left = roverModel.getDistanceToLeft(), right = roverModel.getDistanceToRight();

        int randomNumberSoBoDoesntStutterInFrontOfWall = 4;

        if(center > bumper + randomNumberSoBoDoesntStutterInFrontOfWall){
            return new InstructionSequence().botTravelForward(stepSize).append(scans);
        }else if(right > left  &&  right > bumper){
            return new InstructionSequence().botTurnRight(90).botTravelForward(stepSize).append(scans);
        }else if (left > right  &&  left > bumper){
            return new InstructionSequence().botTurnLeft(90).botTravelForward(stepSize).append(scans);
        } else {
            return new InstructionSequence().botTurnLeft(180).botTravelForward(stepSize).append(scans);
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
            if (guiConfiguration.isStopWhenDone() && !guiConfiguration.isWithCamera() && localizationProvider.isLocalizationDone()) {
                localizationProvider.badParticlesFinalKill();
                localizationProvider.saveLocalizationSequenceToFile();
                guiController.updateWindowAfterLocalizationFinished();
                guiController.repaintWindow();
                stop();
            }
            guiController.repaintWindow();
        }
    }


    /**
     * Handles responses regarding robot-motion..
     *
     * @param response   The instruction-response.
     */
    private void handleBotResponse(Message response) {
        switch (response.getMnemonic()) {
            case BOT_RETURN_POSE:
                break;
            case BOT_LINE_FOLLOWING_ENABLED:
                break;
            case BOT_LINE_FOLLOWING_DISABLED:
                break;
            case BOT_U_TURN:
                guiConfiguration.flipDirection();
                localizationProvider.turnParticles(180);
                break;
            case BOT_TRAVEL_FORWARD:
                localizationProvider.translateParticles((double)response.getParameter());
                break;
            case BOT_TRAVEL_BACKWARD:
                localizationProvider.translateParticles((double)response.getParameter() * -1);
                break;
            case BOT_TURN_LEFT:
                localizationProvider.turnParticles(Math.abs((double)response.getParameter()));
                break;
            case BOT_TURN_RIGHT:
                localizationProvider.turnParticles(Math.abs((double)response.getParameter()) * -1);
                break;
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
                roverModel.setSensorHeadPosition((double)response.getParameter());
                break;
            case SENSOR_TURN_RIGHT:
                roverModel.setSensorHeadPosition((double)response.getParameter() * -1);
                break;
            case SENSOR_MEASURE_COLOR:
                roverModel.setColor((int)response.getParameter());
                break;
            case SENSOR_THREE_WAY_SCAN:
                roverModel.setDistanceToLeft((double) response.getParameters()[0]);
                roverModel.setDistanceToCenter((double) response.getParameters()[1]);
                roverModel.setDistanceToRight((double) response.getParameters()[2]);
                localizationProvider.recalculateParticleWeight(roverModel);
                break;
            case SENSOR_RESET:
                roverModel.setSensorHeadPosition(0);
                break;
            case SENSOR_SINGLE_DISTANCE_SCAN:
                measureDistance1D(response);
                localizationProvider.recalculateParticleWeight(roverModel);
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
        if (localizationProvider.isLocalizationDone()) {
            localizationProvider.recalculateParticleWeight(roverModel);
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

        if (measurementLeft) {
            roverModel.setDistanceToLeft((float) param);
        } else if (measurementRight) {
            roverModel.setDistanceToRight((float)param);
        } else {
            roverModel.setDistanceToCenter((float)param);
        }
    }


    /**
     * Returns an InstructionSequence addressing the sensors selected by the user.
     *
     * @param guiConfiguration     The user-settings, including selected sensors
     * @return                  An InstructionSequence addressing the selected sensors
     */
    private InstructionSequence provideSensingInstructionsFrom(GuiConfiguration guiConfiguration) {
        if (guiConfiguration.isOneDimensional()) {
            return provideSensingInstructionsForOneDim(guiConfiguration);
        }

        if (guiConfiguration.isTwoDimensional()) {
            return provideSensingInstructionForTwoDim(guiConfiguration);
        }

        if (guiConfiguration.isWithCamera()) {
            return provideSensingInstructionsForTwoDimWithCamera(guiConfiguration);
        }

        return new InstructionSequence();
    }


    /**
     * Provides an InstructionSequence addressing the selected sensors in one-dimensional maps.
     *
     * @param guiConfiguration  The user-settings, including selected sensors
     * @return                  An InstructionSequence addressing the selected sensors
     */
    private InstructionSequence provideSensingInstructionsForOneDim(GuiConfiguration guiConfiguration) {
        InstructionSequence sequence = new InstructionSequence();

        if (guiConfiguration.isMeasureDistanceToLeft()) {
            sequence.sensorTurnLeft();
        } else {
            sequence.sensorTurnRight();
        }
        sequence.measureSingleDistance();

        return sequence;
    }


    /**
     * Provides an InstructionSequence addressing the selected sensors in two-dimensional maps.
     *
     * @param guiConfiguration  The user-settings, including selected sensors
     * @return                  An InstructionSequence addressing the selected sensors
     */
    private InstructionSequence provideSensingInstructionForTwoDim(GuiConfiguration guiConfiguration) {
        InstructionSequence sequence = new InstructionSequence();

        if (guiConfiguration.isUseLeftSensor()  &&  guiConfiguration.isUseFrontSensor()  &&  guiConfiguration.isUseRightSensor()) {
            sequence.measureAllDistances();
        } else {
            if (guiConfiguration.isUseLeftSensor()) { sequence.sensorTurnLeft().measureSingleDistance(); }
            if (guiConfiguration.isUseFrontSensor()) { sequence.sensorReset().measureSingleDistance(); }
            if (guiConfiguration.isUseRightSensor()) { sequence.sensorTurnRight().measureSingleDistance(); }
        }

        return sequence;
    }


    /**
     * Provides an InstructionSequence addressing the selected sensors in two-dimensional maps and camera.
     *
     * @param guiConfiguration  The user-settings, including selected sensors
     * @return                  An InstructionSequence addressing the selected sensors
     */
    private InstructionSequence provideSensingInstructionsForTwoDimWithCamera(GuiConfiguration guiConfiguration) {
        InstructionSequence sequence = new InstructionSequence();

        if (guiConfiguration.isUseGeneralQuery()) {sequence.cameraGeneralQuery(); }
        if (guiConfiguration.isUseGeneralQuery()  &&  guiConfiguration.isUseAngleQuery()) { sequence.camAngleQuery(); }
        if (guiConfiguration.isUseSignatureOne()) { sequence.camQuerySignature(1); }
        if (guiConfiguration.isUseSignatureTwo()) { sequence.camQuerySignature(2); }
        if (guiConfiguration.isUseSignatureThree()) { sequence.camQuerySignature(3); }
        if (guiConfiguration.isUseSignatureFour()) { sequence.camQuerySignature(4); }
        if (guiConfiguration.isUseSignatureFive()) { sequence.camQuerySignature(5); }
        if (guiConfiguration.isUseSignatureSix()) { sequence.camQuerySignature(6); }
        if (guiConfiguration.isUseSignatureSeven()) { sequence.camQuerySignature(7); }
        sequence.measureAllDistances();

        return sequence;
    }
}
