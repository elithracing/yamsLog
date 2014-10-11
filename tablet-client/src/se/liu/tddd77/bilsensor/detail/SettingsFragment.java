//package se.liu.tddd77.bilsensor.detail;
//
//import com.google.protobuf.TextFormat;
//
//import se.liu.tddd77.bilsensor.SensorSpinner;
//import se.liu.tddd77.bilsensor.R;
//import se.liu.tddd77.bilsensor.SensorSpinner.SensorSpinnerListener;
//import se.liu.tddd77.bilsensor.list.ElementList;
//import se.liu.tddd77.bilsensor.list.SensorListFragment;
//import Errors.BackendError;
//import FrontendConnection.Backend;
//import android.app.Fragment;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.webkit.WebView.FindListener;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//
//public class SettingsFragment extends Fragment 
//		implements SensorSpinnerListener {
//	
//	
//	
//	public SettingsFragment(){}
//	
//	/**
//	 * Create the interface as described in the XML-file. 
//	 * In addition to this, also update the spinners prompt. 
//	 * Also get all fields that need to be updated when meta data is selected. 
//	 */
//	@Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//        Bundle savedInstanceState) {
//		View view = inflater.inflate(R.layout.settings_detail, container, false);
//		
//		SensorSpinner spinner = (SensorSpinner) view.findViewById(R.id.sensors);
//		if(spinner == null){
//			//TODO: Throw error when no spinner found
//			System.out.println("No Spinner");
//		}
//		else{
//			spinner.setListener(this);
//			spinner.updatePrompt();
//		}
//		
//		// Find relevant views and save them as references
//		
//        return view;
//    }
//	
//	
//	
//	//--------------------------------------------------
//	// Listeners
//	//--------------------------------------------------
//	
//	
//	/**
//	 * The listener for which sensors have been selected in the sensor spinner. 
//	 * Add the selected ones to the list of critical sensors. 
//	 */
//	@Override
//	public void onItemsSelected(boolean[] selected) {
//		System.out.println("SettingsFragment - onItemsSelected: ");
//		//TODO: Clear and add selected or add/remove all?
////		Backend.getInstance().clearCriticalSensors();
////		for(int i = 0; i < selected.length; i++){
////			if(selected[i]){
////				//TODO: Add critical sensors
////				
////				Backend.getInstance().addCriticalSensor(Backend.getInstance().getSensor(i));
////			}
////			else{
////				Mediator.getInstance().removeCriticalSensor(Mediator.getInstance().getSensor(i));
////			}
////		}
//		try {
//			ElementList.getInstance().updateCriticalSensorList(selected);
//		} catch (BackendError e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		SensorListFragment.onUpdate();
//	}
//	
//
//}
