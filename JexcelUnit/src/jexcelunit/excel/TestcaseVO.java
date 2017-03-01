package jexcelunit.excel;

import java.util.ArrayList;

public class TestcaseVO {

	private String testname;
	private String testclass;
	
	private ArrayList<String> constructor_params =null;
	private ArrayList<String> method_params = null;
	
	private String testmethod;
	private String expect;
	private String result;

	private boolean success;
	
	public TestcaseVO(){
		constructor_params= new ArrayList<>();
		method_params = new ArrayList<>();
	}
	
	public void addConstructorParam(String data){
		constructor_params.add(data);
	}
	public ArrayList<String> getConstructorParams(){
		return constructor_params;
	}
	
	public void addMethodParam(String data){
		method_params.add(data);
	}
	public ArrayList<String> getMethodParams(){
		return method_params;
	}
	
	public String getTestname() {
		return testname;
	}
	public void setTestname(String testname) {
		this.testname = testname;
	}
	public String getTestclass() {
		return testclass;
	}
	public void setTestclass(String testclass) {
		this.testclass = testclass;
	}
	public String getTestmethod() {
		return testmethod;
	}
	public void setTestmethod(String testmethod) {
		this.testmethod = testmethod;
	}
	public String getExpect() {
		return expect;
	}
	public void setExpect(String expect) {
		this.expect = expect;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

}
