package ki.robotics.client.GUI;

import ki.robotics.client.MCL.LocalizationProvider;
import ki.robotics.utility.map.Map;

public interface Configuration {
    LocalizationProvider getLocalizationProvider();

    Map getMap();

    String getMapKey();

    int getNumberOfParticles();

    boolean isOneDimensional();

    boolean isTwoDimensional();

    boolean isWithCamera();

    int getStepSize();

    boolean isStopWhenDone();

    int getAcceptableDeviation();

    boolean isStartFromLeft();

    boolean isStartFromRight();

    boolean isMeasureDistanceToLeft();

    boolean isMeasureDistanceToRight();

    void flipDirection();

    boolean isUseRightAngles();

    boolean isUseFreeAngles();

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

    boolean isPaused();
}
