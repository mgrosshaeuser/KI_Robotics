package ki.robotics.client.GUI;

public interface GuiController {
    Configuration getUserSettings();

    void repaintWindow();

    void updateWindowAfterLocalizationFinished();
}
