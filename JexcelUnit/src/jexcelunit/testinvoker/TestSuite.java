package jexcelunit.testinvoker;

public class TestSuite extends TestInvoker{

	public TestSuite(String testname, Class targetclz, Object[] constructor_params, Object expectedResult,
			String methodname, Object[] param1) {
		super(testname, targetclz, constructor_params, methodname, param1,expectedResult);
		// TODO Auto-generated constructor stub
	}
	
	private void setup() {
		// TODO Auto-generated method stub
	/* Make Your Mock Objects  using mockObject.put("mock name", mock object);
	 * Make Your Custom Exceptions using  addException(your Exception e);
	 * */	
	}

}
