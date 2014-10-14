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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import se.liu.tddd77.bilsensor.dialogs.ConnectDialog;
import se.liu.tddd77.bilsensor.dialogs.ConnectDialog.IPListener;
import se.liu.tddd77.bilsensor.graphs.SensorDataHolder;
import Errors.BackendError;
import Errors.NoDataError;
import FrontendConnection.Backend;
import FrontendConnection.Listeners.ProjectListChangedListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ChooseProjectActivity extends Activity implements OnItemClickListener,ProjectListChangedListener, IPListener{

	//private String servername;
	public static String latestConnection;
	private AlertDialog connecting;
	private Thread.UncaughtExceptionHandler exceptionHandler;
	public String connectIP;
	private class Connection extends AsyncTask<String, Void, Boolean>{
		ChooseProjectActivity cp;

		public Connection(ChooseProjectActivity cp){
			this.cp = cp;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try{
				//				Thread.UncaughtExceptionHandler exceptionHandler = new Thread.UncaughtExceptionHandler() {
				//
				//					@Override
				//					public void uncaughtException(Thread thread, Throwable ex) {
				//
				//					}
				//				};
				//				Backend.createInstance(exceptionHandler);



				//TODO: Move to correct place
				Backend.getInstance().connectToServer(params[0], 2001);

				Thread.sleep(1000);
				Backend.getInstance().tick();
				//TODO: Användare kanske ska få välja uppdateringsfrekvens själv.
				Backend.getInstance().sendSettingsRequestMessageALlSensors(20);

				//Log.i("ChooseProject", "Connection - doInBackground: Done");
				return true;
			}
			catch(Exception e){
				e.printStackTrace();
				//TODO: Error handling
				//Log.i("ChooseProject", "Connection - doInBackgroung: Error");
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			//Log.i("ChooseProject", "Connection - onPostExecute: ");

			if(result){
				try {
					Log.d("autoload", "isInDataCOll" + Backend.getInstance().getServerStatus());
					if(Backend.getInstance().getServerStatus() == protobuf.Protocol.StatusMsg.StatusType.DATA_COLLECTION){
						Log.d("autoload", "trying to autoload");
						Intent intent = new Intent(ChooseProjectActivity.this, MainActivity.class);
						intent.putExtra("Project_name", Backend.getInstance().getActiveProject());
						intent.putExtra("Server_IP", connectIP);
						Log.d("autoload", "Project_name " + Backend.getInstance().getActiveProject());
						startActivity(intent);
					}
				} catch (BackendError e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				//Log.i("ChooseProject", "Connection - onPostExecute: Failed connection");

				AlertDialog.Builder builder = new AlertDialog.Builder(cp);
				builder.setMessage("Failed to connect to server, please try to reconnect or exit program.");
				builder.setPositiveButton("Reconnect", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						cp.connectToServer();
					}
				});
				builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

					}
				});

				builder.show();

			}
			connecting.dismiss();
		}

	}

	ArrayList<String> listValues = new ArrayList<String>();

	ArrayAdapter<String> adapter;

	ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		Log.i("ChooseProject", "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_project);
		//Log.i("ChooseProject", "onCreate");

		listView = (ListView)findViewById(R.id.oldprojectslist);

		//listValues.add("Getting Projects...");

		adapter = new ArrayAdapter<String>(ChooseProjectActivity.this,
				android.R.layout.simple_list_item_1, android.R.id.text1, listValues);

		listView.setAdapter(adapter); 

		listView.setOnItemClickListener(this); 


		exceptionHandler = new Thread.UncaughtExceptionHandler(){

			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				ex.printStackTrace();
				//Log.d("ChooseProject", "Uncaught Exception");
				//SensorDataHolder.getInstance().setConnectedToServer(false);
				try {
					Backend.getInstance().restartServerListener();
				} catch (BackendError e) {
					e.printStackTrace();
					//Log.d("ChooseProject", "Could not restart server listener");
				}
			}
		};


		try {
			Backend.createInstance(exceptionHandler);

			//Log.i("ChooseProject", "Backend.createInstance()");
			Backend.getInstance().addProjectListChangedListener(this);
			connectToServer();


		} catch (BackendError e) {
			//Log.i("ChooseProject", "Could not create backend instance or add ProjectListChanged Listener");
			e.printStackTrace();
		}


	}



	public void connectToServer(){
		DialogFragment connectDialog = new ConnectDialog();
		 //connectDialog.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		connectDialog.show(getFragmentManager(), null);
	}

	public void reconnectToServer(View view){
		connectToServer();
	}
	public void changeActivityCreateNewProject(View view){
		//Log.i("ChooseProject", "Starting CreateNewProject");
		Intent intent = new Intent(this, CreateNewProject.class);
		startActivity(intent);	
	}

	protected void onStart(){
		super.onStart();
		Log.i("ChooseProject", "onStart");
	}

	protected void onRestart(){
		super.onRestart();
			//Backend.getInstance().clearDatabase();
		//	listValues.clear(); När var hur skickas att det har ändrats på projektlistan??? Bör la komma när man connectar?
		//	adapter.notifyDataSetChanged();
		
		//connectToServer();
		Log.i("ChooseProject", "onRestart");
	}

	protected void onResume(){
		super.onResume();
		//updateProjectList();

		Log.i("ChooseProject", "onResume");
	}

	protected void onPause(){
		super.onPause();
		Log.i("ChooseProject", "onPause");
	}

	protected void onStop(){
		super.onStop();
		//Log.i("ChooseProject", "onStop");
	}

	protected void onDestroy(){
		super.onDestroy();
		//Log.i("ChooseProject", "onDestroy");
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view,
			int position, long id) {

		//Log.i("ChooseProject", "Project: " + listValues.get(position) + " choosen");

		Intent intent = new Intent(ChooseProjectActivity.this, MainActivity.class);
		intent.putExtra("Project_name", listValues.get(position));
		intent.putExtra("Server_IP", connectIP);
		startActivity(intent); 
		try {
			Backend.getInstance().setActiveProject(listValues.get(position));
		} catch (BackendError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*try {
			Enum<protobuf.Protocol.SetActiveProjectResponseMsg.ResponseType> tmpEnum = Backend.getInstance().setActiveProject(listValues.get(position));
		
			switch(tmpEnum.ordinal()){
			case protobuf.Protocol.SetActiveProjectResponseMsg.ResponseType.SUCCESS_VALUE-1:
				Intent intent = new Intent(ChooseProjectActivity.this, MainActivity.class);
				intent.putExtra("Project_name", listValues.get(position));
				
				startActivity(intent); 
				break;
			case protobuf.Protocol.SetActiveProjectResponseMsg.ResponseType.PROJECT_NOT_FOUND_VALUE-1:
				Toast.makeText(getApplicationContext(), "Project not found", Toast.LENGTH_SHORT).show();
				break;
			case protobuf.Protocol.SetActiveProjectResponseMsg.ResponseType.OTHER_ERROR_VALUE-1:
				Toast.makeText(getApplicationContext(), "Something went wrong: OTHER_ERROR", Toast.LENGTH_SHORT).show();
				break;
			}*/
				
			
//			Backend.getInstance().setActiveProject(listValues.get(position));

			
//		} catch (BackendError e) {
//			Toast.makeText(this, "Error: Could not set active project.", Toast.LENGTH_SHORT).show();
//			e.printStackTrace();
//		}
	}

	@Override
	public void projectListChanged() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				updateProjectList();

			}
		});


	}
	
	public StringBuffer getStringBuffer(){
		try {
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(
					openFileInput("latestservername")));
			String inputString;
			StringBuffer stringBuffer = new StringBuffer();                
			while ((inputString = inputReader.readLine()) != null) {
				stringBuffer.append(inputString); //\n
			}
			return stringBuffer;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void updateLatest(String newName){
		try {
			FileOutputStream outputStream = openFileOutput("latestservername", Context.MODE_PRIVATE);
			outputStream.write(newName.getBytes());
			outputStream.close();
			connectIP = newName;
		} catch (Exception e) {
			e.printStackTrace();
		}

		new Connection(ChooseProjectActivity.this).execute(newName);
		connecting = new AlertDialog.Builder(ChooseProjectActivity.this).setMessage("Connecting...").create();
		connecting.show();

	}
	
	public void updateProjectList(){
		//Log.i("Choose Project", "ProjectListChanged");
		try {

			listValues.clear();
			List<String> listofprojectnames = Backend.getInstance().getProjectFilesFromServer();
			for(String s : listofprojectnames){
				//Log.i("ChooseProject", "Found project " + s);
				listValues.add(s);
			}

		} catch (BackendError e) {
			//Log.i("ChooseProject", "getProjectFilesFromServer Error");
			if(e instanceof NoDataError){
				listValues.add("No Projects Avaliable");
			}

			e.printStackTrace();
		} finally{
			adapter.notifyDataSetChanged();
		}

		//Log.i("ChooseProject", "Project list updated");

	}

}
