package ki.robotics.client.communication;


public interface ComController {
    void start();

    void stop();

    boolean isStopped();

    String getInitialRequest();

    String getNextRequest();

    void handleResponse(String response);
}
