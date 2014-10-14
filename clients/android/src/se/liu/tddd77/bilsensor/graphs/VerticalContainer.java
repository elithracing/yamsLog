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

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

public class VerticalContainer extends LinearLayout implements Tickable {

	ArrayList<Tickable> children = new ArrayList<Tickable>();
	
	public VerticalContainer(Context context) {
		super(context);
	}
	
	/**
	 * You can only add Tickable Views to the container.
	 * If the view is not a tickable, this throws an IllegalArgumentException
	 */
	@Override
	public void addView(View v){
		if(!(v instanceof Tickable)){
			throw new IllegalArgumentException("Tried to add normal view to container of tickables");
		}
		Tickable view = (Tickable) v;
		super.addView(v);
		children.add(view);
	}
	
	/**
	 * You can only remove Tickable Views from the container.
	 * If the view is not a tickable, this throws an IllegalArgumentException
	 */
	@Override
	public void removeView(View view){
		if(!(view instanceof Tickable)){
			throw new IllegalArgumentException("Tried to add normal view to container of tickables");
		}
		Tickable tickable = (Tickable) view;
		super.removeView(view);
		children.remove(tickable);
	}
	
	@Override
	public void tick() {
		for(Tickable child : children){
			child.tick();
		}
	}

	
	
}
