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
