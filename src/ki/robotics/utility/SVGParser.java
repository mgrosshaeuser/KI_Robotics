package ki.robotics.utility;

import ki.robotics.utility.svg_shapes.Shape_Circle;
import ki.robotics.utility.svg_shapes.Shape_Line;
import ki.robotics.utility.svg_shapes.Shape_Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;



/**
 * Utility for parsing SVG-Files.
 *
 * @version 1.0, 12/28/17
 */
public class SVGParser {

    public double graphicWidth;
    public double graphicHeight;

    private ArrayList<Shape_Circle> circles;
    private ArrayList<Shape_Line> lines;
    private ArrayList<Shape_Rectangle> rectangles;



    /**
     * Constructor.
     *
     * @param file  The SVG-File to parse.
     */
    public SVGParser(File file) {
        circles = new ArrayList<>();
        lines = new ArrayList<>();
        rectangles = new ArrayList<>();

        Element root = getDocumentElement(file);
        if (root != null) {
            this.graphicWidth = Double.parseDouble(root.getAttribute("width"));
            this.graphicHeight = Double.parseDouble(root.getAttribute("height"));
            parseNodes(root.getChildNodes());
        }
    }



    /**
     * Returns all circle-elements read from the given file.
     *
     * @return  List of all circles in the file.
     */
    public ArrayList<Shape_Circle> getCircles() {
        return circles;
    }



    /**
     * Returns all line-elements read from the given file.
     *
     * @return  List of all lines in the file.
     */
    public ArrayList<Shape_Line> getLines() {
        return lines;
    }



    /**
     * Returns all rect-elements read from the given file.
     *
     * @return  List of all rectangles in the file.
     */
    public ArrayList<Shape_Rectangle> getRectangles() {
        return rectangles;
    }



    /**
     * Returns the root-element of the document.
     *
     * @param file  A SVG-File.
     * @return      The Document-Element of the given file.
     */
    private Element getDocumentElement(File file) {
        Document document = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            document = builder.parse(file);
            document.normalize();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (document != null) {
            return document.getDocumentElement();
        } else {
            return null;
        }
    }



    /**
     * Parses the nodes from a given NodeList depending on whether these nodes represent groups, circles, ...
     * Group-Elements are parsed recursively.
     *
     * @param list  List of Nodes (e.g. from the document-root or a group.
     */
    private void parseNodes(NodeList list) {
        for (int i = 0  ;  i < list.getLength()  ;  i++) {
            Node node = list.item(i);
            switch (node.getNodeName()) {
                case "g":
                    parseNodes(node.getChildNodes());
                    break;
                case "circle":
                    circles.add(parseCircle((node)));
                    break;
                case "line":
                    lines.add(parseLine(node));
                    break;
                case "rect":
                    rectangles.add(parseRectangle(node));
                    break;
                default:
            }
        }
    }



    /**
     * Parses a Node that was identified as a line-element.
     *
     * @param node  A Node which holds a line.
     * @return      An instance of Shape_Line representing the line-element.
     */
    private Shape_Line parseLine(Node node) {
        Element line = (Element) node;
        int[] colors = getStrokeAndFillColor(line);
        double x1 = Double.parseDouble(line.getAttribute("x1"));
        double y1 = Double.parseDouble(line.getAttribute("y1"));
        double x2 = Double.parseDouble(line.getAttribute("x2"));
        double y2 = Double.parseDouble(line.getAttribute("y2"));
        return new Shape_Line(x1, y1, x2, y2, colors[0], colors[1]);
    }



    /**
     * Parses a Node that was identified as a circle-element.
     *
     * @param node  A Node which holds a circle.
     * @return      An instance of Shape_Circle representing the circle-element.
     */
    private Shape_Circle parseCircle(Node node) {
        Element circle = (Element) node;
        int[] colors = getStrokeAndFillColor(circle);
        double cx = Double.parseDouble(circle.getAttribute("cx"));
        double cy = Double.parseDouble(circle.getAttribute("cy"));
        double r = Double.parseDouble(circle.getAttribute("r"));
        return new Shape_Circle(cx, cy, r, colors[0], colors[1]);
    }



    /**
     * Parses a Node that was identified as a rect-element.
     *
     * @param node  A Node which holds a rectangle.
     * @return      An instance of Shape_Rectangle representing the rect-element.
     */
    private Shape_Rectangle parseRectangle(Node node) {
        Element rectangle = (Element) node;
        int[] colors = getStrokeAndFillColor(rectangle);
        double x = Double.parseDouble(rectangle.getAttribute("x"));
        double y = Double.parseDouble(rectangle.getAttribute("y"));
        double w = Double.parseDouble(rectangle.getAttribute("width"));
        double h = Double.parseDouble(rectangle.getAttribute("height"));
        return new Shape_Rectangle(x, y, w, h, colors[0], colors[1]);
    }



    /**
     * Returns an Array with stroke- and fill-color of a given Element.
     * Array[0] is stroke and Array[1] is fill.
     *
     * @param element   The Element of which to extract the color-information.
     * @return          The extracted colors in an array (i=0 is stroke, i=1 is fill).
     */
    private int[] getStrokeAndFillColor(Element element) {
        String strokeTmp = element.getAttribute("stroke");
        String fillTmp = element.getAttribute("fill");
        int stroke =  (strokeTmp.equals("")) ? Integer.parseInt(strokeTmp.substring(1), 16) : Color.WHITE.getRGB();
        int fill = (fillTmp.equals("")) ? Integer.parseInt(fillTmp.substring(1), 16) : Color.WHITE.getRGB();
        return new int[]{stroke, fill};
    }
}
