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

package se.liu.tddd77.bilsensor.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import se.liu.tddd77.bilsensor.R;
import se.liu.tddd77.bilsensor.detail.SensorChangeListener;
import se.liu.tddd77.bilsensor.graphs.addGraph.GraphType;
import Database.Sensors.Sensor;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GraphContainer extends LinearLayout implements Tickable,
		SensorChangeListener {

	private TextView xLabel;
	private TextView yLabel;
	public SensorGraph child;
	private GraphType type;
	private boolean drawXLabel = false;
	private boolean drawYLabel = false;
	// TODO: Change to private
	public ArrayList<Integer> sensorIds;
	private FragmentManager fragmentManager;
	public Integer xId;
	public ArrayList<ArrayList<Integer>> yId;
	public boolean[] previous = new boolean[3];// { true, true, true };
	public int prevMin = -5, prevMax = 5;
	private HashMap<Integer, ArrayList<Paint>> paintMap = new HashMap<Integer, ArrayList<Paint>>();

	int setIdChild = 0;
	private Paint[] paints;
	ArrayList<Paint> paintList = new ArrayList<Paint>();

	public GraphContainer(Context context, GraphType type,
			List<Integer> sensorIds, int x, ArrayList<ArrayList<Integer>> y,
			LinearLayout layout, FragmentManager fm, boolean[] prev, int prevMin, int prevMax) {
		super(context);
		// previousList.add(new boolean[]{true, true, true});
		this.prevMin = prevMin;
		this.prevMax = prevMax;
		this.type = type;
		this.previous = prev;
		
		this.setOrientation(LinearLayout.VERTICAL);
		Log.i("GraphContainer", "sensor " + sensorIds);
		this.sensorIds = (ArrayList<Integer>) sensorIds;
		// Log.d("GraphContainer", "xLabel: " +
		// sensor.get(0).getAttributesName()[x]);
		/*
		 * for(int i : y){ Log.d("GraphContainer", "yLabel: " +
		 * sensor.get(0).getAttributesName()[i]); }
		 */
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.fragmentManager = fm;

		this.paints = new Paint[50];
		// int r, g, b;
		for (int i = 0; i < paints.length; i++) {
			paints[i] = new Paint();
			paints[i].setStrokeWidth(5);
			paints[i].setAlpha(255);
			paints[i].setAntiAlias(false);
			paints[i].setColor(Color.rgb(i % 3 == 0 ? (110 * (i + 1)) % 256
					: 50 * i % 247, i % 3 == 1 ? (108 * (i)) % 256
					: 50 * i % 250, i % 3 == 2 ? (100 * (i - 1)) % 256
					: 5050 * i % 220));
			// paints[i].setColor(Color.rgb(r, g, b));

		}
		paints[0].setColor(Color.YELLOW);
		paints[1].setColor(Color.BLUE);
		paints[2].setColor(Color.GREEN);
		paints[3].setColor(Color.RED);
		paints[4].setColor(Color.MAGENTA);
		paints[5].setColor(Color.CYAN);
		paints[6].setColor(Color.LTGRAY);
		// setDefaultSettings();
		for (int i = 0; i < sensorIds.size(); i++) {
			for (int j = 0; j < y.get(i).size(); j++) {
				paintList.add(paints[j + i * y.get(i).size()]);
			}
			paintMap.put(sensorIds.get(i), paintList);
			paintList = new ArrayList<Paint>();
		}

		/*
		 * this.xLabel = new EditText(getContext()); this.xLabel.setText("X: " +
		 * sensor.getAttributesName()[x]); this.xLabel.setPadding(widthPadding,
		 * heightPadding, widthPadding, heightPadding); this.yLabel = new
		 * EditText(getContext()); StringBuilder sb = new StringBuilder();
		 * sb.append("Y: "); for(int i : y){
		 * sb.append(sensor.getAttributesName()[i]); sb.append(", "); }
		 * this.yLabel.setText(sb.toString());
		 * this.yLabel.setPadding(widthPadding, heightPadding, widthPadding,
		 * heightPadding);
		 * 
		 * measureSpace();
		 */
		this.xId = x;
		this.yId = y;

		View v = inflater.inflate(R.layout.graph_container, layout);

		// MAGIC{ ///TODO: WHAT IS THIS? FIX
		child = (SensorGraph) v.findViewById(R.id.graph_container_graph);
		this.child.setParent(this);
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int id = (int) (child.getId() + (System.currentTimeMillis() % 900000));
		if (child.getId() != id) {
			child.setId(id);
		} else
			child.setId((int) (id + (System.currentTimeMillis() % 656548))); //Not very beautiful.
	}

	public GraphContainer clone(Context context, LinearLayout layout) {
		return new GraphContainer(context, type,
				(ArrayList<Integer>) sensorIds, xId, yId, layout,
				fragmentManager, previous, prevMin, prevMax);
	}

	public void clear() {
		child.clear();
	}

	int i = 0;

	@Override
	protected void onDraw(Canvas canvas) {
		if (sensorIds.size() != 0) {
			super.onDraw(canvas);
			child.draw(canvas);
			i = 0;
		} else {
			i++;
		}

	}

	public void setDrawXLabel(boolean draw) {
		this.drawXLabel = draw;
	}

	public void setDrawYLabel(boolean draw) {
		this.drawYLabel = draw;
	}

	public void toggleDrawXLabel() {
		this.drawXLabel = !drawXLabel;
	}

	public void toggleDrawYLabel() {
		this.drawYLabel = !drawYLabel;
	}

	public void updateData() {
		if (child != null)
			child.updateData();
	}

	public void changeX(int x) {
		this.xId = x;
		// this.xLabel.setText(sensors.get(0).getAttributesName()[x]);
		// child.changeX(x);
	}

	/*
	 * public void changeY(ArrayList<Integer> yList){ this.yId = yList; for(int
	 * y : this.yId){ this.yLabel.setText(sensor.get(0).getAttributesName()[y]);
	 * } }
	 */

	@Override
	public void tick() {
		if (child != null)
			child.updateData();
	}

	// @Override
	// protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	// int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
	// int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
	//
	// int widthMeasure = MeasureSpec.getSize(widthMeasureSpec);
	// int heightMeasure = MeasureSpec.getSize(heightMeasureSpec);
	//
	// int newWidth = 0;
	// int newHeight = 0;
	//
	// if (widthSpecMode == MeasureSpec.AT_MOST){
	// newWidth = MeasureSpec.makeMeasureSpec(widthMeasure,
	// MeasureSpec.AT_MOST);
	// }
	// else if(widthSpecMode == MeasureSpec.UNSPECIFIED){
	// newWidth = MeasureSpec.makeMeasureSpec(widthMeasure,
	// MeasureSpec.UNSPECIFIED);
	// }
	//
	// if (heightSpecMode == MeasureSpec.AT_MOST){
	// newHeight = MeasureSpec.makeMeasureSpec(heightMeasure,
	// MeasureSpec.AT_MOST);
	// }
	// else{
	// newHeight = MeasureSpec.makeMeasureSpec(heightMeasure,
	// MeasureSpec.UNSPECIFIED);
	// }
	// Log.d("GraphContainer", "Width: " + widthMeasure + ", " +
	// widthMeasureSpec +
	// ". Height: " + heightMeasure + ", " + heightMeasureSpec);
	//
	// setMeasuredDimension(newWidth, newHeight);
	//
	// if(child != null){
	// child.updateDimension(newWidth - widthSpace, newHeight - heightSpace);
	// }
	// }

	@Override
	public void xChanged(int index) {
		this.xId = index;
	}

	/*
	 * @Override public void yAdded(int sensorIndex, int attributeIndex) {
	 * this.yId.get(sensorIndex).add(attributeIndex); }
	 * 
	 * @Override public void yRemoved(int sensorIndex, int attributeindex) {
	 * this.yId.get(sensorIndex).remove((Integer) index); }
	 * 
	 * @Override public void yChanged(ArrayList<Integer> index) { yId.clear();
	 * for(int yId : index){ this.yId.add(yId); } }
	 * 
	 * @Override public void sensorChanged(ArrayList<Sensor> sensor, int x,
	 * int[] y) { this.sensor = sensor; this.xId = x; for(int yId : y){
	 * this.yId.add(yId); } }
	 */

	public void changeGraph(SensorGraph graph) {
		// this.children.get(0) = graph;
		invalidate();
	}

	/*@Override
	public String toString() {
		return "This implementation is outdated (GraphContainer.toString())";// +
																				// sensors.get(0).getSensorName();
																				// //TODO:
																				// Fix
	}*/

	public FragmentManager getFragmentManager() {
		return fragmentManager;
	}

	public ArrayList<ArrayList<ArrayList<Float>>> getGraphSensor() {
		return null; // this.sensorIds;
	}

	@Override
	public void yAdded(int index) {
		// TODO Auto-generated method stub

	}

	@Override
	public void yRemoved(int index) {
		// TODO Auto-generated method stub

	}

	@Override
	public void yChanged(ArrayList<Integer> index) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sensorChanged(ArrayList<Sensor> sensor, int x, int[] y) {
		// TODO Auto-generated method stub

	}

	public ArrayList<Paint> getPaints(int sensorId) {
		return paintMap.get(sensorId);
	}

}
