package ki.robotics.utility.crisp;

import static ki.robotics.utility.crisp.CRISP.*;
import java.util.ArrayList;


/**
 * Decoder for Instructions from a client-request.
 *
 * @version 1.0 12/26/17
 */
public class InstructionSetTranscoder {
    /**
     * Decodes the instructions from an instruction-sequence-string into a list of Instructions.
     *
     * @param instructionSequence   a string containing one or more instructions.
     * @return                      a List of Instructions-instances.
     */
    public ArrayList<Instruction> decodeRequest(String instructionSequence) {
        ArrayList<Instruction> instructions = new ArrayList<>();
        String[] sequence = instructionSequence.split(",");
        for (String s : sequence) {
            instructions.add(decodeInstruction(s));
        }
        return instructions;
    }



    /**
     * Decodes a single Instruction from its string-representation.
     *
     * @param instruction   a string containing one instruction and its parameter.
     * @return              the instruction and parameter as Instruction-object.
     */
    public Instruction decodeInstruction(String instruction) {
        String trimmedInstruction = instruction.trim();
        char instructionGroup = trimmedInstruction.charAt(0);
        String mnemonic = trimmedInstruction.substring(0,4);
        String parameters = trimmedInstruction.substring(4);

        switch (instructionGroup) {
            case BOT_INSTRUCTION:
                return decodeBotInstruction(mnemonic, parameters);
            case SENSOR_INSTRUCTION:
                return decodeSensorInstruction(mnemonic, parameters);
            case CAMERA_INSTRUCTION:
                return decodeCameraInstruction(mnemonic, parameters);
            default:
                return decodeOtherInstruction(mnemonic, parameters);
        }
    }


    /**
     * Decodes instructions regarding the position and movement of the robot.
     *
     * @param mnemonic      The mnemonic representation of the instruction.
     * @param paramString   A String with parameters.
     * @return              The instruction as an Instruction-object.
     */
    private Instruction decodeBotInstruction(String mnemonic, String paramString) {
        switch (mnemonic) {
            case BOT_TRAVEL_FORWARD:
            case BOT_TRAVEL_BACKWARD:
            case BOT_TURN_LEFT:
            case BOT_TURN_RIGHT:
            case BOT_POSE_X:
            case BOT_POSE_Y:
            case BOT_POSE_HEADING:
                double parameter = getSingleFloatParameter(paramString);
                return new Instruction.SingleFloatInstruction(BOT_INSTRUCTION, mnemonic, parameter);
            case BOT_RETURN_POSE:
                return new Instruction(BOT_INSTRUCTION, BOT_RETURN_POSE);
            case BOT_LINE_FOLLOWING_ENABLED:
            case BOT_LINE_FOLLOWING_DISABLED:
                return new Instruction(BOT_INSTRUCTION, mnemonic);
            default:
                return new Instruction(OTHER_INSTRUCTION, UNSUPPORTED_INSTRUCTION);
        }
    }


    /**
     * Decodes instructions regarding the sensors and the sensor-head.
     *
     * @param mnemonic      The mnemonic representation of the instruction.
     * @param paramString   A String with parameters.
     * @return              The instruction as an Instruction-object.
     */
    private Instruction decodeSensorInstruction(String mnemonic, String paramString) {
        switch (mnemonic) {
            case SENSOR_TURN_LEFT:
            case SENSOR_TURN_RIGHT:
                int angle = getSingleIntParameter(paramString);
                angle = (angle == 0) ? 90 : angle;
                return new Instruction.SingleIntInstruction(SENSOR_INSTRUCTION, mnemonic, angle);
            case SENSOR_RESET:
                return new Instruction(SENSOR_INSTRUCTION, mnemonic);
            case SENSOR_MEASURE_COLOR:
            case SENSOR_THREE_WAY_SCAN:
                int intParam = getSingleIntParameter(paramString);
                return new Instruction.SingleIntInstruction(SENSOR_INSTRUCTION, mnemonic, intParam);
            case SENSOR_SINGLE_DISTANCE_SCAN:
            case THREE_WAY_SCAN_LEFT:
            case THREE_WAY_SCAN_CENTER:
            case THREE_WAY_SCAN_RIGHT:
                double doubleParam = getSingleFloatParameter(paramString);
                return new Instruction.SingleFloatInstruction(SENSOR_INSTRUCTION, mnemonic, doubleParam);
            default:
                return new Instruction(OTHER_INSTRUCTION, UNSUPPORTED_INSTRUCTION);
        }
    }


    /**
     * Decodes instructions regarding the camera.
     *
     * @param mnemonic      The mnemonic representation of the instruction.
     * @param paramString   A String with parameters.
     * @return              The instruction as an Instruction-object.
     */
    private Instruction decodeCameraInstruction(String mnemonic, String paramString) {
        switch (mnemonic) {
            case CAMERA_ALL_SIGNATURES_QUERY:
                return new Instruction(CAMERA_INSTRUCTION, mnemonic);
            case CAMERA_ANGLE_QUERY:
                int parameter = getSingleIntParameter(paramString);
                return new Instruction.SingleIntInstruction(CAMERA_INSTRUCTION, mnemonic, parameter);
            case CAMERA_GENERAL_QUERY:
            case CAMERA_SINGLE_SIGNATURE_QUERY:
            case CAMERA_COLOR_CODE_QUERY:
            case CAMERA_SIGNATURE_1:
            case CAMERA_SIGNATURE_2:
            case CAMERA_SIGNATURE_3:
            case CAMERA_SIGNATURE_4:
            case CAMERA_SIGNATURE_5:
            case CAMERA_SIGNATURE_6:
            case CAMERA_SIGNATURE_7:
                int[] parameters = getMultiIntParameter(paramString);
                return new Instruction.MultiIntInstruction(CAMERA_INSTRUCTION, mnemonic, parameters);
            default:
                return new Instruction(OTHER_INSTRUCTION, UNSUPPORTED_INSTRUCTION);
        }
    }


    /**
     * Decodes all other instructions.
     *
     * @param mnemonic      The mnemonic representation of the instruction.
     * @param paramString   A String with parameters.
     * @return              The instruction as an Instruction-object.
     */
    private Instruction decodeOtherInstruction(String mnemonic, String paramString) {
        return new Instruction(OTHER_INSTRUCTION, mnemonic);
    }


    /**
     * Decodes a single integer-value from a parameter-string.
     *
     * @param parameter A String with one integer.
     * @return          The integer from the String.
     */
    private int getSingleIntParameter(String parameter) {
        try {
            parameter = parameter.trim();
            return Integer.parseInt(parameter);
        } catch (Exception e) {
            return 0;
        }
    }


    /**
     * Decodes multiple integer-values from a parameter-string.
     *
     * @param parameter A String with multiple integer.
     * @return          The integers (as int-array) from the String.
     */
    private int[] getMultiIntParameter(String parameter) {
        try {
            parameter = parameter.trim();
            String paramStrings[] = parameter.split(" ");
            int param[] = new int[paramStrings.length];
            for (int i = 0  ;  i < paramStrings.length  ;  i++) {
                param[i] = Integer.parseInt(paramStrings[i]);
            }
            return param;
        } catch (Exception e) {
            return new int[]{0};
        }
    }


    /**
     * Decodes a single floating-point-value from a parameter-string.
     *
     * @param parameter A String with one floating-point-number.
     * @return          The floating-point-number from the String.
     */
    private double getSingleFloatParameter(String parameter) {
        try {
            parameter = parameter.trim();
            return Double.parseDouble(parameter);
        } catch (Exception e) {
            return 0;
        }
    }


    /**
     * Decodes multiple floating-point-values from a parameter-string.
     *
     * @param parameter A String with multiple floating-point-numbers.
     * @return          The floating-point-numbers (as double-array) from the String.
     */
    private double[] getMultiFloatParameter(String parameter) {
        try {
            parameter = parameter.trim();
            String paramStrings [] = parameter.split(" ");
            double param[] = new double[paramStrings.length];
            for (int i = 0  ;  i < paramStrings.length  ;  i++) {
                param[i] = Double.parseDouble(paramStrings[i]);
            }
            return param;
        } catch (Exception e) {
            return new double[]{0};
        }
    }
}
