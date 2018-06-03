package ki.robotics.client;

import ki.robotics.utility.pixyCam.DTOAngleQuery;
import ki.robotics.utility.pixyCam.DTOColorCodeQuery;
import ki.robotics.utility.pixyCam.DTOGeneralQuery;
import ki.robotics.utility.pixyCam.DTOSignatureQuery;

import java.io.Serializable;

public interface SensorModel extends Serializable {
    SensorModel getClone();

    double[] getAllDistances();

    double getDistanceToLeft();

    void setDistanceToLeft(double distanceToLeft);

    double getDistanceToCenter();

    void setDistanceToCenter(double distanceToCenter);

    double getDistanceToRight();

    void setDistanceToRight(double distanceToRight);

    int getColor();

    void setColor(int color);

    double getSensorHeadPosition();

    void setSensorHeadPosition(double sensorHeadPosition);

    DTOGeneralQuery getGeneralQuery();

    void setGeneralQuery(DTOGeneralQuery generalQuery);

    DTOAngleQuery getAngleQuery();

    void setAngleQuery(DTOAngleQuery angleQuery);

    DTOColorCodeQuery getColorCodeQuery();

    void setColorCodeQuery(DTOColorCodeQuery colorCodeQuery);

    DTOSignatureQuery getUnspecifiedSignatureQuery();

    void setSignatureQuery(DTOSignatureQuery signatureQuery);

    DTOSignatureQuery getSignatureQuery1();

    DTOSignatureQuery getSignatureQuery2();

    DTOSignatureQuery getSignatureQuery3();

    DTOSignatureQuery getSignatureQuery4();

    DTOSignatureQuery getSignatureQuery5();

    DTOSignatureQuery getSignatureQuery6();

    DTOSignatureQuery getSignatureQuery7();
}
