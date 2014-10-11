package Database.Messages;

import protobuf.Protocol;

import java.util.List;

/**
 * Created by Johan on 2014-02-26.
 *
 * This message contains metadata.
 * The information contained in the meta data answers
 * different questions; Who is the test leader, what date is it,
 * and so on.
 *
 * This information is optional.
 */
public class ProjectMetaData implements Message {
    private String test_leader;
    private Long date; // POSIX time (seconds since epoch)
    private String email;
    private List<String> member_names;
    private List<String> tags;
    private String description;

    public ProjectMetaData() {
        this.test_leader = null;
        this.date = null;
        this.email = null;
        this.member_names = null;
        this.tags = null;
        this.description = null;
    }

    public ProjectMetaData(Protocol.ProjectMetadataMsg data) {
        this.test_leader = data.getMetadata().getTestLeader();
        this.date = data.getMetadata().getDate();
        this.email = data.getMetadata().getEmail();
        this.member_names = data.getMetadata().getMemberNamesList();
        this.tags = data.getMetadata().getTagsList();
        this.description = data.getMetadata().getDescription();
    }


    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }


    public void setEmail(String email) {
        this.email = email;
    }

    public String getTest_leader() {
        return test_leader;
    }

    public void setTest_leader(String test_leader) {
        this.test_leader = test_leader;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getMember_names() {
        return member_names;
    }

    public void setMember_names(List<String> member_names) {
        this.member_names = member_names;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    /**
     * Creates a string containing all info from this message
     * @return autogenerated string with the contents of the message
     */
    @Override
    public String toString() {
        return "ProjectMetaData{" +
                "test_leader='" + test_leader + '\'' +
                ", experimental_subject='" + '\'' +
                ", date=" + date + '\'' +
                '}';
    }

    /**
    * Gets the ID of the meta data
    * @return integer containing ID
    */
    @Override
    public int getId() {
        return Protocol.GeneralMsg.SubType.PROJECT_METADATA_T.getNumber();
    }

}
