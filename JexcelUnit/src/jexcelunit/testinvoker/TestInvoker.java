package jexcelunit.testinvoker;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import jexcelunit.classmodule.PrimitiveChecker;
import jexcelunit.excel.ExcelReader;
import jexcelunit.excel.ExcelResultSaver;
import jexcelunit.excel.MockVO;
import jexcelunit.excel.TestcaseVO;

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
 **/
@SuppressWarnings("rawtypes")
@RunWith(Parameterized.class) //테스트 케이스를 이용할것이다.
public class TestInvoker {
	private static Map<Class, Object> classmap=new HashMap<Class, Object>(); //해쉬맵으로 테스트에 필요한 객체들을 하나씩만 유지한다.
	private static ArrayList<Class> exceptionlist=new ArrayList<Class>();//사용자 정의 예외 클래스들을 담아두는 곳.
	private static Map<String,String> sheets=new HashMap<String,String>();
	protected static HashMap<String,Object> mock=new HashMap<String,Object>();//모크객체 모음
	private static int sheetNum= -1,rowIndex=0,testnumber=0;
	private static boolean[][] success=null;
	private static String[][] result=null;
	private static File file=null;
	private static int[] rowSize=null;
	private static String currentSheet=null;

	//테스트 케이스들을 확인할 method_params
	private String sheet=null;
	private String testname=null;
	private Class targetclz=null;
	private Constructor constructor = null;
	private Object[] constructor_params=null;
	private Method targetmethod=null;
	private Object[] method_params=null;
	private Object expectedResult=null;

	//테스트이름, 테스트할 클래스, 테스트파라미터,  테스트할 메소드이름, 파라미터들,예상결과를 JUnit이 읽어와 실행시키는 부분이다.
	public TestInvoker(String sheet,String testname,Class targetclz,Constructor constructor,Object[] constructor_params,Method targetmethod,Object[] param1,Object expectedResult){
		this.sheet=sheet;
		this.testname= (String)testname;
		this.targetclz=targetclz;
		this.constructor=constructor;
		this.constructor_params=constructor_params;
		this.expectedResult=expectedResult;
		this.targetmethod=targetmethod;
		this.method_params=param1;
	}


	/*
	 * 1. 어노테이션으로 엑셀 path를 읽어옴.
	 * 2. 받아온  path는  다시 저장할때 사용.
	 * 3. suite가 가진 row index를 또 저장해야하는가 .
	 * 4. 그럴바에 suiteInfo 라는 맴버클래스를 둬서 관리하는게 나을려나.
	 * */
	public static Collection parmeterizingExcel(String filePath) throws InstantiationException{
		
		//메타데이터를 참조할 수 밖에없다.
		//핸들러 레벨에서 타겟 프로젝트 정보를 생성할것.
		file = new File(filePath);
		ArrayList<ArrayList<TestcaseVO>> testcases=null;
		Object[][] parameterized= null;

		if(file.exists()){
			try {
				ExcelReader reader = new ExcelReader(filePath);
				testcases = reader.readExcel();

				if(testcases.size()>0){

					int total_row_index=0, maxRow=0,suiteNum=0;
					rowSize=new int[testcases.size()]; //suite별  rowSize를 저장할것.
					for(ArrayList<TestcaseVO> testcase : testcases){
						int size= testcase.size();
						rowSize[suiteNum++]=size;
						total_row_index+=size;
						if(size>maxRow) maxRow=size;
					}
					parameterized = new Object[total_row_index][8];
					//init success
					success= new boolean[testcases.size()][maxRow];//성공여부저장할것
					for(int i=0; i<testcases.size(); i++)
						Arrays.fill(success[i], true);

					result= new String[testcases.size()][maxRow];//결과값 저장.


					//Setting SheetNames and TestMode.
					ArrayList<String> sheetModes=reader.getTestSheetMode();
					if( sheetModes.size() != testcases.size()) throw new InstantiationException("Check Sheet Info");

					for(int i=0; i<sheetModes.size(); i++){
						ArrayList<TestcaseVO> testcase = testcases.get(i);
						if(testcase.size()>0)
							sheets.put(testcase.get(0).getSheetName(), sheetModes.get(i));
						else throw new InstantiationException("There's no Test Case in the Sheet : "+testcase.get(0).getSheetName());
					}

					//Set @Parameters.
					int row_index=0;
					for(ArrayList<TestcaseVO> testcase : testcases){
						if(row_index < total_row_index){
							for(TestcaseVO currentCase: testcase){
								parameterized[row_index][0]=currentCase.getSheetName();
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

				//setUp Mock Object
				ArrayList<MockVO> mockList= reader.readMocks();
				for(MockVO mock : mockList){
					//make Mock and Put.
					System.out.println(mock.getMockName());
					System.out.println(mock.getConstructor());
					ArrayList<Object> consParams= mock.getConsParams();
					if(consParams!=null)
					for(Object obj :consParams) {
						System.out.println(obj);
					}
					Map<Field,Object> fieldSet = mock.getFieldSet();
					if(fieldSet !=null) {
						for(Field f : fieldSet.keySet()) {
							System.out.println(f.getName() + " : " + fieldSet.get(f));
						}
					}
					
				}
			} catch (Exception e) {
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



	@Before
	public void setObj(){
		if(currentSheet !=sheet){ 
			if(sheets.get(sheet).equals("Scenario")) //새로운 시나리오 테스트
				classmap.clear();
			rowIndex=0; //행 초기화
			sheetNum++;
		}

		if(sheets.get(sheet).equals("Scenario")){
			if(!classmap.containsKey(targetclz)&& targetmethod !=null){ //시나리오 테스트에서 실행할 객체가 없는경우
				makeTestInstance();
			}	
		}
		else if(sheets.get(sheet).equals("Units")){
			if(classmap.containsKey(targetclz))
				classmap.remove(targetclz);
			makeTestInstance();
		}

	}
	private void makeTestInstance(){
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

	private Object[] getMock(Class[] types, Object[] params){
		for(int i= 0; i<types.length; i++){
			Class paramClass=params[i].getClass();
			if(PrimitiveChecker.isPrimitiveOrWrapper(paramClass))
				paramClass= PrimitiveChecker.unWrapping(paramClass);

			if(!types[i].equals(paramClass)){ // 래핑 처리 후에도 타입이 같지 않은 경우. 1. primitive 타입과 wrapper 타입의 차이.	
				Object mockObject=mock.get(params[i]);
				if( types[i].isInstance(mockObject) && mockObject!=null){
					params[i]=mockObject;
				}else
					throw new AssertionError("Wrong Parameter Types");
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
		}catch(AssertionError e){
			success[sheetNum][rowIndex-1]=false;
			throw(e);
		}
		catch(Exception e){handleException(e);}
	}


	private void assertion(Object testResult, Object expectedResult, Class resultType) throws IllegalArgumentException, IllegalAccessException, AssertionError{
		if(PrimitiveChecker.isPrimitiveOrWrapper(testResult.getClass())){ //원시값 테스트
			System.out.println( "Assert 결과  (예상값/테스트결과): " +expectedResult+" " +testResult); //예상결과와 실제결과 출력
			//toString 오버라이딩을 통해 객체 상태를 하는 습관을 가진다면, 이곳에 인풋 객체의 상태를 출력가능하다.
			switch(PrimitiveChecker.getFloatingType(testResult.getClass())){
			case 1:
				assertThat(new Double(Float.toString((float)testResult)),
						is(closeTo(new Double(Float.toString((float)expectedResult)), 0.00001)));
				break;
			case 0:
				assertThat((double)testResult,is(closeTo((double)expectedResult, 0.00001)));
				break;
			default:
				assertThat(testResult,is(expectedResult));
			}


		}
		else{//결과가 원시객체가 아닌 임의 객체인경우
			Field[] flz =resultType.getDeclaredFields();

			for(Field f: flz){
				if (!f.isSynthetic()){
					f.setAccessible(true);
					Class memberclz=f.getType();
					System.out.println(memberclz.getSimpleName()+ " "+f.getName());
					try {
						auto_Assert(testResult, f, memberclz);
					} catch (IllegalArgumentException | IllegalAccessException e) {handleException(e);}
				}
			}
		}
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
	private void auto_Assert(Object testresult, Field f,Class memberclz ) throws IllegalArgumentException, IllegalAccessException, AssertionError{

		if(PrimitiveChecker.isPrimitiveOrWrapper(memberclz) ){ //일반 원소비교
			System.out.println( "Assert 결과  (예상값/테스트결과): "+f.get(expectedResult)+ " "+f.get(testresult));
			switch(PrimitiveChecker.getFloatingType(f.get(testresult).getClass())){
			case 1: //Float
				assertThat(new Double(Float.toString((float)f.get(testresult))),
						is(closeTo(new Double(Float.toString((float)f.get(expectedResult))), 0.00001)));
				break;
			case 0://Double
				assertThat((double)f.get(testresult),is(closeTo((double)f.get(expectedResult), 0.00001)));
				break;
			default:
				assertThat(f.get(testresult),is(equalTo(f.get(expectedResult))));
			}
		}
		else if(memberclz.isArray()){//배열원소 비교
			if(Array.getLength(f.get(testresult)) == Array.getLength(f.get(expectedResult))){
				for(int i= 0; i<Array.getLength(f.get(testresult)); i++){
					if(Array.get(f.get(testresult), i)!=null &&Array.get(f.get(expectedResult), i)!=null){
						assertion(Array.get(f.get(testresult), i),Array.get(f.get(expectedResult), i),f.getType());
					}
				}
			}else fail("Array Length가 일치하지 않습니다.");
		}else if(Collection.class.isInstance(f.get(expectedResult))){//컬렉션 원소 비교
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
		}else fail("There's Custom Object Field.");
		//재귀 필요.
	}	

	/* 이슈 
	 *  모크객체인데 모크객체가 primitive 타입인경우 ? isMock 플래그를 두는게 좋은거같은데
	 * */
	@Test
	public void testMethod() throws Throwable {
		currentSheet=sheet;
		rowIndex++;
		//setObj();
		if(targetmethod==null){ //생성자 테스트인 경우.
			constructor_test();
			return;
		}

		Object testResult=null;
		System.out.println( "\n"+(testnumber++) + " : "+testname +"\n 테스트 클래스 : " +targetclz.getSimpleName());//테스트 번호와 어떤객체로부터  테스트가 이루어지는지출력

		if(targetmethod!=null)
			targetmethod.setAccessible(true);//private 메소드를 테스트하기 위해

		System.out.println("테스트 메소드 : "+targetmethod.getName()); //메소드 이름출력
		//Method param 모크객체 셋팅.
		Class[] paramsTypes= targetmethod.getParameterTypes();
		Object[] params= getMock(paramsTypes, method_params);
		try {			

			testResult=targetmethod.invoke(classmap.get(targetclz), params);

			if(targetmethod.getReturnType()==null ||targetmethod.getReturnType().equals(void.class));
			else{
				Class[] type=null;Object[] returnObj=null;
				if(testResult !=null){
					//모크 셋업
					type= new Class[1];
					result[sheetNum][rowIndex-1]=testResult.toString();
					type[0]= testResult.getClass();//실제 리턴타입
				}				

				if(expectedResult !=null){
					returnObj=new Object[1];
					returnObj[0]=expectedResult;
					if(type!=null)
						if(!type[0].equals(expectedResult.getClass())){//예상값이 mock객체인경우.
							returnObj=getMock(type,returnObj);
							expectedResult=returnObj[0];
						}
				}
				//검증
				assertion(testResult,expectedResult,type[0]);
			}

		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			success[sheetNum][rowIndex-1]=false;
			if(exceptionlist.contains(e.getClass())){
				result[sheetNum][rowIndex-1]="Method Exception Occurred";
				StackTraceElement[] elem =new StackTraceElement[1];			
				elem[0]=new StackTraceElement(targetclz.getName(), targetmethod.getName(), targetclz.getCanonicalName(),1);
				e.setStackTrace(elem);
				throw(e);
			}
			else {result[sheetNum][rowIndex-1]="InitError : Check Cell's data or Custom Exception";
			// TODO Auto-generated catch block
			Throwable fillstack=e.fillInStackTrace();
			Throwable cause=null;
			if(fillstack !=null){
				cause= fillstack.getCause(); 
				if(cause!=null) cause.printStackTrace();
				fail();
				throw(cause);
			}//Method Exception.
			}
		}catch(AssertionError e){
			success[sheetNum][rowIndex-1]=false;
			//정확한 라인 찾기 이슈..
			StackTraceElement[] elem =new StackTraceElement[1];			
			elem[0]=new StackTraceElement(targetclz.getName(), targetmethod.getName(), targetclz.getCanonicalName(),1);
			e.setStackTrace(elem);
			throw(e);
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
	//성공여부 및 결과 저장.
	@AfterClass
	public static void log(){
		try {
			ExcelResultSaver save=new ExcelResultSaver(file.getCanonicalPath());
			Set<String> sheetNames=sheets.keySet();
			Iterator sit= sheetNames.iterator();
			for(int i=0; i<=sheetNum&&sit.hasNext(); i++){
				String sheetname=(String)sit.next();
				//				System.out.println(sheetname);
				save.writeResults(sheetname, rowSize[i], result[i], success[i]);
			}
			save.write();
			save.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}