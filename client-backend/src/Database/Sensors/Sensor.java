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
