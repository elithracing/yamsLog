package se.liu.tddd77.bilsensor.dialogs;

import se.liu.tddd77.bilsensor.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


@SuppressLint({ "NewApi", "ValidFragment" })
public class RecordDialog extends DialogFragment {

	
	private static String sName;
	private static String sNotes;
	private static Boolean fromDiscard = false;
	public interface RecordDialogListener {
		void confirmDestroyExperiment(String sName, String sNotes);
		void onDoneRec(String sName, String sNotes);
		void cancelAction();
		}
	
	public void setETs(String etNameIn, String etNotesIn){
		sName = etNameIn;
		sNotes = etNotesIn;
		fromDiscard = true;
		Log.d("debug", "setETs "+ sName+ " "+ sNotes);
	}

	public  Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.d("debug", "onCreateDialog");

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View dialog_view = inflater.inflate(R.layout.record_fragment, null);
		builder.setView(dialog_view).setNegativeButton("Cancel", null).setNeutralButton("Delete experiment", null).setPositiveButton("Save experiment", null);
		//builder.setView(dialog_view);
		//builder.setView(dialog_view).setPositiveButton("Save experiment", null); //Creating my own 


		final Dialog d = builder.create();

		d.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				
				if (fromDiscard && 	!(sName == null && sNotes == null)){
					Log.d("debug", "fromDiscard and not null names!");
					Log.d("debug", sName +"  "+sNotes);
					((EditText) dialog_view.findViewById(R.id.name)).setText(sName);
					((EditText) dialog_view.findViewById(R.id.notes)).setText(sNotes);
					fromDiscard = false;
				}
				
				Button bp = ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE);
				bp.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (!fromDiscard){
						EditText et;
						et = (EditText) dialog_view.findViewById(R.id.name);
						sName = et.getText().toString();
						et = (EditText) dialog_view.findViewById(R.id.notes);
						sNotes = et.getText().toString();
						Log.d("debug", sName+"\n\n "+sNotes);
						}

						if(!sName.matches("")){					
							((RecordDialogListener) getActivity()).onDoneRec(sName.toString(), sNotes.toString());
							//((Dialog) d).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
							fromDiscard = false;
							d.dismiss();
						}
						else{
							Toast.makeText(builder.getContext(), "You must name your experiment", Toast.LENGTH_SHORT).show();
						}
					}
				});


				Button bNeu = ((AlertDialog) d).getButton(AlertDialog.BUTTON_NEUTRAL);
				bNeu.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (!fromDiscard){
						EditText et;
						et = (EditText) dialog_view.findViewById(R.id.name);
						sName = et.getText().toString();
						et = (EditText) dialog_view.findViewById(R.id.notes);
						sNotes = et.getText().toString();
						Log.d("debug", sName+"\n\n "+sNotes);
						}
						Log.d("debug", "Calling confirmDestroy");
						((RecordDialogListener) getActivity()).confirmDestroyExperiment(sName, sNotes);
						d.dismiss();
					}
				});
				
				Button bNeg = ((AlertDialog) d).getButton(AlertDialog.BUTTON_NEGATIVE);
				bNeg.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Log.d("cancel", "calling");
						((RecordDialogListener) getActivity()).cancelAction();
						//((Dialog) d).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
						d.dismiss();
					}
				});
			}
		});

		d.setCanceledOnTouchOutside(false);
		return d;
	}


}
