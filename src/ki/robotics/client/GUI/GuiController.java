package ki.robotics.client.GUI;

public interface GuiController {
    GuiConfiguration getUserSettings();

    void repaintWindow();

    void updateWindowAfterLocalizationFinished();
}
