package jexcelunit.classmodule;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

public class ParameterInfo extends Info {
	
	private String fieldName;
	private ClassInfo paramInfo;
	@SuppressWarnings("rawtypes")
	public ParameterInfo(Class clz) {
		// TODO Auto-generated constructor stub
		fieldName = "arg";
		ClassInfo classInfo;
		if((classInfo=ClassInfoMap.INSTANCE.getInstance().get(clz.getSimpleName())) !=null){
			paramInfo= classInfo;
		}
		else{ 
			paramInfo= new ClassInfo(clz);
			ClassInfoMap.INSTANCE.getInstance().put(clz.getSimpleName(), paramInfo);
		}
	}
	
	public ParameterInfo(Parameter parameter){
		this(parameter.getType());
		fieldName= parameter.getName();
	
	}
	public ParameterInfo(Field field){
		this(field.getType());
		fieldName= field.getName();
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
		return this.fieldName;
	}

	public ClassInfo getParamInfo() {
		return paramInfo;
	}

	public void setParamInfo(ClassInfo paramInfo) {
		this.paramInfo = paramInfo;
	}

}
