package ki.robotics.utility.map;

import ki.robotics.utility.svg.svg_Circle;

import java.awt.*;

class Landmark {
    private final svg_Circle circle;

    private Landmark(double x, double y, double diameter, String id, int stroke, int fill) {
        this.circle = new svg_Circle(x, y, diameter, id, stroke, fill);
    }

    public Landmark (svg_Circle circle) {
        this(circle.getX(), circle.getY(), circle.getDiameter(), circle.getId(), circle.getStroke(), circle.getFill());
    }

    public Point getCenter() {
        return new Point(
                (int) Math.round(circle.getX()),
                (int) Math.round(circle.getY())
        );
    }

    public void paint(Graphics g, int scaleFactor, int xOffset, int yOffset) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(this.circle.getFill()));
        int diameter = this.circle.getDiameterAsInt();
        int radius = diameter / 2;
        g2d.fillOval(
                (this.circle.getXAsInt() - radius) * scaleFactor + xOffset,
                (this.circle.getYAsInt() - radius) * scaleFactor + yOffset,
                diameter * scaleFactor,
                diameter * scaleFactor
                );
    }
}
