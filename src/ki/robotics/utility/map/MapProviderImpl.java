package ki.robotics.utility.map;

import ki.robotics.client.ClientFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MapProviderImpl implements MapProvider {
    private static final MapProvider INSTANCE = new MapProviderImpl();

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
    private MapProviderImpl() {
        ArrayList<File> mapFiles = getMapFiles();
        for (File file : mapFiles) {
            this.mapKeys.add(file.getName());
        }
    }




    /**
     * Returns the keys to all maps known to the MapProvider as array of Strings.
     *
     * @return an array with all map keys
     */
    @Override
    public String[] getMapKeys() {
        String[] keys = new String[mapKeys.size()];
        mapKeys.toArray(keys);
        return keys;
    }


    /**
     * Returns the map associated with the specified key.
     * If the requested map wasn't loaded yet, it is loaded on-demand.
     *
     * @param key the specified key to a map
     * @return the map associated with the specified key
     */
    @Override
    public Map getMap(String key) {
        Map map;
        if (maps.get(key) == null) {
            map = createMap(key);
            this.maps.put(key, map);
        }
        return maps.get(key);
    }


    /**
     * Creates a map based on the file indicated by the given key.
     *
     * @param key   A map key
     * @return  The map from the file indicated by the key
     */
    private Map createMap(String key) {
        String path = ClientFactory.getProperties().getProperty("mapPath");
        File mapFile = new File(path + key);
        SVGParser parser = new SVGParser(mapFile);
        return parser.getMap();
    }


    /**
     * Returns a list with all svg-files in the default map-directory.
     *
     * @return  A list of map-files
     */
    private ArrayList<File> getMapFiles() {
        String path = ClientFactory.getProperties().getProperty("mapPath");
        File defaultPath = new File(path);
        File mapFiles[] = defaultPath.listFiles();
        ArrayList<File> foundMaps = new ArrayList<>();

        if (mapFiles != null) {
            for (File f : mapFiles) {
                if (f.getName().endsWith(".svg")) {
                    foundMaps.add(f);
                }
            }
        }

        return foundMaps;
    }


}
