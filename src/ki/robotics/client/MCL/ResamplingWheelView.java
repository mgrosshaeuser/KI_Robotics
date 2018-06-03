package ki.robotics.client.MCL;

import ki.robotics.server.robot.virtualRobots.MCLParticle;
import ki.robotics.utility.gui.ExtJPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class ResamplingWheelView extends JFrame {
    private static final String WINDOW_TITLE = "Resampling Wheel";
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 600;

    private int[] resamplingWeights;
    private double[] resamplingWheelFractions;
    private Color[] resamplingWheelColors;
    private ArrayList<MCLParticle> particles;



    ResamplingWheelView(int[] resamplingWeights, ArrayList<MCLParticle> particles) {
        createWindow();
        this.resamplingWeights = resamplingWeights;
        this.resamplingWheelFractions = createResamplingWheelCategoryArray();
        this.resamplingWheelColors = createResamplingWheelColorArray();
        this.particles = particles;
    }


    private double[] createResamplingWheelCategoryArray() {
        double d[] = new double[36];
        int x=0;
        for (int i = 0   ;   i < resamplingWeights.length   ;   i++) {
            for (int j = i   ;   j < resamplingWeights.length   ;   j++) {
                for (int k = j   ;   k < resamplingWeights.length   ;   k++) {
                    int sum = resamplingWeights[i] + resamplingWeights[j] + resamplingWeights[k];
                    d[x++] = 1/(double)sum;
                }
            }
        }
        return d;
    }


    private Color[] createResamplingWheelColorArray() {
        Color c[] = new Color[35];
        double max = 255;
        double min = 0;
        for (int i = 1   ;   i < c.length - 1   ;   i++) {
            int newRed = (int) Math.round(max - (max / 35 * i));
            int newGreen = (int) Math.round(min + (max / 35 * i));
            c[i] = new Color(newRed, newGreen, 0);
        }
        c[0] = new Color(255,0,0);
        c[34] = new Color(0,255,0);
        return c;
    }


    Color weightToColor(double weight) {
        double epsilon = 0.001;

        for (int i = 0; i < resamplingWheelFractions.length   ; i++) {
            if (Math.abs(weight - resamplingWheelFractions[i]) < epsilon) {
                return resamplingWheelColors[i];
            }
        }
        return Color.CYAN;
    }



    private void createWindow() {
        this.setTitle(WINDOW_TITLE);
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        DiagramPanel cake = new DiagramPanel();
        this.add(cake, BorderLayout.CENTER);
        this.setVisible(true);
    }

    void update(ArrayList<MCLParticle> particles) {
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
