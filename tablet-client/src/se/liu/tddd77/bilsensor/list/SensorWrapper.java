package se.liu.tddd77.bilsensor.list;

import se.liu.tddd77.bilsensor.SensorDetailFragment;
import Database.Sensors.Sensor;

/**
 * Default implementation of sensor element, with the default sensor fragment 
 * as the fragment. 
 */
public class SensorWrapper extends SensorElement{

	
	public SensorWrapper(Sensor sensor){
		this(sensor.getSensorName());
//		((SensorFragment) super.fragment).setSensor(sensor);
	}
	
	public SensorWrapper(String name){
		super(name);
		super.fragment = new SensorDetailFragment();
	}
	
}
