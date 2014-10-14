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

import Database.Messages.ProjectMetaData;
import Database.Sensors.Sensor;
import Errors.BackendError;
import FrontendConnection.Backend;
import FrontendConnection.Listeners.ProjectCreationStatusListener;
import protobuf.Protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Aitesh
 * Date: 2014-04-10
 * Time: 09:34
 * To change this template use File | Settings | File Templates.
 */
public class NewDebugger implements ProjectCreationStatusListener {
    private boolean projectListChanged;

    public NewDebugger(){
        try{
            Backend.createInstance(null);       //args[0],Integer.parseInt(args[1]),
            Backend.getInstance().addProjectCreationStatusListener(this);
            Backend.getInstance().connectToServer("130.236.63.46",2001);
        } catch (BackendError b){
            b.printStackTrace();
        }
        projectListChanged = false;
    }

    private synchronized boolean readWriteBoolean(Boolean b){
        if(b!=null) projectListChanged=b;
        return projectListChanged;
    }

    public void runPlayback() {
        try{
            Thread.sleep(1000); //Backend.getInstance().sendSettingsRequestMessageALlSensors(0);

            readWriteBoolean(false);
            Random r = new Random();
            String[] strings = new String[]{"Name","Code","File","Today","Monday","Tuesday","Wednesday","Thursday","Friday","Gotta","Get","Out","It","S","Friday"};
            String project = "projectName" +r.nextInt()%10000;
            String playbackProject = "realCollection";

            Thread.sleep(1000);
            System.out.println("Setting active project to : " + playbackProject);

            Backend.getInstance().setActiveProject(playbackProject);
            ProjectMetaData d = new ProjectMetaData();

            d.setTest_leader("ffu");
            d.setDate(1l);
            List<String> s = new ArrayList<String>();
            s.add("memer1");
            d.setMember_names(s);
            d.setTags(s);
            d.setDescription("desc");

            List l = d.getMember_names();
            l.add(r.nextInt()+". name");
            d.setMember_names(l);
            Backend.getInstance().sendProjectMetaData(d);
            System.out.println("starting data collection");
            Thread.sleep(100);

            String experimentName = "smallrun";
            //Backend.getInstance().setActiveProject(playbackProject);

            // projektnamn:
            Thread.sleep(3000);
           // Backend.getInstance().getSensorConfigurationForPlayback().getSensorId();

            List<Protocol.SensorConfiguration> playbackConfig;
            playbackConfig = Backend.getInstance().getSensorConfigurationForPlayback();

            List<Integer> listOfIds = new ArrayList<Integer>();


          /*  for (Protocol.SensorConfiguration aPlaybackConfig : playbackConfig) {
                listOfIds.add(aPlaybackConfig.getSensorId());
            }*/


            System.out.println("LIST OF IDS SENT TO SERVER-------------------------------------------");
            System.out.println(listOfIds);
            System.out.println("LIST OF IDS END -----------------------------------------------------");


            System.out.println("LIST OF IDS CURRENTLY IN DATABASE -----------------------------------");
            System.out.println(Backend.getInstance().getSensors());
            System.out.println("LIST IN DATABASE END ------------------------------------------------");

            Backend.getInstance().sendExperimentPlaybackRequest(experimentName, listOfIds);

            //Thread.sleep(3000);
            //Backend.getInstance().stopConnection();

        } catch (BackendError b){
            b.printStackTrace();
        } catch (InterruptedException ignore){
        }

    }

    public void run(){
        try{
            Thread.sleep(1000); Backend.getInstance().sendSettingsRequestMessageALlSensors(0);
            readWriteBoolean(false);
            Random r = new Random();
            String[] strings = new String[]{"Name","Code","File","Today","Monday","Tuesday","Wednesday","Thursday","Friday","Gotta","Get","Out","It","S","Friday"};
            String project = "projectName" +r.nextInt()%10000;
            String playbackProject = "realCollection";
            for(int i = 0; i < 1000; i++){
               // project+=strings[r.nextInt(strings.length)];
            }
            //System.out.println(project);
               //if(r.nextInt()>0) return;
            Thread.sleep(1000); Backend.getInstance().createNewProjectRequest(project);//"projectName"+ System.currentTimeMillis());


            if(!readWriteBoolean(null)){
                System.out.println("Waiting on projectListAgain");
                while (!readWriteBoolean(null));
                System.out.println("finished waiting");
            }
            // = Backend.getInstance().getProjectFilesFromServer().get());
            System.out.println("Setting active project to : " + project);

            Backend.getInstance().setActiveProject(project);
            ProjectMetaData d = new ProjectMetaData();
            //d.setEmail("NotMyEmail@gmail.com");
            d.setTest_leader("ffu");
            d.setDate(1l);
            List<String> s = new ArrayList<String>();
            s.add("memer1");
            d.setMember_names(s);
            d.setTags(s);
            d.setDescription("desc");

            List l = d.getMember_names();
            l.add(r.nextInt() + ". name");
            d.setMember_names(l);
            Backend.getInstance().sendProjectMetaData(d);
          /*  while(!readWriteBoolean(null) && readWriteBoolean(null)){
                Backend.getInstance().sendProjectMetaData(d);
                List<String> f =d.getTags();
                f.add(String.valueOf(System.currentTimeMillis()));
                d.setTags(f);
            }              **/
            System.out.println("starting data collection");
            Thread.sleep(100);
            String experimentName = "experimentNam5"+System.currentTimeMillis();

            //String experimentName = "smallrun";

            // projektnamn:

            Backend.getInstance().startDataCollection(experimentName);

            Thread.sleep(3000);
            Backend.getInstance().stopDataCollection();                                                                                                                                                                                                                                              Thread.sleep(1);
        } catch (BackendError b){
            b.printStackTrace();
        } catch (InterruptedException ignore){
        }

        System.out.println("Exiting");
    }

    public void runTest(){
//        try{
//            Thread.sleep(1000); Backend.getInstance().sendSettingsRequestMessageALlSensors(0);
//            Thread.sleep(1000); Backend.getInstance().createNewProjectRequest("projectName"+ System.currentTimeMillis());
//
//            //Backend.getInstance().startDataCollection("test1234");
//
//            //Thread.sleep(5000); Backend.getInstance().stopDataCollection();
//            Backend localInstance = Backend.getInstance();
//
//            Sensor sensor = localInstance.getSensors().get(0);
//            for(int i = 0; i<sensor.getAttributeList(0).size();i++){
//                System.out.print(String.format("%f", sensor.getAttributeList(0).get(i).floatValue()).replace(',', '.') + ",");
//
//                System.out.print(sensor.getId()  + ",");
//                for (int j = 1; j < sensor.getAttributesName().length;j++){
//
//                    System.out.print(sensor.getAttributeList(j).get(i).floatValue() + " ,");
//                }
//                System.out.println();
//            }
//
//        } catch (BackendError b){
//            b.printStackTrace();
//        } catch (InterruptedException ignore){}
    }





    @Override
    public void projectCreationStatusChanged(Protocol.CreateNewProjectResponseMsg.ResponseType responseType) {
        readWriteBoolean(true);
    }
}
