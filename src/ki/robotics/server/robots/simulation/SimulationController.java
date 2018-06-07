package ki.robotics.server.robots.simulation;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

/**
 * The controller for the simulation.
 */
class SimulationController {
    private final SimulationModel model;
    private final SimulationView view;


    /**
     * Constructor.
     */
    SimulationController() {
        this.model = new SimulationModel();
        this.view = new SimulationView(this, model);
    }


    /**
     * Returns the simulation-(data-)model.
     *
     * @return  The simulation-(data-)model
     */
    SimulationModel getModel() {
        return this.model;
    }


    /**
     * Repaints the associated view.
     */
    void repaintWindow() {
        view.repaint();
    }


    /**
     * Listener for the action: User selects map from JComboBox.
     */
    class MapSelectionActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (view.getMapOverlay().isModifiable()) {
                int index = ((JComboBox)e.getSource()).getSelectedIndex();
                String mapkey = model.getMapKeys()[index];
                model.setMap(model.getMapProvider().getMap(mapkey));
                view.getMapOverlay().setNewMap(model.getMap());
                repaintWindow();
            }
        }
    }


    /**
     * Listener for the action: User changes heading of the robot by moving the JSlider.
     */
    class HeadingSliderChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            if (view.getMapOverlay().isModifiable()) {
                int selectedValue = ((JSlider)e.getSource()).getValue();
                model.getPose().setHeading(selectedValue);
                view.getControlPanel().updateHeadingLabel("Heading: " + model.getPose().getHeading());
                repaintWindow();
            }
        }
    }


    /**
     * Listener for the action: User clicks Lock-button to lock the GUI (prevent changes to configuration)
     */
    class LockButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.isLocked()) {
                view.getControlPanel().updateLockButtonTest("Lock");
                model.setLocked(false);
                view.getMapOverlay().setModifiable(true);
            } else {
                view.getControlPanel().updateLockButtonTest("Unlock");
                model.setLocked(true);
                view.getMapOverlay().setModifiable(false);
            }

        }
    }


    /**
     * Listener for the action: User clicks on map to enter coordinates for the robot.
     */
    class MapOverlayClickedMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            if (view.getMapOverlay().isModifiable()) {
                view.getMapOverlay().userCoordinateInput();
                repaintWindow();
            }
        }
    }


    /**
     * Listener for the action: User moves the robot by mouse-dragging.
     */
    class RoverDraggedMouseMotionListener extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            if (view.getMapOverlay().isModifiable()) {
                Rectangle robotBounds = view.getMapOverlay().getRover().getRobotBounds();
                if (robotBounds.contains(e.getX(), e.getY())) {
                    int scaleFactor = view.getMapOverlay().getScaleFactor();
                    int xOffset = view.getMapOverlay().getXOffset();
                    int yOffset = view.getMapOverlay().getYOffset();
                    int xTemp = (e.getX() - xOffset) / scaleFactor;
                    int yTemp = (e.getY() - yOffset) / scaleFactor;
                    model.getPose().setLocation(xTemp, yTemp);
                    repaintWindow();
                }
            }
        }
    }


    /**
     * Listener for the action: User sets cursor to the X-coordinate input JTextField
     * (in the robot-coordinate input sub-panel)
     */
    class UserXCoordinateInputFocusListener extends FocusAdapter {
        @Override
        public void focusGained(FocusEvent e) {
            super.focusGained(e);
            ((JTextField) e.getSource()).selectAll();
        }
    }


    /**
     * Listener for the action: User sets cursor to the Y-coordinate input JTextField
     * (in the robot-coordinate input sub-panel).
     */
    class UserYCoordinateInputFocusListener extends FocusAdapter {
        @Override
        public void focusGained(FocusEvent e) {
            super.focusGained(e);
            ((JTextField) e.getSource()).selectAll();
        }
    }


    /**
     * Listener for the action: User clicks OK-button of the robot-coordinate input sub-panel.
     */
    class UserCoordinateInputOKButtonActionListener implements ActionListener {
        private final SimulationView.MapOverlay.CoordinateInput dialog;

        UserCoordinateInputOKButtonActionListener(SimulationView.MapOverlay.CoordinateInput dialog) {
            this.dialog = dialog;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int xVal = Integer.parseInt(dialog.xInput.getText());
                int yVal = Integer.parseInt(dialog.yInput.getText());
                model.getPose().setLocation(xVal, yVal);
            } catch (NumberFormatException ignored) {
            }
            repaintWindow();
            dialog.dispose();
        }
    }


    /**
     * Listener for the action: User hits ENTER while any of the elements of the robot-coordinate input
     * sub-panel has the focus.
     */
    class UserCoordinateInputEnterKeyListener extends KeyAdapter {
        private final SimulationView.MapOverlay.CoordinateInput dialog;

        UserCoordinateInputEnterKeyListener(SimulationView.MapOverlay.CoordinateInput dialog) {
            this.dialog = dialog;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            super.keyReleased(e);
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                try {
                    int xVal = Integer.parseInt(dialog.xInput.getText());
                    int yVal = Integer.parseInt(dialog.yInput.getText());
                    model.getPose().setLocation(xVal, yVal);
                } catch (NumberFormatException ignored) {

                }
                repaintWindow();
                dialog.dispose();
            }
        }
    }
}
