/**
 * yamsLog is a program for real time multi sensor logging and 
 * supervision
 * Copyright (C) 2014  
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

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
