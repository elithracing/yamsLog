package se.liu.tddd77.bilsensor.graphs;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;


public abstract class SensorGraph extends View /*implements OnScaleGestureListener*/{
	protected List<Integer> yThisIsNeverUsed = new ArrayList<Integer>();
	protected int xWhatIsThis;
	public SensorGraph(Context context){
		super(context);
	}
	


	public SensorGraph(Context context, AttributeSet attr){
		super(context, attr);
	}
	
	
	@Override
	public abstract void draw(Canvas canvas);
	public abstract void setParent(GraphContainer parent);
	public abstract void updateData();
	public abstract void clear();
	
	public void addY(int i) {
		// TODO Auto-generated method stub
		
	}
	
	
	public void updateDimension(int newWidth, int newHeight) {
		setMeasuredDimension(newWidth, newHeight);
	}




	

	
}
