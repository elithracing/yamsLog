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

package se.liu.tddd77.bilsensor.detail;

import java.util.ArrayList;

import Database.Sensors.Sensor;

//TODO: Move and add to class


public interface SensorChangeListener{
	public void xChanged(int index);
	public void yAdded(int index);
	public void yRemoved(int index);
	public void yChanged(ArrayList<Integer> index);
	//public void sensorChanged(Sensor sensor, int x, int[] y);
	public void sensorChanged(ArrayList<Sensor> sensor, int x, int[] y);
}
