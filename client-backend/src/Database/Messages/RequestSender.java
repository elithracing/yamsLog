
package Database.Messages;

import Errors.BackendError;
import FrontendConnection.Backend;
import protobuf.Protocol;

/**
 * Created by Johan on 2014-04-03.
 *
 * Singleton used to sent requests to the server.
 * Some general requests are sent with requestSender.sendRequest(request) but some has to be called manually
 * since they can contain things that are not generally in a request (e.g. playbackRequest wants a list of sensorIDs)
 */

public class RequestSender {
    static private RequestSender instance = null;
    private Backend backend;
    static public RequestSender getInstance() throws BackendError {
        if (instance == null) {
            instance = new RequestSender(Backend.getInstance());
        }
        return instance;
    }
    private RequestSender(Backend backend) {
        this.backend = backend;
    }

    /**
     * Some requests need two Strings to be sent to the server; this is handled with this method.
     * @param type is the SubType to be sent to the server.
     * @param messageOne is the first String. This might be an old name for a rename request.
     * @param messageTwo is the second String. This might be a new name for a rename request.
     * @throws BackendError
     */
    public void sendRequest(Protocol.GeneralMsg.SubType type, String messageOne,String messageTwo) throws BackendError{
        switch (type){
            case RENAME_EXPERIMENT_REQUEST_T:
                backend.sendMessage(Protocol.GeneralMsg.newBuilder()
                        .setSubType(type).
                                setRenameExperimentRequest(
                                        Protocol.RenameExperimentRequestMsg.newBuilder()
                                                .setOldName(messageOne)
                                                .setNewName(messageTwo)
                                                .build()
                                ).build());
                break;
            case RENAME_PROJECT_REQUEST_T:
                backend.sendMessage(Protocol.GeneralMsg.newBuilder()
                    .setSubType(type).
                            setRenameProjectRequest(
                                    Protocol.RenameProjectRequestMsg.newBuilder()
                                            .setOldName(messageOne)
                                            .setNewName(messageTwo)
                                            .build()
                            ).build());
                break;
        }

    }

    /**
     * Generic requests with a SubType and a String.
     * This might be a request to create a new project with the name in the String message.
     * @param type is the SubType to be sent to the server.
     * @param message is a String to be sent to the server. This might be a name.
     * @throws BackendError
     */
    public void sendRequest(Protocol.GeneralMsg.SubType type, String message) throws BackendError {

        switch(type) {
            case CREATE_NEW_PROJECT_REQUEST_T:
                backend.sendMessage(Protocol.GeneralMsg.newBuilder().
                        setSubType(type).setCreateNewProjectRequest(Protocol.CreateNewProjectRequestMsg.
                        newBuilder().setName(message).build()).build());
                break;
            case REMOVE_PROJECT_REQUEST_T:
                backend.sendMessage(Protocol.GeneralMsg.newBuilder().
                        setSubType(type).setRemoveProjectRequest(Protocol.RemoveProjectRequestMsg.
                        newBuilder().setName(message).build()).build());
                break;
            case SET_ACTIVE_PROJECT_REQUEST_T:
                backend.sendMessage(Protocol.GeneralMsg.newBuilder().
                        setSubType(type).setSetActiveProjectRequest(Protocol.SetActiveProjectRequestMsg.
                        newBuilder().setName(message).build()).build());
                break;
            case EXPERIMENT_DATA_COLLECTION_START_REQUEST_T:
                backend.sendMessage(Protocol.GeneralMsg.newBuilder().
                        setSubType(type).setExperimentDataCollectionStartRequest(Protocol.ExperimentDataCollectionStartRequestMsg
                        .newBuilder().setName(message).build()).build());
                break;
            case REMOVE_EXPERIMENT_REQUEST_T:
                backend.sendMessage(Protocol.GeneralMsg.newBuilder().
                setSubType(type).setRemoveExperimentRequest(Protocol.RemoveExperimentRequestMsg
                        .newBuilder().setName(message).build()).build());
                break;
            default:
                //System.out.println("Request not found");
                //System.out.println(type);
                break;
        }
    }
}
