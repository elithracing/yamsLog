package dataSource;

import java.util.Date;
import java.util.List;

/**
 * Created by max on 2015-03-23.
 */
public interface MetaLoader {

    /**
     * Returns all Sensor names
     */
    public List<String> getSensorNames();

    /**
     * Returns all Sensor names
     */
    public List<String> getAttributeNames(String sensor);


    /**
     * Return all project names
     */
    public List<String> getProjectNames();

    /**
     * Return currently selected project
     */
    public String getCurrentProject();

    /**
     * Set current project. Creates the project if it doesn't exit.
     */
    public void changeProject(String name);

    /**
     * Sets the date for current data
     */
    public void setDate(Date date);

    /**
     * Sets the email for current data
     * @param email
     */
    public void setEmail(String email);

    /**
     * Adds a tester for current data
     * @param name
     */
    public void addTester(String name);

    /**
     * Clears all tester from current data
     */
    public void clearTesters();

    /**
     * Adds a tag for current data
     * @param tag
     */
    public void addTag(String tag);

    /**
     * Removes all tags from current data
     */
    public void clearTags();

    /**
     * Sets description for current data
     * @param descr
     */
    public void setDescription(String descr);

    /**
     * Commits the metadata which are set
     */
    public void commitMetaData();
}
