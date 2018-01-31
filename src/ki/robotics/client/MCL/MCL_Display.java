package ki.robotics.client.MCL;

import ki.robotics.client.ComController;
import ki.robotics.common.ExtButtonGroup;
import ki.robotics.common.ExtJPanel;
import ki.robotics.common.MapPanel;
import ki.robotics.utility.map.Map;
import ki.robotics.robot.MCLParticle;
import ki.robotics.utility.map.MapProvider;
import lejos.robotics.navigation.Pose;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

/**
 * The client-ui for the monte-carlo-localization.
 *
 * @version 1.0 01/02/18
 */
public class MCL_Display extends JFrame{
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 800;
    private static final String DEFAULT_SELECTED_MAP = "Houses";

    private Map map;
    private final ClientMapPanel mapPanel;
    private MCL_Provider mclProvider;

    private final ComController ComController;




    /**
     * Constructor.
     *
     * @param ComController    A GUIComController, handling communication and response-decoding.
     */
    public MCL_Display(ComController ComController) {
        this.ComController = ComController;
        this.map = MapProvider.getInstance().getMap(DEFAULT_SELECTED_MAP);
        this.mapPanel = new ClientMapPanel(this, this.map);

        this.setTitle("Monte Carlo Localization");
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(new ControlPanel(this), BorderLayout.PAGE_START);
        add(mapPanel, BorderLayout.CENTER);
        this.setVisible(true);
    }



    /**
     * Returns a reference to the monte-carlo-localization-provider in use.
     *
     * @return  A reference to the mcl-provider in use.
     */
    public MCL_Provider getMclProvider() {
        return mclProvider;
    }



    /**
     * Incorporates user-selections into the monte-carlo-localization and signals the GUIComController to
     * try to connect to the server.
     *
     * @param configuration     User-Selections concerning the MCL
     */
    private void start(Configuration configuration) {
        int[] limitations = MapProvider.getInstance().getMapLimitations(configuration.getMapKey());
        this.mclProvider = new MCL_Provider(map, configuration.getNumberOfParticles(), limitations);
        ComController.start(configuration);
        repaint();
    }



    /**
     * Signals the GUIComController to disconnect from the server.
     */
    private void stop() {
        ComController.stop();
        repaint();
    }


    /**
     * Specialized MapPanel for the client
     */
    private class ClientMapPanel extends MapPanel {
        private static final int PARTICLE_DIAMTER = 4;

        /**
         * Constructor.
         *
         * @param parent    The parent-JFrame.
         */
        ClientMapPanel(JFrame parent, Map map) {
            super(parent, map);
        }



        /**
         * Paints the mcl-particles an the supposed position of the robot on top of the map, which is
         * painted in the super-class.
         *
         * @param g     The graphical context.
         */
        @Override
        public void paint(Graphics g) {
            super.paint(g);

            if (mclProvider == null) {
                return;
            }

            ArrayList<MCLParticle> particles = mclProvider.getParticles();
            if (particles != null) {
                float medianWeight = mclProvider.getMedianParticleWeight();
                for (MCLParticle p : particles) {
                    p.paint(g, PARTICLE_DIAMTER, getScaleFactor(), getxOffset(), getyOffset(), medianWeight);
                }
            }
            Pose p = mclProvider.getEstimatedBotPose();

            g.setColor(Color.RED);
            g.drawOval(
                    (Math.round(p.getX())-10) * getScaleFactor() + getxOffset(),
                    (Math.round(p.getY())-10) * getScaleFactor() + getyOffset(), 20* getScaleFactor(),20* getScaleFactor());

        }
    }





    /**
     * Control-Panel to modify the terms of the monte-carlo-localization.
     */
    private class ControlPanel extends JPanel {
        private final String[] mapkeys;
        private final MCL_Display parent;

        private JTabbedPane specificElements = new JTabbedPane();
        private ExtJPanel oneDimensionalControls = new ExtJPanel();
        private ExtJPanel twoDimensionalControls = new ExtJPanel();

        private final JRadioButton turnRightAngle = new JRadioButton("90° Angles");
        private final JRadioButton turnFree = new JRadioButton("Free Angles");
        private final JCheckBox leftSensor = new JCheckBox("Left sensor");
        private final JCheckBox frontSensor = new JCheckBox("Front sensor");
        private final JCheckBox rightSensor = new JCheckBox("Right sensor");
        private final JRadioButton startFromLeft = new JRadioButton("Start from left");
        private final JRadioButton startFromRight = new JRadioButton("Start from right");
        private final JTextField stepsize = new JTextField("10", 5);
        private final JTextField particles = new JTextField("1000",5);
        private final JButton start = new JButton("Start");
        private final JButton stop = new JButton("Stop");


        /**
         * Constructor.
         *
         * @param parent    The parent-MCL-Display.
         */
        ControlPanel(final MCL_Display parent) {
            this.parent = parent;
            this.mapkeys = MapProvider.getInstance().getMapKeys();
            this.setLayout(new FlowLayout());

            initializeOneDimensionalControls();
            initializeTwoDimensionalControls();
            specificElements.addTab("One-D", oneDimensionalControls);
            specificElements.addTab("Two-D", twoDimensionalControls);
            oneDimensionalControls.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    super.componentShown(e);
                    String mapkey1D = Configuration.ConfigOneD.DEFAULT.getMapKey();
                    parent.mapPanel.setNewMap(MapProvider.getInstance().getMap(mapkey1D));
                    map = MapProvider.getInstance().getMap(mapkey1D);
                    mclProvider = null;
                    parent.repaint();
                }
            });
            twoDimensionalControls.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    String mapKey2D = Configuration.ConfigTwoD.DEFAULT.getMapKey();
                    parent.mapPanel.setNewMap(MapProvider.getInstance().getMap(mapKey2D));
                    map = MapProvider.getInstance().getMap(mapKey2D);
                    mclProvider = null;
                    parent.repaint();
                }
            });
            add(specificElements);
            initializeCommonComponents();
        }

        private void initializeOneDimensionalControls() {
            ExtButtonGroup leftOrRight = new ExtButtonGroup();
            startFromLeft.setSelected(true);
            startFromRight.setSelected(false);
            leftOrRight.addAll(startFromLeft, startFromRight);
            oneDimensionalControls.addAll(startFromLeft, startFromRight);
        }

        private void initializeTwoDimensionalControls() {
            addMovementLimitationsSelectionToUI();
            addSensorSelectionToUI();
        }

        /**
         * Initialization of the common gui-elements.
         */
        private void initializeCommonComponents() {
            ExtJPanel others = new ExtJPanel();
            others.setLayout(new GridLayout(2,3));
            JLabel stepLabel = new JLabel("Stepsize: ");
            stepLabel.setLabelFor(stepsize);
            JLabel particleCnt = new JLabel("Particles: ");
            particleCnt.setLabelFor(particles);
            stepsize.setHorizontalAlignment(JTextField.RIGHT);
            particles.setHorizontalAlignment(JTextField.RIGHT);

            start.addActionListener(new StartButtonActionListener());
            stop.addActionListener(new StopButtonActionListener());

            others.addAll(stepLabel, stepsize, start, particleCnt, particles, stop);
            add(others);
        }



        /**
         * Adding gui-elements for specifying the allowed movement of the robot.
         */
        private void addMovementLimitationsSelectionToUI() {
            ExtJPanel container = new ExtJPanel();
            container.setLayout(new GridLayout(2,2));

            ExtButtonGroup angleGroup = new ExtButtonGroup();
            turnFree.setSelected(true);
            angleGroup.addAll(turnFree, turnRightAngle);

            container.addAll(turnFree, turnRightAngle);

            twoDimensionalControls.add(container);
        }


        /**
         * Adding gui-elements for specifying which of the distance-sensors to use.
         */
        private void addSensorSelectionToUI() {
            ExtJPanel sensorContainer = new ExtJPanel();
            sensorContainer.setLayout(new GridLayout(3,1));
            leftSensor.setSelected(true);
            frontSensor.setSelected(true);
            rightSensor.setSelected(true);
            sensorContainer.addAll(leftSensor, frontSensor, rightSensor);
            twoDimensionalControls.add(sensorContainer);
        }


        /**
         * Action-Listener for the Start-Button.
         */
        private class StartButtonActionListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.mapPanel.setModifiable(false);
                int step = Integer.parseInt(stepsize.getText());
                int numOfParticles = Integer.parseInt(particles.getText());
                String mapkey;
                if (oneDimensionalControls.isShowing()) {
                    mapkey = Configuration.ConfigOneD.DEFAULT.getMapKey();
                    Configuration config1D = new Configuration.ConfigOneD(
                            mapkey,
                            true,
                            false,
                            step,
                            numOfParticles,
                            startFromLeft.isSelected()
                    );
                    setEnabled(false);
                    start(config1D);
                }
                if (twoDimensionalControls.isShowing()) {
                    mapkey = Configuration.ConfigTwoD.DEFAULT.getMapKey();
                    Configuration config2D = new Configuration.ConfigTwoD(
                            mapkey,
                            false,
                            true,
                            step,
                            numOfParticles,
                            turnRightAngle.isSelected(),
                            turnFree.isSelected(),
                            leftSensor.isSelected(),
                            frontSensor.isSelected(),
                            rightSensor.isSelected());
                    start(config2D);
                    setEnabled(false);
                    start(config2D);
                }
            }
        }

        /**
         * Action-Listener for the Stop-Button.
         */
        private class StopButtonActionListener implements  ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.mapPanel.setModifiable(true);
                stop();
                start.setEnabled(true);
            }
        }

    }
}
