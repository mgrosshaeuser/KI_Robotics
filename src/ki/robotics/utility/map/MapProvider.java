package ki.robotics.utility.map;

import java.awt.*;
import java.io.File;
import java.util.HashMap;

public class MapProvider {
    public static final String MAP_KEY_ROOM = "Room";
    public static final String MAP_KEY_HOUSES = "Houses";

    private static final MapProvider INSTANCE = new MapProvider();
    private static final int NO_LIMITATION = -1;

    private final HashMap<String, File> files;
    private final HashMap<String, Polygon> boundaries;
    private final HashMap<String, int[]> limitations;     // int[0] fixated x-value, int[1] fixated y-value, int[2] fixated heading

    private MapProvider() {
        files = new HashMap<>();
        boundaries = new HashMap<>();
        limitations = new HashMap<>();


        this.files.put(MAP_KEY_ROOM, new File(getClass().getClassLoader().getResource("room.svg").getFile()));
        this.boundaries.put (MAP_KEY_ROOM, new Polygon(new int[]{0, 150, 150, 100, 100, 0}, new int[]{0, 0, 150, 150, 200, 200},6));
        this.limitations.put(MAP_KEY_ROOM, new int[]{NO_LIMITATION, NO_LIMITATION, NO_LIMITATION});

        this.files.put(MAP_KEY_HOUSES, new File(getClass().getClassLoader().getResource("Houses.svg").getFile()));
        this.boundaries.put(MAP_KEY_HOUSES, new Polygon (new int[]{1,499, 499,1}, new int[]{69,69,71,71}, 4));
        this.limitations.put(MAP_KEY_HOUSES, new int[]{NO_LIMITATION, 70,0});

    /*
        this.files.put("Street", new File(getClass().getClassLoader().getResource("street.svg").getFile()));
        this.boundaries.put ("Street", new Polygon (new int[]{0,400, 400, 0}, new int[]{70,70,72,72},4));
        this.limitations.put("Street", new int[]{NO_LIMITATION,70,0});
    */
    }

    public static MapProvider getInstance() { return INSTANCE; }

    public String[] getMapKeys() {
        String[] keys = new String[files.keySet().size()];
        keys = files.keySet().toArray(keys);
        return keys;
    }

    public Map getMap(String key) {
        return new Map(files.get(key), boundaries.get(key));
    }

    public int[] getMapLimitations(String key) {
        return limitations.get(key);
    }
}
