package jexcelunit.testinvoker;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import jexcelunit.excel.ExcelReader;
import jexcelunit.excel.TestcaseVO;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * 클래스 설명 : Reflection을 통한 통합 테스팅 코드.
 * 이 클래스는 import하게된 부분을 보게되면 알 수있듯, CoffeeMaker를 알고 있지않다. 즉,특정 프로젝트와 연관이 없다.
 * 이 클래스를 상속받아 테스트하고자 하는 프로젝트에 맞게 사용하면 된다.
 * Date: 2016/03/18
 * Student Num : 2010112469
 * Major : 컴퓨터 공학 
 * Name : 서태훈 
 * (리플랙션을 사용하면 테스트메소드와 사용자 가 원하는 테스트 객체와 완전한 분리가 가능)
 * 
 * 
 * 
 * 2017-03-01
 * Interface 이슈 .
 * Testing 버튼을 눌렀을때. 
 * JUnit 테스트를 자동으로 시켜줄건가...
 *  1. TestInvoker 를 상속받은  Suite 클래스를 하나 생성해준다. Mock 객체.
 *  2. JUnit과 같은 런타임 환경을 하나 직접 만들던가.. 이건좀 오래걸릴수도. JUnit을 만들어야하니까;
 *  3. 
 **/
@RunWith(Parameterized.class) //테스트 케이스를 이용할것이다.
public class TestInvoker {
	private static Map<Class, Object> classmap= new HashMap<Class, Object>(); //해쉬맵으로 테스트에 필요한 객체들을 하나씩만 유지한다.
	private static ArrayList<Class> exceptionlist=new ArrayList<Class>();//사용자 정의 예외 클래스들을 담아두는 곳.
	//	private static Method[] methods; //테스트할 객체의 메소드를 받는부분
	private static HashMap<String,Object> mock=new HashMap<String,Object>();//모크객체 모음
	
	private static int testnumber=0; //테스트 run 넘버

	//테스트 케이스들을 확인할 method_params
	private String testname=null;
	private Class targetclz=null;
	private Constructor constructor = null;
	private Object[] constructor_params=null;
	private Method targetmethod=null;
	private Object[] method_params=null;
	private Object expectedResult=null;


	//테스트이름, 테스트할 클래스, 테스트파라미터,  테스트할 메소드이름, 파라미터들,예상결과를 JUnit이 읽어와 실행시키는 부분이다.
	public TestInvoker(String testname,Class targetclz,Constructor constructor,Object[] constructor_params,Method targetmethod,Object[] param1,Object expectedResult){
		this.testname= (String)testname;
		this.targetclz=targetclz;
		this.constructor=constructor;
		this.constructor_params=constructor_params;
		this.expectedResult=expectedResult;
		this.targetmethod=targetmethod;
		this.method_params=param1;
	}

	public static Collection parmeterizingExcel(){
		ExcelReader reader = new ExcelReader();
		//메타데이터를 참조할 수 밖에없다.
		//핸들러 레벨에서 타겟 프로젝트 정보를 생성할것.
		File file = new File(".");
		ArrayList<TestcaseVO> testcases=null;
		Object[][] parameterized= null;

		if(file.exists()){
			try {
				File realPath= new File(file.getCanonicalPath());

				testcases = reader.readExcel(realPath.getName(), file.getCanonicalPath());
				TestcaseVO currentCase =null;

				if(!testcases.isEmpty())
				{
					for(TestcaseVO c : testcases){
						System.out.println(c.getTestname());
					}

				}
				parameterized = new Object[testcases.size()][7];
				for(int row_index = 0; row_index < testcases.size(); row_index++){
					currentCase = testcases.get(row_index);
					parameterized[row_index][0]=currentCase.getTestname();
					parameterized[row_index][1]=currentCase.getTestclass();
					parameterized[row_index][2]=currentCase.getConstructor();
					parameterized[row_index][3]=currentCase.getConstructorParams().toArray();
					parameterized[row_index][4]=currentCase.getMet();
					parameterized[row_index][5]=currentCase.getMethodParams().toArray();
					parameterized[row_index][6]=currentCase.getResult();	

					//To do list : 1. Testinvoker 메소드 수정 (con, method을 바로 실행하도록..
					//2. Custom Parameter Converting 모크객체. Date Format 이슈 처리.
					//3. 로그
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//읽어들인 리스트를 String, Class, Object[] Object, String Object로 바까야함.
		return Arrays.asList(parameterized);
	} 

	//사용안함.
	public static void setUp() {
		// TODO Auto-generated method stub

	}

	@Parameters( name = "{index}: {0}")
	public static Collection<Object[][]> parameterized(){
		setUp();
		return parmeterizingExcel();
	}

	//사용자가 예상하는 익셉션이 있는경우 이 함수를 통해 더해준다
	public static void addException(Class e){
		exceptionlist.add(e);
	}

	private void handleException(Exception e){
		//e.printStackTrace();
		StackTraceElement[] exceptionClass=e.getCause().getStackTrace();
		if(exceptionClass!=null)
			for (StackTraceElement s: exceptionClass){ //리플렉션 익셉션을 제외한 stacktrace 출력
				System.out.println(s);
				if(s.getClassName().equals(Method.class.getName()))break;
			}
		if(!exceptionlist.isEmpty())
			for(Class ex :exceptionlist){
				if(e.getCause().getClass().equals(ex)){
					System.out.println(e.getCause()); //예외 종류 출력
					break;
				}
			}
	}

	//	private Class unBoxing(Class wrapper){
	//		switch(wrapper.getTypeName().charAt(10)){
	//		case 'S': return wrapper.getTypeName().contains("Short")?short.class:String.class;							
	//		case 'B': return wrapper.getTypeName().contains("Byte")?Byte.class:Boolean.class;
	//		case 'C':return char.class;
	//		case 'I':return int.class;
	//		case 'L':return long.class;
	//		case 'D':return double.class;
	//		case 'F':return float.class;
	//		case 'V':return void.class;
	//		default : return null;
	//		}
	//	}

	@SuppressWarnings("unused")
	@Before
	public void setObj(){
		if(!classmap.containsKey(targetclz)&& targetmethod !=null){ //실행할 객체가 없는경우
			//System.out.println(classmap.containsKey(targetclz)+"새로생성");
			constructor.setAccessible(true);
			try {
				if(constructor_params.length==0)
					classmap.put(targetclz, constructor.newInstance());
				//타입이 안맞으면 mock 객체 가져올것.
				else
					classmap.put(targetclz, constructor.newInstance(constructor_params));
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				// TODO Auto-generated catch block
				handleException(e);
			}
		}
	}

	private void constructor_test(){
		System.out.println( "\n"+(testnumber++) + " : "+testname +"\n 테스트 클래스 : " +targetclz.getSimpleName());//테스트 번호와 어떤객체로부터  테스트가 이루어지는지출력
		try{
			//
			constructor.setAccessible(true);
			
			if(constructor_params.length==0)
				assertNotNull(constructor.newInstance());
			//타입이 안맞으면 mock 객체 가져올것.
			else
				assertNotNull(constructor.newInstance(constructor_params));

		}catch(Exception e){handleException(e);}
	}


	private boolean isNeedUnBoxing(Class clz){
		if(clz.isPrimitive() || (clz.getSuperclass()==Number.class)||
				(clz==String.class) ||(clz==Character.class)
				||(clz==Boolean.class)){ //원시값 테스트
			return true;
		}
		else
			return false;
	}

	/**********************************************************************
	 * 이름         : auto_Assert
	 * 파라미터   : 
	 * 			Object testresult	: 테스트 메소드를 실행한 후 받은 리턴 객체. 사용자 정의객체이거나,  
	 * 			Field  f			: testresult의 맴버 변수의 이름
	 * 			Class  memeberclz	: testresult의 맴버 변수의 타입 
	 * 역할         : 예상결과를 비교해준다. PrimitiveType이 아닌경우 객체 내부의 필드값을 꺼내서 일일이 비교해준다.
	 ********************************************************************** */
	private void auto_Assert(Object testresult, Field f,Class memeberclz ) throws Exception{
		if(isNeedUnBoxing(memeberclz) ){
			System.out.println( "Assert 결과  (예상값/테스트결과): "+f.get(expectedResult)+ " "+f.get(testresult));
			assertThat(f.get(testresult),is(f.get(expectedResult)));
		}
		else if(memeberclz.isArray()){//배열원소 비교
			if(Array.getLength(f.get(testresult)) == Array.getLength(f.get(expectedResult))){
				for(int i= 0; i<Array.getLength(f.get(testresult)); i++){
					if(Array.get(f.get(testresult), i)!=null &&Array.get(f.get(expectedResult), i)!=null){
						System.out.println( "Assert 결과  (예상값/테스트결과): "+Array.get(f.get(expectedResult), i)+ " "+Array.get(f.get(testresult), i));
						assertThat(Array.get(f.get(testresult), i), is(Array.get(f.get(expectedResult), i)));
					}
				}
			}
		}
	}	

	//	private Method get_TargetMethod() throws Exception{
	//		Method target=null;
	//		Class[] types=null;
	//		Method[] methods = targetclz.getMethods();
	//		for(Method m : methods)
	//			if(m.getName().equals(methodname))
	//				target=m;
	//		return target;
	//	}

	@Test
	public void testMethod() {
		//setObj();
		if(targetmethod==null){ //생성자 테스트인 경우.
			constructor_test();
			return;
		}

		Object testresult=null;
		System.out.println( "\n"+(testnumber++) + " : "+testname +"\n 테스트 클래스 : " +targetclz.getSimpleName());//테스트 번호와 어떤객체로부터  테스트가 이루어지는지출력
		try {

			if(targetmethod!=null)
				targetmethod.setAccessible(true);//private 메소드를 테스트하기 위해

			System.out.println("테스트 메소드 : "+targetmethod.getName()); //메소드 이름출력

			//Method param 셋팅.

			testresult=targetmethod.invoke(classmap.get(targetclz), method_params); 				

			if(expectedResult !=null){
				if(isNeedUnBoxing(expectedResult.getClass())){ //원시값 테스트
					System.out.println( "Assert 결과  (예상값/테스트결과): " +expectedResult +" " +testresult); //예상결과와 실제결과 출력
					//toString 오버라이딩을 통해 객체 상태를 하는 습관을 가진다면, 이곳에 인풋 객체의 상태를 출력가능하다.
					assertThat(testresult,is(expectedResult)); //테스팅 결과를 확인.
				}
				else{//결과가 원시객체가 아닌 임의 객체인경우
					Class type =expectedResult.getClass();
					Field[] flz =type.getDeclaredFields();
					for(Field f: flz){
						if (!f.isSynthetic()){
							f.setAccessible(true);
							Class memeberclz=f.getType();
							System.out.println(memeberclz.getSimpleName()+ " "+f.getName());
							auto_Assert(testresult, f, memeberclz);
						}
					}
				}

			}
		} catch (Exception e){	
			//리플렉션 자체가 java.lang.reflect.InvocationTargetException 익셉션을 던지게 된다.
			//따라서 StackTrace에서 caused 된 에러를 찾아서 캐치해야한다. 아래는 caused된 익셉션 클래스를 읽어온다.
			handleException(e);
		}

		if(targetmethod !=null)//메소드가 정상실행되었다면,
			System.out.println("테스트 완료");
		else {System.out.println("해당 메소드가 존재하지 않습니다."); fail();}//메소드를 탐색했으나 없는경우.
	}
}