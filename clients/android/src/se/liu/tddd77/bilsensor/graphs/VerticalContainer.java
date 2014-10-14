package se.liu.tddd77.bilsensor.graphs;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

public class VerticalContainer extends LinearLayout implements Tickable {

	ArrayList<Tickable> children = new ArrayList<Tickable>();
	
	public VerticalContainer(Context context) {
		super(context);
	}
	
	/**
	 * You can only add Tickable Views to the container.
	 * If the view is not a tickable, this throws an IllegalArgumentException
	 */
	@Override
	public void addView(View v){
		if(!(v instanceof Tickable)){
			throw new IllegalArgumentException("Tried to add normal view to container of tickables");
		}
		Tickable view = (Tickable) v;
		super.addView(v);
		children.add(view);
	}
	
	/**
	 * You can only remove Tickable Views from the container.
	 * If the view is not a tickable, this throws an IllegalArgumentException
	 */
	@Override
	public void removeView(View view){
		if(!(view instanceof Tickable)){
			throw new IllegalArgumentException("Tried to add normal view to container of tickables");
		}
		Tickable tickable = (Tickable) view;
		super.removeView(view);
		children.remove(tickable);
	}
	
	@Override
	public void tick() {
		for(Tickable child : children){
			child.tick();
		}
	}

	
	
}
