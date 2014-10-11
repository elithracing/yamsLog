package se.liu.tddd77.bilsensor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

import protobuf.Protocol.SensorStatusMsg.SensorStatusType;
import se.liu.tddd77.bilsensor.graphs.SensorDataHolder;
import se.liu.tddd77.bilsensor.graphs.Tickable;
import Database.Sensors.Sensor;
import Errors.BackendError;
import FrontendConnection.Backend;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StatusFragment extends Fragment implements Tickable{
	private View view;

	private GridLayout layout;

	private Boolean[] visibility = null;

	HashMap<Integer, ArrayList<TextView>> valuemap = new HashMap<Integer, ArrayList<TextView>>();

	HashMap<Integer, ImageView> sensormap = new HashMap<Integer, ImageView>();

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.view = inflater.inflate(R.layout.fragment_sensor_status, container, false);
		layout = (GridLayout) view.findViewById(R.id.sensor_status);
		TextView sensortitle = new TextView(getActivity());
		TextView statustitle = new TextView(getActivity());
		statustitle.setPadding(5, 20, 50, 20);
		sensortitle.setPadding(0, 20, 50, 20);
		sensortitle.setText("Sensors:");
		sensortitle.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
		statustitle.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
		
		try {
			ListIterator<Sensor> iterator = Backend.getInstance().getSensors().listIterator();
			int sensorCount = Backend.getInstance().getSensors().size();
			if(visibility==null){ 
				visibility = new Boolean[sensorCount];
				for(int i = 0; i< sensorCount; i++){
					visibility[i]=false;
				}
			}
			layout.setRowCount(sensorCount+1);
			layout.addView(statustitle);

			while(iterator.hasNext()){
				int sensorid = iterator.next().getId();
				ImageView status = new ImageView(getActivity());
				status.setPadding(40, 0, 50, 20);
				sensormap.put(sensorid, status);
				//status.setImageResource(R.drawable.unknown);
				layout.addView(status);
			}

			layout.addView(sensortitle);


			Iterator<Entry<Integer, ImageView>> it = sensormap.entrySet().iterator();
			while(it.hasNext()){
				Entry<Integer, ImageView> currentobject = it.next();
				LinearLayout sensoritem = new LinearLayout(getActivity());

				sensoritem.setOrientation(LinearLayout.VERTICAL);
				TextView sensorname = new TextView(getActivity());
				sensorname.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
				sensorname.setPadding(0, 0, 50, 20);
				sensorname.setClickable(true);

				sensorname.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						if(((LinearLayout) v.getParent()).getChildAt(1).getVisibility()==View.VISIBLE)
							((LinearLayout)v.getParent()).getChildAt(1).setVisibility(View.GONE);
						else
							((LinearLayout)v.getParent()).getChildAt(1).setVisibility(View.VISIBLE);
						((GridLayout)((LinearLayout)v.getParent()).getParent()).invalidate();

					}
				});
				sensorname.setText(Backend.getInstance().getSensorById(currentobject.getKey()).getSensorName());//getName());
				sensoritem.addView(sensorname);

				LinearLayout attributelist = new LinearLayout(getActivity());
				attributelist.setOrientation(LinearLayout.VERTICAL);

				ArrayList<TextView> attributestextviewlist = new ArrayList<TextView>();

				for(String name : Backend.getInstance().getSensorById(currentobject.getKey()).getAttributesName()){
					GridLayout attributeandvalue = new GridLayout(getActivity());

					TextView attribute = new TextView(getActivity());
					attribute.setText(name + ": ");
					attributeandvalue.addView(attribute);
					//TextView value = new TextView(getActivity());
					//value.setText("Initiating...");

					attributestextviewlist.add(new TextView(getActivity()));
					attributestextviewlist.get(attributestextviewlist.size()-1).setText("Initiating");
					attributeandvalue.addView(attributestextviewlist.get(attributestextviewlist.size()-1));

					attributelist.addView(attributeandvalue);
				}
				sensoritem.addView(attributelist);
				//sensoritem.getChildAt(1).setVisibility(View.GONE);
				try{
					attributelist.setVisibility(valuemap.get(currentobject.getKey()).get(0).getVisibility());
				} catch(Exception e){
					attributelist.setVisibility(View.GONE);
				}
				valuemap.put(currentobject.getKey(), attributestextviewlist);




				//TextView sensorname = new TextView(getActivity());
				//sensorname.setText(it.next().getKey().getName());
				layout.addView(sensoritem);

			}


		} catch (BackendError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return view;
	}
	

	
	int counter = 0;
	static final int X_ID = 0;
	//HashMap<Integer, ImageView> latestValues = new HashMap<Integer, ImageView>();
	@Override
	public void tick() {

		ArrayList<List<Float>> data = SensorDataHolder.getInstance().getLatestValues();
		for(List<Float> attrList : data.subList(1, data.size())){
			Integer latest = Math.round(data.get(X_ID).get(counter));
			//Log.d("LineGraph1", attrList.size()+ "Attrlist.size");
			for(int i = 0; i < attrList.size(); i++){	
				//Log.d("LineGraph1", "datagetgetcounter = " + data.get(0).get(counter));
				if(valuemap.get(latest).size() <= i) break;
				valuemap.get(latest).
				get(i).
				setText(attrList.get(i).toString());
			}
					counter++;
		}
		counter = 0;
		
		
		Iterator<Entry<Integer, ImageView>> it = sensormap.entrySet().iterator();
		while(it.hasNext()){
				Entry<Integer, ImageView> entry = it.next();
				SensorStatusType status;
				try {
					status = Backend.getInstance().getSensorById(entry.getKey()).getSensorStatus();
					Log.d("statusmap", "" + status);
					//status = SensorDataHolder.getInstance().getSensorWorking(entry.getKey());
					//SensorDataHolder.getInstance().setSensorWorking(entry.getKey(), status);
					if (status == SensorStatusType.WORKING)// || status == null) 
						
						entry.getValue().setImageResource(R.drawable.greenstatus); 
					else entry.getValue().setImageResource(R.drawable.redstatus); 
				} catch (BackendError e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		}
				//SensorDataHolder.getInstance().setSensorWorking(entry.getKey(), status);
				//Backend.getInstance().getSensorById(entry.getKey()).activeOnScreen(true);
			//	Log.d("StatusFragment", ((Sensor)(Backend.getInstance().getSensorById(entry.getKey()))).toString());
				//TODO: THIS BELOW

				//	Log.i("StatusFragment", ((Integer)valuemap.get(entry.getKey()).size()).toString());
				//	valuemap.get(entry.getKey()).get(0).setText(((Integer)test).toString());

			//	if (data.isEmpty()) return;
				//if(valuemap.get(Backend.getInstance().getSensorById(entry.getKey())).get(0).getVisibility()==View.VISIBLE)
					//for(int i = 0; i < Backend.getInstance().getSensorById(entry.getKey()).getAttributesName().length; i++)
		


				//TODO: Kolla denna grej med fler sensorer.
				


		
		
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
	}

}
