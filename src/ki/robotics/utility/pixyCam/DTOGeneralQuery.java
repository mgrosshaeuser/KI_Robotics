package ki.robotics.utility.pixyCam;

public class DTOGeneralQuery {
    private static final int ONE_BYTE = 8;

    private final int signatureOfLargestBlock;
    private final int xCenterOfLargestBlock;
    private final int yCenterOfLargestBlock;
    private final int widthOfLargestBlock;
    private final int heightOfLargestBlock;

    public DTOGeneralQuery(byte[] camResponse) {
        signatureOfLargestBlock = camResponse[0] + (camResponse[1] << ONE_BYTE );
        xCenterOfLargestBlock = camResponse[2];
        yCenterOfLargestBlock = camResponse[3];
        widthOfLargestBlock = camResponse[4];
        heightOfLargestBlock = camResponse[5];
    }

    public DTOGeneralQuery(String botTransmission) {
        String[] values = botTransmission.trim().split(" ");
        signatureOfLargestBlock = Integer.parseInt(values[0]);
        xCenterOfLargestBlock = Integer.parseInt(values[1]);
        yCenterOfLargestBlock = Integer.parseInt(values[2]);
        widthOfLargestBlock = Integer.parseInt(values[3]);
        heightOfLargestBlock = Integer.parseInt(values[4]);
    }



    public int getSignatureOfLargestBlock() {
        return signatureOfLargestBlock;
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
                signatureOfLargestBlock,
                xCenterOfLargestBlock,
                yCenterOfLargestBlock,
                widthOfLargestBlock,
                heightOfLargestBlock
        };
    }


    @Override
    public String toString() {
        String query = "";
        query += signatureOfLargestBlock + " ";
        query += xCenterOfLargestBlock + " ";
        query += yCenterOfLargestBlock + " ";
        query += widthOfLargestBlock + " ";
        query += heightOfLargestBlock;
        return query;
    }
}
