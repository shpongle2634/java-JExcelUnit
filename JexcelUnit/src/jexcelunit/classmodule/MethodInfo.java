package jexcelunit.classmodule;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class MethodInfo extends Info{

	private Method method;
	private ParameterInfo[] parameters, sequences;
	private ClassInfo returnClass;
	
	public MethodInfo(Method method){
		//Method 및  Name 설정
		this.method= method;
		name= method.getName();
		
		//Method Return Type 설정
		Map<String, ClassInfo> classInfoMap = ClassInfoMap.INSTANCE.getInstance();
		Class<?> returnType= method.getReturnType();
		 if(classInfoMap.containsKey(returnType.getSimpleName())){
			 returnClass= classInfoMap.get(returnType.getSimpleName());
		 }
		 else {
			 returnClass= new ClassInfo(returnType);
			 classInfoMap.put(returnClass.getName(), returnClass);
		 }
		 
		//Parameter 설정
		parameters = new ParameterInfo[method.getParameterCount()];
		Parameter[] params= method.getParameters();
		for(int i=0; i< parameters.length; i++)
			parameters[i] = new ParameterInfo(params[i]);
		
		//not implemented yet. about sequences;
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
	public ParameterInfo[] getParameters() {
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
