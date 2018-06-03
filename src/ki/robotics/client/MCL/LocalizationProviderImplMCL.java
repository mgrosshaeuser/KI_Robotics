package ki.robotics.client.MCL;

import ki.robotics.client.GUI.Configuration;
import ki.robotics.client.SensorModel;
import ki.robotics.server.robot.virtualRobots.MCLParticle;
import ki.robotics.utility.map.Map;
import ki.robotics.utility.pixyCam.DTOGeneralQuery;
import ki.robotics.utility.pixyCam.DTOSignatureQuery;
import lejos.robotics.navigation.Pose;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

/**
 * Utility-class for performing the monte-carlo-localization.
 *
 * @version 1.0 01/02/18
 */
public class LocalizationProviderImplMCL implements LocalizationProvider {
    private MclModel mclModel;


    /**
     * Constructor.
     *
     * @param map                   The map used for localization
     * @param numberOfParticles     The number of particles to distribute in the map
     * @param limitations           Limitations for particle-values regarding x- or y-Axis or heading
     * @param userSettings          User-settings for localization
     */
    public LocalizationProviderImplMCL(Map map, int numberOfParticles, int[] limitations, Configuration userSettings) {
        ArrayList<MCLParticle> particles = generateInitialParticleSet(numberOfParticles, map, limitations);
        WorldStateImplMCL ws = new WorldStateImplMCL(this, map, particles);
        this.mclModel = new MclModel(ws, userSettings);
        mclModel.takeSnapShot();
    }



    /**
     * Returns the acceptable spreading of the particles around the estimated bot-position.
     *
     * @return  The acceptable spreading.
     */
    @Override
    public int getAcceptableSpreading() { return mclModel.getAcceptableSpreading(); }



    /**
     * Returns a List of the particles from the associated MCL-Model.
     *
     * @return  A list of the particles.
     */
    @Override
    public ArrayList<MCLParticle> getParticles() {
        return mclModel.getParticles();
    }



    /**
     * Generates a set of random particles.
     *
     * @return      A set of random particles.
     */
    private ArrayList<MCLParticle> generateInitialParticleSet(int numberOfParticles, Map map, int[] limitations) {
        ArrayList<MCLParticle> particles = new ArrayList<>();
        for (int i = 0  ;  i < numberOfParticles  ;  i++) {
            particles.add(createRandomParticle(map, limitations));
        }
        return particles;
    }


    /**
     * Creates one random particle within given limitations and map-bounds.
     *
     * @param limitations   Limitations for x- and y-Axis and heading.
     * @return              A random particle.
     */
    private MCLParticle createRandomParticle(Map map, int[] limitations) {
        Polygon boundaries = map.getOperatingRange();
        Rectangle limits = boundaries.getBounds();
        double xOffset = limits.getX();
        double yOffset = limits.getY();
        double widthLimit = limits.getWidth();
        double heightLimit = limits.getHeight();

        while (true) {
            int x = (limitations[0] >= 0) ? limitations[0] : (int)Math.round(Math.random() * widthLimit + xOffset);
            int y = (limitations[1] >= 0) ? limitations[1] : (int)Math.round(Math.random() * heightLimit + yOffset);
            int h = (limitations[2] >= 0  ? limitations[2] : (int) (Math.round(Math.random() * 4) *90));
            if (boundaries.contains(x, y)) {
                return new MCLParticle(new Pose(x, y, h), map, 1, Color.GRAY);
            }
        }
    }


    /**
     * Recalculates the particle-weights based on the current sensor-model (sensor-feedback from the robot).
     *
     * @param bot   The sensor-model.
     */
    @Override
    public void recalculateParticleWeight(SensorModel bot) {
        ArrayList<MCLParticle> particles = mclModel.getParticles();
        for (MCLParticle p : particles) {
            if (p.isOutOfBounds()) {
                p.setWeight(0);
                p.setColor(Color.BLACK);
            } else {
                double deviation = calculateBotParticleDeviation(bot, p);
                p.setWeight((float) deviation);
                p.setColor(mclModel.getResamplingWheel().weightToColor(deviation));
            }
        }
        mclModel.getResamplingWheel().update(particles);
        mclModel.setSensorModel(bot);
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
        if (mclModel.getUserSettings().isWithCamera() ){
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


    /**
     * Maps the deviation from a reference-value to a weight-category for the particles.
     *
     * @param deviation         Deviation between calculated distance and sensor-feedback
     * @param referenceValue    Sensor-feedback
     * @return                  The weight associated with the given deviation from the reference-value
     */
    private int deviationToWeight(double deviation, double referenceValue) {
        int[] resamplingWeights = MclModel.RESAMPLING_WEIGHTS;
        if (deviation > 0.9 * referenceValue) {
            return resamplingWeights[0];
        } else if (deviation > 0.75 * referenceValue) {
            return resamplingWeights[1];
        } else if (deviation > 0.5 * referenceValue) {
            return resamplingWeights[2];
        } else if (deviation > 0.25 * referenceValue) {
            return resamplingWeights[3];
        } else {
            return resamplingWeights[4];
        }
    }


    /**
     * Normalizes the weight of all particles.
     */
    private void normalizeParticleWeight() {
        double sum = getSumOfParticleWeights();
        for (MCLParticle p : mclModel.getParticles()) {
            p.setWeight((float)(p.getWeight() / sum));
        }
    }


    /**
     * Resampling of the particles. Implementation of the ResamplingWheel.
     */
    private void resample() {
        normalizeParticleWeight();
        Random r = new Random();
        ArrayList<MCLParticle> resampledParticles = new ArrayList<>();
        int particleCount = mclModel.getNumberOfParticles();
        int index = Math.abs(r.nextInt()) % particleCount;
        double beta = 0.0;
        double maxWeight = getHighestParticleWeight();
        for (int i = 0  ;  i < particleCount  ;  i++) {
            beta += r.nextDouble() * 2 * maxWeight;
            while (beta > mclModel.getParticles().get(index).getWeight()) {
                beta -= mclModel.getParticles().get(index).getWeight();
                index = (index + 1) % particleCount;
            }
            MCLParticle clonedParticle = mclModel.getParticles().get(index).getClone();
            clonedParticle.setWeight(0);
            resampledParticles.add(clonedParticle);
        }
        mclModel.setParticles(resampledParticles);
        mclModel.logInstruction("Resample");
        mclModel.takeSnapShot();
        checkLocalizationStatus();
    }


    /**
     * Finds the highest weight among all particles.
     *
     * @return  The highest weight in the particle-set.
     */
    private float getHighestParticleWeight() {
        float weight = 0f;
        for (MCLParticle p : mclModel.getParticles()) {
            float pWeight = p.getWeight();
            weight = pWeight > weight ? pWeight : weight;
        }
        return weight;
    }



    /**
     * Sums up the weights of all particles.
     *
     * @return  The sum of the weight of all particles.
     */
    private double getSumOfParticleWeights() {
        double sum = 0;
        for (MCLParticle p : mclModel.getParticles()) {
                sum += p.getWeight();
        }
        return sum;
    }


    /**
     * Returns the current estimation for the pose of the robot as Pose-object.
     *
     * @return  The current pose-estimation of the robot (as Pose)
     */
    private Pose getEstimatedBotPose() {
        double[] estimatedPose = getEstimatedPose();
        float estimatedX = (float) estimatedPose[0];
        float estimatedY = (float) estimatedPose[1];
        float estimatedHeading = (float) estimatedPose[2];
        return new Pose(estimatedX, estimatedY, estimatedHeading);
    }


    /**
     * Estimates the current robot-position by calculating the arithmetic means of the x- and
     * y-coordinates and the heading of the particles.
     *
     * @return  The estimated Pose of the robot as double-Array containing x, y and heading.
     */
    @Override
    public double[] getEstimatedPose() {
        double xSum = 0;
        double ySum = 0;
        double hSum = 0;
        for (MCLParticle p : mclModel.getParticles()) {
            Pose pPose = p.getPose();
            xSum += pPose.getX();
            ySum += pPose.getY();
            hSum += pPose.getHeading();
        }
        int particleCount = mclModel.getNumberOfParticles();
        double estimatedX = xSum/particleCount;
        double estimatedY = ySum/particleCount;
        double estimatedHeading = hSum/particleCount;
        return new double[]{estimatedX, estimatedY, estimatedHeading};
    }


    /**
     * Returns the distance from the estimated robot-position to the farthest particle.
     *
     * @return  The distance from the estimated robot-position to the farthest particle
     */
    public double getSpreadingAroundEstimatedBotPose() {
        double distance = 0;
        Pose bPose = getEstimatedBotPose();
        for (MCLParticle p : mclModel.getParticles()) {
            Pose pPose = p.getPose();
            double dx = pPose.getX()- bPose.getX();
            double dy = pPose.getY()-bPose.getY();
            double pDistance = Math.sqrt((Math.pow(dx, 2)) + (Math.pow(dy, 2)));
            distance = distance > pDistance ? distance : pDistance;
        }
        return distance;
    }


    /**
     * Checks whether all particles lie within the acceptable spreading and updates the data-model.
     */
    private void checkLocalizationStatus() {
        if (mclModel.isLocalized()) {
            return;
        }
        Pose bPose = getEstimatedBotPose();
        for (MCLParticle p : mclModel.getParticles()) {
            Pose pPose = p.getPose();
            double dx = pPose.getX()- bPose.getX();
            double dy = pPose.getY()-bPose.getY();
            double distance;
            if (mclModel.getUserSettings().isOneDimensional()) {
                if (Math.abs(dx) > mclModel.getAcceptableSpreading()) {
                    mclModel.setLocalized(false);
                    return;
                }
            } else {
                distance = Math.sqrt((Math.pow(dx, 2)) + (Math.pow(dy, 2)));
                if (distance > mclModel.getAcceptableSpreading()) {
                    mclModel.setLocalized(false);
                    return;
                }
            }
        }
        mclModel.setLocalized(true);
    }


    /**
     * Returns a boolean value indicating whether localization is finished (true) or not (false).
     *
     * @return  A boolean value indicating wheter localization is finished or not
     */
    @Override
    public boolean isLocalizationDone() {
        return mclModel.isLocalized();
    }


    /**
     * Performs a final resampling and saves the final localization-state.
     */
    @Override
    public void badParticlesFinalKill() {
       resample();
    }


    /**
     * Performs a translation of the particles over the given distance.
     *
     * @param distance  The distance to translate each particle.
     */
    @Override
    public void translateParticles(double distance) {
        resample();
        Random r = new Random();
        for (MCLParticle p : mclModel.getParticles()) {
            float d = (float) r.nextGaussian();
            while (d < -1  ||  d > 1) {
                d = (float) r.nextGaussian();
            }
            p.botTravelForward(distance * (1 +(d/10)));
        }
        mclModel.logInstruction("Move " + String.valueOf(distance));
        mclModel.takeSnapShot();
    }


    /**
     * Turns the heading of the particles.
     *
     * @param degrees   The degrees to turn.
     */
    @Override
    public void turnParticles(double degrees){
        Random r = new Random();
        for (MCLParticle p : mclModel.getParticles()) {
            double d = r.nextGaussian();
            degrees = mclModel.getUserSettings().isTwoDimensional() ? (int) Math.round(degrees * (1+(d/540))) : (int) Math.round(degrees);
            p.turnFull((int)degrees);
        }
        mclModel.logInstruction("Turn " + String.valueOf(degrees));
        mclModel.takeSnapShot();
    }


    /**
     * Saves the (serialized) localization-sequence to a file using the current date and time as filename.
     */
    @Override
    public void saveLocalizationSequenceToFile() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String filename = simpleDateFormat.format(calendar.getTime());
        String file = String.valueOf(filename + ".log");
        try (FileOutputStream fileOS = new FileOutputStream(file)) {
            ObjectOutputStream objectOS = new ObjectOutputStream(fileOS);
            objectOS.writeObject(mclModel.getWorldStateSequence());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Forwards the request to reset to the latest world-state to the data-model.
     */
    @Override
    public void resetToLatestWorldState() {
        mclModel.resetToLatestWorldState();
    }


    /**
     * Forwards the request to make a step back in the localization-history to the data-model.
     */
    @Override
    public void stepBackInLocalizationHistory() {
        mclModel.navigateBackwardInHistory();
    }


    /**
     * Forwards the request to make a step forward in the localization-history to the data-model.
     */
    @Override
    public void stepForwardInLocalizationHistory() {
        mclModel.navigateForwardInHistory();
    }

}
