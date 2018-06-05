package ki.robotics.client;

import ki.robotics.client.GUI.GuiConfiguration;
import ki.robotics.client.GUI.impl.GuiControllerImplClientController;
import ki.robotics.client.GUI.GuiController;
import ki.robotics.client.MCL.LocalizationProvider;
import ki.robotics.client.MCL.impl.LocalizationProviderImplMCL;
import ki.robotics.client.communication.ClientComController;
import ki.robotics.client.communication.ClientComControllerImplGUI;
import ki.robotics.client.communication.ClientComControllerImplTerminal;
import ki.robotics.utility.map.Map;


/**
 * Factory class for client-side object-instantiation across packages.
 */
public class ClientFactory {
    /**
     * Returns a new instance of the a MCL-provider satisfying the requirements from interface LocalizationProvider.
     *
     * @param map       The map to be used
     * @param numberOfParticles The number of particles to be generated
     * @param limitations   The limitations depending on one- or two-dimensional environment
     * @param userSettings  The user-settings of interface-type GuiConfiguration
     *
     * @return A new instance of LocalizationProviderImplMCL as interface-type LocalizationProvider
     */
    public static LocalizationProvider createNewLocalizationProvider(Map map, int numberOfParticles, int[] limitations, GuiConfiguration userSettings) {
        return new LocalizationProviderImplMCL(map, numberOfParticles, limitations, userSettings);
    }


    /**
     * Returns a new instance of a robot-sensor-model satisfying the requirements from interface SensorModel.
     *
     * @return A new instance of SensorModelImplRoverModel as interface-type SensorModel
     */
    public static SensorModel createNewSensorModel() {
        return new SensorModelImplRoverModel();
    }


    /**
     * Returns a new instance of a GUI-controller satisfying the requirements from interface GuiController.
     *
     * @param clientComController A client-side communication-controller.
     *
     * @return A new instance of GuiControllerImplClientController as interface-type GuiController
     */
    public static GuiController createNewGuiController(ClientComController clientComController) {
        return new GuiControllerImplClientController(clientComController);
    }


    /**
     * Returns a new instance of a graphical client-side communication-controller satisfying the requirements
     * from interface ClientComController.
     *
     * @return A new instance of ClientComControllerImplGUI as interface-type ClientComController
     */
    static ClientComController createNewGraphicalClient() {
        return new ClientComControllerImplGUI();
    }


    /**
     * Returns a new instance of a terminal-based client-side communication-controller satisfying the requirements
     * from interface ClientComController.
     *
     * @return A new instance of ClientComControllerImplTerminal as interface-type ClientComController
     */
    static ClientComController createNewTerminalClient() {
        return new ClientComControllerImplTerminal();
    }
}
