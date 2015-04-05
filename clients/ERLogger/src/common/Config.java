package common;

import dataSource.Loader;
import dataSource.MetaLoader;
import dataSource.ProtobufLoader;

/**
 * Created by max on 2014-11-17.
 */
public class Config {
    public static String SERVER_HOST = "localhost";
    public static int SERVER_PORT = 2001;
    public static final Loader DATA_LOADER = new ProtobufLoader();
    public static final MetaLoader META_LOADER = (MetaLoader) DATA_LOADER;
    public static final String UNKNOWN_SENSOR_NAME = "Unknown";
}
