package se.liu.tddd77.bilsensor.list;

import se.liu.tddd77.bilsensor.R;
import se.liu.tddd77.bilsensor.SensorDetailActivity;
import se.liu.tddd77.bilsensor.SensorDetailFragment;
import Errors.BackendError;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
//import FrontendConnection.Backend;

/**
 * This activity is a shell containing a fragment. Used by devices with small displays. 
 */
public class SensorListActivity extends FragmentActivity implements
		SensorListFragment.ListEventListener {
	
	private boolean mSplitScreen;

	//TODO: Maybe the selection in the list should not be shown?
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("SensorListActivity - onCreate: ");
		// Figures out if the element is being shown on a small screen
		// Should not be needed, right?
		if (findViewById(R.id.sensor_detail_container) != null) {
			mSplitScreen = true;
		}
	}
	
	/**
	 * Once an element has been selected, get the related fragment and replace 
	 * "sensor detail container" with it. 
	 * @throws BackendError 
	 */
	@Override
	public void onItemSelected(int index){
			Intent detailIntent = new Intent(this, SensorDetailActivity.class);
			detailIntent.putExtra(SensorDetailFragment.ARG_ITEM_ID, index);
			startActivity(detailIntent);
			Log.i("SensorListActivity", "onItemSelected: Not split screen");
	}
	/*
	public void onItemSelected(int index) throws BackendError {
		if (mSplitScreen) {
			Log.i("SensorListActivity","mSplitScreen");
			Bundle arguments = new Bundle();
//			arguments.putString(SensorDetailFragment.ARG_ITEM_ID, Backend.getInstance().getSensor(index).getName());
			Fragment fragment = new SensorDetailFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
					.replace(R.id.sensor_detail_container, fragment).commit();
			System.out.println("SensorListActivity - onItemSelected: Split Screen");

		} else {
			Intent detailIntent = new Intent(this, SensorDetailActivity.class);
			detailIntent.putExtra(SensorDetailFragment.ARG_ITEM_ID, index);
			startActivity(detailIntent);
			System.out.println("SensorListActivity - onItemSelected: Not Split Screen");
		}
	}*/
}
