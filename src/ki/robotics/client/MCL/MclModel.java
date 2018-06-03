package ki.robotics.client.MCL;

import ki.robotics.client.GUI.Configuration;
import ki.robotics.client.SensorModel;
import ki.robotics.server.robot.virtualRobots.MCLParticle;
import ki.robotics.utility.map.Map;

import java.util.ArrayList;

class MclModel {
    private static final int MCL_ACCEPTABLE_SPREADING_FOR_LOCAL_LOCALIZATION = 20;

    //static final int[] RESAMPLING_WEIGHTS = new int[]{20,10,5,2,1};     // Best working with physical robot.
    static final int[] RESAMPLING_WEIGHTS = new int[]{81,27,9,3,1};   // Best working in Simulation.

    private Configuration userSettings;
    private final int acceptableSpreading;
    private boolean localized;

    private ResamplingWheelView resamplingWheel;

    private ArrayList<WorldState> worldStateSequence;
    private int worldStateSequencePointer;
    private WorldStateImplMCL currentWorldState;
    private WorldStateImplMCL worldStateTempBackUp;




    MclModel(WorldStateImplMCL worldState, Configuration userSettings) {
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



    Map getMap() { return currentWorldState.getMap(); }


    int getNumberOfParticles() { return this.currentWorldState.getNumberOfParticles(); }


    Configuration getUserSettings() { return this.userSettings; }


    int getAcceptableSpreading() { return this.acceptableSpreading; }



    boolean isLocalized() { return localized; }

    void setLocalized(boolean localized) { this.localized = localized; }



    ArrayList<MCLParticle> getParticles() { return  this.currentWorldState.getParticles(); }

    void setParticles(ArrayList<MCLParticle> particles) { this.currentWorldState.setParticles(particles); }



    ArrayList<WorldState> getWorldStateSequence() { return this.worldStateSequence; }


    ResamplingWheelView getResamplingWheel() { return resamplingWheel; }



    void setSensorModel(SensorModel sensorModel) { currentWorldState.setSensorModel(sensorModel); }


    void logInstruction(String instruction) { currentWorldState.addInstruction(instruction); }




    void resetToLatestWorldState() {
        worldStateSequencePointer = worldStateSequence.size();
        if (worldStateTempBackUp != null) {
            currentWorldState = worldStateTempBackUp;
            worldStateTempBackUp = null;
        }
    }

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




    void takeSnapShot() {
        currentWorldState.takeSnapShot();
        worldStateSequence.add(currentWorldState.getClone());
        currentWorldState.reset();
        worldStateSequencePointer = worldStateSequence.size();
    }


}
