package ki.robotics.utility.map;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

public class SVGParserTest {

    private String testFile = "maps/room.svg";
    private SVGParser parser;

    @BeforeMethod
    public void setUp() {
        parser = new SVGParser(new File(getClass().getClassLoader().getResource(testFile).getFile()));
    }

    @AfterMethod
    public void tearDown() {
    }



    @Test
    public void testGetMap() {
    }

    @Test
    public void testGetDocumentElement() {
    }

    @Test
    public void testParseNodes() {
    }

    @Test
    public void testParseWalls() {
    }

    @Test
    public void testParseLandmarks() {
    }

    @Test
    public void testParseFloorTiles() {
    }

    @Test
    public void testGetStrokeColor() {
    }

    @Test
    public void testGetFillColor() {
    }

    @Test
    public void testParseColorAttribute() {
    }

    @Test
    public void testParseDoubleAttribute() {
    }
}