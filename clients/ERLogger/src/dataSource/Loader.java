package dataSource;

import common.ERDataField;

/**
 * Created with IntelliJ IDEA.
 * User: Jakob lövhall
 * Date: 2013-11-17
 * Time: 19:09
 *
 * Interface used by classes implementing a data input interface.
 */
public interface Loader {

    /**
     * Stop collecting data.
     */
    public void stop();

    /**
     * Start collecting data.
     */
    public boolean start();

    /**
     * Start collecting data
     * @param name Name of data collection
     */
    public boolean start(String name);

    /**
     * Connect to data source.
     */
    public boolean connect();

    /**
     * Disconnect from data source.
     */
    public void disconnect();

    /**
     * Request data from server.
     */
    public void requestData();

    /**
     * Request data stop from server.
     */
    public void requestDataStop();

    /**
     * Read one data field.
     */
    public ERDataField[] readPackage();

    /**
     */
    void dispose();
}
