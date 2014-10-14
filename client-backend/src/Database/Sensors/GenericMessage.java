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

import Database.Database;
import Errors.StatusNotReceivedError;
import FrontendConnection.Listeners.SensorStatusListener;
import FrontendConnection.Listeners.UpdatedFrontendDataListener;
import protobuf.Protocol;
import protobuf.Protocol.SensorStatusMsg.AttributeStatusType;
import protobuf.Protocol.SensorStatusMsg.SensorStatusType;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;



/**
 * Created with IntelliJ IDEA.
 * User: Aitesh
 * Date: 2014-03-24
 * Time: 10:25
 * To change this template use File | Settings | File Templates.
 *
 * This class is the equivalent to a sensorobject.
 */
public class GenericMessage implements Sensor {

    private ArrayList<ArrayList<Float>> dataList;
    private ArrayList<ArrayList<Float>> clonedDataList;
    private ArrayList<ArrayList<Float>> frontendDataList;
    UpdatedFrontendDataListener frontendDataListener;
    
    private ReentrantLock listsLock;
    private ReentrantLock mainQueueLock;
    private ReentrantLock missedQueueLock;
    private ReentrantLock filterStatusLock;

    private LinkedBlockingQueue<Protocol.DataMsg> dataMsgQueue;
    private LinkedBlockingQueue<Protocol.DataMsg> missedDataMsgQueue;

    private Integer sensorId;
    private String[] attributeNames = null;
    private String sensorName = null;
    private Protocol.SensorStatusMsg.SensorStatusType sensorStatus;
    private Protocol.SensorStatusMsg.AttributeStatusType[] attributeStatuses;
    private int addedElementsSinceLastFilter;
    private int attributesCount;
	private UpdatedFrontendDataListener frontendListListener;
	private boolean filtering = false;
    private static int FILTER_LIMIT = 	2000;
    private static int FULL_RES_LIMIT = 	1000;
    private static final int TIME_ID_INDEX = 0;

    public GenericMessage(int id, Database database){
        this.sensorId = id;
        
//        sensorStatus = null;
        attributeStatuses = null;
        this.dataList = new ArrayList<ArrayList<Float>>();
        this.frontendDataList = new ArrayList<ArrayList<Float>>();
        
        this.dataMsgQueue = new LinkedBlockingQueue<Protocol.DataMsg>();
        this.missedDataMsgQueue = new LinkedBlockingQueue<Protocol.DataMsg>();

        this.listsLock = new ReentrantLock();
        this.mainQueueLock = new ReentrantLock();
        this.filterStatusLock = new ReentrantLock();
        this.missedQueueLock = new ReentrantLock();
    	sensorStatus = Protocol.SensorStatusMsg.SensorStatusType.NOT_WORKING;
    }
    
    @Override
    public void addData(Protocol.GeneralMsg generalMsg) throws Exception{
    	mainQueueLock.lock();
    	dataMsgQueue.put(generalMsg.getData());
    	mainQueueLock.unlock();
    }

    public void handleQueue()throws Exception{
		while(!dataMsgQueue.isEmpty()){
        	//Lock main queue to ensure mutual exclusion
        	mainQueueLock.lock();
            Protocol.DataMsg msg = dataMsgQueue.poll();
            mainQueueLock.unlock();
            
            //Insert all data points into data lists
            filterStatusLock.lock();
            if(!filtering){
            	listsLock.lock();


            	for (int j = 0; j < msg.getDataCount(); j++) {
            		dataList.get(j+1).add(new Float(msg.getData(j)));
					frontendDataList.get(j+1).add(new Float(msg.getData(j)));
				}            	
            	
            	frontendDataList.get(TIME_ID_INDEX).add(new Float(msg.getTime()));
            	dataList.get(TIME_ID_INDEX).add(new Float(msg.getTime()));
            	listsLock.unlock();
             	addedElementsSinceLastFilter++;
            }else{
                missedQueueLock.lock();
				missedDataMsgQueue.put(msg);
                missedQueueLock.unlock();
                listsLock.lock();
            	for (int j = 0; j < msg.getDataCount(); j++) {
					frontendDataList.get(j+1).add(new Float(msg.getData(j)));
				}
            	frontendDataList.get(TIME_ID_INDEX).add(new Float(msg.getTime()));
            	listsLock.unlock();
            	
    		}
    		filterStatusLock.unlock();
		}
		if(addedElementsSinceLastFilter > FILTER_LIMIT){
			filterStatusLock.lock();
			if(!filtering){
		    	filtering = true;
		    	filterStatusLock.unlock();
		    	addedElementsSinceLastFilter = 0;
		        Thread filterDataThread = new Thread(new filterDataRunnable());
		        filterDataThread.start();
		        return;
			}
			filterStatusLock.unlock();
		}
    }
    
    private class filterDataRunnable implements Runnable {
    	//Runnable for filter data thread
        public void run(){
        	System.out.println("BACKEND: Starting filter thread " + sensorName);
        	threadFilterData();
        }
      }
    
    private void threadFilterData(){
    	//Thread function for filter data
    	
    	
    	clonedDataList = new ArrayList<ArrayList<Float>>();
    	for(int i = 0; i < dataList.size(); i++){
    		//Filter data in dataList
    		for(int j = 1; j < (dataList.get(i).size() - FULL_RES_LIMIT); j+=2){
				//int j = 1 because you don't want to delete first element
				dataList.get(i).remove(j);
			}
    		//Copy data in dataList to clonedDataList
			clonedDataList.add(new ArrayList<Float>(dataList.get(i)));
    	}
    	
    	//Append missed elements that have been added to newestDataList
    	missedQueueLock.lock();
    	while(!missedDataMsgQueue.isEmpty()){
			Protocol.DataMsg msg = missedDataMsgQueue.poll();
			
			for (int j = 0; j < msg.getDataCount(); j++) {
				clonedDataList.get(j+1).add(new Float(msg.getData(j)));
				dataList.get(j+1).add(new Float(msg.getData(j)));
	        }
			clonedDataList.get(TIME_ID_INDEX).add(new Float(msg.getTime()));
			dataList.get(TIME_ID_INDEX).add(new Float(msg.getTime()));
		}
    	missedQueueLock.unlock();
    	
    	//Update references to the new filtered lists and notify frontend
    	listsLock.lock();
    	frontendDataList = clonedDataList;
    	//clonedDataList = null;
    	updateFrontendList(frontendDataList);
    	listsLock.unlock();
    	
    	//Update filtering status
    	filterStatusLock.lock();
    	filtering = false;
    	filterStatusLock.unlock();
    	System.out.println("BACKEND: Filtering done for " + sensorName);
    }

    public void setFrontendDataChangedListener(UpdatedFrontendDataListener frontendListListener){
        //This check might be unnecessary 
    	if(this.frontendListListener != frontendListListener)
        	this.frontendDataListener = frontendListListener;
        updateAllSharedFrontendData();
    }
    
    public void updateFrontendList(ArrayList<ArrayList<Float>> updatedFrontendList){
    	frontendDataListener.updatedFrontendList(updatedFrontendList, sensorId);
    }
    
    public void updateAllSharedFrontendData(){
    	if(sensorName != null)
    		frontendDataListener.updatedSensorName(sensorName, sensorId);
    	if(attributeNames != null)
    		frontendDataListener.updatedAttributeNames(attributeNames, sensorId);
    	if(frontendDataList != null)
    		frontendDataListener.updatedFrontendList(frontendDataList, sensorId);
    	if(attributeStatuses != null)
    		frontendDataListener.updatedCurrentAttributeStatus(attributeStatuses, sensorId);
    }    

    @Override
    public void setId(int id) {
        this.sensorId = id;
    }

    @Override
    public void setSensorName(String name) {
        this.sensorName = name;
        if(frontendDataListener != null)
        	frontendDataListener.updatedSensorName(name, sensorId);
    }
    



    @Override
    public void setAttributesName(String[] string) {
        this.attributeNames = string;
        if(frontendDataListener != null)
        	frontendDataListener.updatedAttributeNames(string, sensorId);
    }
    



    public void setAttributesCount(int attributesCount) {
    	//+1 for time attribute which is not counted when called
    	this.attributesCount = attributesCount +1;
        attributeStatuses = new Protocol.SensorStatusMsg.AttributeStatusType[this.attributesCount];
        //i < value +1 to account for the time list as well
        for(int i = 0; i < this.attributesCount; i++){
            attributeStatuses[i] = Protocol.SensorStatusMsg.AttributeStatusType.INSIDE_LIMITS;
            dataList.add(new ArrayList<Float>());
            frontendDataList.add(new ArrayList<Float>());       	
        }
    }
    
    @Override
    public void setSensorStatus(Protocol.SensorStatusMsg.Sensor sensorStatus) throws StatusNotReceivedError {
        this.sensorStatus = sensorStatus.getStatus();
    	//readWriteSensorStatus(sensorStatus.getStatus());
        readWriteAttributeStatus(sensorStatus);
    }
    
    @Override
    public void setFilterConstants(int fullResLimit, int filterLimit){
    	FULL_RES_LIMIT = fullResLimit;
    	FILTER_LIMIT = filterLimit;
    }
    
    private synchronized Protocol.SensorStatusMsg.SensorStatusType readWriteSensorStatus (Protocol.SensorStatusMsg.SensorStatusType status) throws StatusNotReceivedError {
        if (status!=null) sensorStatus = status;
        if (sensorStatus == null) throw new StatusNotReceivedError();
        return sensorStatus;
    }

    private synchronized Protocol.SensorStatusMsg.AttributeStatusType[] readWriteAttributeStatus (Protocol.SensorStatusMsg.Sensor status) throws StatusNotReceivedError {
        if (status!=null) {
        	if (attributeStatuses == null) throw new StatusNotReceivedError();
        	attributeStatuses[TIME_ID_INDEX] = Protocol.SensorStatusMsg.AttributeStatusType.INSIDE_LIMITS;
            for (int i = 1; i < status.getAttributesCount(); i++) {
                attributeStatuses[status.getAttributes(i).getIndex()+1] = status.getAttributes(i).getStatus();
            }
            if(frontendDataListener != null)
            	frontendDataListener.updatedCurrentAttributeStatus(attributeStatuses, sensorId);
        }
        return attributeStatuses;
    }    

	@Override
	public String getSensorName() {
		return sensorName;
	}

	@Override
	public String[] getAttributesName() {
		return attributeNames;
	}

	@Override
	public int getAttributesCount() {
		return attributesCount;
	}

	@Override
	public AttributeStatusType[] getAttributeStatuses() {
		return attributeStatuses;
	}

	@Override
	public SensorStatusType getSensorStatus() {
		return sensorStatus;
	}
	
	@Override
	public int getId(){
		return sensorId;
	}
	

	public void resetData(){
		
		listsLock.lock();
		if(dataList != null)
			dataList.clear();
		if(frontendDataList != null)
			frontendDataList.clear();
		if(clonedDataList != null)
			clonedDataList.clear();
		listsLock.unlock();
		
		mainQueueLock.lock();
		if(dataMsgQueue != null)
			dataMsgQueue.clear();
		mainQueueLock.unlock();
		
		filterStatusLock.lock();
		addedElementsSinceLastFilter = 0;
		filterStatusLock.unlock();
		
        for(int i = 0; i < attributesCount; i++){
            attributeStatuses[i] = Protocol.SensorStatusMsg.AttributeStatusType.INSIDE_LIMITS;
            dataList.add(new ArrayList<Float>());
            frontendDataList.add(new ArrayList<Float>());       	
        }
        if(frontendDataList != null && frontendDataListener != null){
        	frontendDataListener.updatedFrontendList(frontendDataList, sensorId);
        }
	}



}



