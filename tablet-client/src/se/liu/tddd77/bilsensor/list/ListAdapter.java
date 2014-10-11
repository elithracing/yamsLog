package se.liu.tddd77.bilsensor.list;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import protobuf.Protocol.SensorStatusMsg.AttributeStatusType;
import protobuf.Protocol.SensorStatusMsg.SensorStatusType;
import se.liu.tddd77.bilsensor.R;
import se.liu.tddd77.bilsensor.SensorDetailFragment;
import se.liu.tddd77.bilsensor.dialogs.RemoveViewDialog;
import se.liu.tddd77.bilsensor.dialogs.RemoveViewDialog.RemoveViewListener;
import se.liu.tddd77.bilsensor.graphs.GraphContainer;
import Database.Sensors.Sensor;
import Errors.BackendError;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

//TODO: Reimplement sensor status colors
public class ListAdapter extends ArrayAdapter<ListElement<?extends Fragment>> implements RemoveViewListener {

	public interface ListItemListener{
		public void itemSelected(int i);	
		boolean removeView(int pos, ListAdapter listAdapter);
	}
	public boolean error = false;
	private boolean alwaysDrawSelected = true;
	private boolean drawSelected = alwaysDrawSelected;
	private int selected = 0;
	private boolean drawDown = false;
	private int down = 0;
	private LayoutInflater inflater;
	private EnumMap<SensorStatusType, Integer> colorMap = new EnumMap<SensorStatusType, Integer>(SensorStatusType.class);
	private List<ListElement<? extends Fragment>> data;

	private int white = Color.WHITE;
	private int red = Color.rgb(255, 28, 28);
	private int lightRed = Color.rgb(255, 120, 120);
	private int yellow = Color.YELLOW;
	private int lightYellow = Color.rgb(255, 255, 165);
	private int gray = Color.rgb(200, 200, 200);
	private int darkGray = Color.rgb(150, 150, 150);
	private ListItemListener mListener;
	public boolean warning = false;
	private static int pos;
	private ListAdapter listAdapter;
	/**
	 * Anonymous class for speeding up updates
	 */
	private static class ViewHolder{
		public TextView text;
		public LinearLayout background;
		public Button removeViewButton;
	}

	/**
	 * Create the list and initiate its data. 
	 * It automatically assumes that the first element is selected. Also adds 
	 * the colors to the map for showing the status of the sensors. 
	 * @param context The context in which the list is created. 
	 * @param data The data to represent. 
	 * @param listener The listener to inform when a item has been selected
	 */
	public ListAdapter(Context context, List<ListElement<? extends Fragment>> data, ListItemListener listener) {
		super(context, R.layout.view_list_item,
				android.R.id.text1, data);
		listAdapter = this;
		this.data = data;
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mListener = listener;
		colorMap.put(SensorStatusType.WORKING, white); 
		colorMap.put(SensorStatusType.NOT_WORKING, red);
	}

	/**
	 * How to represent each element in the list. 
	 * The list has a background that is the same as the text unless the
	 * sensor is broken, in which case it takes the color from the map. 
	 * Above the background there is a TextView which has the name of the 
	 * sensor and takes on a different color depending on if the element 
	 * is up, down or selected. 
	 */
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// Create a view or reuse the last one
		// Using a holder to speed up the creation.
		pos = position;
		ViewHolder holder;
		if(convertView == null){

			holder = new ViewHolder();

			if(position==0){
				convertView = inflater.inflate(R.layout.list_item, parent, false);
				holder.text = (TextView) convertView.findViewById(R.id.list_item_text);
				holder.background= (LinearLayout) convertView.findViewById(R.id.list_item_background);

			}else{
				convertView = inflater.inflate(R.layout.view_list_item, parent, false);
				holder.removeViewButton = (Button) convertView.findViewById(R.id.removeviewbutton);
				holder.text = (TextView) convertView.findViewById(R.id.viewnametext);
				holder.background= (LinearLayout) convertView.findViewById(R.id.listitembackground);

			}

			convertView.setTag(holder);
		}
		else{
			holder = (ViewHolder) convertView.getTag();
		}
		if(position!=0){
			convertView.findViewById(R.id.removeviewbutton).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

				((ListItemListener) mListener).removeView(position, listAdapter);

				}
			});
		}
		int color;

		// Text coloring
		color = changeBackgroundColor(position);
		holder.text.setBackgroundColor(color);
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ListAdapter.this.setActivatedPosition(position);
				Log.d("ListAdapter", "getView: " + position + " clicked");
				mListener.itemSelected(position);
			}
		});

		if(!data.isEmpty()){
			holder.text.setText(data.get(position).toString());
		}




		if(error){
			color = red;
		} else if(warning){
			color = yellow;
		} else {
			color = gray;
		}
		holder.background.setBackgroundColor(color);

		return convertView;
	}

	private int changeBackgroundColor(int position){
		try{
			SensorDetailFragment sensorFragment = (SensorDetailFragment)data.get(position).fragment;
			List<Sensor> sensors = sensorFragment.getSensors();
			for(Sensor s : sensors){

				if(s.getSensorStatus() == SensorStatusType.NOT_WORKING){
					if(position == selected){
						return lightRed;
					}
					else return red;
				} else {
					//TODO: Kollar bara på noll:te elementet. Eftersom vi bara har en graf så funkar det, om vi implementerar
					//fler grafer så måste alla element loopas igenom.
					for(int index: sensorFragment.getGraphs().get(0).yId.get(0)){
						if(s.getAttributeStatuses()[index-1] == AttributeStatusType.OUTSIDE_LIMITS){
							if(position == selected){
								return lightYellow;
							} else return yellow;
						}
					}
					if(position== selected){
						return white;
					} else {
						return darkGray;
					}
				}
			}

		}
		catch(ClassCastException e){

		}
		if(position == selected){
			return white;
		} else {
			return darkGray;
		}

	}

	/**
	 * Down means the button is pressed, but not yet released. 
	 * @param position The index of the button being pressed down
	 */
	public void down(int position){
		down = position;
		drawDown = true;
		//System.out.println("ListAdapter - down: " + position);
	}

	/**
	 * The previously pressed button is released, becoming selected
	 */
	public void release(){
		//System.out.println("ListAdapter - release: " + selected);
		selected = down;
		drawSelected = alwaysDrawSelected;
		drawDown = false;
	}

	/**
	 * Set the element being selected. (Does not change anything outside the list) 
	 * @param position The index of the button to select. 
	 */
	public void setActivatedPosition(int position) {
		//System.out.println("ListAdapter - setActivatedPosition: " + position);
		selected = position;
		drawSelected = alwaysDrawSelected;
		// You need to call this to redraw the list
		// It will notice that data hasn't changed and handle it nicely
		super.notifyDataSetChanged();
	}

	List<Integer> list;	
	List<GraphContainer> tmpGraphList;
	@Override

	public List<Integer> deleteView() {
		list = new ArrayList<Integer>();
		
		mListener.itemSelected(pos-1);
		SensorDetailFragment fragment = (SensorDetailFragment) ElementList.getInstance().getElements().get(pos).getFragment();
		tmpGraphList =  fragment.getGraphs();
		for (int i = 0; i < fragment.getGraphs().size(); i++){
				list.addAll(tmpGraphList.get(i).sensorIds);
		}
		ElementList.getInstance().elementList.remove(pos);
		//int a = (int) ElementList.getInstance().elementList.get(pos).sensor.getID();
		ListAdapter.this.notifyDataSetChanged();
		return list;
	}

}
