package FrontendConnection.Listeners;

import protobuf.Protocol;

/**
 * Created by Johan on 2014-04-11.
 */
public interface ServerErrorMessageListener {
    public void ServerErrorMessageReceived(Protocol.ErrorMsg.ErrorType errorType);
}
