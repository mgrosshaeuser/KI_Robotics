package ki.robotics.utility.crisp;

/**
 * C.R.I.S.P. - Compact RobotInstructions and Status Protocol
 */
public class CRISP {
    //Instruction-Group specifiers.
    public static final char BOT_INSTRUCTION =      'B';
    public static final char SENSOR_INSTRUCTION =   'S';
    public static final char CAMERA_INSTRUCTION =   'C';
    public static final char OTHER_INSTRUCTION =    'O';


    //Instruction-Set. Commonly used as confirmatory status-codes as well.
    public static final String BOT_TRAVEL_FORWARD =             "BTRF";
    public static final String BOT_TRAVEL_BACKWARD =            "BTRB";
    public static final String BOT_TURN_LEFT =                  "BTNL";
    public static final String BOT_TURN_RIGHT =                 "BTNR";
    public static final String BOT_RETURN_POSE =                "BPOS";
    public static final String BOT_LINE_FOLLOWING_ENABLED =     "BLFE";
    public static final String BOT_LINE_FOLLOWING_DISABLED =    "BLFD";

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
    public static final String BOT_U_TURN =                     "BUTN";

    public static final String UNSUPPORTED_INSTRUCTION =        "IERR";
    public static final String END_OF_INSTRUCTION_SEQUENCE =    "EOSQ";
}