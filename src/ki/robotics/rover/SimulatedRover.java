package ki.robotics.rover;

import ki.robotics.datastructures.Instruction;
import ki.robotics.datastructures.Map;
import lejos.robotics.navigation.Pose;

import javax.swing.*;
import java.awt.*;
import java.io.File;


/**
 * A simulated robot using a JFrame rather than actual movement to display behaviiour and
 * calculates rather than measures sensor-data.
 *
 * @version 1.1 12/28/17
 */
public class SimulatedRover extends JFrame implements Robot{
    public static final int WINDOW_WIDTH = 600;
    public static final int WINDOW_HEIGHT = 800;

    private static final int ANIMATION_FRAME_INTERVAL = 100;

    private int scaleFactor = 1;
    private int xOffset = 0;
    private int yOffset = 0;

    private Map map;
    private MapOverlay mapOverlay = new MapOverlay();
    private Rover rover = new Rover();
    private Pose pose;



    /**
     *Constructor.
     */
    public SimulatedRover() {
        this.map = new Map(new File(getClass().getClassLoader().getResource("map2.svg").getFile()));
        this.pose = new Pose();
        pose.setLocation(Rover.BOT_DIAMETER/2 , Rover.BOT_DIAMETER/2);
        this.setTitle("Simulated Rover");
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        add(mapOverlay);
        mapOverlay.add(rover);
        this.setVisible(true);
    }



    /**
     * Paints the JFrame after updating the scale-factor and offsets to cope with resizing of the window.
     * @param g     The graphical context.
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        updateVisualParameters();
        Graphics2D g2d = (Graphics2D) g;
    }


    /**
     * (Re)Calculates the visual parameters, including
     * - the scale-factor to use the available space at its best
     * - the x- and y-offsets to center the simulation in the window.
     */
    private void updateVisualParameters() {
        int maxX = map.getRequiredMinWidth();
        int maxY = map.getRequiredMinHeight();

        double width = mapOverlay.getVisibleRect().getWidth();
        double height = mapOverlay.getVisibleRect().getHeight();

        double scaleX = width / maxX;
        double scaleY = height / maxY;

        double limitingFactor = scaleX > scaleY ? scaleY : scaleX;

        scaleFactor = (int) Math.abs(Math.floor(limitingFactor));

        xOffset = ((int) Math.abs(width) - (maxX * scaleFactor)) / 2;
        yOffset = ((int) Math.abs(height) - (maxY * scaleFactor)) / 2;
    }






    //////////////////////////////////////////////////////////////////////////////
    //                                                                          //
    //          Implementation of the methods from the Robot-interface          //
    //                                                                          //
    //////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean botTravelForward(double distance) {
        int temp = rover.getSensorHeadOrientation();
        if (distance > 0) {
            sensorHeadReset();
        } else {
            sensorHeadTurnRight(180);
        }

        distance = (measureDistance() >= distance + 5) ? distance : measureDistance() - 5;
        float dx = (float) (Math.cos(Math.toRadians(pose.getHeading())) * distance);
        float dy = (float) (Math.sin(Math.toRadians(pose.getHeading())) * distance) * -1;

        float xStep = dx / (float)distance;
        float yStep = dy / (float)distance;
        for (int i = 0  ;  i < distance ; i++) {
            timedPoseTranslation(xStep, yStep);
        }

        rover.setSensorHeadOrientation(temp);
        repaint();
        return true;
    }



    @Override
    public boolean botTravelBackward(double distance) {
        botTravelForward(distance * -1);
        repaint();
        return true;
    }



    @Override
    public boolean botTurnLeft(double degree) {
        float newHeading = (pose.getHeading() + (float) degree) % 360;
        pose.setHeading(newHeading);
        repaint();
        return true;
    }



    @Override
    public boolean botTurnRight(double degree) {
        float newHeading = (pose.getHeading() - (float) degree) % 360;
        if (newHeading < 0) {
            newHeading += 360;
        }
        pose.setHeading(newHeading);
        repaint();
        return true;
    }



    @Override
    public boolean sensorHeadTurnLeft(double position) {
        this.rover.setSensorHeadOrientation((int) Math.round(position));
        repaint();
        return true;
    }

    @Override
    public boolean sensorHeadTurnRight(double position) {
        this.rover.setSensorHeadOrientation((int) Math.round(position * -1));
        repaint();
        return true;
    }

    @Override
    public boolean sensorHeadReset() {
        this.rover.setSensorHeadOrientation(0);
        repaint();
        return true;
    }

    @Override
    public int measureColor() {
        return map.getColorAtPosition(Math.round(pose.getX()), Math.round(pose.getY()));
    }

    @Override
    public double measureDistance() {
        return map.getDistanceToObstacle(pose.getX(), pose.getY(), 360 - pose.getHeading() - rover.sensorHeadOrientation);
    }

    @Override
    public double[] ultrasonicThreeWayScan() {
        sensorHeadTurnLeft(90);
        double a = measureDistance();
        sensorHeadReset();
        double b = measureDistance();
        sensorHeadTurnRight(90);
        double c = measureDistance();
        return new double[]{a, b, c};
    }

    @Override
    public Pose getPose() {
        return pose;
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
        System.out.println("Unknown Instruction: " + instruction);
        return true;
    }







    /**
     * Animates the motion of the robot.
     *
     * @param dx    horizontal distance of motion.
     * @param dy    vertical distance of motion.
     */
    private void timedPoseTranslation(float dx, float dy) {
        pose.translate(dx, dy);
        repaint();
        try {
            Thread.sleep(ANIMATION_FRAME_INTERVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    /**
     * A JPanel for showing the map.
     */
    private class MapOverlay extends JPanel {
        /**
         * Paints the map and the rover. Currently the wall-color is neglected.
         * @param g     The graphics-context to paint on.
         */
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            this.setBackground(Color.LIGHT_GRAY);
            if (map != null) map.paint(g, scaleFactor, xOffset, yOffset);
            if (rover != null) rover.paint(g);
        }

    }





    /**
     * JComponent as the visual representation of the simulated rover.
     */
    private class Rover extends JComponent {
        public static final int BOT_DIAMETER = 10;
        public static final int BOT_OPENING = 20;
        public static final double SCALE_FOR_SENSOR_HEAD = 1.2;
        public static final int SENSOR_HEAD_ANGLE = 30;

        private int sensorHeadOrientation;


        /**
         * Constructor.
         *
         * Initializes the rovers sensor-head to point in the current travel-direction.
         */
        public Rover() {
            this.sensorHeadOrientation = 0;
        }



        /**
         * Sets the sensor-head to a new angle.
         *
         * @param angle The new angle of the sensor-head.
         */
        public void setSensorHeadOrientation(int angle) {
            this.sensorHeadOrientation = angle;
        }



        /**
         * Returns the current angle of the sensor-head.
         *
         * @return Current angle of sensor-head.
         */
        public int getSensorHeadOrientation() {
            return  this.sensorHeadOrientation;
        }



        /**
         * Paints the visual representation of the rover onto the mapOverlay. That visual representation consists of:
         * - the sensor-head as a filled arc at the lowermost layer,
         * - a black background for the rover as a filled circle at the intermediate layer and
         * - the rover itself as a yellow arc at the uppermost layer.
         *
         * Any resemblance to video-games from the 1980's is pure coincidence ;-)
         *
         * @param g     The graphical context.
         */
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2d = (Graphics2D) g;

            paintSensorHead(g2d, Color.GREEN);
            paintRobot(g2d, Color.YELLOW, Color.LIGHT_GRAY);
        }

        private void paintSensorHead(Graphics2D g2d, Color color) {
            int headX = ((int)Math.round((pose.getX() - (BOT_DIAMETER  * SCALE_FOR_SENSOR_HEAD) / 2)) * scaleFactor) + xOffset;
            int headY = ((int)Math.round((pose.getY() - (BOT_DIAMETER * SCALE_FOR_SENSOR_HEAD) / 2)) * scaleFactor) + yOffset;
            int headDia = (int) Math.round(BOT_DIAMETER  * scaleFactor * SCALE_FOR_SENSOR_HEAD);
            int startAngle = Math.round(sensorHeadOrientation + pose.getHeading() - SENSOR_HEAD_ANGLE / 2);

            g2d.setColor(color);
            g2d.fillArc(headX, headY, headDia, headDia, startAngle, SENSOR_HEAD_ANGLE);
        }


        private void paintRobot(Graphics2D g2d, Color bot, Color background) {
            int botX = (Math.round((pose.getX() - BOT_DIAMETER / 2)) * scaleFactor) + xOffset;
            int botY = (Math.round((pose.getY() - BOT_DIAMETER / 2)) * scaleFactor) + yOffset;
            int botDia = BOT_DIAMETER * scaleFactor;

            g2d.setColor(background);
            g2d.fillOval(botX,botY,botDia,botDia);

            int startAngle = (int)Math.abs(pose.getHeading() + BOT_OPENING / 2);
            int arcAngle = 360 - BOT_OPENING;

            g2d.setColor(bot);
            g2d.fillArc(botX, botY, botDia, botDia, startAngle, arcAngle );
        }

    }
}
