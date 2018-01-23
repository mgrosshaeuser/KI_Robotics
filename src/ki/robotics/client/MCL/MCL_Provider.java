package ki.robotics.client.MCL;

import ki.robotics.utility.map.Map;
import ki.robotics.robot.MCLParticle;
import lejos.robotics.navigation.Pose;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.Thread.sleep;

/**
 * Utility-class for performing the monte-carlo-localization.
 *
 * @version 1.0 01/02/18
 */
public class MCL_Provider {
    private Map map;
    private int particleCount;
    private int fixedX;
    private int fixedY;
    private int fixedHeading;

    private ArrayList<MCLParticle> particles;

    /**
     * Constructor.
     *
     * @param map               The map used for the MCL.
     * @param particleCount     The number of particles used for the MCL.
     * @param limitations       Limitations for particle-location and -heading;
     */
    public MCL_Provider(Map map, int particleCount, int[] limitations) {
        this.map = map;
        this.particleCount = particleCount;
        this.fixedX = limitations[0];
        this.fixedY = limitations[1];
        this.fixedHeading = limitations[2];
        this.particles = generateSetOfRandomParticles();
    }


    /**
     * Returns a List of the particles.
     * @return  A list of the particles.
     */
    public ArrayList<MCLParticle> getParticles() {
        return this.particles;
    }


    /**
     * Generates a set of random particles.
     *
     * @return      A set of random particles.
     */
    private ArrayList<MCLParticle> generateSetOfRandomParticles() {
        ArrayList<MCLParticle> particles = new ArrayList<>();
        for (int i = 0  ;  i < particleCount  ;  i++) {
            particles.add(createRandomParticle(fixedX, fixedY, fixedHeading));
        }
        return particles;
    }


    /**
     * Creates one random particle within given limitations and map-bounds.
     *
     * @param fixedX        A fixed x-value; -1 if free.
     * @param fixedY        A fixed y-value; -1 if free.
     * @param fixedHeading  A fixed heading-balue; -1 if free.
     * @return              A random particle.
     */
    private MCLParticle createRandomParticle(int fixedX, int fixedY, int fixedHeading) {
        Polygon boundaries = map.getMapBoundaries();
        Rectangle limits = boundaries.getBounds();
        double xOffset = limits.getX();
        double yOffset = limits.getY();
        double widthLimit = limits.getWidth();
        double heightLimit = limits.getHeight();

        while (true) {
            int x = (fixedX >= 0) ? fixedX : (int)Math.round(Math.random() * widthLimit + xOffset);
            int y = (fixedY >= 0) ? fixedY : (int)Math.round(Math.random() * heightLimit + yOffset);
            int h = (fixedHeading >= 0 ? fixedHeading : (int) (Math.round(Math.random() * 4) *90));
            if (boundaries.contains(x, y)) {
                return new MCLParticle(new Pose(x, y, h), map, 1);
            }
        }
    }


    /**
     * Sets the particle-weights relative to the robot-feedback (SensorModel).
     *
     * @param bot   The sensor-model of the robot.
     */
    public void recalculateParticleWeight(SensorModel bot) {
        for (MCLParticle p : particles) {
            double deviation = calculateBotParticleDeviation(bot, p);
            p.setWeight((float)deviation);
            //p.setWeight(p.getWeight() * (float)calculateProbability(bot, p));
          }
    }

    /**
     * Calculates an absolute weight for a particle as the multiplicative inverse of the arithmetic
     * mean of the deviations of all used distance-sensor-directions.
     *
     * @param bot       The SensorModel to hold the robot-sensor-feedback.
     * @param particle  The particle to compare with the sensor-model.
     * @return          The absolute weight of the particle.
     */
    private double calculateBotParticleDeviation(SensorModel bot, MCLParticle particle) {
        float[] botDistances = bot.getAllDistances();
        double[] particleDistances = particle.ultrasonicThreeWayScan();

        int cnt = 0;
        double xDeviation = 0, yDeviation = 0, hDeviation = 0;
        int xWeight = 0, yWeight = 0, hWeight = 0;

        if (botDistances[0] > 0  &&  particleDistances[0] > 0) {
            xDeviation = Math.abs(botDistances[0] - particleDistances[0]);
            xWeight = deviationToWeight(xDeviation, botDistances[0]);
            cnt++;
        }

        if (botDistances[1] > 0  &&  particleDistances[1] > 0) {
            yDeviation = Math.abs(botDistances[1] - particleDistances[1]);
            yWeight = deviationToWeight(yDeviation, botDistances[1]);
            cnt++;
        }
        if (botDistances[2] > 0  &&  particleDistances[2] > 0) {
            hDeviation = Math.abs(botDistances[2] - particleDistances[2]);
            hWeight = deviationToWeight(hDeviation, botDistances[2]);
            cnt ++;
        }

        int deviation = xWeight + yWeight + hWeight;
        if (deviation > 0) {
            return 1.0 / (double)deviation;
        }
        return 1;
    }


    private int deviationToWeight(double deviation, double referenceValue) {
        if (deviation > 0.9 * referenceValue) {
            return 16;
        } else if (deviation > 0.75 * referenceValue) {
            return 8;
        } else if (deviation > 0.5 * referenceValue) {
            return 4;
        } else if (deviation > 0.25 * referenceValue) {
            return 2;
        } else {
            return 1;
        }
    }


    private double calculateProbability(SensorModel bot, MCLParticle particle) {
        float[] botDistances = bot.getAllDistances();
        double[] particleDistances = particle.ultrasonicThreeWayScan();

        double meanLeft = particleDistances[0];
        double measuredLeft = botDistances[0];

        double meanCenter = particleDistances[1];
        double measuredCenter = botDistances[1];

        double meanRight = particleDistances[2];
        double measuredRight = botDistances[2];

        double sigma = 3;

        double pLeft = (1.0/(sigma * Math.sqrt(2.0 * Math.PI))) * Math.exp(-((Math.pow(measuredLeft - meanLeft, 2))/(2.0 * Math.pow(sigma, 2))));
        double pCenter = (1.0/(sigma * Math.sqrt(2.0 * Math.PI))) * Math.exp(-((Math.pow(measuredCenter - meanCenter, 2))/(2.0 * Math.pow(sigma, 2))));
        double pRight = (1.0/(sigma * Math.sqrt(2.0 * Math.PI))) * Math.exp(-((Math.pow(measuredRight - meanRight, 2))/(2.0 * Math.pow(sigma, 2))));

        return (pLeft + pCenter + pRight) /3;
    }


    /**
     * Normalizes the weight of all particles.
     */
    public void normalizeParticleWeight() {
        double sum = getSumOfParticleWeights();
        for (MCLParticle p : particles) {
            p.setWeight((float)(p.getWeight() / sum));
        }
    }


    /**
     * Resamples the particles.
     */
    public void resample() {
        normalizeParticleWeight();
        Random r = new Random();
        ArrayList<MCLParticle> newSet = new ArrayList<>();
        int index = Math.abs(r.nextInt()) % particleCount;
        double beta = 0.0;
        double maxWeight = getHighestParticleWeight();
        for (int i = 0  ;  i < particleCount  ;  i++) {
            beta += r.nextDouble() * 2 * maxWeight;
            while (beta > particles.get(index).getWeight()) {
                beta -= particles.get(index).getWeight();
                index = (index + 1) % particleCount;
            }
            newSet.add(new MCLParticle(particles.get(index)));
        }
        particles = newSet;
    }

    private void printParticles(String indicator) {
        System.out.println("\n" + indicator);
        for (MCLParticle p : particles) {
            System.out.print(p.getWeight() + " ");
        }
        if (particles.get(0).getWeight() != particles.get(0).getWeight()) {
            try {
                sleep(2000);
                System.exit(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Finds the highest weight among all particles.
     *
     * @return  The highest weight in the particle-set.
     */
    public float getHighestParticleWeight() {
        float weight = 0f;
        for (MCLParticle p : particles) {
            float pWeight = p.getWeight();
            weight = pWeight > weight ? pWeight : weight;
        }
        return weight;
    }


    public float getMedianParticleWeight() {
        return (float)( getSumOfParticleWeights() / particleCount);
    }

    /**
     * Sums up the weights of all particles.
     *
     * @return  The sum of the weight of all particles.
     */
    private double getSumOfParticleWeights() {
        double sum = 0;
        for (MCLParticle p : particles) {
                sum += p.getWeight();
        }
        return sum;
    }


    /**
     * Tries to guess the robot-position by calculating the arithmetic means of the x- and y-coordinates and
     * the heading of the particles, while mcl is still running.
     *
     * @return  The estimated Pose of the robot.
     */
    public Pose getEstimatedBotPose() {
        double xSum = 0;
        double ySum = 0;
        double hSum = 0;
        for (MCLParticle p : particles) {
            Pose pPose = p.getPose();
            xSum += pPose.getX();
            ySum += pPose.getY();
            hSum += pPose.getHeading();
        }
        Pose p = new Pose();
        p.setLocation((float)(xSum/particleCount), (float)(ySum/particleCount));
        p.setHeading((float)(hSum/particleCount));
        return p;
    }


    /**
     * Performes a translation of the particles over the given distance.
     *
     * @param distance  The distance to translate each particle.
     */
    public void translateParticle(float distance) {
        resample();
        Random r = new Random();
        for (MCLParticle p : particles) {
            float d = (float)Math.abs(r.nextGaussian());
            p.botTravelForward(distance * (1 +(d/10)));
        }
    }


    /**
     * Turns the heading of the particles.
     *
     * @param degrees   The degrees to turn.
     */
    public void turnFull(int degrees){
        Random r = new Random();
        for (MCLParticle p : particles) {
            //double d = r.nextGaussian();            //????????????????????
            //degrees = (int) Math.round(degrees * (1+(d/10)));
            p.turnFull(degrees);
        }
    }
}
