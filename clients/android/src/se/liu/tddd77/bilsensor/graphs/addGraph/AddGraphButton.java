package se.liu.tddd77.bilsensor.graphs.addGraph;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

import se.liu.tddd77.bilsensor.SensorDetailFragment;
import se.liu.tddd77.bilsensor.graphs.SelectGraphDialog;
import se.liu.tddd77.bilsensor.graphs.SelectSensorDialog;
import se.liu.tddd77.bilsensor.graphs.SelectXDialog;
import se.liu.tddd77.bilsensor.graphs.SelectYDialog;
import se.liu.tddd77.bilsensor.graphs.SensorGraph;
import Database.Sensors.Sensor;
import Errors.BackendError;
import FrontendConnection.Backend;
import android.app.Fragment;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class AddGraphButton extends Button implements SelectGraphDialog.GraphDialogListener, 
SelectSensorDialog.SensorDialogListener, SelectXDialog.XDialogListener, SelectYDialog.YDialogListener{

	public interface sensorAddListener{
		public void addSensorRequired(int i);
	}
	public interface AddGraphListener{
		public void graphSelected(AddGraphButton button, GraphType type, ArrayList<Sensor> sensor, int x, ArrayList<ArrayList<Integer>> y);
	}

	public AddGraphListener mGraphListener;
	//public Class<? extends SensorGraph> graphClass;
	private final Fragment fragment;
	private GraphType type;
	private ArrayList<Sensor> sensors;
	private int x;
	
	
	public int getsX() {
		return x;
	}



	private ArrayList<ArrayList<Integer>> y;

	public AddGraphButton(final Context context, SensorDetailFragment sensorFragment, AddGraphListener listener) {
		super(context);
		Log.d("AddGraphButton","addGraphButton");
		this.mGraphListener = listener;
		this.fragment = sensorFragment;
		
		this.setText("Click to add graph");
		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("AddGraphButton", "Click");
				y  = new ArrayList<ArrayList<Integer>>();
				sensors = new ArrayList<Sensor>();
				SelectGraphDialog dialog = new SelectGraphDialog(AddGraphButton.this);
				dialog.show(fragment.getFragmentManager(), null);
			}
		});
	}
	
	
	
	//--------------------------------------------------
	// Listeners
	//--------------------------------------------------
	
	@Override
	public void selectSensors(GraphType type) {
		this.type = type;
		SelectSensorDialog dialog = new SelectSensorDialog(this);
		dialog.show(fragment.getFragmentManager(), null);
	}
	
	
	
	/**
	 * Once a sensor is selected, set it 
	 */
	int oi = 0;
	int k = 0 ;
	int sensorAmount;
	ArrayList<Integer> sIL;
	@Override
	
	public void selectXAxis(ArrayList<Integer> sensorIndexList) {
			sensorAmount = sensorIndexList.size();
			sIL = sensorIndexList;
			try {
				for(int i = 0; sensorAmount > i; i++)
				this.sensors.add(Backend.getInstance().getSensors().get(sensorIndexList.get(i)));
			} catch (BackendError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(oi == 0){
			SelectXDialog dialog = new SelectXDialog(this.sensors.get(oi), this);
			dialog.show(fragment.getFragmentManager(), null);
			}else selectYAxis(this.x);
			}


	
	@Override
	public void selectYAxis(int x) {
		this.x = x;
		Log.d("debug", oi + " oi");
		SelectYDialog dialog = new SelectYDialog(this.sensors.get(oi), this, x);
		dialog.show(fragment.getFragmentManager(), null);
	}
	
	
	
	@Override
	public void instantiateGraph(ArrayList<Integer> y) {
		if(y.isEmpty()){
			selectYAxis(this.x);
			return;
		}
		this.y.add(y);
		Log.d("debug", oi + " oi");
		((sensorAddListener) fragment.getActivity()).addSensorRequired(this.sensors.get(oi).getId());
		oi++;
		//mGraphListener.graphSelected(this, type, this.sensors, x, this.y);
		if(oi < sensorAmount) selectYAxis(this.x); 
		else{
		oi= 0;
		mGraphListener.graphSelected(this, type, this.sensors, x, this.y);
		this.sensors.clear();

		}
	}



}
