package ki.robotics.client.GUI.impl;

import ki.robotics.client.GUI.GuiConfiguration;
import ki.robotics.client.GUI.GuiController;
import ki.robotics.client.MCL.LocalizationProvider;
import ki.robotics.client.MCL.Particle;
import ki.robotics.client.communication.ClientComController;
import ki.robotics.client.MCL.WorldState;
import ki.robotics.utility.map.Map;
import ki.robotics.utility.map.MapPanel;
import ki.robotics.utility.map.MapProvider;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Controller for the client-GUI.
 */
public class GuiControllerImplClientController implements GuiController {
    private GuiConfigurationImplClientModel guiModel;
    private ClientView guiView;
    private ClientComController clientComController;


    /**
     * Constructor
     *
     * @param clientComController   Message-interpreting intermediate to the client-communicator.
     */
    public GuiControllerImplClientController(ClientComController clientComController) {
        this.clientComController = clientComController;
        this.guiModel = new GuiConfigurationImplClientModel();
        this.guiView = new ClientView(this, guiModel);
        this.guiView.initializeView();
    }



    /**
     * Repaints the GUI-view.
     */
    public void repaintWindow() {
        this.guiView.repaint();
    }


    /**
     * Updates the GUI once a localization is finished.
     */
    public void updateWindowAfterLocalizationFinished() {
        this.guiView.updateWindowAfterLocalizationFinished();
    }


    /**
     * Starts the localization.
     */
    private void start() {
        int[] limitations = MapProvider.getInstance().getMapLimitations(guiModel.getMapKey());
        if (guiModel.isOneDimensional()   && guiModel.isStartFromRight()) {
            limitations[2] = 180;
        }
        guiModel.getLocalizationModel().createLocalizationProvider();
        guiView.setTitle(ClientView.WINDOW_TITLE + " | paused");
        clientComController.start();
        guiView.repaint();
    }


    /**
     * Stops the localization
     */
    private void stop() {
        clientComController.stop();
        guiView.repaint();
    }


    /**
     * Returns a sub-set of the GUI-(data-)model corresponding to the GuiConfiguration-interface.
     *
     * @return  A GuiConfiguration-sub-set of the GUI-(data-)model
     */
    @Override
    public GuiConfiguration getUserSettings() {
        return this.guiModel;
    }


    /**
     * Brings the GUI-(data-)model in an ordered post-localization-state.
     */
    void postLocalizationWork() {
        guiModel.getLocalizationModel().setPaused(true);
    }





    /**
     * Returns the Action to be performed when the user presses CTRL + SPACE.
     * That action is switching between running and pausing the localization, including the update of
     * status-information in the primary-window.
     *
     * @return  The Action to be performed when the user presses CTRL + SPACE
     */
    Action getSpacePressedAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LocalizationProvider localizationProvider;
                localizationProvider = guiModel.getLocalizationProvider();
                if (guiModel.isInReplayMode()   ||   localizationProvider == null) {
                    return;
                }
                if (guiModel.isPaused()) {
                        guiModel.getLocalizationModel().setPaused(false);
                        guiModel.getLocalizationProvider().resetToLatestWorldState();
                        guiView.setTitle(ClientView.WINDOW_TITLE + " | running");
                } else {
                    guiModel.getLocalizationModel().setPaused(true);
                    guiView.setTitle(ClientView.WINDOW_TITLE + " | paused");
                }
                guiView.repaint();
            }

        };
    }


    /**
     * Returns the Action to be performed when the user presses CTRL + LEFT ARROW.
     * That action is navigating backward one step in the localization-history
     *
     * @return  The Action to be performed when the user presses CTRL + LEFT ARROW
     */
    Action getLeftArrowPressedAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (guiModel.isInReplayMode()) {
                    if (guiModel.getReplayModel().getWorldStatesForReplay() == null) {
                        return;
                    }
                    int replayPointer = guiModel.getReplayModel().getReplayPointer();
                    guiModel.getReplayModel().setReplayPointer(--replayPointer);
                    guiView.repaint();
                    return;
                }
                if (guiModel.isPaused()) {
                    LocalizationProvider localizationProvider = guiModel.getLocalizationProvider();
                    if (localizationProvider != null) {
                        guiModel.getLocalizationProvider().stepBackInLocalizationHistory();
                        guiView.repaint();
                    }
                }
            }
        };
    }


    /**
     * Returns the Action to be performed when the user presses CTRL + RIGHT ARROW.
     * That action is navigating forward one step in the localization-history
     *
     * @return  The Action to be performed when the user presses CTRL + RIGHT ARROW
     */
    Action getRightArrowPressedAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (guiModel.isInReplayMode()) {
                    if (guiModel.getReplayModel().getWorldStatesForReplay() == null) {
                        return;
                    }
                    int replayPointer = guiModel.getReplayModel().getReplayPointer();
                    guiModel.getReplayModel().setReplayPointer(++replayPointer);
                    guiView.repaint();
                    return;
                }
                if (guiModel.isPaused()) {
                    LocalizationProvider localizationProvider = guiModel.getLocalizationProvider();
                    if (localizationProvider != null) {
                        localizationProvider.stepForwardInLocalizationHistory();
                        guiView.repaint();
                    }
                }
            }
        };
    }


    /**
     * Listener for the Action: User starts localization by clicking the start-button (which then becomes the
     * stop-button).
     */
    public class StartButtonActionListener implements ActionListener {
        private ClientView.ControlPanel controlPanel;

        /**
         * Constructor
         *
         * @param controlPanel  Reference to the ControlPanel that holds the user-input- and control-elements.
         */
        StartButtonActionListener(ClientView.ControlPanel controlPanel) {
            this.controlPanel = controlPanel;
        }

        /**
         * Delegates model- and view-update and starts localization.
         *
         * @param e ActionEvent from the start-button
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (! guiModel.getLocalizationModel().isInReplayMode()) {
                updateModel();
                updateGUI();
                start();
            }
        }

        /**
         * Updates the data-model with the new user-inputs
         */
        private void updateModel() {
            int distancePerTravelInstruction = guiModel.getStepSize();
            int numberOfParticles = guiModel.getNumberOfParticles();
            try {
                distancePerTravelInstruction = Integer.valueOf(controlPanel.distancePerTravelInstruction.getText());
                numberOfParticles = Integer.valueOf(controlPanel.numberOfParticles.getText());
            } catch (NumberFormatException e1) {
                e1.printStackTrace();
            }
            if (distancePerTravelInstruction > 0) {
                guiModel.getLocalizationModel().setStepSize(distancePerTravelInstruction);
            }
            if (numberOfParticles > 0) {
                guiModel.getLocalizationModel().setNumberOfParticles(numberOfParticles);
            }
        }

        /**
         * Pre-start update of GUI.
         */
        private void updateGUI() {
            controlPanel.startButton.setText("Stop");
            controlPanel.startButton.removeActionListener(this);
            controlPanel.startButton.addActionListener(new StopButtonActionListener(controlPanel));
        }
    }


    /**
     * Listener for the Action: User stops ongoing localization by clicking the stop-button (which then becomes
     * the start-button).
     */
    public class StopButtonActionListener implements ActionListener {
        private ClientView.ControlPanel controlPanel;

        /**
         * Constructor
         *
         * @param controlPanel  Reference to the ControlPanel that holds the user-input- and control-elements.
         */
        StopButtonActionListener(ClientView.ControlPanel controlPanel) {
            this.controlPanel = controlPanel;
        }

        /**
         * Stops localization and updates view and model.
         *
         * @param e ActionEvent from the stop-button
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            stop();
            controlPanel.startButton.setText("Start");
            controlPanel.startButton.removeActionListener(this);
            controlPanel.startButton.addActionListener(new StartButtonActionListener(controlPanel));
            guiModel.getLocalizationModel().setPaused(true);
        }
    }


    public class ReplayButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            //TODO Implementation
        }
    }


    /**
     * ChangeListener for: User changes acceptable spreading through moving the slider.
     */
    public class DeviationSliderChangeListener implements ChangeListener {
        private ClientView.ControlPanel controlPanel;

        /**
         * Constructor
         *
         * @param controlPanel  Reference to the ControlPanel that holds the user-input- and control-elements.
         */
        DeviationSliderChangeListener(ClientView.ControlPanel controlPanel) {
            this.controlPanel = controlPanel;
        }

        /**
         * Updates gui and view with the user selected acceptable spreading.
         *
         * @param e ChangeEvent from the acceptable-spreading-slider
         */
        @Override
        public void stateChanged(ChangeEvent e) {
            int acceptableValue = controlPanel.acceptableLocalizationSpreadingSlider.getValue();
            guiModel.getLocalizationModel().setAcceptableSpreading(acceptableValue);
            String textToDisplay = "Acceptable Tolerance: " + acceptableValue + " cm";
            controlPanel.labelForAcceptableLocalizationSpreadingSlider.setText(textToDisplay);
        }
    }


    /**
     * ComponentListener for: User selects one-dimensional-environment-tab.
     */
    public class OneDimensionalControlSubPanelComponentListener extends ComponentAdapter {
        /**
         * Updates view and model. Repaints view.
         *
         * @param e ComponentEvent from the control-panels one-dimensional-tab.
         */
        @Override
        public void componentShown(ComponentEvent e) {
            super.componentShown(e);
            guiModel.getLocalizationModel().setOneDimensional();
            guiView.getMapPanel().setNewMap(guiModel.getMap());
            guiView.repaint();
        }
    }


    /**
     * ComponentListener for: User selects two-dimensional-environment-tab.
     */
    public class TwoDimensionalControlSubPanelComponentListener extends ComponentAdapter {
        /**
         * Updates view and model. Repaints view.
         *
         * @param e ComponentEvent from the control-panels two-dimensional-tab.
         */
        @Override
        public void componentShown(ComponentEvent e) {
            super.componentShown(e);
            guiModel.getLocalizationModel().setTwoDimensional();
            guiView.getMapPanel().setNewMap(guiModel.getMap());
            guiView.repaint();
        }
    }


    /**
     * ComponentListener for: User selects two-dimensional-environment-with-camera-tab.
     */
    public class TwoDimWithCameraControlSubPanelComponentListener extends ComponentAdapter {
        /**
         * Updates view and model. Repaints view.
         *
         * @param e ComponentEvent from the control-panels two-dimensional-camera-tab.
         */
        @Override
        public void componentShown(ComponentEvent e) {
            super.componentShown(e);
            guiModel.getLocalizationModel().setWithCamera();
            guiView.getMapPanel().setNewMap(guiModel.getMap());
            guiView.repaint();
        }
    }


    /**
     * ComponentListener for: User selects replay-tab.
     */
    public class ReplayControlSubPanelComponentListener extends ComponentAdapter {
        @Override
        public void componentShown(ComponentEvent e) {
            super.componentShown(e);
            guiModel.getLocalizationModel().setInReplayMode();
        }
    }


    /**
     * ActionListener for: User selects line-following from left for one-dimensional maps.
     */
    public class startLineFollowingFromLeftActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            guiModel.getMovementModel().setStartFromLeft();
        }
    }


    /**
     * ActionListener for: User selects line-following from right for one-dimensional maps.
     */
    public class startLineFollowingFromRightActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            guiModel.getMovementModel().setStartFromRight();
        }
    }


    /**
     * ActionListener for: User selects random-angles for robot motion on two-dimensional maps.
     */
    public class useRandomAnglesWhenTurningActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            guiModel.getMovementModel().setUseFreeAngles();
        }
    }


    /**
     * ActionListener for: User selects right-angles for robot motion on two-dimensional maps.
     */
    public class useRightAnglesWhenTurningActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            guiModel.getMovementModel().setUseRightAngles();
        }
    }


    /**
     * ActionListener for: User selects to use camera-general-queries.
     */
    public class useCamGeneralQueryActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.getCameraModel().setUseGeneralQuery(selected);
        }
    }


    /**
     * ActionListener for: User selects to use camera-angle-queries.
     */
    public class useCamAngleQueryActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.getCameraModel().setUseAngleQuery(selected);
        }
    }


    /**
     * ActionListener for: User selects to use camera-queries for signature-1.
     */
    public class useCamSignatureOneQueryActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.getCameraModel().setUseSignatureOne(selected);
        }
    }


    /**
     * ActionListener for: User selects to use camera-queries for signature-2.
     */
    public class useCamSignatureTwoQueryActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.getCameraModel().setUseSignatureTwo(selected);
        }
    }


    /**
     * ActionListener for: User selects to use camera-queries for signature-3.
     */
    public class useCamSignatureThreeQueryActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.getCameraModel().setUseSignatureThree(selected);
        }
    }


    /**
     * ActionListener for: User selects to use camera-queries for signature-4.
     */
    public class useCamSignatureFourQueryActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.getCameraModel().setUseSignatureFour(selected);
        }
    }


    /**
     * ActionListener for: User selects to use camera-queries for signature-5.
     */
    public class useCamSignatureFiveQueryActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.getCameraModel().setUseSignatureFive(selected);
        }
    }


    /**
     * ActionListener for: User selects to use camera-queries for signature-6.
     */
    public class useCamSignatureSixQueryActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.getCameraModel().setUseSignatureSix(selected);
        }
    }


    /**
     * ActionListener for: User selects to use camera-queries for signature-7.
     */
    public class useCamSignatureSevenQueryActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.getCameraModel().setUseSignatureSeven(selected);
        }
    }


    /**
     * ActionListener for: User selects to use distance-measurement to the left.
     */
    public class measureDistanceToLeftActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.getSensorModel().setUseLeftSensor(selected);
        }
    }


    /**
     * ActionListener for: User selects to use distance-measurement ahead.
     */
    public class measureDistanceAheadActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.getSensorModel().setUseFrontSensor(selected);
        }
    }


    /**
     * ActionListener for: User selects to use distance-measurement to the right.
     */
    public class measureDistanceToRightActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.getSensorModel().setUseRightSensor(selected);
        }
    }


    /**
     * ActionListener for: User selects to stop the localization automatically.
     */
    public class setStopWhenLocalizationFinishedActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.getLocalizationModel().setStopWhenDone(selected);
        }
    }


    /**
     * ActionListener for: User selects a file from the list of saved localizations, for replay.
     */
    public class ReplayFileSelectionActionListener implements ActionListener {
        ClientView.ControlPanel.ReplayControlsSubPanel replayControlsSubPanel;

        /**
         * Constructor.
         *
         * @param replayControlsSubPanel The sub-panel holding the replay-control-elements
         */
        ReplayFileSelectionActionListener(ClientView.ControlPanel.ReplayControlsSubPanel replayControlsSubPanel) {
            this.replayControlsSubPanel = replayControlsSubPanel;
        }

        /**
         * Updates view and model with user-selection. Delegates loading selected file.
         *
         * @param e ActionEvent from the file-selection (JComboBox)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            String selection = (String)((JComboBox)e.getSource()).getSelectedItem();
            ArrayList<WorldState> replay = loadSelectedLocalizationFromFile(selection);
            guiModel.getReplayModel().setWorldStatesForReplay(replay);
            guiModel.getReplayModel().setReplayPointer(0);
            WorldState ws = replay.get(0);
            Map map = MapProvider.getInstance().getMap(ws.getMapKey());
            guiModel.getLocalizationModel().setMap(map);
            guiView.getMapPanel().setNewMap(map);
            replayControlsSubPanel.replayButton.setEnabled(true);
            guiView.repaint();
        }

        /**
         * Loads a saved localization from file.
         *
         * @param filename The filename of the saves localization.
         * @return  The saved localization as list (ArrayList<WorldState>)
         */
        private ArrayList<WorldState> loadSelectedLocalizationFromFile(String filename) {
            filename = filename + ".log";
            File file = new File(filename);

            ArrayList<WorldState> worldStates = new ArrayList<>();
            try {
                FileInputStream fileOS = new FileInputStream(file);
                ObjectInputStream objectOS = new ObjectInputStream(fileOS);
                worldStates = (ArrayList<WorldState>) objectOS.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return worldStates;
        }
    }


    /**
     * ActionListener for: User clicks on map-panel to select a particle.
     */
    public class ParticleSelectionMouseListener extends MouseAdapter {
        /**
         * Delegates the transformation of screen-coordinates to map-coordinates.
         * Delegates finding the selected particle.
         * Updated view and model.
         *
         * @param mouseEvent MouseEvent from the map-panel
         */
        @Override
        public void mouseClicked(MouseEvent mouseEvent) {
            if (guiModel.getLocalizationProvider() == null)
                return;

            Point clickCoordinates = transformClickCoordinatesToMapCoordinateSystem(mouseEvent);
            Particle selectedParticle = findClosestParticleToUserClick(clickCoordinates);
            guiModel.getLocalizationModel().setSelectedParticle(selectedParticle);
            guiView.refreshParticleInfo();
        }

        /**
         * Returns the coordinates of the user-click, transformed to the map-coordinate-system.
         *
         * @param mouseEvent MouseEvent from the map-panel
         * @return  The click-coordinates in the map-coordinate-system, of type Point.
         */
        private Point transformClickCoordinatesToMapCoordinateSystem(MouseEvent mouseEvent) {
            MapPanel mapPanel = guiView.getMapPanel();

            int menuBarYOffset = 35;
            int yOffset = menuBarYOffset + guiView.getControlPanel().getHeight() + mapPanel.getYOffset();
            int xOffset = mapPanel.getXOffset();

            float xClickTransformed = (mouseEvent.getX() - xOffset) / mapPanel.getScaleFactor();
            float yClickTransformed = (mouseEvent.getY() - yOffset) / mapPanel.getScaleFactor();

            return new Point(Math.round(xClickTransformed), Math.round(yClickTransformed));
        }

        /**
         * Iterates through the list of particles to find the particle closest to the click-coordinates.
         *
         * @param clickCoordinates The coordinates of the user-click (int the map-coordinate-system!)
         * @return  The particle closest to the user-click
         */
        private Particle findClosestParticleToUserClick(Point clickCoordinates) {
            ArrayList<Particle> particles = guiModel.getLocalizationProvider().getParticles();
            Particle currentParticle = particles.get(0);
            for (Particle p : particles) {
                currentParticle = chooseCloserParticleToUserClick(clickCoordinates, currentParticle, p);
            }
            return currentParticle;
        }


        /**
         * Return, of two given particles, the one that is closer to the click-coordinates.
         *
         * @param clickCoordinates The coordinates of the user-click (int the map-coordinate-system!)
         * @param particleA First particle for comparison.
         * @param particleB Second particle for comparison
         * @return  The particle which is closer to the click-coordinates.
         */
        private Particle chooseCloserParticleToUserClick(Point clickCoordinates, Particle particleA, Particle particleB) {
            double dxParticleA = clickCoordinates.getX() - particleA.getPose().getX();
            double dyParticleA = clickCoordinates.getY() - particleA.getPose().getY();
            double distanceClickPointToParticleA = Math.sqrt(Math.pow(dxParticleA, 2) + Math.pow(dyParticleA, 2));

            double dxParticleB = clickCoordinates.getX() - particleB.getPose().getX();
            double dyParticleB = clickCoordinates.getY() - particleB.getPose().getY();
            double distanceClickPointToParticleB = Math.sqrt(Math.pow(dxParticleB, 2) + Math.pow(dyParticleB, 2));

            if (distanceClickPointToParticleA < distanceClickPointToParticleB) {
                return particleA;
            } else {
                return particleB;
            }
        }

    }
}
