package se.liu.tddd77.bilsensor.list;

import Database.Sensors.Sensor;
import Errors.BackendError;
import FrontendConnection.Listeners.SensorStatusListener;
import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

@SuppressWarnings("rawtypes")
public class SensorListFragment extends ListFragment 
	implements SensorStatusListener{

	public static ListAdapter adapter;
	private static final String STATE_ACTIVATED_POSITION = "activated_position";
	private ListEventListener mListener = sListenerCallback;
	
	private int mActivatedPosition = 0;
	
	public interface ListEventListener {
		public void onItemSelected(int index) throws BackendError;
	}
	
	private static ListEventListener sListenerCallback = new ListEventListener() {
		@Override
		public void onItemSelected(int index) {
			adapter.notifyDataSetChanged();
		}
	};
	
	public SensorListFragment() {
	}
	
	/**
	 * Create the view. 
	 * The list starts of with just a header. This should change first when 
	 * sensors have been selected, then again once the recording has started. 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("SensorListFragment - onCreate: ");
	
		Log.d("SensorListFragment", "Adapter created");
		setListAdapter(adapter);
		Log.d("SensorListFragment", "Adapter set");
		onUpdate();
	}
	
	/**
	 * When the sensor list gets updated the list must first cleared, then 
	 * the header should be added, followed by the sensors and the footer
	 */
	public static void onUpdate(){
		adapter.clear();
		
//		adapter.add(header);
		System.out.println("SensorListFragment - onUpdate: started");
//		List<Sensor> critical = Backend.getInstance().getCriticalSensors();
		//TODO: Adds the sensors as new wrappers, should get the list from mediator
//		for(Sensor sensor : critical){
//			adapter.add(new SensorWrapper(sensor));
//		}
		
		//TODO: add elements in better way?
		for(ListElement element : ElementList.getInstance().getElements()){
			adapter.add(element);
		}
		
		adapter.notifyDataSetChanged();
	}
	
	/**
	 * Reinitialize the data after a reset. 
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Log.d("SensorListFragment", "onViewCreated: Super done");
		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			adapter.setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
		}
		Log.d("SensorListFragment", "onViewCreated: All done");
	}
	
	
	
	/**
	 * Set the listener to the current container. 
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof ListEventListener)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mListener = (ListEventListener) activity;
	}
	
	/**
	 * Fall back on default listener when detaching from container. 
	 */
	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mListener = sListenerCallback;
	}
	
	
	
	/**
	 * Select the item when clicked. 
	 */
	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		try {
			mListener.onItemSelected(position);
			System.out.println("SensorListFragment - onListItemClick: ");
		} catch (BackendError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		adapter.setActivatedPosition(position);
		mActivatedPosition = position;
	}
	
	/**
	 * Save the current selection for resets. 
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}
	
	/**
	 * Redraw the list when the status of a sensor is changed
	 */
	@Override
	public void statusChanged(Sensor sensor) {

		Log.d("SensorListFragment","statusChanged");

		adapter.notifyDataSetChanged();
	}
}
