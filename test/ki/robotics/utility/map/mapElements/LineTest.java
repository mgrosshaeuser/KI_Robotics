package ki.robotics.utility.map.mapElements;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.awt.geom.Point2D;

import static org.testng.Assert.*;

public class LineTest {

    @BeforeMethod
    public void setUp() {
    }

    @AfterMethod
    public void tearDown() {
    }

    @Test
    public void testGetStroke() {
    }

    @Test
    public void testSetStroke() {
    }

    @Test
    public void testGetLength() {
        Line line = new Line(new Point2D.Double(0,0), new Point2D.Double(20,40));
        double length = line.getLength();
        assertEquals(length,44.7213,0.0001);
    }

    @Test
    public void testGetSlope() {
        Line line = new Line(new Point2D.Double(0,0), new Point2D.Double(20,40));
        assertEquals(line.getSlope(), 2, 0.0001);
    }

    @Test
    public void testGetAngleTo() {
        Line lineOne = new Line(new Point2D.Double(0,0), new Point2D.Double(100,0));
        Line lineTwo = new Line(new Point2D.Double(0,0), new Point2D.Double(100,100));
        double angle = lineOne.getAngleTo(lineTwo);
        assertEquals(angle, 45,0.0001);
    }

    @Test
    public void testGetCrossProductWith() {
    }

    @Test
    public void testGetIntersectionPointWith() {
        Line lineOne = new Line(new Point2D.Double(0,0), new Point2D.Double(100,100));
        Line lineTwo = new Line(new Point2D.Double(0,100), new Point2D.Double(100,0));
        Point2D intersectionPoint = lineOne.getIntersectionPointWith(lineTwo);
        assertEquals(intersectionPoint.getX(), 50, 0.0001);
        assertEquals(intersectionPoint.getY(), 50, 0.0001);
    }

    @Test
    public void testPaint() {
    }
}