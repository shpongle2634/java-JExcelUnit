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
import java.util.Iterator;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import jexcelunit.excel.ExcelReader;
import jexcelunit.excel.TestcaseVO;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/*****
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
@SuppressWarnings("rawtypes")
@RunWith(Parameterized.class) //테스트 케이스를 이용할것이다.
public class TestInvoker {
	private static Map<Class, Object> classmap= new HashMap<Class, Object>(); //해쉬맵으로 테스트에 필요한 객체들을 하나씩만 유지한다.
	private static ArrayList<Class> exceptionlist=new ArrayList<Class>();//사용자 정의 예외 클래스들을 담아두는 곳.
	//	private static Method[] methods; //테스트할 객체의 메소드를 받는부분
	protected static HashMap<String,Object> mock=new HashMap<String,Object>();//모크객체 모음
	private static int suitenumber=0;
	private static int testnumber=0; //테스트 run 넘버

	//테스트 케이스들을 확인할 method_params
	private int suite;
	private String testname=null;
	private Class targetclz=null;
	private Constructor constructor = null;
	private Object[] constructor_params=null;
	private Method targetmethod=null;
	private Object[] method_params=null;
	private Object expectedResult=null;


	//테스트이름, 테스트할 클래스, 테스트파라미터,  테스트할 메소드이름, 파라미터들,예상결과를 JUnit이 읽어와 실행시키는 부분이다.
	public TestInvoker(int suite,String testname,Class targetclz,Constructor constructor,Object[] constructor_params,Method targetmethod,Object[] param1,Object expectedResult){
		this.suite=suite;
		this.testname= (String)testname;
		this.targetclz=targetclz;
		this.constructor=constructor;
		this.constructor_params=constructor_params;
		this.expectedResult=expectedResult;
		this.targetmethod=targetmethod;
		this.method_params=param1;
	}

	public static Collection parmeterizingExcel(String fileName){
		ExcelReader reader = new ExcelReader();
		//메타데이터를 참조할 수 밖에없다.
		//핸들러 레벨에서 타겟 프로젝트 정보를 생성할것.
		File file = new File(".");
		ArrayList<ArrayList<TestcaseVO>> testcases=null;
		Object[][] parameterized= null;

		if(file.exists()){
			try {
				testcases = reader.readExcel(fileName, file.getCanonicalPath());

				if(testcases.size()>0)
				{
					int total_row_index=0;
					for(ArrayList<TestcaseVO> testcase : testcases){
						total_row_index+=testcase.size();
					}
					parameterized = new Object[total_row_index][8];

					int row_index=0;
					for(ArrayList<TestcaseVO> testcase : testcases){
						if(row_index < total_row_index){
							for(TestcaseVO currentCase: testcase){
								parameterized[row_index][0]=currentCase.getSuiteNumber();
								parameterized[row_index][1]=currentCase.getTestname();
								parameterized[row_index][2]=currentCase.getTestclass();
								parameterized[row_index][3]=currentCase.getConstructor();
								parameterized[row_index][4]=currentCase.getConstructorParams().toArray();
								parameterized[row_index][5]=currentCase.getMet();
								parameterized[row_index][6]=currentCase.getMethodParams().toArray();
								parameterized[row_index][7]=currentCase.getExpect();	
								row_index++;
							}
						}
					}

				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//읽어들인 리스트를 String, Class, Object[] Object, String Object로 바까야함.
		return Arrays.asList(parameterized);
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

	private Class unBoxing(Class wrapper){
		switch(wrapper.getTypeName().charAt(10)){
		case 'S': return wrapper.getTypeName().contains("Short")?short.class:String.class;							
		case 'B': return wrapper.getTypeName().contains("Byte")?Byte.class:boolean.class;
		case 'C':return char.class;
		case 'I':return int.class;
		case 'L':return long.class;
		case 'D':return double.class;
		case 'F':return float.class;
		case 'V':return void.class;
		default : return null;
		}
	}


	@Before
	public void setObj(){
		if(suitenumber !=suite){ //새로운 시나리오 테스트.
			classmap.clear();
		}
		if(!classmap.containsKey(targetclz)&& targetmethod !=null){ //실행할 객체가 없는경우
			//System.out.println(classmap.containsKey(targetclz)+"새로생성");
			constructor.setAccessible(true);
			try {
				if(constructor_params.length==0)
					classmap.put(targetclz, constructor.newInstance());

				else{
					Class[] paramTypes=constructor.getParameterTypes();
					Object[] params= getMock(paramTypes,constructor_params);
					classmap.put(targetclz, constructor.newInstance(params));
				}

			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				// TODO Auto-generated catch block
				handleException(e);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private Object[] getMock(Class[] types, Object[] params){
		for(int i= 0; i<types.length; i++){
			Class paramClass=params[i].getClass();
			if(isNeedUnBoxing(paramClass))
				paramClass= unBoxing(paramClass);

			if(!types[i].equals(paramClass)){
				Object mockObject=mock.get(params[i]);
				if(mockObject.getClass().equals(types[i]) && mockObject!=null){
					params[i]=mockObject;
				}else
					throw new IllegalArgumentException("Wrong Argument Type");
			}
		}
		return params;
	} 

	private void constructor_test(){
		System.out.println( "\n"+(testnumber++) + " : "+testname +"\n 테스트 클래스 : " +targetclz.getSimpleName());//테스트 번호와 어떤객체로부터  테스트가 이루어지는지출력
		try{
			//
			constructor.setAccessible(true);

			if(constructor_params.length==0)
				assertNotNull(constructor.newInstance());

			else{
				//타입이 안맞으면 mock 객체 가져올것.
				Class[] paramTypes=constructor.getParameterTypes();
				Object[] params= getMock(paramTypes,constructor_params);
				assertNotNull(constructor.newInstance(params));
			}

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
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 ********************************************************************** */
	private void auto_Assert(Object testresult, Field f,Class memberclz ) throws IllegalArgumentException, IllegalAccessException{
		if(isNeedUnBoxing(memberclz) ){
			System.out.println( "Assert 결과  (예상값/테스트결과): "+f.get(expectedResult)+ " "+f.get(testresult));
			assertThat(f.get(testresult),is(f.get(expectedResult)));
		}
		else if(memberclz.isArray()){//배열원소 비교
			if(Array.getLength(f.get(testresult)) == Array.getLength(f.get(expectedResult))){
				for(int i= 0; i<Array.getLength(f.get(testresult)); i++){
					if(Array.get(f.get(testresult), i)!=null &&Array.get(f.get(expectedResult), i)!=null){
						System.out.println( "Assert 결과  (예상값/테스트결과): "+Array.get(f.get(expectedResult), i)+ " "+Array.get(f.get(testresult), i));
						assertThat(Array.get(f.get(testresult), i), is(Array.get(f.get(expectedResult), i)));
					}
				}
			}
		}else if(Collection.class.isInstance(f.get(expectedResult))){
			Collection expect=(Collection) f.get(expectedResult);
			Collection result=(Collection) f.get(testresult);
			Iterator ex_it = expect.iterator();
			Iterator re_it = result.iterator();
			while(ex_it.hasNext() && re_it.hasNext()){
				Object ex=ex_it.next();
				Object re=re_it.next();
				System.out.println( "Assert 결과  (예상값/테스트결과): "+ex +" "+ re);
				assertThat(re, is(ex));
			}
		}
	}	

	@Test
	public void testMethod() throws Throwable {
		//setObj();
		if(targetmethod==null){ //생성자 테스트인 경우.
			constructor_test();
			return;
		}

		Object testresult=null;
		System.out.println( "\n"+(testnumber++) + " : "+testname +"\n 테스트 클래스 : " +targetclz.getSimpleName());//테스트 번호와 어떤객체로부터  테스트가 이루어지는지출력


		if(targetmethod!=null)
			targetmethod.setAccessible(true);//private 메소드를 테스트하기 위해

		System.out.println("테스트 메소드 : "+targetmethod.getName()); //메소드 이름출력
		//Method param 모크객체 셋팅.
		Class[] paramsTypes= targetmethod.getParameterTypes();
		Object[] params= getMock(paramsTypes, method_params);
		try {			
			testresult=targetmethod.invoke(classmap.get(targetclz), params);
				
			if(expectedResult !=null){
				if(isNeedUnBoxing(testresult.getClass())){ //원시값 테스트
					System.out.println( "Assert 결과  (예상값/테스트결과): " +expectedResult +" " +testresult); //예상결과와 실제결과 출력
					//toString 오버라이딩을 통해 객체 상태를 하는 습관을 가진다면, 이곳에 인풋 객체의 상태를 출력가능하다.
					assertThat(testresult,is(expectedResult)); //테스팅 결과를 확인.
				}
				else{//결과가 원시객체가 아닌 임의 객체인경우
					Class[] type =new Class[1];
					type[0]=testresult.getClass();//실제 리턴타입
					Object[] returnObj=new Object[1];
					returnObj[0]=expectedResult;
					if(!type[0].equals(expectedResult.getClass())){//예상값이 mock객체인경우.
						returnObj=getMock(type,returnObj);
						expectedResult=returnObj[0];
					}
					Field[] flz =type[0].getDeclaredFields();

					for(Field f: flz){
						if (!f.isSynthetic()){
							f.setAccessible(true);
							Class memberclz=f.getType();
							System.out.println(memberclz.getSimpleName()+ " "+f.getName());
							try {
								auto_Assert(testresult, f, memberclz);
							} catch (IllegalArgumentException | IllegalAccessException e) {
								// TODO Auto-generated catch block
								handleException(e);
							}
						}
					}
					
					
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			Throwable fillstack=e.fillInStackTrace();
			Throwable cause=null;
			if(fillstack !=null){
				cause= fillstack.getCause(); 
				if(cause!=null) cause.printStackTrace();
				throw(cause);
			}//Method Exception.
		}catch(AssertionError e){
			//정확한 라인 찾기 이슈..
			StackTraceElement[] elem =new StackTraceElement[1];			
			elem[0]=new StackTraceElement(targetclz.getName(), targetmethod.getName(), targetclz.getCanonicalName(),1);
			e.setStackTrace(elem);
			throw(e);
		}

	}


	@After
	public void log(){
		//1.로그관리. 엑셀 Success 설정.
		//2.드롭다운 가능한 리스트뷰로. 로그 파일도 만들것.. 정확히 어느 클래스의 어디서 오류가 났는지
	}
}