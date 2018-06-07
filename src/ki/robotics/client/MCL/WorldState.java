package ki.robotics.client.MCL;

import ki.robotics.utility.map.Map;

import java.io.Serializable;
import java.util.ArrayList;

public interface WorldState<T extends Particle> extends Serializable {
    Map getMap();

    int getNumberOfParticles();

    ArrayList<T> getParticles();

    double[] getEstimatedBotPose();

    double getEstimatedBotPoseSpreading();

    String getCausativeInstruction();

    String getMapKey();
}
