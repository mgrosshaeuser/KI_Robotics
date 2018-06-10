package ki.robotics.client;

import ki.robotics.client.GUI.GuiConfiguration;
import ki.robotics.client.GUI.impl.GuiControllerImplClientController;
import ki.robotics.client.GUI.GuiController;
import ki.robotics.client.MCL.LocalizationProvider;
import ki.robotics.client.MCL.SensorModel;
import ki.robotics.client.MCL.impl.LocalizationProviderImplMCL;
import ki.robotics.client.MCL.impl.SensorModelImplRoverModel;
import ki.robotics.client.communication.ClientComController;
import ki.robotics.client.communication.ClientComControllerImplGUI;
import ki.robotics.client.communication.ClientComControllerImplTerminal;
import ki.robotics.utility.UtilityFactory;
import ki.robotics.utility.map.Map;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


/**
 * Factory class for client-side object-instantiation across packages.
 */
public class ClientFactory extends UtilityFactory {
    private static Properties properties = loadProperties();

    /**
     * Loads and returns project-properties from file 'robotics.config'
     *
     * @return  The project-properties from file 'robotics.config
     */
    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream("./robotics.config")) {
            properties.load(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return properties;
    }


    /**
     * Returns project-properties.
     *
     * @return  The project-properties
     */
    public static Properties getProperties() {
        return properties;
    }


    /**
     * Returns a new instance of a graphical client-side communication-controller satisfying the requirements
     * from interface ClientComController.
     *
     */
    static void createNewGraphicalClient(String host, int port) {
        ClientComControllerImplGUI controller = new ClientComControllerImplGUI(host, port);
        controller.setSensorModel(createNewSensorModel());
        controller.setGuiController(createNewGuiController(controller));
    }


    /**
     * Returns a new instance of a terminal-based client-side communication-controller satisfying the requirements
     * from interface ClientComController.
     *
     */
    static void createNewTerminalClient(String host, int port) {
        new ClientComControllerImplTerminal(host, port);
    }



    /**
     * Returns a new instance of the a MCL-provider satisfying the requirements from interface LocalizationProvider.
     *
     * @param map       The map to be used
     * @param numberOfParticles The number of particles to be generated
     * @param userSettings  The user-settings of interface-type GuiConfiguration
     *
     * @return A new instance of LocalizationProviderImplMCL as interface-type LocalizationProvider
     */
    public static LocalizationProvider createNewLocalizationProvider(Map map, int numberOfParticles, GuiConfiguration userSettings) {
        return new LocalizationProviderImplMCL(map, numberOfParticles, userSettings);
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
    private static GuiController createNewGuiController(ClientComController clientComController) {
        return new GuiControllerImplClientController(clientComController);
    }

}
