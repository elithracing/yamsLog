package se.liu.tddd77.bilsensor.list;

import android.app.Fragment;

//TODO: Don't really like this, but acceptable as a map substitute I guess(?)
public abstract class ListElement<T extends Fragment> {
	
	private String name;
	protected T fragment;
	public boolean elementChanged = false;
	
	public ListElement(String name){
		this.name = name;
	}
	
	@Override
	public String toString(){
		return name;
	}
	
	public String getId(){
		return name;
	}

	public Fragment getFragment() {
		return fragment;
	}

}
