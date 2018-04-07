package ki.robotics.robot;

import ki.robotics.utility.map.Map;
import ki.robotics.utility.map.MapProvider;
import lejos.robotics.navigation.Pose;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MCLParticleTest {

    @Test
    public void testTranslate() {
    }

    @Test(dataProvider = "getTurnFullProvider")
    public void testTurnFull(MCLParticle mclP, int angleToTurn, int expectedResult) {
        mclP.turnFull(angleToTurn);
        Assert.assertEquals((int) mclP.getPose().getHeading(), expectedResult);
    }

    @Test(dataProvider = "getMCLParticlesProvider")
    public void testCompareTo(MCLParticle particle1, MCLParticle particle2, int expectedResult) {
        int result = particle1.compareTo(particle2);
        Assert.assertEquals(result, expectedResult);
    }

    @Test
    public void testIsOutOfBounds() {

    }

    @DataProvider(name = "getTurnFullProvider")
    public Object[][] getTurnFullProvider() {
        Map map = MapProvider.getInstance().getMap("Room");
        int[] headingsToTest = new int[]{0, 90, 110, 180, 202, 270, 360};
        int[] anglesToTurn = new int[]{-360, -180, -90, 0, 90, 180, 360};

        int[][] resultSet = new int[][]{
                //heading 0
                {0, 180, 270, 0, 90, 180, 0},
                //heading 90
                {90, 270, 0, 90, 180, 270, 90},
                //heading 110
                {110, 290, 20, 110, 200, 290, 110},
                //heading 180
                {180, 0, 90, 180, 270, 0, 180},
                //heading 202
                {202, 22, 112, 202, 292, 22, 202},
                //heading 270
                {270, 90, 180, 270, 0, 90, 270},
                //heading 360
                {0, 180, 270, 0, 90, 180, 0},
        };

        MCLParticle[][] mclParticles = new MCLParticle[headingsToTest.length][headingsToTest.length];
        for (int i = 0; i < headingsToTest.length; i++) {
            for (int j = 0; j < headingsToTest.length; j++) {
                Pose pose = new Pose();
                pose.setHeading(headingsToTest[i]);
                mclParticles[i][j] = new MCLParticle(pose, map, 1.0F);
            }
        }

        return new Object[][] {
                {mclParticles[0][0], anglesToTurn[0], resultSet[0][0]},
                {mclParticles[0][1], anglesToTurn[1], resultSet[0][1]},
                {mclParticles[0][2], anglesToTurn[2], resultSet[0][2]},
                {mclParticles[0][3], anglesToTurn[3], resultSet[0][3]},
                {mclParticles[0][4], anglesToTurn[4], resultSet[0][4]},
                {mclParticles[0][5], anglesToTurn[5], resultSet[0][5]},
                {mclParticles[0][6], anglesToTurn[6], resultSet[0][6]},

                {mclParticles[1][0], anglesToTurn[0], resultSet[1][0]},
                {mclParticles[1][1], anglesToTurn[1], resultSet[1][1]},
                {mclParticles[1][2], anglesToTurn[2], resultSet[1][2]},
                {mclParticles[1][3], anglesToTurn[3], resultSet[1][3]},
                {mclParticles[1][4], anglesToTurn[4], resultSet[1][4]},
                {mclParticles[1][5], anglesToTurn[5], resultSet[1][5]},
                {mclParticles[1][6], anglesToTurn[6], resultSet[1][6]},

                {mclParticles[2][0], anglesToTurn[0], resultSet[2][0]},
                {mclParticles[2][1], anglesToTurn[1], resultSet[2][1]},
                {mclParticles[2][2], anglesToTurn[2], resultSet[2][2]},
                {mclParticles[2][3], anglesToTurn[3], resultSet[2][3]},
                {mclParticles[2][4], anglesToTurn[4], resultSet[2][4]},
                {mclParticles[2][5], anglesToTurn[5], resultSet[2][5]},
                {mclParticles[2][6], anglesToTurn[6], resultSet[2][6]},

                {mclParticles[3][0], anglesToTurn[0], resultSet[3][0]},
                {mclParticles[3][1], anglesToTurn[1], resultSet[3][1]},
                {mclParticles[3][2], anglesToTurn[2], resultSet[3][2]},
                {mclParticles[3][3], anglesToTurn[3], resultSet[3][3]},
                {mclParticles[3][4], anglesToTurn[4], resultSet[3][4]},
                {mclParticles[3][5], anglesToTurn[5], resultSet[3][5]},
                {mclParticles[3][6], anglesToTurn[6], resultSet[3][6]},

                {mclParticles[4][0], anglesToTurn[0], resultSet[4][0]},
                {mclParticles[4][1], anglesToTurn[1], resultSet[4][1]},
                {mclParticles[4][2], anglesToTurn[2], resultSet[4][2]},
                {mclParticles[4][3], anglesToTurn[3], resultSet[4][3]},
                {mclParticles[4][4], anglesToTurn[4], resultSet[4][4]},
                {mclParticles[4][5], anglesToTurn[5], resultSet[4][5]},
                {mclParticles[4][6], anglesToTurn[6], resultSet[4][6]},

                {mclParticles[5][0], anglesToTurn[0], resultSet[5][0]},
                {mclParticles[5][1], anglesToTurn[1], resultSet[5][1]},
                {mclParticles[5][2], anglesToTurn[2], resultSet[5][2]},
                {mclParticles[5][3], anglesToTurn[3], resultSet[5][3]},
                {mclParticles[5][4], anglesToTurn[4], resultSet[5][4]},
                {mclParticles[5][5], anglesToTurn[5], resultSet[5][5]},
                {mclParticles[5][6], anglesToTurn[6], resultSet[5][6]},

                {mclParticles[6][0], anglesToTurn[0], resultSet[6][0]},
                {mclParticles[6][1], anglesToTurn[1], resultSet[6][1]},
                {mclParticles[6][2], anglesToTurn[2], resultSet[6][2]},
                {mclParticles[6][3], anglesToTurn[3], resultSet[6][3]},
                {mclParticles[6][4], anglesToTurn[4], resultSet[6][4]},
                {mclParticles[6][5], anglesToTurn[5], resultSet[6][5]},
                {mclParticles[6][6], anglesToTurn[6], resultSet[6][6]},
        };
    }

    @DataProvider(name = "getMCLParticlesProvider")
    public Object[][] getMCLParticlesProvider() {
        return new Object[][] {
                {new MCLParticle(null, null, 0.1F), new MCLParticle(null, null, 0.2F), -1},
                {new MCLParticle(null, null, 0.2F), new MCLParticle(null, null, 0.1F), 1},
                {new MCLParticle(null, null, 0.1F), new MCLParticle(null, null, 0.1F), 0},
        };
    }

    /*
    @DataProvider(name = "getMCLProviderObjectProvider")
    public Object[][] getMCLProviderObjectProvider() {

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

        return new Object[][] {
                {new MCL_Provider(map, config2D.getNumberOfParticles(), limitations, config2D)}
        };
    }
    */
}