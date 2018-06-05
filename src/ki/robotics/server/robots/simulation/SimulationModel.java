package ki.robotics.server.robots.simulation;

import ki.robotics.utility.map.Map;
import ki.robotics.utility.map.MapProvider;
import lejos.robotics.navigation.Pose;

class SimulationModel {
    private static final String DEFAULT_SELECTED_MAP = MapProvider.MAP_KEY_ROOM;

    private Pose pose;
    private int sensorHeadPosition = 0;
    private MapProvider mapProvider;
    private Map map;
    private String[] mapKeys;
    private boolean locked;


    SimulationModel() {
        this.pose = new Pose();
        this.pose.setLocation(10,10);
        this.pose.setHeading(0);
        this.mapProvider = MapProvider.getInstance();
        this.map = mapProvider.getMap(DEFAULT_SELECTED_MAP);
        this.mapKeys = mapProvider.getMapKeys();
    }


    Pose getPose() {
        return pose;
    }


    int getSensorHeadPosition() {
        return sensorHeadPosition;
    }

    void setSensorHeadPosition(int sensorHeadPosition) {
        this.sensorHeadPosition = sensorHeadPosition;
    }

    MapProvider getMapProvider() {
        return this.mapProvider;
    }

    Map getMap() {
        return map;
    }

    void setMap(Map map) {
        this.map = map;
    }

    String[] getMapKeys() {
        return mapKeys;
    }

    boolean isLocked() {
        return locked;
    }

    void setLocked(boolean bool) {
        this.locked = bool;
    }
}
