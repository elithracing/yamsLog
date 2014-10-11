package Database.Messages;

import protobuf.Protocol;

import java.util.List;

/**
 * Created by Johan on 2014-04-02.
 */
public class ExperimentMetaData implements Message {

    private String experimentName;
    private String experimentDescription;
    private List<String> tags;

    public ExperimentMetaData() {
        this.experimentName = null;
        this.experimentDescription = null;
        this.tags = null;
    }

    public ExperimentMetaData(Protocol.ExperimentMetadataMsg data) {
        //this.experimentName = data.getMetadata().getSensorName();
        this.experimentDescription = data.getMetadata().getNotes();
        this.tags = data.getMetadata().getTagsList();
    }

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public String getExperimentDescription() {
        return experimentDescription;
    }

    public void setExperimentDescription(String experimentDescription) {
        this.experimentDescription = experimentDescription;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "ExperimentMetaData{" +
                "experimentName='" + experimentName + '\'' +
                ", experimentDescription='" + experimentDescription + '\'' +
                ", tags=" + tags +
                '}';
    }

    @Override
    public int getId() {
        return Protocol.GeneralMsg.SubType.EXPERIMENT_METADATA_T.getNumber();
    }
}
