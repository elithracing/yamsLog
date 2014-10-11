//package se.liu.tddd77.bilsensor.detail;
//
//import java.util.ArrayList; 
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Random;
//
//import se.liu.tddd77.bilsensor.R;
//import se.liu.tddd77.bilsensor.graphs.GraphContainer;
//import se.liu.tddd77.bilsensor.graphs.LineGraph;
//import se.liu.tddd77.bilsensor.graphs.SelectGraphDialog;
//import se.liu.tddd77.bilsensor.graphs.SelectSensorDialog;
//import se.liu.tddd77.bilsensor.graphs.SensorGraph;
//import se.liu.tddd77.bilsensor.graphs.Tickable;
//import se.liu.tddd77.bilsensor.graphs.addGraph.AddGraphButton;
//import se.liu.tddd77.bilsensor.graphs.addGraph.GraphType;
//import se.liu.tddd77.bilsensor.graphs.addGraph.AddGraphButton.AddGraphListener;
//import Database.Sensors.Sensor;
//import Errors.BackendError;
//import Errors.ThreadIsLockedError;
//import FrontendConnection.Backend;
//import android.app.Fragment;
//import android.content.DialogInterface;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.widget.AbsListView.SelectionBoundsAdjuster;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//public class SensorFragment extends Fragment implements Tickable, AddGraphButton.AddGraphListener{
//
//	//TODO: Add support for more than one graph
//	//TODO: Return an view containing graph containers instead of a graph
//	public TextView text;
//	private List<GraphContainer> graphs;
//	private LinearLayout layout;
//	
//	//TODO: Ugly, change implementation
//	//Since we only open one dialog at a time, it is ok to do it this way
//	private DialogInterface.OnClickListener mGraphListener;
//	private Class<? extends SensorGraph> graphClass;
//	private View view;
//	
//	public SensorFragment(){}
//	
//	/**
//	 * Shows the container and recursively all graphs. 
//	 * If there are no graphs/containers the default view is a button
//	 * which opens a SelectGraphDialog
//	 */
//	@Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//        Bundle savedInstanceState) {
//		this.view = inflater.inflate(R.layout.fragment_sensor_detail, container, false);
//		
//		layout = (LinearLayout) view.findViewById(R.id.sensor_detail);
//		text = (TextView) view.findViewById(R.id.sensor_text);
//		
//		// Show a button to add graphs if the list is empty
//		if(graphs == null || graphs.isEmpty()){
//			graphs = new LinkedList<GraphContainer>();
//			
//			AddGraphButton selectGraph = new AddGraphButton(container.getContext(), this, this);
//			
//			
//			layout.addView(selectGraph);
//		}
//		//TODO: Fix the rest
////		else{
////			try {
////				graphs.add(new GraphContainer(view.getContext(), Backend.getInstance().getSensor(0), 0, 1));
////			} catch (BackendError e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////			}
////			// Add all graphs to the layout
////			for(GraphContainer graph : graphs){
////				layout.addView(graph);
////			}
////		}
//        return view;
//    }
//	
//	//TODO: Fix implementation, shouldn't assume destroy on pause
//	@Override
//	public void onPause() {
//		LinearLayout layout = (LinearLayout) this.getView().findViewById(R.id.sensor_detail);
//		layout.removeAllViews();
//		super.onPause();
//	}
//
//	/**
//	 * Updates all graphs
//	 */
//	public void tick() {
//		Log.i("SensorFragment", "updateInformation");
//		for(GraphContainer graph : graphs){
//			graph.updateData();
//			text.setText(graph.sensor.toString());
//		}
//	}
//	
//	public void addGraph(GraphContainer graph){
//		this.graphs.add(graph);
//		this.layout.addView(graph);
//	}
//
//	@Override
//	public void graphSelected(AddGraphButton button, GraphType type, Sensor sensor, int x, ArrayList<Integer> y) {
//		int index = layout.indexOfChild(button);
//		this.layout.removeViewAt(index);
//		this.layout.addView(new GraphContainer(view.getContext(), type, sensor, x, y));
//	}
//
//}
