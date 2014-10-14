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

package ServerConnection;

import Database.MessageBuilder;

import java.io.InputStream;

/**
 * Used  for receiving data from the server.
 * It will add the data automatically to the database via an instance of a MessageBuilder
 */
class ThreadedListener extends Thread{
    private MessageBuilder builder;     //This object is shared between threads
    private InputStream inputStream;    //This object is shared between threads
    boolean initiated = false;
    /**
     * Constructor for ThreadedListener.
     * @param inputStream   InputStream from server
     * @param builder       MessageBuilder for current Protobuffer
     */

    public ThreadedListener(InputStream inputStream, MessageBuilder builder){
        this.builder = builder;
        this.inputStream = inputStream;
        initiated = true;
    }

    /**
     * Checks the incoming messages from the server. Will not return until execution ends.
     * this function will be called automatically when the thread is started.
     */
    @Override
    public void run(){
    	if(initiated)
        while (true){
            try{
                builder.buildMessage(inputStream);
            } catch (InterruptedException e){
                //TODO: ?
                return;
            } catch (Exception e){
                //Todo: fixa så att detta inte hoppar ur run, eller att be frontend fixa detta endå

                e.printStackTrace();
                throw new RuntimeException(e);

            }
        }
    }
}
