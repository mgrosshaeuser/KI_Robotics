package ki.robotics.server;

import ki.robotics.server.communication.ServerCommunicator;
import ki.robotics.server.communication.ServerCommunicatorImplCommunicator;
import ki.robotics.server.robots.simulation.RobotImplSimulation;
import ki.robotics.server.robots.RobotImplSojourner;

public class ServerFactory {
    public static Robot getSimulatedRobot() {
        return new RobotImplSimulation();
    }

    public static Robot getPhysicalRobot() {
        return RobotImplSojourner.getInstance();
    }



    static ServerCommunicator createNewServerCommunicator(int port, boolean isSimulation) {
        return new ServerCommunicatorImplCommunicator(port, isSimulation);
    }

}
