package se.liu.tddd77.bilsensor.list;

import java.util.ArrayList;
import java.util.List;

import se.liu.tddd77.bilsensor.SensorDetailFragment;
import Database.Sensors.Sensor;
import Errors.BackendError;
import FrontendConnection.Backend;
import android.app.Fragment;
import android.util.Log;

//TODO: Store these in ListAdapter, or does that break MVC too much?
@SuppressWarnings("rawtypes")
public class ElementList {

	private static ElementList instance;
	
	public List<ListElement<? extends Fragment>> elementList = new ArrayList<ListElement<?extends Fragment>>();
	
	
	
	private ElementList(){
		elementList.add(new StatusListElement("Status"));
	}
	
	public void resetList(){
		this.elementList.clear();
		this.elementList.add(new StatusListElement("Status"));
	}
	
	public ListElement getElement(int index){
		return elementList.get(index);
	}
	
	public static ElementList getInstance(){
		if(instance == null){
			instance = new ElementList();
		}
		return instance;
	}

	@Deprecated
	public void addSensor(int i) {
		try {
			Log.i("ElementList","addSensor, really sketchy stuff in this method."); //TODO: Check this out, is it ever used, is it stupid?
			Sensor sensor = Backend.getInstance().getSensor(i);
			SensorElement element = new SensorElement(sensor.getSensorName());
			element.fragment = new SensorDetailFragment();
//			element.fragment.sensor = sensor;
			elementList.add(element);
			
		} catch (BackendError e) {
			// TODO Here or throw and handle upstream?
			e.printStackTrace();
		}
	}
	
	
	
	//TODO: Don't think I want to return elements like this
	public List<ListElement<? extends Fragment>> getElements() {
		return elementList;
	}
	
}
