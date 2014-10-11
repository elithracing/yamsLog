package FrontendConnection.Listeners;

import Database.Sensors.Sensor;

/**
 * Interface for the classes interested in changes in the existing sensors statuses.
 */
public interface SensorStatusListener {
	public void statusChanged(Sensor sensor);

}
