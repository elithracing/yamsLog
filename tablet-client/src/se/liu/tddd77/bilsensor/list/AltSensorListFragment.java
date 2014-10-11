package se.liu.tddd77.bilsensor.list;

import protobuf.Protocol.SensorStatusMsg.AttributeStatusType;
import protobuf.Protocol.SensorStatusMsg.SensorStatusType;
import se.liu.tddd77.bilsensor.MainActivity;
import se.liu.tddd77.bilsensor.R;
import se.liu.tddd77.bilsensor.dialogs.RemoveViewDialog;
import se.liu.tddd77.bilsensor.graphs.SensorDataHolder;
import se.liu.tddd77.bilsensor.list.ListAdapter.ListItemListener;
import Database.Sensors.Sensor;
import Errors.BackendError;
import FrontendConnection.Backend;
import FrontendConnection.Listeners.SensorStatusListener;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;


public class AltSensorListFragment extends Fragment implements AddViewButton.AddViewListener, 
AdapterView.OnItemSelectedListener, SensorStatusListener, ListItemListener {

	private ListView list;
	public ListAdapter adapter;
	private LinearLayout innerLayout;
	private AddViewButton addViewButton;
	private View view;
	public LinearLayout eventLayout;
	
	public interface ListAdapterListener {
		void setListAdapter(ListAdapter listAdapter);
		}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// TODO Put all in scroll
		super.onCreate(savedInstanceState);
		this.view = inflater.inflate(R.layout.list_selectors, container, false);
		this.innerLayout = (LinearLayout) view.findViewById(R.id.list_layout);
		this.innerLayout.setLayoutParams(new LayoutParams(500, 500));
		this.eventLayout = (LinearLayout) view.findViewById(R.id.dynamic_events_buttons_container);
		//Create Graph List Layout
		this.list = (ListView) view.findViewById(R.id.graph_list);
		this.adapter = new ListAdapter(getActivity(), ElementList.getInstance().getElements(), this);
		this.list.setAdapter(adapter);
		this.list.setOnItemSelectedListener(this);		
		//Create AddNewGraphButton and add it to view
		this.addViewButton = new AddViewButton(getActivity(), this);
		statusChanged(null);
		this.addViewButton.setOnClickListener(new OnClickListener() {


			@Override
			public void onClick(View v) {
				SelectViewNameDialog dialog = new SelectViewNameDialog(addViewButton);
				dialog.show(getFragmentManager(), null);
			}
		});
		((LinearLayout)view.findViewById(R.id.add_new_graph_button_container)).addView(addViewButton);

		//The container for dynamic events buttons and the field for adding new dynamic events should
		//start as invisible.
		//	((LinearLayout)view.findViewById(R.id.dynamic_events_buttons_container)).setVisibility(View.GONE);
		//	((LinearLayout)view.findViewById(R.id.add_new_dynamic_event_layout)).setVisibility(View.GONE);
		return innerLayout;
	}




	@Override
	public void viewAdded(String name) {
		SensorElement element = new SensorElement(name);
		ElementList.getInstance().getElements().add(element);
		//		this.adapter.add(element);
		selectItem(ElementList.getInstance().getElements().size()-1); //Will always go to last fragment when loading profile with this implementation.
		this.adapter.notifyDataSetChanged();
		((MainActivity) getActivity()).focusFix();
	}

	private void selectItem(int index){
		Log.d("AltSensorListFragment", "onItemSelected: " + index + " simulated click");
		adapter.setActivatedPosition(index);
		adapter.notifyDataSetChanged();
		((MainActivity)getActivity()).onItemSelected(index);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Log.d("AltSensorListFragment", "onItemSelected: " + position + " clicked");
		adapter.setActivatedPosition(position);
		adapter.notifyDataSetChanged();
		((MainActivity)getActivity()).onItemSelected(position);
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		//TODO: Deselect?
	}

	//int counter = 0;
	@Override
	public void statusChanged(Sensor sensor) {
		boolean error = false;
		boolean warning = false;
		//Log.d("altsensor", "statusChanged id" + sensor.getId());
		try {
			for(Sensor currentsensor: Backend.getInstance().getSensors()){
				//if(currentsensor.getSensorStatus() == null) 
			if(currentsensor != null){
				SensorDataHolder.getInstance().setSensorWorking(currentsensor.getId(), currentsensor.getSensorStatus());
				Log.d("altsensor", "currentsensor is NOT null and status is " + currentsensor.getSensorStatus() + " key: " + currentsensor.getId());
				if(currentsensor.getSensorStatus() == SensorStatusType.NOT_WORKING){
					error = true;

					//counter = 0;
					;
				} else {
					for(AttributeStatusType status : currentsensor.getAttributeStatuses()){
						if(status == AttributeStatusType.OUTSIDE_LIMITS){
							//counter = 0;
							warning = true;
							break;
						}
					}
				}
			}	//counter = 0;

			}
			
		} catch (BackendError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		adapter.error = error;
		adapter.warning = warning;
		Log.i("AltSensorListFragment", "Status Changed");
		this.adapter.notifyDataSetChanged();
		this.view.invalidate();
	}



	@Override
	public void itemSelected(int index) {
		((MainActivity)getActivity()).onItemSelected(index);
	}

	public void addNewGraph(View v) {
		SelectViewNameDialog dialog = new SelectViewNameDialog(addViewButton);
		dialog.show(getFragmentManager(), null);
	}

	public void createGraphListLayout(){

	}

	public void createAddNewGraphButtonLayout(){

	}

	public void createDynamicEventsLayout(){

	}




	@Override
	public boolean removeView(int pos, ListAdapter listAdapter) {
		RemoveViewDialog dialog = new RemoveViewDialog();
		dialog.setListAdapter(listAdapter);
		dialog.show(getFragmentManager(), null);
		return false;
	}

}
