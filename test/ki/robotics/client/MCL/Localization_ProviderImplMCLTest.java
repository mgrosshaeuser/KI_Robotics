package ki.robotics.client.MCL;

import ki.robotics.client.GUI.impl.GuiConfigurationImplClientModel;
import ki.robotics.client.ClientFactory;
import ki.robotics.client.MCL.impl.ParticleImplMCL;
import ki.robotics.client.SensorModel;
import ki.robotics.utility.map.Map;
import ki.robotics.utility.map.MapProvider;
import lejos.robotics.navigation.Pose;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.awt.*;

public class Localization_ProviderImplMCLTest {


    @BeforeMethod
    public void setUp() {
    }

    @AfterMethod
    public void tearDown() {
    }

    @Test(dataProvider = "getDeviationTestDataProvider")
    public void testRecalculateParticleWeight(LocalizationProvider mclProvider, SensorModel bot) {
        mclProvider.recalculateParticleWeight(bot);
    }

    @Test
    public void testBadParticlesFinalKill() {
    }

    @DataProvider(name = "getDeviationTestDataProvider")
    public Object[][] getParticleTestData() {
        int numOfParticles = 1000; //via GUI

        Map map = MapProvider.getInstance().getMap("Room");
        LocalizationProvider localizationProvider = ClientFactory.createNewLocalizationProvider(map, numOfParticles, new int[] {-1, -1, -1}, new GuiConfigurationImplClientModel());

        SensorModel bot = ClientFactory.createNewSensorModel();
        bot.setDistanceToLeft(10.0);
        bot.setDistanceToCenter(40.0);
        bot.setDistanceToRight(90.0);

        Pose pose = new Pose();
        pose.setLocation(82.0F, 145.0F);
        ParticleImplMCL mclParticle = new ParticleImplMCL(pose, null, 1.0F, Color.BLACK);

        return new Object[][] {
                {localizationProvider, bot}
        };
    }
}