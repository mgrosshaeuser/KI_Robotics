package ki.robotics.utility.pixyCam;

public class DTOColorCodeQuery {
    private final int colorCode;
    private final int numberOfMatchingBlocks;
    private final int xCenterOfLargestBlock;
    private final int yCenterOfLargestBlock;
    private final int widthOfLargestBlock;
    private final int heightOfLargestBlock;
    private final int angleOfLargestBlock;

    public DTOColorCodeQuery(byte[] camResponse) {
        colorCode = 0;
        numberOfMatchingBlocks = camResponse[0];
        xCenterOfLargestBlock = camResponse[1];
        yCenterOfLargestBlock = camResponse[2];
        widthOfLargestBlock = camResponse[3];
        heightOfLargestBlock = camResponse[4];
        angleOfLargestBlock = camResponse[5];
    }

    public DTOColorCodeQuery(String botRawTransmission) {
        String[] values = botRawTransmission.trim().split(" ");
        colorCode = Integer.parseInt(values[0]);
        numberOfMatchingBlocks = Integer.parseInt(values[1]);
        xCenterOfLargestBlock = Integer.parseInt(values[2]);
        yCenterOfLargestBlock = Integer.parseInt(values[3]);
        widthOfLargestBlock = Integer.parseInt(values[4]);
        heightOfLargestBlock = Integer.parseInt(values[5]);
        angleOfLargestBlock = Integer.parseInt(values[6]);
    }

    public DTOColorCodeQuery(int[] botTransmission) {
        colorCode = botTransmission[0];
        numberOfMatchingBlocks = botTransmission[1];
        xCenterOfLargestBlock = botTransmission[2];
        yCenterOfLargestBlock = botTransmission[3];
        widthOfLargestBlock = botTransmission[4];
        heightOfLargestBlock = botTransmission[5];
        angleOfLargestBlock = botTransmission[6];
    }

    public int getColorCode() { return colorCode; }

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

    public int getAngleOfLargestBlock() {
        return angleOfLargestBlock;
    }

    public int[] getAllParameters() {
        return new int[] {
                colorCode,
                numberOfMatchingBlocks,
                xCenterOfLargestBlock,
                yCenterOfLargestBlock,
                widthOfLargestBlock,
                heightOfLargestBlock,
                angleOfLargestBlock
        };
    }

    @Override
    public String toString() {
        String query = "";
        query += colorCode + " ";
        query += numberOfMatchingBlocks + " ";
        query += xCenterOfLargestBlock + " ";
        query += yCenterOfLargestBlock + " ";
        query += widthOfLargestBlock + " ";
        query += heightOfLargestBlock + " ";
        query += angleOfLargestBlock;
        return query;
    }
}
