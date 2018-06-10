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
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;



/**
 *
 */
class SVGParser {
    private ElementParser elementParser;

    private MapImpl map;
    private double mapHeight;
    private double mapWidth;



    /**
     * Constructor.
     *
     * @param file the specified SVG-file
     */
    SVGParser(File file) {
        Element documentRoot = getDocumentRoot(file);
        parseDocumentRoot(documentRoot);
        this.map = createMap(file.getName());
    }


    /**
     * Returns the Map created from the svg-file.
     *
     * @return  The Map created from the svg-file
     */
    MapImpl getMap() {
        return this.map;
    }


    /**
     * Returns a new map, created with the parsed data from the svg-file.
     *
     * @param mapKey    A key (name) for the new map
     * @return  The new map.
     */
    private MapImpl createMap(String mapKey) {
        MapImpl map = new MapImpl(mapKey, elementParser.getLines(), elementParser.getRectangles(), elementParser.getCircles());
        map.setHeight(mapHeight);
        map.setWidth(mapWidth);
        if (elementParser.getBaseLine() != null) {
            map.setBaseLine(elementParser.getBaseLine());
        } else {
            map.setOperatingRange(getOperatingRange(mapWidth, mapHeight));
        }
        return map;
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
     * Parses the document-element from the svg-file.
     *
     * @param documentRoot  The document-element from the svg-file
     */
    private void parseDocumentRoot(Element documentRoot) {
        if (documentRoot != null) {
            this.elementParser = new ElementParser();
            this.mapWidth = elementParser.parseFloatingPointAttribute("width", documentRoot);
            this.mapHeight = elementParser.parseFloatingPointAttribute("height", documentRoot);
            elementParser.parseNodes(documentRoot.getChildNodes());
        }
    }


    /**
     * Returns an operating-range-polygon for the map. That is the outer perimeter of the region where
     * the robot operates and particles are supposed to be distributed.
     *
     * @param mapWidth  The width of the map
     * @param mapHeight The height of the map
     * @return  A Polygon enclosing the robots operating-range
     */
    private Polygon getOperatingRange(double mapWidth, double mapHeight) {
        Point2D minPoint = new Point2D.Double(mapWidth, mapHeight);
        Point2D maxPoint = new Point2D.Double(0,0);
        for (Line l : elementParser.getLines()) {
            minPoint = getCoordinateMinimum(minPoint, l.getP1());
            minPoint = getCoordinateMinimum(minPoint, l.getP2());
            maxPoint = getCoordinateMaximum(maxPoint, l.getP1());
            maxPoint = getCoordinateMaximum(maxPoint, l.getP2());
        }
        return createOperatingRangePolygon(minPoint, maxPoint);
    }


    /**
     * Returns a new point representing the MAX-coordinates for the operating-range based on the currently known
     * min-coordinates and a new point under evaluation.
     *
     * @param currentMinPoint   The min-coordinate (upper-left corner) so far discovered
     * @param p2        A coordinate-point possibly leading to smaller min-coordinates
     * @return  The new min-coordinates
     */
    private Point2D getCoordinateMinimum(Point2D currentMinPoint, Point2D p2) {
        double minX = (currentMinPoint.getX() > p2.getX()) ? p2.getX() : currentMinPoint.getX();
        double minY = (currentMinPoint.getY() > p2.getY()) ? p2.getY() : currentMinPoint.getY();
        return new Point2D.Double(minX, minY);
    }


    /**
     * Returns a new point representing the MAX-coordinates for the operating-range based on the currently known
     * min-coordinates and a new point under evaluation.
     *
     * @param currentMaxPoint   The max-coordinate (lower-right corner) so far discovered
     * @param p2    A coordinate-point possibly leading to larger max-coordinates
     * @return  The new max-coordinates
     */
    private Point2D getCoordinateMaximum(Point2D currentMaxPoint, Point2D p2) {
        double maxX = (currentMaxPoint.getX() < p2.getX()) ? p2.getX() : currentMaxPoint.getX();
        double maxY = (currentMaxPoint.getY() < p2.getY()) ? p2.getY() : currentMaxPoint.getY();
        return new Point2D.Double(maxX, maxY);
    }


    /**
     * Returns a Polygon representing the operating-range of the robot.
     * A Polygon (instead of an Rectangle) was chosen, because future versions of this class might employ more
     * sophisticated means to enclose the area of operation.
     *
     * @param minPoint  The coordinate of the upper-left corner of the operating-range
     * @param maxPoint  The coordinate of the lower-right corner of the operating-range
     * @return  The operating-range as Polygon
     */
    private Polygon createOperatingRangePolygon(Point2D minPoint, Point2D maxPoint) {
        int minX = (int) Math.round(minPoint.getX());
        int minY = (int) Math.round(minPoint.getY());
        int maxX = (int) Math.round(maxPoint.getX());
        int maxY = (int) Math.round(maxPoint.getY());
        int xPoints[] = new int[]{minX, maxX, maxX, minX};
        int yPoints[] = new int[]{minY, minY, maxY, maxY};
        return new Polygon(xPoints, yPoints, 4);
    }





    /**
     * Inner class for parsing the document-element from the svg-file.
     */
    private class ElementParser {
        private final ArrayList<Circle> circles = new ArrayList<>();
        private final ArrayList<Line> lines = new ArrayList<>();
        private final ArrayList<Rectangle> rectangles = new ArrayList<>();
        private Line2D baseLine;


        ArrayList<Circle> getCircles() {
            return circles;
        }

        ArrayList<Line> getLines() {
            return lines;
        }

        ArrayList<Rectangle> getRectangles() {
            return rectangles;
        }

        Line2D getBaseLine() {
            return baseLine;
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
                    case "baseline":
                        this.baseLine = parseLine(node);
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

}
