package ki.robotics.utility.map;

import ki.robotics.utility.map.mapElements.Circle;
import ki.robotics.utility.map.mapElements.Rectangle;
import ki.robotics.utility.map.mapElements.Line;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;



/**
 *
 */
class SVGParser {
    private final ArrayList<Circle> circles = new ArrayList<>();
    private final ArrayList<Line> lines = new ArrayList<>();
    private final ArrayList<Rectangle> rectangles = new ArrayList<>();

    private MapImpl map;



    /**
     * Constructs and initializes a parser for the specified SVG-file.
     *
     * @param file the specified SVG-file
     */
    SVGParser(File file) {
        double mapHeight = 0;
        double mapWidth = 0;

        Element documentRoot = getDocumentRoot(file);
        if (documentRoot != null) {
            mapWidth = parseFloatingPointAttribute("width", documentRoot);
            mapHeight = parseFloatingPointAttribute("height", documentRoot);
            parseNodes(documentRoot.getChildNodes());
        }

        map = new MapImpl(lines, rectangles, circles);
        map.setHeight(mapHeight);
        map.setWidth(mapWidth);
    }


    MapImpl getMap() {
        return this.map;
    }



    /**
     * Returns the root-element of the specified SVG-file.
     *
     * @param file the specified SVG-file
     * @return the DocumentElement of the specified file.
     */
    private Element getDocumentRoot(File file) {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.normalize();
            return document.getDocumentElement();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return null;
        }
    }



    /**
     * Redirects Nodes from the specified NodeList to custom parsing-methods, depending on
     * whether these nodes represent circles, lines or rectangles.
     * Group-Elements are treated recursively.
     *
     * @param list the specified NodeList
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
     * Returns a Line created and initialized with the values from the specified Node.
     *
     * @param node the specified Node
     * @return a Line-representation of the specified Node
     */
    private Line parseLine(Node node) {
        Element lineElement = (Element) node;
        double startX = parseFloatingPointAttribute("x1", lineElement);
        double startY = parseFloatingPointAttribute("y1", lineElement);
        double endX = parseFloatingPointAttribute("x2", lineElement);
        double endY = parseFloatingPointAttribute("y2", lineElement);
        Point2D start = new Point2D.Double(startX, startY);
        Point2D end = new Point2D.Double(endX, endY);
        Line line = new Line (start, end);
        line.setStroke(getStrokeColorOf(lineElement));
        return line;
    }



    /**
     * Returns a Circle created and initialized with the values from the specified Node.
     *
     * @param node the specified Node
     * @return a Circle-representation of the specified Node
     */
    private Circle parseCircle(Node node) {
        Element circleElement = (Element) node;
        double centerX = parseFloatingPointAttribute("cx", circleElement);
        double centerY = parseFloatingPointAttribute("cy", circleElement);
        double radius = parseFloatingPointAttribute("r", circleElement);
        Point2D.Double center = new Point2D.Double(centerX, centerY);
        Circle circle = new Circle(center, radius);
        circle.setStroke(getStrokeColorOf(circleElement));
        circle.setFill(getFillColorOf(circleElement));
        circle.setId(circleElement.getAttribute("id"));
        return circle;
    }



    /**
     * Returns a Rectangle created and initialized with the values from the specified Node.
     *
     * @param node the specified Node
     * @return a Rectangle-representation of the specified Node
     */
    private Rectangle parseRectangle(Node node) {
        Element rectangleElement = (Element) node;
        double x = parseFloatingPointAttribute("x", rectangleElement);
        double y = parseFloatingPointAttribute("y", rectangleElement);
        double width = parseFloatingPointAttribute("width", rectangleElement);
        double height = parseFloatingPointAttribute("height", rectangleElement);
        Rectangle rectangle = new Rectangle(x, y, width, height);
        rectangle.setStroke(getStrokeColorOf(rectangleElement));
        rectangle.setFill(getFillColorOf(rectangleElement));
        return rectangle;
    }



    /**
     * Returns the stroke color of the specified Element.
     *
     * @param element the specified Element
     * @return the stroke color of the specified Element.
     */
    private int getStrokeColorOf(Element element) {
        return parseColorAttribute("stroke", element);
    }



    /**
     * Returns the fill color of the specified Element.
     *
     * @param element the specified Element
     * @return the fill color of the specified Element.
     */
    private int getFillColorOf(Element element) {
        return parseColorAttribute("fill", element);
    }



    /**
     * Returns the specified color-attribute from the specified Element.
     *
     * @param attribute the color-attribute as String (e.g. stroke, fill, ...)
     * @param element the specified Element
     * @return the color as integer
     */
    private int parseColorAttribute(String attribute, Element element) {
        String value = element.getAttribute(attribute);
        try {
            return Integer.parseInt(value.substring(1), 16);
        } catch (NumberFormatException  | StringIndexOutOfBoundsException e) {
            return Color.WHITE.getRGB();
        }
    }



    /**
     * Returns the specified floating-point-attribute from the specified Element in double precision.
     *
     * @param attribute the floating-point-attribute as String (e.g. width, x, y, ...)
     * @param element the specified Element
     * @return the attribute-value in double precision
     */
    private double parseFloatingPointAttribute(String attribute, Element element) {
        try {
            return Double.parseDouble(element.getAttribute(attribute));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
