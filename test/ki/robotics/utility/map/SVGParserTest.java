package ki.robotics.utility.map;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;

import static org.testng.Assert.*;

public class SVGParserTest {

    private String testFile = "room.svg";
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