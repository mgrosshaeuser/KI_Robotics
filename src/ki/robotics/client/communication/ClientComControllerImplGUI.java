package ki.robotics.client.communication;

import ki.robotics.client.ClientFactory;
import ki.robotics.client.GUI.GuiConfiguration;
import ki.robotics.client.GUI.GuiController;
import ki.robotics.client.MCL.LocalizationProvider;
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
 * Communication-Controller for:
 *  - generating instructions based on the sensor-model
 *  - interpreting responses from the robot (updating sensor-model and localization-provider).
 */
public final class ClientComControllerImplGUI extends ClientComControllerImpl {
    private GuiController guiController;
    private LocalizationProvider localizationProvider;
    private SensorModel sensorModel;
    private GuiConfiguration guiConfiguration;

    private RequestGenerator requestGenerator;
    private BotResponseHandler botResponseHandler;

    private Thread communicationThread;
    private boolean isStopped;



    /**
     * Constructor.
     *
     * @param host  Server-host
     * @param port  Server-port
     */
    public ClientComControllerImplGUI(String host, int port) {
        super(host, port);
        this.isStopped = true;
    }


    /**
     * Sets the reference to the GUI-controller.
     *
     * @param guiController The reference to the GUI-controller
     */
    public void setGuiController(GuiController guiController) {
        this.guiController = guiController;
    }


    /**
     * Sets the reference to the sensor-model.
     *
     * @param sensorModel   The reference to the sensor-model
     */
    public void setSensorModel(SensorModel sensorModel) {
        this.sensorModel = sensorModel;
    }



    /**
     * Performs pre-start assignments and creates a new thread to handle the communication.
     */
    @Override
    public void start() {
        this.isStopped = false;
        this.guiConfiguration = guiController.getUserSettings();
        this.localizationProvider = this.guiConfiguration.getLocalizationProvider();
        communicationThread = new Thread(new ClientCommunicator(this.host, this.port, this));
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
            this.isStopped = true;
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
        return isStopped;
    }


    /**
     * Initializes RequestGenerator and BotResponseHandler and returns the initial request, supposed to be sent to
     * the robot right after the connection is established.
     *
     * @return  The initial request to the robot.
     */
    @Override
    public String getInitialRequest() {
        this.requestGenerator = new RequestGenerator(guiConfiguration, sensorModel);
        this.botResponseHandler = new BotResponseHandler(localizationProvider, guiController, sensorModel);
        return requestGenerator.getInitialRequest();
    }



    /**
     * Asks the RequestGenerator for the next request to send to the robot, unless localization is
     * currently paused.
     * Called from the server every time a previous instruction sequence was completed.
     *
     * @return  Ongoing instructions-requests.
     */
    @Override
    public String getNextRequest() {
        if (guiConfiguration.isPaused()) {
            return null;
        } else {
            return requestGenerator.getNextRequest();
        }
    }





    /**
     * Forwards the response from the robot to response-handler.
     *
     * @param botResponse  A single response from the robot.
     */
    @Override
    public void handleResponse(String botResponse) {
        botResponseHandler.handleResponse(botResponse);
    }





    /**
     * Generates request addressing the robot, based on user-settings and sensor-model.
     */
    private class RequestGenerator {
        private final GuiConfiguration guiConfiguration;
        private final SensorModel sensorModel;
        private final SensingInstructionProvider sensingInstructionProvider;


        /**
         * Constructor.
         *
         * @param guiConfiguration  The user-settings
         * @param sensorModel   The sensor-model as decision-base
         */
        private RequestGenerator(GuiConfiguration guiConfiguration, SensorModel sensorModel) {
            this.guiConfiguration = guiConfiguration;
            this.sensorModel = sensorModel;
            this.sensingInstructionProvider = new SensingInstructionProvider(guiConfiguration);
        }


        /**
         * Returns the initial request for the robot.
         *
         * @return  The initial request for the robot
         */
        private String getInitialRequest() {
            InstructionSequence sequence = ClientFactory.createNewInstructionSequence();
            InstructionSequence scans = sensingInstructionProvider.provideSensingInstructions();
            if (guiConfiguration.isOneDimensional()) {
                sequence.sensorReset().botEnableLineFollower().append(scans);
            } else {
                sequence.sensorReset().botDisableLineFollower().append(scans);
            }
            return sequence.toString();
        }


        /**
         * Returns the next request for the ongoing localization.
         *
         * @return  The next request for the ongoing localization
         */
        private String getNextRequest() {
            int bumper = 18; //additional 10cm for Soujourner delta ultra sonic sensor to axis

            if (guiConfiguration.isOneDimensional()) {
                return getNextInstructionSequenceForOneDimension().toString();
            } else {
                if (guiConfiguration.isWithCamera() ){
                    return getNextInstructionSequenceWithCamera(bumper).toString();
                } else {
                    return getNextInstructionSequenceForTwoDimensions(bumper).toString();
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
            InstructionSequence sequence = ClientFactory.createNewInstructionSequence();
            return sequence.botTravelForward(stepSize).append(sensingInstructionProvider.provideSensingInstructions());
        }


        /**
         * Formulates the next request (InstructionSequence) for the two-dimensional map.
         *
         * @return  InstructionSequence for the next request.
         */
        private InstructionSequence getNextInstructionSequenceForTwoDimensions(int bumper) {
            int stepSize = guiConfiguration.getStepSize();
            InstructionSequence scans = sensingInstructionProvider.provideSensingInstructions();
            double center = sensorModel.getDistanceToCenter(), left = sensorModel.getDistanceToLeft(), right = sensorModel.getDistanceToRight();

            int randomNumberSoBoDoesntStutterInFrontOfWall = 4;
            InstructionSequence sequence = ClientFactory.createNewInstructionSequence();

            if(center > bumper + randomNumberSoBoDoesntStutterInFrontOfWall){
                return sequence.botTravelForward(stepSize).append(scans);
            }else if(right > left  &&  right > bumper){
                return sequence.botTurnRight().botTravelForward(stepSize).append(scans);
            }else if (left > right  &&  left > bumper){
                return sequence.botTurnLeft().botTravelForward(stepSize).append(scans);
            } else {
                return sequence.botTurnRight(180).botTravelForward(stepSize).append(scans);
            }
        }


        /**
         * Formulates the next request (InstructionSequence) for the two-dimensional map with camera usage.
         *
         * @return  InstructionSequence for the next request.
         */
        private InstructionSequence getNextInstructionSequenceWithCamera(int bumper) {
            if (localizationProvider.getSpreadingAroundEstimatedBotPose() <= guiConfiguration.getAcceptableSpreading()) {
                localizationProvider.badParticlesFinalKill();
                guiController.repaintWindow();
                InstructionSequence sequence = ClientFactory.createNewInstructionSequence();
                return sequence.disconnect();
            }

            int stepSize = guiConfiguration.getStepSize();
            InstructionSequence scans = sensingInstructionProvider.provideSensingInstructions();
            double center = sensorModel.getDistanceToCenter(), left = sensorModel.getDistanceToLeft(), right = sensorModel.getDistanceToRight();

            int randomNumberSoBoDoesntStutterInFrontOfWall = 4;
            InstructionSequence sequence = ClientFactory.createNewInstructionSequence();

            if(center > bumper + randomNumberSoBoDoesntStutterInFrontOfWall){
                return sequence.botTravelForward(stepSize).append(scans);
            }else if(right > left  &&  right > bumper){
                return sequence.botTurnRight(90).botTravelForward(stepSize).append(scans);
            }else if (left > right  &&  left > bumper){
                return sequence.botTurnLeft(90).botTravelForward(stepSize).append(scans);
            } else {
                return sequence.botTurnLeft(180).botTravelForward(stepSize).append(scans);
            }
        }





        private class SensingInstructionProvider {
            private final GuiConfiguration guiConfiguration;


            /**
             * Constructor.
             *
             * @param guiConfiguration  The user-settings
             */
            private SensingInstructionProvider(GuiConfiguration guiConfiguration) {
                this.guiConfiguration = guiConfiguration;
            }


            /**
             * Returns an InstructionSequence addressing the sensors selected by the user.
             *
             * @return                  An InstructionSequence addressing the selected sensors
             */
            private InstructionSequence provideSensingInstructions() {
                if (guiConfiguration.isOneDimensional()) {
                    return provideSensingInstructionsForOneDim();
                }

                if (guiConfiguration.isTwoDimensional()) {
                    return provideSensingInstructionForTwoDim();
                }

                if (guiConfiguration.isWithCamera()) {
                    return provideSensingInstructionsForTwoDimWithCamera();
                }

                return ClientFactory.createNewInstructionSequence();
            }


            /**
             * Provides an InstructionSequence addressing the selected sensors in one-dimensional maps.
             *
             * @return                  An InstructionSequence addressing the selected sensors
             */
            private InstructionSequence provideSensingInstructionsForOneDim() {
                InstructionSequence sequence = ClientFactory.createNewInstructionSequence();

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
             * @return                  An InstructionSequence addressing the selected sensors
             */
            private InstructionSequence provideSensingInstructionForTwoDim() {
                InstructionSequence sequence = ClientFactory.createNewInstructionSequence();

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
             * @return                  An InstructionSequence addressing the selected sensors
             */
            private InstructionSequence provideSensingInstructionsForTwoDimWithCamera() {
                InstructionSequence sequence = ClientFactory.createNewInstructionSequence();

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
    }




    /**
     * Handler for robot-responses
     */
    private class BotResponseHandler {
        private final LocalizationProvider localizationProvider;
        private final GuiController guiController;
        private final SensorModel sensorModel;


        /**
         * Constructor.
         *
         * @param localizationProvider  The localization-provider used for the current localization
         * @param guiController The gui-controller of the currently used GUI
         * @param sensorModel   The sensor-model used in the current localization
         */
        BotResponseHandler(LocalizationProvider localizationProvider, GuiController guiController, SensorModel sensorModel) {
            this.localizationProvider = localizationProvider;
            this.guiController = guiController;
            this.sensorModel = sensorModel;
        }


        /**
         * Conditional routing of the robot-response to specialized methods.
         *
         * @param botResponse   The robot-response
         */
        private void handleResponse(String botResponse) {
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
                    sensorModel.setSensorHeadPosition((double)response.getParameter());
                    break;
                case SENSOR_TURN_RIGHT:
                    sensorModel.setSensorHeadPosition((double)response.getParameter() * -1);
                    break;
                case SENSOR_MEASURE_COLOR:
                    sensorModel.setColor((int)response.getParameter());
                    break;
                case SENSOR_THREE_WAY_SCAN:
                    sensorModel.setDistanceToLeft((double) response.getParameters()[0]);
                    sensorModel.setDistanceToCenter((double) response.getParameters()[1]);
                    sensorModel.setDistanceToRight((double) response.getParameters()[2]);
                    localizationProvider.recalculateParticleWeight(sensorModel);
                    break;
                case SENSOR_RESET:
                    sensorModel.setSensorHeadPosition(0);
                    break;
                case SENSOR_SINGLE_DISTANCE_SCAN:
                    evaluateSingleDistanceMeasurement(response);
                    localizationProvider.recalculateParticleWeight(sensorModel);
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
                    sensorModel.setGeneralQuery(new DTOGeneralQuery(response.getParameters()));
                    break;
                case CAMERA_ANGLE_QUERY:
                    sensorModel.setAngleQuery(new DTOAngleQuery((int) response.getParameter()));
                    break;
                case CAMERA_COLOR_CODE_QUERY:
                    sensorModel.setColorCodeQuery(new DTOColorCodeQuery(response.getParameters()));
                    break;
                case CAMERA_SINGLE_SIGNATURE_QUERY:
                    sensorModel.setSignatureQuery(new DTOSignatureQuery(response.getParameters()));
                    break;
            }
            if (localizationProvider.isLocalizationDone()) {
                localizationProvider.recalculateParticleWeight(sensorModel);
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
        private void evaluateSingleDistanceMeasurement(Message response) {
            double angle = sensorModel.getSensorHeadPosition();
            boolean measurementLeft = angle > 45;
            boolean measurementRight = angle < -45;

            double param = (double)response.getParameters()[0];

            if (measurementLeft) {
                sensorModel.setDistanceToLeft((float) param);
            } else if (measurementRight) {
                sensorModel.setDistanceToRight((float)param);
            } else {
                sensorModel.setDistanceToCenter((float)param);
            }
        }


    }




}
