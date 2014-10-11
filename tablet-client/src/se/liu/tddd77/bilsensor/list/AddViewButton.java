package se.liu.tddd77.bilsensor.list;

import android.content.Context;
import android.widget.Button;

public class AddViewButton extends Button implements SelectViewNameDialog.ViewNameListener {

	public interface AddViewListener{
		public void viewAdded(String name);
	}
	
	private AddViewListener mListener;
	
	public AddViewButton(Context context, AddViewListener listener){
		super(context);
		super.setText("Add view");
		this.mListener = listener;
	}

	@Override
	public void nameSelected(String name) {
		mListener.viewAdded(name);
	}
	
}
