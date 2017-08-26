package jexcelunit.excel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class MockVO {

	private String mockName;
	private Class mockClass;
	private Constructor constructor;
	private ArrayList<Object> consParams;
	private Map<Field,Object> fieldSet;
	public String getMockName() {
		return mockName;
	}
	public void setMockName(String mockName) {
		this.mockName = mockName;
	}
	public Class getMockClass() {
		return mockClass;
	}
	public void setMockClass(Class mockClass) {
		this.mockClass = mockClass;
	}
	public Constructor getConstructor() {
		return constructor;
	}
	public void setConstructor(Constructor constructor) {
		this.constructor = constructor;
	}
	public ArrayList<Object> getConsParams() {
		return consParams;
	}
	public void setConsParams(ArrayList<Object> consParams) {
		this.consParams = consParams;
	}
	public Map<Field, Object> getFieldSet() {
		return fieldSet;
	}
	public void setFieldSet(Map<Field, Object> fieldSet) {
		this.fieldSet = fieldSet;
	}
	
	
}
