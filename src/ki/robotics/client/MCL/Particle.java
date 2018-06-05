package ki.robotics.client.MCL;

import lejos.robotics.navigation.Pose;

import java.awt.*;

public interface Particle {
    Particle getClone();

    double move(double distance);
    void turn(int degrees);

    Pose getPose();

    float getWeight();
    void setWeight(float weight);

    Color getColor();
    void setColor(Color color);

    boolean isOutOfMapOperatingRange();

    void paint(Graphics g, int particleDiameter, int scaleFactor, int xOffset, int yOffset);

    double[] ultrasonicThreeWayScan();

    int[] cameraGeneralQuery();
    int[] cameraSignatureQuery(int signature);
    int[][] cameraAllSignaturesQuery();
    int[] cameraColorCodeQuery(int color);
    int cameraAngleQuery();
}
