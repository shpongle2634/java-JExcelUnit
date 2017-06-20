package jexcelunit.classmodule;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

public class ParameterInfo extends Info {

	private String fieldName;
	private String type;
	private ClassInfo paramInfo;
	@SuppressWarnings("rawtypes")
	public ParameterInfo(Class clz) {
		// TODO Auto-generated constructor stub
		
		fieldName = "arg";
		ClassInfo classInfo;
		if((classInfo=PrimitiveChecker.checkClassInfos(clz)) !=null){
			paramInfo= classInfo;
		}
		else{ 
			paramInfo= new ClassInfo(clz);
			if(PrimitiveChecker.isPrimitive(clz))
				ClassInfoMap.INSTANCE.getInfos().put(clz.getName(), paramInfo);
			else if(ClassInfoMap.INSTANCE.getClassList().contains(clz))
				ClassInfoMap.INSTANCE.getInfos().put(clz.getSimpleName(), paramInfo);
		}
		
		this.name = paramInfo.getName();
	}


	public ParameterInfo(Parameter parameter){
		this(parameter.getType());
		fieldName= parameter.getName();
		type= "Parameter";

	}
	public ParameterInfo(Field field){
		this(field.getType());
		fieldName= field.getName();
		type= "Field";
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}


	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.getParamInfo().getName()+' '+this.fieldName;
	}

	public ClassInfo getParamInfo() {
		return paramInfo;
	}

	public void setParamInfo(ClassInfo paramInfo) {
		this.paramInfo = paramInfo;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.name + ' ' + this.fieldName + " : "+ type;
	}
}
