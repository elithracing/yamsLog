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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import protobuf.Protocol.SensorStatusMsg.SensorStatusType;

import se.liu.tddd77.bilsensor.dialogs.ConfigDialog;
import se.liu.tddd77.bilsensor.dialogs.ConfigDialog.previousValsListener;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class LineGraph extends SensorGraph implements previousValsListener {

	// The offset by which to subtract x by
	// We start drawing from the right, whereas 0 is left
	private int xOffset;
	private int xIndexOffset;
	private boolean xIndexOffsetChanged = false;
	// Since y starts from the bottom we need to subtract this
	private int yOffset;
	private float scaleFactor = 1.f;
	private int startIndex; // The index to the rightmost visible data
	private int stopIndex;
	private boolean autoScaleX = false;
	private boolean autoScaleY = true;
	private float xZoom = 1000;
	private float yZoom = 1 / 20;
	private int width = 0;
	private int height = 0;
	private GraphContainer parent;
	private ScaleGestureDetector mScaleDetector;
	private GestureDetector mGestureDetector;
	private int countHelp = 0;
	private List<ArrayList<Float>> yData;
	private List<Float> xData;
	private ArrayList<Pair<Integer, Float>[]> minList = new ArrayList<Pair<Integer, Float>[]>(), maxList = new ArrayList<Pair<Integer, Float>[]>();

	private float[] newY;
	private float[] oldY;
	private float printMaxY;
	private float printMinY;
	private Paint axisPaint;
	//private static boolean[] formerVals;
	private Paint axisValuesPaint;
	private ArrayList<Paint> paints;

	private Pair<Integer, Float> min[], max[];
	float[] positionOfAxis = null;
	float[] positionOfNumbers = null;
	private int xIndexOffsetSnap = 0;
	private Paint legendPaint;
	private Paint legendBorderPaint;
	private int yellow = Color.rgb(255, 220, 0);
	private List<ArrayList<Float>> data;
	private int activeSensorId;
	private Paint disconnectedPaint;
	private ArrayList<Integer> activeAttributesIds = new ArrayList<Integer>();;
	//private List<List<Number>> active;


	public LineGraph(Context context, AttributeSet attr)  {
		super(context, attr);
		
		this.xIndexOffset = 0;
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		mGestureDetector = new GestureDetector(context, new ScrollListener());
		this.axisPaint = new Paint(Color.BLACK);
		this.axisPaint.setStrokeWidth(5);
		this.axisValuesPaint = new Paint(Color.BLACK);
		this.axisValuesPaint.setTextSize(30);
		// Log.d("LineGraph","Wrong constructor");

		legendPaint = new Paint();
		legendPaint.setTextSize(35);
		legendPaint.setColor(Color.BLACK);
		legendBorderPaint = new Paint();
		legendBorderPaint.setColor(Color.BLACK);
	
		disconnectedPaint = new Paint();
		disconnectedPaint.setTextSize(120);
		disconnectedPaint.setColor(Color.BLACK);

		// this.setFormerVals(new boolean[2]);

	}
	private int sensorAmount;
	public void setParent(GraphContainer parent) {
		this.parent = parent;
		sensorAmount =  this.parent.sensorIds.size();

		
		this.previous = parent.previous;
		if(!previous[2]){
		this.autoScaleY = false;
		printMinY = parent.prevMin;
		printMaxY = parent.prevMax;
		}

		
		for(int k = 0; sensorAmount > k; k++){
			for(int l = 0; this.parent.sensorIds.get(k) > l; l++){
				;
			}
		}

		//for(int i = 0; sensorAmount > i; i++){
		//parent.sensor.get(0).activeOnScreen(true);
		for(int  i = 0; this.parent.yId.size() > i; i++){
			maxList.add(new Pair[this.parent.yId.get(i).size()]);
			minList.add(new Pair[this.parent.yId.get(i).size()]);
			for (int j = 0; j < this.parent.yId.get(i).size(); j++) {
				maxList.get(i)[j] = new Pair<Integer, Float>(-1, Float.MIN_VALUE);
				minList.get(i)[j] = new Pair<Integer, Float>(-1, Float.MAX_VALUE);
			} 
		}
		//		this.max = new Pair[this.parent.yId.size()];
		//		this.min = new Pair[this.parent.yId.size()];

	}
	//SensorDataHolder sensorDataHolder;
	// Traversing in reverse


	private void updateSize() {
		if (this.width == 0 || this.height == 0) {
			forceUpdateSize();
		}
	}

	private void forceUpdateSize() {
		parent.getParent();
		// Log.d("LineGraph","Width: " + this.getWidth());
		this.width = this.getWidth();
		// Log.d("LineGraph","Height: " + this.getHeight());
		this.height = this.getHeight();
		positionOfAxis = new float[] {//TODO: Add some comments on where is what
				(float) Math.floor(width * 0.95),
				(float) Math.floor(height * 0.97),
				//bottom right x
				(float) Math.floor(width * 0.07),
				(float) Math.floor(height * 0.02),
				(float) Math.floor(width * 0.01),
				(float) Math.floor(height * 0.96),
				(float) Math.floor(width * 0.92),
				(float) Math.floor(width * 0.96) };

		positionOfNumbers = new float[] {
				(float) Math.floor(width * 0.95),
				(float) Math.floor(height * 0.9),
				(float) Math.floor(width * 0.02),
				(float) Math.floor(height * 0.08),
				(float) Math.floor(width * 0.01),
				(float) Math.floor(height * 0.98),
				(float) Math.floor(width * 0.96),
				(float) Math.floor(width * 0.96) };


	}


	//	private List<ArrayList<Float>> data;
	//	private int activeSensorId;
	//	private ArrayList<ArrayList<Integer>> activeData;

	// Traversing in reverse
	private static Semaphore readWriteSema = SensorDataHolder.getInstance().getReadWriteSema();;
	@Override
	public void draw(Canvas canvas) {
		if (parent == null) {
			return;
		}
		forceUpdateSize();
		//parent.sensor.startReading(); //TODO: REMOVE?
		// if(x.size()-1-xIndexOffset<1) xIndexOffset = xData.size();
		doneDrawing = false;

		try {
			if(!readWriteSema.tryAcquire()) return;


			for (int i = 0; i < parent.sensorIds.size(); i++){
				activeSensorId = parent.sensorIds.get(i);
				SensorStatusType checkStatus = SensorDataHolder.getInstance().getSensorWorking(activeSensorId);
				//if(checkStatus != SensorStatusType.NOT_WORKING) 
				//colorBackGround(canvas, checkStatus);
				activeAttributesIds.clear();
				activeAttributesIds.add(parent.xId);
				activeAttributesIds.addAll(parent.yId.get(i)); //Adds all y for that graph
				//activeAttributesIds = append(paparent.yId.get(i));
				data = SensorDataHolder.getInstance().getDataForDrawing(activeSensorId, activeAttributesIds);
				min = minList.get(i);
				max = maxList.get(i);
				updateMinMax(data);
				paints = parent.getPaints(activeSensorId);
				if(!data.isEmpty()){
					colorBackGround(canvas, checkStatus);
					setDefaultSettings(data.size()-1); //One attribute is x.
					drawSet(canvas);
					drawLegend(canvas);
					}
					if(!SensorDataHolder.getInstance().getConnectedToServer()){
						canvas.drawText("RECONNECTING TO SERVER", 0, 22, 150f, 200f, disconnectedPaint);
					}
				}
			drawAxis(canvas);
			doneDrawing = true;
			blockedByPrevious = false;
			readWriteSema.release();
		


		} catch (ArrayIndexOutOfBoundsException e) {
			//readWriteSema.release();
			e.printStackTrace();
		}
	}
	boolean blockedByPrevious = false;
	private void colorBackGround(Canvas canvas, SensorStatusType checkStatus) {
		if(checkStatus == SensorStatusType.NOT_WORKING){
			canvas.drawColor(Color.RED);
			blockedByPrevious = true;
		} else if(!blockedByPrevious)
			canvas.drawColor(Color.WHITE);
		
		
	/*	(checkStatus == SensorStatusType.OUTSIDE_LIMITS){
			canvas.drawColor(Color.YELLOW);
			break;
		}*/
		/*if(checkStatus == SensorStatusType.WORKING){
			canvas.drawColor(Color.WHITE);	
		}*/

	}
	boolean changedTop, changedBottom = false;
	int sensorId;
	private void updateMinMax(List<ArrayList<Float>> data) throws ArrayIndexOutOfBoundsException {


		//for (int i = 0; i < parent.sensorIds.size(); i++){
			/*activeSensorId = parent.sensorIds.get(i);
			activeAttributesIds.clear();
			activeAttributesIds.add(parent.xId);
			activeAttributesIds.addAll(parent.yId.get(i));
			data = SensorDataHolder.getInstance().getDataForDrawing(activeSensorId, activeAttributesIds);*/
			xData = data.get(0);
			yData = data.subList(1, data.size());
			
			int endIndex = xIndexOffset == 0 ? xData.size() : xIndexOffsetSnap;
			int saveIndex = 0;
			for (ArrayList<Float> tmpData : yData) {
				//for(int i = 0; i < y.size(); i++){


				// if(min[saveIndex].first < stopIndex) min[saveIndex].second =
				// Float.MAX_VALUE; if(max[saveIndex].first < stopIndex)
				// max[saveIndex].second = Float.MIN_VALUE;

				// for(int tempIndex 
				// Math.max(0,(min[saveIndex].first>stopIndex||!xIndexOffsetChanged?startIndex-xIndexOffset:stopIndex-xIndexOffset));
				// tempIndex < endIndex; tempIndex++){
				
				for (int tempIndex = stopIndex; tempIndex < endIndex && tempIndex < tmpData.size(); tempIndex++) {

					if (tmpData.get(tempIndex).floatValue() < min[saveIndex].second+0.1f){
						min[saveIndex].first = tempIndex;
						min[saveIndex].second = tmpData.get(tempIndex).floatValue();
						changedBottom = true;
					}
				}
				// for(int tempIndex =
				// Math.max(0,(max[saveIndex].first>stopIndex||!xIndexOffsetChanged?startIndex-xIndexOffset:stopIndex-xIndexOffset));
				// tempIndex < endIndex; tempIndex++){
				for (int tempIndex = stopIndex; tempIndex < endIndex && tempIndex < tmpData.size(); tempIndex++) {
					if (tmpData.get(tempIndex).floatValue() > max[saveIndex].second-0.1f) {
						max[saveIndex].first = tempIndex;
						max[saveIndex].second = tmpData.get(tempIndex).floatValue();
						changedTop = true;
					}
				}
			}
			
			if(!changedBottom) min[saveIndex].second += 0.01f*Math.abs(min[saveIndex].second);
			if(!changedTop) max[saveIndex].second -= 0.01f*max[saveIndex].second;
			//saveIndex++;


			// Pair<Integer, Float>[] temp = max;
			// max = min;
			// min = temp;
			xIndexOffsetChanged = false;
		
		changedBottom = false;
		changedTop = false;
		//activeAttributesIds.clear();
	}
	ArrayList<Float> checkList = null;
	//Paintset in?
	private void drawSet(Canvas canvas) {

		if (xData.isEmpty()) {
				return;
		}
		int leftAdjustment = (int) Math.floor(this.width * 0.05);
		float xZoom = this.xZoom * this.scaleFactor;
		// startIndex = xData.size()-1-xIndexOffset;
		startIndex = (xIndexOffset == 0 ? xData.size() - 1 : xIndexOffsetSnap);
		//Log.d("LineGraph1", "xData.size is "+ xData.size());
		if (startIndex < 0)
			return;
		if (autoScaleY) {
			printMinY = getGlobalMinY();
			printMaxY = getGlobalMaxY();
			// Sets up for scaling
			float diff = printMaxY - printMinY;
			if (diff == 0) {
				printMaxY += 0.05;
				printMinY -= 0.05;
			} else { // This gives a ~5% spacing, both up and down.
				printMaxY += diff / 7;
				printMinY -= diff / 7;
			}
		}
		// SETUP
		float newX = 0;

		if(xData.size() > startIndex)
			newX = (xData.get(startIndex) * xZoom);
		float oldX;

		xOffset = (int) (width - newX);

		newX = width;
		//Log.i("LineGraph1", "startindex = " + startIndex);

		// Gets the first y for all attributes in y.
		for (int yIndex = 0; yIndex < yData.size(); yIndex++){
			if(xData.size() > startIndex)
				//Log.d("LineGraph1", "xDataSize, yDataSize, yIndex "+  xData.size() + " "+ yData.size() + " " + yIndex );
				checkList =  yData.get(yIndex);
			if(checkList.size() >= xData.size())
				newY[yIndex] = calculateY(checkList.get(startIndex));
		}
		//Log.d("LineGraph1", "printMaxY " + printMaxY);
		//Log.d("LineGraph1", "printMinY "+ printMinY);
		if(xData.size() > startIndex)
			for (int listIndex = startIndex - 1; listIndex >= 0; listIndex -= 1) {
				// The old new values are now old
				oldX = newX;
				for (int yIndex = 0; yIndex < newY.length; yIndex++) {
					oldY[yIndex] = newY[yIndex];
				}

				// Transform value of X to local position

				newX = ((xData.get(listIndex).floatValue() * xZoom) + xOffset);

			
				// Transform values of all Y to local position
			
				for (int yIndex = 0; yIndex < yData.size(); yIndex++) {
					if(listIndex < yData.get(yIndex).size()){
					newY[yIndex] = calculateY(yData.
							get(yIndex).
							get(listIndex));
					}else{ break;}

				}

				// Puts oldY.length lines between oldX and newx with different y
				for (int i = 0; i < oldY.length; i++){

					canvas.drawLine(oldX - leftAdjustment, oldY[i], newX
							- leftAdjustment, newY[i], paints.get(i));
				}

				if (newX < 0) {
					stopIndex = listIndex;
					break;
				}
				countHelp++;
			}

	}

	private void drawLegend(Canvas canvas) {
		countHelp = 0;
		canvas.drawLine(0, 0, width,
				0, legendBorderPaint);
		canvas.drawLine(0, 0, 0,
				height-0, legendBorderPaint);
		canvas.drawLine( 0, height-0, width-0,
				height-0, legendBorderPaint);
		canvas.drawLine(width-3, 0, width-3,
				height-0, legendBorderPaint);

		for(int j = 0; j < sensorAmount; j++){
			for (int i = 0; i < parent.yId.get(j).size(); i++) {

				canvas.drawRect(width - 350, height - 40 * countHelp - 200, width - 310,
						height - 40 * countHelp - 170, parent.getPaints(parent.sensorIds.get(j)).get(i));//paints[i+j*(parent.yId.get(j).size()-1)]);
				canvas.drawText(
						SensorDataHolder.getInstance().getAttributeName(parent.yId.get(j).get(i), parent.sensorIds.get(j)),
						width - 300, height - 40 * countHelp - 175, legendPaint);
				countHelp++;//TODO: fix this
			}
		}
		countHelp = 0;
	}

	private void setDefaultSettings(int ySize) {

		this.newY = new float[ySize];
		this.oldY = new float[ySize];

	}



	private float getGlobalMinY() {
		float returnVal = Float.MAX_VALUE;
		for (Pair<Integer, Float>[] hoj : minList){
			min = hoj;
		for (Pair<Integer, Float> val : min)
			returnVal = Math.min(val.second, returnVal);
		}
		return returnVal;

	}

	private float getGlobalMaxY() {
		float returnVal = Float.MIN_VALUE;
		for (Pair<Integer, Float>[] hoj : maxList){
			max = hoj;
		for (Pair<Integer, Float> val : max)
			returnVal = Math.max(val.second, returnVal);
		}
		return returnVal;
	}




	private float calculateY(Number yValue) {
		return calculateY(yValue.floatValue());
	}

	private float calculateY(float yValue) {
		
		return (float) (height * (1 - (yValue -printMinY) / (printMaxY - printMinY)));
		//: (float) (height - yValue));
	}
	String botRight;
	String botLeft;
	private void drawAxis(Canvas canvas) {
		canvas.drawLine(positionOfAxis[0],
				(int) calculateY(0), positionOfAxis[2],
				(int) calculateY(0), axisPaint); // X-line
		canvas.drawLine(positionOfAxis[0],
				positionOfAxis[1], positionOfAxis[0],
				positionOfAxis[3], axisPaint); // Y-line
		/*
		 * canvas.drawText(xData.get(stopIndex).toString(),
		 * (float)Math.floor(width*0.01), (float)Math.floor(height*0.99),
		 * axisValuesPaint); canvas.drawText(xData.get(startIndex).toString(),
		 * (float)Math.floor(width*0.92), (float)Math.floor(height*0.99),
		 * axisValuesPaint); canvas.drawText(((Float)printMaxY).toString(),
		 * (float)Math.floor(width*0.96), (float)Math.floor(height*0.02),
		 * axisValuesPaint); canvas.drawText(((Float)printMinY).toString(),
		 * (float)Math.floor(width*0.96), (float)Math.floor(height*0.97),
		 * axisValuesPaint);
		 */

		DecimalFormat df = new DecimalFormat("##.#");
		df.setMinimumFractionDigits(1);
		df.setRoundingMode(RoundingMode.DOWN);
		if(getPrevious()[0]){
			if(stopIndex < xData.size() && startIndex < xData.size()){

				botRight = df.format(xData.get(startIndex));
				botLeft = df.format(xData.get(stopIndex));

				//Botright
				canvas.drawText(botRight,
						positionOfNumbers[6], positionOfNumbers[5],
						axisValuesPaint);
				//BotLeft
				canvas.drawText(botLeft,
						positionOfNumbers[4], positionOfNumbers[5],
						axisValuesPaint);		
			}else{		canvas.drawText("0".toString(),
					positionOfNumbers[4], positionOfNumbers[5],
					axisValuesPaint);	
			canvas.drawText("0",
					positionOfNumbers[6], positionOfNumbers[5],
					axisValuesPaint);
			}
		}
		if(getPrevious()[1]){

			canvas.drawText(((Float) printMaxY).toString(),
					positionOfNumbers[7], positionOfNumbers[3],
					axisValuesPaint);
			canvas.drawText(((Float) printMinY).toString(),
					positionOfNumbers[7], positionOfNumbers[1],
					axisValuesPaint);
		}
	}

	private boolean doneDrawing = true;
	@Override
	public void updateData() {

		if (doneDrawing)
			this.invalidate();
		// Log.i("LineGraph", "invalidated");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mScaleDetector.onTouchEvent(event);
		mGestureDetector.onTouchEvent(event);
		return true;
	}


	private class ScaleListener extends
	ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			if(!readWriteSema.tryAcquire()) return false;
			scaleFactor *= detector.getScaleFactor();
			// don't let the object get too small or too large.
			scaleFactor = Math.max(0.03f, Math.min(scaleFactor, 7.0f));
			invalidate();
			readWriteSema.release();
			return false;
		}
	}

	private class ScrollListener extends
	GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if(!readWriteSema.tryAcquire()) return false;
			// Log.d("onScroll","X: " + distanceX + " Y: " + distanceY);
			if (xIndexOffset == 0) {
				int tempXIndexOffset = xIndexOffset;
				xIndexOffset = Math.max(xIndexOffset - (int) distanceX, 0);
				xIndexOffsetChanged = tempXIndexOffset == xIndexOffset;
				xIndexOffsetSnap = xData.size()
						- xIndexOffset;
			} else {
				xIndexOffsetChanged = true;
				xIndexOffsetSnap = Math.min(
						xData.size() - 1,
						Math.max(xIndexOffsetSnap + (int) distanceX, 0));
			}
			readWriteSema.release();
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {


			DialogFragment configDialog = new ConfigDialog();
			((lineGraphListener) configDialog).setView((LineGraph) parent.child);
			configDialog.show(parent.getFragmentManager(), "dialog");
			/// TODO: functionallity not implemented yet, currently crashing
			// of unknown reason
		}
		//parent.sensor.get(0).getList(0).size()
		//						- xIndexOffset;
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if(!readWriteSema.tryAcquire()) return false;
			// Log.d("onFling","velX: " + velocityX);
			if (velocityX < -10000) { // go to the newest values.
				xIndexOffset = 0;
				xIndexOffsetChanged = true;
				xIndexOffsetSnap = xData.size()-xIndexOffset;
			} else if (velocityX > 10000) { // Go to the oldest values
				//parent.sensor.get(0).startReading();
				xIndexOffset = xData.size() - 1;
				xIndexOffsetSnap = 0;
				xIndexOffsetChanged = true;
				xIndexOffsetSnap = xData.size()
						- xIndexOffset;

			}
			readWriteSema.release();
			return false;
		}  //TODO: Remake.
	}

	public void clear(){
		try {
			readWriteSema.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if( yData != null){
			for(int i = 0; yData.size() > i; i++)

				yData.get(i).clear();

			xData.clear();
			/*minList = new ArrayList<Pair<Integer, Float>[]>();
			maxList = new ArrayList<Pair<Integer, Float>[]>();
			width = 0;
			height = 0;
			xZoom = 1000;*/
			//Zoom = 1 / 20;
		}
		readWriteSema.release();
	}

	public interface lineGraphListener{
		void setView(LineGraph lineGraph);
	}

	//**********CONFIGDIALOG-STUFF**************//
	private boolean[] previous;// = new boolean[]{true, true, true};

	@Override
	public boolean[] getPrevious() {
		// TODO Auto-generated method stub
		//if(previous != null)
		return previous;
		//previous = new boolean[]{true, true, true};
		//return previous;
	}


	int xSet, ySet, ySetLow;

	@Override 
	public void setPrevious(boolean numerical, boolean minmax,
			boolean autoScale, String x, String y, String yLow) {
		previous[0] = numerical;
		previous[1] = minmax;
		previous[2] = autoScale;
		parent.previous = previous;
		
		autoScaleY = autoScale;
		//Decision was made to only change xZoom with pinches.
		/*	if (!x.contentEquals("")){
			xSet = Integer.valueOf(x);
			if(xSet != 0)
			//xZoom = 8800-(xSet*400);
		}*/
		if(!y.contentEquals("")) {
			ySet = Integer.valueOf(yLow);
			parent.prevMax = ySet;
			if(!autoScaleY) printMaxY = ySet;
		}
		if(!yLow.contentEquals("")) {
			ySetLow = Integer.valueOf(y);
			parent.prevMin = ySetLow;
			if(!autoScaleY) printMinY = ySetLow;

		}
	}




	//**********END CONFIGDIALOG-STUFF************//

}
