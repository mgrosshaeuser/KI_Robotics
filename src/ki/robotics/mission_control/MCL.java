package ki.robotics.mission_control;

import ki.robotics.datastructures.Map;
import ki.robotics.datastructures.Particle;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;


/**
 * Monte-Carlo-Localization including a GUI for presentation of results.
 *
 * @version 1.1, 12/28/17
 */
public class MCL extends JFrame {
    public static final int WINDOW_WIDTH = 600;
    public static final int WINDOW_HEIGHT = 800;

    public static final int NUMBER_OF_PARTICLES = 10000;

    private static final double EPSILON = 0.00001;

    private int scaleFactor = 1;
    private int xOffset = 0;
    private int yOffset = 0;

    private Map map;
    private MapOverlay mapOverlay = new MapOverlay();

    private ArrayList<Particle> particles;



    /**
     * Constructor.
     */
    public MCL() {
        this.map = new Map(new File(getClass().getClassLoader().getResource("map2.svg").getFile()));
        particles = generateInitialParticles();

        this.setTitle("Monte Carlo Localization");
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        add(mapOverlay);
        this.setVisible(true);
    }



    /**
     * Supposed to handle the interaction with the robot.
     *
     * @param responses     Response from the robot in answer to request.
     * @return              An initial or continuative request.
     */
    public String execute(ArrayList<String> responses) {
        if (responses == null) {
            return "MDST, MCLR, BTNR 45, MDST, BTRF 30, MDST, MCLR, STNL 90, MDST, BTNR 45, BTRF 50, MCLR";
        } else {
            for (String s : responses) {
                System.out.println(s);
            }
            return "DCNT";
        }
    }


    /**
     * Generates an initially random distribution of particles.
     *
     * @return  Random particles.
     */
    private ArrayList<Particle> generateInitialParticles() {
        ArrayList<Particle> initialParticles = new ArrayList<>();
        int widthBoundary = map.getRequiredMinWidth();
        int heightBoundary = map.getRequiredMinHeight();
        Random random = new Random();

        int particlesInMap = 0;
        do {
            float x = random.nextFloat() % widthBoundary;
            float y = random.nextFloat() % heightBoundary;
            float h = random.nextFloat() % 360;
            double simpleExclusionCriterion = map.getDistanceToObstacle(x, y, h);
            if (Math.round(simpleExclusionCriterion - Double.MAX_VALUE) < EPSILON) {
                initialParticles.add(new Particle(x, y, h, 0));
            }
            particlesInMap++;
        } while (particlesInMap < NUMBER_OF_PARTICLES);
        return initialParticles;
    }



    /**
     * Performs the resampling of the particles.
     */
    private void resample() {
        ArrayList<Particle> resampled = new ArrayList<>();
        Random random = new Random();
        int index = random.nextInt() % NUMBER_OF_PARTICLES;
        double beta = 0;
        double maxWeight = getMaximumParticleWeight(particles);
        for (int i = 0  ;  i < NUMBER_OF_PARTICLES  ; i++) {
            beta += random.nextDouble() * 2 * maxWeight;
            double pWeight = particles.get(index).getWeight();
            while (beta > pWeight) {
                beta -= pWeight;
                index = (index +1) % NUMBER_OF_PARTICLES;
            }
            resampled.add(new Particle(particles.get(index)));
        }
        this.particles = resampled;
    }



    /**
     * Finds the maximum weight held by a particle from the given particle-set.
     *
     * @param particles     A list of particles.
     * @return              The highest weight-value from the particle-set.
     */
    private double getMaximumParticleWeight(ArrayList<Particle> particles) {
        double weight = 0;
        for (Particle p : particles) {
            double pWeight = p.getWeight();
            weight = pWeight > weight ? pWeight : weight;
        }
        return weight;
    }



    /**
     * Paints the JFrame after updating the scale-factor and offsets to cope with resizing of the window.
     * @param g     The graphical context.
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        updateVisualParameters();
        Graphics2D g2d = (Graphics2D) g;
        for (Particle p : particles) {
            p.paint(g);
        }
    }



    /**
     * (Re)Calculates the visual parameters, including
     * - the scale-factor to use the available space at its best
     * - the x- and y-offsets to center the simulation in the window.
     */
    private void updateVisualParameters() {
        int maxX = map.getRequiredMinWidth();
        int maxY = map.getRequiredMinHeight();

        double width = mapOverlay.getVisibleRect().getWidth();
        double height = mapOverlay.getVisibleRect().getHeight();

        double scaleX = width / maxX;
        double scaleY = height / maxY;

        double limitingFactor = scaleX > scaleY ? scaleY : scaleX;

        scaleFactor = (int) Math.abs(Math.floor(limitingFactor));

        xOffset = ((int) Math.abs(width) - (maxX * scaleFactor)) / 2;
        yOffset = ((int) Math.abs(height) - (maxY * scaleFactor)) / 2;
    }



    /**
     * A JPanel for showing the map.
     */
    private class MapOverlay extends JPanel {
        /**
         * Paints the map and the rover. Currently the wall-color is neglected.
         * @param g     The graphics-context to paint on.
         */
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            this.setBackground(Color.LIGHT_GRAY);
            if (map != null) map.paint(g, scaleFactor, xOffset, yOffset);

        }

    }


}
