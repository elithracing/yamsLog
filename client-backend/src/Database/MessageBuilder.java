package Database;

import Database.Messages.ProjectMetaData;
import Database.Sensors.Sensor;
import Errors.*;
import FrontendConnection.Backend;
import protobuf.Protocol;
import protobuf.Protocol.AttributeConfiguration;
import protobuf.Protocol.ErrorMsg;
import protobuf.Protocol.GeneralMsg;
import protobuf.Protocol.SensorConfiguration;

import java.io.IOException;
import java.io.InputStream;

/**
 * Converts the GeneralMsg to a datapoint in a Sensor object or handles the received data.
 */
public class MessageBuilder {
    GeneralMsg gm;
    private Database database;

    /**
     *  The message builder is initialized. It contains a in, which is a buffered reader, since it is not
     *  safe to assume that all information will come at once. It also contains a database, in which to add the relevant
     *  information.
     */
    public MessageBuilder(){
        this.database = Database.getInstance();
    }

    /**
     * Method for building a general message from an InputStream
     * @param is is the InputStream from the server
     * @throws IOException may return IOException if the stream is broken or the data corrupted.
     */
    public void buildMessage(InputStream is) throws BackendError, Exception{
        /*
        A general message is picked up from the Input stream. This input stream can be from the server or the tablet.
         */
        try{
            gm = GeneralMsg.parseDelimitedFrom(is);
            if(gm==null) throw new ConnectionError("No data received.");
		    /*
		     *  Switch on what type of message the Genaral Message is
		    */
            switch (gm.getSubType()){
	
	            case DATA_T:
	                handleData(gm);
	                break;
	            case DYNAMIC_EVENT_T:
	                handleDynamicEvent(gm);
	                break;
	            case SET_DYNAMIC_EVENT_RESPONSE_T:
	                handleDynamicEventResponse(gm);
	                break;
	            case CREATE_NEW_PROJECT_RESPONSE_T:
	                handleCreateNewProjectResponse(gm);
	                break;
	            case RENAME_PROJECT_RESPONSE_T:
	                handleRenameProjectResponse(gm);
	                break;
	            case SET_ACTIVE_PROJECT_RESPONSE_T:
	                handleSetActiveProjectResponse(gm);
	                break;
	            case PROJECT_METADATA_T:
	                handleProjectMetaData(gm);
	                break;
	            case EXPERIMENT_METADATA_T:
	                handleExperimentMetaData(gm);
	                break;
	            case SET_EXPERIMENT_METADATA_RESPONSE_T:
	                handleExperimentMetadataResponse(gm);
	                break;
	            case SET_PROJECT_METADATA_RESPONSE_T:
	                handleProjectMetadataResponse(gm);
	                break;
	            case EXPERIMENT_DATA_COLLECTION_START_RESPONSE_T:
	                handleExperimentDataCollectionStartResponse(gm);
	                break;
	            case EXPERIMENT_DATA_COLLECTION_STOP_RESPONSE_T:
	                handleExperimentDataCollectionStopResponse(gm);
	                break;
	            case RENAME_EXPERIMENT_RESPONSE_T:
	                handleRenameExperimentResponse(gm);
	                break;
	            case EXPERIMENT_PLAYBACK_START_RESPONSE_T:
	                handleExperimentPlaybackStartResponse(gm);
	                break;
	            case EXPERIMENT_PLAYBACK_STOP_RESPONSE_T:
	                handleExperimentPlaybackStopResponse(gm);
	                break;
	            case EXPERIMENT_LIST_T:
	                handleExperimentList(gm);
	                break;
	            case REMOVE_PROJECT_RESPONSE_T:
	                handleRemoveProjectResponse(gm);
	                break;
	            case REMOVE_EXPERIMENT_RESPONSE_T:
	                handleRemoveExperimentResponse(gm);
	                break;
	            case SENSOR_STATUS_T:
	                handleSensorStatus(gm);
	                break;
	            case CONFIGURATION_T:
	                handleConfiguration(gm);
	                break;
	            case SETTINGS_RESPONSE_T:
	                handleSettingsResponse(gm);
	                break;
	            case STATUS_T:
	                handleStatus(gm);
	                break;
	            case PROJECT_LIST_T:
	                handleProjectList(gm);
	                break;
	            case DEBUG_T:
	                handleDebug(gm);
	                break;
	            case ERROR_T:
	                handleError(gm);
	                break;
	            case ACTIVE_PROJECT_T:
	                handleActiveProject(gm);
	                break;
	            case ACTIVE_EXPERIMENT_T:
	            	handleActiveExperiment(gm);
	            	break;
	            default:
                /*
                default case: if we get to this case something was sent from the server
                that we can't recognize. Check protocol for new gm types.
                 */
                ErrorSender.getInstance().sendError(ErrorMsg.ErrorType.UNKNOWN_GENERALMSG_SUBTYPE);
            }
        } catch (IOException e){
            //TODO: Kolla med en riktig server som inte skickar n�got om detta �r ett rpoblem?!?
            throw new ConnectionError("Not receiving data from server.");
        }
    }

    private void handleDynamicEventResponse(GeneralMsg gm) {
        database.getListenerHandler().responseReceivedChanged(gm.getSubType(), gm.getSetDynamicEventResponse().getResponseType());
    }

    private void handleProjectMetadataResponse(GeneralMsg gm) {
        database.getListenerHandler().responseReceivedChanged(gm.getSubType(), gm.getSetProjectMetadataResponse().getResponseType());
    }

    private void handleExperimentMetadataResponse(GeneralMsg gm) {
        database.getListenerHandler().responseReceivedChanged(gm.getSubType(), gm.getSetExperimentMetadataResponse().getResponseType());
    }

    private void handleRemoveExperimentResponse(GeneralMsg gm) {
        database.getListenerHandler().responseReceivedChanged(gm.getSubType(), gm.getRemoveExperimentResponse().getResponseType());

    }

    private void handleRemoveProjectResponse(GeneralMsg gm) {
        database.getListenerHandler().responseReceivedChanged(gm.getSubType(), gm.getRemoveProjectResponse().getResponseType());
    }

    private void handleActiveProject(GeneralMsg gm) throws BackendError{
    	if(gm.getActiveProject().hasName()){
	        database.setActiveProjectName(gm.getActiveProject().getName());
	        Backend.getInstance().activeProjectUpdated(gm.getActiveProject().getName());
    	}
    }
    
    private void handleActiveExperiment(GeneralMsg gm) throws BackendError{
    	if(gm.getActiveExperiment().hasName()){
	        database.setActiveExperimentName(gm.getActiveExperiment().getName());
	        Backend.getInstance().activeExperimentUpdated(gm.getActiveExperiment().getName());
    	}
    }

    /**
     * S�tter listan med experimentfiler som kommer fr�n servern
     * Sets the list of experimentfiles from the server
     * @param gm is the general message being handled.
     */
    private void handleExperimentList(GeneralMsg gm) throws BackendError{
        database.setExperimentFilesList(gm.getExperimentList().getNamesList());
    }

    private void handleProjectList(GeneralMsg gm) throws BackendError{
        database.setProjectFilesList(gm.getProjectList().getProjectsList());
    }

    private void handleExperimentPlaybackStopResponse(GeneralMsg gm) {
        //System.out.println(gm.getExperimentPlaybackStopResponse().toString());
        database.getListenerHandler().responseReceivedChanged(gm.getSubType(),
                gm.getExperimentPlaybackStopResponse().getResponseType());
    }

    private void handleExperimentPlaybackStartResponse(GeneralMsg gm) {
        //System.out.println(gm.getExperimentPlaybackStartResponse().toString());
        database.getListenerHandler().responseReceivedChanged(gm.getSubType(),
                gm.getExperimentPlaybackStartResponse().getResponseType());
    }

    private void handleExperimentDataCollectionStopResponse(GeneralMsg gm) {
        //System.out.println(gm.getExperimentDataCollectionStopResponse().toString());
        database.getListenerHandler().responseReceivedChanged(gm.getSubType(),
                gm.getExperimentDataCollectionStopResponse().getResponseType());
    }

    private void handleExperimentDataCollectionStartResponse(GeneralMsg gm) {
       // System.out.println(gm.getExperimentDataCollectionStartResponse().toString());
        database.getListenerHandler().responseReceivedChanged(gm.getSubType(),
                gm.getExperimentDataCollectionStartResponse().getResponseType());

    }

    private void handleSetActiveProjectResponse(GeneralMsg gm) {
       // System.out.println(gm.getSetActiveProjectResponse());
        database.getListenerHandler().responseReceivedChanged(gm.getSubType(),
                gm.getSetActiveProjectResponse().getResponseType());
       /* try {
            Backend.getInstance().ExperimentDataCollectionStartResponse(gm.getSetActiveProjectResponse().getResponseType());
        } catch (BackendError backendError) {
            backendError.printStackTrace();
        }*/
    }

    private void handleRenameProjectResponse(GeneralMsg gm) {
       // System.out.println(gm.getRenameProjectResponse().toString());
        database.getListenerHandler().responseReceivedChanged(gm.getSubType(),
                gm.getRenameProjectResponse().getResponseType());
    }

    private void handleCreateNewProjectResponse(GeneralMsg gm) {
        database.getListenerHandler().projectCreationStatusChanged(gm.getCreateNewProjectResponse().getResponseType());//System.out.println(gm.getCreateNewProjectResponse().toString());
    }

    private void handleRenameExperimentResponse(GeneralMsg gm){
        database.getListenerHandler().responseReceivedChanged(gm.getSubType(),
                gm.getRenameExperimentResponse().getResponseType());
    }


    /**
     * Gets the configuration for the general message coming from the server.
     * The configuration contains information about, for example, which attributes are treated by the sensor.
     * @param gm
     */
    private void handleConfiguration(GeneralMsg gm){

        for(int i = 0; i < gm.getConfiguration().getSensorConfigurationsCount(); i++){
            try{
                SensorConfiguration sensorConfiguration = gm.getConfiguration().getSensorConfigurations(i);
                Sensor sensor = database.addNewSensor(sensorConfiguration.getSensorId());
                String[] attributeNames = new String[sensorConfiguration.getMaxAttributes()+1];
                attributeNames[0]="time";
                for(int j = 0; j < sensorConfiguration.getAttributeConfigurationsCount();j++){
                    AttributeConfiguration attributeConfiguration = sensorConfiguration.getAttributeConfigurations(j);
                    attributeNames[attributeConfiguration.getIndex()+1] = attributeConfiguration.getName();
                }
                for(int j = 0; j < attributeNames.length; j++){
                    if(attributeNames[j]==null) attributeNames[j] = "attribute " + j;
                }
                sensor.setId(sensorConfiguration.getSensorId());
                sensor.setSensorName(sensorConfiguration.getName());
                sensor.setAttributesName(attributeNames);
                sensor.setAttributesCount(sensorConfiguration.getMaxAttributes());
            } catch (BackendError e) {

            }
        }
    }


    private void handleData(GeneralMsg gm) throws BackendError, Exception{
        try{
            database.getSensorByID(gm.getData().getTypeId()).addData(gm);
        } catch (SensorDoesNotExistError e){
            ErrorSender.getInstance().sendError(ErrorMsg.ErrorType.UNKNOWN_SENSOR_ID);
        }
    }



    /**
     * Gets a message containing a list of sensors whose status has changed
     * This list is iterated over and the status of the sensors change to the value provided by getStatus.
     *
     * @param gm is the general message being handled.
     */

    private void handleSensorStatus(GeneralMsg gm) throws BackendError {
    	System.out.println(gm.toString());
    	System.out.println("NUmber of sensors messages in gm = " + gm.getSensorStatus().getSensorsCount());
    	for(int i=0; i<gm.getSensorStatus().getSensorsCount(); i++) {
        	
            Sensor currentSensor = database.getSensorByID(gm.getSensorStatus().getSensors(i).getSensorId());
            System.out.println("SEnsor message nr " + i  +" to " + currentSensor.getId() + "sätts till " + gm.getSensorStatus().getSensors(i).getStatus().toString());
            currentSensor.setSensorStatus(gm.getSensorStatus().getSensors(i));
            database.getListenerHandler().sensorStatusChanged(currentSensor);
        }
    	System.out.println("Done");
    }

    private void handleSettingsResponse(GeneralMsg gm){
        //System.out.println(gm.getSettingsResponse().toString());
        database.getListenerHandler().responseReceivedChanged(gm.getSubType(),
                gm.getSettingsResponse().getResponseType());
    }

    private void handleDynamicEvent(GeneralMsg gm) {
        //System.out.println(gm.getDynamicEvent());
        database.getListenerHandler().dynamicEvent(gm.getDynamicEvent().getDynamicEvent().getMessage());
    }
    private void handleStatus(GeneralMsg gm) throws StatusNotReceivedError {
        //System.out.println(gm.getStatus().toString()); //todo: sout only for debug
        database.readWriteServerStatus(gm.getStatus().getStatusType());
    }
    private void handleError(GeneralMsg gm){
        //System.out.println(gm.getErrorMessage().toString());//todo: sout only for debug

        database.getListenerHandler().serverErrorMessageReceived(gm.getErrorMessage().getErrorType());
    }
    private void handleDebug(GeneralMsg gm){
       // System.out.println(gm.getDebugMessage().getDebugMessage());
    }
    private void handleProjectMetaData(GeneralMsg gm){
        database.getListenerHandler().projectMetaDataChanged(new ProjectMetaData(gm.getProjectMetadata()));
    }

    private void handleExperimentMetaData(GeneralMsg gm) throws BackendError {
        /*System.out.println(gm);
        database.getListenerHandler().experimentMetaDataChanged(new ExperimentMetaData(gm.getExperimentMetadata()));
        try{
            database.setSensorConfigurationForPlayback(gm.getExperimentMetadata().getMetadata().getSensorConfigurationsList()); //todo, remove
        }catch (BackendError SensorAlreadyExistsError) {
            System.out.println("sens alrdy exists, database size: " + database.getSensors().size());
        }*/
    }
}



