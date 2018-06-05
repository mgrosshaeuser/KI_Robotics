package ki.robotics.server.robots.simulation;

import ki.robotics.utility.map.Map;
import lejos.robotics.navigation.Pose;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

class SimulationController {
    private final SimulationModel model;
    private final SimulationView view;


    SimulationController() {
        this.model = new SimulationModel();
        this.view = new SimulationView(this, model);
    }

    Pose getPose() {
        return model.getPose();
    }

    int getSensorHeadPosition() {
        return model.getSensorHeadPosition();
    }

    void setSensorHeadPosition(int sensorHeadPosition) {
        this.model.setSensorHeadPosition(sensorHeadPosition);
    }

    Map getMap() {
        return model.getMap();
    }

    void setMap(Map map) {
        this.model.setMap(map);
    }


    void repaintWindow() {
        view.repaint();
    }


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

    class UserXCoordinateInputFocusListener extends FocusAdapter {
        @Override
        public void focusGained(FocusEvent e) {
            super.focusGained(e);
            ((JTextField) e.getSource()).selectAll();
        }
    }

    class UserYCoordinateInputFocusListener extends FocusAdapter {
        @Override
        public void focusGained(FocusEvent e) {
            super.focusGained(e);
            ((JTextField) e.getSource()).selectAll();
        }
    }

    class UserCoordinateInputOKButtonActionListener implements ActionListener {
        private final SimulationView.MapOverlay.CoordinateInput dialog;

        UserCoordinateInputOKButtonActionListener(SimulationView.MapOverlay.CoordinateInput dialog) {
            this.dialog = dialog;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int xVal = Integer.parseInt(dialog.getXInput());
                int yVal = Integer.parseInt(dialog.getYInput());
                model.getPose().setLocation(xVal, yVal);
            } catch (NumberFormatException ignored) {
            }
            repaintWindow();
            dialog.dispose();
        }
    }


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
                    int xVal = Integer.parseInt(dialog.getXInput());
                    int yVal = Integer.parseInt(dialog.getYInput());
                    model.getPose().setLocation(xVal, yVal);
                } catch (NumberFormatException ignored) {

                }
                repaintWindow();
                dialog.dispose();
            }
        }
    }
}
