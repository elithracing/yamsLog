package FrontendConnection.Listeners;

import Database.Messages.ExperimentMetaData;

/**
 * Created with IntelliJ IDEA.
 * User: Aitesh
 * Date: 2014-04-09
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */
public interface ExperimentMetaDataListener {
    public void experimentMetaDataChanged(ExperimentMetaData data);
}
