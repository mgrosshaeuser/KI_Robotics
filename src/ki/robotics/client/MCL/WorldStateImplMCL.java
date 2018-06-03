package ki.robotics.client.MCL;

import ki.robotics.client.ClientFactory;
import ki.robotics.client.SensorModel;
import ki.robotics.server.robot.virtualRobots.MCLParticle;
import ki.robotics.utility.map.Map;
import ki.robotics.utility.map.MapProvider;

import java.util.ArrayList;

public class WorldStateImplMCL implements WorldState {
    private transient LocalizationProvider localizationProvider;
    private ArrayList<MCLParticle> particles;
    private double[] estimatedBotPose;
    private double estimatedBotPoseDeviation;
    private String causativeInstruction;
    private SensorModel sensorModel;
    private transient Map map;
    private String mapKey;



    WorldStateImplMCL(LocalizationProvider localizationProvider, Map map, ArrayList<MCLParticle> particles) {
        this.localizationProvider = localizationProvider;
        this.particles = particles;
        this.causativeInstruction = "";
        this.sensorModel = ClientFactory.createNewSensorModel();
        this.map = map;
        this.mapKey = map.getMapKey();
    }

    private WorldStateImplMCL(LocalizationProvider localizationProvider, Map map) {
            this(localizationProvider, map, new ArrayList<MCLParticle>());
        }

    void takeSnapShot() {
        this.estimatedBotPose = localizationProvider.getEstimatedPose();
        this.estimatedBotPoseDeviation = localizationProvider.getSpreadingAroundEstimatedBotPose();
        this.particles = localizationProvider.getParticles();
    }


    @Override
    public int getNumberOfParticles() {
        return this.particles.size();
    }

    @Override
    public ArrayList<MCLParticle> getParticles() {
            return this.particles;
        }

    void setParticles(ArrayList<MCLParticle> particles) { this.particles = particles; }


    @Override
    public Map getMap() {
        if (this.map == null) {
            this.map = MapProvider.getInstance().getMap(mapKey);
        }
        return this.map;
    }


    @Override
    public double[] getEstimatedBotPose() {
        if (estimatedBotPose == null) {
            return new double[]{0,0,0};
        }
        return this.estimatedBotPose;
    }


    @Override
    public double getEstimatedBotPoseDeviation() {
            return this.estimatedBotPoseDeviation;
        }

    @Override
    public String getCausativeInstruction() {
            return this.causativeInstruction;
        }

    void addInstruction(String instruction) { this.causativeInstruction = instruction; }

    void setSensorModel(SensorModel sensorModel) {
            this.sensorModel = sensorModel.getClone();
        }

    void reset() {
        this.causativeInstruction = "";
        this.sensorModel = ClientFactory.createNewSensorModel();
    }

    @Override
    public String getMapKey() { return this.mapKey; }

    WorldState getClone() {
        WorldStateImplMCL snapShot = new WorldStateImplMCL(this.localizationProvider, this.map);
        for (MCLParticle p : this.particles) {
            snapShot.particles.add(p.getClone());
        }

        double[] estimation = localizationProvider.getEstimatedPose();
        snapShot.estimatedBotPose = new double[estimation.length];
        System.arraycopy(estimation, 0, snapShot.estimatedBotPose, 0, estimation.length);
        snapShot.estimatedBotPoseDeviation = this.estimatedBotPoseDeviation;

        snapShot.causativeInstruction = String.valueOf(this.causativeInstruction);

        snapShot.setSensorModel(this.sensorModel.getClone());
        return snapShot;
    }

}
