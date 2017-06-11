package jexcelunit.classmodule;

import java.util.ArrayList;
import java.util.HashMap;


/*
 * Create : 2017.02.22
 * Vendor : Tae hoon Seo
 * Details : get Classes and analyze Class's Methods and Constructors
 * Info index :  class, methods. cosntructors, fields
 * 'infolist' is a Singleton Object. Because 'Info' classes is implemented using Composite Pattern.
 * it means that Info has recursive references. so Class Info must be managed by this class, as a Single Instance.
 * */
@SuppressWarnings("rawtypes")
public class ClassAnalyzer {

	private final ArrayList<Class> classes;

	//Target Classes
	public ClassAnalyzer(ArrayList<Class> classes){
		this.classes=classes;
	}

	//Analyze Classes and Return Info
	public HashMap<String,ClassInfo> getTestInfos(){
		if(classes ==null){
			return null;
		}
		HashMap<String, ClassInfo> map=ClassInfoMap.INSTANCE.getInstance();
		if(!map.isEmpty())
			map.clear();
		for(Class clz :classes){
			if(! map.containsKey(clz.getSimpleName()) && !PrimitiveChecker.isPrimitive(clz)){
				new ClassInfo(clz);
			}
		}
		return map;
	}



}
