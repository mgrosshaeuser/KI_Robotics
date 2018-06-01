package ki.robotics.client.MCL;

import ki.robotics.server.robot.virtualRobots.MCLParticle;

import java.io.Serializable;
import java.util.ArrayList;

public interface WorldState extends Serializable {
    ArrayList<MCLParticle> getParticles();
    double[] getEstimatedBotPose();
    double getEstimatedBotPoseDeviation();
    ArrayList<Object[]> getCausativeInstructions();
}
