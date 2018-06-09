package ki.robotics.client.MCL.impl;

import ki.robotics.client.ClientFactory;
import ki.robotics.client.GUI.GuiConfiguration;
import ki.robotics.client.MCL.LocalizationProvider;
import ki.robotics.client.MCL.SensorModel;
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
 */
public class LocalizationProviderImplMCL implements LocalizationProvider {
    private MclModel mclModel;
    private ParticleSetResampler particleSetResampler;
    private ParticleSetEvaluator particleSetEvaluator;
    private BotToParticleMotionMapper botToParticleMotionMapper;
    private BotPoseEstimator botPoseEstimator;
    private LocalizationRecorder localizationRecorder;





    /**
     * Constructor.
     *
     * @param map                   The map used for localization
     * @param numberOfParticles     The number of particles to distribute in the map
     * @param limitations           Limitations for particle-values regarding x- or y-Axis or heading
     * @param userSettings          User-settings for localization
     */
    public LocalizationProviderImplMCL(Map map, int numberOfParticles, int[] limitations, GuiConfiguration userSettings) {
        this.localizationRecorder = new LocalizationRecorder();

        ParticleSetGenerator particleSetGenerator = new ParticleSetGenerator(numberOfParticles, map, limitations);
        ArrayList<ParticleImplMCL> particles = particleSetGenerator.generateInitialParticleSet();
        WorldStateImplMCL ws = new WorldStateImplMCL(this, map, particles);

        this.mclModel = new MclModel(ws, userSettings);
        this.particleSetResampler = new ParticleSetResampler(localizationRecorder);
        this.particleSetEvaluator = new ParticleSetEvaluator();
        this.botToParticleMotionMapper = new BotToParticleMotionMapper(particleSetResampler, localizationRecorder);
        this.botPoseEstimator = new BotPoseEstimator();

        localizationRecorder.takeSnapShot();
    }





    /**
     * Returns the acceptable spreading of the particles around the estimated bot-position.
     *
     * @return  The acceptable spreading.
     */
    @Override
    public int getAcceptableSpreading() { return mclModel.getAcceptableSpreading(); }



    /**
     * Returns a List of the particles used for localization.
     *
     * @return  A list of the particles.
     */
    @Override
    public ArrayList<ParticleImplMCL> getParticles() {
        return mclModel.getParticles();
    }



    /**
     * Recalculates the particle-weights based on the current sensor-model (sensor-feedback from the robot).
     *
     * @param bot   The sensor-model.
     */
    @Override
    public void recalculateParticleWeight(SensorModel bot) {
        particleSetEvaluator.recalculateParticleWeight(bot);
    }



    /**
     * Estimates the current robot-pose.
     *
     * @return  The estimated Pose of the robot as double-Array containing x, y and heading.
     */
    @Override
    public double[] getEstimatedPose() {
        return botPoseEstimator.getEstimatedPose();
    }



    /**
     * Returns the distance from the estimated robot-position to the farthest particle.
     *
     * @return  The distance from the estimated robot-position to the farthest particle
     */
    @Override
    public double getSpreadingAroundEstimatedBotPose() {
        return botPoseEstimator.getSpreadingAroundEstimatedBotPose();
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
       particleSetResampler.resample();
    }



    /**
     * Performs a translation of the particles over the given distance.
     *
     * @param distance  The distance to translate each particle.
     */
    @Override
    public void translateParticles(double distance) {
        botToParticleMotionMapper.translateParticles(distance);
    }



    /**
     * Turns the heading of the particles.
     *
     * @param degrees   The degrees to turn.
     */
    @Override
    public void turnParticles(double degrees){
        botToParticleMotionMapper.turnParticles(degrees);
    }



    /**
     * Saves the (serialized) localization-sequence to a file using the current date and time as filename.
     */
    @Override
    public void saveLocalizationSequenceToFile() {
        localizationRecorder.saveLocalizationSequenceToFile();
    }



    /**
     * Forwards the request to reset to the latest world-state to the data-model.
     */
    @Override
    public void resetToLatestWorldState() {
        localizationRecorder.resetToLatestWorldState();
    }



    /**
     * Forwards the request to make a step back in the localization-history to the data-model.
     */
    @Override
    public void stepBackInLocalizationHistory() {
        localizationRecorder.stepBackInLocalizationHistory();
    }



    /**
     * Forwards the request to make a step forward in the localization-history to the data-model.
     */
    @Override
    public void stepForwardInLocalizationHistory() {
        localizationRecorder.stepForwardInLocalizationHistory();
    }





    /**
     * Generator for the initial particle-set used for localization.
     */
    private class ParticleSetGenerator {
        private int numberOfParticles;
        private Map map;
        private int[] limitations;


        /**
         * Constructor.
         *
         * @param numberOfParticles     The number of particles to distribute in the map
         * @param map                   The map used for localization
         * @param limitations           Limitations for particle-values regarding x- or y-Axis or heading
         */
        ParticleSetGenerator(int numberOfParticles, Map map, int[] limitations) {
            this.numberOfParticles = numberOfParticles;
            this.map = map;
            this.limitations = limitations;
        }


        /**
         * Generates a set of random particles.
         *
         * @return      A set of random particles.
         */
        ArrayList<ParticleImplMCL> generateInitialParticleSet() {
            ArrayList<ParticleImplMCL> particles = new ArrayList<>();
            for (int i = 0  ;  i < numberOfParticles  ;  i++) {
                particles.add(createRandomParticle());
            }
            return particles;
        }


        /**
         * Creates one random particle within given limitations and map-bounds.
         *
         * @return              A random particle.
         */
        private ParticleImplMCL createRandomParticle() {
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
                    return new ParticleImplMCL(new Pose(x, y, h), map, 1, Color.GRAY);
                }
            }
        }
    }





    /**
     * Re-Evaluator for recalculation of the particle-weights for an entire particle-set.
     */
    private class ParticleSetEvaluator {
        /**
         * Recalculates the particle-weights based on the current sensor-model (sensor-feedback from the robot).
         *
         * @param bot   The sensor-model.
         */
        void recalculateParticleWeight(SensorModel bot) {
            ArrayList<ParticleImplMCL> particles = mclModel.getParticles();
            for (ParticleImplMCL p : particles) {
                if (p.isOutOfMapOperatingRange()) {
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
        private double calculateBotParticleDeviation(SensorModel bot, ParticleImplMCL particle) {
        // Factor the weight from uss scans get multiplied with, depending on how much the particles view deviates from the bots camera view. Between 1 and 3.
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



        /**
         * Calculates an absolute weight for a particle based on camera-data (deviation of angle and size between
         * camera- and particle-data).
         *
         * @param bot       The SensorModel to hold the robot-sensor-feedback (including camera-data).
         * @param particle  The particle to compare with the sensor-model.
         * @return          The absolute weight of the particle.
         */
        private double calculatedCameraSupportedDeviation(SensorModel bot, ParticleImplMCL particle) {
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
    }





    private class ParticleSetResampler {
        private LocalizationRecorder localizationRecorder;

        /**
         * Constructor
         *
         * @param localizationRecorder  An instance of LocalizationRecorder to record the resampling-step.
         */
        ParticleSetResampler(LocalizationRecorder localizationRecorder) {
            this.localizationRecorder = localizationRecorder;
        }

        /**
         * Resampling of the particles. Implementation of the ResamplingWheel.
         */
        private void resample() {
            normalizeParticleWeight();
            Random r = new Random();
            ArrayList<ParticleImplMCL> resampledParticles = new ArrayList<>();
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
                ParticleImplMCL clone = mclModel.getParticles().get(index).getClone();
                clone.setWeight(0);
                resampledParticles.add(clone);
            }
            mclModel.setParticles(resampledParticles);
            localizationRecorder.logInstruction("Resample");
            localizationRecorder.takeSnapShot();
            checkLocalizationStatus();
        }


        /**
         * Normalizes the weight of all particles.
         */
        private void normalizeParticleWeight() {
            double sum = getSumOfParticleWeights();
            for (ParticleImplMCL p : mclModel.getParticles()) {
                p.setWeight((float)(p.getWeight() / sum));
            }
        }


        /**
         * Checks whether all particles lie within the acceptable spreading and updates the data-model.
         */
        private void checkLocalizationStatus() {
            if (mclModel.isLocalized()) {
                return;
            }
            double[] pose = getEstimatedPose();
            Pose bPose = new Pose((float)pose[0], (float)pose[1], (float)pose[2]);
            for (ParticleImplMCL p : mclModel.getParticles()) {
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
         * Finds the highest weight among all particles.
         *
         * @return  The highest weight in the particle-set.
         */
        private float getHighestParticleWeight() {
            float weight = 0f;
            for (ParticleImplMCL p : mclModel.getParticles()) {
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
            for (ParticleImplMCL p : mclModel.getParticles()) {
                sum += p.getWeight();
            }
            return sum;
        }
    }





    private class BotToParticleMotionMapper {
        private ParticleSetResampler particleSetResampler;
        private LocalizationRecorder localizationRecorder;

        /**
         * Constructor.
         *
         * @param particleSetResampler  An instance of ParticleSetResample for resampling after motion
         * @param localizationRecorder  An instance of LocalizationRecorder to record the motion-step
         */
        BotToParticleMotionMapper(ParticleSetResampler particleSetResampler, LocalizationRecorder localizationRecorder) {
            this.particleSetResampler = particleSetResampler;
            this.localizationRecorder = localizationRecorder;
        }



        /**
         * Performs a translation of the particles over the given distance.
         *
         * @param distance  The distance to translate each particle.
         */
        void translateParticles(double distance) {
            particleSetResampler.resample();
            Random r = new Random();
            for (ParticleImplMCL p : mclModel.getParticles()) {
                float d = (float) r.nextGaussian();
                while (d < -1  ||  d > 1) {
                    d = (float) r.nextGaussian();
                }
                p.move(distance * (1 +(d/10)));
            }
            localizationRecorder.logInstruction("Move " + String.valueOf(distance));
            localizationRecorder.takeSnapShot();
        }



        /**
         * Turns the heading of the particles.
         *
         * @param degrees   The degrees to turn.
         */
        void turnParticles(double degrees){
            Random r = new Random();
            for (ParticleImplMCL p : mclModel.getParticles()) {
                double d = r.nextGaussian();
                degrees = mclModel.getUserSettings().isTwoDimensional() ? (int) Math.round(degrees * (1+(d/540))) : (int) Math.round(degrees);
                p.turn((int)degrees);
            }
            localizationRecorder.logInstruction("Turn " + String.valueOf(degrees));
            localizationRecorder.takeSnapShot();
        }
    }





    private class BotPoseEstimator {
        /**
         * Estimates the current robot-position by calculating the arithmetic means of the x- and
         * y-coordinates and the heading of the particles.
         *
         * @return  The estimated Pose of the robot as double-Array containing x, y and heading.
         */
        double[] getEstimatedPose() {
            double xSum = 0;
            double ySum = 0;
            double hSum = 0;
            for (ParticleImplMCL p : mclModel.getParticles()) {
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
        double getSpreadingAroundEstimatedBotPose() {
            double distance = 0;
            double[] pose = getEstimatedPose();
            Pose bPose = new Pose((float)pose[0], (float)pose[1], (float)pose[2]);
            for (ParticleImplMCL p : mclModel.getParticles()) {
                Pose pPose = p.getPose();
                double dx = pPose.getX()- bPose.getX();
                double dy = pPose.getY()-bPose.getY();
                double pDistance = Math.sqrt((Math.pow(dx, 2)) + (Math.pow(dy, 2)));
                distance = distance > pDistance ? distance : pDistance;
            }
            return distance;
        }
    }





    private class LocalizationRecorder {
        /**
         * Saves the (serialized) localization-sequence to a file using the current date and time as filename.
         */
        void saveLocalizationSequenceToFile() {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String filename = simpleDateFormat.format(calendar.getTime());
            String path = ClientFactory.getProperties().getProperty("savedLocalizationsPath");
            String file = path + String.valueOf(filename) + ".log";
            try (FileOutputStream fileOS = new FileOutputStream(file)) {
                ObjectOutputStream objectOS = new ObjectOutputStream(fileOS);
                objectOS.writeObject(mclModel.getWorldStateSequence());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        /**
         * Resets to the latest state of the ongoing localization.
         * Required for continuing the localization after an interim replay.
         */
        void resetToLatestWorldState() {
            mclModel.resetToLatestWorldState();
        }


        /**
         * Moves one step back in the localization-sequence.
         */
        void stepBackInLocalizationHistory() {
            mclModel.navigateBackwardInHistory();
        }


        /**
         * Moves one step forward in the localization-sequence.
         */
        void stepForwardInLocalizationHistory() {
            mclModel.navigateForwardInHistory();
        }


        /**
         * Logs the instruction (of type String) which lead to the current localization-state.
         *
         * @param instruction   The last action performed by the localization-provider
         */
        void logInstruction(String instruction) {
            mclModel.logInstruction(instruction);
        }


        /**
         * Make a snap-shot of the current localization-state. Used preliminary for saving a world-state.
         */
        void takeSnapShot() {
            mclModel.takeSnapShot();
        }
    }
}
