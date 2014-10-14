package se.liu.tddd77.bilsensor.dialogs;

import java.util.List;

import se.liu.tddd77.bilsensor.list.AltSensorListFragment.ListAdapterListener;
import se.liu.tddd77.bilsensor.list.ListAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;


public class RemoveViewDialog extends DialogFragment implements ListAdapterListener{

	private static ListAdapter listAdapter;
	
	public interface removeSensorListener {
		void removeSensorRequired(List<Integer> list);
		}
	public interface RemoveViewListener {
		List<Integer> deleteView();
		}
	
	public Dialog onCreateDialog(Bundle savedInstanceState){
	final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	builder.setMessage("Delete view?");
	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			((removeSensorListener) getActivity()).removeSensorRequired(listAdapter.deleteView());
			// TODO Auto-generated method stub
		}
	});
	builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			
		}
	});
	return builder.create();
	}

	@Override
	public void setListAdapter(ListAdapter listAdapterr) {
		listAdapter = listAdapterr;
	}
	
}
