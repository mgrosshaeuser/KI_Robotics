package ki.robotics.client.GUI.impl;

import ki.robotics.client.MCL.LocalizationProvider;
import ki.robotics.client.MCL.Particle;
import ki.robotics.client.MCL.WorldState;
import ki.robotics.utility.map.MapPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

/**
 * The primary window of the client-GUI.
 */
class ClientView extends JFrame {
    static final String WINDOW_TITLE = "Monte Carlo Localization";
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 800;

    private GuiConfigurationImplClientModel guiModel;
    private GuiControllerImplClientController guiController;

    private ControlPanel controlPanel;
    private ClientMapPanel mapPanel;


    /**
     * Constructor.
     *
     * @param guiController     The GUI-controller
     * @param guiModel          The GUI-(data-)model
     */
    ClientView(GuiControllerImplClientController guiController, GuiConfigurationImplClientModel guiModel) {
        this.guiController = guiController;
        this.guiModel = guiModel;
        this.mapPanel = new ClientMapPanel(guiModel);
    }


    /**
     * Initializes and displays the primary window.
     */
    void initializeView() {
        createWindow();
        addKeyControls();
        this.setVisible(true);
    }


    /**
     * Returns a reference to the ControlPanel of the GUI.
     *
     * @return  A reference to the ControlPanel of the GUI (of type JPanel)
     */
    JPanel getControlPanel() { return controlPanel; }


    /**
     * Returns a reference to the MapPanel of the GUI.
     *
     * @return  A reference to the MapPanel of the GUI (of type JPanel)
     */
    ClientMapPanel getMapPanel() { return  mapPanel; }


    /**
     * Refreshes the information about the selected particle on display.
     */
    void refreshParticleInfo() { controlPanel.refreshParticleInfo(); }


    /**
     * Creates the primary window including control- and map-panel.
     */
    private void createWindow() {
        this.setTitle(WINDOW_TITLE);
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        controlPanel = new ControlPanel();
        add(controlPanel, BorderLayout.PAGE_START);
        add(mapPanel, BorderLayout.CENTER);
        addMouseListener(guiController.new ParticleSelectionMouseListener());
    }


    /**
     * Adds key-listeners to the primary window to realize basic operations:
     * - CTRL + SPACE run/pause localization (or replay)
     * - CTRL + LEFT ARROW move one step backward in localization (or replay)
     * - CTRL + RIGHT ARROW move one step forward in localization (or replay)
     */
    private void addKeyControls() {
        InputMap controlPanelInputMap = this.controlPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap controlPanelActionMap = this.controlPanel.getActionMap();

        controlPanelInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,KeyEvent.CTRL_DOWN_MASK), "SpacePressed");
        controlPanelActionMap.put("SpacePressed", guiController.getSpacePressedAction());

        controlPanelInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,KeyEvent.CTRL_DOWN_MASK), "LeftPressed");
        controlPanelActionMap.put("LeftPressed", guiController.getLeftArrowPressedAction());

        controlPanelInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,KeyEvent.CTRL_DOWN_MASK), "RightPressed");
        controlPanelActionMap.put("RightPressed", guiController.getRightArrowPressedAction());
    }


    /**
     * Performs final adjustments to the GUI once a localization is finished.
     */
    void updateWindowAfterLocalizationFinished() {
        controlPanel.updateControlPanelAfterLocalizationFinished();
        this.setTitle(WINDOW_TITLE);
        guiController.postLocalizationWork();
    }


    /**
     * The control-panel for the client-GUI.
     */
    class ControlPanel extends JPanel {
        private JTabbedPane environmentSpecificControls;
        private OneDimensionalControlsSubPanel oneDimensionalControlsSubPanel;
        private TwoDimensionalControlsSubPanel twoDimensionalControlsSubPanel;
        private CameraControlsSubPanel cameraControlsSubPanel;
        private ReplayControlsSubPanel replayControlsSubPanel;

        JTextField distancePerTravelInstruction = new JTextField( 5);
        JTextField numberOfParticles = new JTextField(5);
        JLabel labelForAcceptableLocalizationSpreadingSlider = new JLabel();
        JSlider acceptableLocalizationSpreadingSlider = new JSlider(1,25);
        private JCheckBox stopWhenLocalizationIsFinished = new JCheckBox("Stop when done");

        JButton startButton = new JButton("Start");


        /**
         * Constructor.
         */
        public ControlPanel() {
            this.setLayout(new FlowLayout());
            this.environmentSpecificControls = createEnvironmentSpecificControls();
            this.add(environmentSpecificControls);
            JPanel genericUserControls = createGenericUserControls();
            this.add(genericUserControls);
        }


        /**
         * Refreshes the information about the selected particle on display.
         */
        void refreshParticleInfo() {
            twoDimensionalControlsSubPanel.refreshParticleInfo();
        }


        /**
         * Performs final adjustments to the control-panel once a localization is finished.
         */
        void updateControlPanelAfterLocalizationFinished() {
            ActionListener[] listeners = startButton.getActionListeners();
            for (ActionListener l : listeners) {
                startButton.removeActionListener(l);
            }
            startButton.setText("Start");
            startButton.addActionListener(guiController.new StartButtonActionListener(this));
        }


        /**
         * Returns a JTabbedPane with one tab for each environment.
         * Control-sub-panels are assigned to environment-tabs:
         * - one-dimensional environment
         * - two-dimensional environment
         * - two-dimensional environment with camera-use
         * - replay-environment
         *
         * @return  A JTabbedPane with environment-specific controls
         */
        private JTabbedPane createEnvironmentSpecificControls() {
            JTabbedPane environmentSpecificControlSubPanels = new JTabbedPane();
            oneDimensionalControlsSubPanel = new OneDimensionalControlsSubPanel();
            twoDimensionalControlsSubPanel = new TwoDimensionalControlsSubPanel();
            cameraControlsSubPanel = new CameraControlsSubPanel();
            replayControlsSubPanel = new ReplayControlsSubPanel();
            environmentSpecificControlSubPanels.addTab("One-D", oneDimensionalControlsSubPanel);
            environmentSpecificControlSubPanels.addTab("Two-D", twoDimensionalControlsSubPanel);
            environmentSpecificControlSubPanels.addTab("Camera", cameraControlsSubPanel);
            environmentSpecificControlSubPanels.addTab("Replay", replayControlsSubPanel);
            return environmentSpecificControlSubPanels;
        }


        /**
         * Returns a JPanel with all environment-independent control-elements.
         *
         * @return  A JPanel with all environment-independent control-elements
         */
         private JPanel createGenericUserControls() {
            JPanel userControls = new JPanel();
            userControls.setLayout(new BorderLayout(5,5));

            JPanel acceptableSpreadingInputElements = createAcceptableSpreadingInputElements();
            userControls.add(acceptableSpreadingInputElements, BorderLayout.NORTH);

            JPanel localizationInputElements = createLocalizationInputElements();
            userControls.add(localizationInputElements, BorderLayout.CENTER);

            stopWhenLocalizationIsFinished.setSelected(guiModel.isStopWhenDone());
            stopWhenLocalizationIsFinished.addActionListener(guiController.new setStopWhenLocalizationFinishedActionListener());
            userControls.add(stopWhenLocalizationIsFinished, BorderLayout.SOUTH);

            return userControls;
        }


        /**
         * Returns a JPanel that holds the input-elements for the acceptable particle-spreading (end-criterion
         * for the localization).
         *
         * @return  A JPanel that holds the input-elements for the acceptable particle-spreading
         */
        private JPanel createAcceptableSpreadingInputElements() {
            JPanel acceptableSpreadingInputElements = new JPanel();
            acceptableSpreadingInputElements.setLayout(new GridLayout(2,1,5,5));

            int acceptableSpreading = guiModel.getAcceptableSpreading();
            labelForAcceptableLocalizationSpreadingSlider.setText("Acceptable Spreading: " + acceptableSpreading + " cm");
            acceptableLocalizationSpreadingSlider.setValue(guiModel.getAcceptableSpreading());
            acceptableLocalizationSpreadingSlider.addChangeListener(guiController.new DeviationSliderChangeListener(this));

            acceptableSpreadingInputElements.add(labelForAcceptableLocalizationSpreadingSlider);
            acceptableSpreadingInputElements.add(acceptableLocalizationSpreadingSlider);

            return acceptableSpreadingInputElements;
        }


        /**
         * Returns a JPanel that holds the basic localization control- and input-elements.
         *
         * @return  A JPanel that holds the basic localization control- and input-elements
         */
        private JPanel createLocalizationInputElements() {
            JPanel inputElements = new JPanel();
            inputElements.setLayout(new GridLayout(2,3,5,5));

            JLabel stepLabel = new JLabel("Step size: ");
            stepLabel.setLabelFor(distancePerTravelInstruction);
            distancePerTravelInstruction.setHorizontalAlignment(JTextField.RIGHT);
            distancePerTravelInstruction.setText(String.valueOf(guiModel.getStepSize()));

            startButton.addActionListener(guiController.new StartButtonActionListener(this));

            JLabel particleCnt = new JLabel("Particles: ");
            particleCnt.setLabelFor(numberOfParticles);
            numberOfParticles.setHorizontalAlignment(JTextField.RIGHT);
            numberOfParticles.setText(String.valueOf(guiModel.getNumberOfParticles()));

            inputElements.add(stepLabel);
            inputElements.add(distancePerTravelInstruction);
            inputElements.add(startButton);
            inputElements.add(particleCnt);
            inputElements.add(numberOfParticles);
            inputElements.add(new JLabel());

            return inputElements;
        }





        /**
         * Realises the one-dimensional-tab for the environment-specific control-tabs.
         */
        private class OneDimensionalControlsSubPanel extends JPanel {
            private JRadioButton startFollowingLineFromLeft = new JRadioButton("Start from left");
            private JRadioButton startFollowingLineFromRight = new JRadioButton("Start from right");

            /**
             * Constructor.
             */
            OneDimensionalControlsSubPanel() {
                ButtonGroup lineFollowingInitialDirection = new ButtonGroup();
                startFollowingLineFromLeft.setSelected(guiModel.isStartFromLeft());
                startFollowingLineFromRight.setSelected(guiModel.isStartFromRight());
                startFollowingLineFromLeft.addActionListener(guiController.new startLineFollowingFromLeftActionListener());
                startFollowingLineFromRight.addActionListener(guiController.new startLineFollowingFromRightActionListener());
                lineFollowingInitialDirection.add(startFollowingLineFromLeft);
                lineFollowingInitialDirection.add(startFollowingLineFromRight);
                this.add(startFollowingLineFromLeft);
                this.add(startFollowingLineFromRight);
                this.addComponentListener(guiController.new OneDimensionalControlSubPanelComponentListener());
            }
        }





        /**
         * Realises the two-dimensional-tab for the environment-specific control-tabs.
         */
        private class TwoDimensionalControlsSubPanel extends JPanel {
            private JRadioButton useRightAnglesWhenTurning = new JRadioButton("90° Angles");
            private JRadioButton useRandomAnglesWhenTurning = new JRadioButton("Free Angles");

            private JCheckBox measureDistanceToLeft = new JCheckBox("Left sensor");
            private JCheckBox measureDistanceAhead = new JCheckBox("Front sensor");
            private JCheckBox measureDistanceToRight = new JCheckBox("Right sensor");

            private JLabel particleLabelInfo = new JLabel("Particle");
            private JLabel particleLabelPoseX = new JLabel("X: ");
            private JLabel particleLabelPoseY = new JLabel("Y: ");
            private JLabel particleLabelWeight = new JLabel("W: ");
            private JLabel particleValuePoseX = new JLabel();
            private JLabel particleValuePoseY = new JLabel();
            private JLabel particleValueWeight = new JLabel();

            /**
             * Constructor.
             */
            TwoDimensionalControlsSubPanel() {
                JPanel movementLimitationControls = createMovementLimitationSelectionControls();
                JPanel sensorSelectionControls = createSensorSelectionControls();
                JPanel mouseClickParticleInfo = createMouseClickParticleInfo();
                this.add(movementLimitationControls);
                this.add(sensorSelectionControls);
                this.add(mouseClickParticleInfo);
                this.addComponentListener(guiController.new TwoDimensionalControlSubPanelComponentListener());
            }


            /**
             * Refreshes the information about the selected particle on display.
             */
            void refreshParticleInfo() {
                Particle selectedParticle = guiModel.getLocalizationModel().getSelectedParticle();
                particleValuePoseX.setText(String.valueOf(selectedParticle.getPose().getX()));
                particleValuePoseY.setText(String.valueOf(selectedParticle.getPose().getY()));
                particleValueWeight.setText(String.valueOf(Math.round(selectedParticle.getWeight() * 10000000.0) / 10000000.0));
            }


            /**
             * Returns a JPanel holding the input-elements to choose (or limit) robot-motion.
             *
             * @return  A JPanel holding robot-motion input-elements
             */
            private JPanel createMovementLimitationSelectionControls() {
                JPanel container = new JPanel();
                container.setLayout(new GridLayout(2,2));
                ButtonGroup angleGroup = new ButtonGroup();
                useRandomAnglesWhenTurning.setSelected(guiModel.isUseFreeAngles());
                useRightAnglesWhenTurning.setSelected(guiModel.isUseRightAngles());
                useRandomAnglesWhenTurning.addActionListener(guiController.new useRandomAnglesWhenTurningActionListener());
                useRightAnglesWhenTurning.addActionListener(guiController.new useRightAnglesWhenTurningActionListener());
                angleGroup.add(useRandomAnglesWhenTurning);
                angleGroup.add(useRightAnglesWhenTurning);
                container.add(useRandomAnglesWhenTurning);
                container.add(useRightAnglesWhenTurning);
                return container;
            }


            /**
             * Returns a JPanel holding the input-elements to specify sensor-usage.
             *
             * @return  A JPanel holding the input-elements to specify sensor-usage
             */
            private JPanel createSensorSelectionControls() {
                JPanel sensorContainer = new JPanel();
                sensorContainer.setLayout(new GridLayout(3,1));
                measureDistanceToLeft.setSelected(guiModel.isUseLeftSensor());
                measureDistanceAhead.setSelected(guiModel.isUseFrontSensor());
                measureDistanceToRight.setSelected(guiModel.isUseRightSensor());
                measureDistanceToLeft.addActionListener(guiController.new measureDistanceToLeftActionListener());
                measureDistanceAhead.addActionListener(guiController.new measureDistanceAheadActionListener());
                measureDistanceToRight.addActionListener(guiController.new measureDistanceToRightActionListener());
                sensorContainer.add(measureDistanceToLeft);
                sensorContainer.add(measureDistanceAhead);
                sensorContainer.add(measureDistanceToRight);
                return sensorContainer;
            }


            /**
             * Returns a JPanel to display information about a selected particle.
             * @return  A JPanel to display information about a selected particle
             */
            private JPanel createMouseClickParticleInfo() {
                JPanel labels = new JPanel();
                labels.setLayout(new GridLayout(3,1));
                labels.add(particleLabelPoseX);
                labels.add(particleLabelPoseY);
                labels.add(particleLabelWeight);

                JPanel values = new JPanel();
                values.setLayout(new GridLayout(3,1));
                values.add(particleValuePoseX);
                values.add(particleValuePoseY);
                values.add(particleValueWeight);

                JPanel container = new JPanel();
                container.setLayout(new BorderLayout());
                container.add(particleLabelInfo, BorderLayout.PAGE_START);
                container.add(labels, BorderLayout.LINE_START);
                container.add(values, BorderLayout.CENTER);

                return container;
            }
        }





        /**
         * Realises the camera-tab for the environment-specific control-tabs.
         */
        private class CameraControlsSubPanel extends JPanel {
            private JCheckBox camPerformGeneralQuery = new JCheckBox("General Query");
            private JCheckBox camPerformAngleQuery = new JCheckBox("Angle Query");
            private JCheckBox camPerformSignatureQueryForSignature1 = new JCheckBox("Signature 1");
            private JCheckBox camPerformSignatureQueryForSignature2 = new JCheckBox("Signature 2");
            private JCheckBox camPerformSignatureQueryForSignature3 = new JCheckBox("Signature 3");
            private JCheckBox camPerformSignatureQueryForSignature4 = new JCheckBox("Signature 4");
            private JCheckBox camPerformSignatureQueryForSignature5 = new JCheckBox("Signature 5");
            private JCheckBox camPerformSignatureQueryForSignature6 = new JCheckBox("Signature 6");
            private JCheckBox camPerformSignatureQueryForSignature7 = new JCheckBox("Signature 7");


            /**
             * Constructor.
             */
            CameraControlsSubPanel() {
                JPanel camSelections = createCameraSelectionControls();
                this.add(camSelections);
                this.addComponentListener(guiController.new TwoDimWithCameraControlSubPanelComponentListener());
            }


            /**
             * Return a JPanel holding the input-elements to choose the extend of camera-usage.
             *
             * @return  A JPanel holding the input-elements to choose the extend of camera-usage
             */
            private JPanel createCameraSelectionControls() {
                JPanel container = new JPanel();
                container.setLayout(new GridLayout(3, 4));
                setCameraSelectionsAccordingToModel();
                addCameraSelectionActionListener();
                container.add(camPerformGeneralQuery);
                container.add(camPerformAngleQuery);
                container.add(new JLabel());
                container.add(new JLabel());
                container.add(camPerformSignatureQueryForSignature1);
                container.add(camPerformSignatureQueryForSignature2);
                container.add(camPerformSignatureQueryForSignature3);
                container.add(camPerformSignatureQueryForSignature4);
                container.add(camPerformSignatureQueryForSignature5);
                container.add(camPerformSignatureQueryForSignature6);
                container.add(camPerformSignatureQueryForSignature7);
                return container;
            }


            /**
             * Synchronizes the camera-input-elements with the data-model.
             */
            private void setCameraSelectionsAccordingToModel() {
                camPerformGeneralQuery.setSelected(guiModel.isUseGeneralQuery());
                camPerformAngleQuery.setSelected(guiModel.isUseAngleQuery());
                camPerformSignatureQueryForSignature1.setSelected(guiModel.isUseSignatureOne());
                camPerformSignatureQueryForSignature2.setSelected(guiModel.isUseSignatureTwo());
                camPerformSignatureQueryForSignature3.setSelected(guiModel.isUseSignatureThree());
                camPerformSignatureQueryForSignature4.setSelected(guiModel.isUseSignatureFour());
                camPerformSignatureQueryForSignature5.setSelected(guiModel.isUseSignatureFive());
                camPerformSignatureQueryForSignature6.setSelected(guiModel.isUseSignatureSix());
                camPerformSignatureQueryForSignature7.setSelected(guiModel.isUseSignatureSeven());
            }


            /**
             * Adds ActionListeners to the camera-input-elements.
             */
            private void addCameraSelectionActionListener() {
                camPerformGeneralQuery.addActionListener(guiController.new useCamGeneralQueryActionListener());
                camPerformAngleQuery.addActionListener(guiController.new useCamAngleQueryActionListener());
                camPerformSignatureQueryForSignature1.addActionListener(guiController.new useCamSignatureOneQueryActionListener());
                camPerformSignatureQueryForSignature2.addActionListener(guiController.new useCamSignatureTwoQueryActionListener());
                camPerformSignatureQueryForSignature3.addActionListener(guiController.new useCamSignatureThreeQueryActionListener());
                camPerformSignatureQueryForSignature4.addActionListener(guiController.new useCamSignatureFourQueryActionListener());
                camPerformSignatureQueryForSignature5.addActionListener(guiController.new useCamSignatureFiveQueryActionListener());
                camPerformSignatureQueryForSignature6.addActionListener(guiController.new useCamSignatureSixQueryActionListener());
                camPerformSignatureQueryForSignature7.addActionListener(guiController.new useCamSignatureSevenQueryActionListener());
            }
        }





        /**
         * Realises the replay-tab for the environment-specific control-tabs.
         */
        class ReplayControlsSubPanel extends JPanel {
            JButton replayButton = new JButton("Replay");


            /**
             * Constructor.
             */
            ReplayControlsSubPanel() {
                this.setLayout(new FlowLayout());
                String files[] = getLocalizationLogFiles();
                JComboBox fileSelection = new JComboBox(files);
                fileSelection.addActionListener(guiController.new ReplayFileSelectionActionListener(this));
                replayButton.addActionListener(guiController.new ReplayButtonActionListener());
                this.addComponentListener(guiController.new ReplayControlSubPanelComponentListener());
                //TODO Add particle selection to replayButton.
                this.add(fileSelection);
            }

            /**
             * Returns the names of the saved localization-files as String[].
             *
             * @return The names of the saved localization-files as String[]
             */
            private String[] getLocalizationLogFiles() {
                File file = new File("./");
                File fileList[] = file.listFiles();
                ArrayList<String> names = new ArrayList<>();
                for (File f : fileList) {
                    String fileName = f.getName();
                    if (fileName.endsWith(".log")) {
                        fileName = fileName.replace(".log", "");
                        names.add(fileName);
                    }
                }
                String fileNames[] = new String[names.size()];
                names.toArray(fileNames);
                return fileNames;
            }
        }
    }





    class ClientMapPanel extends MapPanel {
        private static final int PARTICLE_DIAMETER = 4;
        private GuiConfigurationImplClientModel model;


        /**
         * Constructor.
         *
         * @param model The GUI-(data-)model
         */
        ClientMapPanel(GuiConfigurationImplClientModel model) {
            super(model.getMap());
            this.model = model;
        }


        /**
         * Paints the client-GUI-specific extensions of the MapPanel.
         *
         * @param g The graphical context
         */
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (model.isInReplayMode()) {
                if (model.getReplayModel().getWorldStatesForReplay() != null) {
                    paintReplayState(g);
                }
            } else {
                paintLocalizationProgress(g);
            }
        }


        /**
         * Paints the current ReplayState as indicated by the models replay-pointer.
         *
         * @param g The graphical context
         */
        private void paintReplayState(Graphics g) {
            int replayPointer = model.getReplayModel().getReplayPointer();
            WorldState ws = model.getReplayModel().getWorldStatesForReplay().get(replayPointer);
            paintParticles(g, ws.getParticles());
            double[] botPose = ws.getEstimatedBotPose();
            int radius = (int)Math.ceil(ws.getEstimatedBotPoseSpreading());
            paintBotPoseEstimation(g, botPose, radius);
            g.drawString("Op: " + ws.getCausativeInstruction(), 10, 100);
        }


        /**
         * Paints the current localization-step (the state of the localization-provider).
         *
         * @param g The graphical context
         */
        private void paintLocalizationProgress(Graphics g) {
            LocalizationProvider localizationProvider = model.getLocalizationProvider();

            if (localizationProvider == null) {
                return;
            }

            paintParticles(g, localizationProvider.getParticles());

            double[] botPose = localizationProvider.getEstimatedPose();
            g.setColor(localizationProvider.isLocalizationDone() ? Color.GREEN : Color.RED);
            int radius = (int)Math.ceil(localizationProvider.getSpreadingAroundEstimatedBotPose());
            int acceptableSpreading = localizationProvider.getAcceptableSpreading();
            radius = radius < acceptableSpreading ? acceptableSpreading : radius;

            paintBotPoseEstimation(g, botPose, radius);
        }


        /**
         * Paints all particles in a given list.
         *
         * @param g The graphical context
         * @param particles A list (ArrayList) of particles
         */
        private void paintParticles(Graphics g, ArrayList<Particle> particles) {
            if (particles != null) {
                for (Particle p : particles) {
                    p.paint(g, PARTICLE_DIAMETER, getScaleFactor(), getXOffset(), getYOffset());
                }
            }
        }


        /**
         * Paints information about the estimated robot-pose and a circle representing the spreading-range
         * of the particles around the estimated robot-position.
         *
         * @param g The graphical context
         * @param botPose   The estimated robot-pose
         * @param radius    The spreading-range of the particles.
         */
        private void paintBotPoseEstimation(Graphics g, double[] botPose, int radius) {
            g.drawOval(
                    ((int)Math.round(botPose[0])-radius) * getScaleFactor() + getXOffset(),
                    ((int)Math.round(botPose[1])-radius) * getScaleFactor() + getYOffset(),
                    radius * 2 * getScaleFactor(),
                    radius * 2 * getScaleFactor());
            g.drawString("Deviation: " + String.valueOf(radius),10,40);

            g.setColor(Color.BLACK);
            g.drawString("Estimated Bot Position: ", 10,20);
            g.drawString("X: " + String.valueOf((int)Math.round(botPose[0])),10,55);
            g.drawString("Y: " + String.valueOf((int)Math.round(botPose[1])), 10,70);
            g.drawString("H: " + String.valueOf((int)Math.round(botPose[2])), 10,85);
        }
    }
}
