package se.liu.tddd77.bilsensor.graphs;

import se.liu.tddd77.bilsensor.graphs.addGraph.GraphType;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

public class SelectGraphDialog extends DialogFragment {

	public interface GraphDialogListener {
		public void selectSensors(GraphType type);
	}

	private GraphDialogListener mListener;
	
	
	
	public SelectGraphDialog(GraphDialogListener listener){
		this.mListener = listener;
	}
	
	
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.i("SelectGraphDialog", "onCreateDialog");
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select Graph Type");
		
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.selectSensors(GraphType.values()[which]);
			}
		};
		
		//Build item array
		GraphType[] graphs = GraphType.values();
		String[] items = new String[graphs.length];
		for(int i = 0; i < graphs.length; i++){
			String graphName = graphs[i].getName();
			items[i] = graphName;
			Log.d("SelectGraphDialog", graphName);
		}
		builder.setItems(items, listener);
		
		return builder.create();
	}
	
}
