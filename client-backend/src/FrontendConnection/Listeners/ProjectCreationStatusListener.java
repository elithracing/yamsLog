package FrontendConnection.Listeners;

import protobuf.Protocol;

/**
 * Created by Johan on 2014-04-09.
 */
public interface ProjectCreationStatusListener {
    public void projectCreationStatusChanged(Protocol.CreateNewProjectResponseMsg.ResponseType responseType);
}
