package FrontendConnection.Listeners;

import java.util.ArrayList;

import protobuf.Protocol;

/**
 * Created by tony on 2014-07-01.
 */
public interface UpdatedFrontendDataListener {
	public void updatedSensorName(String name, int id);
	public void updatedAttributeNames(String[] names, int id);
	public void updatedFrontendList(ArrayList<ArrayList<Float>> dataList, int id);
	public void updatedCurrentAttributeStatus(Protocol.SensorStatusMsg.AttributeStatusType[] statuses, int id);
}
