package se.liu.tddd77.bilsensor.list;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;

@SuppressLint("ValidFragment")
public class SelectViewNameDialog extends DialogFragment {

	public interface ViewNameListener{
		public void nameSelected(String name);
	}
	
	private String name;
	private ViewNameListener mListener;
	
	public SelectViewNameDialog(ViewNameListener listener){
		this.mListener = listener;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog;
		//TODO Use AlertDialog.Builder to create the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select Name");
		builder.setMessage("Name: ");
		final EditText input = new EditText(getActivity());
		
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.nameSelected(input.getText().toString());
				((Dialog) dialog).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			}
		};
		builder.setView(input);
		builder.setPositiveButton("OK", listener);
		
		dialog = builder.create();
		return dialog;
	}
	
}
