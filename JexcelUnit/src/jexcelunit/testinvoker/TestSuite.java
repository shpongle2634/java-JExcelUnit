package jexcelunit.testinvoker;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
@SuppressWarnings("rawtypes")
public class TestSuite extends TestInvoker{
	public TestSuite(String suite,String testname, Class targetclz,Constructor constructor, Object[] constructor_params, Method targetmethod,
			Object[] param1, Object expectedResult) {
		super(suite,testname, targetclz,constructor, constructor_params, targetmethod, param1, expectedResult);
		// TODO Auto-generated constructor stub
	}

	private static void setUp() {
		// TODO Auto-generated method stub
		/* Make Your Mock Objects  using mock.put("mock name", mock object);
		 * Make Your Custom Exceptions using  addException(your Exception e);
		 * 
		 * */	
	}
	
	@SuppressWarnings("unchecked")
	@Parameters( name = "[{0}] Test NO.{index} : {1}")
	public static Collection<Object[][]> parameterized() throws InstantiationException{
		setUp();
		return parmeterizingExcel("");
	}
	

}
