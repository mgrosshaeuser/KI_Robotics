package ki.robotics.client.GUI;

import ki.robotics.client.ComController;
import ki.robotics.server.robot.virtualRobots.MCLParticle;
import ki.robotics.utility.map.MapPanel;
import ki.robotics.utility.map.MapProvider;
import lejos.robotics.navigation.Pose;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

class ClientController {
    private ClientModel guiModel;
    private ClientView guiView;
    private ComController comController;



    ClientController(ClientModel guiModel, ClientView guiView, ComController comController) {
        this.guiModel = guiModel;
        this.guiView = guiView;
        this.comController = comController;
    }


    private void start() {
        int[] limitations = MapProvider.getInstance().getMapLimitations(guiModel.getMapKey());
        if (guiModel.isOneDimensional()   && guiModel.isStartFromRight()) {
            limitations[2] = 180;
        }
        guiModel.createMclProvider();
        comController.start(guiModel);
        guiView.repaint();
    }


    private void stop() {
        comController.stop();
        guiView.repaint();
    }


    private void setParticleInfoInGUI(MouseEvent mouseEvent) {
        if (guiModel.getMclProvider() != null) {
            int menuBarYOffset = 35;
            int controlPanelYOffset = guiView.getControlPanel().getHeight();
            int heightOffset = menuBarYOffset + controlPanelYOffset;
            ArrayList<MCLParticle> mclParticles = guiModel.getMclProvider().getParticles();
            int xParticle, yParticle;
            double particleWeight;
            MCLParticle oldNearestMclP = mclParticles.get(0);
            MapPanel mapPanel = guiView.getMapPanel();

            for (MCLParticle mclPtoAnalyse : mclParticles) {
                boolean newParticleIsCloser = isMclPtoAnalyseNearer(oldNearestMclP, mclPtoAnalyse, mouseEvent, heightOffset, mapPanel);

                if (newParticleIsCloser) {
                    oldNearestMclP = mclPtoAnalyse;
                }
            }
            xParticle = (int) ((oldNearestMclP.getPose().getX() * mapPanel.getScaleFactor()) + mapPanel.getXOffset());
            yParticle = (int) ((oldNearestMclP.getPose().getY() * mapPanel.getScaleFactor())
                    + mapPanel.getYOffset() + controlPanelYOffset + menuBarYOffset);
            particleWeight = oldNearestMclP.getWeight();

            guiModel.setSelectedParticleX(xParticle);
            guiModel.setSelectedParticleY(yParticle);
            guiModel.setSelectedParticleWeight(particleWeight);
            guiView.refreshParticleInfo();
        }
    }

    private boolean isMclPtoAnalyseNearer(MCLParticle oldNearestMclP, MCLParticle mclPtoAnalyse, MouseEvent e, int heightOffset, MapPanel mapPanel) {
        int xToAnalyse;
        int yToAnalyse;
        Pose pose = mclPtoAnalyse.getPose();

        xToAnalyse = (int) ((pose.getX() * mapPanel.getScaleFactor()) + mapPanel.getXOffset());
        yToAnalyse = (int) ((pose.getY() * mapPanel.getScaleFactor())
                + mapPanel.getYOffset() + heightOffset);

        int xOldNearestMclP = (int) ((oldNearestMclP.getPose().getX() * mapPanel.getScaleFactor()) + mapPanel.getXOffset());
        int yOldNearestMclP = (int) ((oldNearestMclP.getPose().getY() * mapPanel.getScaleFactor())
                + mapPanel.getYOffset() + heightOffset);

        int xDiffToAnalyse = Math.abs(xToAnalyse - e.getX());
        int yDiffToAnalyse = Math.abs(yToAnalyse - e.getY());
        int addedDiff = xDiffToAnalyse + yDiffToAnalyse;

        int xDiffOldNearestMclP = Math.abs(xOldNearestMclP - e.getX());
        int yDiffOldNearestMclP = Math.abs(yOldNearestMclP - e.getY());
        int addedDiffPrevious = xDiffOldNearestMclP + yDiffOldNearestMclP;
        return addedDiff < addedDiffPrevious;
    }


    public class StartButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            guiView.start.setEnabled(false);
            int distancePerTravelInstruction = guiModel.getStepSize();
            int numberOfParticles = guiModel.getNumberOfParticles();
            try {
                distancePerTravelInstruction = Integer.valueOf(guiView.distancePerTravelInstruction.getText());
                numberOfParticles = Integer.valueOf(guiView.numberOfParticles.getText());
            } catch (NumberFormatException e1) {
                e1.printStackTrace();
            }
            if (distancePerTravelInstruction > 0) {
                guiModel.setStepSize(distancePerTravelInstruction);
            }
            if (numberOfParticles > 0) {
                guiModel.setNumberOfParticles(numberOfParticles);
            }
            start();
        }
    }

    public class StopButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            stop();
            guiView.start.setEnabled(true);
        }
    }




    public class DeviationSliderChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            int acceptableValue = guiView.acceptableLocalizationDeviationSlider.getValue();
            guiModel.setAcceptableTolerance(acceptableValue);
            String textToDisplay = "Acceptable Tolerance: " + acceptableValue + " cm";
            guiView.labelForAcceptableLocalizationDeviationSlider.setText(textToDisplay);
        }
    }




    public class OneDimensionalControlSubPanelComponentListener extends ComponentAdapter {
        @Override
        public void componentShown(ComponentEvent e) {
            super.componentShown(e);
            guiModel.setOneDimensional();
            guiView.getMapPanel().setNewMap(guiModel.getMap());
            guiView.repaint();
        }
    }

    public class TwoDimensionalControlSubPanelComponentListener extends ComponentAdapter {
        @Override
        public void componentShown(ComponentEvent e) {
            super.componentShown(e);
            guiModel.setTwoDimensional();
            guiView.getMapPanel().setNewMap(guiModel.getMap());
            guiView.repaint();
        }
    }

    public class TwoDimWithCameraControlSubPanelComponentListener extends ComponentAdapter {
        @Override
        public void componentShown(ComponentEvent e) {
            super.componentShown(e);
            guiModel.setWithCamera();
            guiView.getMapPanel().setNewMap(guiModel.getMap());
            guiView.repaint();
        }
    }





    public class startLineFollowingFromLeftActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            guiModel.setStartFromLeft();
        }
    }

    public class startLineFollowingFromRightActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            guiModel.setStartFromRight();
        }
    }



    public class useRandomAnglesWhenTurningActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            guiModel.setUseFreeAngles();
        }
    }

    public class useRightAnglesWhenTurningActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            guiModel.setUseRightAngles();
        }
    }



    public class useCamGeneralQueryActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.setUseGeneralQuery(selected);
        }
    }

    public class useCamAngleQueryActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.setUseAngleQuery(selected);
        }
    }

    public class useCamSignatureOneQueryActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.setUseSignatureOne(selected);
        }
    }

    public class useCamSignatureTwoQueryActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.setUseSignatureTwo(selected);
        }
    }

    public class useCamSignatureThreeQueryActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.setUseSignatureThree(selected);
        }
    }

    public class useCamSignatureFourQueryActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.setUseSignatureFour(selected);
        }
    }

    public class useCamSignatureFiveQueryActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.setUseSignatureFive(selected);
        }
    }

    public class useCamSignatureSixQueryActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.setUseSignatureSix(selected);
        }
    }

    public class useCamSignatureSevenQueryActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.setUseSignatureSeven(selected);
        }
    }



    public class measureDistanceToLeftActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.setUseLeftSensor(selected);
        }
    }

    public class measureDistanceAheadActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.setUseFrontSensor(selected);
        }
    }

    public class measureDistanceToRightActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.setUseRightSensor(selected);
        }
    }



    public class setStopWhenLocalizationFinishedActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((JCheckBox)e.getSource()).isSelected();
            guiModel.setStopWhenDone(selected);
        }
    }



    public class setXYWhenClickOnMap extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent mouseEvent) {
            if (guiModel.getMclProvider() == null)
                return;

            Point clickCoordinates = transformClickCoordinatesToMapCoordinateSystem(mouseEvent);
            MCLParticle selectedParticle = findClosestParticleToUserClick(clickCoordinates);
            guiModel.setSelectedParticle(selectedParticle);
            guiView.refreshParticleInfo();
        }


        private Point transformClickCoordinatesToMapCoordinateSystem(MouseEvent mouseEvent) {
            MapPanel mapPanel = guiView.getMapPanel();

            int menuBarYOffset = 35;
            int yOffset = menuBarYOffset + guiView.getControlPanel().getHeight() + mapPanel.getYOffset();
            int xOffset = mapPanel.getXOffset();

            float xClickTransformed = (mouseEvent.getX() - xOffset) / mapPanel.getScaleFactor();
            float yClickTransformed = (mouseEvent.getY() - yOffset) / mapPanel.getScaleFactor();

            return new Point(Math.round(xClickTransformed), Math.round(yClickTransformed));
        }


        private MCLParticle findClosestParticleToUserClick(Point clickCoordinates) {
            ArrayList<MCLParticle> particles = guiModel.getMclProvider().getParticles();
            MCLParticle currentParticle = particles.get(0);
            for (MCLParticle p : particles) {
                currentParticle = chooseCloserParticleToUserClick(clickCoordinates, currentParticle, p);
            }
            return currentParticle;
        }


        private MCLParticle chooseCloserParticleToUserClick(Point clickCoordinates, MCLParticle particleA, MCLParticle particleB) {
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
