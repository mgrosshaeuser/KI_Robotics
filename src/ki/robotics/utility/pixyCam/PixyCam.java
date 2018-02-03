package ki.robotics.utility.pixyCam;

import lejos.hardware.port.Port;
import lejos.hardware.sensor.I2CSensor;

public class PixyCam extends I2CSensor{
    private static final int ONE_BYTE = 8;
    private static final int FIRST_BYTE =  0x000000FF;
    private static final int SECOND_BYTE = 0x0000FF00;

    private static final int GENERAL_QUERY_ADDRESS = 0x50;
    private static final int SIGNATURE_QUERY_BASE_ADDRESS = 0x50;
    private static final int ANGLE_QUERY_ADDRESS = 0x60;
    private static final int COLOR_CODE_QUERY_ADDRESS = 0x58;


    private static final int BYTES_IN_GENERAL_QUERY_RESPONSE = 6;
    private static final int BYTES_IN_SIGNATURE_QUERY_RESPONSE = 5;
    private static final int BYTES_IN_ANGLE_QUERY_RESPONSE = 1;

    private static final int BYTES_IN_COLOR_CODE_QUERY = 2;
    private static final int BYTES_IN_COLOR_CODE_QUERY_RESPONSE = 6;

    private static final int PIXEL_WIDTH_OF_SENSOR = 255;
    private static final int CENTRAL_PIXEL_OF_SENSOR = 128;
    private static final int IMAGE_ANGLE = 75;


    public PixyCam(Port port) {
        super(port, DEFAULT_I2C_ADDRESS);
    }


    public DTOGeneralQuery generalQuery() {
        byte[] buffer = new byte[BYTES_IN_GENERAL_QUERY_RESPONSE];
        getData(GENERAL_QUERY_ADDRESS, buffer, BYTES_IN_GENERAL_QUERY_RESPONSE);
        return  new DTOGeneralQuery(buffer);
    }


    public DTOSignatureQuery signatureQuery(int signature) {
        if (signature < 1  ||  signature > 7) {
            throw new IndexOutOfBoundsException();
        }

        int address = SIGNATURE_QUERY_BASE_ADDRESS + signature;
        byte[] buffer = new byte[BYTES_IN_SIGNATURE_QUERY_RESPONSE];
        getData(address, buffer, BYTES_IN_SIGNATURE_QUERY_RESPONSE);
        return new DTOSignatureQuery(buffer);
    }

    public DTOSignatureQuery[] allSignaturesQuery() {
        DTOSignatureQuery[] queries = new DTOSignatureQuery[7];
        for (int i = 0  ;  i < 7  ;  i++) {
            queries[i] = signatureQuery(i+1);
        }
        return queries;
    }


    public DTOAngleQuery angleQuery() {
        byte[] buffer = new byte[BYTES_IN_ANGLE_QUERY_RESPONSE];
        getData(ANGLE_QUERY_ADDRESS, buffer, BYTES_IN_ANGLE_QUERY_RESPONSE);
        return new DTOAngleQuery(buffer);
    }


    public DTOColorCodeQuery colorCodeQuery(int colorCode) {
        byte lsb = (byte)(colorCode & FIRST_BYTE);
        byte msb = (byte)((colorCode & SECOND_BYTE) >> ONE_BYTE);
        byte[] code = new byte[]{lsb, msb};
        sendData(COLOR_CODE_QUERY_ADDRESS, code, BYTES_IN_COLOR_CODE_QUERY);
        byte[] buffer = new byte[BYTES_IN_COLOR_CODE_QUERY_RESPONSE];
        getData(COLOR_CODE_QUERY_ADDRESS, buffer, BYTES_IN_COLOR_CODE_QUERY_RESPONSE);
        return new DTOColorCodeQuery(buffer);
    }


    public static int angleDegreeToPixel(double degree) {
        if (degree == 0) {
            return CENTRAL_PIXEL_OF_SENSOR;
        } else if(degree < 0) {
            return CENTRAL_PIXEL_OF_SENSOR + ((int) Math.round((Math.abs(degree) / IMAGE_ANGLE * PIXEL_WIDTH_OF_SENSOR)));
        } else {
            return CENTRAL_PIXEL_OF_SENSOR - ((int) Math.round((Math.abs(degree) / IMAGE_ANGLE * PIXEL_WIDTH_OF_SENSOR)));
        }
    }

    public static double anglePixelToDegrees(int pixel) {
        if (pixel == CENTRAL_PIXEL_OF_SENSOR) {
            return 0;
        } else if (pixel < CENTRAL_PIXEL_OF_SENSOR) {
            return (double)pixel / PIXEL_WIDTH_OF_SENSOR * IMAGE_ANGLE *-1;
        } else {
            return (double)pixel / PIXEL_WIDTH_OF_SENSOR * IMAGE_ANGLE;
        }
    }
}
