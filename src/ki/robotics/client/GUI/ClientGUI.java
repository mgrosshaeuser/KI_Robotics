package ki.robotics.client.GUI;

import ki.robotics.client.ComController;
import ki.robotics.utility.map.MapProvider;

import javax.swing.*;
import java.awt.*;

public class ClientGUI extends JFrame {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 800;

    private final ClientGUIModel model;
    private final ClientGUIMapPanel mapPanel;
    private final ClientGUIControlPanel controlPanel;

    private final ki.robotics.client.ComController comController;





    public ClientGUI(ComController comController) {
        this.model = new ClientGUIModel();

        this.comController = comController;
        this.mapPanel = new ClientGUIMapPanel(this, model);
        this.controlPanel = new ClientGUIControlPanel(this, model);

        this.setTitle("Monte Carlo Localization");
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(new ClientGUIControlPanel(this, model), BorderLayout.PAGE_START);
        add(mapPanel, BorderLayout.CENTER);
        this.setVisible(true);
    }




    ClientGUIMapPanel getMapPanel() { return  mapPanel; }
    ClientGUIControlPanel getControlPanel() { return controlPanel; }





    void start() {
        int[] limitations = MapProvider.getInstance().getMapLimitations(model.getMapKey());
        if (model.isOneDimensional()   && model.isStartFromRight()) {
            limitations[2] = 180;
        }
        model.createMclProvider();
        comController.start(model);
        repaint();
    }



    void stop() {
        comController.stop();
        repaint();
    }

}
