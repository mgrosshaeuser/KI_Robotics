package ki.robotics.client.MCL.impl;

import ki.robotics.client.ClientFactory;
import ki.robotics.client.MCL.WorldState;
import ki.robotics.client.MCL.SensorModel;
import ki.robotics.utility.map.Map;

import java.util.ArrayList;


/**
 * Representation of a world-state which is updated by the monte-carlo-localization using robot-sensor-data.
 */
public class WorldStateImplMCL implements WorldState<ParticleImplMCL> {
    private transient LocalizationProviderImplMCL localizationProvider;
    private ArrayList<ParticleImplMCL> particles;
    private double[] estimatedBotPose;
    private double estimatedBotPoseDeviation;
    private String causativeInstruction;
    private SensorModel sensorModel;
    private transient Map map;
    private String mapKey;


    /**
     * Constructor.
     *
     * @param localizationProvider  The localization-provider in use
     * @param map   The map used for localization
     * @param particles The particles used for localization
     */
    WorldStateImplMCL(LocalizationProviderImplMCL localizationProvider, Map map, ArrayList<ParticleImplMCL> particles) {
        this.localizationProvider = localizationProvider;
        this.particles = particles;
        this.causativeInstruction = "";
        this.sensorModel = ClientFactory.createNewSensorModel();
        this.map = map;
        this.mapKey = map.getMapKey();
    }


    /**
     * Private constructor for making deep-copies.
     *
     * @param localizationProvider The localization-provider in use
     * @param map   The map used for localization
     */
    private WorldStateImplMCL(LocalizationProviderImplMCL localizationProvider, Map map) {
            this(localizationProvider, map, new ArrayList<ParticleImplMCL>());
    }


    /**
     * Makes a snap-shot of the current world-state.
     */
    void takeSnapShot() {
        this.estimatedBotPose = localizationProvider.getEstimatedPose();
        this.estimatedBotPoseDeviation = localizationProvider.getSpreadingAroundEstimatedBotPose();
        this.particles = localizationProvider.getParticles();
    }


    /**
     * Returns a deep-copy of the this world-state.
     *
     * @return  A deep-copy of the this world-state
     */
    WorldState getClone() {
        WorldStateImplMCL snapShot = new WorldStateImplMCL(this.localizationProvider, this.map);
        for (ParticleImplMCL p : this.particles) {
            ParticleImplMCL clone = p.getClone();
            snapShot.particles.add(clone);
        }

        double[] estimation = localizationProvider.getEstimatedPose();
        snapShot.estimatedBotPose = new double[estimation.length];
        System.arraycopy(estimation, 0, snapShot.estimatedBotPose, 0, estimation.length);
        snapShot.estimatedBotPoseDeviation = this.estimatedBotPoseDeviation;

        snapShot.causativeInstruction = String.valueOf(this.causativeInstruction);

        snapShot.setSensorModel(this.sensorModel.getClone());
        return snapShot;
    }


    /**
     * Returns the number of particles used in this world-state.
     *
     * @return  The number of particles used in this world-state
     */
    @Override
    public int getNumberOfParticles() {
        return this.particles.size();
    }


    /**
     * Returns the particles used in this world state (as ArrayList)
     *
     * @return The particles used in this world state
     */
    @Override
    public ArrayList<ParticleImplMCL> getParticles() {
            return this.particles;
    }


    /**
     * Sets the particles used in this world state.
     *
     * @param particles     The particles used in this world state
     */
    void setParticles(ArrayList<ParticleImplMCL> particles) { this.particles = particles; }


    /**
     * Returns the map used in this world-state.
     *
     * @return  The map used in this world-state
     */
    @Override
    public Map getMap() {
        if (this.map == null) {
            this.map = ClientFactory.getMapProvider().getMap(mapKey);
        }
        return this.map;
    }


    /**
     * Returns the key for the map used in this world-state.
     *
     * @return  The key for the map used in this world-state
     */
    @Override
    public String getMapKey() {
        return this.mapKey;
    }


    /**
     * Returns the estimated robot-pose in this world-state.
     *
     * @return  The estimated robot-pose in this world-state
     */
    @Override
    public double[] getEstimatedBotPose() {
        if (estimatedBotPose == null) {
            return new double[]{0,0,0};
        }
        return this.estimatedBotPose;
    }


    /**
     * Returns the spreading of the particles around the estimated robot-pose in this world-state.
     *
     * @return  The spreading of the particles around the estimated robot-pose in this world-state
     */
    @Override
    public double getEstimatedBotPoseSpreading() {
            return this.estimatedBotPoseDeviation;
    }


    /**
     * Returns the instruction (as String) which lead to the this world-state.
     *
     * @return  The instruction which lead to the this world-state
     */
    @Override
    public String getCausativeInstruction() {
            return this.causativeInstruction;
    }


    /**
     * Sets the instruction (as String) which lead to the this world-state
     *
     * @param instruction   The instruction which lead to the this world-state
     */
    void setInstruction(String instruction) { this.causativeInstruction = instruction; }


    /**
     * Sets the sensor-model for this world-state.
     *
     * @param sensorModel The sensor-model for this world-state
     */
    void setSensorModel(SensorModel sensorModel) {
            this.sensorModel = sensorModel.getClone();
    }


    /**
     * Resets the values of the non-portable world-state-attributes.
     */
    void reset() {
        this.causativeInstruction = "";
        this.sensorModel = ClientFactory.createNewSensorModel();
    }
}
