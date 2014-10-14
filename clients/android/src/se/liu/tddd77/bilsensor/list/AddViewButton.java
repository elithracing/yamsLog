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

package se.liu.tddd77.bilsensor.list;

import android.content.Context;
import android.widget.Button;

public class AddViewButton extends Button implements SelectViewNameDialog.ViewNameListener {

	public interface AddViewListener{
		public void viewAdded(String name);
	}
	
	private AddViewListener mListener;
	
	public AddViewButton(Context context, AddViewListener listener){
		super(context);
		super.setText("Add view");
		this.mListener = listener;
	}

	@Override
	public void nameSelected(String name) {
		mListener.viewAdded(name);
	}
	
}
