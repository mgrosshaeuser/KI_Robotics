package ki.robotics.client.GUI;

public interface GuiController {
    /**
     * Returns a sub-set of the GUI-(data-)model corresponding to the GuiConfiguration-interface.
     *
     * @return  A GuiConfiguration-sub-set of the GUI-(data-)model
     */
    GuiConfiguration getUserSettings();


    /**
     * Repaints the GUI-view.
     */
    void repaintWindow();


    /**
     * Updates the GUI once a localization is finished.
     */
    void updateWindowAfterLocalizationFinished();
}
