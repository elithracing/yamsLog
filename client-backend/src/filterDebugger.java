import Database.Sensors.GenericMessage;
import Errors.BackendError;
import FrontendConnection.Backend;
import FrontendConnection.Listeners.ProjectCreationStatusListener;
import FrontendConnection.Listeners.UpdatedFrontendDataListener;
import protobuf.Protocol;
import protobuf.Protocol.GeneralMsg;
import protobuf.Protocol.SensorStatusMsg.AttributeStatusType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by Niklas on 2014-07-01.
 */


public class filterDebugger implements ProjectCreationStatusListener, UpdatedFrontendDataListener {

    ArrayList<ArrayList<Float>> dataList;
    GenericMessage sensor;
    Integer counter = 0;
    static int addedData = 0;
    private ReentrantLock readingLock;
    
    filterDebugger(){
        try {
        	readingLock = new ReentrantLock();;
            Backend.createInstance(null);
            Thread.sleep(3);
            Backend.getInstance().connectToServer("localhost", 2001);
            Thread.sleep(15);
            Backend.getInstance().addProjectCreationStatusListener(this);
            Backend.getInstance().sendSettingsRequestMessageSelected(5, Arrays.asList(1));
            Thread.sleep(3);
            sensor = (GenericMessage)Backend.getInstance().getSensorById(1);
            sensor.setFrontendDataChangedListener(this);
            sensor.updateAllSharedFrontendData();
        } catch (BackendError backendError) {
            backendError.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void test() throws BackendError{
    	Thread addDataThread = new Thread(new AddDataRunnable());
    	Thread queueThread = new Thread(new HandleQueueRunnable());
    	Thread readDataThread = new Thread(new ReadDataRunnable());
    	addDataThread.start();
    	queueThread.start();
    	try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	readDataThread.start();
    	System.out.println("Strted Threads");
    }

    public void printLista(ArrayList<Number> list){
        for(int i = 0; list.size() > i; i++)
        System.out.println(list.get(i));
    }

    public void printList(List<ArrayList<Float>> list){
    	for(int i = 0; list.size() > i;){
    		String str = "";
    		for(int j = 0;j < list.get(i).size(); j++){
    			str += list.get(i).get(j) + " ";
    		}
    		System.out.println(str);
		  break;
    	}
    }

    public void risingData(int size, ArrayList<Number> tmpFloat){
        for(Integer i = 0; i < size; i++){
            tmpFloat.add(i.floatValue());
        }
    }
	
	/*
	 *
	 * THREADS
	 *
	 */
	
	
	public class AddDataRunnable implements Runnable {
        public void run(){
        	while(true){
        		protobuf.Protocol.DataMsg.Builder dataMsgBuilder = protobuf.Protocol.DataMsg.newBuilder();
	        	dataMsgBuilder.setTime(counter.doubleValue());
	        	dataMsgBuilder.setTypeId(1);
	        	for(int i = 0; i < 9; i++){
	        		dataMsgBuilder.addData(counter.floatValue());
	        	}
	        	
	            try {
					sensor.addData(Protocol.GeneralMsg.newBuilder().setSubType(GeneralMsg.SubType.DATA_T)
					        .setData(dataMsgBuilder.build()).build());
					addedData++;
				} catch (Exception e) {
					e.printStackTrace();
				}
	        	counter++;
	        	try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
        }
      }
    
    public class ReadDataRunnable implements Runnable {

		@Override
		public void run() {
			while(true){
				readingLock.lock();
				printList(dataList);
				readingLock.unlock();
				try {
					Thread.sleep(60);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
    }
    
    public class HandleQueueRunnable implements Runnable {
		@Override
		public void run() {
			while(true){
				try {
					Backend.getInstance().handleQueue(1);
					addedData = 0;
					Thread.sleep(1);
				} catch (BackendError e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
    }
    
    /*
     * 
     * Implemented functions
     * 
     */
    
    @Override
    public void projectCreationStatusChanged(Protocol.CreateNewProjectResponseMsg.ResponseType responseType) {

    }

	@Override
	public void updatedFrontendList(ArrayList<ArrayList<Float>> dataList, int id) {
		readingLock.lock();
		this.dataList =  dataList;
		readingLock.unlock();
	}
	

	@Override
	public void updatedSensorName(String name, int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updatedAttributeNames(String[] names, int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updatedCurrentAttributeStatus(AttributeStatusType[] statuses,
			int id) {
		// TODO Auto-generated method stub
		
	}

}


