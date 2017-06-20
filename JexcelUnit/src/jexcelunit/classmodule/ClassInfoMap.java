package jexcelunit.classmodule;

import java.util.ArrayList;
import java.util.HashMap;


//SingleTon ClassHashMap
@SuppressWarnings("rawtypes")
public enum ClassInfoMap {
	INSTANCE;
	
	private final HashMap<String,ClassInfo> classInfoList;	
	private final ArrayList<Class> targetClasses;
	
	ClassInfoMap(){
		classInfoList= new HashMap<String, ClassInfo>();
		targetClasses = new ArrayList<Class>();
	}
	public HashMap<String,ClassInfo> getInfos(){
		return classInfoList;
	}
	
	public ArrayList<Class> getClassList(){
		return targetClasses;
	}
	
}
