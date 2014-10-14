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

package se.liu.tddd77.bilsensor.graphs.addGraph;

import se.liu.tddd77.bilsensor.graphs.LineGraph;
import se.liu.tddd77.bilsensor.graphs.SensorGraph;
/*IMPORTANT: When you add a new GraphType you MUST add it at the bottom of the file to not break profile handling*/
//TODO: Add some random GraphTypes to see how it handles indexation. (For testing only)
public enum GraphType {

	LINEGRAPH("LineGraph", LineGraph.class);
	
	private String name;
	public Class<? extends SensorGraph> graphClass;
	
	private GraphType(String name, Class<? extends SensorGraph> graphClass){
		this.graphClass = graphClass;
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	/*public GraphType getTypeByString(String type){
		switch(type){
		case(""):
			
		}
	}*/
	
}
