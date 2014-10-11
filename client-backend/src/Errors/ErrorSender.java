package Errors;

import FrontendConnection.Backend;
import protobuf.Protocol;

/**
 * Created with IntelliJ IDEA.
 * User: Aitesh
 * Date: 2014-03-27
 * Time: 11:17
 * To change this template use File | Settings | File Templates.
 */
public class ErrorSender {
    static private ErrorSender instance = null;
    private Backend backend;
    static public ErrorSender getInstance() throws BackendError{
        if(instance==null){
            instance = new ErrorSender(Backend.getInstance());
        }
        return instance;
    }

    private ErrorSender(Backend backend){
        this.backend = backend;
    }


    public void sendError(Protocol.ErrorMsg.ErrorType type) throws BackendError{
        backend.sendMessage(Protocol.GeneralMsg.newBuilder().
                setSubType(Protocol.GeneralMsg.SubType.ERROR_T).
                setErrorMessage(Protocol.ErrorMsg.newBuilder().
                        setErrorType(type).build()).build());
    }
}
