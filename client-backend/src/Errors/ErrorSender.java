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

package Errors;

import FrontendConnection.Backend;
import protobuf.Protocol;

/**
 * Created with IntelliJ IDEA.
 * User: Aitesh
 * Date: 2014-03-27
 * Time: 11:17
 * To change this template use File | Settings | File Templates.
 */
public class ErrorSender {
    static private ErrorSender instance = null;
    private Backend backend;
    static public ErrorSender getInstance() throws BackendError{
        if(instance==null){
            instance = new ErrorSender(Backend.getInstance());
        }
        return instance;
    }

    private ErrorSender(Backend backend){
        this.backend = backend;
    }


    public void sendError(Protocol.ErrorMsg.ErrorType type) throws BackendError{
        backend.sendMessage(Protocol.GeneralMsg.newBuilder().
                setSubType(Protocol.GeneralMsg.SubType.ERROR_T).
                setErrorMessage(Protocol.ErrorMsg.newBuilder().
                        setErrorType(type).build()).build());
    }
}
