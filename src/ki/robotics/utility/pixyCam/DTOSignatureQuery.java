package ki.robotics.utility.pixyCam;

public class DTOSignatureQuery {
    private static final int SIGNATURE = 0;
    private static final int NUMBER_OF_MATCHING_BLOCKS = 1;
    private static final int X_CENTER_OF_LARGEST_BLOCK = 2;
    private static final int Y_CENTER_OF_LARGEST_BLOCK = 3;
    private static final int WIDTH_OF_LARGEST_BLOCK = 4;
    private static final int HEIGHT_OF_LARGEST_BLOCK = 5;

    private final int signature;
    private final int numberOfMatchingBlocks;
    private final int xCenterOfLargestBlock;
    private final int yCenterOfLargestBlock;
    private final int widthOfLargestBlock;
    private final int heightOfLargestBlock;


    public DTOSignatureQuery(byte[] camResponse) {
        signature = camResponse[SIGNATURE];
        numberOfMatchingBlocks = camResponse[NUMBER_OF_MATCHING_BLOCKS];
        xCenterOfLargestBlock = camResponse[X_CENTER_OF_LARGEST_BLOCK];
        yCenterOfLargestBlock = camResponse[Y_CENTER_OF_LARGEST_BLOCK];
        widthOfLargestBlock = camResponse[WIDTH_OF_LARGEST_BLOCK];
        heightOfLargestBlock = camResponse[HEIGHT_OF_LARGEST_BLOCK];
    }

    public DTOSignatureQuery(String botRawTransmission) {
        String[] values = botRawTransmission.trim().split(" ");
        signature = Integer.parseInt(values[SIGNATURE]);
        numberOfMatchingBlocks = Integer.parseInt(values[NUMBER_OF_MATCHING_BLOCKS]);
        xCenterOfLargestBlock = Integer.parseInt(values[X_CENTER_OF_LARGEST_BLOCK]);
        yCenterOfLargestBlock = Integer.parseInt(values[Y_CENTER_OF_LARGEST_BLOCK]);
        widthOfLargestBlock = Integer.parseInt(values[WIDTH_OF_LARGEST_BLOCK]);
        heightOfLargestBlock = Integer.parseInt(values[HEIGHT_OF_LARGEST_BLOCK]);
    }

    public DTOSignatureQuery(int[] botTransmission) {
        signature = botTransmission[SIGNATURE];
        numberOfMatchingBlocks = botTransmission[NUMBER_OF_MATCHING_BLOCKS];
        xCenterOfLargestBlock = botTransmission[X_CENTER_OF_LARGEST_BLOCK];
        yCenterOfLargestBlock = botTransmission[Y_CENTER_OF_LARGEST_BLOCK];
        widthOfLargestBlock = botTransmission[WIDTH_OF_LARGEST_BLOCK];
        heightOfLargestBlock = botTransmission[HEIGHT_OF_LARGEST_BLOCK];
    }

    public DTOSignatureQuery(Object[] botTransmission) {
        signature = (int)botTransmission[SIGNATURE];
        numberOfMatchingBlocks = (int)botTransmission[NUMBER_OF_MATCHING_BLOCKS];
        xCenterOfLargestBlock = (int)botTransmission[X_CENTER_OF_LARGEST_BLOCK];
        yCenterOfLargestBlock = (int)botTransmission[Y_CENTER_OF_LARGEST_BLOCK];
        widthOfLargestBlock = (int)botTransmission[WIDTH_OF_LARGEST_BLOCK];
        heightOfLargestBlock = (int)botTransmission[HEIGHT_OF_LARGEST_BLOCK];
    }

    public int getSignature() { return signature; }

    public int getNumberOfMatchingBlocks() {
        return numberOfMatchingBlocks;
    }

    public int getxCenterOfLargestBlock() {
        return xCenterOfLargestBlock;
    }

    public int getyCenterOfLargestBlock() {
        return yCenterOfLargestBlock;
    }

    public int getWidthOfLargestBlock() {
        return widthOfLargestBlock;
    }

    public int getHeightOfLargestBlock() {
        return heightOfLargestBlock;
    }


    public int[] getAllParameters() {
        return new int[] {
                signature,
                numberOfMatchingBlocks,
                xCenterOfLargestBlock,
                yCenterOfLargestBlock,
                widthOfLargestBlock,
                heightOfLargestBlock
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(signature).append(" ");
        sb.append(numberOfMatchingBlocks).append(" ");
        sb.append(xCenterOfLargestBlock).append(" ");
        sb.append(yCenterOfLargestBlock).append(" ");
        sb.append(widthOfLargestBlock).append(" ");
        sb.append(heightOfLargestBlock);
        return sb.toString();
    }
}
