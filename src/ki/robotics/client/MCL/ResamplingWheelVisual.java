package ki.robotics.client.MCL;

import ki.robotics.server.robot.virtualRobots.MCLParticle;
import ki.robotics.utility.gui.ExtJPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ResamplingWheelVisual extends JFrame {
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 600;

    private double[] resamplingWheelFractions;
    private Color[] resamplingWheelColors;
    private ArrayList<MCLParticle> particles;

    private DiagramPanel cake;


    ResamplingWheelVisual(double[] resamplingWheelFractions, Color[] resamplingWheelColors, ArrayList<MCLParticle> particles) {
        createWindow();
        this.resamplingWheelFractions = resamplingWheelFractions;
        this.resamplingWheelColors = resamplingWheelColors;
        this.particles = particles;
    }



    private void createWindow() {
        this.setTitle("Resampling Wheel");
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        cake = new DiagramPanel();
        this.add(cake, BorderLayout.CENTER);
        this.setVisible(true);
    }

    public void update(ArrayList<MCLParticle> particles) {
        this.particles = particles;
        repaint();
    }




    private class DiagramPanel extends ExtJPanel {
        private static final int PADDING = 20;
        private DiagramPanel() {

        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;

            int[] particlesPerFraction = numberOfParticlesInFraction();
            int numberOfParticles = particles.size();

            int width = this.getWidth() - 2 * PADDING;
            int height = this.getHeight() - 2 * PADDING;
            int startAngle = 0;
            g2d.setColor(Color.GREEN);
            g2d.fillArc(PADDING, PADDING, width, height, 0, 360);
            for (int i = 0   ;   i < particlesPerFraction.length   ;   i++) {
                g2d.setColor(resamplingWheelColors[i]);
                double particleFraction = particlesPerFraction[i] / (double) numberOfParticles;
                int arc = (int)Math.ceil(particleFraction * 360);
                g2d.fillArc(PADDING, PADDING, width, height, startAngle, arc);
                startAngle += arc;
            }
        }


        private int[] numberOfParticlesInFraction() {
            double epsilon = 0.001;
            int[] array = new int[35];
            for (int i = 0   ;   i < array.length   ;   i++) {
                for (MCLParticle p : particles) {
                    if (Math.abs(p.getWeight() - resamplingWheelFractions[i]) < epsilon) {
                        array[i]++;
                    }
                }
            }
            return array;
        }
    }
}
