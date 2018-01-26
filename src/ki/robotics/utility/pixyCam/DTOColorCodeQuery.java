package ki.robotics.utility.pixyCam;

public class DTOColorCodeQuery {
    private final int numberOfMatchingBlocks;
    private final int xCenterOfLargestBlock;
    private final int yCenterOfLargestBlock;
    private final int widthOfLargestBlock;
    private final int heightOfLargestBlock;
    private final int angleOfLargestBlock;

    public DTOColorCodeQuery(byte[] camResponse) {
        numberOfMatchingBlocks = camResponse[0];
        xCenterOfLargestBlock = camResponse[1];
        yCenterOfLargestBlock = camResponse[2];
        widthOfLargestBlock = camResponse[3];
        heightOfLargestBlock = camResponse[4];
        angleOfLargestBlock = camResponse[5];
    }

    public DTOColorCodeQuery(String botTransmission) {
        String[] values = botTransmission.trim().split(" ");
        numberOfMatchingBlocks = Integer.parseInt(values[0]);
        xCenterOfLargestBlock = Integer.parseInt(values[1]);
        yCenterOfLargestBlock = Integer.parseInt(values[2]);
        widthOfLargestBlock = Integer.parseInt(values[3]);
        heightOfLargestBlock = Integer.parseInt(values[4]);
        angleOfLargestBlock = Integer.parseInt(values[5]);
    }


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

    @Override
    public String toString() {
        String query = "";
        query += numberOfMatchingBlocks + " ";
        query += xCenterOfLargestBlock + " ";
        query += yCenterOfLargestBlock + " ";
        query += widthOfLargestBlock + " ";
        query += heightOfLargestBlock + " ";
        query += angleOfLargestBlock;
        return query;
    }
}
