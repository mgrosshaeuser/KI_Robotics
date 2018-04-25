package ki.robotics.utility.map;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MapProvider {
    public static final String MAP_KEY_ROOM = "Room";
    public static final String MAP_KEY_HOUSES = "Houses";
    public static final String MAP_KEY_MARKED_ROOM = "MarkedRoom";

    private static final MapProvider INSTANCE = new MapProvider();

    private final HashMap<String, Map> maps = new HashMap<>();
    private final ArrayList<String> mapKeys = new ArrayList<>();


    /**
     * Returns the singleton-instance of the MapProvider.
     *
     * @return the singleton-instance of the MapProvider
     */
    public static MapProvider getInstance() { return INSTANCE; }



    /**
     * Constructs and initializes the singleton-instance of the MapProvider.
     *
     */
    private MapProvider() {
        createMap(
                MAP_KEY_ROOM,
                "room.svg",
                new Polygon(new int[]{0, 150, 150, 100, 100, 0}, new int[]{0, 0, 150, 150, 200, 200},6)
        );

        createMap(
                MAP_KEY_HOUSES,
                "Houses.svg",
                new Polygon (new int[]{1,601, 601,1}, new int[]{69,69,71,71}, 4)
        );

        createMap(
                MAP_KEY_MARKED_ROOM,
                "room_with_marks.svg",
                new Polygon(new int[]{0, 150, 150, 100, 100, 0}, new int[]{0, 0, 150, 150, 200, 200},6)
        );


        //TODO Move limitation-handling out of the map and into gui-configurations.
        limitations = new HashMap<>();
        int noLimitation = -1;
        this.limitations.put(MAP_KEY_ROOM, new int[]{noLimitation, noLimitation, noLimitation});
        this.limitations.put(MAP_KEY_MARKED_ROOM, new int[]{noLimitation, noLimitation, noLimitation});
        this.limitations.put(MAP_KEY_HOUSES, new int[]{noLimitation, 70,0});
    }



    /**
     * Returns the keys to all maps known to the MapProvider as array of Strings.
     *
     * @return an array with all map keys
     */
    public String[] getMapKeys() {
        String[] keys = new String[mapKeys.size()];
        mapKeys.toArray(keys);
        return keys;
    }



    /**
     * Returns the map associated with the specified key.
     *
     * @param key the specified key to a map
     * @return the map associated with the specified key
     */
    public Map getMap(String key) {
        return maps.get(key);
    }



    /**
     * Constructs a map identified by the specified key, from a specified file with a specified operating-range.
     *
     * @param key the key to identify the map
     * @param filename the name of the (SVG-)file
     * @param boundaries the operating-range within the map
     */
    private void createMap(String key, String filename, Polygon boundaries) {
        SVGParser parser = new SVGParser(new File(getClass().getClassLoader().getResource(filename).getFile()));
        MapImpl map = parser.getMap();
        map.setOperatingRange(boundaries);
        maps.put(key, map);
        mapKeys.add(key);
    }





    //TODO Remove when limitation-handling is moved to gui-configuration.

    private final HashMap<String, int[]> limitations;     // int[0] fixated x-value, int[1] fixated y-value, int[2] fixated heading


    public int[] getMapLimitations(String key) {
        return limitations.get(key);
    }
}
