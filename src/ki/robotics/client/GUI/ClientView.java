package ki.robotics.client.GUI;

import ki.robotics.client.MCL.LocalizationProvider;
import ki.robotics.client.MCL.WorldState;
import ki.robotics.server.robot.virtualRobots.MCLParticle;
import ki.robotics.utility.gui.ExtButtonGroup;
import ki.robotics.utility.gui.ExtJPanel;
import ki.robotics.utility.map.MapPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

class ClientView extends JFrame {
    static final String WINDOW_TITLE = "Monte Carlo Localization";
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 800;

    private ClientModel guiModel;
    private GuiControllerImplClientController guiController;

    private JPanel controlPanel;
    private ClientMapPanel mapPanel;

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

    private JRadioButton startFollowingLineFromLeft = new JRadioButton("Start from left");
    private JRadioButton startFollowingLineFromRight = new JRadioButton("Start from right");

    private JCheckBox camPerformGeneralQuery = new JCheckBox("General Query");
    private JCheckBox camPerformAngleQuery = new JCheckBox("Angle Query");
    private JCheckBox camPerformSignatureQueryForSignature1 = new JCheckBox("Signature 1");
    private JCheckBox camPerformSignatureQueryForSignature2 = new JCheckBox("Signature 2");
    private JCheckBox camPerformSignatureQueryForSignature3 = new JCheckBox("Signature 3");
    private JCheckBox camPerformSignatureQueryForSignature4 = new JCheckBox("Signature 4");
    private JCheckBox camPerformSignatureQueryForSignature5 = new JCheckBox("Signature 5");
    private JCheckBox camPerformSignatureQueryForSignature6 = new JCheckBox("Signature 6");
    private JCheckBox camPerformSignatureQueryForSignature7 = new JCheckBox("Signature 7");


    JTextField distancePerTravelInstruction = new JTextField( 5);
    JTextField numberOfParticles = new JTextField(5);
    JLabel labelForAcceptableLocalizationDeviationSlider = new JLabel();
    JSlider acceptableLocalizationDeviationSlider = new JSlider(1,25);
    private JCheckBox stopWhenLocalizationIsFinished = new JCheckBox("Stop when done");

    JButton start = new JButton("Start");
    JButton replay = new JButton("Replay");




    public ClientView(GuiControllerImplClientController guiController, ClientModel guiModel) {
        this.guiController = guiController;
        this.guiModel = guiModel;
        this.mapPanel = new ClientMapPanel(guiModel);
    }

    public void initializeView() {
        createWindow();
        addKeyControls();
        this.setVisible(true);
    }

    JPanel getControlPanel() { return controlPanel; }
    ClientMapPanel getMapPanel() { return  mapPanel; }

    void refreshParticleInfo() {
        MCLParticle selectedParticle = guiModel.getSelectedParticle();
        particleValuePoseX.setText(String.valueOf(selectedParticle.getPose().getX()));
        particleValuePoseY.setText(String.valueOf(selectedParticle.getPose().getY()));
        particleValueWeight.setText(String.valueOf(Math.round(selectedParticle.getWeight() * 10000000.0) / 10000000.0));
    }

    private void createWindow() {
        this.setTitle(WINDOW_TITLE);
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.PAGE_START);
        add(mapPanel, BorderLayout.CENTER);
        addMouseListener(guiController.new ParticleSelectionMouseListener());
    }


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



    public void updateWindowAfterLocalizationFinished() {
        ActionListener[] listeners = start.getActionListeners();
        for (ActionListener l : listeners) {
            start.removeActionListener(l);
        }
        start.setText("Start");
        start.addActionListener(guiController.new StartButtonActionListener());

        this.setTitle(WINDOW_TITLE);
        guiController.postLocalizationWork();
    }




    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        JTabbedPane environmentSpecificComponents = createEnvironmentSpecificComponents();
        controlPanel.add(environmentSpecificComponents);
        ExtJPanel commonComponents = createCommonComponents();
        controlPanel.add(commonComponents);
        return controlPanel;
    }


    private JTabbedPane createEnvironmentSpecificComponents() {
        JTabbedPane environmentSpecificControlSubPanels = new JTabbedPane();
        ExtJPanel oneDimensionalControls = createOneDimensionalControlSubPanel();
        ExtJPanel twoDimensionalControls = createTwoDimensionalControlSubPanel();
        ExtJPanel twoDimWithCameraControls = createTwoDimWithCameraControlSubPanel();
        ExtJPanel localizationReplay = createReplayControlSubPanel();
        environmentSpecificControlSubPanels.addTab("One-D", oneDimensionalControls);
        environmentSpecificControlSubPanels.addTab("Two-D", twoDimensionalControls);
        environmentSpecificControlSubPanels.addTab("Camera", twoDimWithCameraControls);
        environmentSpecificControlSubPanels.addTab("Replay", localizationReplay);
        return environmentSpecificControlSubPanels;
    }



    private ExtJPanel createOneDimensionalControlSubPanel() {
        ExtJPanel subPanel = new ExtJPanel();
        ExtButtonGroup lineFollowingInitialDirection = new ExtButtonGroup();
        startFollowingLineFromLeft.setSelected(guiModel.isStartFromLeft());
        startFollowingLineFromRight.setSelected(guiModel.isStartFromRight());
        startFollowingLineFromLeft.addActionListener(guiController.new startLineFollowingFromLeftActionListener());
        startFollowingLineFromRight.addActionListener(guiController.new startLineFollowingFromRightActionListener());
        lineFollowingInitialDirection.addAll(startFollowingLineFromLeft, startFollowingLineFromRight);
        subPanel.addAll(startFollowingLineFromLeft, startFollowingLineFromRight);
        subPanel.addComponentListener(guiController.new OneDimensionalControlSubPanelComponentListener());
        return subPanel;
    }



    private ExtJPanel createTwoDimensionalControlSubPanel() {
        ExtJPanel controls = new ExtJPanel();
        ExtJPanel movementLimitationControls = createMovementLimitationSelectionControls();
        ExtJPanel sensorSelectionControls = createSensorSelectionControls();
        ExtJPanel mouseClickParticleInfo = createMouseClickParticleInfo();
        controls.addAll(movementLimitationControls, sensorSelectionControls, mouseClickParticleInfo);
        controls.addComponentListener(guiController.new TwoDimensionalControlSubPanelComponentListener());
        return controls;
    }



    private ExtJPanel createTwoDimWithCameraControlSubPanel() {
        ExtJPanel controls = new ExtJPanel();
        ExtJPanel camSelections = createCameraSelectionControls();
        controls.add(camSelections);
        controls.addComponentListener(guiController.new TwoDimWithCameraControlSubPanelComponentListener());
        return controls;
    }



    private ExtJPanel createCameraSelectionControls() {
        ExtJPanel container = new ExtJPanel();
        container.setLayout(new GridLayout(3, 4));
        setCameraSelectionsAccordingToModel();
        addCameraSelectionActionListener();
        container.addAll(
                camPerformGeneralQuery, camPerformAngleQuery, new JLabel(), new JLabel(),
                camPerformSignatureQueryForSignature1, camPerformSignatureQueryForSignature2,
                camPerformSignatureQueryForSignature3, camPerformSignatureQueryForSignature4,
                camPerformSignatureQueryForSignature5, camPerformSignatureQueryForSignature6,
                camPerformSignatureQueryForSignature7
        );
        return container;
    }



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


    private ExtJPanel createReplayControlSubPanel() {
        ExtJPanel container = new ExtJPanel();
        container.setLayout(new FlowLayout());
        String files[] = getLocalizationLogFiles();
        JComboBox fileSelection = new JComboBox(files);
        fileSelection.addActionListener(guiController.new ReplayFileSelectionActionListener());
        replay.addActionListener(guiController.new ReplayButtonActionListener());
        container.addComponentListener(guiController.new ReplayControlSubPanelComponentListener());
        //container.addAll(fileSelection, replay);
        //container.addAll(fileSelection, createMouseClickParticleInfo());
        //TODO Add particle selection to replay.
        container.addAll(fileSelection);
        return container;
    }

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


    private ExtJPanel createMovementLimitationSelectionControls() {
        ExtJPanel container = new ExtJPanel();
        container.setLayout(new GridLayout(2,2));
        ExtButtonGroup angleGroup = new ExtButtonGroup();
        useRandomAnglesWhenTurning.setSelected(guiModel.isUseFreeAngles());
        useRightAnglesWhenTurning.setSelected(guiModel.isUseRightAngles());
        useRandomAnglesWhenTurning.addActionListener(guiController.new useRandomAnglesWhenTurningActionListener());
        useRightAnglesWhenTurning.addActionListener(guiController.new useRightAnglesWhenTurningActionListener());
        angleGroup.addAll(useRandomAnglesWhenTurning, useRightAnglesWhenTurning);
        container.addAll(useRandomAnglesWhenTurning, useRightAnglesWhenTurning);
        return container;
    }



    private ExtJPanel createSensorSelectionControls() {
        ExtJPanel sensorContainer = new ExtJPanel();
        sensorContainer.setLayout(new GridLayout(3,1));
        measureDistanceToLeft.setSelected(guiModel.isUseLeftSensor());
        measureDistanceAhead.setSelected(guiModel.isUseFrontSensor());
        measureDistanceToRight.setSelected(guiModel.isUseRightSensor());
        measureDistanceToLeft.addActionListener(guiController.new measureDistanceToLeftActionListener());
        measureDistanceAhead.addActionListener(guiController.new measureDistanceAheadActionListener());
        measureDistanceToRight.addActionListener(guiController.new measureDistanceToRightActionListener());
        sensorContainer.addAll(measureDistanceToLeft, measureDistanceAhead, measureDistanceToRight);
        return sensorContainer;
    }


    private ExtJPanel createMouseClickParticleInfo() {
        ExtJPanel labels = new ExtJPanel();
        ExtJPanel values = new ExtJPanel();
        ExtJPanel container = new ExtJPanel();

        labels.setLayout(new GridLayout(3,1));
        values.setLayout(new GridLayout(3,1));
        container.setLayout(new BorderLayout());

        labels.addAll(particleLabelPoseX, particleLabelPoseY, particleLabelWeight);
        values.addAll(particleValuePoseX, particleValuePoseY, particleValueWeight);

        container.add(particleLabelInfo, BorderLayout.PAGE_START);
        container.add(labels, BorderLayout.LINE_START);
        container.add(values, BorderLayout.CENTER);

        return container;
    }


    private ExtJPanel createCommonComponents() {
        ExtJPanel commonComponents = new ExtJPanel();
        commonComponents.setLayout(new GridLayout(5,1));

        labelForAcceptableLocalizationDeviationSlider.setText("Acceptable Tolerance: " + guiModel.getAcceptableSpreading() + " cm");
        acceptableLocalizationDeviationSlider.setValue(guiModel.getAcceptableSpreading());
        acceptableLocalizationDeviationSlider.addChangeListener(guiController.new DeviationSliderChangeListener());

        ExtJPanel firstRow = new ExtJPanel();
        firstRow.setLayout(new GridLayout(1,3));
        JLabel stepLabel = new JLabel("Step size: ");
        stepLabel.setLabelFor(distancePerTravelInstruction);
        distancePerTravelInstruction.setHorizontalAlignment(JTextField.RIGHT);
        distancePerTravelInstruction.setText(String.valueOf(guiModel.getStepSize()));
        start.addActionListener(guiController.new StartButtonActionListener());
        firstRow.addAll(stepLabel, distancePerTravelInstruction, start);

        ExtJPanel secondRow = new ExtJPanel();
        secondRow.setLayout(new GridLayout(1,3));
        JLabel particleCnt = new JLabel("Particles: ");
        particleCnt.setLabelFor(numberOfParticles);
        numberOfParticles.setHorizontalAlignment(JTextField.RIGHT);
        numberOfParticles.setText(String.valueOf(guiModel.getNumberOfParticles()));
        secondRow.addAll(particleCnt, numberOfParticles, new JLabel());

        stopWhenLocalizationIsFinished.setSelected(guiModel.isStopWhenDone());
        stopWhenLocalizationIsFinished.addActionListener(guiController.new setStopWhenLocalizationFinishedActionListener());
        commonComponents.addAll(
                labelForAcceptableLocalizationDeviationSlider, acceptableLocalizationDeviationSlider,
                firstRow, secondRow, stopWhenLocalizationIsFinished);

        return commonComponents;
    }



    class ClientMapPanel extends MapPanel {
        private static final int PARTICLE_DIAMETER = 4;
        private ClientModel model;


        ClientMapPanel(ClientModel model) {
            super(model.getMap());
            this.model = model;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (model.isInReplayMode()) {
                if (model.getWorldStatesForReplay() != null) {
                    paintReplayState(g);
                }
            } else {
                paintLocalizationProgress(g);
            }
        }


        private void paintReplayState(Graphics g) {
            int replayPointer = model.getReplayPointer();
            WorldState ws = model.getWorldStatesForReplay().get(replayPointer);
            paintParticles(g, ws.getParticles());
            double[] botPose = ws.getEstimatedBotPose();
            int radius = (int)Math.ceil(ws.getEstimatedBotPoseDeviation());
            paintBotPoseEstimation(g, botPose, radius);
            g.drawString("Op: " + ws.getCausativeInstruction(), 10, 100);
        }


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



        private void paintParticles(Graphics g, ArrayList<MCLParticle> particles) {
            if (particles != null) {
                for (MCLParticle p : particles) {
                    p.paint(g, PARTICLE_DIAMETER, getScaleFactor(), getXOffset(), getYOffset());
                }
            }
        }


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
