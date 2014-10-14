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
