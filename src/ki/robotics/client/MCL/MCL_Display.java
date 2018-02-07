package ki.robotics.client.MCL;

import ki.robotics.client.ComController;
import ki.robotics.common.ExtButtonGroup;
import ki.robotics.common.ExtJPanel;
import ki.robotics.common.MapPanel;
import ki.robotics.utility.map.Map;
import ki.robotics.robot.MCLParticle;
import ki.robotics.utility.map.MapProvider;
import lejos.robotics.navigation.Pose;
import sun.security.krb5.Config;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.EventListener;

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
        if (configuration.isOneDimensional()   && ((Configuration.ConfigOneD)configuration).isStartFromRight()) {
                limitations[2] = 180;
        }
        this.mclProvider = new MCL_Provider(map, configuration.getNumberOfParticles(), limitations, configuration);
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

            g.setColor(mclProvider.isLocalizationDone() ? Color.GREEN : Color.RED);
            int radius = (int)Math.ceil(mclProvider.getEstimatedBotPoseDeviation());
            int acceptableTolerance = mclProvider.getAcceptableTolerance();
            radius = radius < acceptableTolerance ? acceptableTolerance : radius;
            g.drawOval(
                    (Math.round(p.getX())-radius) * getScaleFactor() + getxOffset(),
                    (Math.round(p.getY())-radius) * getScaleFactor() + getyOffset(),
                    radius * 2 * getScaleFactor(),
                    radius * 2 * getScaleFactor());
            g.drawString("Deviation: " + String.valueOf(radius),10,40);

            g.setColor(Color.BLACK);
            g.drawString("Estimated Bot Position: ", 10,20);
            g.drawString("X: " + String.valueOf(Math.round(p.getX())),10,55);
            g.drawString("Y: " + String.valueOf(Math.round(p.getY())), 10,70);
            g.drawString("H: " + String.valueOf(Math.round(p.getHeading())), 10,85);
        }
    }





    /**
     * Control-Panel to modify the terms of the monte-carlo-localization.
     */
    private class ControlPanel extends JPanel {
        private final MCL_Display parent;

        private JTabbedPane specificElements = new JTabbedPane();
        private ExtJPanel oneDimensionalControls = new ExtJPanel();
        private ExtJPanel twoDimensionalControls = new ExtJPanel();
        private ExtJPanel twoDimensionalWithCam = new ExtJPanel();

        private final JRadioButton turnRightAngle = new JRadioButton("90° Angles");
        private final JRadioButton turnFree = new JRadioButton("Free Angles");
        private final JCheckBox leftSensor = new JCheckBox("Left sensor");
        private final JCheckBox frontSensor = new JCheckBox("Front sensor");
        private final JCheckBox rightSensor = new JCheckBox("Right sensor");

        private final JRadioButton startFromLeft = new JRadioButton("Start from left");
        private final JRadioButton startFromRight = new JRadioButton("Start from right");

        private final JCheckBox camGeneralQuery = new JCheckBox("General Query");
        private final JCheckBox camAngleQuery = new JCheckBox("Angle Query");
        private final JCheckBox camSignature1 = new JCheckBox("Signature 1");
        private final JCheckBox camSignature2 = new JCheckBox("Signature 2");
        private final JCheckBox camSignature3 = new JCheckBox("Signature 3");
        private final JCheckBox camSignature4 = new JCheckBox("Signature 4");
        private final JCheckBox camSignature5 = new JCheckBox("Signature 5");
        private final JCheckBox camSignature6 = new JCheckBox("Signature 6");
        private final JCheckBox camSignature7 = new JCheckBox("Signature 7");

        private final JTextField stepsize = new JTextField( 5);
        private final JTextField particles = new JTextField(5);
        private final JButton start = new JButton("Start");
        private final JButton stop = new JButton("Stop");
        private final JLabel toleranceLabel = new JLabel();
        private final JSlider tolerance = new JSlider(1,25,10);
        private final JCheckBox stopWhenDone = new JCheckBox("Stop when done");


        /**
         * Constructor.
         *
         * @param parent    The parent-MCL-Display.
         */
        ControlPanel(final MCL_Display parent) {
            this.parent = parent;
            this.setLayout(new FlowLayout());

            initializeOneDimensionalControls();
            initializeTwoDimensionalControls();
            initializeTwoDimensionalCameraControls();

            specificElements.addTab("One-D", oneDimensionalControls);
            specificElements.addTab("Two-D", twoDimensionalControls);
            specificElements.addTab("Camera", twoDimensionalWithCam);

            add(specificElements);
            initializeCommonComponents();
        }

        private void initializeOneDimensionalControls() {
            ExtButtonGroup leftOrRight = new ExtButtonGroup();
            startFromLeft.setSelected(Configuration.ConfigOneD.DEFAULT.isStartFromLeft());
            startFromRight.setSelected(Configuration.ConfigOneD.DEFAULT.isStartFromRight());
            leftOrRight.addAll(startFromLeft, startFromRight);
            oneDimensionalControls.addAll(startFromLeft, startFromRight);
            oneDimensionalControls.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    super.componentShown(e);
                    String mapKey1D = Configuration.ConfigOneD.DEFAULT.getMapKey();
                    parent.mapPanel.setNewMap(MapProvider.getInstance().getMap(mapKey1D));
                    map = MapProvider.getInstance().getMap(mapKey1D);
                    mclProvider = null;
                    stepsize.setText(String.valueOf(Configuration.ConfigOneD.DEFAULT.getStepSize()));
                    particles.setText(String.valueOf(Configuration.ConfigOneD.DEFAULT.getNumberOfParticles()));
                    tolerance.setValue(Configuration.ConfigOneD.DEFAULT.getAcceptableTolerance());
                    parent.repaint();
                }
            });
        }

        private void initializeTwoDimensionalControls() {
            addMovementLimitationsSelectionToUI();
            addSensorSelectionToUI();
            twoDimensionalControls.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    String mapKey2D = Configuration.ConfigTwoD.DEFAULT.getMapKey();
                    parent.mapPanel.setNewMap(MapProvider.getInstance().getMap(mapKey2D));
                    map = MapProvider.getInstance().getMap(mapKey2D);
                    mclProvider = null;
                    stepsize.setText(String.valueOf(Configuration.ConfigTwoD.DEFAULT.getStepSize()));
                    particles.setText(String.valueOf(Configuration.ConfigTwoD.DEFAULT.getNumberOfParticles()));
                    tolerance.setValue(Configuration.ConfigTwoD.DEFAULT.getAcceptableTolerance());
                    parent.repaint();
                }
            });
        }

        private void initializeTwoDimensionalCameraControls() {
            ExtJPanel container = new ExtJPanel();
            container.setLayout(new GridLayout(3, 4));
            camGeneralQuery.setSelected(Configuration.ConfigCamera.DEFAULT.isUseGeneralQuery());
            camAngleQuery.setSelected((Configuration.ConfigCamera.DEFAULT.isUseAngleQuery()));
            camSignature1.setSelected(Configuration.ConfigCamera.DEFAULT.isUseSignatureOne());
            camSignature2.setSelected(Configuration.ConfigCamera.DEFAULT.isUseSignatureTwo());
            camSignature3.setSelected(Configuration.ConfigCamera.DEFAULT.isUseSignatureThree());
            camSignature4.setSelected(Configuration.ConfigCamera.DEFAULT.isUseSignatureFour());
            camSignature5.setSelected(Configuration.ConfigCamera.DEFAULT.isUseSignatureFive());
            camSignature6.setSelected(Configuration.ConfigCamera.DEFAULT.isUseSignatureSix());
            camSignature7.setSelected(Configuration.ConfigCamera.DEFAULT.isUseSignatureSeven());
            container.addAll(
                    camGeneralQuery, camAngleQuery, new JLabel(), new JLabel(),
                    camSignature1, camSignature2, camSignature3, camSignature4,
                    camSignature5, camSignature6, camSignature7);
            twoDimensionalWithCam.add(container);
            twoDimensionalWithCam.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    String mapKeyCam = Configuration.ConfigCamera.DEFAULT.getMapKey();
                    parent.mapPanel.setNewMap(MapProvider.getInstance().getMap(mapKeyCam));
                    map = MapProvider.getInstance().getMap(mapKeyCam);
                    mclProvider = null;
                    stepsize.setText(String.valueOf(Configuration.ConfigCamera.DEFAULT.getStepSize()));
                    particles.setText(String.valueOf(Configuration.ConfigCamera.DEFAULT.getNumberOfParticles()));
                    tolerance.setValue(Configuration.ConfigCamera.DEFAULT.getAcceptableTolerance());
                    parent.repaint();
                }
            });
        }

        /**
         * Initialization of the common gui-elements.
         */
        private void initializeCommonComponents() {
            ExtJPanel commonComponents = new ExtJPanel();
            commonComponents.setLayout(new GridLayout(5,1));

            ExtJPanel one = new ExtJPanel();
            one.setLayout(new GridLayout(1,3));
            JLabel stepLabel = new JLabel("Stepsize: ");
            stepLabel.setLabelFor(stepsize);
            stepsize.setHorizontalAlignment(JTextField.RIGHT);
            stepsize.setText(String.valueOf(Configuration.ConfigOneD.DEFAULT.getStepSize()));
            start.addActionListener(new StartButtonActionListener());
            one.addAll(stepLabel, stepsize, start);

            ExtJPanel two = new ExtJPanel();
            two.setLayout(new GridLayout(1,3));
            JLabel particleCnt = new JLabel("Particles: ");
            particleCnt.setLabelFor(particles);
            particles.setHorizontalAlignment(JTextField.RIGHT);
            particles.setText(String.valueOf(Configuration.ConfigOneD.DEFAULT.getNumberOfParticles()));
            stop.addActionListener(new StopButtonActionListener());
            two.addAll(particleCnt, particles, stop);

            toleranceLabel.setText("Acceptable Tolerance: " + Configuration.ConfigOneD.DEFAULT.getAcceptableTolerance() + " cm");
            tolerance.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    toleranceLabel.setText("Acceptable Tolerance: " + tolerance.getValue() + " cm");
                }
            });
            stopWhenDone.setSelected(Configuration.ConfigOneD.DEFAULT.stopWhenDone());
            commonComponents.addAll(toleranceLabel, tolerance, one, two, stopWhenDone);

            add(commonComponents);
        }



        /**
         * Adding gui-elements for specifying the allowed movement of the robot.
         */
        private void addMovementLimitationsSelectionToUI() {
            ExtJPanel container = new ExtJPanel();
            container.setLayout(new GridLayout(2,2));

            ExtButtonGroup angleGroup = new ExtButtonGroup();
            turnFree.setSelected(Configuration.ConfigTwoD.DEFAULT.isUseFreeAngles());
            turnRightAngle.setSelected(Configuration.ConfigTwoD.DEFAULT.isUseRightAngles());
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
            leftSensor.setSelected(Configuration.ConfigTwoD.DEFAULT.isUseLeftSensor());
            frontSensor.setSelected(Configuration.ConfigTwoD.DEFAULT.isUseFrontSensor());
            rightSensor.setSelected(Configuration.ConfigTwoD.DEFAULT.isUseRightSensor());
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
                String mapKey;
                if (oneDimensionalControls.isShowing()) {
                    mapKey = Configuration.ConfigOneD.DEFAULT.getMapKey();
                    Configuration config1D = new Configuration.ConfigOneD(
                            mapKey,
                            true,
                            false,
                            false,
                            step,
                            numOfParticles,
                            stopWhenDone.isSelected(),
                            tolerance.getValue(),
                            startFromLeft.isSelected()
                    );
                    setEnabled(false);
                    start(config1D);
                }
                if (twoDimensionalControls.isShowing()) {
                    mapKey = Configuration.ConfigTwoD.DEFAULT.getMapKey();
                    Configuration config2D = new Configuration.ConfigTwoD(
                            mapKey,
                            false,
                            true,
                            false,
                            step,
                            numOfParticles,
                            stopWhenDone.isSelected(),
                            tolerance.getValue(),
                            turnRightAngle.isSelected(),
                            turnFree.isSelected(),
                            leftSensor.isSelected(),
                            frontSensor.isSelected(),
                            rightSensor.isSelected()
                    );
                    setEnabled(false);
                    start(config2D);
                }
                if (twoDimensionalWithCam.isShowing()) {
                    mapKey = Configuration.ConfigCamera.DEFAULT.getMapKey();
                    Configuration configWithCam = new Configuration.ConfigCamera(
                            mapKey,
                            false,
                            true,
                            true,
                            step,
                            numOfParticles,
                            stopWhenDone.isSelected(),
                            tolerance.getValue(),
                            camGeneralQuery.isSelected(),
                            camAngleQuery.isSelected(),
                            camSignature1.isSelected(),
                            camSignature2.isSelected(),
                            camSignature3.isSelected(),
                            camSignature4.isSelected(),
                            camSignature5.isSelected(),
                            camSignature6.isSelected(),
                            camSignature7.isSelected()
                    );
                    setEnabled(false);
                    start(configWithCam);
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
