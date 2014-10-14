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

package se.liu.tddd77.bilsensor.detail;

import se.liu.tddd77.bilsensor.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
/**
 * A participant element is an element in a participant list.
 * It consists of the name of the participant, followed by a 
 * button to remove that participant form the list. 
 */
public class ParticipantElement extends LinearLayout{

	public interface DeleteParticipantListener{
		public void onDelete(String participant);
	}
	
	public View getView(){
		return this.mView;
	}
	private View mView;
	private TextView mName;
	private ImageView mDelete;
	private DeleteParticipantListener listener;
	private final ParticipantList mParent;
	
	
	
	/**
	 * Create the view
	 * @param context The context in which to create the element. 
	 * @param name The name of the participant. 
	 * @param parent The list in which to add the element. 
	 */
	//TODO: Layout could be defined in an XML-file. Is it worth the hassle? 
	public ParticipantElement(Context context, String name, ParticipantList parent){
		super(context);
		
		mParent = parent;	

		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mView = (inflater.inflate(R.layout.participant_element, parent));

		this.mDelete = (ImageView)mView.findViewById(R.id.remove_image);
		this.mName = (TextView)mView.findViewById(R.id.participant_element_name);
		this.mName.setText(name);
		
		mDelete.setOnClickListener(new OnClickListener() {

			
			@Override
			public void onClick(View v) {
				// Removing parent since v is the button
				//mParent.removeParticipant(((ParticipantElement)v.getParent()).mName.getText().toString());
				mParent.removeParticipant(ParticipantElement.this.getName());
			}
		});
		
		
		setOrientation(LinearLayout.HORIZONTAL);
	}



	public String getName() {
		return mName.getText().toString();
	}
	
}
