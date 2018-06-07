package ki.robotics.utility.map;

public interface MapProvider {
    String MAP_KEY_ROOM = "Room";
    String MAP_KEY_HOUSES = "Houses";
    String MAP_KEY_MARKED_ROOM = "MarkedRoom";

    String[] getMapKeys();

    Map getMap(String key);

    int[] getMapLimitations(String key);
}
