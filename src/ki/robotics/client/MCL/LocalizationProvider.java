package ki.robotics.client.MCL;

import ki.robotics.client.SensorModel;
import ki.robotics.server.robot.virtualRobots.MCLParticle;

import java.util.ArrayList;

public interface LocalizationProvider {
    ArrayList<MCLParticle> getParticles();

    void translateParticles(double distance);

    void turnParticles(double degree);

    void recalculateParticleWeight(SensorModel sensorModel);

    int getAcceptableSpreading();

    double[] getEstimatedPose();

    double getSpreadingAroundEstimatedBotPose();

    boolean isLocalizationDone();

    void badParticlesFinalKill();

    void saveLocalizationSequenceToFile();

    void resetToLatestWorldState();

    void stepBackInLocalizationHistory();

    void stepForwardInLocalizationHistory();
}
