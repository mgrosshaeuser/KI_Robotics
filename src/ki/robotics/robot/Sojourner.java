package ki.robotics.robot;

import ki.robotics.utility.crisp.Instruction;
import ki.robotics.server.Main;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.I2CSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.navigation.Pose;


/**
 * Singleton for accessing the hardware of a Lego-EV3-Robot.
 */
public class Sojourner implements Robot {

    private static final Sojourner INSTANCE = new Sojourner();

    private MovePilot pilot;
    private EV3UltrasonicSensor uss;
    private EV3ColorSensor cls;
    private I2CSensor cam;
    private PoseProvider poseProvider;
    private Pose pose;

    private int sensorCurrentPosition;



    /**
     * Constructor.
     */
    private Sojourner() {
        pilot = configureMovePilot();

        uss = new EV3UltrasonicSensor(SensorPort.S4);
        cls = new EV3ColorSensor(SensorPort.S1);
        cam = new I2CSensor(SensorPort.S2);

        poseProvider = new OdometryPoseProvider(pilot);
        pose = poseProvider.getPose();
        sensorCurrentPosition = 0;
    }



    /**
     *Configuration of a MovePilot to abstract from the movement-actions.
     *
     * @return  A MovePilot for the robot.
     */
    private MovePilot configureMovePilot() {
        Wheel left = WheeledChassis.modelWheel(Motor.A, 55.5).offset(-55);
        Wheel right = WheeledChassis.modelWheel(Motor.D, 55.5).offset(55);
        Chassis chassis = new WheeledChassis(new Wheel[] {left, right}, WheeledChassis.TYPE_DIFFERENTIAL);
        MovePilot pilot = new MovePilot(chassis);
        pilot.setAngularAcceleration(100);
        pilot.setLinearAcceleration(150);
        pilot.setAngularSpeed(60);
        pilot.setLinearSpeed(100);
        return pilot;
    }



    /**
     * Returns the singleton-instance of this class.
     *
     * @return returns singleton-instance.
     */
    public static Sojourner getInstance() {
        return INSTANCE;
    }





    //////////////////////////////////////////////////////////////////////////////
    //                                                                          //
    //          Implementation of the methods from the Robot-interface          //
    //                                                                          //
    //////////////////////////////////////////////////////////////////////////////

    @Override
    public double botTravelForward(double distance) {
        pilot.travel(distance * 10);
        return distance;
    }

    @Override
    public double botTravelBackward(double distance) {
        pilot.travel(distance * -10);
        return distance;
    }

    @Override
    public boolean botTurnLeft(double degree) {
        pilot.rotate(degree);
        return true;
    }

    @Override
    public boolean botTurnRight(double degree) {
        pilot.rotate(degree * -1);
        return true;
    }

    @Override
    public boolean sensorHeadTurnLeft(double position) {
        int degree = (int) Math.round(position % 180);
        int rotate = degree - sensorCurrentPosition;
        Motor.C.rotate(rotate);
        sensorCurrentPosition = degree;
        return true;
    }

    @Override
    public boolean sensorHeadTurnRight(double position) {
        int degree = (int) Math.round(position % 180) * -1;
        int rotate = degree - sensorCurrentPosition;
        Motor.C.rotate(rotate);
        sensorCurrentPosition = degree;
        return true;
    }

    @Override
    public boolean sensorHeadReset() {
        int rotate = sensorCurrentPosition * -1;
        Motor.C.rotate(rotate);
        sensorCurrentPosition = 0;
        return true;
    }

    @Override
    public int measureColor() {
        int ev3color = cls.getColorID();
        return translateColor(ev3color);
    }

    @Override
    public double measureDistance() {
        SampleProvider sample = uss.getDistanceMode();
        float[] distance = new float[sample.sampleSize()];
        uss.getDistanceMode().fetchSample(distance, 0);
        return distance[0] * 100;
    }

    @Override
    public double[] ultrasonicThreeWayScan() {
        return new double[3];
    }

    @Override
    public Pose getPose() {
        pose = poseProvider.getPose();
        return pose;
    }

    @Override
    public byte[] cameraGeneralQuery() {
        byte[] generalQuery = new byte[6];
        cam.getData(0x50, generalQuery, 6);
        return generalQuery;
    }

    @Override
    public byte[][] cameraSignatureQuery() {
        byte[][] signatures = new byte[7][5];
        for (int i = 0  ;  i < 7  ;  i++) {
            byte[] signature = new byte[5];
            cam.getData(0x51+i, signature, 5);
            signatures[i] = signature;
        }
        return signatures;
    }

    @Override
    public byte[] cameraColorCodeQuery(int color) {
        //TODO Camera-Code
        return null;
    }

    public byte[] cameraAngleQuery() {
        byte[] angleQuery = new byte[1];
        cam.getData(0x60, angleQuery, 1);
        return angleQuery;
    }


    @Override
    public boolean shutdown() {
        Main.shutdown();
        return false;
    }

    @Override
    public boolean disconnect() {
        Main.disconnect();
        return false;
    }

    @Override
    public boolean handleUnsupportedInstruction(Instruction instruction) {

        return true;
    }






    /**
     * Translates from leJOS-colors to java.awt-colors
     *
     * @param c     The leJOS color-code
     * @return      The RGB-value according to java.awt.Color.
     */
    private int translateColor(int c) {
        switch (c) {
            case lejos.robotics.Color.RED: 			return java.awt.Color.RED.getRGB();
            case lejos.robotics.Color.GREEN: 		return java.awt.Color.GREEN.getRGB();
            case lejos.robotics.Color.BLUE: 		return java.awt.Color.BLUE.getRGB();
            case lejos.robotics.Color.YELLOW: 		return java.awt.Color.YELLOW.getRGB();
            case lejos.robotics.Color.MAGENTA:	 	return java.awt.Color.MAGENTA.getRGB();
            case lejos.robotics.Color.ORANGE: 		return java.awt.Color.ORANGE.getRGB();
            case lejos.robotics.Color.WHITE:	 	return java.awt.Color.WHITE.getRGB();
            case lejos.robotics.Color.BLACK: 		return java.awt.Color.BLACK.getRGB();
            case lejos.robotics.Color.PINK: 		return java.awt.Color.PINK.getRGB();
            case lejos.robotics.Color.GRAY: 		return java.awt.Color.GRAY.getRGB();
            case lejos.robotics.Color.LIGHT_GRAY:	return java.awt.Color.LIGHT_GRAY.getRGB();
            case lejos.robotics.Color.DARK_GRAY: 	return java.awt.Color.DARK_GRAY.getRGB();
            case lejos.robotics.Color.CYAN: 		return java.awt.Color.CYAN.getRGB();
            case lejos.robotics.Color.BROWN:		return new java.awt.Color(165, 42, 42).getRed();
            case lejos.robotics.Color.NONE: 		return -1;
            default: return -2;
        }
    }
}
