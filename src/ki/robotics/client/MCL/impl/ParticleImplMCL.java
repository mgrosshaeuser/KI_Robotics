package ki.robotics.client.MCL.impl;

import ki.robotics.client.MCL.Particle;
import ki.robotics.utility.map.Map;
import lejos.robotics.navigation.Pose;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.Serializable;


/**
 * Implementation of a serializable particle for monte-carlo-localization.
 */
public class ParticleImplMCL implements Particle, Comparable<ParticleImplMCL>, Serializable {
    private transient Map map;
    private transient Pose pose;
    private float[] poseSerializable;
    private int sensorHeadPosition;
    private float weight;
    private Color color;


    /**
     * Constructor.
     *
     * @param pose      The particle-pose
     * @param map       The map in which the particle lies
     * @param weight    The weight of the particle
     * @param color     The color of the particle
     */
    public ParticleImplMCL(Pose pose, Map map, float weight, Color color) {
        this.pose = pose;
        this.poseSerializable = new float[]{pose.getX(), pose.getY(), pose.getHeading()};
        this.map = map;
        this.weight = weight;
        this.color = color;
    }


    /**
     * Returns a deep copy of this particle.
     *
     * @return  A deep copy of this particle.
     */
    @Override
    public ParticleImplMCL getClone() {
        Pose pose = new Pose(this.pose.getX(), this.pose.getY(), this.pose.getHeading());
        ParticleImplMCL particle = new ParticleImplMCL(pose, this.map, this.weight, this.color);
        particle.sensorHeadPosition = this.sensorHeadPosition;
        return particle;
    }


    /**
     * Returns the pose of the particle as object of type Pose. If initially null (after deserialization)
     * a new pose-object is first created an assigned, using the values from poseSerializable (float[]).
     *
     * @return  The pose of the particle.
     */
    @Override
    public Pose getPose() {
        if (this.pose == null) {
            this.pose = new Pose(poseSerializable[0], poseSerializable[1], poseSerializable[2]);
        }
        return this.pose;
    }


    /**
     * Moves the particle over the given distance. The return value is the value the particle was moved, which
     * will be less than the parameter-value if an obstacle prohibits further movement.
     *
     * @param distance The distance to move the particle
     * @return  The actual distance the particle was moved.
     */
    @Override
    public double move(double distance) {
        float bumper = 5f;
        int temp = sensorHeadPosition;
        if (distance > 0) {
            sensorHeadPosition = 0;
            distance = (measureDistance() >= distance + bumper) ? distance : measureDistance() - bumper;
        } else {
            sensorHeadPosition = 180;
            distance = (measureDistance() >= Math.abs(distance) + bumper) ? distance : -(measureDistance() - bumper);
        }

        float dx = (float) (Math.cos(Math.toRadians(pose.getHeading())) * distance);
        float dy = (float) (Math.sin(Math.toRadians(pose.getHeading())) * distance) * -1;

        pose.translate(dx, dy);
        upDatePoseSerializable();
        sensorHeadPosition = temp;
        return distance;
    }


    /**
     * Turns the particle.
     *
     * @param degrees The degrees to turn the particle.
     */
    @Override
    public void turn(int degrees) {
        float n = (pose.getHeading() + degrees) % 360;
        if (n < 0) {
            n += 360;
        }
        pose.setHeading(Math.abs(n));
        upDatePoseSerializable();
    }


    /**
     * Returns the weight of the particle.
     *
     * @return The weight of the particle
     */
    @Override
    public float getWeight() {
        return this.weight;
    }


    /**
     * Sets the weight of the particle.
     *
     * @param weight The new weight of the particle
     */
    @Override
    public void setWeight(float weight) {
        this.weight = weight;
    }


    /**
     * Returns the color of the particle.
     *
     * @return The color of the particle
     */
    @Override
    public Color getColor() {
        return this.color;
    }


    /**
     * Sets the color of the particle.
     *
     * @param color The new color for the particle
     */
    @Override
    public void setColor(Color color) {
        this.color = color;
    }


    /**
     * Updates the serializable pose (float[]) using the values from the particle-pose (of type Pose).
     */
    private void upDatePoseSerializable() {
        this.poseSerializable[0] = this.pose.getX();
        this.poseSerializable[1] = this.pose.getY();
        this.poseSerializable[2] = this.pose.getHeading();
    }


    /**
     * Returns a boolean value indication whether the particle lies outside (true) or within (false) the
     * operating-range given by the map.
     *
     * @return  A boolean value indicating whether the particle lies outside the maps operating-range
     */
    @Override
    public boolean isOutOfMapOperatingRange() {
        float xPos = this.pose.getX();
        float yPos = this.pose.getY();

        if (map.getBaseLine() != null) {
            Line2D baseLine = map.getBaseLine();
            int acceptableDistanceFromBaseLine = 10;
            return baseLine.ptLineDist(xPos, yPos) > acceptableDistanceFromBaseLine;
        }

        if (map.getOperatingRange() != null) {
            Polygon boundaries = this.map.getOperatingRange();
            return !boundaries.contains(xPos, yPos);
        }

        return false;
    }


    /**
     * Compares this particle with the specified particle.
     * Only the distinction between equality and inequality is of interest.
     *
     *
     * @param o The other particle
     * @return numerical value indicating equality (0) or inequality (!=0)
     */
    @Override
    public int compareTo(ParticleImplMCL o) {
        return Float.compare(this.weight, o.weight);
    }


    /**
     * Paints the particle in a given graphical context, using current GUI-parameters
     *
     * @param g         The graphical context
     * @param particleDiameter  The particle-diameter
     * @param scaleFactor   The scale-factor (of map in window)
     * @param xOffset      The x-offset to center the map vertically
     * @param yOffset       The y-offset to center the map horizontally.
     */
    @Override
    public void paint(Graphics g, int particleDiameter, int scaleFactor, int xOffset, int yOffset) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(this.color);
        g2d.fillOval(
                Math.round(this.getPose().getX() - (particleDiameter / 2)) * scaleFactor + xOffset,
                Math.round(this.getPose().getY() - (particleDiameter / 2)) * scaleFactor + yOffset,
                particleDiameter * scaleFactor,
                particleDiameter * scaleFactor
        );

        g2d.setColor(Color.BLUE);
        g2d.fillArc(
                (Math.round((getPose().getX() - particleDiameter / 2)) * scaleFactor) + xOffset,
                (Math.round((getPose().getY() - particleDiameter / 2)) * scaleFactor) + yOffset,
                Math.round(particleDiameter * scaleFactor),
                Math.round(particleDiameter * scaleFactor),
                Math.round(sensorHeadPosition + pose.getHeading() - 10),
                20
        );
    }


    /**
     * Simulates the corresponding robot-action of measuring the distances to the left, ahead and to the right.
     * Returns a double[] holding this values in this order.
     *
     * @return  A double[] holding distance to the left, ahead and to the right.
     */
    @Override
    public double[] ultrasonicThreeWayScan() {
        int temp = sensorHeadPosition;
        sensorHeadPosition = 90;
        double a = measureDistance();
        sensorHeadPosition = 0;
        double b = measureDistance();
        sensorHeadPosition = -90;
        double c = measureDistance();
        sensorHeadPosition = temp;
        return new double[]{a, b, c};
    }


    /**
     * Simulates distance-measurement by performing the necessary calculations within the map.
     *
     * @return  The measured distance towards the direction of the sensor-head.
     */
    private double measureDistance() {
        Point2D currentLocation = new Point2D.Double(pose.getX(), pose.getY());
        double viewingDirection = 360 - pose.getHeading() - sensorHeadPosition;
        return map.getDistanceToNearestObstacle(currentLocation, viewingDirection);
    }


    /**
     * Returns a simulated camera general-query.
     *
     * @return  A simulated camera general-query
     */
    @Override
    public int[] cameraGeneralQuery() {
        return map.getGeneralCameraQuery(pose.getX(), pose.getY(), 360 - pose.getHeading());
    }


    /**
     * Returns a simulated camera-signature query for the given signature.
     *
     * @param signature the signature to query for
     * @return  A simulated camera-signature query for the given signature
     */
    @Override
    public int[] cameraSignatureQuery(int signature) {
        return map.getCameraSignatureQuery(pose.getX(), pose.getY(), 360-pose.getHeading(), signature);
    }


    /**
     * Returns a simulated camera-all-signatures-query.
     *
     * @return  A simulated camera-all-signatures-query
     */
    @Override
    public int[][] cameraAllSignaturesQuery() {
        int numberOfAvailableSignatures = 7;
        int[][] result = new int[numberOfAvailableSignatures][];
        for (int i = 0   ;   i < numberOfAvailableSignatures   ;   i++) {
            result[i] = map.getCameraSignatureQuery(pose.getX(), pose.getY(), 360 - pose.getHeading(), i);
        }
        return result;
    }


    /**
     * Returns a simulated camera-color-code-query for the given color-code.
     *
     * @param color The color-code to query for.
     * @return  A simulated camera-color-code-query for the given color-code
     */
    @Override
    public int[] cameraColorCodeQuery(int color) {
        return map.getCameraColorCodeQuery(pose.getX(), pose.getY(), 360 - pose.getHeading(), color);
    }


    /**
     * Returns a simulated camera-angle-query.
     * @return  A simulated camera-angle-query
     */
    @Override
    public int cameraAngleQuery() {
        return map.getCameraAngleQuery(pose.getX(), pose.getY(), 360 - pose.getHeading());
    }
}
