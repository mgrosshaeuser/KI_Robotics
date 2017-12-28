package ki.robotics.datastructures;

import lejos.robotics.navigation.Pose;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;





/**
 * Representation of a particle for monte-carlo-localization.
 *
 * @version 1.1, 12/28/17
 */
public class Particle extends JComponent{

    private Pose pose;
    private double weight;


    /**
     * Constructor
     *
     * @param x             x-Value for the particle.
     * @param y             y-Value for the particle.
     * @param heading       Heading for the particle.
     * @param weight        Weight of the particle.
     */
    public Particle(float x, float y, float heading, double weight) {
        this(new Pose(x, y, heading), weight);
    }



    /**
     * Constructor.
     *
     * @param pose      Pose for the particle.
     * @param weight    Weight of the particle.
     */
    public Particle(Pose pose, double weight) {
        this.pose = pose;
        this.weight = weight;
    }


    /**
     * Constructor.
     *
     * @param particle  A particle as template.
     */
    public Particle(Particle particle) {
        this.pose = particle.pose;
        this.weight = particle.weight;
    }



    /**
     * Supposed to paint a visual representation of the particle in a graphics context.
     *
     * @param g     The graphics context.
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        //TODO Implementation
    }



    /**
     * Returns the Pose of the particle.
     *
     * @return  The pose of the particle.
     */
    public Pose getPose() {
        return this.pose;
    }



    /**
     * Sets the pose of the particle.
     *
     * @param pose  The new pose for the particle.
     */
    public void setPose(Pose pose) {
        this.pose = pose;
    }



    /**
     * Returns the weight of the particle.
     *
     * @return  The weight of the particle.
     */
    public double getWeight() {
        return this.weight;
    }


    /**
     * Sets the weight of the particle.
     *
     * @param weight    The new weight for the particle.
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }


    /**
     * Returns a Point with the rounded x- and y-values of the particle (e.g. for painting the particle)
     *
     * @return  Point representing the particle.
     */
    public Point getIntegerPosition() {
        return new Point(Math.round(pose.getX()), Math.round(pose.getY()));
    }

}
