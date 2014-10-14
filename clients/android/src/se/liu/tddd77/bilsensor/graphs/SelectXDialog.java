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

package se.liu.tddd77.bilsensor.graphs;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Database.Sensors.Sensor;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

public class SelectXDialog extends DialogFragment {

	public interface XDialogListener{
		public void selectYAxis(int x);
	}
	
	private XDialogListener mListener;
	private Sensor sensor;

	
	
	public SelectXDialog(Sensor sensor, XDialogListener listener){
		this.sensor = sensor;
		this.mListener = listener;

	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select X Attribute");
		
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.selectYAxis(which);
			}
		};
		String[] items = sensor.getAttributesName();
		builder.setItems(items, listener);
		
		return builder.create();
	}
	
	
	
}
