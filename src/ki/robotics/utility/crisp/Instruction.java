package ki.robotics.utility.crisp;


/**
 * Representation of an Instruction from the I_Robot-Instruction-Set.
 *
 * @version 1.0, 12/26/17
 */
public class Instruction {
    private final char instructionGroup;
    private final String mnemonic;



    /**
     * Constructor
     *
     * @param instructionGroup  Instruction Group (e.g Robot-, Sensor- or Camera-Instruction)
     * @param mnemonic          Mnemonic (4 Characters) of the Instruction.
     */
    public Instruction(char instructionGroup, String mnemonic) {
        this.instructionGroup = instructionGroup;
        this.mnemonic = mnemonic;
    }


    /**
     * Return the instruction-group to which this instruction belongs.
     *
     * @return  char identifying the instruction-group.
     */
    public char getInstructionGroup() { return this.instructionGroup; }


    /**
     * Returns the Mnemonic of the Instruction.
     *
     * @return  Returns the Mnemonic of the Instruction.
     */
    public String getMnemonic() {
        return this.mnemonic;
    }



    /**
     * Returns the String-Representation of the Instruction with its (optional) parameter.
     *
     * @return  Returns the String-Representation.
     */
    @Override
    public String toString() {
        return mnemonic;
    }


    /**
     * Specialized class for instructions with a single integer-parameter.
     */
    public static class SingleIntInstruction extends Instruction{
        private final int parameter;

        /**
         * Constructor
         *
         * @param instructionGroup  Instruction Group (e.g Robot-, Sensor- or Camera-Instruction)
         * @param mnemonic          Mnemonic (4 Characters) of the Instruction.
         * @param parameter         Parameter for the Instruction.
         */
        public SingleIntInstruction(char instructionGroup, String mnemonic, int parameter) {
            super(instructionGroup, mnemonic);
            this.parameter = parameter;
        }

        public int getParameter() {return parameter;}

        @Override
        public String toString() {return this.getMnemonic() + " " + parameter;}
    }




    /**
     * Specialized class for instructions with multiple integer-parameters.
     */
    public static class MultiIntInstruction extends Instruction{
        private final int[] parameters;

        /**
         * Constructor
         *
         * @param instructionGroup  Instruction Group (e.g Robot-, Sensor- or Camera-Instruction)
         * @param mnemonic          Mnemonic (4 Characters) of the Instruction.
         * @param parameters        Parameters for the Instruction.
         */
        public MultiIntInstruction(char instructionGroup, String mnemonic, int ... parameters) {
            super(instructionGroup, mnemonic);
            this.parameters = parameters;
        }

        public int[] getParameters() { return this.parameters; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(this.getMnemonic());
            for (int i : parameters) {
                sb.append(" ").append(i);
            }
            return sb.toString();
        }
    }




    /**
     * Specialized class for instructions with a single floating-point-parameter.
     */
    public static class SingleFloatInstruction extends Instruction{
        private final double parameter;

        /**
         * Constructor
         *
         * @param instructionGroup  Instruction Group (e.g Robot-, Sensor- or Camera-Instruction)
         * @param mnemonic          Mnemonic (4 Characters) of the Instruction.
         * @param parameter         Parameter for the Instruction.
         */
        public SingleFloatInstruction(char instructionGroup, String mnemonic, double parameter) {
            super(instructionGroup, mnemonic);
            this.parameter = parameter;
        }

        public double getParameter() { return this.parameter; }

        @Override
        public String toString() { return this.getMnemonic() + " " + parameter; }
    }




    /**
     * Specialized class for instructions with multiple floating-point-parameters.
     */
    public static class MultiFloatInstruction extends Instruction{
        private final double[] parameters;

        /**
         * Constructor
         *
         * @param instructionGroup  Instruction Group (e.g Robot-, Sensor- or Camera-Instruction)
         * @param mnemonic          Mnemonic (4 Characters) of the Instruction.
         * @param parameters        Parameters for the Instruction.
         */
        public MultiFloatInstruction(char instructionGroup, String mnemonic, double ... parameters) {
            super(instructionGroup, mnemonic);
            this.parameters = parameters;
        }

        public double[] getParameters() { return this.parameters; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(this.getMnemonic());
            for (double d: parameters) {
                sb.append(" ").append(d);
            }
            return sb.toString();
        }
    }
}
