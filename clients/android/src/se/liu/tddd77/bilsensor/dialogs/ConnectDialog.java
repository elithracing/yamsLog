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
