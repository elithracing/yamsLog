package FrontendConnection.Listeners;

import protobuf.Protocol;

/**
 * Created by Johan on 2014-04-15.
 *
 * This listener is used as a general listener for some responses from the server.
 */
public interface ResponseReceivedListener {
    public void responseReceivedChanged(Protocol.GeneralMsg.SubType type, Enum messageType);
}
