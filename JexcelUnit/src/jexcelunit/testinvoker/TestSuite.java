package jexcelunit.testinvoker;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class TestSuite extends TestInvoker{
	public TestSuite(String testname, Class targetclz,Constructor constructor, Object[] constructor_params, Method targetmethod,
			Object[] param1, Object expectedResult) {
		super(testname, targetclz,constructor, constructor_params, targetmethod, param1, expectedResult);
		// TODO Auto-generated constructor stub
	}

	private void setup() {
		// TODO Auto-generated method stub
	/* Make Your Mock Objects  using mockObject.put("mock name", mock object);
	 * Make Your Custom Exceptions using  addException(your Exception e);
	 * */	
	}

}
