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

import Errors.BackendError;

/**
 * Created with IntelliJ IDEA.
 * User: Aitesh
 * Date: 2014-02-20
 * Time: 09:49
 * To change this template use File | Settings | File Templates.
 */
public class OutsideCode {
    public static void main(String[] args) throws BackendError{
        //TEMPORARY CODE!
        filterDebugger fd = new filterDebugger();
        fd.test();
        //NewDebugger nd = new NewDebugger();
       // nd.runPlayback();
       // nd.runTest();
        /*
        try{
            Backend.createInstance(null);       //args[0],Integer.parseInt(args[1]),
            Backend.getInstance().connectToServer(args[0], Integer.parseInt(args[1]));
            Thread.sleep(1000); Backend.getInstance().sendSettingsRequestMessageALlSensors();
           //Thread.sleep(1000000);
            Thread.sleep(1000); Backend.getInstance().createNewProjectRequest("projectName"+ System.currentTimeMillis());

            Thread.sleep(10000); Backend.getInstance().startDataCollection("test1234");


            Thread.sleep(1000); Backend.getInstance().stopDataCollection();

            //Backend.getInstance().startDataCollection("test123");
          //  while (true) Backend.getInstance().getHandle().startDataCollection("notAFileNameHurrDurrDurr.not");

        }catch (Exception ignore){
            ignore.printStackTrace();
        }


        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        System.out.println("Slept, now printing");
        boolean b = true;
        if(b) return;
        int s= 0;
        while (b){


            try {
                Thread.sleep(1000/60);
                for(int i = 0; i <  Backend.getInstance().getSensors().size();i++){
                    double[] notTime = new double[Backend.getInstance().getSensors().get(i).getAttributesName().length];
                    try{
                        Backend.getInstance().getSensors().get(i).startReading();
                    }catch (BackendError r){
                        r.printStackTrace();
                        Thread.sleep(3600000);
                    }
                    try {
                        for(int k = 0; k < Backend.getInstance().getSensors().get(i).getAttributesName().length;k++){
                            for(int j = 1; j < Backend.getInstance().getSensors().get(i).getAttributeList(k).size()&&j<=200;j++)
                                notTime[k]+=Double.parseDouble(Backend.getInstance().getSensors().get(i).getAttributeList(k).get(Backend.getInstance().getSensors().get(i).getAttributeList(k).size() - j).toString());
                        }

                        if(++s%60==0){
                            System.out.print(System.currentTimeMillis() + "  ");
                            for(int k=0; k < notTime.length; k++){
                                System.out.print(notTime[k]+"  ");
                            }
                            System.out.println();
                        }

                    } catch (Exception ignore){
                        ignore.printStackTrace();
                        Thread.sleep(1000);
                    }
                    Backend.getInstance().getSensors().get(i).endReading();
                }
            } catch (Exception ignore){
                ignore.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

        System.out.println("finished printing");
          */
        //new Backend(args[0],Integer.parseInt(args[1]));
    }
}
