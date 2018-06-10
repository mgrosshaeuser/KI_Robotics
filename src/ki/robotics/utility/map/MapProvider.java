package ki.robotics.utility.map;

public interface MapProvider {
    String MAP_KEY_ROOM = "room.svg";
    String MAP_KEY_HOUSES = "Houses.svg";
    String MAP_KEY_MARKED_ROOM = "room_with_marks.svg";

    String[] getMapKeys();

    Map getMap(String key);
}
