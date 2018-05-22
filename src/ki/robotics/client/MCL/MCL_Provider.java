package ki.robotics.client.MCL;

import ki.robotics.utility.map.Map;
import ki.robotics.server.robot.virtualRobots.MCLParticle;
import ki.robotics.utility.pixyCam.DTOGeneralQuery;
import ki.robotics.utility.pixyCam.DTOSignatureQuery;
import lejos.robotics.navigation.Pose;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Utility-class for performing the monte-carlo-localization.
 *
 * @version 1.0 01/02/18
 */
public class MCL_Provider {
    //private static final int[] RESAMPLING_WEIGHTS = new int[]{20,10,5,2,1};     // Best working with physical robot.
    private static final int[] RESAMPLING_WEIGHTS = new int[]{81,27,9,3,1};   // Best working in Simulation.
    private static final Color RESAMPLING_WHEEL_PARTICLE_INITIAL_COLOR = Color.GRAY;


    private static double[] resamplingWheelFractions;
    private static Color[] resamplingWheelColors;



    public static final int MCL_TOLERANCE_FOR_LOCAL_LOCALIZATION = 20;

    private final Map map;
    private final int particleCount;
    private final int fixedX;
    private final int fixedY;
    private final int fixedHeading;
    private final Configuration configuration;
    private final int acceptableTolerance;
    private final ArrayList<MCLParticle> particles;
    private boolean localized;

    private ArrayList<ParticleSet> particleSets = new ArrayList<>();
    private ParticleSet currentSet;

    private ResamplingWheelVisual wheelVisual;

    /**
     * Constructor.
     *
     * @param map               The map used for the MCL.
     * @param particleCount     The number of particles used for the MCL.
     * @param limitations       Limitations for particle-location and -heading;
     */
    public MCL_Provider(Map map, int particleCount, int[] limitations, Configuration configuration) {
        this.map = map;
        this.particleCount = particleCount;
        this.fixedX = limitations[0];
        this.fixedY = limitations[1];
        this.fixedHeading = limitations[2];
        this.configuration = configuration;
        this.particles = generateSetOfRandomParticles();

        //erstellte Partikel zu set hinzufügen, set speichern
        this.currentSet = new ParticleSet(particles);
        this.particleSets.add(currentSet);

        if (configuration.isWithCamera()) {
            this.acceptableTolerance = MCL_TOLERANCE_FOR_LOCAL_LOCALIZATION;
        } else {
            this.acceptableTolerance = configuration.getAcceptableTolerance();
        }
        resamplingWheelFractions = createResamplingWheelCategoryArray();
        resamplingWheelColors = createResamplingWheelColorArray();
        this.wheelVisual = new ResamplingWheelVisual(resamplingWheelFractions, resamplingWheelColors, currentSet.getParticles());
    }



    private double[] createResamplingWheelCategoryArray() {
        double d[] = new double[36];
        int x=0;
        for (int i = 0   ;   i < RESAMPLING_WEIGHTS.length   ;   i++) {
            for (int j = i   ;   j < RESAMPLING_WEIGHTS.length   ;   j++) {
                for (int k = j   ;   k < RESAMPLING_WEIGHTS.length   ;   k++) {
                    int sum = RESAMPLING_WEIGHTS[i] + RESAMPLING_WEIGHTS[j] + RESAMPLING_WEIGHTS[k];
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


    /**
     * Returns a List of the particles.
     * @return  A list of the particles.
     */
    public ArrayList<MCLParticle> getParticles() {
        return this.currentSet.getParticles();
    }


    public int getAcceptableTolerance() { return acceptableTolerance; }

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
     * @param fixedHeading  A fixed heading-value; -1 if free.
     * @return              A random particle.
     */
    private MCLParticle createRandomParticle(int fixedX, int fixedY, int fixedHeading) {
        Polygon boundaries = map.getOperatingRange();
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
                return new MCLParticle(new Pose(x, y, h), map, 1, RESAMPLING_WHEEL_PARTICLE_INITIAL_COLOR);
            }
        }
    }


    /**
     * Sets the particle-weights relative to the robot-feedback (SensorModel).
     *
     * @param bot   The sensor-model of the robot.
     */
    public void recalculateParticleWeight(SensorModel bot) {
        ArrayList<MCLParticle> particles = currentSet.getParticles();
        for (MCLParticle p : particles) {
            if (p.isOutOfBounds()) {
                p.setWeight(0);
                p.setColor(Color.BLACK);
            } else {
                double deviation = calculateBotParticleDeviation(bot, p);
                p.setWeight((float) deviation);
                p.setColor(weightToColor(deviation));
            }
        }
        wheelVisual.update(particles);
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

        /*
        Factor the weight from uss scans get multiplied with, depending on how much the particles view deviates from the bots camera view. Between 1 and 3.
         */
        double seeingColorScale = 1;
        if (configuration.isWithCamera() ){
            double camDeviation = calculatedCameraSupportedDeviation(bot, particle);
            seeingColorScale = ( camDeviation > 0 ) ? camDeviation*4+1 : 1;
          }

        //0 is left, 1 is center, 2 is right
        double[] botDistances = bot.getAllDistances();
        double[] particleDistances = particle.ultrasonicThreeWayScan();

        double leftDeviation, centerDeviation, rightDeviation;
        int leftWeight = 0, centerWeight = 0, rightWeight = 0;

        if (botDistances[0] > 0  &&  particleDistances[0] > 0) {
            leftDeviation = Math.abs(botDistances[0] - particleDistances[0]);
            leftWeight = deviationToWeight(leftDeviation, botDistances[0]);
        }

        if (botDistances[1] > 0  &&  particleDistances[1] > 0) {
            centerDeviation = Math.abs(botDistances[1] - particleDistances[1]);
            centerWeight = deviationToWeight(centerDeviation, botDistances[1]);
        }
        if (botDistances[2] > 0  &&  particleDistances[2] > 0) {
            rightDeviation = Math.abs(botDistances[2] - particleDistances[2]);
            rightWeight = deviationToWeight(rightDeviation, botDistances[2]);
        }

        int deviation = leftWeight + centerWeight + rightWeight;
        if (deviation > 0) {
            return (1.0 / (double)deviation) * seeingColorScale;
        }
        return 1;
    }


    private double calculatedCameraSupportedDeviation(SensorModel bot, MCLParticle particle) {
        DTOGeneralQuery botGeneralQuery = bot.getGeneralQuery();
        DTOGeneralQuery particleGeneralQuery = new DTOGeneralQuery(particle.cameraGeneralQuery());

        if (botGeneralQuery.getSignatureOfLargestBlock() == particleGeneralQuery.getSignatureOfLargestBlock()) {
            DTOSignatureQuery botSignatureQuery, particleSignatureQuery;
            switch (botGeneralQuery.getSignatureOfLargestBlock()) {
                case 1:
                    botSignatureQuery = bot.getSignatureQuery1();
                    particleSignatureQuery = new DTOSignatureQuery(particle.cameraSignatureQuery(1));
                    break;
                case 2:
                    botSignatureQuery = bot.getSignatureQuery2();
                    particleSignatureQuery = new DTOSignatureQuery(particle.cameraSignatureQuery(2));
                    break;
                case 3:
                    botSignatureQuery = bot.getSignatureQuery3();
                    particleSignatureQuery = new DTOSignatureQuery(particle.cameraSignatureQuery(3));
                    break;
                case 4:
                    botSignatureQuery = bot.getSignatureQuery4();
                    particleSignatureQuery = new DTOSignatureQuery(particle.cameraSignatureQuery(4));
                    break;
                case 5 :
                    botSignatureQuery = bot.getSignatureQuery5();
                    particleSignatureQuery = new DTOSignatureQuery(particle.cameraSignatureQuery(5));
                    break;
                case 6 :
                    botSignatureQuery = bot.getSignatureQuery6();
                    particleSignatureQuery = new DTOSignatureQuery(particle.cameraSignatureQuery(6));
                    break;
                case 7:
                    botSignatureQuery = bot.getSignatureQuery7();
                    particleSignatureQuery = new DTOSignatureQuery(particle.cameraSignatureQuery(7));
                    break;
                default:
                    return 0;
            }

            double deviation = particle.getWeight();
            if ( botSignatureQuery != null) {
                double angleDeviation = Math.abs(botSignatureQuery.getxCenterOfLargestBlock() - particleSignatureQuery.getxCenterOfLargestBlock());
                double sizeDeviation = Math.abs(botSignatureQuery.getWidthOfLargestBlock() - particleSignatureQuery.getWidthOfLargestBlock());
                double angleRelatedWeight = deviationToWeight(angleDeviation, botSignatureQuery.getxCenterOfLargestBlock());
                double sizeRelatedWeight = deviationToWeight(sizeDeviation, botSignatureQuery.getWidthOfLargestBlock());
                deviation += 1.0 / (angleRelatedWeight + sizeRelatedWeight);
            }
            return deviation;
        }

        return 0;
    }


    private int deviationToWeight(double deviation, double referenceValue) {
        if (deviation > 0.9 * referenceValue) {
            return RESAMPLING_WEIGHTS[0];
        } else if (deviation > 0.75 * referenceValue) {
            return RESAMPLING_WEIGHTS[1];
        } else if (deviation > 0.5 * referenceValue) {
            return RESAMPLING_WEIGHTS[2];
        } else if (deviation > 0.25 * referenceValue) {
            return RESAMPLING_WEIGHTS[3];
        } else {
            return RESAMPLING_WEIGHTS[4];
        }
    }

    private Color weightToColor(double weight) {

        double epsilon = 0.001;

        for (int i = 0; i < resamplingWheelFractions.length   ; i++) {
            if (Math.abs(weight - resamplingWheelFractions[i]) < epsilon) {
                return resamplingWheelColors[i];
            }
        }
        return Color.CYAN;
    }



    /**
     * Normalizes the weight of all particles.
     */
    private void normalizeParticleWeight() {
        double sum = getSumOfParticleWeights();
        for (MCLParticle p : currentSet.getParticles()) {
            p.setWeight((float)(p.getWeight() / sum));
        }
    }


    /**
     * Resamples the particles.
     */
    private void resample() {
        normalizeParticleWeight();
        Random r = new Random();
        ArrayList<MCLParticle> newSet = new ArrayList<>();
        int index = Math.abs(r.nextInt()) % particleCount;
        double beta = 0.0;
        double maxWeight = getHighestParticleWeight();
        for (int i = 0  ;  i < particleCount  ;  i++) {
            beta += r.nextDouble() * 2 * maxWeight;
            while (beta > currentSet.getParticles().get(index).getWeight()) {
                beta -= currentSet.getParticles().get(index).getWeight();
                index = (index + 1) % particleCount;
            }
            newSet.add(new MCLParticle(currentSet.getParticles().get(index)));
        }
        currentSet = new ParticleSet(newSet);
        particleSets.add(currentSet);
        checkLocalizationStatus();
    }

    /**
     * Finds the highest weight among all particles.
     *
     * @return  The highest weight in the particle-set.
     */
    private float getHighestParticleWeight() {
        float weight = 0f;
        for (MCLParticle p : currentSet.getParticles()) {
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
        for (MCLParticle p : currentSet.getParticles()) {
                sum += p.getWeight();
        }
        return sum;
    }


    /**
     * Tries to guess the robot-position by calculating the arithmetic means of the x- and y-coordinates and
     * the heading of the particles, while mcl is still ready.
     *
     * @return  The estimated Pose of the robot.
     */
    public Pose getEstimatedBotPose() {
        double xSum = 0;
        double ySum = 0;
        double hSum = 0;
        for (MCLParticle p : currentSet.getParticles()) {
            Pose pPose = p.getPose();
            xSum += pPose.getX();
            ySum += pPose.getY();
            hSum += pPose.getHeading();
        }
        float estimatedX = (float) xSum/particleCount;
        float estimatedY = (float) ySum/particleCount;
        float estimatedHeading = (float) hSum/particleCount;
        return new Pose(estimatedX, estimatedY, estimatedHeading);
    }


    private void checkLocalizationStatus() {
        if (localized) {
            return;
        }
        Pose bPose = getEstimatedBotPose();
        for (MCLParticle p : currentSet.getParticles()) {
            Pose pPose = p.getPose();
            double dx = pPose.getX()- bPose.getX();
            double dy = pPose.getY()-bPose.getY();
            double distance;
            if (configuration.isOneDimensional()) {
                if (Math.abs(dx) > acceptableTolerance) {
                    localized = false;
                    return;
                }
            } else {
                distance = Math.sqrt((Math.pow(dx, 2)) + (Math.pow(dy, 2)));
                if (distance > acceptableTolerance) {
                    localized = false;
                    return;
                }
            }
        }
        localized = true;
    }


    public boolean isLocalizationDone() {
        return localized;
    }


    public double getEstimatedBotPoseDeviation() {
        double distance = 0;
        Pose bPose = getEstimatedBotPose();
        for (MCLParticle p : currentSet.getParticles()) {
            Pose pPose = p.getPose();
            double dx = pPose.getX()- bPose.getX();
            double dy = pPose.getY()-bPose.getY();
            double pDistance = Math.sqrt((Math.pow(dx, 2)) + (Math.pow(dy, 2)));
            distance = distance > pDistance ? distance : pDistance;
        }
        return distance;
    }


    public void badParticlesFinalKill() {
       resample();
    }


    /**
     * Performes a translation of the particles over the given distance.
     *
     * @param distance  The distance to translate each particle.
     */
    public void translateParticle(double distance) {
        resample();
        Random r = new Random();
        for (MCLParticle p : currentSet.getParticles()) {
            float d = (float) r.nextGaussian();
            while (d < -1  ||  d > 1) {
                d = (float) r.nextGaussian();
            }
            p.botTravelForward(distance * (1 +(d/10)));
            //p.botTravelForward(distance);
        }
    }


    /**
     * Turns the heading of the particles.
     *
     * @param degrees   The degrees to turn.
     */
    public void turnFull(double degrees){
        Random r = new Random();
        for (MCLParticle p : currentSet.getParticles()) {
            double d = r.nextGaussian();
            degrees = configuration.isTwoDimensional() ? (int) Math.round(degrees * (1+(d/540))) : (int) Math.round(degrees);
            p.turnFull((int)degrees);
        }
    }

    public void saveParticlesToFile(){
        try {
            DateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
            Date date = new Date();
            BufferedWriter writer = new BufferedWriter(new FileWriter(sdf.format(date)+".txt"));
            int s=1;
            Pose pose;
            for(ParticleSet p: particleSets){
                writer.write("set" + s++ + ":");
                writer.newLine();
                int i=1;
                for(MCLParticle particle : p.getParticles()){
                    pose = particle.getPose();
                    writer.write("particle" + i++ + ": " + pose.toString() + " W:" + particle.getWeight()+ " C:" + particle.getColor() + ";");
                }
                writer.newLine();


            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ParticleSet{
        private ArrayList<MCLParticle> particles;
        private ParticleSet(ArrayList<MCLParticle> particles){
            this.particles = particles;
        }

        private void setParticles(ArrayList<MCLParticle> particles){
            this.particles=particles;
        }
        private ArrayList<MCLParticle> getParticles(){
            return particles;
        }

    }
}
