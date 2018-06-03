package ki.robotics.client;

import ki.robotics.client.GUI.GuiControllerImplClientController;
import ki.robotics.client.GUI.Configuration;
import ki.robotics.client.GUI.GuiController;
import ki.robotics.client.MCL.LocalizationProvider;
import ki.robotics.client.MCL.LocalizationProviderImplMCL;
import ki.robotics.client.communication.ComController;
import ki.robotics.utility.map.Map;

public class ClientFactory {
    public static LocalizationProvider createNewLocalizationProvider(Map map, int numberOfParticles, int[] limitations, Configuration userSettings) {
        return new LocalizationProviderImplMCL(map, numberOfParticles, limitations, userSettings);
    }

    public static SensorModel createNewSensorModel() {
        return new SensorModelImplRoverModel();
    }

    public static GuiController createNewGuiController(ComController comController) {
        return new GuiControllerImplClientController(comController);
    }
}
