package jexcelunit.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class ClassInfo{
	
	Class clz=null;

	Set<Constructor> constructors= new HashSet<>();
	Set<Method> methods = new HashSet<>();
	
	public ClassInfo(Class clz){
		this.clz=clz;
	}
	
	public void addConstructor(Constructor constructor){
		constructors.add(constructor);
	}
	
	public void addMethod(Method method){
		methods.add(method);
	}

	public Set<Constructor> getConstructors() {
		return constructors;
	}

	public Set<Method> getMethods() {
		return methods;
	}
	
	public Class getClz() {
		return clz;
	}

	public void setClz(Class clz) {
		this.clz = clz;
	}

}