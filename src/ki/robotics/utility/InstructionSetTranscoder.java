package ki.robotics.utility;

import ki.robotics.datastructures.Instruction;

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
    private Instruction decodeInstruction(String instruction) {
        String trimmedInstruction = instruction.trim();
        String mnemonic = trimmedInstruction.substring(0,4);
        double argument = 0;
        if (trimmedInstruction.length() > 4) {
            try {
                argument = Double.parseDouble(trimmedInstruction.substring(4).trim());
            } catch (NumberFormatException e) {
                argument = 0;
            }
        }
        return new Instruction(mnemonic, argument);
    }
}
