package ki.robotics.robot;

import ki.robotics.utility.crisp.Instruction;
import ki.robotics.utility.map.Map;
import lejos.robotics.navigation.Pose;

import java.awt.*;

/**
 * Implementation of the Robot-Interface through the VirtualRobotModel abstract-class.
 * Represents a MCL-Particle in a way that it can be addressed as a robot (which it represents conceptually).
 *
 * @version 1.0 01/02/18
 */
public class MCLParticle extends VirtualRobotModel implements Comparable<MCLParticle> {
    private float weight;

    /**
     * Constructor.
     *
     * @param pose      A Pose for the new particle.
     * @param map       The map.
     * @param weight    The weight of the new particle.
     */
    public MCLParticle(Pose pose, Map map, float weight) {
        this.pose = pose;
        this.map = map;
        this.weight = weight;
    }

    /**
     * Constructor.
     *
     * @param particle  A particle as 'template'.
     */
    public MCLParticle(MCLParticle particle) {
        this.pose = new Pose(particle.pose.getX(), particle.pose.getY(), particle.pose.getHeading());
        this.map = particle.map;
        this.sensorHeadPosition = particle.sensorHeadPosition;
        this.weight = 0;
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
        pose.setHeading(n);
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
    public boolean handleUnsupportedInstruction(Instruction instruction) {
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
     * @param medianWeight     The median particle weight used to link weight to color.
     */
    public void paint(Graphics g, int diameter, int scaleFactor, int xOffset, int yOffset, double medianWeight) {
        Graphics2D g2d = (Graphics2D) g;

        if (weight < (0.75 * medianWeight)) {
            g2d.setColor(new Color(150,0,100));
        } else if (weight >= (0.75 * medianWeight)  &&  weight < (1.25 * medianWeight)) {
            g2d.setColor(new Color(150,150,100));
        } else {
            g2d.setColor(new Color(0,150,100));
        }
        g2d.fillOval(
                Math.round(this.pose.getX() - (diameter / 2)) * scaleFactor + xOffset,
                Math.round(this.pose.getY() - (diameter / 2)) * scaleFactor + yOffset,
                diameter * scaleFactor,
                diameter * scaleFactor
        );

        g2d.setColor(Color.BLUE);
        g2d.fillArc(
                (Math.round((pose.getX() - diameter / 2)) * scaleFactor) + xOffset,
                (Math.round((pose.getY() - diameter / 2)) * scaleFactor) + yOffset,
                Math.round(diameter  * scaleFactor),
                Math.round(diameter  * scaleFactor),
                Math.round(sensorHeadPosition + pose.getHeading() - 10),
                20
        );
    }


    /**
     * Using the Comparable-Interface to compare particles according to their weight.
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(MCLParticle o) {
        if (this.weight < o.weight) {
            return -1;
        } else if (this.weight > o.weight) {
            return 1;
        } else {
            return 0;
        }
    }
}
