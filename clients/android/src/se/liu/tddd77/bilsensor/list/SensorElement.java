package se.liu.tddd77.bilsensor.list;

import se.liu.tddd77.bilsensor.SensorDetailFragment;
import android.util.Log;

/**
 * A sensor element is a list element containing a sensor fragment
 */
public class SensorElement extends ListElement<SensorDetailFragment>{

	public SensorElement(String name) {
		super(name);
		super.fragment = new SensorDetailFragment();
		Log.i("SensorElement","Construction");
	}

}
