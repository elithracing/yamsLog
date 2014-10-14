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

package se.liu.tddd77.bilsensor.graphs;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

import android.R.bool;
import android.util.Log;

import protobuf.Protocol.SensorStatusMsg.AttributeStatusType;
import protobuf.Protocol.SensorStatusMsg.SensorStatusType;
import Errors.BackendError;
import FrontendConnection.Backend;
import FrontendConnection.Listeners.UpdatedFrontendDataListener;


public class SensorDataHolder implements UpdatedFrontendDataListener{
	private ArrayList<ArrayList<Float>> tmpList = null;
	private static HashMap<Integer, ArrayList<ArrayList<Float>>> allNumericalData = new HashMap<Integer, ArrayList<ArrayList<Float>>>();
	private static HashMap<Integer, SensorStatusType> sensorStatusMap = new HashMap<Integer, SensorStatusType>();
	private static List<Integer> sensorIds = new ArrayList<Integer>();;
	private static SensorDataHolder instance = null;
	private static boolean connectedToServer = true;
	public Semaphore readWriteSema = new Semaphore(1);
	//private static UpdatedFrontendDataListener listener;

	public Semaphore getReadWriteSema() {
		return readWriteSema;
	}

	public SensorDataHolder(){

		try {
			instance = this;
			Backend.getInstance().setUpdatedFrontendDataListener(this);
		} catch (BackendError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static SensorDataHolder getInstance(){
		if(instance == null){
			instance = new SensorDataHolder();
			return instance;
		}
		return instance;
	}
	
	public boolean hasSensor(int sensorId){
		return allNumericalData.containsKey(sensorId);
	}
	
	

	public ArrayList<ArrayList<Float>> getDataForDrawing(int sensorId, ArrayList<Integer> attributes){
		
		//if(!attributes.isEmpty()) Log.d("Linegraph1", "getData!");
		tmpList = new ArrayList<ArrayList<Float>>();
		if(!allNumericalData.containsKey(sensorId)){
			return tmpList;
		}
		//Log.d("LineGraph1", "Adding data to sensor." + sensorId);
		for(int j = 0; j< attributes.size(); j++){
			tmpList.add(allNumericalData.get(sensorId).get(attributes.get(j))); 
		}
		
		//Log.d("LineGraph12", "tmpList.size() = " + Integer.toString(tmpList.size()));
		//for(int i = 0; i < tmpList.size(); i++)Log.d("LineGraph12", "tmpList.size() = " + Integer.toString(tmpList.get(i).size()));

		return tmpList;
	}
	
	public ArrayList<List<Float>> getLatestValues() {
		//Log.d("LineGraph1", "getLatestValues.");
		ArrayList<List<Float>>returnList = new ArrayList<List<Float>>();
		//List<Float> tmpList = new ArrayList<Float>();
		//for(int i=0; sensorIds.size() > i; i++){
		returnList.clear();

		returnList.add(new ArrayList<Float>());
		try {
			for(int sensorId : Backend.getInstance().getSensorIds()){
				List<Float> tmpList = new ArrayList<Float>();
				returnList.get(0).add(Float.valueOf(sensorId)); //0 is the place for the sensor it's regarding.
				ArrayList<ArrayList<Float>> sensorData = allNumericalData.get(sensorId);
				for(int j = 0; j < sensorData.size() ; j++){

					ArrayList<Float> smallTmpList = sensorData.get(j); //arraylist floats
					if(!smallTmpList.isEmpty())
					tmpList.add(smallTmpList.get(smallTmpList.size()-1));
				}

				returnList.add(tmpList);
				//tmpList.clear();
			}
			
		} catch (BackendError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		//if(returnList.isEmpty()) Log.d("LineGraph1", "returnList is empty."); else Log.d("LineGraph1", "returnList NOT EMPTY");
		return returnList;
	}
	
	
	@Override
	public void updatedFrontendList(ArrayList<ArrayList<Float>> sensorData, int sensorId) {
		if(!sensorData.isEmpty())Log.d("LineGraph12", "updatedFEList.");
		
		Integer sId = Integer.valueOf(sensorId);
		try {
			readWriteSema.acquire();
		} catch (InterruptedException e) {
			//readWriteSema.release();
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		if(!allNumericalData.containsKey(sId)){
			sensorIds.add(sensorId);
			allNumericalData.put(sId, sensorData);
		}else{

			allNumericalData.remove(sId);
			allNumericalData.put(sId, sensorData);
		}
		readWriteSema.release();
		// TODO Auto-generated method stub	
	}
	
	String tmpString = new String();
	public String getAttributeName(int attributeId, int sensorId){
		tmpString = attributeNameData.get(sensorId)[attributeId];
		return tmpString;
	}
	
	HashMap<Integer, String[]> attributeNameData = new HashMap<Integer, String[]>();
	@Override
	public void updatedAttributeNames(String[] attributeNames, int sensorId) {
		if(attributeNameData.containsKey(sensorId)) attributeNameData.remove(sensorId);
		attributeNameData.put(sensorId, attributeNames);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updatedCurrentAttributeStatus(AttributeStatusType[] statusType,
			int sensorId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updatedSensorName(String newName, int sensorId) {
		// TODO Auto-generated method stub
		
	}

	public void setSensorWorking(Integer key, SensorStatusType status) {
		Log.d("statusmap", "setting working " + status + " key: " + key);
				sensorStatusMap.put(key, status);		
	}
	
	public SensorStatusType getSensorWorking(Integer key){
			Log.d("statusmap", "Found value: "+ sensorStatusMap.get(key) );
			return sensorStatusMap.get(key);
	}

	public void setConnectedToServer(boolean b) {
		// TODO Auto-generated method stub
		connectedToServer = b;
	}
	
	public boolean getConnectedToServer() {
		// TODO Auto-generated method stub
		return connectedToServer;
	}


}