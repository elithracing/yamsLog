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
