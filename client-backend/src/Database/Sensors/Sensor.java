package Database.Sensors;

import Errors.BackendError;
import Errors.StatusNotReceivedError;
import FrontendConnection.Listeners.UpdatedFrontendDataListener;
import protobuf.Protocol;


/**
 * Created with IntelliJ IDEA.
 * User: Aitesh
 * Date: 2014-02-26
 * Time: 10:36
 * To change this template use File | Settings | File Templates.
 */
public interface Sensor {
	
	public void addData(Protocol.GeneralMsg generalMsg) throws Exception;
	/*
	 * Setters
	 */
    public void setId(int id);
    public void setSensorName(String string);
    public void setAttributesName(String[] string);
    public void setAttributesCount(int value);
    public void setSensorStatus(Protocol.SensorStatusMsg.Sensor sensorStatus) throws StatusNotReceivedError;
    public void setFilterConstants(int fullResLimit, int filterLimit);
    /*
     * Getters
     */
    public int getId();
    public String getSensorName();
    public Protocol.SensorStatusMsg.SensorStatusType getSensorStatus();
    public String[] getAttributesName();
    public int getAttributesCount();
    public Protocol.SensorStatusMsg.AttributeStatusType[] getAttributeStatuses(); 
    /*
     * Listener functions
     */
    public void updateAllSharedFrontendData();
    public void setFrontendDataChangedListener(UpdatedFrontendDataListener frontendListListener);
    public void resetData();
}
