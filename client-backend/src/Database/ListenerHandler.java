package Database;

import Database.Messages.ExperimentMetaData;
import Database.Messages.ProjectMetaData;
import Database.Sensors.ReadWriteLock;
import Database.Sensors.Sensor;
import FrontendConnection.Backend;
import FrontendConnection.Listeners.*;
import protobuf.Protocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Johan on 2014-04-11.
 * 
 * This class contains all listeners used. These listeners are used to notify
 * front-end about certain events, like changes in sensor status, lists being
 * updated, etc.
 */
public class ListenerHandler {

	/*
	 * The listeners are all in collections. They need to be synchronized, since
	 * otherwise they can be accessed simultaneously.
	 * 
	 * These locks are the build in locks in java.
	 */

	private Collection<SensorStatusListener> statusListeners;
	private Collection<ExperimentListChangedListener> experimentListChangedListeners;
	private Collection<ProjectListChangedListener> projectListChangedListeners;
	private Collection<ExperimentMetaDataListener> experimentMetaDataListeners;
	private Collection<ProjectMetaDataListener> projectMetaDataListeners;
	private Collection<ProjectCreationStatusListener> projectCreationStatusListeners;
	private Collection<ServerErrorMessageListener> serverErrorMessageListeners;
	private Collection<DynamicEventListener> dynamicEventListeners;
	private Collection<ResponseReceivedListener> responseReceivedListeners;
	private Collection<ServerStatusListener> serverStatusListeners;
	private Collection<ActiveProjectListener> activeProjectListeners;
	private Collection<ActiveExperimentListener> activeExperimentListeners;

	
	private ReadWriteLock sensorStatusListenerLock;
	private ReadWriteLock experimentListChangedListenersLock;
	private ReadWriteLock projectListChangedListenersLock;
	private ReadWriteLock experimentMetaDataListenersLock;
	private ReadWriteLock projectMetaDataListenersLock;
	private ReadWriteLock projectCreationStatusListenersLock;
	private ReadWriteLock serverErrorMessageListenersLock;
	private ReadWriteLock dynamicEventListenersLock;
	private ReadWriteLock responseReceivedListenersLock;
	private ReadWriteLock serverStatusListenerLock;
	private ReadWriteLock activeProjectListenerLock;
	private ReadWriteLock activeExperimentListenerLock;

	
	private List<String> dynamicEventQueue;
	private List<Sensor> statusChangedQueue;
	private List<Pair<Protocol.GeneralMsg.SubType, Enum>> responseReceivedQueue;
	private List<Object> experimentListQueue;
	private List<ExperimentMetaData> experimentMetaDataQueue;
	private List<ProjectMetaData> projectMetaDataQueue;
	private List<Protocol.CreateNewProjectResponseMsg.ResponseType> projectCreationStatusQueue;
	private List<Object> projectListQueue;
	private Protocol.StatusMsg.StatusType serverStatus;

	private String activeProject;
	private String activeExperiment;

	public ListenerHandler() {
		statusListeners = new ArrayList<SensorStatusListener>();
		experimentListChangedListeners = new ArrayList<ExperimentListChangedListener>();
		projectListChangedListeners = new ArrayList<ProjectListChangedListener>();
		projectMetaDataListeners = new ArrayList<ProjectMetaDataListener>();
		experimentMetaDataListeners = new ArrayList<ExperimentMetaDataListener>();
		projectCreationStatusListeners = new ArrayList<ProjectCreationStatusListener>();
		serverErrorMessageListeners = new ArrayList<ServerErrorMessageListener>();
		dynamicEventListeners = new ArrayList<DynamicEventListener>();
		responseReceivedListeners = new ArrayList<ResponseReceivedListener>();
		serverStatusListeners = new ArrayList<ServerStatusListener>();
		activeProjectListeners = new ArrayList<ActiveProjectListener>();
		activeExperimentListeners = new ArrayList<ActiveExperimentListener>();
		
		sensorStatusListenerLock = new ReadWriteLock();
		experimentListChangedListenersLock = new ReadWriteLock();
		projectListChangedListenersLock = new ReadWriteLock();
		experimentMetaDataListenersLock = new ReadWriteLock();
		projectMetaDataListenersLock = new ReadWriteLock();
		projectCreationStatusListenersLock = new ReadWriteLock();
		serverErrorMessageListenersLock = new ReadWriteLock();
		dynamicEventListenersLock = new ReadWriteLock();
		responseReceivedListenersLock = new ReadWriteLock();
		serverStatusListenerLock = new ReadWriteLock();
		activeProjectListenerLock = new ReadWriteLock();
		activeExperimentListenerLock = new ReadWriteLock();
		
		dynamicEventQueue = new ArrayList<String>();
		statusChangedQueue = new ArrayList<Sensor>();
		responseReceivedQueue = new ArrayList<Pair<Protocol.GeneralMsg.SubType, Enum>>();
		experimentListQueue = new ArrayList<Object>();
		experimentMetaDataQueue = new ArrayList<ExperimentMetaData>();
		projectMetaDataQueue = new ArrayList<ProjectMetaData>();
		projectCreationStatusQueue = new ArrayList<Protocol.CreateNewProjectResponseMsg.ResponseType>();
		projectListQueue = new ArrayList<Object>();

	}

	/**
	 * This function and the functions similar to the other types of listeners,
	 * tries to acquire a lock and then accesses the resource. Whatever happens,
	 * it always releases the lock, since it uses the try/finally clauses.
	 * 
	 * @param listener
	 *            is the listener to be added.
	 */

	public void addDynamicEventListener(DynamicEventListener listener) {
		try {
			while (!dynamicEventListenersLock.getLock(true))
				;
			dynamicEventListeners.add(listener);
		} finally {
			dynamicEventListenersLock.releaseLock(true);
		}
	}

	/**
	 * Adds a sensor status listener to the collection of listeners.
	 * 
	 * @param listener
	 *            is the listener to be added.
	 */

	public void addSensorStatusListener(SensorStatusListener listener) {
		try {
			while (!sensorStatusListenerLock.getLock(true))
				;
			statusListeners.add(listener);
		} finally {
			sensorStatusListenerLock.releaseLock(true);
		}
	}

	/**
	 * Adds a response received listener to the collection of listeners.
	 * 
	 * @param listener
	 *            is the listener to be added.
	 */

	public void addResponseReceivedListener(ResponseReceivedListener listener) {
		try {
			while (!responseReceivedListenersLock.getLock(true)) {
				responseReceivedListeners.add(listener);
			}
		} finally {
			responseReceivedListenersLock.releaseLock(true);
		}
	}

	/**
	 * Removes a listener from the collection of listeners.
	 * 
	 * @param listener
	 *            is the listener to be removed.
	 */

	public void removeResponseReceivedListener(ResponseReceivedListener listener) {
		try {
			while (!responseReceivedListenersLock.getLock(true))
				;
			responseReceivedListeners.remove(listener);
		} finally {
			responseReceivedListenersLock.releaseLock(true);
		}
	}

	/**
	 * Add a status listener
	 * 
	 * @param listener
	 *            a class implementing the ExperimentListChangedListener
	 *            interface
	 */
	public void addExperimentListChangedListener(
			ExperimentListChangedListener listener) {
		try {
			while (!experimentListChangedListenersLock.getLock(true))
				;
			experimentListChangedListeners.add(listener);
		} finally {
			experimentListChangedListenersLock.releaseLock(true);
		}
	}

	/**
	 * Add a status listener
	 * 
	 * @param listener
	 *            a class implementing the ProjectListChangedListener interface
	 */
	public void addProjectListChangedListener(
			ProjectListChangedListener listener) {
		try {
			while (!projectListChangedListenersLock.getLock(true))
				;
			projectListChangedListeners.add(listener);
		} finally {
			projectListChangedListenersLock.releaseLock(true);
		}
	}

	/**
	 * Removes listener sent as argument.
	 * 
	 * @param listener
	 *            is the listener to be removed.
	 */

	public void removeProjectListChangedListener(
			ProjectListChangedListener listener) {
		try {
			while (!projectCreationStatusListenersLock.getLock(true))
				;
			projectListChangedListeners.remove(listener);
		} finally {
			projectCreationStatusListenersLock.releaseLock(true);
		}
	}

	/**
	 * Notifies listeners that an error has been received from the server.
	 * 
	 * @param errorType
	 *            is what kind of error has been received.
	 */
	public void serverErrorMessageReceived(Protocol.ErrorMsg.ErrorType errorType) {
		try {
			while (!serverErrorMessageListenersLock.getLock(false))
				;
			for (ServerErrorMessageListener listener : serverErrorMessageListeners)
				listener.ServerErrorMessageReceived(errorType);
		} finally {
			serverErrorMessageListenersLock.releaseLock(false);
		}
	}

	/**
	 * Called from front-end to add a listener for server error messages.
	 * 
	 * @param listener
	 *            is the listener to be added
	 */

	public void addServerErrorMessageReceivedListener(
			ServerErrorMessageListener listener) {
		try {
			while (!serverErrorMessageListenersLock.getLock(true));
			serverErrorMessageListeners.add(listener);
		} finally {
			serverErrorMessageListenersLock.releaseLock(true);
		}
	}

	/**
	 * Called by front-end to add a listener for experiment metadata.
	 * 
	 * @param listener
	 *            is the listener to be added
	 */

	public void addExperimentMetaDataListener(
			ExperimentMetaDataListener listener) {
		try {
			while (!experimentMetaDataListenersLock.getLock(true))
				;
			experimentMetaDataListeners.add(listener);
		} finally {
			experimentListChangedListenersLock.releaseLock(true);
		}

	}

	/**
	 * Called by front-end to add a listener for project metadata.
	 * 
	 * @param listener
	 *            is the listener to be added
	 */

	public void addProjectMetaDataListener(ProjectMetaDataListener listener) {
		try {
			while (!projectMetaDataListenersLock.getLock(true))
				;
			projectMetaDataListeners.add(listener);
		} finally {
			projectMetaDataListenersLock.releaseLock(true);
		}
	}
	


	/**
	 * Called by front-end to add a listener for project creation status.
	 * 
	 * @param listener
	 *            is the listener to be added
	 */

	public void addProjectCreationStatusListener(
			ProjectCreationStatusListener listener) {
		try {
			while (!projectCreationStatusListenersLock.getLock(true))
				;
			projectCreationStatusListeners.add(listener);
		} finally {
			projectCreationStatusListenersLock.releaseLock(true);
		}
	}
	
	public void addServerStatusListener(ServerStatusListener listener){
		try {
			while (!serverStatusListenerLock.getLock(true))
				;
			serverStatusListeners.add(listener);
		} finally {
			serverStatusListenerLock.releaseLock(true);
		}
	}
	
	public void addActiveProjectListener(ActiveProjectListener listener){
		try {
			while (!activeProjectListenerLock.getLock(true))
				;
			activeProjectListeners.add(listener);
		} finally {
			activeProjectListenerLock.releaseLock(true);
		}
	}
	
	public void addActiveExperimentListener(ActiveExperimentListener listener){
		try {
			while (!activeExperimentListenerLock.getLock(true))
				;
			activeExperimentListeners.add(listener);
		} finally {
			activeExperimentListenerLock.releaseLock(true);
		}
	}

	/**
	 * Called when adding or reading data from the queue of data received.
	 * 
	 * @param queue
	 *            is the queue of data
	 * @param data
	 *            is data to add
	 * @param read
	 *            is <CODE>TRUE</CODE> if reading and <CODE>FALSE</CODE> if
	 *            writing
	 * @return a list of data read from the queue.
	 */

	private synchronized List addOrReadFromQueue(List queue, Object data,
			boolean read) {
		List returnList = null;
		if (read) {
			returnList = new ArrayList(queue);
			queue.clear();
		} else
			queue.add(data);
		return returnList;
	}

	/**
	 * Called by frontend (Through the Backend-class) to update listeners. This
	 * needed to be done since otherwise the listeners were notified across
	 * several threads, which is illegal in Android.
	 */

	public void tick() {
		
		try {
			while (!serverStatusListenerLock.getLock(false));
				for (ServerStatusListener listener : serverStatusListeners)	
					listener.serverStatus(serverStatus);
		} finally {
			projectListChangedListenersLock.releaseLock(false);
		}
		
		try {
			while (!activeProjectListenerLock.getLock(false));
				for (ActiveProjectListener listener : activeProjectListeners)	
					listener.activeProject(activeProject);
		} finally {
			projectListChangedListenersLock.releaseLock(false);
		}
		
		try {
			while (!activeExperimentListenerLock.getLock(false));
				for (ActiveExperimentListener listener : activeExperimentListeners)	
					listener.activeExperiment(activeExperiment);
		} finally {
			projectListChangedListenersLock.releaseLock(false);
		}
		
		try {
			while (!dynamicEventListenersLock.getLock(false));
				for (DynamicEventListener listener : dynamicEventListeners)
					for (String event : (List<String>) addOrReadFromQueue(
														dynamicEventQueue, null, true))
						listener.dynamicEvent(event);
		} finally {
			dynamicEventListenersLock.releaseLock(false);
		}

		try {
			while (!sensorStatusListenerLock.getLock(false))
				;
			for (SensorStatusListener listener : statusListeners)
				for (Sensor sensor : (List<Sensor>) addOrReadFromQueue(
						statusChangedQueue, null, true))
					listener.statusChanged(sensor);
		} finally {
			sensorStatusListenerLock.releaseLock(false);
		}

		try {
			while (!responseReceivedListenersLock.getLock(false)) {
				for (ResponseReceivedListener listener : responseReceivedListeners)
					for (Pair<Protocol.GeneralMsg.SubType, Enum> response : (List<Pair<Protocol.GeneralMsg.SubType, Enum>>) addOrReadFromQueue(
							responseReceivedQueue, null, true))
						listener.responseReceivedChanged(response.first,
								response.second);
			}
		} finally {
			responseReceivedListenersLock.releaseLock(false);
		}

		try {
			while (!experimentListChangedListenersLock.getLock(false))
				;
			for (ExperimentListChangedListener listener : experimentListChangedListeners) {
				for (Object o : addOrReadFromQueue(experimentListQueue, null,
						true))
					listener.experimentListChanged();
			}
		} finally {
			experimentListChangedListenersLock.releaseLock(false);
		}

		try {
			while (!experimentMetaDataListenersLock.getLock(false))
				;
			for (ExperimentMetaDataListener listener : experimentMetaDataListeners)
				for (ExperimentMetaData data : (List<ExperimentMetaData>) addOrReadFromQueue(
						experimentListQueue, null, true))
					listener.experimentMetaDataChanged(data);
		} finally {
			experimentMetaDataListenersLock.releaseLock(false);
		}

		try {
			while (!projectMetaDataListenersLock.getLock(false))
				;
			for (ProjectMetaDataListener listener : projectMetaDataListeners)
				for (ProjectMetaData data : (List<ProjectMetaData>) addOrReadFromQueue(
						projectMetaDataQueue, null, true))
					listener.projectMetaDataChanged(data);
		} finally {
			projectMetaDataListenersLock.releaseLock(false);
		}

		try {
			while (!projectCreationStatusListenersLock.getLock(false))
				;
			for (ProjectCreationStatusListener listener : projectCreationStatusListeners)
				for (Protocol.CreateNewProjectResponseMsg.ResponseType responseType : (List<Protocol.CreateNewProjectResponseMsg.ResponseType>) addOrReadFromQueue(
						projectCreationStatusQueue, null, true))
					listener.projectCreationStatusChanged(responseType);
		} finally {
			projectCreationStatusListenersLock.releaseLock(false);
		}

		try {
			while (!projectListChangedListenersLock.getLock(false))
				;
			for (ProjectListChangedListener listener : projectListChangedListeners)
				for (Object o : addOrReadFromQueue(projectListQueue, null, true))
					listener.projectListChanged();
		} finally {
			projectListChangedListenersLock.releaseLock(false);
		}
	}

	/**
	 * An event, in the form of a string, is broadcast to all listeners.
	 * 
	 * @param event
	 *            is the event being broadcast.
	 */
	public void dynamicEvent(String event) {
		addOrReadFromQueue(dynamicEventQueue, event, false);
	}

	/**
	 * Notifies listeners about a change in sensor status.
	 * 
	 * @param sensor
	 *            is the sensor whose status has been changed
	 */

	public void sensorStatusChanged(Sensor sensor) {
		addOrReadFromQueue(statusChangedQueue, sensor, false);
	}

	/**
	 * Notifies listeners that a response has been received from the server
	 * 
	 * @param type
	 * @param messageType
	 */

	public void responseReceivedChanged(Protocol.GeneralMsg.SubType type,
																Enum messageType) {
		addOrReadFromQueue(responseReceivedQueue, new Pair(type, messageType),false);
	}

	/**
	 * Standard implementation of notifying the listeners
	 */
	public void experimentListChanged() {
		addOrReadFromQueue(experimentListQueue, null, false);
	}

	public void experimentMetaDataChanged(ExperimentMetaData data) {
		addOrReadFromQueue(experimentMetaDataQueue, data, false);
	}

	public void projectMetaDataChanged(ProjectMetaData data) {
		addOrReadFromQueue(projectMetaDataQueue, data, false);
	}

	public void projectCreationStatusChanged(
			Protocol.CreateNewProjectResponseMsg.ResponseType responseType) {
		addOrReadFromQueue(projectCreationStatusQueue, responseType, false);
	}

	/**
	 * Standard implementation of notifying listeners
	 */
	public void projectListChanged() {
		addOrReadFromQueue(projectListQueue, null, false);
	}
	
	public void serverStatusChanged(Protocol.StatusMsg.StatusType status){
		serverStatus = status;
	}
	
	public void activeProject(String projectName){
		activeProject = projectName;
	}
	
	public void activeExperiment(String experimentName){
		activeExperiment = experimentName;
	}
	

}
