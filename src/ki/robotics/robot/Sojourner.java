package ki.robotics.robot;

import ki.robotics.utility.crisp.Instruction;
import ki.robotics.server.Main;
import ki.robotics.utility.pixyCam.DTOSignatureQuery;
import ki.robotics.utility.pixyCam.PixyCam;
import lejos.hardware.Sound;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
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

    private final MovePilot pilot;
    private final EV3UltrasonicSensor uss;
    private final NXTRegulatedMotor sensorHead = Motor.C;
    private final EV3ColorSensor cls;
    private final PixyCam cam;
    private final PoseProvider poseProvider;
    private Pose pose;

    private int sensorCurrentPosition;



    /**
     * Constructor.
     */
    private Sojourner() {
        pilot = configureMovePilot();

        uss = new EV3UltrasonicSensor(SensorPort.S4);
        cls = new EV3ColorSensor(SensorPort.S1);
        cam = new PixyCam(SensorPort.S2);

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
        double diameter = 55.5;
        double offset = 56.35;
        Wheel left = WheeledChassis.modelWheel(Motor.A, diameter).offset(-offset);
        Wheel right = WheeledChassis.modelWheel(Motor.D, diameter).offset(offset);
        Chassis chassis = new WheeledChassis(new Wheel[] {left, right}, WheeledChassis.TYPE_DIFFERENTIAL);
        MovePilot pilot = new MovePilot(chassis);
        pilot.setAngularAcceleration(100);
        pilot.setLinearAcceleration(150);
        pilot.setAngularSpeed(60);
        pilot.setLinearSpeed(100);
        Sound.setVolume(20);
        Sound.twoBeeps();
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
        sensorHeadReset();
        double distanceToFront = measureDistance();
        double bumper = 8;

        distance = (distanceToFront >= distance + bumper) ? distance : (distanceToFront - bumper);
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
        pilot.rotate(degree * - 1);
        return true;
    }

    @Override
    public boolean botTurnRight(double degree) {
        pilot.rotate(degree);
        return true;
    }

    @Override
    public boolean sensorHeadTurnLeft(double position) {
        int degrees = (int) (position + sensorCurrentPosition) % 360;
        sensorHead.rotateTo(degrees);
        sensorCurrentPosition = degrees;
        return true;
    }

    @Override
    public boolean sensorHeadTurnRight(double position) {
        int degrees = (int) (sensorCurrentPosition - position) % 360;
        sensorHead.rotateTo(degrees);
        sensorCurrentPosition = degrees;
        return true;
    }

    @Override
    public boolean sensorHeadReset() {
        sensorHead.rotateTo(0);
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

    /**
     *
     * @return distance left, front, right
     */
    @Override
    public double[] ultrasonicThreeWayScan() {
        double[] sonicValues = new double[3];
        int degreeStep = 90;
        //front measurement
        sonicValues[1] = measureDistance();
        sensorHeadTurnLeft(degreeStep);
        //left measurement
        sonicValues[0] = measureDistance();
        sensorHeadTurnRight(2 * degreeStep);
        //right measurement
        sonicValues[2] = measureDistance();
        //turn head straight to front
        sensorHeadReset();
        return sonicValues;
    }

    @Override
    public Pose getPose() {
        pose = poseProvider.getPose();
        return pose;
    }

    @Override
    public String cameraGeneralQuery() {
        return cam.generalQuery().toString();
    }

    @Override
    public String cameraSignatureQuery(int signature) {
        return cam.signatureQuery(signature).toString();
    }

    @Override
    public String[] cameraAllSignaturesQuery() {
        DTOSignatureQuery[] signatures = cam.allSignaturesQuery();
        String[] response = new String[signatures.length];
        for (int i = 0  ;  i < signatures.length  ;  i++) {
            response[i] = signatures[i].toString();
        }
        return response;
    }

    @Override
    public String cameraColorCodeQuery(int color) {
        return cam.colorCodeQuery(color).toString();
    }

    public String cameraAngleQuery() {
        return cam.angleQuery().toString();
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
