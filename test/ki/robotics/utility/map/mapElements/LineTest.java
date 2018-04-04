package ki.robotics.utility.map.mapElements;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.awt.geom.Point2D;

import static org.testng.Assert.*;

public class LineTest {
    private static double DELTA = 0.00001;

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

    @Test(dataProvider = "getLengthDataProvider")
    public void testGetLength(Point2D P1, Point2D P2, double length) {
        Line line = new Line(P1, P2);
        assertEquals(line.getLength(), length, DELTA);
    }

    @Test(dataProvider = "getSlopeDataProvider")
    public void testGetSlope(Point2D P1, Point2D P2, double slope) {
        Line line = new Line(P1, P2);
        assertEquals(line.getSlope(), slope, DELTA);
    }

    @Test(dataProvider = "getAngleDataProvider")
    public void testGetAngleTo(Point2D P1, Point2D P2, Point2D Q1, Point2D Q2, double angle) {
        Line lineOne = new Line(P1, P2);
        Line lineTwo = new Line(Q1, Q2);
        assertEquals(lineOne.getAngleTo(lineTwo), angle, DELTA);
    }

    @Test(dataProvider = "getCrossProductDataProvider")
    public void testGetCrossProductWith() {
    }

    @Test(dataProvider = "getIntersectionPointDataProvider")
    public void testGetIntersectionPointWith(Point2D P1, Point2D P2, Point2D Q1, Point2D Q2, Point2D intersection) {
        Line lineOne = new Line(P1, P2);
        Line lineTwo = new Line(Q1, Q2);
        Point2D intersectionPoint = lineOne.getIntersectionPointWith(lineTwo);
        assertEquals(intersectionPoint.getX(), intersection.getX(), DELTA);
        assertEquals(intersectionPoint.getY(), intersection.getY(), DELTA);
    }

    @Test
    public void testPaint() {
    }





    @DataProvider(name = "getLengthDataProvider")
    public Object[][] getLengthDataProvider() {
        return new Object[][]
                {
                        {new Point2D.Double(0, 0), new Point2D.Double(3, 4), 5},
                        {new Point2D.Double(1, 1), new Point2D.Double(4, 5), 5},
                        {new Point2D.Double(-1, -1), new Point2D.Double(2, 3), 5}
                };
    }

    @DataProvider(name = "getSlopeDataProvider")
    public Object[][] getSlopeDataProvider() {
        return new Object[][]
                {
                        {new Point2D.Double(0, 0), new Point2D.Double(10, 10), 1},
                        {new Point2D.Double(0, 0), new Point2D.Double(10, 5), 0.5},
                        {new Point2D.Double(0, 0), new Point2D.Double(5, 10), 2}
                };
    }

    @DataProvider(name = "getAngleDataProvider")
    public Object[][] getAngleDataProvider() {
        return new Object[][]
                {
                        {new Point2D.Double(0, 0), new Point2D.Double(10, 0),
                                new Point2D.Double(0, 0), new Point2D.Double(10, 10), 45 },
                        {new Point2D.Double(0, 0), new Point2D.Double(10, 0),
                                new Point2D.Double(0, 0), new Point2D.Double(0, 10), 90 }
                };
    }

    @DataProvider(name = "getCrossProductDataProvider")
    public Object[][] getCrossProductDataProvider() {
        return new Object[][]{};
    }

    @DataProvider(name = "getIntersectionPointDataProvider")
    public Object[][] getIntersectionPointDataProvider() {
        return new Object[][]
                {
                        {new Point2D.Double(0, 0), new Point2D.Double(10, 10),
                                new Point2D.Double(0, 10), new Point2D.Double(10, 0),
                                new Point2D.Double(5,5) },
                        {new Point2D.Double(5, 0), new Point2D.Double(-5, 10),
                                new Point2D.Double(-5, 0), new Point2D.Double(5, 10),
                                new Point2D.Double(0,5) },
                };
    }

}