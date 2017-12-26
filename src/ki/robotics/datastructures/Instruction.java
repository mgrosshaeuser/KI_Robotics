package ki.robotics.datastructures;


/**
 * Representation of an Instruction from the Robot-Instruction-Set.
 *
 * @version 1.0, 12/26/17
 */

public class Instruction {
    private String mnemonic;
    private double parameter;



    /**
     * Constructor
     *
     * @param mnemonic      Mnemonic (4 Characters) of the Instruction.
     * @param parameter     (Optional) Parameter for the Instruction.
     */
    public Instruction(String mnemonic, double parameter) {
        this.mnemonic = mnemonic;
        this.parameter = parameter;
    }



    /**
     * Returns the Menomic of the Instruction.
     *
     * @return  Returns the Mnemonic of the Instruction.
     */
    public String getMnemonic() {
        return this.mnemonic;
    }



    /**
     * Returns the (optional) Parameter of the Instruction.
     *
     * @return  The Parameter of the Instruction.
     */
    public double getParameter() {
        return this.parameter;
    }



    /**
     * Returns the String-Representation of the Instruction with its (optional) parameter.
     *
     * @return  Returns the String-Representation.
     */
    @Override
    public String toString() {
        return mnemonic + " " + parameter + "\n";
    }
}
