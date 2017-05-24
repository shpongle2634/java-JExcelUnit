package jexcelunit.classmodule;

import java.util.HashMap;


//SingleTon ClassHashMap
public enum ClassInfoMap {
	INSTANCE;
	
	private final HashMap<String,ClassInfo> classInfoList;	
	ClassInfoMap(){
		classInfoList= new HashMap<String, ClassInfo>();
	}
	public HashMap<String,ClassInfo> getInstance(){
		return classInfoList;
	}
}
