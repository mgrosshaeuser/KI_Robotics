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

    private double distanceToLeft;
    private double distanceToCenter;
    private double distanceToRight;
    private int color;
    private double sensorHeadPosition;

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


    public double[] getAllDistances() {
        return new double[]{distanceToLeft, distanceToCenter, distanceToRight};
    }

    public double getDistanceToLeft() {
        return distanceToLeft;
    }

    public void setDistanceToLeft(double distanceToLeft) {
        this.distanceToLeft = distanceToLeft;
    }

    public double getDistanceToCenter() {
        return distanceToCenter;
    }

    public void setDistanceToCenter(double distanceToCenter) {
        this.distanceToCenter = distanceToCenter;
    }

    public double getDistanceToRight() {
        return distanceToRight;
    }

    public void setDistanceToRight(double distanceToRight) {
        this.distanceToRight = distanceToRight;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public double getSensorHeadPosition() {
        return sensorHeadPosition;
    }

    public void setSensorHeadPosition(double sensorHeadPosition) {
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

    public void setSignatureQuery(DTOSignatureQuery signatureQuery) {
        switch (signatureQuery.getSignature()) {
            case 1:
                this.signatureQuery1 = signatureQuery;
                break;
            case 2:
                this.signatureQuery2 = signatureQuery;
                break;
            case 3:
                this.signatureQuery3 = signatureQuery;
                break;
            case 4:
                this.signatureQuery4 = signatureQuery;
                break;
            case 5:
                this.signatureQuery5 = signatureQuery;
                break;
            case 6:
                this.signatureQuery6 = signatureQuery;
                break;
            case 7:
                this.signatureQuery7 = signatureQuery;
                break;
            default:
                this.unspecifiedSignatureQuery = signatureQuery;
                break;

        }
    }

    public DTOSignatureQuery getSignatureQuery1() {
        return signatureQuery1;
    }

    public DTOSignatureQuery getSignatureQuery2() {
        return signatureQuery2;
    }

    public DTOSignatureQuery getSignatureQuery3() {
        return signatureQuery3;
    }

    public DTOSignatureQuery getSignatureQuery4() {
        return signatureQuery4;
    }

    public DTOSignatureQuery getSignatureQuery5() {
        return signatureQuery5;
    }

    public DTOSignatureQuery getSignatureQuery6() {
        return signatureQuery6;
    }

    public DTOSignatureQuery getSignatureQuery7() {
        return signatureQuery7;
    }
}
