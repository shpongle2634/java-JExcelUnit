package jexcelunit.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFName;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import jexcelunit.utils.ClassInfo;


/*
 * Created : 2017.02.23
 * Vendor  : Taehoon Seo
 * Description : create excel file including classes, methods and constructor informations.
 * */
public class ExcelCreator implements CommonData{
	private final int CONSTRUCTOR = 0;
	private final int METHOD = 1;
	public final String[] TESTDATASET = {"TestName" ,"TestClass","Constructor Param", "TestMethod", "Method Param", "Expected", "Result", "Success"};
	
	//create xlsx for Testcases.
	public void createXlsx(String projectName ,String rootpath , HashMap<String, ClassInfo> classinfos) throws IOException{
		/*
		 * 테스트 이름, 클래스, 생성자파라미터, 메소드, 메소드파라미터, 리턴, 결과
		 * 1. 다중 파라미터를 어떻게 ? 파라미터 개수륿 분석해서 칼럼 수 조정할것
		 * 2. 모크객체의 이용방법은 어떻게? 모크객체 이름으로 접근하도록을 기본. 모크객체 생성도 엑셀로 지원 가능?
		 * 3. 테스트방식 : 시나리오 or 독립.
		 * 4. 생성파일 네이밍.
		 * 5. 중복파일 처리.
		 * */


		//Check .xlxs file is exists
		boolean will_create=true;
		File root = new File(rootpath);
		File[] filelist=  root.listFiles();
		for(File f: filelist){
			//			System.out.println(f.getName());
			if(f.getName().equals(projectName+".xlsx")){
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				will_create= MessageDialog.openQuestion(
						window.getShell(),
						"Do you want to create new .xlxs or overwrite?",
						"Warnning : If you overwrite, you can lose your data");
				if(will_create)f.delete();
				break;
			}
		}

		//Create
		if(will_create){
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet xssfSheet = workbook.createSheet("TestSuite 1");	
			XSSFRow row =null;
			XSSFCell cell =null;

			//make hidden sheet
			hiddensheet(workbook,classinfos);

			row=xssfSheet.createRow(0);//번줄은 info

			//칼럼의 갯수는 생성자 혹은 메소드 파라미터에 의해 가변적으로 정할것.
			//칼럼에 Data Validation을 지정.
			int consCount=getMaxParamCount(CONSTRUCTOR,classinfos);
			int metsCount=getMaxParamCount(METHOD,classinfos);
			int cellvalindex=0;
			int totalCellCount= TESTDATASET.length + consCount + metsCount-2;
			for(int i =0; i<totalCellCount; i++){
				String val=TESTDATASET[cellvalindex];


				if(val.equals("Constructor Param")){					
					//Set Param Validation Type
					/*
					 * 이슈 정리 : 
					 * 1. 클래스마다 다른 타입 유효성
					 * 2. 클래스마다 다른 메소드 유효성
					 * = >해결 : 히든 시트에 드롭다운 리스트로 사용될 것을 작성할것.
					 * 추후에 그 뭐냐 엑셀 새로생성이 아닐경우, 히든시트 수정. 데이터유효성 재정의 하는 방향으로 가보자..
					 * 3. 테스트 로그를 어떻게 관리할 것인가..
					 * 4. 결국 별도의 테스트 로그를 보거나 할만한 플러그인이 필요하다는건데...
					 * */
					for(int k=0; k<consCount; k++){
						cell=row.createCell(i+k);
						xssfSheet.setColumnWidth(cellvalindex, 4500);
						cell.setCellValue(val+(k+1));
					}
					i+=consCount-1;
					cellvalindex++;
				}
				else if(val.equals("TestMethod")){
					xssfSheet.setColumnWidth(cellvalindex, 3000);
					setValidation("INDIRECT($B2)", xssfSheet, cellvalindex);
					cell=row.createCell(i);
					cell.setCellValue(val);
					cellvalindex++;
				}

				else if(val.equals("Method Param")){

					for(int k=0; k<metsCount; k++){
						cell=row.createCell(i+k);
						xssfSheet.setColumnWidth(cellvalindex, 4000);
						cell.setCellValue(val+(k+1));
					}
					i+=metsCount-1;
					cellvalindex++;
				}
				else{
					xssfSheet.setColumnWidth(cellvalindex, 3000);
					cell=row.createCell(i);
					cell.setCellValue(val);
					cellvalindex++;
				}

			}

			setValidation("Class", xssfSheet, 1);
			//save xlsx
			FileOutputStream fileoutputstream=new FileOutputStream(rootpath+"/"+ projectName+".xlsx");
			//파일을 쓴다
			workbook.write(fileoutputstream);
			if(workbook!=null) workbook.close();
			//필수로 닫아주어야함
			if( fileoutputstream!=null)
				fileoutputstream.close();

			System.out.println("Created");
		}

	}

	// Create Hidden sheet for DataValidation List.
	private void hiddensheet(XSSFWorkbook workbook, HashMap<String,ClassInfo> classinfos){
		XSSFSheet hidden = workbook.createSheet("hidden");
		Set<String> keys = classinfos.keySet();
		ClassInfo info =null;
		XSSFRow firstrow=hidden.createRow(0);

		int col_index=0;
		for(String key : keys){
			info =classinfos.get(key);
			/*
			 * 1. 클래스 -생성자 파라미터타입 유효성검증 	파라미터 개수가 같은데, 타입이 다른경우 ? 제약을 걸기에 모호함. 생성자는 이름이 같으니까..	
			 * 2. 클래스 - 메소드리스트 				ok
			 * 3. 메소드 - 파라미터 타입				1과 같은 이슈.
			 * 4. 클래스 - 리턴타입					파라미터가 같으나 리턴이 다른건 없음. 타입제약을 걸기엔 조금 무리가 있겠는데?
			 * => 결국 제약가능한건 클래스 이름, 클래스에 따른 메소드리스트 정도
			 * */

			firstrow.createCell(col_index).setCellValue(key);
			Set<Method> mets = info.getMethods();

			Iterator<Method> mit =mets.iterator();
			if(mets.size() >0){

				for(int i =1; i <= mets.size() && mit.hasNext(); i ++){
					Method met= mit.next();
					XSSFRow row= hidden.getRow(i);
					if(row == null) row= hidden.createRow(i);
					row.createCell(col_index).setCellValue(met.getName());
				}
				//Set Class-Method Data ReferenceList
				XSSFName namedcell =workbook.createName();
				namedcell.setNameName(info.getClz().getSimpleName()); //Nameing이 중요.
				char currentCol=(char) ('A'+col_index);
				String formula= "hidden!$"+currentCol+"$2:$"+currentCol+"$" + (mets.size()+1);
				System.out.println("Create : " + formula);
				namedcell.setRefersToFormula(formula);

			}
			col_index++;
		}

		//Set Class Data ReferenceList.
		XSSFName namedcell =workbook.createName();
		namedcell.setNameName("Class"); //Nameing이 중요.
		char cell=(char) ('A'+col_index-1);
		String formula= "hidden!$A$1:$"+cell+"$1";
		namedcell.setRefersToFormula(formula);


		//Set hidden Sheet if true=  hidden.
		workbook.setSheetHidden(1, false);

	}

	//Init DataValidation for Update Cell
	private void initValidation(){


	}

	//클래스 이름 제약
	private void setValidation(String namedcell, XSSFSheet xssfSheet ,int col){


		DataValidation dataValidation = null;
		DataValidationConstraint constraint = null;
		DataValidationHelper validationHelper = null;
		validationHelper = new XSSFDataValidationHelper(xssfSheet);
		constraint= validationHelper.createFormulaListConstraint(namedcell);
		CellRangeAddressList addresslist =null;
		if(constraint !=null){
			addresslist = new CellRangeAddressList(1,500,col,col);
			System.out.println(constraint.getFormula1());
			dataValidation= validationHelper.createValidation(constraint, addresslist);


			dataValidation.setSuppressDropDownArrow(true);
			dataValidation.setShowErrorBox(true);
			dataValidation.createErrorBox("Wrong Input", "You must input Right Type.");
			xssfSheet.addValidationData(dataValidation);

		}		
	}


	private int getMaxParamCount(int option,HashMap<String, ClassInfo> classinfos){
		int max= 0;
		Set<String> keys= classinfos.keySet();
		for(String key : keys){
			ClassInfo ci = classinfos.get(key);

			if(option == CONSTRUCTOR){

				for(Constructor con : ci.getConstructors()){
					if( max < con.getParameterCount())
						max = con.getParameterCount();
				}	
			}
			else if(option == METHOD){
				for(Method met : ci.getMethods()){
					if( max < met.getParameterCount())
						max = met.getParameterCount();
				}
			}

		}
		return max;
	}



}
