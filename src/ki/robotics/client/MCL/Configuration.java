package ki.robotics.client.MCL;

import ki.robotics.utility.crisp.CRISP;
import ki.robotics.utility.map.MapProvider;

import java.util.ArrayList;

/**
 * Represents the user-selections from the client-ui.
 *
 * @version 1.0 01/02/18
 */
public abstract class Configuration {
    private final String mapKey;
    private final boolean isOneDimensional;
    private final boolean isTwoDimensional;
    private final boolean isWithCamera;
    private final int stepSize;
    private final int numberOfParticles;


    /**
     * Constructor
     *
     * @param mapKey                A key, identifying a map provided by the MapProvider
     * @param isOneDimensional      Marks the operating range as one-dimensional.
     * @param isTwoDimensional      Marks the operating range as two-dimensional.
     * @param stepSize              Distance to robot moves with every travel-instruction.
     * @param numberOfParticles     Number of particles for the monte-carlo-localization.
     */
    public Configuration(String mapKey, boolean isOneDimensional, boolean isTwoDimensional,
                         boolean isWithCamera, int stepSize, int numberOfParticles) {
        this.mapKey = mapKey;
        this.isOneDimensional = isOneDimensional;
        this.isTwoDimensional = isTwoDimensional;
        this.isWithCamera = isWithCamera;
        this.stepSize = stepSize;
        this.numberOfParticles = numberOfParticles;
    }

    public String getMapKey() {
        return mapKey;
    }

    public boolean isOneDimensional() {
        return isOneDimensional;
    }

    public boolean isTwoDimensional() {
        return isTwoDimensional;
    }

    public boolean isWithCamera() { return  isWithCamera; }

    public int getStepSize() {
        return stepSize;
    }

    public int getNumberOfParticles() {
        return numberOfParticles;
    }

    public abstract ArrayList<String> getSensingInstructions();




    public static class ConfigOneD extends Configuration{
        public static final ConfigOneD DEFAULT = new ConfigOneD(
                MapProvider.MAP_KEY_HOUSES,
                true,
                false,
                false,
                10,
                1000,
                true
        );

        private final boolean startFromLeft;
        private boolean measureDistanceToLeft;

        /**
         * Constructor
         *
         * @param mapKey            A key, identifying a map provided by the MapProvider
         * @param isOneDimensional  Marks the operating range as one-dimensional.
         * @param isTwoDimensional  Marks the operating range as two-dimensional.
         * @param stepsize          Distance to robot moves with every travel-instruction.
         * @param numberOfParticles Number of particles for the monte-carlo-localization.
         * @param startFromLeft     Bot starts from the left (true) or from the right (false)
         */
        public ConfigOneD(String mapKey, boolean isOneDimensional, boolean isTwoDimensional, boolean isWithCamera,
                          int stepsize, int numberOfParticles, boolean startFromLeft) {
            super(mapKey, isOneDimensional, isTwoDimensional, isWithCamera, stepsize, numberOfParticles);
            this.startFromLeft = startFromLeft;
            this.measureDistanceToLeft = startFromLeft;
        }

        public void flipDirection() {
            this.measureDistanceToLeft = !measureDistanceToLeft;
        }

        public boolean isStartFromLeft() {
            return startFromLeft;
        }

        public boolean isStartFromRight() {
            return !startFromLeft;
        }

        @Override
        public ArrayList<String> getSensingInstructions() {
            ArrayList<String> instr = new ArrayList<>();
            if (measureDistanceToLeft) {
                instr.add(CRISP.SENSOR_TURN_LEFT + " 90, " + CRISP.SENSOR_SINGLE_DISTANCE_SCAN);
            } else {
                instr.add(CRISP.SENSOR_TURN_RIGHT + " 90, " + CRISP.SENSOR_SINGLE_DISTANCE_SCAN);
            }
            return instr;
        }
    }





    public static class ConfigTwoD extends Configuration{
        public static final ConfigTwoD DEFAULT = new ConfigTwoD(
                MapProvider.MAP_KEY_ROOM,
                false,
                true,
                false,
                10,
                1000,
                true,
                false,
                true,
                true,
                true
        );

        private final boolean useRightAngles;
        private final boolean useFreeAngles;
        private final boolean useLeftSensor;
        private final boolean useFrontSensor;
        private final boolean useRightSensor;

        /**
         * Constructor
         *
         * @param mapKey            A key, identifying a map provided by the MapProvider
         * @param isOneDimensional  Marks the operating range as one-dimensional.
         * @param isTwoDimensional  Marks the operating range as two-dimensional.
         * @param stepsize          Distance to robot moves with every travel-instruction.
         * @param numberOfParticles Number of particles for the monte-carlo-localization.
         */
        public ConfigTwoD(String mapKey, boolean isOneDimensional, boolean isTwoDimensional, boolean isWithCamera,
                          int stepsize, int numberOfParticles,  boolean useRightAngles, boolean useFreeAngles,
                          boolean useLeftSensor, boolean useFrontSensor, boolean useRightSensor) {
            super(mapKey, isOneDimensional, isTwoDimensional, isWithCamera, stepsize, numberOfParticles);
            this.useRightAngles = useRightAngles;
            this.useFreeAngles = useFreeAngles;
            this.useLeftSensor = useLeftSensor;
            this.useFrontSensor = useFrontSensor;
            this.useRightSensor = useRightSensor;
        }

        public boolean isUseRightAngles() {
            return useRightAngles;
        }

        public boolean isUseFreeAngles() {
            return useFreeAngles;
        }

        public boolean isUseLeftSensor() {
            return useLeftSensor;
        }

        public boolean isUseFrontSensor() {
            return useFrontSensor;
        }

        public boolean isUseRightSensor() {
            return useRightSensor;
        }


        /**
         * Returns a List of the sensor reading that have to be performed by the robot, according to the specifications
         * made in the constructor.
         *
         * @return  List of sensor-related instructions to be executed.
         */
        public ArrayList<String> getSensingInstructions() {
            ArrayList<String> instr = new ArrayList<>();
            if (useRightSensor && useFrontSensor && useLeftSensor) {
                instr.add(CRISP.SENSOR_THREE_WAY_SCAN);
            } else {
                if (useLeftSensor)  { instr.add(CRISP.SENSOR_TURN_LEFT + " 90, " + CRISP.SENSOR_SINGLE_DISTANCE_SCAN);  }
                if (useFrontSensor) { instr.add(CRISP.SENSOR_RESET + ", " + CRISP.SENSOR_SINGLE_DISTANCE_SCAN);         }
                if (useRightSensor) { instr.add(CRISP.SENSOR_TURN_RIGHT + " 90, " + CRISP.SENSOR_SINGLE_DISTANCE_SCAN); }
            }
            return instr;
        }
    }






    public static class ConfigCamera extends Configuration {
        public static final ConfigCamera DEFAULT = new ConfigCamera(
                MapProvider.MAP_KEY_MARKED_ROOM,
                false,
                true,
                true,
                10,
                1000,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                false,
                false
        );

        private final boolean useGeneralQuery;
        private final boolean useAngleQuery;
        private final boolean useSignatureOne;
        private final boolean useSignatureTwo;
        private final boolean useSignatureThree;
        private final boolean useSignatureFour;
        private final boolean useSignatureFive;
        private final boolean useSignatureSix;
        private final boolean useSignatureSeven;

        /**
         * Constructor
         *
         * @param mapKey            A key, identifying a map provided by the MapProvider
         * @param isOneDimensional  Marks the operating range as one-dimensional.
         * @param isTwoDimensional  Marks the operating range as two-dimensional.
         * @param stepSize          Distance to robot moves with every travel-instruction.
         * @param numberOfParticles Number of particles for the monte-carlo-localization.
         */
        public ConfigCamera(String mapKey, boolean isOneDimensional, boolean isTwoDimensional, boolean isWithCamera,
                            int stepSize, int numberOfParticles, boolean useGeneralQuery, boolean useAngleQuery,
                            boolean useSignatureOne, boolean useSignatureTwo, boolean useSignatureThree,
                            boolean useSignatureFour, boolean useSignatureFive, boolean useSignatureSix,
                            boolean useSignatureSeven) {
            super(mapKey, isOneDimensional, isTwoDimensional, isWithCamera, stepSize, numberOfParticles);
            this.useGeneralQuery = useGeneralQuery;
            this.useAngleQuery = useAngleQuery;
            this.useSignatureOne = useSignatureOne;
            this.useSignatureTwo = useSignatureTwo;
            this.useSignatureThree = useSignatureThree;
            this.useSignatureFour = useSignatureFour;
            this.useSignatureFive = useSignatureFive;
            this.useSignatureSix = useSignatureSix;
            this.useSignatureSeven = useSignatureSeven;
        }

        @Override
        public ArrayList<String> getSensingInstructions() {
            ArrayList<String> instructions = new ArrayList<>();
            if (useGeneralQuery) { instructions.add(CRISP.CAMERA_GENERAL_QUERY);}
            if (useGeneralQuery  &&  useAngleQuery) { instructions.add(CRISP.CAMERA_ANGLE_QUERY); }
            if (useSignatureOne) {instructions.add(CRISP.CAMERA_SINGLE_SIGNATURE_QUERY + " 1");}
            if (useSignatureTwo) {instructions.add(CRISP.CAMERA_SINGLE_SIGNATURE_QUERY + " 2");}
            if (useSignatureThree) {instructions.add(CRISP.CAMERA_SINGLE_SIGNATURE_QUERY + " 3");}
            if (useSignatureFour) {instructions.add(CRISP.CAMERA_SINGLE_SIGNATURE_QUERY + " 4");}
            if (useSignatureFive) {instructions.add(CRISP.CAMERA_SINGLE_SIGNATURE_QUERY + " 5");}
            if (useSignatureSix) {instructions.add(CRISP.CAMERA_SINGLE_SIGNATURE_QUERY + " 6");}
            if (useSignatureSeven) {instructions.add(CRISP.CAMERA_SINGLE_SIGNATURE_QUERY + " 7");}
            return instructions;
        }

        public boolean isUseGeneralQuery() {
            return useGeneralQuery;
        }

        public boolean isUseAngleQuery() {
            return useAngleQuery;
        }

        public boolean isUseSignatureOne() {
            return useSignatureOne;
        }

        public boolean isUseSignatureTwo() {
            return useSignatureTwo;
        }

        public boolean isUseSignatureThree() {
            return useSignatureThree;
        }

        public boolean isUseSignatureFour() {
            return useSignatureFour;
        }

        public boolean isUseSignatureFive() {
            return useSignatureFive;
        }

        public boolean isUseSignatureSix() {
            return useSignatureSix;
        }

        public boolean isUseSignatureSeven() {
            return useSignatureSeven;
        }
    }
}
