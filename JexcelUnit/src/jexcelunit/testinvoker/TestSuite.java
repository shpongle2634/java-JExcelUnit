package jexcelunit.testinvoker;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

public class TestSuite extends TestInvoker{
	public TestSuite(int suite,String testname, Class targetclz,Constructor constructor, Object[] constructor_params, Method targetmethod,
			Object[] param1, Object expectedResult) {
		super(suite,testname, targetclz,constructor, constructor_params, targetmethod, param1, expectedResult);
		// TODO Auto-generated constructor stub
	}

	private static void setUp() {
		// TODO Auto-generated method stub
		/* Make Your Mock Objects  using mockObject.put("mock name", mock object);
		 * Make Your Custom Exceptions using  addException(your Exception e);
		 * */	
	}

	@Parameters( name = "{index}: Suite{0} {1}")
	public static Collection<Object[][]> parameterized(){
		setUp();
		return parmeterizingExcel();
	}
	

}
