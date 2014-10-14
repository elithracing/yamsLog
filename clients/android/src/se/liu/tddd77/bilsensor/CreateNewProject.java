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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import protobuf.Protocol.CreateNewProjectResponseMsg.ResponseType;
import se.liu.tddd77.bilsensor.detail.AltParticipantList;
import Database.Messages.ProjectMetaData;
import Errors.BackendError;
import FrontendConnection.Backend;
import FrontendConnection.Listeners.ProjectCreationStatusListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class CreateNewProject extends Activity implements ProjectCreationStatusListener{

	public EditText mParticipantName;
	public EditText mTag;
	public AltParticipantList participantList;
	public AltParticipantList tagList;
	public String mProjectName = null;
	public String mLeaderName = null;
	public String mLeaderEmail = null;
	public String mDescription = null;
	ArrayList<String> mParticipantList = null;
	ArrayList<String> mTagList = null;
	static ResponseType projectcreationstatus;
	private boolean projectListChangedBoolean;
	private EnumMap<ResponseType, String> result = new EnumMap<ResponseType, String>(ResponseType.class);


	@Override
	protected void onCreate(Bundle savedInstanceState){

		result.put(ResponseType.ILLEGAL_NAME, "Project name illegal");
		result.put(ResponseType.NAME_TAKEN, "Project name taken");
		result.put(ResponseType.OTHER_ERROR, "Unknown error creating project");
		result.put(ResponseType.SUCCESS, "Project created");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_new_project);
		participantList = (AltParticipantList)findViewById(R.id.participant_list);
		tagList = (AltParticipantList)findViewById(R.id.tag_list);

		readWriteBoolean(false);


		try {
			Backend.getInstance().addProjectCreationStatusListener(this);
		} catch (BackendError e) {
			Log.i("CreateNewProject", "Could not add project creation status listener");
			e.printStackTrace();
		}

	}


	public void finishSettings(View view){

		mProjectName = ((EditText)findViewById(R.id.projectname)).getText().toString();
		mLeaderName = ((EditText)findViewById(R.id.leadername)).getText().toString();
		mLeaderEmail = ((EditText)findViewById(R.id.leaderemail)).getText().toString();
		mParticipantList = participantList.getParticipants();
		mTagList = tagList.getParticipants();
		mDescription =((EditText)findViewById(R.id.description)).getText().toString();	

		if(mProjectName == null||mProjectName.isEmpty()){
			Log.i("CreateNewProject", "Project name invalid");
			Toast.makeText(this, "Please enter Project name", Toast.LENGTH_SHORT).show();
			return;
		}

		/*if(!((EditText)findViewById(R.id.participant_name)).getText().toString().isEmpty()){
			Toast.makeText(this, "Participant not added", Toast.LENGTH_SHORT).show();
			return;
		}
		if(!((EditText)findViewById(R.id.tag_name)).getText().toString().isEmpty()){
			Toast.makeText(this, "Tag not added", Toast.LENGTH_SHORT).show();
			return;
		}

		if(!(mLeaderEmail == null || mLeaderEmail.isEmpty())){
			Pattern My_pattern = Pattern.compile(".+@.+\\..+");
			Log.i("CreateNewProject", My_pattern.toString());
			Matcher m = My_pattern.matcher(mLeaderEmail);

			if(!m.matches()){
				Log.i("CreateNewProject", "Leader Email incorrect");	
				Toast.makeText(this, "Leader Email is invalid", Toast.LENGTH_LONG).show();
				return;
			}
		}*/

		if(stringExceedMaxLength(mProjectName, "Project Name")||
				stringExceedMaxLength(mLeaderName, "Leader Name")||
				stringExceedMaxLength(mLeaderEmail, "Leader Email")){
			Log.i("CreateNewProject", "A String exceeded max length");
			return;
		}else{

			ProjectMetaData projectMetaData = new ProjectMetaData();
			try{		
				Log.i("CreateNewProject", "Sending Metadata to backend.");
			
				Backend.getInstance().createNewProjectRequest(mProjectName);

				projectMetaData.setTest_leader(mLeaderName);
				projectMetaData.setEmail(mLeaderEmail);
				projectMetaData.setMember_names(mParticipantList);
				projectMetaData.setTags(mTagList);
				projectMetaData.setDescription(mDescription);
				projectMetaData.setDate(System.currentTimeMillis());		
				
			}
			catch (BackendError e){
				Log.i("CreateNewProject", "Could not run createNewProjectRequest or set metadata");
				e.printStackTrace();
			}


			Log.i("CreateNewProject", "Entering busy waiting");

			boolean error = false;
			int x = 0;
			while(!readWriteBoolean(null)){
				try {
					Thread.sleep(1);
					Backend.getInstance().tick();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BackendError e){
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				x++;
				if(x > 2500){
					break;

				}
			}

			

			readWriteBoolean(false);
			Log.i("CreateNewProject", "Got out of busy waiting");
			switch(projectcreationstatus)
			{
			case NAME_TAKEN:
				Toast.makeText(this, "Project name taken", Toast.LENGTH_SHORT).show();				
				break;
			case OTHER_ERROR:
				Toast.makeText(this, "Creating Project Error", Toast.LENGTH_SHORT).show();				
				break;
			case SUCCESS:

				List<String> projects;
				try {
					Log.i("CreateNewProject", "Create Project - SUCCESS - getProjectFilesFromServer");
					projects = Backend.getInstance().getProjectFilesFromServer();
					//					for(String s : projects){
					//						Log.i("CreateNewProject", s);
					//					}
					if(projects.contains(mProjectName)){

						Log.i("CreateNewProject", "setActiveProject");
						Backend.getInstance().setActiveProject(mProjectName);
						Backend.getInstance().sendProjectMetaData(projectMetaData);

						
						Log.i("CreateNewProject", "Starting MainActivity");
						
						Intent intent = new Intent(this, MainActivity.class);
						
						intent.putExtra("Project_name", mProjectName);
						
						startActivity(intent);
					}

				} catch (BackendError e) {
					Log.i("CreateNewProject","Could not setActiveProject, send Metadata or start MainActivity");
					e.printStackTrace();

				}

				break;

			default:
				break;
			}

		}


	}


	//Gets the entry from EditText field participant_name and adds it to the
	//ParticipantList. Removes the entry to allow a new one.
	public void addMember(View view){
		mParticipantName = (EditText)findViewById(R.id.participant_name);
		Log.i("CreateNewProject", "Adding participant: " + mParticipantName.getText().toString());
		participantList.addParticipant(mParticipantName.getText().toString());
		mParticipantName.setText("");
	}

	//Same as function above, except for the tagslist instead.
	public void addTag(View view){
		mTag = (EditText)findViewById(R.id.tag_name);
		Log.i("CreateNewProject", "Adding Tag: " + mTag.getText().toString());
		tagList.addParticipant(mTag.getText().toString());
		mTag.setText("");
	}

	public boolean stringExceedMaxLength(String s, String field){


		if(s.length() >= 1000){
			Toast.makeText(this, "Entry " + field + " exceeds max length", Toast.LENGTH_LONG).show();
			return true;
		}
		return false;
	}

	private synchronized boolean readWriteBoolean(Boolean b){
		if(b!=null) projectListChangedBoolean=b; 
		return projectListChangedBoolean; 
	}

	@Override
	public void projectCreationStatusChanged(ResponseType responseType) {
		projectcreationstatus = responseType;
		Toast.makeText(this, result.get(responseType), Toast.LENGTH_LONG).show();
		Log.i("CreateNewProject", "projectCreationStatusChanged");
		readWriteBoolean(true);
	}


	protected void onStart(){
		super.onStart();
		Log.i("CreateNewProject", "onStart");
	}

	protected void onRestart(){
		super.onRestart();
		Log.i("CreateNewProject", "onRestart");
	}

	protected void onResume(){
		super.onResume();
		Log.i("CreateNewProject", "onResume");
	}

	protected void onPause(){
		super.onPause();
		Log.i("CreateNewProject", "onPause");
	}

	protected void onStop(){
		super.onStop();
		Log.i("CreateNewProject", "onStop");
	}

	protected void onDestroy(){
		super.onDestroy();
		Log.i("CreateNewProject", "onDestroy");
	}

}