package ki.robotics.server.robot.virtualRobots;

import ki.robotics.server.Robot;
import ki.robotics.server.robot.RobotImplVirtualRobot;
import ki.robotics.utility.crisp.Message;
import ki.robotics.utility.map.Map;
import lejos.robotics.navigation.Pose;

import java.awt.*;
import java.io.Serializable;

/**
 * Implementation of the Robot-Interface through the RobotImplVirtualRobot abstract-class.
 * Represents a MCL-Particle in a way that it can be addressed as a robot (which it represents conceptually).
 *
 * @version 1.0 01/02/18
 */
public class MCLParticle extends RobotImplVirtualRobot implements Comparable<MCLParticle>, Serializable {
    private float weight;
    private Color color;
    private float[] poseSerializable;

    /**
     * Constructor.
     *
     * @param pose      A Pose for the new particle.
     * @param map       The map.
     * @param weight    The weight of the new particle.
     */
    public MCLParticle(Pose pose, Map map, float weight, Color color) {
        this.pose = pose;
        this.poseSerializable = new float[]{pose.getX(), pose.getY(), pose.getHeading()};
        this.map = map;
        this.weight = weight;
        this.color = color;
    }


    public MCLParticle getClone() {
        Pose pose = new Pose (this.pose.getX(), this.pose.getY(), this.pose.getHeading());
        MCLParticle particle = new MCLParticle(pose, this.map, this.weight, this.color);
        particle.sensorHeadPosition = this.sensorHeadPosition;
        return particle;
    }

    @Override
    public Pose getPose() {
        if (this.pose == null) {
            this.pose = new Pose(poseSerializable[0], poseSerializable[1], poseSerializable[2]);
        }
        return this.pose;
    }

    /**
     * Constructor
     * @param
     */
    public MCLParticle(float x, float y, float heading, float weight, Color color){
        this.pose = new Pose(x,y,heading);
        this.poseSerializable = new float[]{pose.getX(), pose.getY(), pose.getHeading()};
        this.weight = weight;
        this.color = color;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Translates a particle over given distances in x- and y-direction.
     *
     * @param dx    Translation-distance along the x-axis.
     * @param dy    Translation-distance along the y-axis.
     */
    @Override
    public void translate(float dx, float dy) {
         pose.translate(dx, dy);
    }

    /**
     * Turns the heading over the given degrees.
     *
     * @param degrees   Degrees to turn the heading.
     */
    @Override
    public void turnFull(int degrees) {
        float n = (pose.getHeading() + degrees) % 360;
        if (n < 0) {
            n += 360;
        }
        pose.setHeading(Math.abs(n));
    }

    /**
     * Turns the position of the sensor-head carrying the distance-sensor.
     *
     * @param position  The new position of the sensor-head.
     */
    @Override
    public void turnSensor(int position) {
        sensorHeadPosition = position;
    }

    @Override
    public boolean shutdown() {
        return false;
    }

    @Override
    public boolean disconnect() {
        return false;
    }

    @Override
    public boolean handleUnsupportedInstruction(Message instruction) {
        return true;
    }


    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }


    /**
     * Paints the particle in a given graphical context.
     *
     * @param g             The graphical context.
     * @param diameter      The visual-diameter of the particle.
     * @param scaleFactor   The scale-factor for visualization.
     * @param xOffset       The offset on the x-axis for visualization.
     * @param yOffset       The offset on the y-axis for visualization.
     */
    public void paint(Graphics g, int diameter, int scaleFactor, int xOffset, int yOffset) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(this.color);
        g2d.fillOval(
                Math.round(this.getPose().getX() - (diameter / 2)) * scaleFactor + xOffset,
                Math.round(this.getPose().getY() - (diameter / 2)) * scaleFactor + yOffset,
                diameter * scaleFactor,
                diameter * scaleFactor
        );

        g2d.setColor(Color.BLUE);
        g2d.fillArc(
                (Math.round((getPose().getX() - diameter / 2)) * scaleFactor) + xOffset,
                (Math.round((getPose().getY() - diameter / 2)) * scaleFactor) + yOffset,
                Math.round(diameter  * scaleFactor),
                Math.round(diameter  * scaleFactor),
                Math.round(sensorHeadPosition + pose.getHeading() - 10),
                20
        );
    }


    /**
     * compare particles according to their weight.
     *
     * @param o particle to compare with
     * @return comparison result by float object
     */
    @Override
    public int compareTo(MCLParticle o) {
        return Float.compare(this.weight, o.weight);
    }

    public boolean isOutOfBounds() {
        float xPos = this.pose.getX();
        float yPos = this.pose.getY();
        Polygon boundaries = this.map.getOperatingRange();

        return !boundaries.contains(xPos, yPos);
    }
}
