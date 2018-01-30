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

import javax.sound.sampled.Line;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


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

    private final int deltaSensorAxis = 15;
    private boolean stayOnWhiteLine = true;


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

        if (stayOnWhiteLine && measureColor() != java.awt.Color.WHITE.getRGB()) {
            getBackToWhiteLine();
        }

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
    public int[] cameraGeneralQuery() {
        return cam.generalQuery().getAllParameters();
    }

    @Override
    public int[] cameraSignatureQuery(int signature) {
        return cam.signatureQuery(signature).getAllParameters();
    }

    @Override
    public int[][] cameraAllSignaturesQuery() {
        DTOSignatureQuery[] signatures = cam.allSignaturesQuery();
        int[][] response = new int[signatures.length][];
        for (int i = 0  ;  i < signatures.length  ;  i++) {
            response[i] = signatures[i].getAllParameters();
        }
        return response;
    }

    @Override
    public int[] cameraColorCodeQuery(int color) {
        return cam.colorCodeQuery(color).getAllParameters();
    }

    public int cameraAngleQuery() { return cam.angleQuery().getAngleOfLargestColorCodedBlock(); }


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

    @Override
    public boolean setStayOnWhiteLine(boolean stayOnWhiteLine) {
        this.stayOnWhiteLine = stayOnWhiteLine;
        return stayOnWhiteLine;
    }

    /**
     * lets Sojourner end up on the line with its axis
     */
    private void getBackToWhiteLine() {

        LineReturnCode lrc = alignColorSensor();
        pilot.travel(-deltaSensorAxis * 10);
        int endCorrection = 10;

        if(!lrc.isDirection()){ //in Gegenteil der letzten Richtung drehen
            botTurnLeft(lrc.getDegrees() + endCorrection);
        } else {
            botTurnRight(lrc.getDegrees() + endCorrection);
        }
        pilot.travel(deltaSensorAxis * 10);
    }

    /**
     * lets Sojourner end up on the line with its sensor
     */
    private LineReturnCode alignColorSensor() {

        final List<Integer> wiggleSteps = Arrays.asList(5, 10, 15, 20, 25, 30, 35, 40, 45);
        final Iterator<Integer> iteratorWiggle = wiggleSteps.iterator();
        boolean pivotRight = true;
        int iteratorStep=0;

        while (measureColor() != java.awt.Color.WHITE.getRGB()) {
            if(iteratorWiggle.hasNext()) iteratorStep = iteratorWiggle.next();

            if (pivotRight) {
                botTurnRight(iteratorStep);
            } else {
                botTurnLeft(iteratorStep);
            }
            pivotRight = !pivotRight;

        }

        return new LineReturnCode(iteratorStep, pivotRight);
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


    private class LineReturnCode{
        int degrees;
        boolean direction;

        private LineReturnCode(int degrees, boolean direction){
            this.degrees = degrees;
            this.direction = direction;
        }

        public int getDegrees() {
            return degrees;
        }

        public boolean isDirection() {
            return direction;
        }
    }
}
