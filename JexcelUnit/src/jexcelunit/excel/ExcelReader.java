package jexcelunit.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jexcelunit.classmodule.PrimitiveChecker;

/*
 * Created : 2017.02.25
 * Vendor : Taehoon Seo
 * Description : Read Excel Sheet and Convert to Testcases set.
 * 
 * */
@SuppressWarnings("rawtypes")
public class ExcelReader {
	public final Attribute[] TESTDATASET = Attribute.values();

	private HashMap<String,String> classFullNames = new HashMap<>();
	private DataFormatter formatter=new DataFormatter();
	private FileInputStream inputstream=null;
	private ArrayList<String> testModes= new ArrayList<String>();

	public ExcelReader(){

	}

	public XSSFWorkbook getWorkbook(String filePath) throws IOException{
		//open Excel File.
		File file = new File(filePath);
		inputstream = new FileInputStream(file);
		return new XSSFWorkbook(inputstream);
	}

	private void setClassMap(XSSFSheet clzmetSheet){
		XSSFRow clzNames= clzmetSheet.getRow(0);
		Iterator<Cell> it= clzNames.cellIterator();
		Cell cell=null;
		while(it.hasNext()){
			cell=it.next();
			String fullString = formatter.formatCellValue(cell);
			String key= fullString.substring(fullString.lastIndexOf(".")+1, fullString.length());
			classFullNames.put(key, fullString);
		}
	}
	public ArrayList<String> getTestSheetMode(){
		return testModes;
	}
	/*
	 * Excel Reading Issue
	 * */
	public ArrayList<ArrayList<TestcaseVO>> readExcel(String filePath) throws IOException{
		ArrayList<ArrayList<TestcaseVO>> caselists= new ArrayList<ArrayList<TestcaseVO>>();
		XSSFWorkbook workbook = null;
		workbook=getWorkbook(filePath);

		if(workbook!=null){
			//Read ClassName. and main Tain Map<Simple Name,Full Name>
			XSSFSheet clzhidden= workbook.getSheet("ClassMethodhidden");
			if(clzhidden!=null){
				setClassMap(clzhidden);
			}

			//Read sheet
			for(int sheet_index=0;sheet_index<workbook.getNumberOfSheets();sheet_index++){

				if(!workbook.isSheetHidden(sheet_index)){
					ArrayList<TestcaseVO> caselist=new ArrayList<TestcaseVO>();
					XSSFSheet xssfsheet = null;
					xssfsheet=workbook.getSheetAt(sheet_index);

					//Read First Line and Check Test Mode.
					XSSFRow firstRow = xssfsheet.getRow(0);
					XSSFCell testModeCell= firstRow.getCell(1);
					if(testModeCell==null) testModes.add("Scenario");
					else{
						String testMode= formatter.formatCellValue(testModeCell);
						testModes.add(testMode);
					}

					//Read Second line and set vo info.
					XSSFRow secondRow = xssfsheet.getRow(1);
					XSSFCell infocell = null;
					int colSize= secondRow.getPhysicalNumberOfCells();
					int[] voOption = new int[colSize];
					for(int i=0; i < colSize; i++){
						infocell= secondRow.getCell(i);

						for(int j =0; j < TESTDATASET.length; j++){
							if(infocell.getStringCellValue().contains(TESTDATASET[j].toString())){						
								//remember index
								voOption[i]=j;
								break;
							}	
						}
					}

					//loop to convert data to VO Object. Except First, Second Row.
					for(int i= 2; i<xssfsheet.getPhysicalNumberOfRows(); i++){
						XSSFRow currentRow= xssfsheet.getRow(i);
						TestcaseVO vo= new TestcaseVO();
						vo.setSheetName(xssfsheet.getSheetName());
						//get Cell Values
						if(currentRow.getCell(0)!=null){
							for(int j= 0 ; j<colSize; j ++){
								XSSFCell currentCell = currentRow.getCell(j);	
								if(currentCell !=null){
									//Set vo Values.
									setVOvalue(vo,TESTDATASET[voOption[j]],workbook,currentCell);
								}
							}
						}
						caselist.add(vo);
					}
					caselists.add(caselist); //save sheets		

				}

			}
			// ArrayList<TestCaseVO> 의 List형태로 리턴.
			//Invoker에서 global한 Suite Index를 두어 Parameterize 리턴함수가 다른 배열을 리턴하도록.
			workbook.close();
		}
		if(inputstream !=null)
			inputstream.close();

		return caselists;
	} 

	//Set Vo values depend on first Row
	private void setVOvalue(TestcaseVO vo , Attribute OPTION, XSSFWorkbook workbook, XSSFCell currentCell){
		//change values to String


		switch(OPTION){
		case TestName: //테스트 이름
			vo.setTestname(formatter.formatCellValue(currentCell));
			break;
		case TestClass: //Set Class and Set Constructor
			String fullcons= formatter.formatCellValue(currentCell);//셀값 원본.
			String clzname =fullcons.substring(0, fullcons.indexOf('(')); //Class name만 분리

			String classFullname= classFullNames.get(clzname);//패키지이름을 포함한 클래스명.

			//파라미터 분리.
			String paramFullText =fullcons.substring(fullcons.indexOf('(')+1,fullcons.indexOf(')'));
			String[] paramsText=paramFullText.equals("")?new String[]{}:paramFullText.split(",");

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
						vo.setConstructor(con);
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
		case ConsParam:

			//Extract Parameter Type and convert Object or Mock Object;
			String con_paramString= formatter.formatCellValue(currentCell);//String value

			Class[] con_paramTypes= null;
			con_paramTypes= vo.getCons_param();
			try{
				if(con_paramTypes==null && con_paramString !=null){
					throw new Exception("Detected Wrong Constructor Parameter.\n at "+currentCell.getSheet().getSheetName()+"\n at "+(currentCell.getRowIndex()+1));
				}

				if(con_paramTypes.length>0&& con_paramTypes!=null){
					int index= vo.getConstructorParams().size();
					Class con_targetType =con_paramTypes[index];
					Object conparam=PrimitiveChecker.convertObject(con_targetType,con_paramString);
					vo.addConstructorParam(conparam);
				}}catch(Exception e){
					e.printStackTrace();
				}
			break;
		case TestMethod://Set Method and Parameter Types
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
					Class[] params =null;
					params= met.getParameterTypes();
					if(params==null && metParamsText.length==0){ find =true;}
					else if(params.length==metParamsText.length){
						for(int mp_index=0; mp_index<metParamsText.length; mp_index++){
							String paramType = metParamsText[mp_index].split(" ")[0];
							if(!params[mp_index].getSimpleName().equals(paramType))
							{
								find=false;//Wrong
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
		case MetParam: //Extract parameter and convert Object

			String met_paramString= formatter.formatCellValue(currentCell);//String value
			Class[] met_paramTypes= null;
			met_paramTypes=vo.getMet_param();
			try{
				if(met_paramTypes==null && met_paramString !=null){
					throw new Exception("Detected Wrong Method Parameter.\n at "+currentCell.getSheet().getSheetName()+"\n at "+(currentCell.getRowIndex()+1));
				}
				if(met_paramTypes.length>0&&met_paramTypes !=null){
					int met_param_index= vo.getMethodParams().size();
					Class met_targetType=met_paramTypes[met_param_index]; //Current Param Type

					Object metparam=PrimitiveChecker.convertObject(met_targetType,met_paramString);
					vo.addMethodParam(metparam);		
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			break;
		case Expected:
			String expectString= formatter.formatCellValue(currentCell);//String value
			Class returnType = vo.getMet().getReturnType();			
			if(!returnType.equals(void.class) || returnType !=null){
				Object expect= PrimitiveChecker.convertObject(returnType, expectString);
				vo.setExpect(expect);	
			}
			break;
		case Result:
			break;
		case Success:
			break;
		}
	}


}
