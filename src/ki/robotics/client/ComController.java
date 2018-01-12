package ki.robotics.client;

import ki.robotics.client.MCL.Configuration;

public interface ComController {
    void start(Configuration configuration);

    void stop();

    String getInitialRequest();

    String getNextRequest();

    void handleResponse(String response);
}
