//package se.liu.tddd77.bilsensor.detail;
//
//import java.util.List;
//
//import se.liu.tddd77.bilsensor.R;
//import android.content.Context;
//import android.graphics.Color;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.AdapterView.OnItemSelectedListener;
//
////TODO: Reimplement sensor status colors
//@SuppressWarnings("rawtypes")
//public class ParticipantListAdapter extends ArrayAdapter<String> {
//	
//	public interface ListItemListener{
//		public void itemSelected(int i);
//	}
//	
//	private boolean alwaysDrawSelected = true;
//	private boolean drawSelected = alwaysDrawSelected;
//	private int selected = 0;
//	private boolean drawDown = false;
//	private int down = 0;
//	private LayoutInflater inflater;
////	private EnumMap<SensorStatus, Integer> colorMap = new EnumMap<SensorStatus, Integer>(SensorStatus.class);
//	
//	private ListItemListener mListener;
//	
//	/**
//	 * Anonymous class for speeding up updates
//	 */
//	private static class ViewHolder{
//		public TextView text;
//		public LinearLayout background;
//	}
//	
//	/**
//	 * Create the list and initiate its data. 
//	 * It automatically assumes that the first element is selected. Also adds 
//	 * the colors to the map for showing the status of the sensors. 
//	 * @param context The context in which the list is created. 
//	 * @param data The data to represent. 
//	 * @param listener The listener to inform when a item has been selected
//	 */
//	public ParticipantListAdapter(Context context, List<ListElement> data, ListItemListener listener) {
//		super(context, android.R.layout.simple_list_item_activated_1,
//				android.R.id.text1, data);
//		this.data = data;
//		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		this.mListener = listener;
////		colorMap.put(SensorStatus.NORMAL, white); // Not needed
////		colorMap.put(SensorStatus.MINOR_FAILURE, yellow);
////		colorMap.put(SensorStatus.CRITICAL_FAILURE, red);
//	}
//	
//	/**
//	 * How to represent each element in the list. 
//	 * The list has a background that is the same as the text unless the
//	 * sensor is broken, in which case it takes the color from the map. 
//	 * Above the background there is a TextView which has the name of the 
//	 * sensor and takes on a different color depending on if the element 
//	 * is up, down or selected. 
//	 */
//	@Override
//	public View getView(final int position, View convertView, ViewGroup parent) {
//		// Create a view or reuse the last one
//		// Using a holder to speed up the creation. 
//		ViewHolder holder;
//		if(convertView == null){
//			convertView = inflater.inflate(R.layout.list_item, parent, false);
//			holder = new ViewHolder();
//			holder.text = (TextView) convertView.findViewById(R.id.list_item_text);
//			holder.background= (LinearLayout) convertView.findViewById(R.id.list_item_background);
//			
//			convertView.setTag(holder);
//		}
//		else{
//			holder = (ViewHolder) convertView.getTag();
//		}
//
//		int color;
//		// Text coloring
//		if(drawSelected && position == selected){
//			color = white;
//		}
//		else if(drawDown && position == down){
//			color = gray;
//		}
//		else{
//			color = darkGray;
//		}
//		holder.text.setBackgroundColor(color);
//		convertView.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				ListAdapter.this.setActivatedPosition(position);
//				Log.d("ListAdapter", "getView: " + position + " clicked");
//				mListener.itemSelected(position);
//			}
//		});
//		
//		if(!data.isEmpty()){
//			holder.text.setText(data.get(position).toString());
//		}
//		// Layout coloring
//		//TODO: Ugly
////		try{
////			SensorFragment sensorFragment = (SensorFragment)data.get(position).fragment;
////			Sensor sensor = sensorFragment.sensor;
//////			color = colorMap.get(sensor.status);
////		}
////		catch(ClassCastException e){
////			
////		}
//		holder.background.setBackgroundColor(color);
//		
//		return convertView;
//	}
//	
//	/**
//	 * Down means the button is pressed, but not yet released. 
//	 * @param position The index of the button being pressed down
//	 */
//	public void down(int position){
//		down = position;
//		drawDown = true;
//		System.out.println("ListAdapter - down: " + position);
//	}
//	
//	/**
//	 * The previously pressed button is released, becoming selected
//	 */
//	public void release(){
//		System.out.println("ListAdapter - release: " + selected);
//		selected = down;
//		drawSelected = alwaysDrawSelected;
//		drawDown = false;
//	}
//	
//	/**
//	 * Set the element being selected. (Does not change anything outside the list) 
//	 * @param position The index of the button to select. 
//	 */
//	public void setActivatedPosition(int position) {
//		System.out.println("ListAdapter - setActivatedPosition: " + position);
//		selected = position;
//		drawSelected = alwaysDrawSelected;
//		// You need to call this to redraw the list
//		// It will notice that data hasn't changed and handle it nicely
//		super.notifyDataSetChanged();
//	}
//
//}
