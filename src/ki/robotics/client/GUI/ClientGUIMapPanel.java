package ki.robotics.client.GUI;

import ki.robotics.client.MCL.MCL_Provider;
import ki.robotics.common.MapPanel;
import ki.robotics.robot.MCLParticle;
import lejos.robotics.navigation.Pose;

import java.awt.*;
import java.util.ArrayList;

public class ClientGUIMapPanel extends MapPanel {
    private static final int PARTICLE_DIAMETER = 4;
    private ClientGUIModel model;


    public ClientGUIMapPanel(ClientGUI parent, ClientGUIModel model) {
        super(parent, model.getMap());
        this.model = model;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        MCL_Provider mclProvider = model.getMclProvider();

        if (mclProvider == null) {
            return;
        }

        ArrayList<MCLParticle> particles = mclProvider.getParticles();
        if (particles != null) {
            float medianWeight = mclProvider.getMedianParticleWeight();
            for (MCLParticle p : particles) {
                p.paint(g, PARTICLE_DIAMETER, getScaleFactor(), getxOffset(), getyOffset(), medianWeight);
            }
        }
        Pose p = mclProvider.getEstimatedBotPose();

        g.setColor(mclProvider.isLocalizationDone() ? Color.GREEN : Color.RED);
        int radius = (int)Math.ceil(mclProvider.getEstimatedBotPoseDeviation());
        int acceptableTolerance = mclProvider.getAcceptableTolerance();
        radius = radius < acceptableTolerance ? acceptableTolerance : radius;
        g.drawOval(
                (Math.round(p.getX())-radius) * getScaleFactor() + getxOffset(),
                (Math.round(p.getY())-radius) * getScaleFactor() + getyOffset(),
                radius * 2 * getScaleFactor(),
                radius * 2 * getScaleFactor());
        g.drawString("Deviation: " + String.valueOf(radius),10,40);

        g.setColor(Color.BLACK);
        g.drawString("Estimated Bot Position: ", 10,20);
        g.drawString("X: " + String.valueOf(Math.round(p.getX())),10,55);
        g.drawString("Y: " + String.valueOf(Math.round(p.getY())), 10,70);
        g.drawString("H: " + String.valueOf(Math.round(p.getHeading())), 10,85);
    }
}
