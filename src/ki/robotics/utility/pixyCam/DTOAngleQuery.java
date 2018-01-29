package ki.robotics.utility.pixyCam;

public class DTOAngleQuery {
    private final int angleOfLargestColorCodedBlock;

    public DTOAngleQuery(byte[] camResponse) {
        angleOfLargestColorCodedBlock = camResponse[0];
    }

    public DTOAngleQuery(String botRawTransmission) {
        angleOfLargestColorCodedBlock = Integer.parseInt(botRawTransmission.trim());
    }

    public DTOAngleQuery(int botTransmission) {
        this.angleOfLargestColorCodedBlock = botTransmission;
    }

    public int getAngleOfLargestColorCodedBlock() {
        return angleOfLargestColorCodedBlock;
    }

    @Override
    public String toString() {
        String query = "";
        query += angleOfLargestColorCodedBlock;
        return query;
    }
}
