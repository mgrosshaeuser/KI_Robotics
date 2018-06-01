package ki.robotics.client.MCL;

import ki.robotics.client.GUI.Configuration;
import ki.robotics.utility.map.Map;

public class LocalizationProviderFactory {
    public static LocalizationProvider getLocalizationProvider(Map map, int numberOfParticles, int[] limitations, Configuration userSettings) {
        MclModel model = new MclModel(map, numberOfParticles, limitations, userSettings);
        LocalizationProviderImplMCL mclProvider = new LocalizationProviderImplMCL(model);
        model.createInitialWorld(mclProvider);
        return mclProvider;
    }
}
