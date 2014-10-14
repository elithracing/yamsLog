//package FrontendConnection;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import se.liu.tddd77.bilsensor.data.Metadata;
//import se.liu.tddd77.bilsensor.data.Sensor;
//import se.liu.tddd77.bilsensor.data.SensorList;
//import se.liu.tddd77.bilsensor.list.ListElement;
//import se.liu.tddd77.bilsensor.list.SensorElement;
//
///**
// * Singleton handling communication between different parts of the application. 
// */
//public class Mediator {
//
//	private static Mediator instance;
//	
//	private ArrayList<SensorStatusListener> statusListeners = new ArrayList<SensorStatusListener>();
//	
//	private SensorList sensors = new SensorList();
//	private SensorList criticalSensors = new SensorList();
//	
//	public List<ListElement> LIST_ELEMENTS = new ArrayList<ListElement>();
//	public List<SensorElement> SENSOR_ELEMENTS = new ArrayList<SensorElement>();
//	
//	private Mediator(){
//		
//	}
//	
//	public static Mediator getInstance(){
//		if(instance == null){
//			instance = new Mediator();
//		}
//		return instance;
//	}
//	
//	
//	
//	//--------------------------------------------------
//	// Sensor status
//	//--------------------------------------------------
//	/**
//	 * When the status of a sensor has changed, notify all listeners. 
//	 * @param sensor The sensor whose status has changed. 
//	 */
//	public void statusChanged(Sensor sensor){
//		// Inform listeners
//		for(SensorStatusListener listener : statusListeners){
//			listener.statusChanged(sensor);
//		}
//		
//		// Inform fragment associated with sensor
//		for(ListElement element : LIST_ELEMENTS){
//			//TODO: Inform fragments
//		}
//	}
//	
//	/**
//	 * Add a listener to the list. 
//	 * @param listener The listener to be added to the list. 
//	 */
//	public void addStatusListener(SensorStatusListener listener){
//		statusListeners.add(listener);
//	}
//	
//	
//	
//	//--------------------------------------------------
//	// Sensor list
//	//--------------------------------------------------
//	
//	public SensorList getSensors(){
//		return sensors;
//	}
//
//	public Sensor getSensor(int index) {
//		return sensors.get(index);
//	}
//	
//	public void addSensor(Sensor sensor){
//		sensors.add(sensor);
//	}
//	
//	public void setSensors(Sensor[] sensors){
//		this.sensors.clear();
//		for(Sensor sensor : sensors){
//			this.sensors.add(sensor);
//		}
//	}
//	
//	
//	
//	public SensorList getCriticalSensors(){
//		return criticalSensors;
//	}
//
//	public Sensor getCriticalSensor(int index) {
//		return criticalSensors.get(index);
//	}
//	
//	public void addCriticalSensor(Sensor sensor){
//		criticalSensors.add(sensor);
//	}
//
//	public void removeCriticalSensor(Sensor sensor) {
//		criticalSensors.remove(sensor);
//	}
//	
//	public void setCriticalSensors(Sensor[] sensors){
//		this.criticalSensors.clear();
//		for(Sensor sensor : sensors){
//			this.criticalSensors.add(sensor);
//		}
//	}
//	
//	/**
//	 * Update the list of critical sensors using an array of booleans. 
//	 * Iterate through the array: if an element is true, the sensor with the 
//	 * same index should be added to the list of critical sensors. 
//	 * @param selected The array of booleans to compare with. 
//	 */
//	public void updateCriticalSensorList(boolean[] selected) {
//		if(selected.length != sensors.size()){
//			//TODO: Throw error?
//		}
//		else{
//			for(int i = 0; i < selected.length; i++){
//				if(selected[i]){
//					criticalSensors.add(sensors.get(i));
//				}
//			}
//		}
//	}
//
//	public void clearCriticalSensors() {
//		criticalSensors.clear();
//	}
//	
//	
//	
//	//--------------------------------------------------
//	// ListElement list
//	//--------------------------------------------------
//	
//	
//	
//	//TODO: Create an actual implementation. 
//	public Metadata getMetadata(){
//		return new Metadata("Experiment", new String[]{"Peron 1", "Person 2", "Person 3", "Person 4"}, 2);
//	}
//	
//}
