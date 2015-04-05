package common;

import dataSource.Loader;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Jakob lövhall
 * Date: 2013-10-01
 * Time: 17:09
 *
 * Data API. Singleton.
 */

public class Data {

    private static Data _mySelf = null;
    private float _currentTime;
    private List<DataListener> _listeners;
    private ERDataField[] _cur = new ERDataField[0]; // used for the serialization of the data.
    private Map<String, XYSeriesCollection> xySeriesCollectionMap = new HashMap<>();

    /**
     * singleton getter for this object to make sure that the same configuration is use throe out the program.
     * @return a single (all ways the same) instance of this class
     */
    public static Data getInstance(){
        if(_mySelf == null)
            _mySelf = new Data();

        return _mySelf;
    }

    private Data() {
        _currentTime = 0;

        _listeners = new ArrayList<>();
    }

    /**
     * get a new set of data
     * @param newDataFields the new data
     */
    public void setNewDataFields(ERDataField[] newDataFields){
       _cur = newDataFields;
    }

    public List<String> getSensorNames() {
        return Config.META_LOADER.getSensorNames();
    }

    public List<String> getAttributeNames(String sensor) {
        return Config.META_LOADER.getAttributeNames(sensor);
    }

    public List<String> getProjectNames() {
        return Config.META_LOADER.getProjectNames();
    }

    public String getCurrentProject() {
        return Config.META_LOADER.getCurrentProject();
    }

    public void setCurrentProject(String name) {
        Config.META_LOADER.changeProject(name);
    }

    /**
     * @return a string with sensor names and converted values
     */
    public String toString(){
        StringBuilder sb=new StringBuilder();

        for(ERDataField field : _cur){
            sb.append(field.getSensor());
            sb.append(" ");
            sb.append(field.getValue());
            sb.append(" ");
        }
        sb.append("\n");
        return sb.toString();
    }

    public XYSeriesCollection getXySeriesCollection(ERSensorConfig[] sensorConf)
    {
        XYSeriesCollection newCollection = new XYSeriesCollection();
        for (ERSensorConfig conf : sensorConf) {
            XYSeriesCollection coll = getXySeriesCollection(conf);
            for (int i = 0; i < coll.getSeriesCount(); ++i) {
                coll.addSeries(coll.getSeries(i));
            }
        }

        return newCollection;
    }

    public XYSeriesCollection getXySeriesCollection(ERSensorConfig sensorConf)
    {
        XYSeriesCollection newCollection = new XYSeriesCollection();
        if (!xySeriesCollectionMap.containsKey(sensorConf.sensor)) {
            xySeriesCollectionMap.put(sensorConf.sensor, new XYSeriesCollection());
        }
        XYSeriesCollection seriesCollection = xySeriesCollectionMap.get(sensorConf.sensor);
        for (String attr : sensorConf) {
            XYSeries xySeries;
            int idx = seriesCollection.getSeriesIndex(attr);
            if (idx == -1) {
                // Couldn't find
                xySeries = new XYSeries(attr);
                seriesCollection.addSeries(xySeries);
            }
            else {
                xySeries = seriesCollection.getSeries(idx);
            }
            newCollection.addSeries(xySeries);
        }
        return newCollection;
    }

    @Deprecated
    public XYSeriesCollection getXySeriesCollection(String sensor) {
        if (!xySeriesCollectionMap.containsKey(sensor)) {
            xySeriesCollectionMap.put(sensor, new XYSeriesCollection());
        }
        return xySeriesCollectionMap.get(sensor);
    }

    public void setCurrentTime(float time){
        _currentTime = time;
    }

    // TODO Get from data instead ( should be first ? )
    public float getCurrentTime(){
        return _currentTime;
    }

    public void readInput(){
        if(Config.DATA_LOADER == null){ return; }

        ERDataField[] _latestData = Config.DATA_LOADER.readPackage();

        if(_latestData != null){
            for (ERDataField dataField : _latestData) {
                addERDataField(_mySelf.getCurrentTime(), dataField, getXySeriesCollection(dataField.getSensor()));
            }
            _mySelf.setNewDataFields(_latestData);
            notifyListeners();
        }
    }

    public void purgeData() {
        _cur = null;
        xySeriesCollectionMap.clear();
    }

    public void addListener(DataListener d){
        _listeners.add(d);
    }

    public void removeListener(DataListener d){
        _listeners.remove(d);
    }

    private void notifyListeners(){
        for(DataListener d : _listeners)
            d.dataUpdated(Data.getInstance());
    }

    private void addERDataField(double time, ERDataField dataField, XYSeriesCollection collection) {
        if (dataField != null) {
            XYSeries series;
            try {
                series = collection.getSeries(dataField.getAttribute());
            } catch (UnknownKeyException e) {
                if (dataField.getAttribute().equals("time")) {
                    /* FIXME: This only works since it's the first value */
                    if (dataField.getValue() < getCurrentTime()) {
                        /* Time has been reset, remove old data */
                        purgeData();
                    }
                    setCurrentTime(dataField.getValue());
                    return;
                }
                series = new XYSeries(dataField.getAttribute());
                collection.addSeries(series);
            }
            if (series != null) {
                series.add(time, dataField.getValue());
            }
        }
    }
}
