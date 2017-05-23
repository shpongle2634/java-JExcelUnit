package jexcelunit.classmodule;

import java.lang.reflect.Method;

public class MethodInfo extends Info{

	private Method method;
	private ParameterInfo[] parameters, sequences;
	private ClassInfo returnClass;
	
	public MethodInfo(Method method){
		this.method= method;
		name= method.getName();
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ClassInfo[] getParameters() {
		return parameters;
	}
	
	public ParameterInfo[] getSequences() {
		return sequences;
	}

	public void setSequences(ParameterInfo[] sequences) {
		this.sequences = sequences;
	}

	public void setParameters(ParameterInfo[] parameters) {
		this.parameters = parameters;
	}

	public ClassInfo getReturnClass() {
		return returnClass;
	}
	public void setReturnClass(ClassInfo returnClass) {
		this.returnClass = returnClass;
	}

	
}
