package ki.robotics.server.robots.simulation;

import ki.robotics.utility.map.Map;
import ki.robotics.utility.map.MapPanel;
import ki.robotics.utility.map.MapProvider;

import javax.swing.*;
import java.awt.*;

class SimulationView extends JFrame {
    private static final String WINDOW_TITLE = "Simulated Rover";
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 800;

    private final SimulationController controller;
    private final SimulationModel model;

    private final ControlPanel controlPanel;
    private final MapOverlay mapOverlay;


    /**
     * Constructor.
     *
     */
    SimulationView(SimulationController controller, SimulationModel model) {
        this.controller = controller;
        this.model = model;
        this.setTitle(WINDOW_TITLE);
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.controlPanel = new ControlPanel();
        add(controlPanel, BorderLayout.PAGE_START);
        this.mapOverlay = new MapOverlay(this.model.getMap());
        add(mapOverlay, BorderLayout.CENTER);
        this.setVisible(true);
    }


    MapOverlay getMapOverlay() {
        return mapOverlay;
    }

    ControlPanel getControlPanel() {
        return this.controlPanel;
    }





    /**
     * The control-panel for the simulation.
     */
    class ControlPanel extends JPanel {
        private final MapProvider mapProvider;

        private JComboBox maps;
        private JButton lockButton;
        private JLabel headingLabel;


        private final String[] mapkeys;


        /**
         * Constructor.
         *
         */
        ControlPanel() {
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
            maps.addActionListener(controller.new MapSelectionActionListener());

            mapSelectionContainer.add(mapLabel, BorderLayout.PAGE_START);
            mapSelectionContainer.add(maps, BorderLayout.CENTER);
            add(mapSelectionContainer);
        }


        /**
         * Adds gui-elements to specify the initial heading of the simulated robot.
         */
        private void addBotInitPositionChooseToUI() {
            int currentHeading = Math.round(model.getPose().getHeading());
            final JSlider headingSlider = new JSlider(0,359,currentHeading);
            headingSlider.setMinorTickSpacing(5);
            headingSlider.setMajorTickSpacing(45);
            headingSlider.setPaintTicks(true);
            headingSlider.setSnapToTicks(true);
            headingLabel = new JLabel(String.valueOf("Heading: " + currentHeading));
            headingSlider.addChangeListener(controller.new HeadingSliderChangeListener());

            JPanel initContainer = new JPanel();
            initContainer.setLayout(new GridLayout(2,1));

            initContainer.add(headingLabel);
            initContainer.add(headingSlider);
            add(initContainer);
        }



        /**
         * Adds control-elements for locking and unlocking the control-panel.
         */
        private void addControlElementsToUI() {
            JPanel controlContainer = new JPanel();
            controlContainer.setLayout(new BorderLayout());

            lockButton = new JButton("Lock");
            lockButton.addActionListener(controller.new LockButtonActionListener());

            controlContainer.add(lockButton, BorderLayout.PAGE_START);
            add(controlContainer);
        }


        void updateHeadingLabel(String text) {
            headingLabel.setText(text);
        }

        void updateLockButtonTest(String text) {
            lockButton.setText(text);
        }

    }





    /**
     * Specialization of the MapOverlay for the simulation.
     */
    class MapOverlay extends MapPanel {
        private final Rover rover;

        /**
         * Constructor.
         *
         * @param map       The map in use.
         */
        MapOverlay(Map map) {
            super(map);
            this.rover = new Rover(this);
            this.grabFocus();
            this.addMouseListener(controller.new MapOverlayClickedMouseListener());
            this.addMouseMotionListener(controller.new RoverDraggedMouseMotionListener());
        }

        void userCoordinateInput() {
            new CoordinateInput(this);
        }

        Rover getRover() {
            return this.rover;
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
        class CoordinateInput extends JFrame {
            private final JTextField xInput = new JTextField(10);
            private final JTextField yInput = new JTextField(10);
            private final JButton okButton = new JButton("OK");

            /**
             * Constructor.
             */
            CoordinateInput(MapOverlay parent) {
                this.setTitle("Insert Robot-Coordinates");
                this.setLocationRelativeTo(parent);
                this.setSize(250,100);
                this.setLayout(new FlowLayout());
                this.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
                this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                initializeElements();
                this.setVisible(true);
            }


            /**
             * Adds the gui-elements to the dialog.
             */
            private void initializeElements() {
                JPanel window = new JPanel();
                window.setLayout(new BorderLayout());

                JPanel panel = new JPanel();
                panel.setLayout(new GridLayout(2,2));

                JLabel xLabel = new JLabel("x-Value: ");
                xLabel.setLabelFor(xInput);
                JLabel yLabel = new JLabel("y-Value: ");
                yLabel.setLabelFor(yInput);

                xInput.setText("" + Math.round(model.getPose().getX()));
                xInput.addFocusListener(controller.new UserXCoordinateInputFocusListener());
                yInput.setText("" + Math.round(model.getPose().getY()));
                yInput.addFocusListener(controller.new UserYCoordinateInputFocusListener());

                panel.add(xLabel);
                panel.add(xInput);
                panel.add(yLabel);
                panel.add(yInput);
                window.add(panel, BorderLayout.CENTER);

                okButton.addActionListener(controller.new UserCoordinateInputOKButtonActionListener(this));

                window.add(okButton, BorderLayout.PAGE_END);
                window.addKeyListener(controller.new UserCoordinateInputEnterKeyListener(this));
                yInput.addKeyListener(controller.new UserCoordinateInputEnterKeyListener(this));
                xInput.addKeyListener(controller.new UserCoordinateInputEnterKeyListener(this));
                okButton.addKeyListener(controller.new UserCoordinateInputEnterKeyListener(this));
                add(window);
            }


            String getXInput() {
                return xInput.getText();
            }


            String getYInput() {
                return yInput.getText();
            }
        }
    }





    /**
     * Visual representation of the simulated robot.
     */
    class Rover extends JComponent {
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
            int xOffset = window.getXOffset();
            int yOffset = window.getYOffset();

            if (model.getPose() != null) {
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
            int headX = (Math.round((model.getPose().getX() - (BOT_DIAMETER  * SENSOR_HEAD_SCALE_FACTOR) / 2)) * scaleFactor) + xOffset;
            int headY = (Math.round((model.getPose().getY() - (BOT_DIAMETER * SENSOR_HEAD_SCALE_FACTOR) / 2)) * scaleFactor) + yOffset;
            int headDia =  Math.round(BOT_DIAMETER  * scaleFactor * SENSOR_HEAD_SCALE_FACTOR);
            int startAngle = Math.round(model.getSensorHeadPosition() + model.getPose().getHeading() - SENSOR_HEAD_ANGLE / 2);

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
            int botX = (Math.round((model.getPose().getX() - BOT_DIAMETER / 2)) * scaleFactor) + xOffset;
            int botY = (Math.round((model.getPose().getY() - BOT_DIAMETER / 2)) * scaleFactor) + yOffset;
            int botDia = BOT_DIAMETER * scaleFactor;

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillOval(botX,botY,botDia,botDia);

            int startAngle = (int)Math.abs(model.getPose().getHeading() + BOT_HEAD_OPENING_ANGLE / 2);
            int arcAngle = 360 - BOT_HEAD_OPENING_ANGLE;

            g2d.setColor(Color.YELLOW);
            g2d.fillArc(botX, botY, botDia, botDia, startAngle, arcAngle );

            g2d.setColor(Color.BLACK);
            g2d.drawString("RobotImplSojourner:",10,20);
            g2d.drawString("X: " + String.valueOf(model.getPose().getX()), 10,40);
            g2d.drawString("Y: " + String.valueOf(model.getPose().getY()), 10,55);
            g2d.drawString("H: " + String.valueOf(model.getPose().getHeading()), 10, 70);
        }


        /**
         * Returns a Rectangle containing the visual representation of the robot given the visual parameters.
         *
         * @return  A Rectangle enclosing the robot.
         */
        Rectangle getRobotBounds() {
            int scaleFactor = window.getScaleFactor();
            int xOffset = window.getXOffset();
            int yOffset = window.getYOffset();
            int botX = (Math.round((model.getPose().getX() - BOT_DIAMETER / 2)) * scaleFactor) + xOffset;
            int botY = (Math.round((model.getPose().getY() - BOT_DIAMETER / 2)) * scaleFactor) + yOffset;
            int sideLength = BOT_DIAMETER * scaleFactor;
            return new Rectangle(botX, botY, sideLength, sideLength);
        }
    }
}
