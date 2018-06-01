package ki.robotics.client.MCL;

import ki.robotics.client.SensorModel;
import ki.robotics.server.robot.virtualRobots.MCLParticle;

import java.util.ArrayList;

public class WorldStateImplMCL implements WorldState {
    private transient LocalizationProvider localizationProvider;
    private ArrayList<MCLParticle> particles;
    private double[] estimatedBotPose;
    private double estimatedBotPoseX;
    private double estimatedBotPoseY;
    private double estimatedBotPoseHeading;
    private double estimatedBotPoseDeviation;
    private ArrayList<Object[]> causativeInstructions;
    private SensorModel sensorModel;

    WorldStateImplMCL(LocalizationProvider localizationProvider, ArrayList<MCLParticle> particles) {
        this.localizationProvider = localizationProvider;
        this.particles = particles;
        this.causativeInstructions = new ArrayList<>();
        this.sensorModel = new SensorModel();
    }

    private WorldStateImplMCL(LocalizationProvider localizationProvider) {
            this(localizationProvider,new ArrayList<MCLParticle>());
        }

    void takeSnapShot() {
        this.estimatedBotPose = localizationProvider.getEstimatedPose();
        this.estimatedBotPoseDeviation = localizationProvider.getEstimatedPoseDeviation();
        this.particles = localizationProvider.getParticles();
    }

    @Override
    public ArrayList<MCLParticle> getParticles() {
            return this.particles;
        }

    void setParticles(ArrayList<MCLParticle> particles) { this.particles = particles; }


    @Override
    public double[] getEstimatedBotPose() {
        return this.estimatedBotPose;
    }


    @Override
    public double getEstimatedBotPoseDeviation() {
            return this.estimatedBotPoseDeviation;
        }

    @Override
    public ArrayList<Object[]> getCausativeInstructions() {
            return this.causativeInstructions;
        }

    void addInstruction(Object[] instruction) {
            this.causativeInstructions.add(instruction);
        }

    void setSensorModel(SensorModel sensorModel) {
            this.sensorModel = SensorModel.makeDeepCopy(sensorModel);
        }

    void reset() {
        this.causativeInstructions = new ArrayList<>();
        this.sensorModel = new SensorModel();
    }

    WorldState getClone() {
        WorldStateImplMCL snapShot = new WorldStateImplMCL(this.localizationProvider);
        for (MCLParticle p : this.particles) {
            snapShot.particles.add(MCLParticle.makeDeepCopy(p));
        }
        snapShot.estimatedBotPoseX = this.estimatedBotPoseX;
        snapShot.estimatedBotPoseY = this.estimatedBotPoseY;
        snapShot.estimatedBotPoseHeading = this.estimatedBotPoseHeading;
        snapShot.estimatedBotPoseDeviation = this.estimatedBotPoseDeviation;
        for (Object[] o : this.causativeInstructions) {
            Object instruction[] = new Object[o.length];
            System.arraycopy(o, 0, instruction, 0, o.length);
            snapShot.causativeInstructions.add(instruction);
        }
        snapShot.setSensorModel(SensorModel.makeDeepCopy(this.sensorModel));
        return snapShot;
    }

}
