package ki.robotics.client.GUI;

import ki.robotics.common.ExtButtonGroup;
import ki.robotics.common.ExtJPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;



public class ClientGUIControlPanel extends JPanel {
    private ClientGUI parent;
    private ClientGUIModel model;

    private JRadioButton turnRightAngle = new JRadioButton("90° Angles");
    private JRadioButton turnFree = new JRadioButton("Free Angles");
    private JCheckBox leftSensor = new JCheckBox("Left sensor");
    private JCheckBox frontSensor = new JCheckBox("Front sensor");
    private JCheckBox rightSensor = new JCheckBox("Right sensor");

    private JRadioButton startFromLeft = new JRadioButton("Start from left");
    private JRadioButton startFromRight = new JRadioButton("Start from right");

    private JCheckBox camGeneralQuery = new JCheckBox("General Query");
    private JCheckBox camAngleQuery = new JCheckBox("Angle Query");
    private JCheckBox camSignature1 = new JCheckBox("Signature 1");
    private JCheckBox camSignature2 = new JCheckBox("Signature 2");
    private JCheckBox camSignature3 = new JCheckBox("Signature 3");
    private JCheckBox camSignature4 = new JCheckBox("Signature 4");
    private JCheckBox camSignature5 = new JCheckBox("Signature 5");
    private JCheckBox camSignature6 = new JCheckBox("Signature 6");
    private JCheckBox camSignature7 = new JCheckBox("Signature 7");

    private JTextField stepSize = new JTextField( 5);
    private JTextField numberOfParticles = new JTextField(5);
    private JButton start = new JButton("Start");
    private JButton stop = new JButton("Stop");
    private JLabel toleranceLabel = new JLabel();
    private JSlider toleranceSlider = new JSlider(1,25);
    private JCheckBox stopWhenDone = new JCheckBox("Stop when done");



    public ClientGUIControlPanel(ClientGUI parent, ClientGUIModel model) {
        this.parent = parent;
        this.model = model;
        this.setLayout(new FlowLayout());

        JTabbedPane specificComponents = createEnvironmentSpecificComponents();
        this.add(specificComponents);

        ExtJPanel commonComponents = createCommonComponents();
        this.add(commonComponents);
    }



    private JTabbedPane createEnvironmentSpecificComponents() {
        JTabbedPane environmentSpecificControls = new JTabbedPane();
        ExtJPanel oneDimensionalControls = createOneDimensionalControlsSubPanel();
        ExtJPanel twoDimensionalControls = createTwoDimensionalControlsSubPanel();
        ExtJPanel twoDimWithCameraControls = createTwoDimWithCameraControlsSubPanel();
        environmentSpecificControls.addTab("One-D", oneDimensionalControls);
        environmentSpecificControls.addTab("Two-D", twoDimensionalControls);
        environmentSpecificControls.addTab("Camera", twoDimWithCameraControls);
        return environmentSpecificControls;
    }



    private ExtJPanel createCommonComponents() {
        ExtJPanel commonComponents = new ExtJPanel();
        commonComponents.setLayout(new GridLayout(5,1));

        toleranceLabel.setText("Acceptable Tolerance: " + model.getAcceptableTolerance() + " cm");
        toleranceSlider.setValue(model.getAcceptableTolerance());
        toleranceSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                toleranceLabel.setText("Acceptable Tolerance: " + toleranceSlider.getValue() + " cm");
            }
        });

        ExtJPanel firstRow = new ExtJPanel();
        firstRow.setLayout(new GridLayout(1,3));
        JLabel stepLabel = new JLabel("Step size: ");
        stepLabel.setLabelFor(stepSize);
        stepSize.setHorizontalAlignment(JTextField.RIGHT);
        stepSize.setText(String.valueOf(model.getStepSize()));
        start.addActionListener(new ClientGUIControlPanel.StartButtonActionListener());
        firstRow.addAll(stepLabel, stepSize, start);

        ExtJPanel secondRow = new ExtJPanel();
        secondRow.setLayout(new GridLayout(1,3));
        JLabel particleCnt = new JLabel("Particles: ");
        particleCnt.setLabelFor(numberOfParticles);
        numberOfParticles.setHorizontalAlignment(JTextField.RIGHT);
        numberOfParticles.setText(String.valueOf(model.getNumberOfParticles()));
        stop.addActionListener(new ClientGUIControlPanel.StopButtonActionListener());
        secondRow.addAll(particleCnt, numberOfParticles, stop);

        stopWhenDone.setSelected(model.isStopWhenDone());
        stopWhenDone.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setStopWhenDone(stopWhenDone.isSelected());
            }
        });
        commonComponents.addAll(toleranceLabel, toleranceSlider, firstRow, secondRow, stopWhenDone);

        return commonComponents;
    }



    private ExtJPanel createOneDimensionalControlsSubPanel() {
        ExtJPanel controls = new ExtJPanel();
        ExtButtonGroup leftOrRight = new ExtButtonGroup();
        startFromLeft.setSelected(model.isStartFromLeft());
        startFromLeft.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setStartFromLeft();
            }
        });
        startFromRight.setSelected(model.isStartFromRight());
        startFromRight.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setStartFromRight();
            }
        });
        leftOrRight.addAll(startFromLeft, startFromRight);
        controls.addAll(startFromLeft, startFromRight);
        controls.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                model.setOneDimensional();
                parent.getMapPanel().setNewMap(model.getMap());
                parent.repaint();
            }
        });
        return controls;
    }




    private ExtJPanel createTwoDimensionalControlsSubPanel() {
        ExtJPanel controls = new ExtJPanel();
        ExtJPanel movementLimitationControls = createMovementLimitationSelectionControls();
        ExtJPanel sensorSelectionControls = createSensorSelectionControls();
        controls.addAll(movementLimitationControls, sensorSelectionControls);
        controls.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                model.setTwoDimensional();
                parent.getMapPanel().setNewMap(model.getMap());
                parent.repaint();
            }
        });
        return controls;
    }




    private ExtJPanel createTwoDimWithCameraControlsSubPanel() {
        ExtJPanel controls = new ExtJPanel();
        ExtJPanel camSelections = createCameraSelectionControls();
        controls.add(camSelections);
        controls.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                model.setWithCamera();
                parent.getMapPanel().setNewMap(model.getMap());
                parent.repaint();
            }
        });
        return controls;
    }



    private ExtJPanel createCameraSelectionControls() {
        ExtJPanel container = new ExtJPanel();
        container.setLayout(new GridLayout(3, 4));
        setCameraSelectionsAccordingToModel();
        addCameraSelectionActionListener();
        container.addAll(
                camGeneralQuery, camAngleQuery, new JLabel(), new JLabel(),
                camSignature1, camSignature2, camSignature3, camSignature4,
                camSignature5, camSignature6, camSignature7);
        return container;
    }



    private void setCameraSelectionsAccordingToModel() {
        camGeneralQuery.setSelected(model.isUseGeneralQuery());
        camAngleQuery.setSelected(model.isUseAngleQuery());
        camSignature1.setSelected(model.isUseSignatureOne());
        camSignature2.setSelected(model.isUseSignatureTwo());
        camSignature3.setSelected(model.isUseSignatureThree());
        camSignature4.setSelected(model.isUseSignatureFour());
        camSignature5.setSelected(model.isUseSignatureFive());
        camSignature6.setSelected(model.isUseSignatureSix());
        camSignature7.setSelected(model.isUseSignatureSeven());
    }



    private void addCameraSelectionActionListener() {
        camGeneralQuery.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setUseGeneralQuery(camGeneralQuery.isSelected());
            }
        });
        camAngleQuery.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setUseAngleQuery(camAngleQuery.isSelected());
            }
        });
        camSignature1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setUseSignatureOne(camSignature1.isSelected());
            }
        });
        camSignature2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setUseSignatureTwo(camSignature2.isSelected());
            }
        });
        camSignature3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setUseSignatureThree(camSignature3.isSelected());
            }
        });
        camSignature4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setUseSignatureFour(camSignature4.isSelected());
            }
        });
        camSignature5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setUseSignatureFive(camSignature5.isSelected());
            }
        });
        camSignature6.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setUseSignatureSix(camSignature6.isSelected());
            }
        });
        camSignature7.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setUseSignatureSeven(camSignature7.isSelected());
            }
        });
    }



    private ExtJPanel createMovementLimitationSelectionControls() {
        ExtJPanel container = new ExtJPanel();
        container.setLayout(new GridLayout(2,2));

        ExtButtonGroup angleGroup = new ExtButtonGroup();

        turnFree.setSelected(model.isUseFreeAngles());
        turnFree.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setUseFreeAngles();
            }
        });

        turnRightAngle.setSelected(model.isUseRightAngles());
        turnRightAngle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setUseRightAngles();
            }
        });

        angleGroup.addAll(turnFree, turnRightAngle);
        container.addAll(turnFree, turnRightAngle);
        return container;
    }



    private ExtJPanel createSensorSelectionControls() {
        ExtJPanel sensorContainer = new ExtJPanel();
        sensorContainer.setLayout(new GridLayout(3,1));

        leftSensor.setSelected(model.isUseLeftSensor());
        leftSensor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setUseLeftSensor(leftSensor.isSelected());
            }
        });

        frontSensor.setSelected(model.isUseFrontSensor());
        frontSensor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setUseFrontSensor(frontSensor.isSelected());
            }
        });

        rightSensor.setSelected(model.isUseRightSensor());
        rightSensor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setUseRightSensor(rightSensor.isSelected());
            }
        });

        sensorContainer.addAll(leftSensor, frontSensor, rightSensor);
        return sensorContainer;
    }



    /**
     * Action-Listener for the Start-Button.
     */
    private class StartButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            start.setEnabled(false);
            try {
                model.setStepSize(Integer.valueOf(stepSize.getText()));
                model.setNumberOfParticles(Integer.valueOf(numberOfParticles.getText()));
            } catch (NumberFormatException e1) {
                e1.printStackTrace();
            }
            parent.start();
        }
    }



    /**
     * Action-Listener for the Stop-Button.
     */
    private class StopButtonActionListener implements  ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            parent.stop();
            start.setEnabled(true);
        }
    }

}