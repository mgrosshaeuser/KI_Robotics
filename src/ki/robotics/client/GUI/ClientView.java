package ki.robotics.client.GUI;

import ki.robotics.client.ComController;
import ki.robotics.client.MCL.MCL_Provider;
import ki.robotics.common.ExtButtonGroup;
import ki.robotics.common.ExtJPanel;
import ki.robotics.common.MapPanel;
import ki.robotics.robot.MCLParticle;
import lejos.robotics.navigation.Pose;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ClientView extends JFrame {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 800;

    private ClientModel guiModel;
    private ClientController guiController;

    private ClientMapPanel mapPanel;

    private JRadioButton useRightAnglesWhenTurning = new JRadioButton("90° Angles");
    private JRadioButton useRandomAnglesWhenTurning = new JRadioButton("Free Angles");
    private JCheckBox measureDistanceToLeft = new JCheckBox("Left sensor");
    private JCheckBox measureDistanceAhead = new JCheckBox("Front sensor");
    private JCheckBox measureDistanceToRight = new JCheckBox("Right sensor");

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
    private JButton stop = new JButton("Stop");




    public ClientView(ComController comController) {
        this.guiModel = new ClientModel();
        this.guiController = new ClientController(guiModel, this, comController);
        this.mapPanel = new ClientMapPanel(this, guiModel);
        createWindow();
    }


    ClientMapPanel getMapPanel() { return  mapPanel; }



    private void createWindow() {
        this.setTitle("Monte Carlo Localization");
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.PAGE_START);
        add(mapPanel, BorderLayout.CENTER);
        this.setVisible(true);
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
        environmentSpecificControlSubPanels.addTab("One-D", oneDimensionalControls);
        environmentSpecificControlSubPanels.addTab("Two-D", twoDimensionalControls);
        environmentSpecificControlSubPanels.addTab("Camera", twoDimWithCameraControls);
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
        controls.addAll(movementLimitationControls, sensorSelectionControls);
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





    private ExtJPanel createCommonComponents() {
        ExtJPanel commonComponents = new ExtJPanel();
        commonComponents.setLayout(new GridLayout(5,1));

        labelForAcceptableLocalizationDeviationSlider.setText("Acceptable Tolerance: " + guiModel.getAcceptableTolerance() + " cm");
        acceptableLocalizationDeviationSlider.setValue(guiModel.getAcceptableTolerance());
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
        stop.addActionListener(guiController.new StopButtonActionListener());
        secondRow.addAll(particleCnt, numberOfParticles, stop);

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


        ClientMapPanel(ClientView parent, ClientModel model) {
            super(parent, model.getMap());
            this.model = model;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);

            MCL_Provider mclProvider = model.getMclProvider();

            if (mclProvider == null) {
                return;
            }

            ArrayList<MCLParticle> particles = mclProvider.getParticles();
            if (particles != null) {
                float medianWeight = mclProvider.getMedianParticleWeight();
                for (MCLParticle p : particles) {
                    p.paint(g, PARTICLE_DIAMETER, getScaleFactor(), getxOffset(), getyOffset(), medianWeight);
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


}
