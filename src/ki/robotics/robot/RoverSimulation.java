package ki.robotics.robot;

import ki.robotics.common.ExtJPanel;
import ki.robotics.common.MapPanel;
import ki.robotics.server.BotServer;
import ki.robotics.utility.crisp.Instruction;
import ki.robotics.utility.map.Map;
import ki.robotics.utility.map.MapProvider;
import lejos.robotics.navigation.Pose;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Implementation of the Robot-Interface through the VirtualRobotModel-class.
 * Simulates and displays a robot.
 *
 * @version 1.0 01/02/18
 */
public class RoverSimulation extends VirtualRobotModel {
    private static final String DEFAULT_SELECTED_MAP = "Room";
    private static final int ANIMATION_INTER_FRAME_TIME = 50;

    private BotServer botServer;
    private SimulationDisplay window;

    /**
     * Constructor.
     *
     * @param botServer     An instance of the BotServer for communication.
     */
    public RoverSimulation(BotServer botServer) {
        this.botServer = botServer;
        this.map = MapProvider.getInstance().getMap(DEFAULT_SELECTED_MAP);
        this.window = new SimulationDisplay(map);
        this.pose = new Pose();
        this.pose.setLocation(10,10);
        this.pose.setHeading(0);
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
        System.out.println("Unknows Instruction: >>" + instruction.getMnemonic() + "<< with parameter: >>" + instruction.getParameter() + "<<.");
        return true;
    }






    /**
     * The simulation-gui including the display and a control-panel.
     */
    private class SimulationDisplay extends JFrame {
        private static final int WINDOW_WIDTH = 600;
        private static final int WINDOW_HEIGHT = 800;

        private ControlPanel controlPanel;
        private MapOverlay mapOverlay;


        /**
         * Constructor.
         *
         * @param map   The map in use.
         */
        public SimulationDisplay(Map map) {
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
        private SimulationDisplay parent;
        private MapProvider mapProvider;

        private JComboBox maps;
        private JTextField heading;
        private JButton lock;
        private boolean isLocked = false;

        private String[] mapkeys;


        /**
         * Constructor.
         *
         * @param parent The parent (SimulationDisplay).
         */
        public ControlPanel(SimulationDisplay parent) {
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
            heading = new JTextField("Pose",4);
            heading.setHorizontalAlignment(JTextField.RIGHT);
            heading.setEnabled(false);
            ExtJPanel initContainer = new ExtJPanel();
            initContainer.setLayout(new GridLayout(2,4));

            JButton hpp = new JButton("H++");
            hpp.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (parent.mapOverlay.isModifiable()) {
                        pose.setHeading((pose.getHeading() + 10) % 360);
                        heading.setText("" + Math.round(pose.getHeading()));
                        window.repaint();
                    }
                }
            });

            JButton hp = new JButton("H+");
            hp.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (parent.mapOverlay.isModifiable()) {
                        pose.setHeading((pose.getHeading() + 1) % 360);
                        heading.setText("" + Math.round(pose.getHeading()));
                        window.repaint();
                    }
                }
            });

            JButton hZero = new JButton("0");
            hZero.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (parent.mapOverlay.isModifiable()) {
                        pose.setHeading(0);
                        heading.setText("" + Math.round(pose.getHeading()));
                        window.repaint();
                    }
                }
            });

            JButton hn = new JButton("H-");
            hn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (parent.mapOverlay.isModifiable()) {
                        if (pose.getHeading() >= 1) {
                            pose.setHeading(pose.getHeading() - 1);
                        } else {
                            pose.setHeading(359);
                        }
                        heading.setText("" + Math.round(pose.getHeading()));
                        window.repaint();
                    }
                }
            });

            JButton hnn = new JButton("H--");
            hnn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (parent.mapOverlay.isModifiable()) {
                        if (pose.getHeading() >= 10) {
                            pose.setHeading(pose.getHeading() - 10);
                        } else {
                            pose.setHeading(360 - (10 - pose.getHeading()));
                        }
                        heading.setText("" + Math.round(pose.getHeading()));
                        window.repaint();
                    }
                }
            });

            initContainer.addAll(hpp, hp, hZero, hnn, hn, heading);
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

        private Rover rover;

        /**
         * Constructor.
         *
         * @param parent    The parent (SimulationDisplay).
         * @param map       The map in use.
         */
        public MapOverlay(SimulationDisplay parent, Map map) {
            super(parent, map);

            this.rover = new Rover(this);
            this.grabFocus();
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    Rectangle botOutline = rover.getBounds(getScaleFactor(), getxOffset(), getyOffset());
                    if (isModifiable()) {
                        new CoordinateInput();
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
            private JTextField xInput = new JTextField(10);
            private JTextField yInput = new JTextField(10);
            private JButton okButton = new JButton("OK");

            /**
             * Constructor.
             */
            public CoordinateInput() {
                this.setTitle("Insert Robot-Coordinates");
                this.setLocationRelativeTo(parent);
                this.setSize(250,100);
                this.setLayout(new FlowLayout());
                this.setModalExclusionType(Dialog.ModalExclusionType.NO_EXCLUDE);
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
                        } catch (NumberFormatException e1) {

                        }
                        dispose();
                    }
                });
                window.add(okButton, BorderLayout.PAGE_END);
                add(window);
            }
        }
    }




    /**
     * Visual representation of the simulated robot.
     */
    private class Rover extends JComponent {
        public static final int BOT_DIAMETER = 10;
        private static final int BOT_HEAD_OPENING_ANGLE = 20;
        private static final float SENSOR_HEAD_SCALE_FACTOR = 1.2f;
        private static final int SENSOR_HEAD_ANGLE = 30;

        private MapOverlay window;

        /**
         * Constructor.
         *
         * @param window    The visual environment for the robot.
         */
        public Rover(MapOverlay window) {
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
        public Rectangle getBounds(int scaleFactor, int xOffset, int yOffset) {
            int botX = (Math.round((pose.getX() - BOT_DIAMETER / 2)) * scaleFactor) + xOffset;
            int botY = (Math.round((pose.getY() - BOT_DIAMETER / 2)) * scaleFactor) + yOffset;
            int sideLength = BOT_DIAMETER * scaleFactor;
            return new Rectangle(botX, botY, sideLength, sideLength);
        }
    }
}
