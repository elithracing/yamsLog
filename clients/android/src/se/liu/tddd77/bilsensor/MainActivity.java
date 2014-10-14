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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;

import se.liu.tddd77.bilsensor.dialogs.ConfigDialog.previousValsListener;
import se.liu.tddd77.bilsensor.dialogs.DeleteExperimentDialog;
import se.liu.tddd77.bilsensor.dialogs.DeleteExperimentDialog.ConfirmDeleteListener;
import se.liu.tddd77.bilsensor.dialogs.PlaybackMenu;
import se.liu.tddd77.bilsensor.dialogs.PlaybackMenu.PlaybackListener;
import se.liu.tddd77.bilsensor.dialogs.RecordDialog;
import se.liu.tddd77.bilsensor.dialogs.RecordDialog.RecordDialogListener;
import se.liu.tddd77.bilsensor.dialogs.RemoveViewDialog.removeSensorListener;
import se.liu.tddd77.bilsensor.graphs.GraphContainer;
import se.liu.tddd77.bilsensor.graphs.SensorDataHolder;
import se.liu.tddd77.bilsensor.graphs.SensorGraph;
import se.liu.tddd77.bilsensor.graphs.Tickable;
import se.liu.tddd77.bilsensor.graphs.addGraph.AddGraphButton.sensorAddListener;
import se.liu.tddd77.bilsensor.graphs.addGraph.GraphType;
import se.liu.tddd77.bilsensor.list.AltSensorListFragment;
import se.liu.tddd77.bilsensor.list.ElementList;
import se.liu.tddd77.bilsensor.list.ListElement;
import se.liu.tddd77.bilsensor.list.SensorListFragment;
import Database.Messages.ExperimentMetaData;
import Database.Sensors.Sensor;
import Errors.BackendError;
import FrontendConnection.Backend;
import android.R.string;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
//import android.app.Fragment;

// Handles fragments, list fragment
public class MainActivity extends FragmentActivity implements SensorListFragment.ListEventListener, PlaybackListener, RecordDialogListener, ConfirmDeleteListener, sensorAddListener, removeSensorListener{

	//TODO: Move onClick to their respective fragments/activities
	//TODO: Handle life cycle on all activities
	private boolean isRecording = false;
	private boolean mSplitScreen = false;
	private static List<Button> buttonList;
	private String projectname;
	public static List<Integer> activeSensorsIdList;
	private boolean initiated = false;
	private static ImageButton recButton;
	//	private SensorListFragment list;
	private AltSensorListFragment list;
	private List<ListElement<? extends android.app.Fragment>> elementList;
	private Timer updateTimer;
	private Timer queueTimer;
	private Handler handler = new Handler();
	private LinearLayout mLinearLayout;
	private EditText mEditText;
	private String expName, expNotes;
	public static boolean hasBeenCreated;
	public Lock lock;
	int sillyInt = 0;
	private String projectName, experimentName, serverIP;
	private static boolean justConnected, needsReboot, block;
	Thread thread = new Thread()
	{
	    @Override
	    public void run() {
	        try {
	            while(true) {
					try {
						if(!Backend.getInstance().serverIsAlive()){
							needsReboot = true;
							Backend.getInstance().stopConnection();
							SensorDataHolder.getInstance().setConnectedToServer(false);
							Backend.getInstance().connectToServer(serverIP, 2001);
						}
						if(Backend.getInstance().serverIsAlive() && needsReboot)
						//if(isRecording)
						{
							sleep(1000);
							Log.d("statusmap", "IS ALIVE AND REBOOTS");
							needsReboot = false;
							Log.d("cancel", "serverstatus: " + Backend.getInstance().getServerStatus());
							SensorDataHolder.getInstance().setConnectedToServer(true);
							if (Backend.getInstance().getServerStatus() == protobuf.Protocol.StatusMsg.StatusType.IDLE){
								
								if(isRecording){
									Log.d("statusmap", "justConnected true");
									justConnected = true;
									}
								Backend.getInstance().sendSettingsRequestMessageALlSensors(20);
								//handleRecord((ImageButton) findViewById(R.id.loadButton));
								Backend.getInstance().setActiveProject(projectName);
								//Backend.getInstance().renameExperimentRequest(Backend.getInstance().getTemporaryName(), experimentName);
								
								//onDoneRec(null, null);
								
							}else{
								Log.d("cancel", "Setting message");
								sleep(800);
								Backend.getInstance().sendSettingsRequestMessageSelected(20, activeSensorsIdList);
								sleep(800);
								Backend.getInstance().sendSettingsRequestMessageSelected(20, activeSensorsIdList);}
						}
					} catch (BackendError e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                sleep(1000);
	               
	            }
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	    }
	};


	
	private final Runnable updateHandler = new Runnable() {

		@Override
		public void run() {
			sillyInt++;
			try {
				if(sillyInt == 5 && Backend.getInstance().getServerStatus() == protobuf.Protocol.StatusMsg.StatusType.DATA_COLLECTION){
					try {
						loadProfile("crash.txt");
						handleRecord((ImageButton) findViewById(R.id.recButton));
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				if(justConnected && !block){
					block = true;
					Log.d("statusmap", "justConnected gets justice");
					handleRecord((ImageButton) findViewById(R.id.recButton));
					//justConnected = false;
				}
			} catch (BackendError e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//Log.i("MainActivity", "updateHandler: Run");
			android.app.Fragment currentFragment = getFragmentManager().findFragmentById(R.id.sensor_detail_container);
			if(currentFragment instanceof Tickable){
				((Tickable) currentFragment).tick();
			}

			try {

				Backend.getInstance().tick();

				
				/*if(timeout){
					for(Sensor sensor : Backend.getInstance().getSensors()){
						((Sensor) sensor).timeout(5000); //TODO: THIS PART WAS IN CODE!
					}*/
				//}
			} catch (BackendError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//list.tick();
		}
	};



	public MainActivity(){
		super();
	}

	
	private final Runnable queueHandler = new Runnable() {

		@Override
		public void run() {
			//Log.i("MainActivity", "updateHandler: Run");
			try {
				for(Sensor sensor : Backend.getInstance().getSensors()){
					Backend.getInstance().handleQueue(sensor.getId());
				}
			} catch (BackendError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	
	
	


	/**
	 * Creates the main activity.
	 * As this is the main class for the application, this initiates all 
	 * required fields. It inflates the layout according to the XML-files and  
	 * checks how to display certain items, among other things. 
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(initiated) return;
		initiated = true;
		super.onCreate(savedInstanceState);
		Log.i("MainActivity", "onCreate: ");
		thread.start();
		//TODO: Works by magic, make actual implementation. Is this supposed to be used?
		//if(findViewById == null) Log.d("MainActivity", "Twopane is null lol");
		setContentView(se.liu.tddd77.bilsensor.R.layout.activity_sensor_twopane); //Changed from activity_sensor_list
		activeSensorsIdList = new ArrayList<Integer>();
		hasBeenCreated = true;
		
		// Split screen
		if (findViewById(R.id.sensor_detail_container) != null) {

			Log.i("MainActivity", "onCreate: Split Screen");
			mSplitScreen = true;
		}
		// Single screen
		else{
			Log.i("MainActivity", "onCreate: Single Screen");
			setContentView(R.layout.main);
			mSplitScreen = false;
		}
		list = (AltSensorListFragment) getFragmentManager().findFragmentById(se.liu.tddd77.bilsensor.R.id.sensor_list);
		buttonList = new ArrayList<Button>();
		mEditText = (EditText) findViewById(R.id.add_new_dynamic_event_text);
		mLinearLayout = (LinearLayout) findViewById(R.id.linearLayout_focus);
		
		try {
			projectName = Backend.getInstance().getActiveProject();
		} catch (BackendError e1) {

			e1.printStackTrace();
		}

		try {
			Backend.getInstance().addSensorStatusListener(list);
		} catch (BackendError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//TODO: This does nothing???? Now it does!?
		try {
			setTitle(Backend.getInstance().getActiveProject());
		} catch (BackendError e) {
			//TODO: G�ra n�tt h�r?
			e.printStackTrace();
		}

		updateTimer = new Timer();
		updateTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				updateGui();
			}

		}, 0, /*16*/ 160);
		
		queueTimer = new Timer();
		queueTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				handler.post(queueHandler);
			}

		}, 0, 10);
		//}
		//hasBeenCreated = true;
		onItemSelected(0);
			

		 /*catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}



	private void updateGui(){
		handler.post(updateHandler);
	}



	//--------------------------------------------------
	// List buttons
	//--------------------------------------------------

	/**
	 * Run every time an item in the list is clicked. 
	 * If the application is run on a tablet, replace the current fragment with
	 * the one associated with the index. Otherwise, start the activity
	 * associated with the index. 
	 */
	@Override
	public void onItemSelected(int index){
//		try {
//			//Backend.getInstance().clearActiveOnScreen();
//		} catch (BackendError e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		if (mSplitScreen) {
			android.app.Fragment fragment = ElementList.getInstance().getElement(index).getFragment();
			if(fragment == getFragmentManager().findFragmentById(R.id.sensor_detail_container)){
				Log.i("MainActivity", "onItemSelected: Old fragment");
				return;
			}

			Bundle arguments = new Bundle();
			arguments.putInt(SensorDetailFragment.ARG_ITEM_ID, index);
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
			.replace(R.id.sensor_detail_container, fragment).commit();
			Log.i("MainActivity", "onItemSelected: Split screen");

		} else {
			Intent detailIntent = new Intent(this, SensorDetailActivity.class);
			detailIntent.putExtra(SensorDetailFragment.ARG_ITEM_ID, index);
			startActivity(detailIntent);
			Log.i("MainActivity", "onItemSelected: Not split screen");
		}
	}



	private boolean recording = false;
	//--------------------------------------------------
	// Menu buttons
	//--------------------------------------------------
	public void handleRecord(ImageButton button){
		//TODO: Start recording
		recButton= button;

		if(!isRecording){
			
			try {
				Backend.getInstance().resetSensorsData();
				Backend.getInstance().sendSettingsRequestMessageSelected(20, activeSensorsIdList);
				if(Backend.getInstance().getServerStatus() == protobuf.Protocol.StatusMsg.StatusType.IDLE)
				Backend.getInstance().startDataCollection("fromSurfplatta" +  new java.util.Date(System.currentTimeMillis()).toString() );
				experimentName = Backend.getInstance().getTemporaryName();
			} catch (BackendError e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//TODO: Only allow reaction when done with setup?
			Log.i("MainActivity", "handleRecord: Started recording");
				isRecording = true;
				recording = true;
				//timeout = true;

				recButton.setImageResource(R.drawable.recred);
			
		}
		else{
			DialogFragment recordFragment = new RecordDialog();
			recordFragment.show(getFragmentManager(), "dialog");
			//TODO: If cancel the save, continue running?
		}
	}

	public void showRecordDialog(){
		Log.d("showRecordDialog", "RECORDDIALOG");
		DialogFragment recordFragment = new RecordDialog();
		((RecordDialog) recordFragment).setETs(expName, expNotes);
		recordFragment.show(getFragmentManager(), "dialog");
	}
	
	public void handlePlay(ImageButton button){
		

		if(!isRecording){
			//TODO: Only allow reaction when done with setup?
			Log.i("MainActivity", "handleRecord: Started recording");
			PlaybackMenu menu = new PlaybackMenu(this);
			menu.show(getFragmentManager(), null);
			recording = true;
			button.setImageResource(R.drawable.play);
		}
		else{
			button.setImageResource(R.drawable.stop);
			//			Backend.getInstance().stopExp();
		}

	}

	/*public void openSettings(){

	}*/



	//--------------------------------------------------
	// Menu
	//--------------------------------------------------

	/**
	 * The menu consists of two parts, the record button and the settings. 
	 * The record button is a checkable button. The settings is the collection 
	 * of the other available options. They should all be added through the 
	 * XML-file with the definition of the event here in onOptionsItemSelected.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		View view = getLayoutInflater().inflate(R.layout.actionbar, null);
		actionBar.setCustomView(view);

		final TextView projectName = (TextView) findViewById(R.id.project_name);
		Log.i("MainActivity", "Getting project name");
		Bundle projectnamebundle = getIntent().getExtras();
		projectname = projectnamebundle.getString("Project_name");
		serverIP = projectnamebundle.getString("Server_IP");
		if(projectname == null){
			Log.i("MainActivity", "Projectname == null");
		} else {
			Log.i("MainActivity", "Projectname != null");
		}
		Log.i("MainActivity", projectname);
		projectName.setText(projectname);

		final ImageButton button = (ImageButton) findViewById(R.id.recButton);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				handleRecord(button);
			}
		});
	/*	final ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
		playButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				handlePlay(playButton);
			}
		});*/
		final ImageButton buttonMenu = (ImageButton) findViewById(R.id.menuButton);
		buttonMenu.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				try {
					saveProfile("profil.txt");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		final ImageButton loadMenu = (ImageButton) findViewById(R.id.loadButton);
		loadMenu.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				//try {
				//SelectViewNameDialog dialog = new SelectViewNameDialog(avb);
				//dialog.show(getFragmentManager(), null);
				try {
					loadProfile("profil.txt");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BackendError e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			}
		});

		//playButton.setMaxWidth(playButton.getHeight());

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Contains the events for all the menu items. 
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){

		// Record: Switch the state of the button
		// If it is checked, start recording. Otherwise, stop recording. 
		/*case R.id.action_record:
			item.setChecked(!item.isChecked());
			handleRecord(item.isChecked());
			return true;

			//TODO: Settings: All options should be added as different alternatives
			// like this one. */
		case R.id.action_settings:
			//openSettings();
			return true;

			// Default: Let super handle the undefined ones
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void addNewDynamicEvent(View view){
		EditText textfield = (EditText) ((LinearLayout)view.getParent()).findViewById(R.id.add_new_dynamic_event_text);
		Log.i("AltSensorListFragment", textfield.getText().toString());
		String name = textfield.getText().toString();
		textfield.setText("");
		addNewDynamicEventString(name);
	}


	@SuppressLint("UseValueOf")
	public void addNewDynamicEventString(String name){
		//TODO: Storlek p� denna ruta m�ste h�llas konstant.
		if(name == null||name.isEmpty()){
			Toast.makeText(this, "Dynamic Events require a name.", Toast.LENGTH_SHORT).show();
			return;
		}
		//TODO: �r this korrekt?

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.HORIZONTAL);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);//(LinearLayout.LayoutParams)layout.getLayoutParams();
		layout.setLayoutParams(params);

		Button button = new Button(this);
		buttonList.add(button);

		LinearLayout.LayoutParams buttonparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,	LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
		button.setLayoutParams(buttonparams);
		button.setText(name);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					Backend.getInstance().sendDynamicMessage((Long) (System.currentTimeMillis()/1000L), ((Button)v).getText().toString());
				} catch (BackendError e) {
					e.printStackTrace();
				}

			}});

		Button rmEventButton = new Button(this);
		LinearLayout.LayoutParams rmButtonparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,	LinearLayout.LayoutParams.MATCH_PARENT, 0.0f);
		rmEventButton.setLayoutParams(rmButtonparams);
		rmEventButton.setText("-");
		rmEventButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				//TODO: For-loop through trying to find the one with matching name?
				buttonList.remove(((Button)((LinearLayout)v.getParent()).getChildAt(0)));
				((LinearLayout)v.getParent().getParent()).removeView((LinearLayout)v.getParent());		
			}
		});
		layout.addView(button);
		layout.addView(rmEventButton);
		((LinearLayout)findViewById(R.id.dynamic_events_buttons_container)).addView(layout);
		//((LinearLayout)((LinearLayout)(view.getParent().getParent())).getChildAt(1)).addView(layout);
		//((LinearLayout) ((LinearLayout)(view.getParent().getParent()))findViewById(R.id.dynamic_events_buttons_container)).addView(button);

		//textfield.setText("");

	}


	//===========PROFILE HANDLING==============


	//Share resources between save and load?
	public void saveProfile(String name) throws IOException {
		if (isExternalStorageWritable()) {
			Log.d("MainActivity", "Saving Profile");
			//Append project-name to name to make it 1 profile with the same name/project?
			//Setup for filewriting
			File profileFile = new File(Environment.getExternalStorageDirectory(), name);
			Log.d("MainActivity", Environment.getExternalStorageDirectory().toString());
			FileWriter writer = new FileWriter(profileFile);
			BufferedWriter buf = new BufferedWriter(writer);
			Log.d("MainActivity", "BufferedWriter created");
			Log.d("MainActivity", profileFile.getAbsolutePath());


			SensorDetailFragment tmpFragment;
			elementList = ElementList.getInstance().getElements();
			List<GraphContainer> tmpGraphs;
			GraphContainer tmpGraph;

			ArrayList<ArrayList<Integer>> yId;
			int xId;
			String elementName;
			int sensorId;
			int graphType = GraphType.LINEGRAPH.ordinal(); //TODO: This must be gotten.
			//GraphType type = GraphType.LINEGRAPH.ordinal();

			//GraphType type = GraphType.values()[typeRepresentingNumber]; //FOR LOADING


			// Actual saving-loops
			// For every fragment
			buf.write(elementList.size() + "\n");
			for (int l = 1; l < elementList.size(); l++) {
				tmpFragment = (SensorDetailFragment) elementList.get(l).getFragment(); // Kan vara status -If tmpFragment instanceof SDF, Endast spara SDF?
				elementName = elementList.get(l).getId();
				tmpGraphs = tmpFragment.getSensorGraphs();

				buf.write(elementName + ":" + tmpGraphs.size()+ "\n");
				// For every graph
				for (int i = 0; tmpGraphs.size() > i; i++) {
					tmpGraph = tmpGraphs.get(i);
					buf.write(tmpGraph.sensorIds.size() + "\n");
					for(int sId = 0; sId < tmpGraph.sensorIds.size(); sId++) buf.write(tmpGraph.sensorIds.get(sId) + ":");
					buf.write("\n");
					xId = tmpGraph.xId;

					yId =  tmpGraph.yId; //TODO: FIX PROFILES!!!!!!!
					// For everything to plot in y
					//buf.write(sensorId + ":" + graphType + ":" + xId + ":" + yId.size()); //TODO FIIIIXAAA
					buf.write(yId.size() + ":"+ "\n");
					for (int k = 0; yId.size() > k; k++) {
						buf.write(yId.get(k).size()+ ":" +"\n");
						for(int m = 0; yId.get(k).size() > m; m++){
							buf.write(yId.get(k).get(m)+ ":");
						}		

						buf.write("\n");

					}		
					buf.write(xId+ ":");
					buf.write("\n");
				}
			}
			saveDynamicEvents(buf);
			buf.flush();
			buf.close();
			writer.flush();
			writer.close();

			Log.d("MainActivity", profileFile.getAbsolutePath());

		} else {
			Log.d("MainActivity", "Writable = false");
		}

	}

	//protected boolean initbool = false;

	void saveDynamicEvents(BufferedWriter buf) throws IOException{
		buf.write(buttonList.size()+ "\n");
		for (int i = 0; buttonList.size() > i; i++){
			buf.write(buttonList.get(i).getText().toString() + ":");
		}

	}

	void loadDynamicEvents(BufferedReader buf) throws IOException{
		list.eventLayout.removeAllViews();
		buttonList.clear();
		String DELIMITER = ":";
		String[] strTemp = new String[40];
		strTemp = buf.readLine().split(DELIMITER);
		int size = Integer.parseInt(strTemp[0]);
		if (size > 0){
			strTemp = buf.readLine().split(DELIMITER);
			for(int i = 0; size > i; i++){
				addNewDynamicEventString(strTemp[i]);
			}
		}
	}
	boolean existsSensors = true;
//ArrayList<ArrayList>
	public void loadProfile(String name) throws IOException, BackendError{//File as argument or get the file here?
		ElementList.getInstance().resetList();
		String DELIMITER = ":";
		String[] strTemp = new String[40]; //Magical constant, shouldn't ever be exceeded.
		FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory() + "/" + name);
		Log.d("MainActivity", Environment.getExternalStorageDirectory() + "/" + name);
		BufferedReader buf = new BufferedReader(new InputStreamReader(fis));
		strTemp = buf.readLine().split(DELIMITER); //Arraylist perhaps?
		int amountOfFragments = Integer.parseInt(strTemp[0]);
		Log.d("MainActivity", amountOfFragments + " AmountOfFragments");
		//elementList = ElementList.getInstance().getElements();
		int amountOfGraphs;
		int xId = -1;
		int ySize;
		int sensorId = -1;
		GraphType type = null;
		//int graphType = GraphType.LINEGRAPH.ordinal();
		type = GraphType.LINEGRAPH;
		List<ArrayList<Integer>> yId = new ArrayList<ArrayList<Integer>>();
		SensorDetailFragment tmpFragment;
		List<Integer> sensorIds = new ArrayList<Integer>();
		
		
		//For every fragment
		for (int l = 1; amountOfFragments > l; l++) {
			strTemp = buf.readLine().split(DELIMITER);
			list.viewAdded(strTemp[0]); //Adds a SDF
			Log.d("MainActivity", strTemp[0].toString() + " strTemp[0]");
			tmpFragment = (SensorDetailFragment) ElementList.getInstance().getElements().get(l).getFragment(); // Kan vara status -If tmpFragment instanceof SDF, Endast spara SDF?
			//FragmentTransaction tr = getFragmentManager().beginTransaction();
			//tr.replace(R.id.sensor_detail_container, tmpFragment);
			//tr.commit();
			tmpFragment.getFragmentManager().executePendingTransactions();
			
			
			//tmpFragment.
			Log.d("MainActivity", tmpFragment.toString() + "tmpFragment");
			amountOfGraphs = Integer.parseInt(strTemp[1]);
			
			
			// For every graph
			for (int i = 0; amountOfGraphs > i; i++) {
				strTemp = buf.readLine().split(DELIMITER);
				int amountOfSensors = Integer.parseInt(strTemp[0]);
				strTemp = buf.readLine().split(DELIMITER);

				
				for(int m = 0; amountOfSensors > m; m++){
					sensorId = Integer.parseInt(strTemp[m]);
					addSensorRequired(sensorId);
					sensorIds.add(sensorId);	
					yId.add(new ArrayList<Integer>());
				}
			
				strTemp = buf.readLine().split(DELIMITER);
				//sensorId = Integer.parseInt(strTemp[0]);
				int ySizer = Integer.parseInt(strTemp[0]);
				for(int pp = 0; ySizer > pp; pp++){
					strTemp = buf.readLine().split(DELIMITER);
					int yAttrSize = Integer.parseInt(strTemp[0]); 
					strTemp = buf.readLine().split(DELIMITER);
						for(int n = 0; n < yAttrSize; n++){
							int tmpInt = Integer.parseInt(strTemp[n]);
							yId.get(pp).add(tmpInt);
						}
				}
				strTemp = buf.readLine().split(DELIMITER);
				xId = Integer.parseInt(strTemp[0]);
										
						
				
				//}
				//GraphType.values()[Integer.parseInt(strTemp[1])];
				
				//ySize = Integer.parseInt(strTemp[3]);

			/*	for (int k = 0; ySize > k; k++) {
					yId.add(Integer.parseInt(strTemp[4+k]));
				}*/

				if(!(xId == -1 || sensorId == -1)){
					Log.d("MainActivity", type + " type " + xId+ " xId " + sensorId + " sensorId ");
				tmpFragment.onAttach(this);
				for(int i1 = 0; sensorIds.size() > i1; i1++){
					if(!SensorDataHolder.getInstance().hasSensor(sensorIds.get(i1))){
						existsSensors = false;
					}
				}
				if(existsSensors)
				((SensorDetailFragment) tmpFragment).graphSaved(type, (ArrayList<Integer>) sensorIds, xId, (ArrayList<ArrayList<Integer>>) yId);
				existsSensors = true;
				yId = new ArrayList<ArrayList<Integer>>();
				sensorIds = new ArrayList<Integer>();
				}
			}

		}
		loadDynamicEvents(buf);
		buf.close();
		fis.close();

	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}


	public void onBackPressed(){
		if(recording){
			Toast.makeText(this, "Can not exit until measurment is stopped.", Toast.LENGTH_LONG).show();
		}
		else{
			super.onBackPressed();
			//TODO: Throw away the backend, force user to reconnect. Clear project list.
		}
	}

	@Override
	public void nameSelected(String name) throws BackendError {
		Backend.getInstance().startExperimentPlayback(name);
	//	((ImageButton)findViewById(R.id.playButton)).setImageResource(R.drawable.stop);
	}

	int elementListSize;
	@Override
	public void onDoneRec(String sName, String sNotes) {
		if (sName == null && sNotes == null){
			
			try {
				if(projectName != null && experimentName != null){
				Backend.getInstance().setActiveProject(projectName);
				Backend.getInstance().stopDataCollection();
				Backend.getInstance().removeExperimentRequest(experimentName);
				}
				else{
					Backend.getInstance().stopDataCollection();
					Backend.getInstance().removeExperimentRequest(Backend.getInstance().getActiveExperiment());
				}
			
			} catch (BackendError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			try {
				elementListSize = ElementList.getInstance().getElements().size();
				for(int i = 1; elementListSize > i; i++){
					((SensorDetailFragment)	ElementList.getInstance().getElement(i).getFragment()).doneRec();
				}
				Backend.getInstance().setActiveProject(Backend.getInstance().getActiveProject());
				Backend.getInstance().stopDataCollection();
				Backend.getInstance().renameExperimentRequest(Backend.getInstance().getTemporaryName(), sName);

			} catch (BackendError e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	

			ExperimentMetaData em = new ExperimentMetaData();
			em.setExperimentName(sName);
			em.setExperimentDescription(sNotes);
			try {
				Backend.getInstance().sendExperimentMetaData(em);
				Toast.makeText(this, "Experiment saved", Toast.LENGTH_SHORT).show();
			} catch (BackendError e) {
				Log.d("debug", "BACKEND ERROR WHEN METADATA");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			justConnected = false;
			block = false;
		}
	
		recording = false;
		//timeout = false;
		recButton.setImageResource(R.drawable.recblack);
		isRecording = false;
	    mEditText.clearFocus();
	    mLinearLayout.requestFocus();
	
		Log.i("MainActivity", "handleRecord: Stopped recording");	
		
	}
	
	public void addSensorRequired(int sensorId){
		activeSensorsIdList.add(sensorId);
		try {
			Backend.getInstance().sendSettingsRequestMessageSelected(20, activeSensorsIdList);
		} catch (BackendError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void removeSensorRequired(List<Integer> list){
		for (int i = 0; i < list.size(); i++){
		activeSensorsIdList.remove(list.get(i));
		}
		try {
			Backend.getInstance().sendSettingsRequestMessageSelected(20, activeSensorsIdList);
		} catch (BackendError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@Override
	
	public void confirmDestroyExperiment(String sName, String sNotes) {
		// TODO Auto-generated method stub
		Log.d("projectname", "" + projectName);
		expName = sName;
		expNotes = sNotes;
		DeleteExperimentDialog cdd = new DeleteExperimentDialog();
		cdd.show(getFragmentManager(), null);
	}

		
	public void focusFix(){
	    mEditText.clearFocus();
	    mLinearLayout.requestFocus();
	}
	
	protected void onStart(){
		super.onStart();
		Log.i("MainActivity", "onStart");
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		Log.i("MainActivity", "onResume");
	}
	
	@Override
	protected void onPause(){
		hasBeenCreated = true;
		super.onPause();
		
		Log.i("MainActivity", "onPause");
	}
	
	protected void onStop(){
		super.onStop();
		try {
			saveProfile("crash.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i("MainActivity", "onStop");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isFinishing()){

			if(updateTimer != null){
				updateTimer.cancel();
			}
		/*	try {
				//Backend.getInstance().stopConnection();
			} catch (BackendError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} */
		}
		Debug.stopMethodTracing();
		Log.i("MainActivity", "onDestroy");
	}



	@Override
	public void cancelAction() {
		if(justConnected){
			try {
				Backend.getInstance().sendSettingsRequestMessageALlSensors(20);
				Backend.getInstance().setActiveProject(projectName);
				recButton.setImageResource(R.drawable.recblack);
				isRecording = false;
			} catch (BackendError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			block = false;
			justConnected = false;
			//handleRecord((ImageButton) findViewById(R.id.loadButton));

		}
		
	}
	

	
}