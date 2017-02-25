package jexcelunit.excel;

public class TestcaseVO {

	private String testname;
	private String testclass;
	private String[] constructor_params;
	private String testmethod;
	private String[] method_params;
	private String expect;
	private String result;


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

}
