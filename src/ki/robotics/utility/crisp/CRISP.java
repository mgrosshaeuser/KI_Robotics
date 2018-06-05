package ki.robotics.utility.crisp;

/**
 * C.R.I.S.P. - Compact RobotInstructions and Status Protocol
 */
public interface CRISP {
    //Instruction-Group specifiers.
    char BOT_INSTRUCTION =      'B';
    char SENSOR_INSTRUCTION =   'S';
    char CAMERA_INSTRUCTION =   'C';
    char OTHER_INSTRUCTION =    'O';


    //Instruction-Set. Commonly used as confirmatory status-codes as well.
    String BOT_TRAVEL_FORWARD =             "BTRF";
    String BOT_TRAVEL_BACKWARD =            "BTRB";
    String BOT_TURN_LEFT =                  "BTNL";
    String BOT_TURN_RIGHT =                 "BTNR";
    String BOT_RETURN_POSE =                "BPOS";
    String BOT_LINE_FOLLOWING_ENABLED =     "BLFE";
    String BOT_LINE_FOLLOWING_DISABLED =    "BLFD";

    String SENSOR_TURN_LEFT =               "STNL";
    String SENSOR_TURN_RIGHT =              "STNR";
    String SENSOR_RESET =                   "SRST";
    String SENSOR_MEASURE_COLOR =           "SCLR";
    String SENSOR_SINGLE_DISTANCE_SCAN =    "SDST";
    String SENSOR_THREE_WAY_SCAN =          "STWS";

    String CAMERA_GENERAL_QUERY =           "CGEN";
    String CAMERA_SINGLE_SIGNATURE_QUERY =  "CSSG";
    String CAMERA_ALL_SIGNATURES_QUERY =    "CASG";
    String CAMERA_COLOR_CODE_QUERY =        "CCOL";
    String CAMERA_ANGLE_QUERY =             "CANG";

    String SHUTDOWN =                       "DOWN";
    String DISCONNECT =                     "DCNT";

    //Supplementary status codes.
    String BOT_U_TURN =                     "BUTN";

    String UNSUPPORTED_INSTRUCTION =        "IERR";
    String END_OF_INSTRUCTION_SEQUENCE =    "EOSQ";
}