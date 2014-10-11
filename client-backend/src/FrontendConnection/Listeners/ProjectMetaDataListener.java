package FrontendConnection.Listeners;

import Database.Messages.ProjectMetaData;

/**
 * Created with IntelliJ IDEA.
 * User: Aitesh
 * Date: 2014-04-09
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */
public interface ProjectMetaDataListener {
    public void projectMetaDataChanged(ProjectMetaData data);
}
