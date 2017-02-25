package jexcelunit.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


/*
 * Create : 2017.02.22
 * Vendor : Tae hoon Seo
 * Details : get Classes and analyze Class's Methods and Constructors
 * 
 * */
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
		
		HashMap<String,ClassInfo> infolist = new HashMap<>();
		for(Class clz :classes){
			ClassInfo result = analyze(clz);
			infolist.put(clz.getSimpleName(),result);
		}
		
		return infolist;
	}
	
	
	//analyze class and return info
	private ClassInfo analyze(Class clz){
		ClassInfo info= new ClassInfo(clz);
		
		//save constructors
		Constructor[] cons= clz.getDeclaredConstructors();
		for(Constructor con : cons){
//			System.out.println(con.getName() + "(" + Arrays.toString(con.getParameters())+ ")");
			info.addConstructor(con);
		}
		
		//save methods
		Method[] mets = clz.getDeclaredMethods();
		for(Method met : mets){
//			System.out.println(met.getName() + "(" + Arrays.toString(met.getParameters())+ ")");
			info.addMethod(met);
		}
		
		return info;
	} 
	
}
