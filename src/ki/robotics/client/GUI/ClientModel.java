package ki.robotics.client.GUI;

import ki.robotics.client.MCL.Configuration;
import ki.robotics.client.MCL.MCL_Provider;
import ki.robotics.utility.map.Map;
import ki.robotics.utility.map.MapProvider;

public class ClientModel implements Configuration {
    private static final String ONE_DIMENSION_MAP_KEY = MapProvider.MAP_KEY_HOUSES;
    private static final String TWO_DIMENSION_MAP_KEY = MapProvider.MAP_KEY_ROOM;
    private static final String TWO_DIMENSION_WITH_CAM_MAP_KEY = MapProvider.MAP_KEY_MARKED_ROOM;

    private MapProvider mapProvider = MapProvider.getInstance();
    private Map map;
    private String mapKey;
    private MCL_Provider mclProvider;

    private boolean isOneDimensional = true;
    private boolean isTwoDimensional = false;
    private boolean isWithCamera = false;

    private int stepSize = 10;
    private int numberOfParticles = 1000;
    private boolean stopWhenDone = true;
    private int acceptableTolerance = 10;

    private int userClickX;
    private int userClickY;


    // Specific attributes for 1-D-movement.
    private boolean startFromLeft = true;
    private boolean measureDistanceToLeft = true;


    // Specific attributes for 2-D-movement.
    private boolean useRightAngles = true;
    private boolean useFreeAngles = false;
    private boolean useLeftSensor = true;
    private boolean useFrontSensor = true;
    private boolean useRightSensor = true;


    // Specific attributes for 2-D-movement with Camera.
    private boolean useGeneralQuery = true;
    private boolean useAngleQuery = true;
    private boolean useSignatureOne = true;
    private boolean useSignatureTwo = true;
    private boolean useSignatureThree = true;
    private boolean useSignatureFour = true;
    private boolean useSignatureFive = true;
    private boolean useSignatureSix = true;
    private boolean useSignatureSeven = true;





    void createMclProvider() {
        this.mclProvider = new MCL_Provider(map, numberOfParticles, new int[]{-1,-1,-1}, this);
    }

    @Override
    public MCL_Provider getMclProvider() { return this.mclProvider; }


    @Override
    public Map getMap() {
        if (map == null) {
            this.mapKey = ONE_DIMENSION_MAP_KEY;
            return mapProvider.getMap(ONE_DIMENSION_MAP_KEY);
        }
        return map;
    }

    @Override
    public String getMapKey() {
        return this.mapKey;
    }

    @Override
    public int getNumberOfParticles() {
        return numberOfParticles;
    }

    void setNumberOfParticles(int numberOfParticles) { this.numberOfParticles = numberOfParticles; }





    @Override
    public boolean isOneDimensional() {
        return isOneDimensional;
    }

    void setOneDimensional() {
        isOneDimensional = true;
        isTwoDimensional = false;
        isWithCamera = false;
        map = mapProvider.getMap(ONE_DIMENSION_MAP_KEY);
        mapKey = ONE_DIMENSION_MAP_KEY;
        mclProvider = null;
    }

    @Override
    public boolean isTwoDimensional() {
        return isTwoDimensional;
    }

    void setTwoDimensional() {
        isTwoDimensional = true;
        isOneDimensional = false;
        isWithCamera = false;
        map = mapProvider.getMap(TWO_DIMENSION_MAP_KEY);
        mapKey = TWO_DIMENSION_MAP_KEY;
        mclProvider = null;
    }

    @Override
    public boolean isWithCamera() {
        return isWithCamera;
    }

    void setWithCamera() {
        isWithCamera = true;
        isOneDimensional = false;
        isTwoDimensional = false;
        map = mapProvider.getMap(TWO_DIMENSION_WITH_CAM_MAP_KEY);
        mapKey = TWO_DIMENSION_WITH_CAM_MAP_KEY;
        mclProvider = null;
    }




    @Override
    public int getStepSize() {
        return stepSize;
    }

    void setStepSize(int stepSize) {
        this.stepSize = stepSize;
    }

    @Override
    public boolean isStopWhenDone() {
        return stopWhenDone;
    }

    void setStopWhenDone(boolean stopWhenDone) {
        this.stopWhenDone = stopWhenDone;
    }

    @Override
    public int getAcceptableTolerance() {
        return acceptableTolerance;
    }

    void setAcceptableTolerance(int acceptableTolerance) {
        this.acceptableTolerance = acceptableTolerance;
    }



    @Override
    public boolean isStartFromLeft() {
        return startFromLeft;
    }

    void setStartFromLeft() {
        this.startFromLeft = true;
        this.measureDistanceToLeft = true;
    }

    @Override
    public boolean isStartFromRight() { return !startFromLeft; }

    void setStartFromRight() {
        this.startFromLeft = false;
        this.measureDistanceToLeft = false;
    }

    @Override
    public boolean isMeasureDistanceToLeft() {
        return measureDistanceToLeft;
    }

    @Override
    public boolean isMeasureDistanceToRight() { return ! measureDistanceToLeft; }

    @Override
    public void flipDirection() { this.measureDistanceToLeft = !measureDistanceToLeft; }




    @Override
    public boolean isUseRightAngles() {
        return useRightAngles;
    }

    void setUseRightAngles() {
        this.useRightAngles = true;
        this.useFreeAngles = false;
    }

    @Override
    public boolean isUseFreeAngles() {
        return useFreeAngles;
    }

    void setUseFreeAngles() {
        this.useFreeAngles = true;
        this.useRightAngles = false;
    }


    @Override
    public boolean isUseLeftSensor() {
        return useLeftSensor;
    }

    void setUseLeftSensor(boolean useLeftSensor) {
        this.useLeftSensor = useLeftSensor;
    }

    @Override
    public boolean isUseFrontSensor() {
        return useFrontSensor;
    }

    void setUseFrontSensor(boolean useFrontSensor) {
        this.useFrontSensor = useFrontSensor;
    }

    @Override
    public boolean isUseRightSensor() {
        return useRightSensor;
    }

    void setUseRightSensor(boolean useRightSensor) {
        this.useRightSensor = useRightSensor;
    }




    @Override
    public boolean isUseGeneralQuery() {
        return useGeneralQuery;
    }

    void setUseGeneralQuery(boolean useGeneralQuery) {
        this.useGeneralQuery = useGeneralQuery;
    }

    @Override
    public boolean isUseAngleQuery() {
        return useAngleQuery;
    }

    void setUseAngleQuery(boolean useAngleQuery) {
        this.useAngleQuery = useAngleQuery;
    }

    @Override
    public boolean isUseSignatureOne() {
        return useSignatureOne;
    }

    void setUseSignatureOne(boolean useSignatureOne) {
        this.useSignatureOne = useSignatureOne;
    }

    @Override
    public boolean isUseSignatureTwo() {
        return useSignatureTwo;
    }

    void setUseSignatureTwo(boolean useSignatureTwo) {
        this.useSignatureTwo = useSignatureTwo;
    }

    @Override
    public boolean isUseSignatureThree() {
        return useSignatureThree;
    }

    void setUseSignatureThree(boolean useSignatureThree) {
        this.useSignatureThree = useSignatureThree;
    }

    @Override
    public boolean isUseSignatureFour() {
        return useSignatureFour;
    }

    void setUseSignatureFour(boolean useSignatureFour) {
        this.useSignatureFour = useSignatureFour;
    }

    @Override
    public boolean isUseSignatureFive() {
        return useSignatureFive;
    }

    void setUseSignatureFive(boolean useSignatureFive) {
        this.useSignatureFive = useSignatureFive;
    }

    @Override
    public boolean isUseSignatureSix() {
        return useSignatureSix;
    }

    void setUseSignatureSix(boolean useSignatureSix) {
        this.useSignatureSix = useSignatureSix;
    }

    @Override
    public boolean isUseSignatureSeven() {
        return useSignatureSeven;
    }

    void setUseSignatureSeven(boolean useSignatureSeven) {
        this.useSignatureSeven = useSignatureSeven;
    }

    int getUserClickX() { return userClickX; }

    protected void setSelectedParticleX(int userClickX) { this.userClickX = userClickX; }

    int getUserClickY() { return userClickY; }

    protected void setSelectedParticleY(int userClickY) { this.userClickY = userClickY; }
}
