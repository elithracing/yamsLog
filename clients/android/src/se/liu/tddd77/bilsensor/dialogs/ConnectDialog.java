package se.liu.tddd77.bilsensor.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;


@SuppressLint({ "NewApi", "ValidFragment" })
public class ConnectDialog extends DialogFragment {
 
	public interface IPListener { 
		StringBuffer getStringBuffer();
		void updateLatest(String newName);
		
		}

	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("Choose server:");
		final EditText input = new EditText(getActivity());
		input.setFocusable(true);
		input.setFocusableInTouchMode(true);
		//input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		input.setText(((IPListener) getActivity()).getStringBuffer());

/*
		if(servername != null){
			input.setText(servername);
		}
	*/	
		builder.setView(input);
		builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				//Input-Check?
					((IPListener)getActivity()).updateLatest(input.getText().toString());
			}
		});
		builder.setNegativeButton("Dismiss",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

			}
		});
		
		Dialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		return dialog;

}
}
