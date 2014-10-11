package se.liu.tddd77.bilsensor.dialogs;

import java.util.List;

import Errors.BackendError;
import FrontendConnection.Backend;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

@SuppressLint("ValidFragment")
public class PlaybackMenu extends DialogFragment {

	public interface PlaybackListener{
		public void nameSelected(String name) throws BackendError;
	}
	
	private PlaybackListener mListener;
	
	public PlaybackMenu(PlaybackListener listener){
		this.mListener = listener;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select X Attribute");
		 
		try {
			List<String> tmp = Backend.getInstance().getExperimentFilesFromServer();
			final String[] items = new String[tmp.size()];
			for(int i = 0; i < items.length; i++){
				items[i] = tmp.get(i);
			}			
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					try {
						mListener.nameSelected(items[which]);
					} catch (BackendError e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			builder.setItems(items, listener);
		} catch (BackendError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return builder.create();
	}
	
}
