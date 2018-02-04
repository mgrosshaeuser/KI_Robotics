package ki.robotics.robot;

import ki.robotics.common.ExtJPanel;
import ki.robotics.common.MapPanel;
import ki.robotics.server.BotServer;
import ki.robotics.utility.crisp.Instruction;
import ki.robotics.utility.map.Map;
import ki.robotics.utility.map.MapProvider;
import lejos.robotics.navigation.Pose;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

/**
 * Implementation of the Robot-Interface through the VirtualRobotModel-class.
 * Simulates and displays a robot.
 *
 * @version 1.0 01/02/18
 */
public class RoverSimulation extends VirtualRobotModel {
    private static final String DEFAULT_SELECTED_MAP = MapProvider.MAP_KEY_HOUSES;
    private static final int ANIMATION_INTER_FRAME_TIME = 50;

    private final BotServer botServer;
    private final SimulationDisplay window;

    /**
     * Constructor.
     *
     * @param botServer     An instance of the BotServer for communication.
     */
    public RoverSimulation(BotServer botServer) {
        this.pose = new Pose();
        this.pose.setLocation(10,10);
        this.pose.setHeading(0);
        this.botServer = botServer;
        this.map = MapProvider.getInstance().getMap(DEFAULT_SELECTED_MAP);
        this.window = new SimulationDisplay(map);
        window.repaint();
    }


    /**
     * Translates the simulated robot over given distances in x- and y-direction.
     *
     * @param dx    Translation-distance along the x-axis.
     * @param dy    Translation-distance along the y-axis.
     */
    @Override
    void translate(float dx, float dy) {
        float distance = (float) Math.sqrt(Math.pow(dx,2) + Math.pow(dy,2));
        float xStep = dx / distance;
        float yStep = dy / distance;
        for (int i = 0  ;  i < distance  ;  i++) {
            pose.translate(xStep, yStep);
            window.repaint();
            pause(ANIMATION_INTER_FRAME_TIME);
        }
    }


    /**
     * Turns the heading over the given degrees.
     *
     * @param degrees   Degrees to turn the heading.
     */
    @Override
    void turnFull(int degrees) {
        if (degrees > 0) {
            for (int i = 0  ;  i < degrees  ;  i++) {
                pose.setHeading((Math.round(pose.getHeading() + 1)) % 360);
                pause(ANIMATION_INTER_FRAME_TIME / 10);
            }
        } else {
            int diff = Math.round(pose.getHeading()) - Math.abs(degrees);
            if (diff >= 0) {
                for (int i = 0  ;  i > degrees  ;  i--) {
                    pose.setHeading(Math.round(pose.getHeading() - 1));
                    pause (ANIMATION_INTER_FRAME_TIME / 10);
                }
            } else {
                while (pose.getHeading() > 0) {
                    pose.setHeading(Math.round(pose.getHeading() -1));
                    pause (ANIMATION_INTER_FRAME_TIME / 10);
                }
                pose.setHeading(0);
                for (int i = 0  ;  i >= diff  ;  i--) {
                    pose.setHeading(360 + i);
                    pause (ANIMATION_INTER_FRAME_TIME/10);
                }
            }
        }
    }


    /**
     * Turns the position of the sensor-head carrying the distance-sensor.
     *
     * @param position  The new position of the sensor-head.
     */
    @Override
    void turnSensor(int position) {
        if (position > sensorHeadPosition) {
            while ( position > sensorHeadPosition) {
                sensorHeadPosition++;
                pause(ANIMATION_INTER_FRAME_TIME / 20);
            }
        } else {
            while (position < sensorHeadPosition) {
                sensorHeadPosition--;
                pause(ANIMATION_INTER_FRAME_TIME / 20);
            }
        }
    }


    /**
     * Stops the current (gui-)thread to make the movement of the simulated robot observable.
     *
     * @param ms    Delay between two updates of the display.
     */
    private void pause(int ms) {
        window.repaint();
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean shutdown() {
        return false;
    }

    @Override
    public boolean disconnect() {
        return false;
    }

    @Override
    public boolean handleUnsupportedInstruction(Instruction instruction) {
        System.out.println("Unknows Instruction: >>" + instruction.getMnemonic());
        return true;
    }

    @Override
    public boolean setStayOnWhiteLine(boolean stayOnWhiteLine) {
        //no color sensor in simulation
        return true;
    }


    /**
     * The simulation-gui including the display and a control-panel.
     */
    private class SimulationDisplay extends JFrame {
        private static final int WINDOW_WIDTH = 800;
        private static final int WINDOW_HEIGHT = 800;

        private final ControlPanel controlPanel;
        private final MapOverlay mapOverlay;


        /**
         * Constructor.
         *
         * @param map   The map in use.
         */
        SimulationDisplay(Map map) {
            this.setTitle("Simulated Rover");
            this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
            this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            this.setLayout(new BorderLayout());
            this.controlPanel = new ControlPanel(this);
            add(controlPanel, BorderLayout.PAGE_START);
            this.mapOverlay = new MapOverlay(this, map);
            add(mapOverlay, BorderLayout.CENTER);
            this.setVisible(true);
        }

    }




    /**
     * The control-panel for the simulation.
     */
    private class ControlPanel extends JPanel {
        private final SimulationDisplay parent;
        private final MapProvider mapProvider;

        private JComboBox maps;
        private JButton lock;
        private boolean isLocked = false;


        private final String[] mapkeys;


        /**
         * Constructor.
         *
         * @param parent The parent (SimulationDisplay).
         */
        ControlPanel(SimulationDisplay parent) {
            this.parent = parent;
            this.mapProvider = MapProvider.getInstance();
            this.setLayout(new FlowLayout());
            this.mapkeys = mapProvider.getMapKeys();
            addMapSelectionToUI();
            addBotInitPositionChooseToUI();
            addControlElementsToUI();
        }


        /**
         * Adds gui-elements for map-selection to the gui.
         */
        private void addMapSelectionToUI() {
            JPanel mapSelectionContainer = new JPanel();
            mapSelectionContainer.setLayout(new BorderLayout());
            JLabel mapLabel = new JLabel("Select Map");

            maps = new JComboBox(mapkeys);
            maps.setSelectedIndex(0);
            maps.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (parent.mapOverlay.isModifiable()) {
                        String mapkey = mapkeys[maps.getSelectedIndex()];
                        parent.mapOverlay.setNewMap(mapProvider.getMap(mapkey));
                        map = mapProvider.getMap(mapkey);
                        parent.repaint();
                    }
                }
            });

            mapSelectionContainer.add(mapLabel, BorderLayout.PAGE_START);
            mapSelectionContainer.add(maps, BorderLayout.CENTER);
            add(mapSelectionContainer);
        }


        /**
         * Adds gui-elements to specify the initial heading of the simulated robot.
         */
        private void addBotInitPositionChooseToUI() {
            int currentHeading = Math.round(pose.getHeading());
            final JLabel headingLabel = new JLabel(String.valueOf("Heading: " + currentHeading));
            final JSlider headingSlider = new JSlider(0,359,currentHeading);
            headingSlider.setMinorTickSpacing(5);
            headingSlider.setMajorTickSpacing(45);
            headingSlider.setPaintTicks(true);
            headingSlider.setSnapToTicks(true);

            headingSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (parent.mapOverlay.isModifiable()) {
                        pose.setHeading(headingSlider.getValue());
                        headingLabel.setText("Heading: " + pose.getHeading());
                        window.repaint();
                    }
                }
            });

            ExtJPanel initContainer = new ExtJPanel();
            initContainer.setLayout(new GridLayout(2,1));

            initContainer.addAll(headingLabel, headingSlider);
            add(initContainer);
        }



        /**
         * Adds control-elements for locking and unlocking the control-panel.
         */
        private void addControlElementsToUI() {
            JPanel controlContainer = new JPanel();
            controlContainer.setLayout(new BorderLayout());

            lock = new JButton("Lock");
            lock.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isLocked) {
                        lock.setText("Lock");
                        isLocked = false;
                        parent.mapOverlay.setModifiable(true);
                    } else {
                        lock.setText("Unlock");
                        isLocked = true;
                        parent.mapOverlay.setModifiable(false);
                    }

                }
            });

            controlContainer.add(lock, BorderLayout.PAGE_START);
            add(controlContainer);
        }

    }




    /**
     * Specialization of the MapOverlay for the simulation.
     */
    private class MapOverlay extends MapPanel {
        private SimulationDisplay parent;

        private final Rover rover;

        /**
         * Constructor.
         *
         * @param parent    The parent (SimulationDisplay).
         * @param map       The map in use.
         */
        MapOverlay(SimulationDisplay parent, Map map) {
            super(parent, map);

            this.rover = new Rover(this);
            this.grabFocus();
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    Rectangle botOutline = rover.getBounds(getScaleFactor(), getxOffset(), getyOffset());
                    if (isModifiable()) {
                        new CoordinateInput((MapOverlay)e.getSource());
                        repaint();
                    }
                }
            });
           this.addMouseMotionListener(new MouseMotionAdapter() {
               @Override
               public void mouseDragged(MouseEvent e) {
                   super.mouseDragged(e);
                   if (isModifiable() &&  rover.getBounds(getScaleFactor(), getxOffset(), getyOffset()).contains(e.getX(), e.getY())) {
                       int xTemp = (e.getX() - getxOffset()) / getScaleFactor();
                       int yTemp = (e.getY() - getyOffset()) / getScaleFactor();
                       pose.setLocation(xTemp, yTemp);
                       repaint();
                   }
               }
           });
        }


        /**
         * Paints the simulated robot. The map is already painted by the super-class.
         *
         * @param g     The graphical context.
         */
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (rover != null) {
                rover.paint(g);
            }
        }


        /**
         * Allows precise specification of the robot-coordinates.
         */
        private class CoordinateInput extends JFrame {
            private final JTextField xInput = new JTextField(10);
            private final JTextField yInput = new JTextField(10);
            private final JButton okButton = new JButton("OK");
            private MapOverlay parent;

            /**
             * Constructor.
             */
            CoordinateInput(MapOverlay parent) {
                this.setTitle("Insert Robot-Coordinates");
                this.parent = parent;
                this.setLocationRelativeTo(parent);
                this.setSize(250,100);
                this.setLayout(new FlowLayout());
                this.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
                this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                initializeElements();
                this.setVisible(true);
            }


            /**
             * Adds the gui-elements to the window.
             */
            private void initializeElements() {
                JPanel window = new JPanel();
                window.setLayout(new BorderLayout());

                ExtJPanel panel = new ExtJPanel();
                panel.setLayout(new GridLayout(2,2));

                JLabel xLabel = new JLabel("x-Value: ");
                xLabel.setLabelFor(xInput);
                JLabel yLabel = new JLabel("y-Value: ");
                yLabel.setLabelFor(yInput);

                xInput.setText("" + Math.round(pose.getX()));
                yInput.setText("" + Math.round(pose.getY()));

                panel.addAll(xLabel, xInput, yLabel, yInput);
                window.add(panel, BorderLayout.CENTER);

                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            int xVal = Integer.parseInt(xInput.getText());
                            int yVal = Integer.parseInt(yInput.getText());
                            pose.setLocation(xVal, yVal);
                        } catch (NumberFormatException ignored) {

                        }
                        parent.repaint();
                        dispose();
                    }
                });
                window.add(okButton, BorderLayout.PAGE_END);
                window.addKeyListener(new EnterKeyListener());
                yInput.addKeyListener(new EnterKeyListener());
                xInput.addKeyListener(new EnterKeyListener());
                okButton.addKeyListener(new EnterKeyListener());
                add(window);
            }

            private class EnterKeyListener extends KeyAdapter {
                @Override
                public void keyReleased(KeyEvent e) {
                    super.keyReleased(e);
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        try {
                            int xVal = Integer.parseInt(xInput.getText());
                            int yVal = Integer.parseInt(yInput.getText());
                            pose.setLocation(xVal, yVal);
                        } catch (NumberFormatException ignored) {

                        }
                        parent.repaint();
                        dispose();
                    }
                }
            }
        }
    }




    /**
     * Visual representation of the simulated robot.
     */
    private class Rover extends JComponent {
        static final int BOT_DIAMETER = 10;
        private static final int BOT_HEAD_OPENING_ANGLE = 20;
        private static final float SENSOR_HEAD_SCALE_FACTOR = 1.2f;
        private static final int SENSOR_HEAD_ANGLE = 30;

        private final MapOverlay window;

        /**
         * Constructor.
         *
         * @param window    The visual environment for the robot.
         */
        Rover(MapOverlay window) {
            this.window = window;
        }

        /**
         * Paints the robot.
         *
         * @param g The graphical context.
         */
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2d = (Graphics2D) g;

            int scaleFactor = window.getScaleFactor();
            int xOffset = window.getxOffset();
            int yOffset = window.getyOffset();

            if (pose != null) {
                paintSensorHead(g2d, scaleFactor, xOffset, yOffset);
                paintRobot(g2d, scaleFactor, xOffset, yOffset);
            }
        }

        /**
         * Paints the visual representation of the sensor-head.
         *
         * @param g2d               The graphical context.
         * @param scaleFactor       The visual scale-factor.
         * @param xOffset           The visual x-offset.
         * @param yOffset           The visual y-offset.
         */
        private void paintSensorHead(Graphics2D g2d, int scaleFactor, int xOffset, int yOffset) {
            int headX = (Math.round((pose.getX() - (BOT_DIAMETER  * SENSOR_HEAD_SCALE_FACTOR) / 2)) * scaleFactor) + xOffset;
            int headY = (Math.round((pose.getY() - (BOT_DIAMETER * SENSOR_HEAD_SCALE_FACTOR) / 2)) * scaleFactor) + yOffset;
            int headDia =  Math.round(BOT_DIAMETER  * scaleFactor * SENSOR_HEAD_SCALE_FACTOR);
            int startAngle = Math.round(sensorHeadPosition + pose.getHeading() - SENSOR_HEAD_ANGLE / 2);

            g2d.setColor(Color.BLUE);
            g2d.fillArc(headX, headY, headDia, headDia, startAngle, SENSOR_HEAD_ANGLE);
        }


        /**
         * Paints the visual representation of the robot itself.
         * @param g2d               The graphical context.
         * @param scaleFactor       The visual scale-factor.
         * @param xOffset           The visual x-offset.
         * @param yOffset           The visual y-offset.
         */
        private void paintRobot(Graphics2D g2d, int scaleFactor, int xOffset, int yOffset) {
            int botX = (Math.round((pose.getX() - BOT_DIAMETER / 2)) * scaleFactor) + xOffset;
            int botY = (Math.round((pose.getY() - BOT_DIAMETER / 2)) * scaleFactor) + yOffset;
            int botDia = BOT_DIAMETER * scaleFactor;

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillOval(botX,botY,botDia,botDia);

            int startAngle = (int)Math.abs(pose.getHeading() + BOT_HEAD_OPENING_ANGLE / 2);
            int arcAngle = 360 - BOT_HEAD_OPENING_ANGLE;

            g2d.setColor(Color.YELLOW);
            g2d.fillArc(botX, botY, botDia, botDia, startAngle, arcAngle );
        }


        /**
         * Returns a Rectangle containing the visual representation of the robot given the visual parameters.
         *
         * @param scaleFactor       The visual scale-factor.
         * @param xOffset           The visual x-offset.
         * @param yOffset           The visual y-offset.
         * @return  A Rectangle enclosing the robot.
         */
        Rectangle getBounds(int scaleFactor, int xOffset, int yOffset) {
            int botX = (Math.round((pose.getX() - BOT_DIAMETER / 2)) * scaleFactor) + xOffset;
            int botY = (Math.round((pose.getY() - BOT_DIAMETER / 2)) * scaleFactor) + yOffset;
            int sideLength = BOT_DIAMETER * scaleFactor;
            return new Rectangle(botX, botY, sideLength, sideLength);
        }
    }
}
