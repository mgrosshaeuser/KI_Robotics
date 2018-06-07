package ki.robotics.server.robots;

import ki.robotics.server.ServerFactory;
import ki.robotics.utility.map.Map;
import lejos.robotics.navigation.Pose;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.awt.*;

public class ParticleImplMCLTest {

    @Test
    public void testTranslate() {
    }

    @Test(dataProvider = "getTurnFullProvider")
    public void testTurnFull(ki.robotics.client.MCL.impl.ParticleImplMCL mclP, int angleToTurn, int expectedResult) {
        mclP.turn(angleToTurn);
        Assert.assertEquals((int) mclP.getPose().getHeading(), expectedResult);
    }

    @Test(dataProvider = "getParticleImplMCLsProvider")
    public void testCompareTo(ki.robotics.client.MCL.impl.ParticleImplMCL particle1, ki.robotics.client.MCL.impl.ParticleImplMCL particle2, int expectedResult) {
        int result = particle1.compareTo(particle2);
        Assert.assertEquals(result, expectedResult);
    }

    @DataProvider(name = "getTurnFullProvider")
    public Object[][] getTurnFullProvider() {
        Map map = ServerFactory.getMapProvider().getMap("Room");
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

        ki.robotics.client.MCL.impl.ParticleImplMCL[][] ParticleImplMCLs = new ki.robotics.client.MCL.impl.ParticleImplMCL[headingsToTest.length][headingsToTest.length];
        for (int i = 0; i < headingsToTest.length; i++) {
            for (int j = 0; j < headingsToTest.length; j++) {
                Pose pose = new Pose();
                pose.setHeading(headingsToTest[i]);
                ParticleImplMCLs[i][j] = new ki.robotics.client.MCL.impl.ParticleImplMCL(pose, map, 1.0F, Color.BLACK);
            }
        }

        return new Object[][] {
                {ParticleImplMCLs[0][0], anglesToTurn[0], resultSet[0][0]},
                {ParticleImplMCLs[0][1], anglesToTurn[1], resultSet[0][1]},
                {ParticleImplMCLs[0][2], anglesToTurn[2], resultSet[0][2]},
                {ParticleImplMCLs[0][3], anglesToTurn[3], resultSet[0][3]},
                {ParticleImplMCLs[0][4], anglesToTurn[4], resultSet[0][4]},
                {ParticleImplMCLs[0][5], anglesToTurn[5], resultSet[0][5]},
                {ParticleImplMCLs[0][6], anglesToTurn[6], resultSet[0][6]},

                {ParticleImplMCLs[1][0], anglesToTurn[0], resultSet[1][0]},
                {ParticleImplMCLs[1][1], anglesToTurn[1], resultSet[1][1]},
                {ParticleImplMCLs[1][2], anglesToTurn[2], resultSet[1][2]},
                {ParticleImplMCLs[1][3], anglesToTurn[3], resultSet[1][3]},
                {ParticleImplMCLs[1][4], anglesToTurn[4], resultSet[1][4]},
                {ParticleImplMCLs[1][5], anglesToTurn[5], resultSet[1][5]},
                {ParticleImplMCLs[1][6], anglesToTurn[6], resultSet[1][6]},

                {ParticleImplMCLs[2][0], anglesToTurn[0], resultSet[2][0]},
                {ParticleImplMCLs[2][1], anglesToTurn[1], resultSet[2][1]},
                {ParticleImplMCLs[2][2], anglesToTurn[2], resultSet[2][2]},
                {ParticleImplMCLs[2][3], anglesToTurn[3], resultSet[2][3]},
                {ParticleImplMCLs[2][4], anglesToTurn[4], resultSet[2][4]},
                {ParticleImplMCLs[2][5], anglesToTurn[5], resultSet[2][5]},
                {ParticleImplMCLs[2][6], anglesToTurn[6], resultSet[2][6]},

                {ParticleImplMCLs[3][0], anglesToTurn[0], resultSet[3][0]},
                {ParticleImplMCLs[3][1], anglesToTurn[1], resultSet[3][1]},
                {ParticleImplMCLs[3][2], anglesToTurn[2], resultSet[3][2]},
                {ParticleImplMCLs[3][3], anglesToTurn[3], resultSet[3][3]},
                {ParticleImplMCLs[3][4], anglesToTurn[4], resultSet[3][4]},
                {ParticleImplMCLs[3][5], anglesToTurn[5], resultSet[3][5]},
                {ParticleImplMCLs[3][6], anglesToTurn[6], resultSet[3][6]},

                {ParticleImplMCLs[4][0], anglesToTurn[0], resultSet[4][0]},
                {ParticleImplMCLs[4][1], anglesToTurn[1], resultSet[4][1]},
                {ParticleImplMCLs[4][2], anglesToTurn[2], resultSet[4][2]},
                {ParticleImplMCLs[4][3], anglesToTurn[3], resultSet[4][3]},
                {ParticleImplMCLs[4][4], anglesToTurn[4], resultSet[4][4]},
                {ParticleImplMCLs[4][5], anglesToTurn[5], resultSet[4][5]},
                {ParticleImplMCLs[4][6], anglesToTurn[6], resultSet[4][6]},

                {ParticleImplMCLs[5][0], anglesToTurn[0], resultSet[5][0]},
                {ParticleImplMCLs[5][1], anglesToTurn[1], resultSet[5][1]},
                {ParticleImplMCLs[5][2], anglesToTurn[2], resultSet[5][2]},
                {ParticleImplMCLs[5][3], anglesToTurn[3], resultSet[5][3]},
                {ParticleImplMCLs[5][4], anglesToTurn[4], resultSet[5][4]},
                {ParticleImplMCLs[5][5], anglesToTurn[5], resultSet[5][5]},
                {ParticleImplMCLs[5][6], anglesToTurn[6], resultSet[5][6]},

                {ParticleImplMCLs[6][0], anglesToTurn[0], resultSet[6][0]},
                {ParticleImplMCLs[6][1], anglesToTurn[1], resultSet[6][1]},
                {ParticleImplMCLs[6][2], anglesToTurn[2], resultSet[6][2]},
                {ParticleImplMCLs[6][3], anglesToTurn[3], resultSet[6][3]},
                {ParticleImplMCLs[6][4], anglesToTurn[4], resultSet[6][4]},
                {ParticleImplMCLs[6][5], anglesToTurn[5], resultSet[6][5]},
                {ParticleImplMCLs[6][6], anglesToTurn[6], resultSet[6][6]},
        };
    }

    @DataProvider(name = "getParticleImplMCLsProvider")
    public Object[][] getParticleImplMCLsProvider() {
        return new Object[][] {
                {new ki.robotics.client.MCL.impl.ParticleImplMCL(null, null, 0.1F, Color.BLACK), new ki.robotics.client.MCL.impl.ParticleImplMCL(null, null, 0.2F, Color.BLACK), -1},
                {new ki.robotics.client.MCL.impl.ParticleImplMCL(null, null, 0.2F, Color.BLACK), new ki.robotics.client.MCL.impl.ParticleImplMCL(null, null, 0.1F, Color.BLACK), 1},
                {new ki.robotics.client.MCL.impl.ParticleImplMCL(null, null, 0.1F, Color.BLACK), new ki.robotics.client.MCL.impl.ParticleImplMCL(null, null, 0.1F, Color.BLACK), 0},
        };
    }
}