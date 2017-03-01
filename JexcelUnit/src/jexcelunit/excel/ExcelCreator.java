package jexcelunit.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
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
						xssfSheet.setColumnWidth(i+k, 4500);
						cell.setCellValue(val+(k+1));
						
					}
					i+=consCount-1;
					cellvalindex++;
				}
				else if(val.equals("TestMethod")){
					xssfSheet.setColumnWidth(i, 3000);
					setValidation("INDIRECT(LEFT($B2,FIND(\"(\",$B2)-1))", xssfSheet, i);
					cell=row.createCell(i);
					cell.setCellValue(val);
					cellvalindex++;
				}

				else if(val.equals("Method Param")){

					for(int k=0; k<metsCount; k++){
						cell=row.createCell(i+k);
						xssfSheet.setColumnWidth(i+k, 4000);
						cell.setCellValue(val+(k+1));
					}
					i+=metsCount-1;
					cellvalindex++;
				}
				else{
					xssfSheet.setColumnWidth(i, 3000);
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

	/*
	 * 1. 클래스 -생성자 파라미터타입 유효성검증 	파라미터 개수가 같은데, 타입이 다른경우 ? 제약을 걸기에 모호함. 생성자는 이름이 같으니까..	
	 * 2. 클래스 - 메소드리스트 				ok
	 * 3. 메소드 - 파라미터 타입				1과 같은 이슈.
	 * 4. 클래스 - 리턴타입					파라미터가 같으나 리턴이 다른건 없음. 타입제약을 걸기엔 조금 무리가 있겠는데?
	 * => 결국 제약가능한건 클래스 이름, 클래스에 따른 메소드리스트 정도
	 * => 코멘트를 이용하면 괜찮! 그리고 리스트제약을 풀네임으로 하면 가능하다.
	 * => 클래스 선택또한  생성자 포함해서 입력하도록 하자.
	 * 
	 * @@ 파라미터 이름까지는 리플렉션으로 얻을 수가 없다고한다 카더라. 외부 라이브러리를 써야하나..
	 * */

	// Create Hidden sheet for DataValidation List.
	private void hiddensheet(XSSFWorkbook workbook, HashMap<String,ClassInfo> classinfos){
		//생성자리스트 및 생성자 파라미터 유효성 -완료
		XSSFSheet cons_param_sheet = workbook.createSheet("ConstructorParamhidden");
		//메소드-파라미터 유효성
		XSSFSheet method_param_sheet = workbook.createSheet("MethodParamhidden");
		//클래스리스트 및 메소드 이름 유효성-완료
		XSSFSheet class_method_sheet = workbook.createSheet("ClassMethodhidden");
		Set<String> keys = classinfos.keySet();
		ClassInfo info =null;
		XSSFRow clz_met_firstrow=class_method_sheet.createRow(0);
		XSSFRow met_par_firstrow =method_param_sheet.createRow(0);
		XSSFRow cons_par_firstrow = cons_param_sheet.createRow(0);
		Drawing drawing = class_method_sheet.createDrawingPatriarch();//to Create Cell Comment
		CreationHelper factory =workbook.getCreationHelper();
		int clz_met_col_index=0;
		int cons_total=0, mets_total=0;
		//Class Loop Start
		for(String key : keys){
			info =classinfos.get(key);
			//클래스
			Class clz= info.getClz();

			//클래스 -메소드 설정
			XSSFCell infocell=clz_met_firstrow.createCell(clz_met_col_index);
			infocell.setCellValue(key);

			ClientAnchor anchor= factory.createClientAnchor();
			anchor.setCol1(clz_met_col_index);
			anchor.setCol2(clz_met_col_index+3);
			anchor.setRow1(0);
			anchor.setRow2(2);

			Comment comment= drawing.createCellComment(anchor);
			RichTextString str = factory.createRichTextString(info.getClz().getName());
			comment.setString(str);
			infocell.setCellComment(comment);


			/*
			 * To do : 
			 * 1. 메소드+파라미터타입 네임생성 ok
			 * 2. 타입리스트 생성 ok
			 * 3. 데이터 유효성 설정.  보류
			 * 4. Reader에서 Testing 모듈로 전환 시작.
			 * 5. 로그 관리 :
			 * 6. 
			 * */
			//Method loop 
			Set<Method> mets = info.getMethods();
			Iterator<Method> mit =mets.iterator();
			if(mets.size() >0){
				XSSFCell clz_met_cell= null;
				XSSFCell met_par_cell=null;
				for(int i =1; i <= mets.size() && mit.hasNext(); i ++){
					Method met= mit.next();

					//클래스 -메소드 부분
					XSSFRow clz_met_row= class_method_sheet.getRow(i);

					if(clz_met_row == null) clz_met_row= class_method_sheet.createRow(i);
					Parameter[] params= met.getParameters();

					//Search method Params
					String methodStr =met.getReturnType().getSimpleName()+" "+met.getName() + "(";
					String methodNamedStr="MET"+met.getName();
					Parameter param= null;
					for(int param_index=0; param_index<params.length; param_index++){

						param= params[param_index];
						String paramType= param.getType().getSimpleName();
						String paramStr = paramType+ " " + param.getName();
						methodStr+=paramStr;
						if(param_index!=params.length-1)
							methodStr+=',';

						//METHOD-PARAM SET
						if(param.getType().isArray())
							methodNamedStr+=paramType.substring(0, paramType.indexOf('['))+"Array";
						else
							methodNamedStr+= paramType;

						XSSFRow met_par_row= method_param_sheet.getRow(param_index+1); //2번째 줄에서부터 생성할것.
						if(met_par_row == null) met_par_row= method_param_sheet.createRow(param_index+1);
						XSSFCell paramTypeCell = met_par_row.createCell(mets_total); //파라미터 타입 리스트 생성.
						paramTypeCell.setCellValue(paramType);

					}
					methodStr+=')';
					System.out.println(methodStr);

					//ClassMethod sheet
					clz_met_cell= clz_met_row.createCell(clz_met_col_index);
					clz_met_cell.setCellValue(methodStr);

					//MethodParam Sheet
					met_par_cell = met_par_firstrow.createCell(mets_total);
					met_par_cell.setCellValue(methodStr);

					
					
					//Set Simple method Name.
					ClientAnchor methodanchor= factory.createClientAnchor();
					methodanchor.setCol1(clz_met_col_index);
					methodanchor.setCol2(clz_met_col_index+3);
					methodanchor.setRow1(i);
					methodanchor.setRow2(i+1);

					Comment methodcomment= drawing.createCellComment(methodanchor);
					RichTextString method_commentStr = factory.createRichTextString(met.getName());
					methodcomment.setString(method_commentStr);
					clz_met_cell.setCellComment(methodcomment);

					if(params.length>0){
						//Method Parameter List Name.
						XSSFName namedCell= workbook.createName();
						namedCell.setNameName(methodNamedStr);
						char cell = (char) ('A'+ mets_total);
						String formula= "MethodParamhidden!$"+cell+"$2:$"+cell+"$" + (params.length+1);
						namedCell.setRefersToFormula(formula);
					}
					
					mets_total++;
				}//Method loop End



				//Set Class-Method Data ReferenceList
				XSSFName namedcell =workbook.createName();
				namedcell.setNameName(clz.getSimpleName()); //Nameing이 중요.
				char currentCol=(char) ('A'+clz_met_col_index);
				String formula= "ClassMethodhidden!$"+currentCol+"$2:$"+currentCol+"$" + (mets.size()+1);
				//				System.out.println("Create : " + formula);
				namedcell.setRefersToFormula(formula);				
			}

			//생성자 리스트 및 생성자 파라미터
			XSSFRow con_par_row=null;
			Constructor[] conset= clz.getDeclaredConstructors();
			Constructor con =null;
			for(int con_index=0; con_index< conset.length; con_index++){ 
				con= conset[con_index];

				//다른이름이지만, 같은 유효성을 가리키는 이름 생성. INDIRECT를 위해서 생성함..
				String consName=info.getClz().getSimpleName()+"(";
				String consParamNamed="CON"+info.getClz().getSimpleName();
				Parameter[] params= con.getParameters();

				for(int param_index =0; param_index< params.length; param_index++ ){
					//생성자 풀스트링 설정
					Parameter param = params[param_index];
					consName+=param.getType().getSimpleName()+" "+param.getName();
					if(param_index != params.length-1) consName+=',';

					//생성자+파라미터 타입으로  네이밍스트링 만듬
					consParamNamed+=param.getType().getSimpleName();


					//파라미터 타입 셀생성
					con_par_row=cons_param_sheet.getRow(param_index+1);
					if(con_par_row ==null) con_par_row=cons_param_sheet.createRow(param_index+1);
					XSSFCell paramcell=con_par_row.createCell(cons_total);
					paramcell.setCellValue(param.getType().getSimpleName());
					//					System.out.println(param.getType());
				}
				consName+=')';
				System.out.println(consName);

				//생성자 셀 생성. .
				XSSFCell consCell = cons_par_firstrow.createCell(cons_total);
				consCell.setCellValue(consName);

				if(params.length>0){
					//생성자 네임생성
					XSSFName namedcell= workbook.createName();
					namedcell.setNameName(consParamNamed);

					char cell=(char) ('A'+cons_total);
					String formula="ConstructorParamhidden!$"+cell+"$2:$"+cell+"$"+(params.length+1);
					namedcell.setRefersToFormula(formula);
				}
				cons_total++;
			}//Constructor Loop End

			clz_met_col_index++;
		}//Class loop End.



		//Set Class Data ReferenceList.
		XSSFName namedcell =workbook.createName();
		namedcell.setNameName("Class"); //Nameing이 중요.
		char cell=(char) ('A'+cons_total-1);
		String formula= "ConstructorParamhidden!$A$1:$"+cell+"$1";
		namedcell.setRefersToFormula(formula);
		System.out.println("total : " + cons_total);

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
			//			System.out.println(constraint.getFormula1());
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
