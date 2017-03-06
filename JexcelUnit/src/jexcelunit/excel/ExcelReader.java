package jexcelunit.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.text.DateFormatter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/*
 * Created : 2017.02.25
 * Vendor : Taehoon Seo
 * Description : Read Excel Sheet and Convert to Testcases set.
 * 
 * */
public class ExcelReader {
	public final int TESTNAME=0, TESTCLASS=1, CONSTPARAM=2, METHOD=3, METHODPARAM=4, EXPECTED=5, RESULT=6, SUCCESS=7;
	public final String[] TESTDATASET = {"TestName" ,"TestClass","Constructor Param", "TestMethod", "Method Param", "Expected", "Result", "Success"};
	private Set<Class> newInstancable= new HashSet<>();
	private HashMap<String,String> classFullNames = new HashMap<>();
	private DataFormatter formatter=new DataFormatter();
	public ExcelReader(){
		newInstancable.add(String.class);
		newInstancable.add(Short.class);
		newInstancable.add(Double.class);
		newInstancable.add(Integer.class);
		newInstancable.add(Short.class);
		newInstancable.add(Float.class);
//		newInstancable.add(short.class);
//		newInstancable.add(double.class);
//		newInstancable.add(int.class);
//		newInstancable.add(short.class);
//		newInstancable.add(float.class);
	}
	/*
	 * Excel Reading Issue
	 * */
	@SuppressWarnings("resource")
	public ArrayList<TestcaseVO> readExcel(String projectname, String rootpath) throws IOException{
		ArrayList<TestcaseVO> caselist= new ArrayList<TestcaseVO>();

		//open Excel File.
		File file = new File(rootpath +'/'+projectname+".xlsx");

		FileInputStream inputstream = new FileInputStream(file);
		XSSFWorkbook workbook = null;
		workbook= new XSSFWorkbook(inputstream);

		if(workbook!=null){
			//Read sheet
			XSSFSheet xssfsheet = null;
			xssfsheet = workbook.getSheetAt(0); //read testcase sheet;

			//Read first line and set vo info.
			XSSFRow firstrow = xssfsheet.getRow(0);
			XSSFCell infocell = null;
			int colSize= firstrow.getPhysicalNumberOfCells();
			int[] voOption = new int[colSize];
			for(int i=0; i < colSize; i++){
				infocell= firstrow.getCell(i);

				for(int j =0; j < TESTDATASET.length; j++){
					if(infocell.getStringCellValue().contains(TESTDATASET[j])){						
						//remember index
						voOption[i]=j;
						break;
					}	
				}
			}

			//Read ClassName.
			XSSFSheet clzhidden= workbook.getSheet("ClassMethodhidden");
			if(clzhidden!=null){
				XSSFRow clzNames= clzhidden.getRow(0);
				Iterator<Cell> it= clzNames.cellIterator();
				Cell cell=null;
				while(it.hasNext()){
					cell=it.next();
					String fullString = formatter.formatCellValue(cell);
					String key= fullString.substring(fullString.lastIndexOf(".")+1, fullString.length());
					classFullNames.put(key, fullString);
				}
			}


			//loop to convert data to VO Object. Except first Row.
			for(int i= 1; i<xssfsheet.getPhysicalNumberOfRows(); i++){
				XSSFRow currentRow= xssfsheet.getRow(i);
				TestcaseVO vo= new TestcaseVO();

				//get Cell Values
				if(!"".equals(currentRow.getCell(0).getStringCellValue())){
					for(int j= 0 ; j<colSize; j ++){
						XSSFCell currentCell = currentRow.getCell(j);	
						if(currentCell !=null){
							//Set vo Values.
							setVOvalue(vo,voOption[j],workbook,currentCell);
						}
					}
				}
				caselist.add(vo);
			}
			workbook.close();
		}
		inputstream.close();

		return caselist;
	} 

	//Set Vo values depend on first Row
	private void setVOvalue(TestcaseVO vo , int OPTION,XSSFWorkbook workbook, XSSFCell currentCell){
		//change values to String


		switch(OPTION){
		case TESTNAME: //테스트 이름
			vo.setTestname(formatter.formatCellValue(currentCell));
			break;
		case TESTCLASS: //Set Class and Set Constructor
			String fullcons= formatter.formatCellValue(currentCell);//셀값 원본.
			String clzname =fullcons.substring(0, fullcons.indexOf('(')); //Class name만 분리

			String classFullname= classFullNames.get(clzname);//패키지이름을 포함한 클래스명.

			//파라미터 분리.
			String paramFullText =fullcons.substring(fullcons.indexOf('('),fullcons.indexOf(')'));
			String[] paramsText= paramFullText.split(",");

			try {
				Class testClass= Class.forName(classFullname);
				vo.setTestclass(testClass);

				Constructor[] cons =testClass.getDeclaredConstructors();//생성자를 찾음.
				for(Constructor con : cons){
					boolean find=true;
					Class[] params = con.getParameterTypes();//파라미터분리.
					if(params.length==paramsText.length){
						for(int index=0; index<paramsText.length; index++){
							String paramType = paramsText[index].split(" ")[0];
							if(!params[index].getSimpleName().equals(paramType))
							{
								find=false;//Wrong Constructor
								break;
							}	
						}	
					}else find=false;
					if(find){ //생성자 찾음.
						vo.setCon(con);
						if(params!=null || params.length!=0)
							vo.setCons_param(params);
						break;
					}
				}


			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case CONSTPARAM:
			//Extract Parameter Type and convert Object or Mock Object;
			String con_paramString= formatter.formatCellValue(currentCell);//String value

			Class[] con_paramTypes= vo.getCons_param();
			if(con_paramTypes.length!=0|| con_paramTypes!=null){
				int index= vo.getConstructorParams().size();
				Class con_targetType=con_paramTypes[index-1];
				Object conparam=convertObject(con_targetType,con_paramString);
				vo.addConstructorParam(conparam);
			}
			break;
		case METHOD://Set Method and Parameter Types
			String fullmet= formatter.formatCellValue(currentCell);
			String metName =fullmet.substring(fullmet.indexOf(' ')+1, fullmet.indexOf('(')); //Method name 추출

			String metParamFullText =fullmet.substring(fullmet.indexOf('(')+1,fullmet.indexOf(')'));
			String[] metParamsText=metParamFullText.equals("")?new String[]{}:metParamFullText.split(",");

			Class metclass=vo.getTestclass();
			Method[] mets=null;
			mets=metclass.getDeclaredMethods();
			for(Method met : mets){
				boolean find=true;
				if(metName.equals(met.getName())){
					Class[] params = met.getParameterTypes();
					if(params.length==metParamsText.length){
						for(int mp_index=0; mp_index<metParamsText.length; mp_index++){
							String paramType = metParamsText[mp_index].split(" ")[0];
							if(!params[mp_index].getSimpleName().equals(paramType))
							{
								find=false;//Worng
								break;
							}	
						}	
					}else find=false;
					if(find){
						vo.setMet(met);
						if(params.length!=0&&params!=null)
							vo.setMet_param(params);
						break;
					}	
				}
			}

			break;
		case METHODPARAM: //Extract parameter and convert Object
			String met_paramString= formatter.formatCellValue(currentCell);//String value
			Class[] met_paramTypes= vo.getCons_param();
			if(met_paramTypes.length!=0&&met_paramTypes!=null){
				int met_param_index= vo.getConstructorParams().size();
				Class met_targetType=met_paramTypes[met_param_index-1]; //Current Param Type

				Object metparam=convertObject(met_targetType,met_paramString);
				vo.addMethodParam(metparam);		
			}
			break;
		case EXPECTED:
			String expectString= formatter.formatCellValue(currentCell);//String value
			Class returnType = vo.getMet().getReturnType();			
			if(!returnType.equals(void.class) || returnType !=null){
				Object expect= convertObject(returnType, expectString);
				vo.setExpect(expect);	
			}
			break;
		case RESULT:
			break;
		case SUCCESS:
			break;
		}
	}

	//	private void convertStringToObject(String type){
	//		if(type.equals("int"))
	//			
	//			||type.equals("Integer")|| type.equals("double")|| type.equals("Double")|| type.equals("Float")||type.equals("float")||type.equals("Number"))
	//			
	//		
	//	}

	private Object convertObject(Class targetType,String paramString){
		try {
			Object paramObject=null;
			if(newInstancable.contains(targetType))
			{
				paramObject=targetType.getConstructor(String.class).newInstance(paramString);
				return paramObject;
			}	
			else if(targetType.equals(char.class))
				return paramString.toCharArray()[0];
			else if(targetType.equals(char[].class))
				return paramString.toCharArray();
			else if(targetType.equals(int.class))	return Integer.parseInt(paramString);
			else if(targetType.equals(double.class))	return Integer.parseInt(paramString);
			else if(targetType.equals(float.class))	return Integer.parseInt(paramString);
			else if(targetType.equals(short.class))	return Integer.parseInt(paramString);
			else if(targetType.equals(Date.class))	
			{
				return new Date();
				
			}	
			else //mock;
			{
				Constructor con =null;
				con= targetType.getConstructor(String.class);
				if(con !=null){
					paramObject=con.newInstance(paramString);
					return paramObject;
				}else
					return paramString;
			}	
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
