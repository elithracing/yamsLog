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

package se.liu.tddd77.bilsensor;

import se.liu.tddd77.bilsensor.list.ElementList;
import se.liu.tddd77.bilsensor.list.SensorListActivity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

public class SensorDetailActivity extends FragmentActivity {

	/**
	 * Automatically create layout based on XML-files. 
	 * The activity is just a shell for the fragment, run on smaller screens. 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor_detail);
		
		System.out.println("SensorDetailActivity - onCreate: ");
		if (savedInstanceState == null) {
			// Get the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle arguments = new Bundle();
			Fragment fragment = null;
			try{
				fragment = ElementList.getInstance().getElement(
						getIntent().getIntExtra(SensorDetailFragment.ARG_ITEM_ID, 0)).getFragment();
			}
			catch(IndexOutOfBoundsException e){
				Log.d("SensorDetailActivity", "List out of bounds");
			}
			if(fragment == null){
				Log.d("SensorDetailActivity","fragment == null");
				fragment = new SensorDetailFragment();
			}
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
					.add(R.id.sensor_detail_container, fragment).commit();
		}
		else{
			
		}
	}
	
	/**
	 * Since this is the only part shown, we need to be able to go back. 
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpTo(this, new Intent(this,
					SensorListActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
