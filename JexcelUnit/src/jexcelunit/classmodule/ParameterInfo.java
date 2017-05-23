package jexcelunit.classmodule;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

public class ParameterInfo extends ClassInfo {
	
	private String fieldName;
	@SuppressWarnings("rawtypes")
	public ParameterInfo(Class clz) {
		super(clz);
		// TODO Auto-generated constructor stub
	}
	
	public ParameterInfo(Parameter parameter){
		super(parameter.getType());
		fieldName= parameter.getName();
	
	}
	public ParameterInfo(Field field){
		super(field.getType());
		fieldName= field.getName();
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	

}
