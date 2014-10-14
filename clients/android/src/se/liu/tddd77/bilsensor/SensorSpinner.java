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

package se.liu.tddd77.bilsensor;

import java.util.List;

import Database.Sensors.Sensor;
import Errors.BackendError;
import FrontendConnection.Backend;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * A spinner is a drop-down list. 
 */
@Deprecated
public class SensorSpinner extends Spinner
implements OnMultiChoiceClickListener, OnCancelListener{

	private boolean[] selected = new boolean[0];
	private String prompt;
	private List<Sensor> data;
	private SensorSpinnerListener listener;
	
	
	
	/**
	 * Interface for the listeners
	 */
	public interface SensorSpinnerListener {
        public void onItemsSelected(boolean[] selected);
    }
	
	
	
	public SensorSpinner(Context context) {
		super(context);
	}

	public SensorSpinner(Context context, AttributeSet attrs){
		super(context, attrs);
	}

	public SensorSpinner(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
	}
	
	
	
	/**
	 * When the user cancels or accepts the changes, inform the listeners of what choices were made. 
	 */
	@Override
	public void onCancel(DialogInterface dialog) {
		int nrSelected = 0;
		for(boolean b : selected){
			if(b){
				nrSelected++;
			}
		}
		prompt = Integer.toString(nrSelected) + " sensors selected";
		System.out.println("SensorSpinner - onCancel");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
				android.R.layout.simple_spinner_item,
				new String[] { prompt });
		setAdapter(adapter);
		listener.onItemsSelected(selected);
	}
	
	/**
	 * Update the selection when a choice is selected. 
	 */
	@Override
	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		selected[which] = isChecked;
		System.out.println("SensorSpinner - onClick");
	}
	
	/**
	 * Build the dialog window when the button is pressed. 
	 */
	@Override
	public boolean performClick(){
		if(data == null){
//			return false;
			try {
				updateData();
			} catch (BackendError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("SensorSpinner - performClick");
		try {
			updateData();
		} catch (BackendError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String sensors[] = new String[data.size()];
		for(int i = 0; i < data.size(); i++){
			sensors[i] = data.get(i).getSensorName();
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setMultiChoiceItems(sensors, selected, this);

		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.cancel();
			}
		});
		builder.setOnCancelListener(this);
		builder.show();

		return true;
	}
	
	/**
	 * Set the listener
	 * @param listener
	 */
	public void setListener(SensorSpinnerListener listener) {
		this.listener = listener;
	}

	
	
	/**
	 * Updates data to reflect the current state of the sensors
	 * and sets the critical ones as selected. 
	 * @throws BackendError 
	 */
	public void updateData() throws BackendError{
		if(Backend.getInstance() == null){
			//TODO: Actual implementation with errors and all that
			return;
		}
		this.data = Backend.getInstance().getSensors();
		selected = new boolean[data.size()];
		List<Sensor> critical = Backend.getInstance().getSensors();
		for(int i = 0; i < selected.length; i++){
			if(critical.contains(data.get(i))){
				selected[i] = true;
			}
		}
	}
	
	/**
	 * Set the text on the button to the number of sensors selected
	 */
	public void updatePrompt(){
		int i = 0;
		for(boolean choice : selected){
			if(choice){
				i++;
			}
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
				android.R.layout.simple_spinner_item, new String[] { i + " sensors selected" });
		setAdapter(adapter);
	}

}
