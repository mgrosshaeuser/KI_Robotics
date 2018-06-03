package ki.robotics.client.MCL;

import ki.robotics.server.robot.virtualRobots.MCLParticle;
import ki.robotics.utility.map.Map;

import java.io.Serializable;
import java.util.ArrayList;

public interface WorldState extends Serializable {
    Map getMap();

    int getNumberOfParticles();

    ArrayList<MCLParticle> getParticles();

    double[] getEstimatedBotPose();

    double getEstimatedBotPoseDeviation();

    String getCausativeInstruction();

    String getMapKey();
}
