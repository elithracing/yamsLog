package ServerConnection;

import Database.Database;
import Database.MessageBuilder;
import Errors.BackendError;
import Errors.ConnectionError;
import Errors.ThreadDoesNotExistError;
import FrontendConnection.Backend;
import protobuf.Protocol;
import protobuf.Protocol.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
/**
 * Handle for connection with the specified server.
 */

public class Handle {
    //private String hostName;
    //private int portNumber;
    private Database database;
    private Thread listenerThread;
    private Socket serverSocket;

    public Handle(){
        this.database = Database.getInstance();
    }

    /**
     * Connects the handle to the specified server
     * The timeoutvalue for the connection is the second argument to serverSocket.connect(..,timeout)
     * it is set to 4000 milliseconds
     * @throws IOException if handle does not get a valid connection to the server
     */
    public void connectToServer(String hostName, int portNumber,Thread.UncaughtExceptionHandler eh) throws BackendError{
        //this.hostName = hostName;
        // this.portNumber = portNumber;
        serverSocket = new Socket();
        InetSocketAddress socketAddress = new InetSocketAddress(hostName, portNumber);
        try {
            serverSocket.connect(socketAddress, 4000);
        } catch (ConnectException e) {
            throw new ConnectionError("Connection refused.");
        } catch (IOException e) {
            e.printStackTrace();
            throw new ConnectionError("IOException during attempt to connect.");
        }
        try {
            startServerListener(eh);
        } catch (IOException e) {
            throw new ConnectionError("Inputstream could not be created.");
        }

    }

    public void startDataCollection(String fileName)throws BackendError{
        //Creates a new message of type StartDataCollection and sets the string field in i to fileName.
        sendMessage(GeneralMsg.newBuilder().setSubType(GeneralMsg.SubType.EXPERIMENT_DATA_COLLECTION_START_REQUEST_T)
                .setExperimentDataCollectionStartRequest(ExperimentDataCollectionStartRequestMsg
                        .newBuilder().setName(fileName).build()).build());

    }

    public void stopDataCollection() throws BackendError{
        //Creates a new message of type StopDataCollection
        sendMessage(GeneralMsg.newBuilder().setSubType(GeneralMsg.SubType.EXPERIMENT_DATA_COLLECTION_STOP_REQUEST_T)
                .setExperimentDataCollectionStopRequest(ExperimentDataCollectionStopRequestMsg
                        .newBuilder().build()).build());
    }

    /**
     * closes the open socket
     * @throws IOException
     */
    public void closeSocket() throws IOException{
        serverSocket.close();
    }

    public Database getDatabase(){
        return database;
    }

    /**
     * May only be used in production
     * Sends a debug message with the current system time to the connected server.
     */
    public void debugMessage() throws BackendError{
        sendMessage(GeneralMsg.newBuilder()
                .setSubType(GeneralMsg.SubType.DEBUG_T).setDebugMessage(DebugMsg.newBuilder()
                        .setDebugMessage("det här är en sträng").build()).build());
    }


    public void debugger(String debugMSG) throws BackendError{
         sendMessage(GeneralMsg.newBuilder()
              .setSubType(GeneralMsg.SubType.DEBUG_T).setDebugMessage(DebugMsg.newBuilder()
                 .setDebugMessage("debugger sends string " + debugMSG).build()).build());
        }
    /**
     * Sends the message that is provided to the specified server
     * @param message to send to the server.
     */
    public synchronized void sendMessage(GeneralMsg message) throws BackendError{
        try{
            //Thread.sleep(message.getSerializedSize());
            //System.out.println(message.getSerializedSize());
            //Possible solution to the problem of too much data
            message.writeDelimitedTo(serverSocket.getOutputStream());
           // System.out.println("Sending " + message);
        } catch (Exception e){
            //This might appear when the client tries to sent too much data in a short period.
            throw new ConnectionError("Could not connect to server in attempt to write " + message);
        }
    }

    public void sendDynamicMessage(Double time, String message) throws BackendError {
        DynamicEventStruct.Builder structBuilder = DynamicEventStruct.newBuilder();

        sendMessage(Protocol.GeneralMsg.newBuilder().setSubType(GeneralMsg.SubType.SET_DYNAMIC_EVENT_REQUEST_T)
                .setSetDynamicEventRequest(SetDynamicEventRequestMsg.newBuilder()
                        .setDynamicEvent(structBuilder.setMessage(message).setTime(time).build())).build());
    }


    public void startServerListener(Thread.UncaughtExceptionHandler eh) throws IOException{
        if(listenerThread!=null){
           // System.out.println(listenerThread.getState());
        }
        listenerThread = new ThreadedListener(serverSocket.getInputStream(), new MessageBuilder());
        listenerThread.setUncaughtExceptionHandler(eh);
        listenerThread.start();
    }


    public void restartServerListener() throws BackendError{
        if(listenerThread==null) throw new ThreadDoesNotExistError();
        listenerThread.run();
    }

    public void stopConnection() throws BackendError{
        listenerThread.interrupt();
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new ConnectionError();
        }
    }
    
    public boolean serverIsConnected(){
    	try {
    		//TODO: Implement a new heartbeat meassage in the protobuf protocol
			Backend.getInstance().debugger("Heartbeat");
		} catch (BackendError e) {
			return false;
		}
    	return true;
    }
}



