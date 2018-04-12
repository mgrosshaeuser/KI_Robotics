package ki.robotics.client.MCL;

import ki.robotics.client.GUI.ClientModel;
import ki.robotics.robot.MCLParticle;
import ki.robotics.utility.map.Map;
import ki.robotics.utility.map.MapProvider;
import lejos.robotics.navigation.Pose;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MCL_ProviderTest {


    @BeforeMethod
    public void setUp() {
    }

    @AfterMethod
    public void tearDown() {
    }

    @Test(dataProvider = "getDeviationTestDataProvider")
    public void testRecalculateParticleWeight(MCL_Provider mclProvider, SensorModel bot) {
        mclProvider.recalculateParticleWeight(bot);
    }

    @Test
    public void testBadParticlesFinalKill() {
    }

    @DataProvider(name = "getDeviationTestDataProvider")
    public Object[][] getParticleTestData() {
        int numOfParticles = 1000; //via GUI

        Map map = MapProvider.getInstance().getMap("Room");
        MCL_Provider mclProvider = new MCL_Provider(map, numOfParticles, new int[] {-1, -1, -1}, new ClientModel());

        SensorModel bot = new SensorModel();
        bot.setDistanceToLeft(10.0);
        bot.setDistanceToCenter(40.0);
        bot.setDistanceToRight(90.0);

        Pose pose = new Pose();
        pose.setLocation(82.0F, 145.0F);
        MCLParticle mclParticle = new MCLParticle(pose, null, 1.0F);

        return new Object[][] {
                {mclProvider, bot}
        };
    }
}