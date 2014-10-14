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

import android.app.Fragment;

//TODO: Don't really like this, but acceptable as a map substitute I guess(?)
public abstract class ListElement<T extends Fragment> {
	
	private String name;
	protected T fragment;
	public boolean elementChanged = false;
	
	public ListElement(String name){
		this.name = name;
	}
	
	@Override
	public String toString(){
		return name;
	}
	
	public String getId(){
		return name;
	}

	public Fragment getFragment() {
		return fragment;
	}

}
