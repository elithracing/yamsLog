package se.liu.tddd77.bilsensor.list;

import se.liu.tddd77.bilsensor.StatusFragment;
import android.app.Fragment;
import android.util.Log;

public class StatusListElement extends ListElement<StatusFragment>{

	Fragment statusFragment;
	
	public StatusListElement(String name) {
		super(name);
		super.fragment = new StatusFragment();
		Log.d("StatusListElement", "StatusListElement Created");
	}
		
}
