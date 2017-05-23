package jexcelunit.classmodule;

import java.util.HashMap;


//SingleTon ClassHashMap
public enum ClassMap {
	INSTANCE;
	
	private final HashMap<String,ClassInfo> classInfoList;	
	ClassMap(){
		classInfoList= new HashMap<String, ClassInfo>();
	}
	public HashMap<String,ClassInfo> getInstance(){
		return classInfoList;
	}
}
