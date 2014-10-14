/**
 * yamsLog is a program for real time multi sensor logging and 
 * supervision
 * Copyright (C) 2014  
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package FrontendConnection;

import Database.Database;
import Database.Messages.ExperimentMetaData;
import Database.Messages.ProjectMetaData;
import Database.Messages.RequestSender;
import Database.Sensors.GenericMessage;
import Database.Sensors.Sensor;
import Errors.BackendAlreadyExistsError;
import Errors.BackendDoesNotExistError;
import Errors.BackendError;
import Errors.OtherItemRequiredError;
import FrontendConnection.Listeners.*;
import ServerConnection.Handle;
import protobuf.Protocol;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: Aitesh Date: 2014-02-20 Time: 09:50
 * 
 * The Backend class is the connection between the front-end and the back-end.
 * It is created as a singleton and makes it possible for the front-end to get
 * data from the database. It can also be used to send messages to the server
 */
public class Backend {

	private static Backend instance = null;
	private static Database database;
	final private Handle handle;
	private Thread.UncaughtExceptionHandler eh;
	private String temporaryName;

	private Backend(Thread.UncaughtExceptionHandler eh) throws BackendError {
		this.database = Database.getInstance();
		this.handle = new Handle();
		this.eh = eh;
	}
	/**
	 * Creates an instance of the Backend.
	 * 
	 * @param eh
	 *            is an exception handler for uncaught exceptions.
	 * @return an instance of the backend with the connection with the server
	 * @throws BackendError
	 *             if there already exists an instance of backend.
	 */
	public static Backend createInstance(Thread.UncaughtExceptionHandler eh)
			throws BackendError {
		if (instance == null) {
			instance = new Backend(eh);
			return instance;
		} else
			throw new BackendAlreadyExistsError();
	}

	/**
	 * Gets the actual instance of the backend.
	 * 
	 * @return an instance of the backend with connection to server.
	 * @throws BackendError
	 *             if called before backend is created.
	 */
	public static Backend getInstance() throws BackendError {
		if (instance == null)
			throw new BackendDoesNotExistError();
		return instance;
	}

	public void tick() {
		database.tick();
	}

	public List getSensorConfigurationForPlayback() {
		return database.getSensorConfigurationForPlayback(); // todo remove
	}
	
	public void setUpdatedFrontendDataListener(UpdatedFrontendDataListener frontendListListener){
		List<Sensor> sensors = Database.getInstance().getSensors();
		for(int i = 0; i < sensors.size(); i++){
			sensors.get(i).setFrontendDataChangedListener(frontendListListener);
		}
	}
	
    public void setFilterConstants(int fullResLimit, int filterLimit){
    	List<Sensor> sensors = Database.getInstance().getSensors();
		for(int i = 0; i < sensors.size(); i++){
			sensors.get(i).setFilterConstants(fullResLimit, filterLimit);
		}
    }

	/**
	 * Constructor for the Backend that instantiates handle and
	 * exceptionhandler.
	 * 
	 * @param eh
	 *            is an exception handler for uncaught exceptions.
	 * @throws BackendError
	 *             is thrown if handle already exists
	 */



	/**
	 * attempts to connect to a server with a name and port.
	 * 
	 * @param server
	 *            is defaulted to morris.isy.liu.se during development.
	 * @param port
	 *            is set to 2001 but this is only during development.
	 * @throws BackendError
	 */

	public void connectToServer(String server, int port) throws BackendError {
		handle.connectToServer(server, port, eh);
	}

	@Deprecated
	public Sensor getSensor(int index) {
		return database.getSensor(index);
	}

	public Sensor getSensorById(int id) throws BackendError {
		return database.getSensorByID(id);
	}

	/**
	 * @return The actual list of sensors from the database.
	 */
	public List<Sensor> getSensors() {
		return database.getSensors();
	}

	/**
	 * Used to fetch the names of files that are on the server.
	 * 
	 * @return the list of files currently on the server, as it is in the
	 *         database.
	 * @throws BackendError
	 */

	public List<String> getProjectFilesFromServer() throws BackendError {
		return database.getProjectFilesList();
	}

	/**
	 * Called by frontend to get the list of experiment files that are on the
	 * server
	 * 
	 * @return the list of experiments
	 * @throws BackendError
	 */
	public List<String> getExperimentFilesFromServer() throws BackendError {
		return database.getExperimentFilesList();
	}

	/**
	 * Is used to send used-specified events to the server, which are saved for
	 * later.
	 * 
	 * @param time
	 *            is the timestamp
	 * @param message
	 *            is the message being sent
	 * @throws BackendError
	 */

	public void sendDynamicMessage(double time, String message)
			throws BackendError {
		handle.sendDynamicMessage(time, message);
	}

	/**
	 * Start data collection with specified name.
	 * 
	 * @param filename
	 *            is the filename we want to use.
	 * @throws BackendError
	 */

	public void startDataCollection(String filename) throws BackendError {
		handle.startDataCollection(filename);
		setTemporaryName(filename);
	}

	/**
	 * Sends a message to the outputStream connected to the socket.
	 * 
	 * @param message
	 *            is sent.
	 * @throws BackendError
	 */

	public void sendMessage(Protocol.GeneralMsg message) throws BackendError {
		// System.out.println("Sending\n"+message);
		handle.sendMessage(message);
	}

	/**
	 * The rate at which data �s sent from the server can be regulated using
	 * this function. This is because there might be bad connection at times,
	 * which might call for a decrease in data being sent (for example if the
	 * client is connected over a wireless connection instead of LAN.
	 * 
	 * @param time
	 *            is how much the server should wait between sending data to
	 *            specific sensors.
	 * @throws BackendError
	 */

	public void sendSettingsRequestMessageALlSensors(int time)
			throws BackendError {
		sendMessage(Protocol.GeneralMsg
				.newBuilder()
				.setSubType(Protocol.GeneralMsg.SubType.SETTINGS_REQUEST_T)
				.setSettingsRequest(
						Protocol.SettingsRequestMsg.newBuilder()
								.setMinTime(time).addAllSensorIds(getIDList())
								.build()).build());
	}

	private List<Integer> getIDList() {
		ArrayList<Integer> returnList = new ArrayList<Integer>();
		for (int i = 0; i < getSensors().size(); i++) {
			returnList.add(getSensors().get(i).getId());
		}
		return returnList;
	}

	public void sendSettingsRequestMessageSelected(int time,
													List<Integer> intList) throws BackendError {
		sendMessage(Protocol.GeneralMsg
				.newBuilder()
				.setSubType(Protocol.GeneralMsg.SubType.SETTINGS_REQUEST_T)
				.setSettingsRequest(
						Protocol.SettingsRequestMsg.newBuilder()
								.setMinTime(time).addAllSensorIds(intList)
								.build()).build());
	}

	/**
	 * Requests for the server to start data collection.
	 * 
	 * @throws BackendError
	 */

	public void setTemporaryName(String filename) {
		temporaryName = filename;
	}

	public String getTemporaryName() {
		return temporaryName;
	}

	/**
	 * Requests for the server to stop data collection.
	 * 
	 * @throws BackendError
	 */

	public void stopDataCollection() throws BackendError {
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		handle.stopDataCollection();
	}

	public void debugger(String s) throws BackendError {
		handle.debugger(s);
	}

	public void createNewProjectRequest(String projectName) throws BackendError {
		RequestSender.getInstance().sendRequest(
				Protocol.GeneralMsg.SubType.CREATE_NEW_PROJECT_REQUEST_T,
				projectName);
	}

	public void renameProjectRequest(String oldName, String newName)
			throws BackendError {
		handle.debugger("renameRequest");
		RequestSender.getInstance().sendRequest(
				Protocol.GeneralMsg.SubType.RENAME_PROJECT_REQUEST_T, oldName,
				newName);
	}

	public void renameExperimentRequest(String oldName, String newName)
			throws BackendError {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		RequestSender.getInstance().sendRequest(
				Protocol.GeneralMsg.SubType.RENAME_EXPERIMENT_REQUEST_T,
				oldName, newName);
	}

	public void removeProjectRequest(String projectName) throws BackendError {
		RequestSender.getInstance().sendRequest(
				Protocol.GeneralMsg.SubType.REMOVE_PROJECT_REQUEST_T,
				projectName);
	}

	public void removeExperimentRequest(String experimentName)
			throws BackendError {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		RequestSender.getInstance().sendRequest(
				Protocol.GeneralMsg.SubType.REMOVE_EXPERIMENT_REQUEST_T,
				experimentName);
	}

	/**
	 * Sends a project meta data defined by the front-end. It contains several
	 * fields that are all optional (aside from needing a project name).
	 * 
	 * @param projectMetaData
	 *            is set by front-end
	 * @throws Errors.BackendError
	 */

	public void sendProjectMetaData(ProjectMetaData projectMetaData)
			throws BackendError {
		Protocol.ProjectMetadataStruct.Builder structBuilder = Protocol.ProjectMetadataStruct
				.newBuilder();
		if (projectMetaData.getTest_leader() != null)
			structBuilder.setTestLeader(projectMetaData.getTest_leader());
		if (projectMetaData.getDate() != null)
			structBuilder.setDate(projectMetaData.getDate());
		if (projectMetaData.getEmail() != null)
			structBuilder.setEmail(projectMetaData.getEmail());
		if (projectMetaData.getMember_names() != null)
			structBuilder.addAllMemberNames(projectMetaData.getMember_names());
		if (projectMetaData.getTags() != null)
			structBuilder.addAllTags(projectMetaData.getTags());
		if (projectMetaData.getDescription() != null)
			structBuilder.setDescription(projectMetaData.getDescription());
		sendMessage(Protocol.GeneralMsg
				.newBuilder()
				.setSubType(
						Protocol.GeneralMsg.SubType.SET_PROJECT_METADATA_REQUEST_T)
				.setSetProjectMetadataRequest(
						Protocol.SetProjectMetadataRequestMsg.newBuilder()
								.setMetadata(structBuilder.build()).build())
				.build());
	}

	/*
	 * public void debugMeta(String expName){ try { handle.debugger(expName); }
	 * catch (BackendError backendError) { backendError.printStackTrace(); } }
	 */

	public void sendExperimentMetaData(ExperimentMetaData experimentMetaData)
			throws BackendError {
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Protocol.SetExperimentMetadataRequestMsg.Builder msgBuilder = Protocol.SetExperimentMetadataRequestMsg
				.newBuilder();
		if (experimentMetaData.getExperimentDescription() != null)
			msgBuilder.setNotes(experimentMetaData.getExperimentDescription());
		if (experimentMetaData.getExperimentName() == null)
			throw new OtherItemRequiredError();
		else
			msgBuilder
					.setExperimentName(experimentMetaData.getExperimentName());
		if (experimentMetaData.getTags() != null)
			msgBuilder.addAllTags(experimentMetaData.getTags());

		sendMessage(Protocol.GeneralMsg
				.newBuilder()
				.setSubType(
						Protocol.GeneralMsg.SubType.SET_EXPERIMENT_METADATA_REQUEST_T)
				.setSetExperimentMetadataRequest(msgBuilder.build()).build());
	}

	public void sendExperimentPlaybackRequest(String name,
			List<Integer> listOfSensorIds) throws BackendError {
		Protocol.ExperimentPlaybackStartRequestMsg.Builder msgBuilder = Protocol.ExperimentPlaybackStartRequestMsg
				.newBuilder();
		if (name != null)
			msgBuilder.setName(name);
		if (listOfSensorIds != null)
			msgBuilder.addAllSensorIds(listOfSensorIds);

		sendMessage(Protocol.GeneralMsg
				.newBuilder()
				.setSubType(
						Protocol.GeneralMsg.SubType.EXPERIMENT_PLAYBACK_START_REQUEST_T)
				.setExperimentPlaybackStartRequest(msgBuilder.build()).build());
	}

	/**
	 * The name of the active project is sent to the server. This is part of the
	 * protocol.
	 * 
	 * @param projectName
	 *            is the name of the project.
	 * @throws BackendError
	 */

	public void setActiveProject(String projectName) throws BackendError {
		sendMessage(Protocol.GeneralMsg
				.newBuilder()
				.setSubType(
						Protocol.GeneralMsg.SubType.SET_ACTIVE_PROJECT_REQUEST_T)
				.setSetActiveProjectRequest(
						Protocol.SetActiveProjectRequestMsg.newBuilder()
								.setName(projectName).build()).build());
		database.setActiveProjectName(projectName);
		
	}
	
	public void activeProjectUpdated(String projectName){
		database.getListenerHandler().activeProject(projectName);
	}
	
	public void activeExperimentUpdated(String experimentName){
		database.getListenerHandler().activeExperiment(experimentName);
	}

	public void handleQueue(int sensorId) throws BackendError {
		try {
			((GenericMessage) database.getSensorByID(sensorId)).handleQueue();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * gets the name of the active project from the database.
	 * 
	 * @return the name of the active project.
	 */

	public String getActiveProject() {
		return database.getActiveProjectName();
	}

	/**
	 * This will be used when playing old experiments. this is not yet
	 * implemented in front-end. todo
	 * 
	 * @param experimentName
	 *            is the name of the wanted experiment.
	 * @throws BackendError
	 */

	public void startExperimentPlayback(String experimentName)
			throws BackendError {
		// todo: playback inte impl. i front-end.
		// todo: errorhandling.
		RequestSender
				.getInstance()
				.sendRequest(
						Protocol.GeneralMsg.SubType.EXPERIMENT_PLAYBACK_START_REQUEST_T,
						experimentName);

	}
	
	public String getActiveExperiment(){
		return database.getActiveExperimentName();
	}

	/*
	 * LISTENERS
	 * 
	 * Below are listeners, they listen for events coming from the server. this
	 * might be for example a response to a message or an error.
	 * 
	 * They are used to notify the front-end that something has occurred that
	 * they may be interested in.
	 */

	/**
	 * Adds a listener for the project list. Notifies front-end if the list of
	 * projects from the server has been updated.
	 * 
	 * @param listener
	 *            gets added to the collection of listeners.
	 */
	public void addProjectListChangedListener(
			ProjectListChangedListener listener) {
		database.getListenerHandler().addProjectListChangedListener(listener);
	}

	/**
	 * Adds a listener for the experiment list. An experiment (or several) is
	 * located in a project.
	 * 
	 * Notifies front-end if the list of experiments from the server has been
	 * updated.
	 * 
	 * @param listener
	 *            is added to the experiment list changed listeners.
	 */

	public void addExperimentListChangedListener(
			ExperimentListChangedListener listener) {
		database.getListenerHandler()
				.addExperimentListChangedListener(listener);
	}

	/**
	 * Notifies front-end about the status of the project creation. For example
	 * if the project name already exists on the server.
	 * 
	 * @param listener
	 *            is added to the project creation status listeners.
	 */
	public void addProjectCreationStatusListener(
			ProjectCreationStatusListener listener) {
		database.getListenerHandler()
				.addProjectCreationStatusListener(listener);
	}

	/**
	 * Notifies front-end about the arrival of experiment meta data. This meta
	 * data might include test driver, driving conditions, etc.
	 * 
	 * @param listener
	 *            is added to the experiment metadata listeners.
	 */

	public void addExperimentMetaDataListener(
			ExperimentMetaDataListener listener) {
		database.getListenerHandler().addExperimentMetaDataListener(listener);
	}

	/**
	 * Notifies front-end about the arrival of project meta data. This meta data
	 * might include test leader, e-mail, etc.
	 * 
	 * @param listener
	 *            is added to the project metadata listeners.
	 */

	public void addProjectMetaDataListener(ProjectMetaDataListener listener) {
		database.getListenerHandler().addProjectMetaDataListener(listener);
	}

	/**
	 * Notifies front-end about the arrival of a dynamic event. This dynamic
	 * event in particular is supposed to be used during playback, since that is
	 * the only time we will be getting dynamic events from the server.
	 * 
	 * @param listener
	 *            is added to the dynamic event listeners.
	 */

	public void addDynamicEventListener(DynamicEventListener listener) {
		database.getListenerHandler().addDynamicEventListener(listener);
	}

	/**
	 * Notifies front-end if the status of the server is changed. The status can
	 * be for example IDLE or DATA_COLLECTION.
	 * 
	 * @param listener
	 *            is added to the listeners for sensor status.
	 */

	public void addSensorStatusListener(SensorStatusListener listener) {
		database.getListenerHandler().addSensorStatusListener(listener);
	}
	
	public void addServerStatusListener(ServerStatusListener listener){
		database.getListenerHandler().addServerStatusListener(listener);
	}
	
	public void addActiveProjectListener(ActiveProjectListener listener){
		database.getListenerHandler().addActiveProjectListener(listener);
	}

	/**
	 * Removes existing listener from the collection of listeners.
	 * 
	 * @param listener
	 *            is removed from the collection.
	 */
	public void removeProjectListChangedListener(
			ProjectListChangedListener listener) {
		database.getListenerHandler()
				.removeProjectListChangedListener(listener);
	}

	/**
	 * Called by front-end. Might not work. Todo: test
	 * 
	 * @throws BackendError
	 */
	public void restartServerListener() throws BackendError {
		handle.restartServerListener();
	}
	
	
	public void stopConnection() throws BackendError {
		handle.stopConnection();
	}
	
	public void resetSensorsData(){
		for(Sensor sensor: Database.getInstance().getSensors()){
			sensor.resetData();
		}
	}
	
	public LinkedList<Integer> getSensorIds(){
		LinkedList<Integer> sensorIds = new LinkedList<Integer>();
		for(Sensor sensor : Database.getInstance().getSensors()){
			sensorIds.add(sensor.getId());
		}
		return sensorIds;
	}
	
	public Protocol.StatusMsg.StatusType getServerStatus(){
		return Database.getInstance().getServerStatus();
	}
	
	public boolean serverIsAlive(){
		return handle.serverIsConnected();
	}
}
