package se.liu.tddd77.bilsensor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import se.liu.tddd77.bilsensor.graphs.GraphContainer;
import se.liu.tddd77.bilsensor.graphs.Tickable;
import se.liu.tddd77.bilsensor.graphs.addGraph.AddGraphButton;
import se.liu.tddd77.bilsensor.graphs.addGraph.GraphType;
import se.liu.tddd77.bilsensor.list.ElementList;
import Database.Sensors.Sensor;
import Errors.ThreadIsLockedError;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class SensorDetailFragment extends Fragment implements Tickable, AddGraphButton.AddGraphListener{

	public static final String ARG_ITEM_ID = "sensor_detail_fragment";
	private Activity mActivity;
	//TODO: Add support for more than one graph
	//TODO: Return an view containing graph containers instead of a graph
	//public TextView text;
	private List<GraphContainer> graphs;
	private LinearLayout layout;
	private ArrayList<Sensor> sensorList;

	//TODO: Ugly, change implementation
	//Since we only open one dialog at a time, it is OK to do it this way
	private View view;

	public SensorDetailFragment(){ 
		Log.i("SensorDetailFragment","Construction");
	}
	
	/*public void onCreate (Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if(graphs==null) graphs = new LinkedList<GraphContainer>();
	}*/
	/**
	 * Shows the container and recursively all graphs. 
	 * If there are no graphs/containers the default view is a button
	 * which opens a SelectGraphDialog
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("SensorDetailFragment","onCreateView");
		this.view = inflater.inflate(R.layout.fragment_sensor_detail, container, false);
		
		layout = (LinearLayout) view.findViewById(R.id.sensor_detail);
		//TODO: THIS IS PRODUCTION ONLY; DO REMOVE
		//text = (TextView) view.findViewById(R.id.sensor_text);
		if(graphs==null) graphs = new LinkedList<GraphContainer>();

		// Show a button to add graphs if the list is empty
		if(ElementList.getInstance().getElements()!= null && ElementList.getInstance().getElements().size()>1){
		
			Log.d("SensorDetailFragment","adding addgraphbutton");
			AddGraphButton selectGraph = new AddGraphButton(this.getActivity(), this, this);
			layout.addView(selectGraph);
			Log.d("SensorDetailFragment","Trying to add old graphs");
			// Add all graphs to the layout
			List<GraphContainer> cloneList = new ArrayList<GraphContainer>(graphs);
			graphs.clear();
			for(GraphContainer gc : cloneList){
				addGraph(gc.clone(getActivity(), layout));
			}

		}

		layout.invalidate();
		layout.requestLayout();
		return view;
	}

	//TODO: Fix implementation, shouldn't assume destroy on pause
	//	@Override
	//	public void onPause() {
	//		LinearLayout layout = (LinearLayout) this.getView().findViewById(R.id.sensor_detail);
	//		if(layout != null){
	//			layout.removeAllViews();
	//		}
	//		super.onPause();
	//	}

	/**
	 * Updates all graphs
	 */
	public void tick() {
		//Log.i("SensorDetailFragment", "updateInformation"); //TODO: Might need to looop
		for(GraphContainer graph : graphs){
				graph.updateData();	

		}
	}


	public List<Sensor> getSensors(){
		ArrayList<Sensor> result = new ArrayList<Sensor>();
		if(sensorList == null) return result;
		if(graphs==null || sensorList.isEmpty()) return result;
		for(int i = 0; i < this.graphs.size(); i++){
			for(int j = 0; j < this.graphs.get(i).sensorIds.size(); j++)
				result.add(sensorList.get(j));
		}
		return result;
	}
		//int i = 0;
	public void addGraph(GraphContainer graph){
		
		//layout.removeViewAt(Math.max(0, layout.getChildCount()-2)); //TODO: non ugly implementation
		this.graphs.add(graph);
		this.layout.addView(graph);
		//this.layout.addView(new AddGraphButton(this.getActivity(), this, this));
	}
	
	public void doneRec(){
		for(int i = 0; i < graphs.size(); i++){
		graphs.get(i).clear();
		}
	}
	
	List<Integer> sensorIds;
	@Override
	public void graphSelected(AddGraphButton button, GraphType type, ArrayList<Sensor> sensor, int x, ArrayList<ArrayList<Integer>> y) {
		Log.d("MainActivity", getActivity().getClass().getSimpleName());
		this.sensorList = sensor;
		sensorIds = new ArrayList<Integer>();
		for(int i = 0; i < sensor.size(); i++) sensorIds.add(sensor.get(i).getId());
		GraphContainer graphContainer = new GraphContainer(getActivity(), type, sensorIds, x, y, layout, getFragmentManager(), new boolean[] {true, true, true}, 0, 0);
		Log.d("MainActivity", graphContainer.toString());
		//Log.d("MainActivity", graphContainer.sensor.toString());
		//TODO: Fix layout so this isn't necessary
		//TODO: graphContainer.child.setLayoutParams(new LayoutParams(500,500));
		//graphContainer.child.setBackgroundColor(Color.GREEN);
		this.addGraph(graphContainer);
		//		this.layout.addView(graphContainer);
		this.layout.invalidate();
		this.layout.requestLayout();

		//XML Version
		//		int index = layout.indexOfChild(button);
		//		this.layout.removeViewAt(index);
		//		this.layout.addView(new GraphContainer(view.getContext(), type, sensor, x, y));
		//		LayoutInflater inflater = (LayoutInflater)this.getActivity().getSystemService
		//				  (Context.LAYOUT_INFLATER_SERVICE);
		//		View view = inflater.inflate(R.layout.graph_container, null);
		//		
		//		LineGraph actualGraph = new LineGraph(view.getContext(), sensor, x, y);
		//		LinearLayout xmlLayout = (LinearLayout) view.findViewById(R.id.graph_container_layout);
		//		xmlLayout.addView(actualGraph);
		//		LinearLayout layout = (LinearLayout) view.findViewById(R.id.graph_container_layout);
		//		layout.addView(view);
		//		this.layout.addView(view);

	}

	@Override
	public void onPause(){
		super.onPause();
		Log.i("SensorDetailFragment" , "onPause");
	}

	@Override
	public void onResume(){
		super.onResume();
		Log.i("SensorDetailFragment" , "onResume");

	}
	@Override
	public void onStop(){
		super.onStop();
		Log.i("SensorDetailFragment" , "onStop");
	}
	@Override
	public void onDestroy(){
		super.onDestroy();
		Log.i("SensorDetailFragment" , "onDestroy");
	}
									
	public List<GraphContainer> getSensorGraphs(){
		return this.graphs;
	}

	public void graphSaved(GraphType type, ArrayList<Integer> sensorIds2, int x, ArrayList<ArrayList<Integer>> y) {
		GraphContainer graphContainer = new GraphContainer(mActivity, type, sensorIds2, x, y, layout, getFragmentManager(), new boolean[]{true, true, true}, 0, 0);
		Log.d("MainActivity", mActivity.getClass().getSimpleName());
		Log.d("MainActivity", graphContainer.toString());
		//if(graphs==null) graphs = new LinkedList<GraphContainer>();
		//TODO: Get the available sensors through STATUSFRAGMENT and make sure the sensor being loaded is in place.
		this.addGraph(graphContainer);
		this.layout.invalidate();
		this.layout.requestLayout();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = activity;
	}
	
	public List<GraphContainer> getGraphs(){
		return graphs;
	}

}
