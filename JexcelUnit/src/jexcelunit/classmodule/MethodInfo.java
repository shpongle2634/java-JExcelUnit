package jexcelunit.classmodule;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class MethodInfo extends Info{

	private Method method;
	private ClassInfo returnClass;

	public MethodInfo(Method method){
		//Method 및  Name 설정
		this.method= method;
		name= method.getName();

		//Method Return Type 설정
//		Map<String, ClassInfo> classInfoMap = ClassInfoMap.INSTANCE.getInfos();
		Class<?> returnType= method.getReturnType();
		if((returnClass=PrimitiveChecker.checkClassInfos(returnType)) ==null){
			returnClass= new ClassInfo(returnType);
		}
		//Parameter 설정
		Parameter[] params= method.getParameters();
		for(int i=0; i< params.length; i++)
			addChildren(new ParameterInfo(params[i]));

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

	public ClassInfo getReturnClass() {
		return returnClass;
	}
	public void setReturnClass(ClassInfo returnClass) {
		this.returnClass = returnClass;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String params="(";
		if(children.size()>0){
			for (int i =0; i<children.size(); i++) {
				params+= children.get(i).getName();
				if(i!=children.size()-1)
					params+=", ";
			}
		}
		params+=")";

		return returnClass.getName() +' '+this.name+params+" : Method ";
	}

}
