package ki.robotics.client.MCL.impl;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Visualization of the resampling-wheel used during monte-carlo-localization.
 */
class ResamplingWheelView extends JFrame {
    private static final String WINDOW_TITLE = "Resampling Wheel";
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 600;

    private int[] resamplingWeights;
    private double[] resamplingWheelFractions;
    private Color[] resamplingWheelColors;
    private ArrayList<ParticleImplMCL> particles;


    /**
     * Constructor.
     *
     * @param resamplingWeights int[] with weights marking the borders of the resampling-categories
     * @param particles     The particles used for localization
     */
    ResamplingWheelView(int[] resamplingWeights, ArrayList<ParticleImplMCL> particles) {
        createWindow();
        this.resamplingWeights = resamplingWeights;
        this.resamplingWheelFractions = createResamplingWheelCategoryArray();
        this.resamplingWheelColors = createResamplingWheelColorArray();
        this.particles = particles;
    }


    /**
     * Creates the resampling categories. That is the combination of the resampling-categories for three
     * directions of distance-measurement.
     *
     * @return  double[] holding the resampling-categories
     */
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


    /**
     * Creates a Color[] holding a distinct color associated with each resampling-category.
     *
     * @return  A Color[] holding a color for each resampling-category.
     */
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


    /**
     * Makes the connection between a particles weight and the associated color.
     *
     * @param weight    The weight of a particle
     * @return  The Color corresponding to the weight
     */
    Color weightToColor(double weight) {
        double epsilon = 0.001;

        for (int i = 0; i < resamplingWheelFractions.length   ; i++) {
            if (Math.abs(weight - resamplingWheelFractions[i]) < epsilon) {
                return resamplingWheelColors[i];
            }
        }
        return Color.CYAN;
    }


    /**
     * Creates and displays the resampling-wheel-window.
     */
    private void createWindow() {
        this.setTitle(WINDOW_TITLE);
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        DiagramPanel cake = new DiagramPanel();
        this.add(cake, BorderLayout.CENTER);
        this.setVisible(true);
    }

    /**
     * Updates the resampling-wheel with a new (resampled) set of particles.
     *
     * @param particles The new set of particles.
     */
    void update(ArrayList<ParticleImplMCL> particles) {
        this.particles = particles;
        repaint();
    }





    /**
     * A Panel for painting the resampling-wheel.
     */
    private class DiagramPanel extends JPanel {
        private static final int PADDING = 20;
        private DiagramPanel() {

        }

        /**
         * Paints the resampling wheel in a given graphical context.
         *
         * @param g     The graphical context
         */
        @Override
        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;

            int[] particlesPerFraction = numberOfParticlesInCategory();
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

        /**
         * Returns an int[] with one element for each resampling-category, holding the number of particles
         * in that category.
         *
         * @return An int[] with the number of particles per resampling-category.
         */
        private int[] numberOfParticlesInCategory() {
            double epsilon = 0.001;
            int[] array = new int[35];
            for (int i = 0   ;   i < array.length   ;   i++) {
                for (ParticleImplMCL p : particles) {
                    if (Math.abs(p.getWeight() - resamplingWheelFractions[i]) < epsilon) {
                        array[i]++;
                    }
                }
            }
            return array;
        }
    }
}
