package ki.robotics.utility.crisp;

/**
 * C.R.I.S.P. - Compact RobotInstructions and Status Protocol
 */
public class CRISP {
    //Instruction-Set. Commonly used as confirmatory status-codes as well.
    public static final String BOT_TRAVEL_FORWARD = "BTRF";
    public static final String BOT_TRAVEL_BACKWARD = "BTRB";
    public static final String BOT_TURN_LEFT = "BTNL";
    public static final String BOT_TURN_RIGHT = "BTNR";
    public static final String SENSOR_TURN_LEFT = "STNL";
    public static final String SENSOR_TURN_RIGHT = "STNR";
    public static final String SENSOR_RESET = "SRST";
    public static final String MEASURE_COLOR = "MCLR";
    public static final String N_WAY_SCAN = "MDST";
    public static final String THREE_WAY_SCAN = "USTW";
    public static final String RETURN_POSE = "POSE";
    public static final String BOT_SHUTDOWN = "SHTD";
    public static final String BOT_DISCONNECT = "DCNT";
    public static final String CAMERA_GENERAL_QUERY = "CGEN";
    public static final String CAMERA_SINGLE_SIGNATURE_QUERY = "CSSG";
    public static final String CAMERA_ALL_SIGNATURES_QUERY = "CASG";
    public static final String CAMERA_COLOR_CODE_QUERY = "CCOL";
    public static final String CAMERA_ANGLE_QUERY = "CANG";

    //Supplementary status codes.
    public static final String THREE_WAY_SCAN_LEFT = "TWSL";
    public static final String THREE_WAY_SCAN_CENTER = "TWSC";
    public static final String THREE_WAY_SCAN_RIGHT = "TWSR";
    public static final String POSE_X = "POSX";
    public static final String POSE_Y = "POSY";
    public static final String POSE_HEADING = "POSH";
    public static final String UNSUPPORTED_INSTRUCTION = "IERR";
    public static final String INSTRUCTION_SEQUENCE_FINISHED ="SFIN";
    public static final String CAMERA_SIGNATURE_1 = "CSG1";
    public static final String CAMERA_SIGNATURE_2 = "CSG2";
    public static final String CAMERA_SIGNATURE_3 = "CSG3";
    public static final String CAMERA_SIGNATURE_4 = "CSG4";
    public static final String CAMERA_SIGNATURE_5 = "CSG5";
    public static final String CAMERA_SIGNATURE_6 = "CSG6";
    public static final String CAMERA_SIGNATURE_7 = "CSG7";
}
