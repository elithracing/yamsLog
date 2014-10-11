package FrontendConnection.Listeners;

import protobuf.Protocol;

/**
 * Created by Tony on 2014-07-08.
 */
public interface ServerStatusListener {
	public void serverStatus(Protocol.StatusMsg.StatusType status);
}
