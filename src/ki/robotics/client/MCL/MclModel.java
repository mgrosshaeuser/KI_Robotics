package ki.robotics.client.MCL;

import ki.robotics.client.GUI.Configuration;
import ki.robotics.client.SensorModel;
import ki.robotics.server.robot.virtualRobots.MCLParticle;
import ki.robotics.utility.map.Map;

import java.awt.*;
import java.util.ArrayList;

class MclModel {
    private static final int MCL_ACCEPTABLE_SPREADING_FOR_LOCAL_LOCALIZATION = 20;

    //static final int[] RESAMPLING_WEIGHTS = new int[]{20,10,5,2,1};     // Best working with physical robot.
    static final int[] RESAMPLING_WEIGHTS = new int[]{81,27,9,3,1};   // Best working in Simulation.

    private Map map;
    private int numberOfParticles;
    private int[] limitations;
    private Configuration userSettings;

    private ResamplingWheelView resamplingWheel;

    private ArrayList<WorldState> worldStateSequence;
    private int worldStatePointer;
    private WorldStateImplMCL currentWorld;
    private WorldStateImplMCL currentWorldBackUp;

    private final int acceptableSpreading;
    private boolean localized;



    MclModel(Map map, int numberOfParticles, int[] limitations, Configuration userSettings) {
        this.map = map;
        this.numberOfParticles = numberOfParticles;
        this.limitations = limitations;
        this.userSettings = userSettings;

        if (userSettings.isWithCamera()) {
            this.acceptableSpreading = MCL_ACCEPTABLE_SPREADING_FOR_LOCAL_LOCALIZATION;
        } else {
            this.acceptableSpreading = userSettings.getAcceptableDeviation();
        }
    }


    void createInitialWorld(LocalizationProviderImplMCL localizationProvider) {
        worldStateSequence = new ArrayList<>();
        ArrayList<MCLParticle> particles = localizationProvider.generateInitialParticleSet();
        this.numberOfParticles = particles.size();
        currentWorld = new WorldStateImplMCL(localizationProvider, particles);
        createResamplingWheel();
    }


    Map getMap() { return this.map; }

    int getNumberOfParticles() { return this.numberOfParticles; }

    int[] getLimitations() { return this.limitations; }

    Configuration getUserSettings() { return this.userSettings; }

    int getAcceptableSpreading() { return this.acceptableSpreading; }



    boolean isLocalized() { return localized; }

    void setLocalized(boolean localized) { this.localized = localized; }



    ArrayList<MCLParticle> getParticles() { return  this.currentWorld.getParticles(); }

    void setParticles(ArrayList<MCLParticle> particles) { this.currentWorld.setParticles(particles); }



    ArrayList<WorldState> getWorldStateSequence() { return this.worldStateSequence; }

    ResamplingWheelView getResamplingWheel() { return resamplingWheel; }



    void setSensorModel(SensorModel sensorModel) { currentWorld.setSensorModel(sensorModel); }



    void logInstruction(Object[] instruction) { currentWorld.addInstruction(instruction); }




    void resetToLatestWorldState() {
        this.worldStatePointer = worldStateSequence.size();
        if (worldStatePointer > 0) {
            this.currentWorld = currentWorldBackUp;
            currentWorldBackUp = null;
        }
    }

    void navigateBackwardInHistory() {
        if (worldStatePointer > 0) {
            if (currentWorldBackUp == null) {
                currentWorldBackUp = (WorldStateImplMCL) currentWorld.getClone();
            }
            worldStatePointer--;
            currentWorld = (WorldStateImplMCL) worldStateSequence.get(worldStatePointer);
        }
    }

    void navigateForwardInHistory() {
        if (worldStatePointer < worldStateSequence.size() - 1) {
            if (currentWorldBackUp == null) {
                currentWorldBackUp = (WorldStateImplMCL) currentWorld.getClone();
            }
            worldStatePointer++;
            currentWorld = (WorldStateImplMCL) worldStateSequence.get((worldStatePointer));
        }
    }




    void takeSnapShot() {
        currentWorld.takeSnapShot();
        worldStateSequence.add(currentWorld.getClone());
        currentWorld.reset();
        worldStatePointer = worldStateSequence.size();
    }




    private void createResamplingWheel() {
        double[] resamplingWheelFractions = createResamplingWheelCategoryArray();
        Color[] resamplingWheelColors = createResamplingWheelColorArray();
        this.resamplingWheel = new ResamplingWheelView(resamplingWheelFractions, resamplingWheelColors, currentWorld.getParticles());
    }


    private double[] createResamplingWheelCategoryArray() {
        double d[] = new double[36];
        int x=0;
        for (int i = 0   ;   i < RESAMPLING_WEIGHTS.length   ;   i++) {
            for (int j = i   ;   j < RESAMPLING_WEIGHTS.length   ;   j++) {
                for (int k = j   ;   k < RESAMPLING_WEIGHTS.length   ;   k++) {
                    int sum = RESAMPLING_WEIGHTS[i] + RESAMPLING_WEIGHTS[j] + RESAMPLING_WEIGHTS[k];
                    d[x++] = 1/(double)sum;
                }
            }
        }
        return d;
    }


    private Color[] createResamplingWheelColorArray() {
        Color c[] = new Color[35];
        double max = 255;
        double min = 0;
        for (int i = 1   ;   i < c.length - 1   ;   i++) {
            int newRed = (int) Math.round(max - (max / 35 * i));
            int newGreen = (int) Math.round(min + (max / 35 * i));
            c[i] = new Color(newRed, newGreen, 0);
        }
        c[0] = new Color(255,0,0);
        c[34] = new Color(0,255,0);
        return c;
    }
}
