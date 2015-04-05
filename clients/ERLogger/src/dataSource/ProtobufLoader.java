package dataSource;

import Database.Messages.ExperimentMetaData;
import Database.Messages.ProjectMetaData;
import Errors.BackendError;
import FrontendConnection.Backend;
import FrontendConnection.Listeners.ExperimentMetaDataListener;
import FrontendConnection.Listeners.ProjectMetaDataListener;
import FrontendConnection.Listeners.UpdatedFrontendDataListener;
import common.*;
import protobuf.Protocol;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.BackingStoreException;

/**
 * Created by max on 2014-10-27.
 *
 * Uses yamsLog client-backend to communicate with yamsLog server.
 *
 * ALL communication with client-backend/server goes through here.
 */
public class ProtobufLoader implements Loader, MetaLoader, UpdatedFrontendDataListener,
        ProjectMetaDataListener {

    // TODO Set this somewhere in UI
    private static final int SOME_INT = 30;

    private Map<Integer, ArrayList<ArrayList<Float>>> currentDataMap = new ConcurrentHashMap<>();
    private Map<Integer, String> sensorNameMap = new ConcurrentHashMap<>();
    private Map<Integer, String[]> attributeNamesMap = new ConcurrentHashMap<>();
    private Map<Integer, Protocol.SensorStatusMsg.AttributeStatusType[]> attributeStatusesMap = new ConcurrentHashMap<>();

    private ProjectMetaData projectMetaData = new ProjectMetaData();

    public ProtobufLoader() {
        try {
            Backend.createInstance(null);
        } catch (BackendError backendError) {
            SimpleDebug.err("Error when creating yamsLog client-backend");
            backendError.printStackTrace();
        }
    }

    /**
     * Run for first initialization. Fetches initial configuration from client-backend.
     */
    private void initializeSensorConfiguration() {
        try {
            List<Database.Sensors.Sensor> sensorList = Backend.getInstance().getSensors();

            for (Database.Sensors.Sensor sensor : sensorList) {
                sensorNameMap.put(sensor.getId(), sensor.getSensorName());
                attributeNamesMap.put(sensor.getId(), sensor.getAttributesName());
                attributeStatusesMap.put(sensor.getId(), sensor.getAttributeStatuses());
            }
        } catch (BackendError backendError) {
            SimpleDebug.err("Error when initializing sensors");
            backendError.printStackTrace();
        }
    }

    /**
     * Finds ID for Sensor.
     * TODO This is a stupid way of using maps...
     */
    private int findIDByName(String sensor) {
        for (Map.Entry<Integer, String> entry : sensorNameMap.entrySet()) {
            if (entry.getValue().equals(sensor)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    // MetaLoader functions //////////////////////////////////////////

    @Override
    public void commitMetaData() {
        try {
            Backend.getInstance().sendProjectMetaData(projectMetaData);
        } catch (BackendError backendError) {
            backendError.printStackTrace();
        }
    }

    @Override
    public List<String> getSensorNames() {
        return new ArrayList<>(sensorNameMap.values());
    }

    @Override
    public List<String> getAttributeNames(String sensor) {
        String[] attr = attributeNamesMap.get(findIDByName(sensor));
        return attr == null ? null : new ArrayList<>(Arrays.asList(attr));
    }

    @Override
    public List<String> getProjectNames() {
        try {
            return Backend.getInstance().getProjectFilesFromServer();
        } catch (BackendError backendError) {
            backendError.printStackTrace();
        }
        return null;
    }

    @Override
    public String getCurrentProject() {
        try {
            return Backend.getInstance().getActiveProject();
        } catch (BackendError backendError) {
            backendError.printStackTrace();
        }
        return null;
    }

    @Override
    public void changeProject(String name) {
        try {
            if (getProjectNames().contains(name)) {
                Backend.getInstance().setActiveProject(name);
            }
            else {
                Backend.getInstance().createNewProjectRequest(name);
            }
        } catch (BackendError backendError) {
            backendError.printStackTrace();
        }
    }

    @Override
    public void setDate(Date date) {
        if (projectMetaData != null) {
            projectMetaData.setDate(date.getTime());
        }
    }

    @Override
    public void setEmail(String email) {
        if (projectMetaData != null) {
            projectMetaData.setEmail(email);
        }
    }

    @Override
    public void addTester(String name) {
        if (projectMetaData == null) {
            return;
        }
        if (projectMetaData.getTest_leader() == null) {
            projectMetaData.setTest_leader(name);
        }
        else {
            if (projectMetaData.getMember_names() == null) {
                projectMetaData.setMember_names(new ArrayList<String>());
            }
            projectMetaData.getMember_names().add(name);
        }
    }

    @Override
    public void clearTesters() {
        if (projectMetaData != null) {
            projectMetaData.setTest_leader(null);
            projectMetaData.setMember_names(null);
        }
    }

    @Override
    public void addTag(String tag) {
        if (projectMetaData == null) {
            return;
        }
        if (projectMetaData.getTags() == null) {
            projectMetaData.setTags(new ArrayList<String>());
        }
        projectMetaData.getTags().add(tag);
    }

    @Override
    public void clearTags() {
        if (projectMetaData != null) {
            projectMetaData.setTags(null);
        }
    }

    @Override
    public void setDescription(String descr) {
        if (projectMetaData != null) {
            projectMetaData.setDescription(descr);
        }
    }

    // Loader functions //////////////////////////////////////////

    @Override
    public boolean start() {
        /* Default name to date */
        SimpleDateFormat dateFormat = new SimpleDateFormat("yymmdd:HHmmss");
        Date date = new Date();
        String newName = dateFormat.format(date);
        return start(newName);
    }

    @Override
    public boolean start(String name) {
        try {
            // TODO: Separate project and experiment creation
            Backend.getInstance().startDataCollection(name);
            return true;
        } catch (BackendError backendError) {
            SimpleDebug.err("Error when starting data collection");
            backendError.printStackTrace();
        }
        return false;
    }

    @Override
    public void stop() {
        try {
            Backend.getInstance().stopDataCollection();
        } catch (BackendError backendError) {
            SimpleDebug.err("Error when stopping data collection");
            backendError.printStackTrace();
        }
    }

    @Override
    public boolean connect() {
        try {
            initializeSensorConfiguration();
            Backend.getInstance().setUpdatedFrontendDataListener(this);
            Backend.getInstance().addProjectMetaDataListener(this);
            Backend.getInstance().connectToServer(Config.SERVER_HOST, Config.SERVER_PORT);
            return true;
        } catch (BackendError backendError) {
            SimpleDebug.err("Couldn't connect to server");
            backendError.printStackTrace();
        }
        return false;
    }

    @Override
    public void disconnect() {
        try {
            Backend.getInstance().stopConnection();
        } catch (BackendError backendError) {
            backendError.printStackTrace();
        }
    }

    @Override
    public void requestData() {
        try {
            // TODO Figure out how and where to set this ONCE
            Backend.getInstance().setUpdatedFrontendDataListener(this);
            Backend.getInstance().addProjectMetaDataListener(this);
            Backend.getInstance().sendSettingsRequestMessageALlSensors(SOME_INT);
        } catch (BackendError backendError) {
            SimpleDebug.err("Error when sending sensor settings");
            backendError.printStackTrace();
        }
    }

    @Override
    public void requestDataStop() {
        try {
            // TODO Should we remove ourselves as FrontendDataListener as well ?
            Backend.getInstance().resetSensorsData();
            Backend.getInstance().sendSettingsRequestMessageSelected(0, new LinkedList<Integer>());
        } catch (BackendError backendError) {
            SimpleDebug.err("Error when sending empty sensor settings");
            backendError.printStackTrace();
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public ERDataField[] readPackage() {
        List<ERDataField> dataFields = new LinkedList<>();
        ERDataField dataField;

        // Handle stuff in client-backend
        try {
            Backend.getInstance().tick();
            for (int sensorId : sensorNameMap.keySet()) {
                Backend.getInstance().handleQueue(sensorId);
            }
        } catch (BackendError backendError) {
            backendError.printStackTrace();
        }

        // Go through all sensors and get newest data
        for (int sensorId : sensorNameMap.keySet()) {
            if (currentDataMap.containsKey(sensorId) && attributeNamesMap.containsKey(sensorId)) {
                Iterator<ArrayList<Float>> dataIterator = currentDataMap.get(sensorId).iterator();
                for (String attribute : attributeNamesMap.get(sensorId)) {
                    if (!dataIterator.hasNext()) {
                        break;
                    }
                    ArrayList<Float> data = dataIterator.next();
                    if (!data.isEmpty()) {
                        dataField = new ERDataField(sensorNameMap.get(sensorId), attribute);
                        dataField.setValue(data.get(data.size() - 1));
                        dataFields.add(dataField);
                    }
                }
            }
        }

        ERDataField[] ret = new ERDataField[dataFields.size()];
        ret = dataFields.toArray(ret);
        return ret;
    }

    // UpdatedFrontendDataListener functions /////////////////////
    /**
     * These functions are used by yamsLog client-backend to communicate
     * changes in server.
     */

    /**
     * Handle sensor name change for sensor i.
     */
    @Override
    public void updatedSensorName(String s, int i) {
        sensorNameMap.put(i, s);
    }

    /**
     * Handle atribute name change for sensor i.
     */
    @Override
    public void updatedAttributeNames(String[] strings, int i) {
        attributeNamesMap.put(i, strings);
    }

    /**
     * Handle updated data (for sensor i?).
     */
    @Override
    public void updatedFrontendList(ArrayList<ArrayList<Float>> arrayLists, int i) {
        currentDataMap.put(i, arrayLists);
    }

    /**
     * Handle attribute status change for sensor i e.g sensor not working.
     */
    @Override
    public void updatedCurrentAttributeStatus(protobuf.Protocol.SensorStatusMsg.AttributeStatusType[] attributeStatusTypes, int i) {
        attributeStatusesMap.put(i, attributeStatusTypes);
    }

    @Override
    public void projectMetaDataChanged(ProjectMetaData projectMetaData) {
        this.projectMetaData = projectMetaData;
    }

}
