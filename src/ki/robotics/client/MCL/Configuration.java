package ki.robotics.client.MCL;

import ki.robotics.client.MCL.MCL_Provider;
import ki.robotics.utility.map.Map;

import java.util.ArrayList;

public interface Configuration {
    MCL_Provider getMclProvider();

    Map getMap();

    String getMapKey();

    int getNumberOfParticles();

    boolean isOneDimensional();

    boolean isTwoDimensional();

    boolean isWithCamera();

    int getStepSize();

    boolean isStopWhenDone();

    int getAcceptableTolerance();

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
}
