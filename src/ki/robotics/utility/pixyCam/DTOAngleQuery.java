package ki.robotics.utility.pixyCam;

public class DTOAngleQuery {
    private int angleOfLargestColorCodedBlock;

    public DTOAngleQuery(byte[] camResponse) {
        angleOfLargestColorCodedBlock = camResponse[0];
    }

    public DTOAngleQuery(String botTransmission) {
        angleOfLargestColorCodedBlock = Integer.parseInt(botTransmission.trim());
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
