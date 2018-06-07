package ki.robotics.server;

import ki.robotics.server.communication.ServerComControllerImpl;
import ki.robotics.server.communication.ServerCommunicator;
import ki.robotics.server.communication.ServerCommunicatorImpl;
import ki.robotics.server.robots.Robot;
import ki.robotics.server.robots.simulation.RobotImplSimulation;
import ki.robotics.server.robots.RobotImplSojourner;
import ki.robotics.utility.UtilityFactory;

/**
 * Factory class for server-side object-instantiation across packages.
 */
public class ServerFactory extends UtilityFactory {
    /**
     * Returns an instance of ServerCommunicatorImpl satisfying the requirement of interface ServerCommunicator.
     * Fully equipped with a robot-reference (a simulated or real robot) and a communication-controller.
     *
     * @param port         The server-port to open
     * @param isSimulation  Boolean value indicating whether to use a real or a simulated robot
     * @return  An instance of ServerCommunicatorImpl as interface-type ServerCommunicator
     */
    static ServerCommunicator createNewServerCommunicator(int port, boolean isSimulation) {
        ServerCommunicator communicator = new ServerCommunicatorImpl(port);

        Robot robot;
        if (isSimulation) {
            robot = new RobotImplSimulation();
        } else {
            robot = RobotImplSojourner.getInstance();
        }

        ServerComControllerImpl controller = new ServerComControllerImpl(communicator, robot);
        robot.registerComController(controller);
        communicator.registerComController(controller);

        return communicator;
    }
}
