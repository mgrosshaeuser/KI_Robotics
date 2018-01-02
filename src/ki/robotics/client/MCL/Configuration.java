package ki.robotics.client.MCL;

import ki.robotics.utility.crisp.CRISP;
import ki.robotics.utility.crisp.Instruction;

import java.awt.image.CropImageFilter;
import java.util.ArrayList;

/**
 * Represents the user-selections from the client-ui.
 *
 * @version 1.0 01/02/18
 */
public class Configuration {
    private String mapKey;
    private boolean isOneDimensional;
    private boolean isTwoDimensional;
    private boolean useRightAngles;
    private boolean useFreeAngles;
    private boolean useLeftSensor;
    private boolean useFrontSensor;
    private boolean useRightSensor;
    private int stepsize;
    private int numberOfParticles;


    /**
     * Constructor
     *
     * @param mapKey                A key, identifying a map provided by the MapProvider
     * @param isOneDimensional      Marks the operating range as one-dimensional.
     * @param isTwoDimensional      Marks the operating range as two-dimensional.
     * @param useRightAngles        Bot can only change direction in 90°-steps.
     * @param useFreeAngles         Bot can change direction in 1°-steps.
     * @param useLeftSensor         Distance-Measurement to the left is desired.
     * @param useFrontSensor        Distance-Measurement to the front is desired.
     * @param useRightSensor        Distance-Measurement to the right is desired.
     * @param stepsize              Distance to robot moves with every travel-instruction.
     * @param numberOfParticles     Number of particles for the monte-carlo-localization.
     */
    public Configuration(String mapKey, boolean isOneDimensional, boolean isTwoDimensional, boolean useRightAngles,
                         boolean useFreeAngles, boolean useLeftSensor, boolean useFrontSensor,
                         boolean useRightSensor, int stepsize, int numberOfParticles) {
        this.mapKey = mapKey;
        this.isOneDimensional = isOneDimensional;
        this.isTwoDimensional = isTwoDimensional;
        this.useRightAngles = useRightAngles;
        this.useFreeAngles = useFreeAngles;
        this.useLeftSensor = useLeftSensor;
        this.useFrontSensor = useFrontSensor;
        this.useRightSensor = useRightSensor;
        this.stepsize = stepsize;
        this.numberOfParticles = numberOfParticles;
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
            instr.add(CRISP.THREE_WAY_SCAN);
        } else {
            if (useLeftSensor) {
                instr.add(CRISP.SENSOR_TURN_LEFT + " 90, " + CRISP.MEASURE_DISTANCE);
            }
            if (useFrontSensor) {
                instr.add(CRISP.SENSOR_RESET + ", " + CRISP.MEASURE_DISTANCE);
            }
            if (useRightSensor) {
                instr.add(CRISP.SENSOR_TURN_RIGHT + " 90, " + CRISP.MEASURE_DISTANCE);
            }
        }
        return instr;
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

    public int getStepsize() {
        return stepsize;
    }

    public int getNumberOfParticles() {
        return numberOfParticles;
    }
}
