package ki.robotics.client.GUI.impl;

import ki.robotics.client.GUI.GuiConfiguration;
import ki.robotics.client.MCL.LocalizationProvider;
import ki.robotics.client.ClientFactory;
import ki.robotics.client.MCL.Particle;
import ki.robotics.client.MCL.WorldState;
import ki.robotics.utility.map.Map;
import ki.robotics.utility.map.MapProvider;

import java.util.ArrayList;

/**
 * The GUI-(data-)model, as implementation of interface GuiConfiguration, using sub-models for
 * - replay-data
 * - sensor-data
 * - camera-data
 * - movement-data
 * - localization-data
 */
public class GuiConfigurationImplClientModel implements GuiConfiguration {
    private final ReplayModel replayModel;
    private final SensorModel sensorModel;
    private final CameraModel cameraModel;
    private final MovementModel movementModel;
    private final LocalizationModel localizationModel;


    /**
     * Constructor
     */
    public GuiConfigurationImplClientModel() {
        this.replayModel = new ReplayModel();
        this.sensorModel = new SensorModel();
        this.cameraModel = new CameraModel();
        this.movementModel = new MovementModel(sensorModel);
        this.localizationModel = new LocalizationModel(this);
    }


    /**
     * Returns a reference to the replay-sub-model.
     *
     * @return  A reference to the replay-sub-model
     */
    ReplayModel getReplayModel() {
        return replayModel;
    }

    /**
     * Returns a reference to the sensor-sub-model.
     *
     * @return  A reference to the sensor-sub-model
     */
    SensorModel getSensorModel() {
        return sensorModel;
    }


    /**
     * Returns a reference to the camera-sub-model.
     *
     * @return  A reference to the camera-sub-model
     */
    CameraModel getCameraModel() {
        return cameraModel;
    }


    /**
     * Returns a reference to the movement-sub-model.
     *
     * @return  A reference to the movement-sub-model
     */
    MovementModel getMovementModel() {
        return movementModel;
    }


    /**
     * Returns a reference to the localization-sub-model.
     *
     * @return  A reference to the localization-sub-model
     */
    LocalizationModel getLocalizationModel() {
        return localizationModel;
    }





    // Implementation of interface GuiConfiguration as forwarding to the localization-sub-model

    @Override
    public LocalizationProvider getLocalizationProvider() { return localizationModel.getLocalizationProvider(); }

    @Override
    public Map getMap() { return localizationModel.getMap(); }

    @Override
    public String getMapKey() { return localizationModel.getMapKey(); }

    @Override
    public int getNumberOfParticles() { return localizationModel.getNumberOfParticles(); }

    @Override
    public boolean isPaused() { return localizationModel.isPaused(); }

    @Override
    public boolean isOneDimensional() { return localizationModel.isOneDimensional(); }

    @Override
    public boolean isTwoDimensional() { return localizationModel.isTwoDimensional(); }

    @Override
    public boolean isWithCamera() { return localizationModel.isWithCamera(); }

    @Override
    public boolean isInReplayMode() { return localizationModel.isInReplayMode(); }

    @Override
    public int getStepSize() { return localizationModel.getStepSize(); }

    @Override
    public boolean isStopWhenDone() { return localizationModel.isStopWhenDone(); }

    @Override
    public int getAcceptableSpreading() { return localizationModel.getAcceptableSpreading(); }





    // Implementation of interface GuiConfiguration as forwarding to the movement-sub-model

    @Override
    public boolean isStartFromLeft() { return movementModel.isStartFromLeft(); }

    @Override
    public boolean isStartFromRight() { return movementModel.isStartFromRight(); }

    @Override
    public void flipDirection() { movementModel.flipDirection(); }

    @Override
    public boolean isUseRightAngles() { return movementModel.isUseRightAngles(); }

    @Override
    public boolean isUseFreeAngles() { return movementModel.isUseFreeAngles(); }





    // Implementation of interface GuiConfiguration as forwarding to the sensor-sub-model

    @Override
    public boolean isMeasureDistanceToLeft() { return sensorModel.isMeasureDistanceToLeft(); }

    @Override
    public boolean isMeasureDistanceToRight() { return sensorModel.isMeasureDistanceToRight(); }

    @Override
    public boolean isUseLeftSensor() { return sensorModel.isUseLeftSensor(); }

    @Override
    public boolean isUseFrontSensor() { return sensorModel.isUseFrontSensor(); }

    @Override
    public boolean isUseRightSensor() { return sensorModel.isUseRightSensor(); }





    // Implementation of interface GuiConfiguration as forwarding to the camera-sub-model

    @Override
    public boolean isUseGeneralQuery() { return cameraModel.isUseGeneralQuery(); }

    @Override
    public boolean isUseAngleQuery() { return cameraModel.isUseAngleQuery(); }

    @Override
    public boolean isUseSignatureOne() { return cameraModel.isUseSignatureOne(); }

    @Override
    public boolean isUseSignatureTwo() { return cameraModel.isUseSignatureTwo(); }

    @Override
    public boolean isUseSignatureThree() { return cameraModel.isUseSignatureThree(); }

    @Override
    public boolean isUseSignatureFour() { return cameraModel.isUseSignatureFour(); }

    @Override
    public boolean isUseSignatureFive() { return cameraModel.isUseSignatureFive(); }

    @Override
    public boolean isUseSignatureSix() { return cameraModel.isUseSignatureSix(); }

    @Override
    public boolean isUseSignatureSeven() { return cameraModel.isUseSignatureSeven(); }





    /**
     * Sub-(data-)model for sensor-related data.
     */
    class SensorModel {
        private boolean measureDistanceToLeft = true;

        private boolean useLeftSensor = true;
        private boolean useFrontSensor = true;
        private boolean useRightSensor = true;


        /**
         * Returns a boolean value indicating whether distance-measurement to the left is enabled (true) or
         * not (false). Used for one-dimensional maps
         *
         * @return  Boolean value indicating distance-measurement to the left
         */
        boolean isMeasureDistanceToLeft() { return measureDistanceToLeft; }


        /**
         * Returns a boolean value indicating whether distance-measurement to the right is enabled (true) or
         * not (false). Used for one-dimensional maps.
         *
         * @return  Boolean value indicating distance-measurement to the right
         */
        boolean isMeasureDistanceToRight() { return ! measureDistanceToLeft; }


        /**
         * Returns a boolean value indication whether distance-measurement to the left is enabled (true) or
         * not (false). Used for two-dimensional maps.
         *
         * @return  Boolean value indicating distance-measurement to the left
         */
        boolean isUseLeftSensor() { return useLeftSensor; }


        /**
         * Enabling (true) or disabling (false) of distance-measurement to the left. Used for two-dimensional maps.
         *
         * @param useLeftSensor     true (enabling), false (disabling) distance-measurement to the left
         */
        void setUseLeftSensor(boolean useLeftSensor) { this.useLeftSensor = useLeftSensor; }


        /**
         * Returns a boolean value indication whether distance-measurement ahead is enabled (true) or
         * not (false). Used for two-dimensional maps.
         *
         * @return  Boolean value indicating distance-measurement ahead
         */
        boolean isUseFrontSensor() { return useFrontSensor; }


        /**
         * Enabling (true) or disabling (false) of distance-measurement ahead. Used for two-dimensional maps.
         *
         * @param useFrontSensor     true (enabling), false (disabling) distance-measurement ahead
         */
        void setUseFrontSensor(boolean useFrontSensor) { this.useFrontSensor = useFrontSensor; }


        /**
         * Returns a boolean value indication whether distance-measurement to the right is enabled (true) or
         * not (false). Used for two-dimensional maps.
         *
         * @return  Boolean value indicating distance-measurement to the right
         */
        boolean isUseRightSensor() { return useRightSensor; }


        /**
         * Enabling (true) or disabling (false) of distance-measurement to the right. Used for two-dimensional maps.
         *
         * @param useRightSensor     true (enabling), false (disabling) distance-measurement to the right.
         */
        void setUseRightSensor(boolean useRightSensor) { this.useRightSensor = useRightSensor; }
    }





    class CameraModel{
        private boolean useGeneralQuery = true;
        private boolean useAngleQuery = true;
        private boolean useSignatureOne = true;
        private boolean useSignatureTwo = true;
        private boolean useSignatureThree = true;
        private boolean useSignatureFour = true;
        private boolean useSignatureFive = true;
        private boolean useSignatureSix = true;
        private boolean useSignatureSeven = true;


        /**
         * Returns a boolean value indication the usage of general queries to the camera.
         *
         * @return  A boolean value indication the usage of general queries to the camera
         */
        boolean isUseGeneralQuery() {
            return useGeneralQuery;
        }


        /**
         * Enabling (true) or disabling (false) the usage of general queries to the camera.
         *
         * @param useGeneralQuery   true (enabling) or false (disabling) general queries.
         */
        void setUseGeneralQuery(boolean useGeneralQuery) {
            this.useGeneralQuery = useGeneralQuery;
        }


        /**
         * Returns a boolean value indication the usage of angle-queries to the camera.
         *
         * @return  A boolean value indication the usage of angle-queries to the camera
         */
        boolean isUseAngleQuery() {
            return useAngleQuery;
        }


        /**
         * Enabling (true) or disabling (false) the usage of angle-queries to the camera.
         *
         * @param useAngleQuery   true (enabling) or false (disabling) anglequeries.
         */
        void setUseAngleQuery(boolean useAngleQuery) {
            this.useAngleQuery = useAngleQuery;
        }


        /**
         * Returns a boolean value indication the usage of queries for signature-1 to the camera.
         *
         * @return  A boolean value indication the usage of queries for signature-1 to the camera
         */
        boolean isUseSignatureOne() {
            return useSignatureOne;
        }


        /**
         * Enabling (true) or disabling (false) the usage of queries for signature-1 to the camera.
         *
         * @param useSignatureOne   true (enabling) or false (disabling) signature-1 queries.
         */
        void setUseSignatureOne(boolean useSignatureOne) {
            this.useSignatureOne = useSignatureOne;
        }


        /**
         * Returns a boolean value indication the usage of queries for signature-2 to the camera.
         *
         * @return  A boolean value indication the usage of queries for signature-2 to the camera
         */
        boolean isUseSignatureTwo() {
            return useSignatureTwo;
        }


        /**
         * Enabling (true) or disabling (false) the usage of queries for signature-2 to the camera.
         *
         * @param useSignatureTwo   true (enabling) or false (disabling) signature-2 queries.
         */
        void setUseSignatureTwo(boolean useSignatureTwo) {
            this.useSignatureTwo = useSignatureTwo;
        }


        /**
         * Returns a boolean value indication the usage of queries for signature-3 to the camera.
         *
         * @return  A boolean value indication the usage of queries for signature-3 to the camera
         */
        boolean isUseSignatureThree() {
            return useSignatureThree;
        }


        /**
         * Enabling (true) or disabling (false) the usage of queries for signature-3 to the camera.
         *
         * @param useSignatureThree   true (enabling) or false (disabling) signature-3 queries.
         */
        void setUseSignatureThree(boolean useSignatureThree) {
            this.useSignatureThree = useSignatureThree;
        }


        /**
         * Returns a boolean value indication the usage of queries for signature-4 to the camera.
         *
         * @return  A boolean value indication the usage of queries for signature-4 to the camera
         */
        boolean isUseSignatureFour() {
            return useSignatureFour;
        }


        /**
         * Enabling (true) or disabling (false) the usage of queries for signature-4 to the camera.
         *
         * @param useSignatureFour   true (enabling) or false (disabling) signature-4 queries.
         */
        void setUseSignatureFour(boolean useSignatureFour) {
            this.useSignatureFour = useSignatureFour;
        }


        /**
         * Returns a boolean value indication the usage of queries for signature-5 to the camera.
         *
         * @return  A boolean value indication the usage of queries for signature-5 to the camera
         */
        boolean isUseSignatureFive() {
            return useSignatureFive;
        }


        /**
         * Enabling (true) or disabling (false) the usage of queries for signature-5 to the camera.
         *
         * @param useSignatureFive   true (enabling) or false (disabling) signature-5 queries.
         */
        void setUseSignatureFive(boolean useSignatureFive) {
            this.useSignatureFive = useSignatureFive;
        }


        /**
         * Returns a boolean value indication the usage of queries for signature-6 to the camera.
         *
         * @return  A boolean value indication the usage of queries for signature-6 to the camera
         */
        boolean isUseSignatureSix() {
            return useSignatureSix;
        }


        /**
         * Enabling (true) or disabling (false) the usage of queries for signature-6 to the camera.
         *
         * @param useSignatureSix   true (enabling) or false (disabling) signature-6 queries.
         */
        void setUseSignatureSix(boolean useSignatureSix) {
            this.useSignatureSix = useSignatureSix;
        }


        /**
         * Returns a boolean value indication the usage of queries for signature-7 to the camera.
         *
         * @return  A boolean value indication the usage of queries for signature-7 to the camera
         */
        boolean isUseSignatureSeven() {
            return useSignatureSeven;
        }


        /**
         * Enabling (true) or disabling (false) the usage of queries for signature-7 to the camera.
         *
         * @param useSignatureSeven   true (enabling) or false (disabling) signature-7 queries.
         */
        void setUseSignatureSeven(boolean useSignatureSeven) {
            this.useSignatureSeven = useSignatureSeven;
        }
    }





    class MovementModel {
        // Specific attributes for 1-D-movement.
        private boolean startFromLeft = true;
        // Specific attributes for 2-D-movement.
        private boolean useRightAngles = true;
        private boolean useFreeAngles = false;

        private final SensorModel sensorModel;


        /**
         * Constructor.
         *
         * @param sensorModel   The sensor-sub-model.
         */
        private MovementModel(SensorModel sensorModel) {
            this.sensorModel = sensorModel;
        }


        /**
         * Returns a boolean value indicating whether a one-dimensional localization is supposed to start from
         * the left-side of the map.
         *
         * @return  A boolean value indicating whether localization starts from the left-side of the map.
         */
        boolean isStartFromLeft() {
            return startFromLeft;
        }


        /**
         * Sets localization on a one-dimensional map starting from left.
         */
        void setStartFromLeft() {
            this.startFromLeft = true;
            sensorModel.measureDistanceToLeft = true;
        }


        /**
         * Returns a boolean value indicating whether a one-dimensional localization is supposed to start from
         * the right-side of the map.
         *
         * @return  A boolean value indicating whether localization starts from the right-side of the map.
         */
        boolean isStartFromRight() { return !startFromLeft; }


        /**
         * Sets localization on a one-dimensional map starting from right.
         */
        void setStartFromRight() {
            this.startFromLeft = false;
            sensorModel.measureDistanceToLeft = false;
        }


        /**
         * Changes the direction of distance-measurement for one-dimensional maps. Used after the robot performed
         * a 180-degree turn.
         */
        void flipDirection() {
            sensorModel.measureDistanceToLeft = !sensorModel.measureDistanceToLeft;
        }


        /**
         * Returns a boolean value indicating whether the robot has to perform 90-degree-turns on a two-dimensional map.
         *
         * @return  A boolean value indicating whether the robot has to preform 90-degree-turns.
         */
        boolean isUseRightAngles() {
            return useRightAngles;
        }


        /**
         * Limits the robots motion to perform only 90-degree-turns on a two-dimensional map.
         */
        void setUseRightAngles() {
            this.useRightAngles = true;
            this.useFreeAngles = false;
        }


        /**
         * Returns a boolean value indicating whether the robot has to perform  free turns on a two-dimensional map.
         *
         * @return  A boolean value indicating whether the robot has to preform free turns.
         */
        boolean isUseFreeAngles() {
            return useFreeAngles;
        }


        /**
         * Frees the robot from limitations regarding the degree of turning.
         */
        void setUseFreeAngles() {
            this.useFreeAngles = true;
            this.useRightAngles = false;
        }
    }





    class LocalizationModel {
        private static final String ONE_DIMENSION_MAP_KEY = MapProvider.MAP_KEY_HOUSES;
        private static final String TWO_DIMENSION_MAP_KEY = MapProvider.MAP_KEY_ROOM;
        private static final String TWO_DIMENSION_WITH_CAM_MAP_KEY = MapProvider.MAP_KEY_MARKED_ROOM;

        private MapProvider mapProvider = MapProvider.getInstance();
        private Map map;
        private String mapKey;
        private LocalizationProvider localizationProvider;

        private boolean paused = true;

        private boolean isOneDimensional = true;
        private boolean isTwoDimensional = false;
        private boolean isWithCamera = false;
        private boolean isInReplayMode = false;

        private int stepSize = 10;
        private int numberOfParticles = 1000;
        private boolean stopWhenDone = true;
        private int acceptableTolerance = 10;

        private Particle selectedParticle;

        private final GuiConfiguration model;



        /**
         * Constructor.
         *
         * @param model A reference to the full data-model.
         */
        LocalizationModel(GuiConfiguration model) {
            this.model = model;
        }



        /**
         * Creates a new LocalizationProvider
         */
        void createLocalizationProvider() {
            this.localizationProvider = ClientFactory.createNewLocalizationProvider(map, numberOfParticles, new int[]{-1,-1,-1}, model);
        }


        /**
         * Returns the localization-provider currently used.
         *
         * @return The current LocalizationProvider
         */
        LocalizationProvider getLocalizationProvider() { return this.localizationProvider; }


        /**
         * Returns the current map. If no map is present a default-map is first assigned to the model.
         *
         * @return  The current map
         */
        public Map getMap() {
            if (map == null) {
                this.mapKey = ONE_DIMENSION_MAP_KEY;
                return mapProvider.getMap(ONE_DIMENSION_MAP_KEY);
            }
            return map;
        }


        /**
         * Sets a new Map to the model.
         *
         * @param map   The new map for the model.
         */
        void setMap (Map map) { this.map = map; }


        /**
         * Returns the key (of type String) identifying the current map.
         *
         * @return  The key of the current map of type String.
         */
        String getMapKey() {
            return this.mapKey;
        }


        /**
         * Returns the number of particles used for the current localization.
         *
         * @return The number of particles of the current localization.
         */
        int getNumberOfParticles() {
            return numberOfParticles;
        }


        /**
         * Sets the number of particles for the current localization.
         *
         * @param numberOfParticles The number of particles for localization.
         */
        void setNumberOfParticles(int numberOfParticles) { this.numberOfParticles = numberOfParticles; }


        /**
         * Returns a boolean value indicating whether the localization is currently paused.
         *
         * @return  A boolean value indicating whether the localization is currently paused
         */
        boolean isPaused() { return this.paused; }


        /**
         * Sets the localization paused (true) or running (false).
         *
         * @param bool  true (pause) or false (run)
         */
        void setPaused(boolean bool) { this.paused = bool; }


        /**
         * Returns a boolean value indicating whether the current localization is operating in a one-dimensional
         * environment (on a one-dimensional map).
         *
         * @return  A boolean value indicating operation on one-dimensional map (true) or other (false).
         */
        boolean isOneDimensional() {
            return isOneDimensional;
        }


        /**
         * Sets the context for operation to a one-dimensional environment ( a one-dimensional map).
         */
        void setOneDimensional() {
            isOneDimensional = true;
            isTwoDimensional = false;
            isWithCamera = false;
            isInReplayMode = false;
            map = mapProvider.getMap(ONE_DIMENSION_MAP_KEY);
            mapKey = ONE_DIMENSION_MAP_KEY;
            localizationProvider = null;
        }


        /**
         * Returns a boolean value indicating whether the current localization is operating in a two-dimensional
         * environment (on a two-dimensional map).
         *
         * @return  A boolean value indicating operation on two-dimensional map (true) or other (false).
         */
        boolean isTwoDimensional() {
            return isTwoDimensional;
        }


        /**
         * Sets the context for operation to a two-dimensional environment (a two-dimensional map).
         */
        void setTwoDimensional() {
            isTwoDimensional = true;
            isOneDimensional = false;
            isWithCamera = false;
            isInReplayMode = false;
            map = mapProvider.getMap(TWO_DIMENSION_MAP_KEY);
            mapKey = TWO_DIMENSION_MAP_KEY;
            localizationProvider = null;
        }


        /**
         * Returns a boolean value indicating whether the current localization is operating in a two-dimensional
         * environment (on a two-dimensional map) with camera-usage.
         *
         * @return  A boolean value indicating operation on two-dimensional map with camera-usage (true) or other (false).
         */
        boolean isWithCamera() {
            return isWithCamera;
        }


        /**
         * Sets the context for operation to a two-dimensional environment (a two-dimensional map) with camera-usage.
         */
        void setWithCamera() {
            isWithCamera = true;
            isOneDimensional = false;
            isTwoDimensional = false;
            isInReplayMode = false;
            map = mapProvider.getMap(TWO_DIMENSION_WITH_CAM_MAP_KEY);
            mapKey = TWO_DIMENSION_WITH_CAM_MAP_KEY;
            localizationProvider = null;
        }


        /**
         * Returns a boolean value indicating whether the localization-provider is currently in replay-mode.
         *
         * @return  A boolean value indicating whether the localization-provider is currently in replay-mode
         */
        boolean isInReplayMode() { return this.isInReplayMode; }


        /**
         * Sets the localization-provider to replay-mode.
         */
        void setInReplayMode() {
            isWithCamera = false;
            isOneDimensional = false;
            isTwoDimensional = false;
            isInReplayMode = true;
        }


        /**
         * Returns the step-size for each move-operation of the robot.
         *
         * @return  The step-size for each move-operation of the robot
         */
        int getStepSize() {
            return stepSize;
        }


        /**
         * Sets the step-size for each move-operation of the robot.
         *
         * @param stepSize  The step-size for each move-operation of the robot.
         */
        void setStepSize(int stepSize) {
            this.stepSize = stepSize;
        }


        /**
         * Returns a boolean value indicating whether to stop the localization once all particles lie within a
         * certain spreading range.
         *
         * @return  A boolean value indicating whether to stop when sufficiently certain about robot-pose.
         */
        boolean isStopWhenDone() {
            return stopWhenDone;
        }


        /**
         * Sets to stop the localization automatically once the robot-pose is sufficiently ascertained (true) or
         * to continue infinitely (false).
         *
         * @param stopWhenDone  Enable (true) or disable (false) automatic-stop.
         */
        void setStopWhenDone(boolean stopWhenDone) {
            this.stopWhenDone = stopWhenDone;
        }


        /**
         * Returns the acceptable spreading of particles around the estimated robot-pose.
         *
         * @return  The acceptable spreading of particles around the estimated robot-pose
         */
        int getAcceptableSpreading() {
            return acceptableTolerance;
        }


        /**
         * Sets the acceptable spreading of particles around the estimated robot-pose.
         *
         * @param acceptableSpreading   The acceptable spreading of particles around the estimated robot-pose
         */
        void setAcceptableSpreading(int acceptableSpreading) {
            this.acceptableTolerance = acceptableSpreading;
        }


        /**
         * Returns a reference to a particle selected by mouse-click.
         *
         * @return  A particle selected by mouse-click
         */
        Particle getSelectedParticle() { return selectedParticle; }
        void setSelectedParticle(Particle particle) { this.selectedParticle = particle; }

    }





    class ReplayModel {
        private ArrayList<WorldState> worldStatesForReplay;
        private int replayPointer = 0;


        /**
         * Returns the current index to the world-state-sequence.
         *
         * @return  The current index to the world-state-sequence
         */
        int getReplayPointer() { return this.replayPointer; }


        /**
         * Sets a new value to the index to the world-state-sequence.
         *
         * @param val  A new value to the index to the world-state-sequence
         */
        void setReplayPointer(int val) {
            if (val >= 0   &&   worldStatesForReplay != null   &&   val < worldStatesForReplay.size()) {
                this.replayPointer = val;
            }
        }


        /**
         * Returns a reference to the world-state-sequence, that is all steps of the localization.
         *
         * @return  The world-state-sequence
         */
        ArrayList<WorldState> getWorldStatesForReplay() { return this.worldStatesForReplay; }


        /**
         * Sets a new world-state-sequence (list of localization steps) for replay.
         *
         * @param worldStates A list (ArrayList<WorldState>) of localization steps.
         */
        void setWorldStatesForReplay(ArrayList<WorldState> worldStates) { this.worldStatesForReplay = worldStates; }


    }
}
