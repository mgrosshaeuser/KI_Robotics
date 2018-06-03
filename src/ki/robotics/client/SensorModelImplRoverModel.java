package ki.robotics.client;

import ki.robotics.utility.pixyCam.DTOAngleQuery;
import ki.robotics.utility.pixyCam.DTOColorCodeQuery;
import ki.robotics.utility.pixyCam.DTOGeneralQuery;
import ki.robotics.utility.pixyCam.DTOSignatureQuery;

/**
 * A simple 'book-keeping'-class for the sensor-feedback from the robot.
 *
 * @version 1.0 01/02/18
 */
class SensorModelImplRoverModel implements SensorModel {

    private double distanceToLeft;
    private double distanceToCenter;
    private double distanceToRight;
    private int color;
    private double sensorHeadPosition;

    private transient DTOGeneralQuery generalQuery;
    private transient DTOAngleQuery angleQuery;
    private transient DTOColorCodeQuery colorCodeQuery;
    private transient DTOSignatureQuery unspecifiedSignatureQuery;
    private transient DTOSignatureQuery signatureQuery1;
    private transient DTOSignatureQuery signatureQuery2;
    private transient DTOSignatureQuery signatureQuery3;
    private transient DTOSignatureQuery signatureQuery4;
    private transient DTOSignatureQuery signatureQuery5;
    private transient DTOSignatureQuery signatureQuery6;
    private transient DTOSignatureQuery signatureQuery7;



    public SensorModel getClone() {
        SensorModelImplRoverModel newModel = new SensorModelImplRoverModel();
        newModel.distanceToLeft = this.distanceToLeft;
        newModel.distanceToCenter = this.distanceToCenter;
        newModel.distanceToRight = this.distanceToRight;
        newModel.color = this.color;
        newModel.sensorHeadPosition = this.sensorHeadPosition;
        return newModel;
    }


    @Override
    public double[] getAllDistances() {
        return new double[]{distanceToLeft, distanceToCenter, distanceToRight};
    }

    @Override
    public double getDistanceToLeft() {
        return distanceToLeft;
    }

    @Override
    public void setDistanceToLeft(double distanceToLeft) {
        this.distanceToLeft = distanceToLeft;
    }

    @Override
    public double getDistanceToCenter() {
        return distanceToCenter;
    }

    @Override
    public void setDistanceToCenter(double distanceToCenter) {
        this.distanceToCenter = distanceToCenter;
    }

    @Override
    public double getDistanceToRight() {
        return distanceToRight;
    }

    @Override
    public void setDistanceToRight(double distanceToRight) {
        this.distanceToRight = distanceToRight;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public double getSensorHeadPosition() {
        return sensorHeadPosition;
    }

    @Override
    public void setSensorHeadPosition(double sensorHeadPosition) {
        this.sensorHeadPosition = sensorHeadPosition%360;
    }



    @Override
    public DTOGeneralQuery getGeneralQuery() {
        return generalQuery;
    }

    @Override
    public void setGeneralQuery(DTOGeneralQuery generalQuery) {
        this.generalQuery = generalQuery;
    }

    @Override
    public DTOAngleQuery getAngleQuery() {
        return angleQuery;
    }

    @Override
    public void setAngleQuery(DTOAngleQuery angleQuery) {
        this.angleQuery = angleQuery;
    }


    @Override
    public DTOColorCodeQuery getColorCodeQuery() {
        return colorCodeQuery;
    }

    @Override
    public void setColorCodeQuery(DTOColorCodeQuery colorCodeQuery) {
        this.colorCodeQuery = colorCodeQuery;
    }

    @Override
    public DTOSignatureQuery getUnspecifiedSignatureQuery() {
        return unspecifiedSignatureQuery;
    }

    @Override
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

    @Override
    public DTOSignatureQuery getSignatureQuery1() {
        return signatureQuery1;
    }

    @Override
    public DTOSignatureQuery getSignatureQuery2() {
        return signatureQuery2;
    }

    @Override
    public DTOSignatureQuery getSignatureQuery3() {
        return signatureQuery3;
    }

    @Override
    public DTOSignatureQuery getSignatureQuery4() {
        return signatureQuery4;
    }

    @Override
    public DTOSignatureQuery getSignatureQuery5() {
        return signatureQuery5;
    }

    @Override
    public DTOSignatureQuery getSignatureQuery6() {
        return signatureQuery6;
    }

    @Override
    public DTOSignatureQuery getSignatureQuery7() {
        return signatureQuery7;
    }
}
