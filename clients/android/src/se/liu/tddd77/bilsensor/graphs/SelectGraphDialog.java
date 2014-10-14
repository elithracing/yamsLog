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
