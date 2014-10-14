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

package se.liu.tddd77.bilsensor.dialogs;

import java.util.List;

import se.liu.tddd77.bilsensor.R;
import se.liu.tddd77.bilsensor.graphs.LineGraph;
import se.liu.tddd77.bilsensor.graphs.LineGraph.lineGraphListener;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;


@SuppressLint({ "NewApi", "ValidFragment" })
public class ConfigDialog extends DialogFragment implements lineGraphListener{
	private boolean[] previous = new boolean[2];
	
	public interface previousValsListener {
		boolean[] getPrevious();
		void setPrevious(boolean numerical, boolean minmax, boolean autoScale, String x, String y, String yLow);
		}



	public  Dialog onCreateDialog(Bundle savedInstanceState) {
		
		Log.d("configDialog", getActivity().toString());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		final View view = inflater.inflate(R.layout.config_fragment, null);
		Log.d("configDialog", view.toString());
		Log.d("configDialog", view.findViewById(R.id.showNum).toString());
		//Log.d("configDialog", previous.toString());
		previous = 	lineGraph.getPrevious();//((previousValsListener) getActivity()).getPrevious();
		((CheckBox) view.findViewById(R.id.showNum)).setChecked(previous[0]);
		((CheckBox) view.findViewById(R.id.showMinMax)).setChecked(previous[1]);
		((CheckBox) view.findViewById(R.id.autoScale)).setChecked(previous[2]);



	/*	Button removegraphview = new Button(getActivity());
		removegraphview.setText("Remove graphview");
		removegraphview.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Are you sure you want to remove this graphview?");
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						
						
						//TODO: We need to find the SensorDetail fragment that is connected to the graph which created this
						//dialog. Possible?						
					}
				});
				builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

					}
				});
				builder.show();
			
			}
		});*/
		//((ViewGroup)view).addView(removegraphview);

		builder.setView(view)
		// Add action buttons
		.setPositiveButton("Done", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				//Needs to handle not entering any data.

				try{
					EditText y = (EditText) view.findViewById(R.id.yAxis);
					//int ys = Integer.parseInt(y.getText().toString());
					Log.i("Dialog Input y", y.getText().toString());
				}
				catch (NumberFormatException e){
					Log.i("NFE", "No number in");
				}
				//Need to tell associated graph to update its values, listeners for updated values.
				lineGraph.setPrevious(((CheckBox) view.findViewById(R.id.showNum)).isChecked(), 
						((CheckBox) view.findViewById(R.id.showMinMax)).isChecked(),
						((CheckBox) view.findViewById(R.id.autoScale)).isChecked(),
						"",//((EditText) view.findViewById(R.id.xAxis)).getText().toString(), 
						 
						((EditText) view.findViewById(R.id.yAxis)).getText().toString(), 
						((EditText) view.findViewById(R.id.yAxisLow)).getText().toString());
				



			}

		})

		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				//Do nothing the proper way
				Log.i("cancel", "dismissed");
			}


		});      
		return builder.create();
	}


	private LineGraph lineGraph;
	@Override
	public void setView(LineGraph lineGrapha) {
		lineGraph = lineGrapha;
		
	}


}

