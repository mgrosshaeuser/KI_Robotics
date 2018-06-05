package ki.robotics.client.MCL.impl;

import ki.robotics.client.GUI.GuiConfiguration;
import ki.robotics.client.MCL.WorldState;
import ki.robotics.client.SensorModel;
import ki.robotics.utility.map.Map;

import java.util.ArrayList;

/**
 * Data model for monte-carlo-localization.
 */
class MclModel {
    private static final int MCL_ACCEPTABLE_SPREADING_FOR_LOCAL_LOCALIZATION = 20;

    //static final int[] RESAMPLING_WEIGHTS = new int[]{20,10,5,2,1};     // Best working with physical robot.
    static final int[] RESAMPLING_WEIGHTS = new int[]{81,27,9,3,1};   // Best working in Simulation.

    private GuiConfiguration userSettings;
    private final int acceptableSpreading;
    private boolean localized;

    private ResamplingWheelView resamplingWheel;

    private ArrayList<WorldState> worldStateSequence;
    private int worldStateSequencePointer;
    private WorldStateImplMCL currentWorldState;
    private WorldStateImplMCL worldStateTempBackUp;


    /**
     * Constructor.
     *
     * @param worldState    The initial WorldState
     * @param userSettings  The user-settings
     */
    MclModel(WorldStateImplMCL worldState, GuiConfiguration userSettings) {
        this.worldStateSequence = new ArrayList<>();
        this.currentWorldState = worldState;
        this.userSettings = userSettings;
        this.resamplingWheel = new ResamplingWheelView(RESAMPLING_WEIGHTS, currentWorldState.getParticles());

        if (userSettings.isWithCamera()) {
            this.acceptableSpreading = MCL_ACCEPTABLE_SPREADING_FOR_LOCAL_LOCALIZATION;
        } else {
            this.acceptableSpreading = userSettings.getAcceptableSpreading();
        }
    }


    /**
     * Returns the map currently used by the localization-provider.
     *
     * @return  The map currently used by the localization-provider
     */
    Map getMap() { return currentWorldState.getMap(); }


    /**
     * Returns the number of particles used for the current localization.
     * @return  The number of particles used for the current localization
     */
    int getNumberOfParticles() { return this.currentWorldState.getNumberOfParticles(); }


    /**
     * Returns the currently used user-settings.
     *
     * @return  The currently used user-settings
     */
    GuiConfiguration getUserSettings() { return this.userSettings; }


    /**
     * Returns the acceptable spreading of particles around the estimated robot-pose.
     *
     * @return  The acceptable spreading of particles around the estimated robot-pose
     */
    int getAcceptableSpreading() { return this.acceptableSpreading; }


    /**
     * Returns a boolean value indication whether the localization is finished (true) or not (false).
     *
     * @return  A boolean value indication whether the localization is finished
     */
    boolean isLocalized() { return localized; }


    /**
     * Sets the localization as finished (true) or not (false).
     *
     * @param localized Status of the localization, finished (true) or not (false)
     */
    void setLocalized(boolean localized) { this.localized = localized; }


    /**
     * Returns a list (ArrayList) of the particles used for localization.
     *
     * @return  A list (ArrayList) of the particles used for localization
     */
    ArrayList<ParticleImplMCL> getParticles() { return  this.currentWorldState.getParticles(); }


    /**
     * Sets the particles used for localization.
     *
     * @param particles The particles used for localization.
     */
    void setParticles(ArrayList<ParticleImplMCL> particles) { this.currentWorldState.setParticles(particles); }


    /**
     * Returns the localization-sequence (world-state-sequence) as list (ArrayList<WorldState>).
     *
     * @return  The localization-sequence (world-state-sequence) as list
     */
    ArrayList<WorldState> getWorldStateSequence() { return this.worldStateSequence; }


    /**
     * Returns a reference to the visualization of the resampling-wheel.
     *
     * @return  A reference to the visualization of the resampling-wheel
     */
    ResamplingWheelView getResamplingWheel() { return resamplingWheel; }


    /**
     * Sets the current sensor-model (newest robot-feedback).
     *
     * @param sensorModel   The current sensor-model
     */
    void setSensorModel(SensorModel sensorModel) { currentWorldState.setSensorModel(sensorModel); }


    /**
     * Logs the instruction (of type String) which lead to the current localization-state.
     *
     * @param instruction   The last action performed by the localization-provider
     */
    void logInstruction(String instruction) { currentWorldState.setInstruction(instruction); }



    /**
     * Resets to the latest state of the ongoing localization.
     * Required for continuing the localization after an interim replay.
     */
    void resetToLatestWorldState() {
        worldStateSequencePointer = worldStateSequence.size();
        if (worldStateTempBackUp != null) {
            currentWorldState = worldStateTempBackUp;
            worldStateTempBackUp = null;
        }
    }


    /**
     * Moves one step back in the localization-sequence.
     */
    void navigateBackwardInHistory() {
        if (worldStateSequencePointer > 0) {
            if (worldStateTempBackUp == null) {
                worldStateTempBackUp = (WorldStateImplMCL) currentWorldState.getClone();
            }
            worldStateSequencePointer--;
            WorldStateImplMCL temp = (WorldStateImplMCL) worldStateSequence.get(worldStateSequencePointer);
            if (temp != null)
                currentWorldState = temp;
        }
    }


    /**
     * Moves one step forward in the localization-sequence.
     */
    void navigateForwardInHistory() {
        if (worldStateSequencePointer == worldStateSequence.size() -1   &&   worldStateTempBackUp != null) {
            resetToLatestWorldState();
            return;
        }
        if (worldStateSequencePointer < worldStateSequence.size() - 1) {
            if (worldStateTempBackUp == null) {
                worldStateTempBackUp = (WorldStateImplMCL) currentWorldState.getClone();
            }
            worldStateSequencePointer++;
            WorldStateImplMCL temp = (WorldStateImplMCL) worldStateSequence.get(worldStateSequencePointer);
            if (temp != null)
                currentWorldState = temp;
        }
    }


    /**
     * Makes a snap-shot of the current localization-state. Used preliminary for saving a world-state.
     */
    void takeSnapShot() {
        currentWorldState.takeSnapShot();
        worldStateSequence.add(currentWorldState.getClone());
        currentWorldState.reset();
        worldStateSequencePointer = worldStateSequence.size();
    }


}
