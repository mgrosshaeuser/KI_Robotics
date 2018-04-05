package ki.robotics.client.MCL;

import ki.robotics.robot.MCLParticle;
import ki.robotics.utility.map.Map;
import ki.robotics.utility.map.MapProvider;
import lejos.robotics.navigation.Pose;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class MCL_ProviderTest {


    @BeforeMethod
    public void setUp() {
    }

    @AfterMethod
    public void tearDown() {
    }

    @Test
    public void testGetParticles() {
    }

    @Test
    public void testGetAcceptableTolerance() {
    }

    @Test
    public void testRecalculateParticleWeight() {
    }

    @Test
    public void testGetMedianParticleWeight() {
    }

    @Test
    public void testGetEstimatedBotPose() {
    }

    @Test
    public void testIsLocalizationDone() {
    }

    @Test
    public void testGetEstimatedBotPoseDeviation() {
    }

    @Test
    public void testBadParticlesFinalKill() {
    }

    @Test
    public void testTranslateParticle() {
    }

    @Test(dataProvider = "getTurnFullProvider")
    public void testTurnFull(MCL_Provider mclP, int[] anglesToTest) {

        for (int turnAngle : anglesToTest) {
            int maxAngle = 360;
            int maxGauss = 10;
            int gaussRange = Math.round(turnAngle * (1 + (maxGauss / 540)));

            for (int i = 0; i < mclP.getParticles().size(); i++) {

                MCLParticle particle = mclP.getParticles().get(i);
                Pose pose = particle.getPose();

                float postHeadingShould = (pose.getHeading() + turnAngle);
                mclP.turnFull(turnAngle);
                float postHeadingIs = pose.getHeading();

                Assert.assertTrue(gaussRange >= Math.abs(postHeadingShould - postHeadingIs) || maxAngle - gaussRange <= Math.abs(postHeadingShould - postHeadingIs));
            }
        }
    }

    @DataProvider(name = "getTurnFullProvider")
    public Object[][] getTurnFullProvider() {

        int step = 10; //via GUI in cm
        int numOfParticles = 1000; //via GUI
        int tolerance = 10; //via GUI in cm
        String mapKey = Configuration.ConfigTwoD.DEFAULT.getMapKey();
        Configuration config2D = new Configuration.ConfigTwoD(
                mapKey,
                false,
                true,
                false,
                step,
                numOfParticles,
                true,
                tolerance,
                true,
                true,
                true,
                true,
                true);
        Map map = MapProvider.getInstance().getMap("Room");
        int[] limitations = MapProvider.getInstance().getMapLimitations(config2D.getMapKey());


        int[] anglesToTest = new int[]{0, 90, 170, 300, 360};

        return new Object[][] {
                {new MCL_Provider(map, config2D.getNumberOfParticles(), limitations, config2D), anglesToTest}
        };
    }
}