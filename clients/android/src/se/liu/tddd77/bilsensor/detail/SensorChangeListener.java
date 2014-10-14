package se.liu.tddd77.bilsensor.detail;

import java.util.ArrayList;

import Database.Sensors.Sensor;

//TODO: Move and add to class


public interface SensorChangeListener{
	public void xChanged(int index);
	public void yAdded(int index);
	public void yRemoved(int index);
	public void yChanged(ArrayList<Integer> index);
	//public void sensorChanged(Sensor sensor, int x, int[] y);
	public void sensorChanged(ArrayList<Sensor> sensor, int x, int[] y);
}
