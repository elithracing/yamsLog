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

import java.util.ArrayList;
import java.util.List;

import Database.Sensors.Sensor;
import Errors.BackendError;
import FrontendConnection.Backend;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class SelectSensorDialog extends DialogFragment {

	public interface SensorDialogListener{
		public void selectXAxis(ArrayList<Integer> sensor);
	}

	private SensorDialogListener mListener;



	public SelectSensorDialog(SensorDialogListener listener){
		this.mListener = listener;
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
	}


	private ArrayList<Integer> sensorIds = new ArrayList<Integer>();
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.i("SelectSensorDialog", "onCreateDialog");
		List<Sensor> sensors = null;
		try {
			sensors = Backend.getInstance().getSensors();
		} catch (BackendError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String[] items = new String[sensors.size()];
		final boolean[] selected = new boolean[sensors.size()];

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select Sensor");

		boolean[] checkedItems = new boolean[items.length];

		DialogInterface.OnMultiChoiceClickListener listener = new DialogInterface.OnMultiChoiceClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				selected[which] = isChecked;

			}
		};

		DialogInterface.OnClickListener close = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				for(int i = 0; i < selected.length; i++){
					if(selected[i])
						sensorIds.add(i);
				}
				if (sensorIds.size() != 0){
					mListener.selectXAxis(sensorIds);
				}else{
				Toast toast = Toast.makeText(getActivity(), "Select at least one sensor.", Toast.LENGTH_SHORT);
				toast.show();
				}
			};
		};
			builder.setMultiChoiceItems(items, checkedItems, listener);
			builder.setPositiveButton("OK", close);

			//Build item array

			for(int i = 0; i < sensors.size(); i++){
				String sensorName = sensors.get(i).getSensorName();
				items[i] = sensorName;
				Log.d("SelectSensorDialog", sensorName);
			}
			//builder.setItems(items, listener);


			//Special buttons because Android closes the dialog when OK is pressed unless you override it.
			final AlertDialog d = builder.create();
			/*d.show();
		d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
		{            
			@Override
			public void onClick(View v)
			{
				Boolean wantToCloseDialog = false;

				for(int i = 0; i < selected.length; i++){
					if(selected[i])
						sensorIds.add(i);
				}
				if (sensorIds.size() != 0){
					mListener.selectXAxis(sensorIds);
					wantToCloseDialog = true;

					if(wantToCloseDialog)
						d.dismiss();
				}
			}
		});*/

			return d;
		}


}

