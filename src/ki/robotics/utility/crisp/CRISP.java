package ki.robotics.utility.crisp;

/**
 * C.R.I.S.P. - Compact RobotInstructions and Status Protocol
 */
public class CRISP {
    //Instruction-Group specifiers.
    public static final char BOT_INSTRUCTION =      'B';
    public static final char SENSOR_INSTRUCTION =   'S';
    public static final char CAMERA_INSTRUCTION =   'C';



    //Instruction-Set. Commonly used as confirmatory status-codes as well.
    public static final String BOT_TRAVEL_FORWARD =             "BTRF";
    public static final String BOT_TRAVEL_BACKWARD =            "BTRB";
    public static final String BOT_TURN_LEFT =                  "BTNL";
    public static final String BOT_TURN_RIGHT =                 "BTNR";
    public static final String BOT_RETURN_POSE =                "BPOS";

    public static final String SENSOR_TURN_LEFT =               "STNL";
    public static final String SENSOR_TURN_RIGHT =              "STNR";
    public static final String SENSOR_RESET =                   "SRST";
    public static final String SENSOR_MEASURE_COLOR =           "SCLR";
    public static final String SENSOR_SINGLE_DISTANCE_SCAN =    "SDST";
    public static final String SENSOR_THREE_WAY_SCAN =          "STWS";

    public static final String CAMERA_GENERAL_QUERY =           "CGEN";
    public static final String CAMERA_SINGLE_SIGNATURE_QUERY =  "CSSG";
    public static final String CAMERA_ALL_SIGNATURES_QUERY =    "CASG";
    public static final String CAMERA_COLOR_CODE_QUERY =        "CCOL";
    public static final String CAMERA_ANGLE_QUERY =             "CANG";

    public static final String SHUTDOWN =                       "DOWN";
    public static final String DISCONNECT =                     "DCNT";



    //Supplementary status codes.
    public static final String BOT_POSE_X =                     "BPSX";
    public static final String BOT_POSE_Y =                     "BPSY";
    public static final String BOT_POSE_HEADING =               "BPSH";

    public static final String THREE_WAY_SCAN_LEFT =            "STWL";
    public static final String THREE_WAY_SCAN_CENTER =          "STWC";
    public static final String THREE_WAY_SCAN_RIGHT =           "STWR";

    public static final String CAMERA_SIGNATURE_1 =             "CSG1";
    public static final String CAMERA_SIGNATURE_2 =             "CSG2";
    public static final String CAMERA_SIGNATURE_3 =             "CSG3";
    public static final String CAMERA_SIGNATURE_4 =             "CSG4";
    public static final String CAMERA_SIGNATURE_5 =             "CSG5";
    public static final String CAMERA_SIGNATURE_6 =             "CSG6";
    public static final String CAMERA_SIGNATURE_7 =             "CSG7";

    public static final String UNSUPPORTED_INSTRUCTION =        "IERR";
    public static final String END_OF_INSTRUCTION_SEQUENCE =    "EOSQ";
}
