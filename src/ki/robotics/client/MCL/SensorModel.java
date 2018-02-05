package ki.robotics.client.MCL;

import ki.robotics.utility.pixyCam.DTOAngleQuery;
import ki.robotics.utility.pixyCam.DTOColorCodeQuery;
import ki.robotics.utility.pixyCam.DTOGeneralQuery;
import ki.robotics.utility.pixyCam.DTOSignatureQuery;

/**
 * A simple 'book-keeping'-class for the sensor-feedback from the robot.
 *
 * @version 1.0 01/02/18
 */
public class SensorModel {

    private float distanceToLeft;
    private float distanceToCenter;
    private float distanceToRight;
    private int color;
    private float sensorHeadPosition;

    private DTOGeneralQuery generalQuery;
    private DTOAngleQuery angleQuery;
    private DTOColorCodeQuery colorCodeQuery;
    private DTOSignatureQuery unspecifiedSignatureQuery;
    private DTOSignatureQuery signatureQuery1;
    private DTOSignatureQuery signatureQuery2;
    private DTOSignatureQuery signatureQuery3;
    private DTOSignatureQuery signatureQuery4;
    private DTOSignatureQuery signatureQuery5;
    private DTOSignatureQuery signatureQuery6;
    private DTOSignatureQuery signatureQuery7;


    public float[] getAllDistances() {
        return new float[]{distanceToLeft, distanceToCenter, distanceToRight};
    }

    public float getDistanceToLeft() {
        return distanceToLeft;
    }

    public void setDistanceToLeft(float distanceToLeft) {
        this.distanceToLeft = distanceToLeft;
    }

    public float getDistanceToCenter() {
        return distanceToCenter;
    }

    public void setDistanceToCenter(float distanceToCenter) {
        this.distanceToCenter = distanceToCenter;
    }

    public float getDistanceToRight() {
        return distanceToRight;
    }

    public void setDistanceToRight(float distanceToRight) {
        this.distanceToRight = distanceToRight;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getSensorHeadPosition() {
        return sensorHeadPosition;
    }

    public void setSensorHeadPosition(float sensorHeadPosition) {
        this.sensorHeadPosition = sensorHeadPosition%360;
    }



    public DTOGeneralQuery getGeneralQuery() {
        return generalQuery;
    }

    public void setGeneralQuery(DTOGeneralQuery generalQuery) {
        this.generalQuery = generalQuery;
    }

    public DTOAngleQuery getAngleQuery() {
        return angleQuery;
    }

    public void setAngleQuery(DTOAngleQuery angleQuery) {
        this.angleQuery = angleQuery;
    }


    public DTOColorCodeQuery getColorCodeQuery() {
        return colorCodeQuery;
    }

    public void setColorCodeQuery(DTOColorCodeQuery colorCodeQuery) {
        this.colorCodeQuery = colorCodeQuery;
    }

    public DTOSignatureQuery getUnspecifiedSignatureQuery() {
        return unspecifiedSignatureQuery;
    }

    public void setUnspecifiedSignatureQuery(DTOSignatureQuery unspecifiedSignatureQuery) {
        this.unspecifiedSignatureQuery = unspecifiedSignatureQuery;
    }

    public DTOSignatureQuery getSignatureQuery1() {
        return signatureQuery1;
    }

    public void setSignatureQuery1(DTOSignatureQuery signatureQuery1) {
        this.signatureQuery1 = signatureQuery1;
    }

    public DTOSignatureQuery getSignatureQuery2() {
        return signatureQuery2;
    }

    public void setSignatureQuery2(DTOSignatureQuery signatureQuery2) {
        this.signatureQuery2 = signatureQuery2;
    }

    public DTOSignatureQuery getSignatureQuery3() {
        return signatureQuery3;
    }

    public void setSignatureQuery3(DTOSignatureQuery signatureQuery3) {
        this.signatureQuery3 = signatureQuery3;
    }

    public DTOSignatureQuery getSignatureQuery4() {
        return signatureQuery4;
    }

    public void setSignatureQuery4(DTOSignatureQuery signatureQuery4) {
        this.signatureQuery4 = signatureQuery4;
    }

    public DTOSignatureQuery getSignatureQuery5() {
        return signatureQuery5;
    }

    public void setSignatureQuery5(DTOSignatureQuery signatureQuery5) {
        this.signatureQuery5 = signatureQuery5;
    }

    public DTOSignatureQuery getSignatureQuery6() {
        return signatureQuery6;
    }

    public void setSignatureQuery6(DTOSignatureQuery signatureQuery6) {
        this.signatureQuery6 = signatureQuery6;
    }

    public DTOSignatureQuery getSignatureQuery7() {
        return signatureQuery7;
    }

    public void setSignatureQuery7(DTOSignatureQuery signatureQuery7) {
        this.signatureQuery7 = signatureQuery7;
    }
}
