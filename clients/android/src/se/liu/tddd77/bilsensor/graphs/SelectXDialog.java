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
