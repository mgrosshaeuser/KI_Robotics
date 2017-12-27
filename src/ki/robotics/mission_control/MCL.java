package ki.robotics.mission_control;

import ki.robotics.datastructures.Map;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;


/**
 * Monte-Carlo-Localization including a GUI for presentation of results.
 *
 * @version 1.0, 12/27/17
 */
public class MCL extends JFrame {
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 1000;

    private int scaleFactor = 1;
    private int xOffset = 0;
    private int yOffset = 0;

    private Map map;
    private MapOverlay mapOverlay = new MapOverlay();



    /**
     * Constructor.
     */
    public MCL() {
        this.map = new Map(new File(getClass().getClassLoader().getResource("map.svg").getFile()));

        this.setTitle("Monte Carlo Localization");
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        add(mapOverlay);
        this.setVisible(true);
    }



    /**
     * Supposed to handle the interaction with the robot.
     *
     * @param responses     Response from the robot in answer to request.
     * @return              An initial or continuative request.
     */
    public String execute(ArrayList<String> responses) {
        if (responses == null) {
            return "MDST, BTNR 90, MDST, BTRF 70, MDST, STNL 90, MDST";
        } else {
            for (String s : responses) {
                System.out.println(s);
            }
            return "DCNT";
        }
    }



    /**
     * Paints the JFrame after updating the scale-factor and offsets to cope with resizing of the window.
     * @param g     The graphical context.
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        updateVisualParameters();
        Graphics2D g2d = (Graphics2D) g;
    }



    /**
     * (Re)Calculates the visual parameters, including
     * - the scale-factor to use the available space at its best
     * - the x- and y-offsets to center the simulation in the window.
     */
    private void updateVisualParameters() {
        int maxX = map.getRequiredMinWidth();
        int maxY = map.getRequiredMinHeight();

        double width = mapOverlay.getVisibleRect().getWidth();
        double height = mapOverlay.getVisibleRect().getHeight();

        double scaleX = width / maxX;
        double scaleY = height / maxY;

        double limitingFactor = scaleX > scaleY ? scaleY : scaleX;

        scaleFactor = (int) Math.abs(Math.floor(limitingFactor));

        xOffset = ((int) Math.abs(width) - (maxX * scaleFactor)) / 2;
        yOffset = ((int) Math.abs(height) - (maxY * scaleFactor)) / 2;
    }



    /**
     * A JPanel for showing the map.
     */
    private class MapOverlay extends JPanel {
        /**
         * Paints the map and the rover. Currently the wall-color is neglected.
         * @param g     The graphics-context to paint on.
         */
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            this.setBackground(Color.BLACK);
            if (map != null) map.paint(g, scaleFactor, xOffset, yOffset);

        }

    }


}
