package se.liu.tddd77.bilsensor.dialogs;

import Errors.BackendError;
import FrontendConnection.Backend;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Debug;
import android.view.View;
import android.widget.Button;



public class DeleteExperimentDialog extends DialogFragment{
	
	public interface ConfirmDeleteListener {
		void showRecordDialog();
		void onDoneRec(String sName, String sNotes);
		}

	public Dialog onCreateDialog(Bundle savedInstanceState){
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final Dialog d;
		builder.setTitle("Are you sure?");
		builder.setMessage("This data collection will be deleted");
		builder.setNegativeButton("Cancel", null).setPositiveButton("Delete", null);
		d = builder.create();
		d.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				// TODO Auto-generated method stub
				Button bp = ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE);
				bp.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						try {
							Debug.stopMethodTracing();
							Debug.startMethodTracing("Stefan"+ System.currentTimeMillis());
							
							Backend.getInstance().stopDataCollection();
							;
							((ConfirmDeleteListener) getActivity()).onDoneRec(null, null);
						} catch (BackendError e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// TODO Auto-generated method stub
						d.dismiss();
					}
				});
				Button bn = ((AlertDialog) d).getButton(AlertDialog.BUTTON_NEGATIVE);
				bn.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						// TODO Auto-generated method stub
						d.dismiss();
						((ConfirmDeleteListener) getActivity()).showRecordDialog();
					}

				});
			}
		});


	
	d.setCanceledOnTouchOutside(false);

	return d; //RETURNS THE D
}
}