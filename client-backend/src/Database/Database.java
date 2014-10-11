package Database;

import Database.Sensors.GenericMessage;
import Database.Sensors.Sensor;
import Errors.*;
import protobuf.Protocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The database in the backend, holds the relevant data received.
 */
public class Database {

    private static Database instance = null;
    private HashMap<Integer, Sensor> idToSensor;
    private List<String> experimentFilesList;
    private List<String> projectFilesList;
    private Protocol.StatusMsg.StatusType serverStatus;
	private ListenerHandler listenerHandler;
    private String activeProjectName;
    private List sensorConfigurationForPlayback;
	private String activeExperimentName;

    public static Database getInstance(){
        if(instance==null) instance = new Database();
        return instance;
    }

    /**
     * Constructor for the Database, call with the callers thread, currently unused
     * Initializes the objects used by the class
     */
    private Database(){
        idToSensor = new HashMap<Integer, Sensor>();
        experimentFilesList = new ArrayList<String>();
        projectFilesList = new ArrayList<String>();
        listenerHandler = new ListenerHandler();
        sensorConfigurationForPlayback = null;
        activeProjectName = null;
        serverStatus = null;
        activeExperimentName = null;
    }

    public List getSensorConfigurationForPlayback() {
        return sensorConfigurationForPlayback;
    }

    public void tick(){
        listenerHandler.tick();
    }

    public void updateAllSensorsTime(){
//        for(Sensor s : getSensors()){
//            s.updateTime();
//        }
    }

    //This may be used in future, although this is not checked at all.
   /* public void setSensorConfigurationForPlayback(List<Protocol.SensorConfiguration> sensorConfigurationForPlayback) throws BackendError {
        this.sensorConfigurationForPlayback = sensorConfigurationForPlayback;
        for(int sensorIndex=0;sensorIndex<sensorConfigurationForPlayback.size();sensorIndex++) {
            try{
                Protocol.SensorConfiguration config = sensorConfigurationForPlayback.get(sensorIndex);
                Sensor tempSensor = addNewSensor(config.getSensorId());
                String[] strings = new String[config.getAttributeConfigurationsCount()+1];  //TODO!
                strings[0]="time";
                for(int attributeIndex = 0; attributeIndex < config.getAttributeConfigurationsCount();attributeIndex++){
                    Protocol.AttributeConfiguration attributeConfiguration = config.getAttributeConfigurations(attributeIndex);
                    strings[attributeConfiguration.getIndex()+1] = attributeConfiguration.getSensorName();
                }
                for(int attributeIndex = 0; attributeIndex < strings.length; attributeIndex++){
                    if(strings[attributeIndex]==null) strings[attributeIndex] = "attribute " + attributeIndex;
                }
                tempSensor.setId(sensorConfigurationForPlayback.get(sensorIndex).getSensorId());
                tempSensor.setSensorName(sensorConfigurationForPlayback.get(sensorIndex).getSensorName());
                tempSensor.setAttributesName(strings);
                tempSensor.setAttributesCount(sensorConfigurationForPlayback.get(sensorIndex).getMaxAttributes());
            } catch (SensorAlreadyExistsError e){
                //TODO: Somthing
                // System.out.println("Sensor already exists, id: " + sensorConfigurationForPlayback.get(sensorIndex).getSensorId());
            }
        }
    }    */



    public String getActiveProjectName() {
        return activeProjectName;
    }

    public void setActiveProjectName(String activeProjectName) {
        this.activeProjectName = activeProjectName;
    }
    
    public void setActiveExperimentName(String activeExperimentName) {
        this.activeExperimentName = activeExperimentName;
    }
    
    public String getActiveExperimentName() {
        return activeExperimentName;
    }

    private synchronized List<String> readOrWriteExperimentFilesList(List<String> data) throws BackendError{
        if(data!=null) this.experimentFilesList = data;
        if(this.experimentFilesList==null) throw new NoDataError();
        return new ArrayList<String>(this.experimentFilesList);
    }

    public void setExperimentFilesList(List<String> experimentListFromServer) throws BackendError{
        readOrWriteExperimentFilesList(experimentListFromServer);
        listenerHandler.experimentListChanged();
    }

    public List<String> getExperimentFilesList() throws BackendError {
        return readOrWriteExperimentFilesList(null);
    }

    private synchronized List<String> readOrWriteProjectFilesList(List<String> data) throws BackendError{
        if(data!=null) this.projectFilesList = data;
        if(projectFilesList==null) throw new NoDataError();
        return new ArrayList<String>(projectFilesList);
    }

    public void setProjectFilesList(List<String> projectFilesList) throws BackendError{
        readOrWriteProjectFilesList(projectFilesList);
        listenerHandler.projectListChanged();
    }

    public synchronized Protocol.StatusMsg.StatusType readWriteServerStatus(Protocol.StatusMsg.StatusType status) throws StatusNotReceivedError {
        if (status != null) serverStatus = status;
        if (serverStatus == null) throw new StatusNotReceivedError();
        return serverStatus;
    }

    public List<String> getProjectFilesList() throws BackendError {
        return readOrWriteProjectFilesList(null);
    }

    public List<Sensor> getSensors() {
        return new ArrayList(idToSensor.values());
    }

    @Deprecated
    public Sensor getSensor(int index) {
        return (Sensor)idToSensor.values().toArray()[index];
    }

    public Sensor addNewSensor(int id) throws BackendError {
        if(idToSensor.containsKey(id))throw new SensorAlreadyExistsError();
        idToSensor.put(id,new GenericMessage(id,this));
        return idToSensor.get(id);
    }

    public Sensor getSensorByID(int id) throws BackendError{
        if(idToSensor.containsKey(id)) return idToSensor.get(id);
        throw new SensorDoesNotExistError(id);
    }

    public ListenerHandler getListenerHandler() {
        return listenerHandler;
    }
    

    public void sensorStatusUpdated(Sensor sensor){
        listenerHandler.sensorStatusChanged(sensor);
    }
    
    public Protocol.StatusMsg.StatusType getServerStatus() {
		return serverStatus;
	}
}