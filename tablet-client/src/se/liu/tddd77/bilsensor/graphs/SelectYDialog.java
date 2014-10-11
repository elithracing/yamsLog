package se.liu.tddd77.bilsensor.graphs;

import java.util.ArrayList;

import Database.Sensors.Sensor;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class SelectYDialog extends DialogFragment {

	public interface YDialogListener{
		public void instantiateGraph(ArrayList<Integer> y);
	}
	
	private YDialogListener mListener;
	private Sensor sensor;
	private boolean[] selected;
	private int takenX;
	final String[] items = null;
	final String strTmp = null;
	
	
	public SelectYDialog(Sensor sensor, YDialogListener listener, int x){
		this.sensor = sensor;
		this.takenX = x;
		this.mListener = listener;
		selected = new boolean[sensor.getAttributesName().length];
	}
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		try{
			mListener = (YDialogListener) activity;
		}
		catch(ClassCastException e){
			//TODO: Error handling
		}
	}
	

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//TODO Use AlertDialog.Builder to create the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select desired Y-attributes");
		
		final String[] items = sensor.getAttributesName();
		final String strTmp = items[takenX].toString();
		
		items[takenX] = items[takenX]+" (IN USE AS X-AXIS. WILL NOT BE DISPLAYED)";
		boolean[] checkedItems = new boolean[items.length];
		DialogInterface.OnMultiChoiceClickListener listener = new DialogInterface.OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				SelectYDialog.this.selected[which] = isChecked;
			}
		};
		
		DialogInterface.OnClickListener close = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {

				ArrayList<Integer> y = new ArrayList<Integer>();
				for(int i = 0; i < selected.length; i++){
					if(selected[i] && i != takenX){
						y.add(i);
					}
				}
				mListener.instantiateGraph(y);
				items[takenX] = strTmp;
			}
		};
		builder.setMultiChoiceItems(items, checkedItems, listener);
		items[takenX] = strTmp;
		//items[takenX] = strTmp;
		builder.setPositiveButton("OK", close);
		
		return builder.create();
	}
	
	public void setSelectedY(ArrayList<Integer> selected){
		for(int i : selected){
			this.selected[i] = true;
		}
	}
	
 /*   @Override
    public void onDismiss(DialogInterface dialog) {
    
		items[takenX] = strTmp;
    }*/
	
}