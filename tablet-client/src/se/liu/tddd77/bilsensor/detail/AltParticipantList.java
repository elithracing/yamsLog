package se.liu.tddd77.bilsensor.detail;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.liu.tddd77.bilsensor.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class AltParticipantList extends ListView {

	private View mView;
	private ArrayAdapter<String> mAdapter;

	ArrayList<String> namemap = new ArrayList<String>();

	public AltParticipantList(Context context, AttributeSet attr) {
		super(context, attr);
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mView = (inflater.inflate(R.layout.participant_element, (ViewGroup) this.getParent()));
		this.mAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_list_item_1 , namemap);
		this.setAdapter(mAdapter);


		this.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				removeParticipant(namemap.get(position));
			}

		});
	}






	public void addParticipant(String name){
		Log.i("AltParticipantList", "Adding Participant: " + name);
		if(name.isEmpty()){
			Toast.makeText(this.getContext(),  "Invalid entry", Toast.LENGTH_LONG);
		}

		//String[] names = name.split("\\w[ ]*[\\w]*");
		StringBuilder builder = new StringBuilder();
		Boolean duplicates = false;
		Pattern My_pattern = Pattern.compile("\\w[ \\w]*");
		Matcher m = My_pattern.matcher(name);

		while (m.find()){
			String s = m.group(0);


			if(s == null || s.isEmpty()){
				Log.i("AltParticipantList", "Entry is empty");
				Toast.makeText(this.getContext(),  "Entry is Empty", Toast.LENGTH_LONG);
			}else if(s.length() >= 1000){
				Log.i("AltParticipantList", "Entry exceeds max length");
				Toast.makeText(this.getContext(),  "Entry exceeds max length", Toast.LENGTH_LONG);
			}else{

				Log.i("AltParticipantList" , "Found Element: " + s);
				if(!namemap.contains(s)){
					namemap.add(s);
					mAdapter.notifyDataSetChanged();
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
	}

	public void removeParticipant(String name){
		Log.i("AltParticipantList", "Removing Element: " + name);
		namemap.remove(name);
		mAdapter.notifyDataSetChanged();

	}

	public ArrayList<String> getParticipants(){
		return namemap;
	}

}
