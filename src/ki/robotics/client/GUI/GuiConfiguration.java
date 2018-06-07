package ki.robotics.client.GUI;

import ki.robotics.client.MCL.LocalizationProvider;
import ki.robotics.utility.map.Map;

public interface GuiConfiguration {
    LocalizationProvider getLocalizationProvider();

    Map getMap();

    String getMapKey();

    int getNumberOfParticles();

    boolean isPaused();

    boolean isOneDimensional();

    boolean isTwoDimensional();

    boolean isWithCamera();

    boolean isInReplayMode();

    int getStepSize();

    boolean isStopWhenDone();

    int getAcceptableSpreading();



    boolean isStartFromLeft();

    boolean isStartFromRight();

    void flipDirection();

    boolean isUseRightAngles();

    boolean isUseFreeAngles();



    boolean isMeasureDistanceToLeft();

    boolean isMeasureDistanceToRight();

    boolean isUseLeftSensor();

    boolean isUseFrontSensor();

    boolean isUseRightSensor();



    boolean isUseGeneralQuery();

    boolean isUseAngleQuery();

    boolean isUseSignatureOne();

    boolean isUseSignatureTwo();

    boolean isUseSignatureThree();

    boolean isUseSignatureFour();

    boolean isUseSignatureFive();

    boolean isUseSignatureSix();

    boolean isUseSignatureSeven();
}
