//package se.liu.tddd77.bilsensor.profilehandler;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//
//public class profilehandler {
//
//public profilehandler(){
//	
//}
//
//public boolean SaveProfile(String name){
//	
//	File file = new File(name);
//	try {
//		if(!file.exists()){
//		file.createNewFile();
//		FileWriter writer = new FileWriter(file);
//		//writer.write("AWESOME" +  System.getProperty("line.separator"));
//		//writer.write("It worked!" +  System.getProperty("line.separator"));
//		writer.flush();
//		writer.close();
//		//System.out.println(profile.getAbsoluteFile());
//		return true;
//		}
//		else{
//			return false;
//		}
//	} catch (IOException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	return false;
//}
//
////TODO: Ska antingen sätta ihop profilen eller skicka tillbaka information för att detta ska göras utanför.
//public boolean LoadProfile(String name){
//	
//	try {
//		FileReader fr = new FileReader(name);
//		BufferedReader reader = new BufferedReader(fr);
//		String dataread;
//	//	dataread = reader.readLine();
//	//	System.out.println(dataread);s
//	//	dataread = reader.readLine();
//	//	System.out.println(dataread);		
//		reader.close();
//		return true;
//	}catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//	}		
//	return false;
//}
//}
//
//
