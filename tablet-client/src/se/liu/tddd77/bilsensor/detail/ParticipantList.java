package se.liu.tddd77.bilsensor.detail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ParticipantList extends LinearLayout {
	
	HashMap<String,View> namemap = new HashMap<String,View>();

	public ParticipantList(Context context) {
		this(context, null);
	}
	
	public ParticipantList(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		// Set the layout in accordance with the XML-file
		setOrientation(LinearLayout.VERTICAL);
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//addView(inflater.inflate(R.layout.participant_header, (ViewGroup) getParent()));
	}
	
	
	public void addParticipant(String name){
		Log.i("ParticipantList", name);
		if(name.isEmpty()){
			Toast.makeText(this.getContext(),  "Invalid entry", Toast.LENGTH_LONG);
		}
		//TODO: Här blir något fel, mellanslag ger 2 olika element. borde ej vara så.
		String[] names = name.split("(,|;|.) ");
		StringBuilder builder = new StringBuilder();
		Boolean duplicates = false;
		for(String s : names){
			Log.i("Found element" , s);
			if(!namemap.containsKey(s)){
				View participant = new ParticipantElement(getContext(), s, this);
				addView(participant);
				namemap.put(s, participant);
				Log.i("ParticipantsList" , participant.toString());
				Log.i("ParticipantsList" , "Parent: " + participant.getParent().toString());
				
				
			}
			else{
				duplicates = true;
				builder.append(s + ", ");
			}
		}
		if(duplicates){
			Toast.makeText(this.getContext(), "List already contains: " + builder.substring(0, builder.length()-2), Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Removes the participant from the list. Make sure to send the whole element, not just the button. 
	 * @param v The view of the participant to remove. 
	 */
	public void removeParticipant(String v){
		
		Iterator<Entry<String, View>> it = namemap.entrySet().iterator();
		
		while(it.hasNext()){
		Log.i("ParticipantList", it.next().getKey());
		}
		Log.i("ParticipantsList", namemap.get(v).toString());
		Log.i("ParticipantsList" , "Parent: " + namemap.get(v).getParent().toString());
		Log.i("ParticipantsList" , this.toString());
		removeView((View)namemap.get(v).getParent());
//		for(int i = 0; i < this.getChildCount(); i++){
//			Log.i("ParticipantList", this.getChildAt(i).toString());
//			this.removeViewAt(i);
//		}
		this.invalidate();
		namemap.remove(v);
		
		System.out.println("ParticipantList - removeParticipant: Removing Participant " + v);
	}
	

	
	/**
	 * Returns the name of all participants currently in the list. 
	 * @return A list containing the names of all the participants currently in the list. 
	 */
	public ArrayList<String> getParticipants(){
		ArrayList<String> participants = new ArrayList<String>();
		
		for(int i = 0; i < getChildCount(); i++){
			View view = getChildAt(i);
			if(view instanceof ParticipantElement){
				participants.add(((ParticipantElement)view).getName());
			}
		}
		
		return participants;
	}
	
	/**
	 * Set the list of participants. 
	 * To be used when loading meta-data. 
	 * @param participants An array of all participants to be included in the list. 
	 */
	public void setData(String[] participants) {
		// Remove all old participants
		for(int i = 0; i < getChildCount(); i++){
			View view = getChildAt(i);
			if(view instanceof ParticipantElement){
				removeView(view);
			}
		}
		// Add the new participants
		for(String participant : participants){
			addParticipant(participant);
		}
	}
	
}
