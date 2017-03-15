package jexcelunit.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Name;
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
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

import jexcelunit.utils.ClassInfo;


/*
 * Created : 2017.02.23
 * Vendor  : Taehoon Seo
 * Description : create excel file including classes, methods and constructor informations.
 * */
@SuppressWarnings("rawtypes")
public class ExcelCreator{
	private final int CONSTRUCTOR = 0;
	private final int METHOD = 1;
	public final String[] TESTDATASET = {"TestName" ,"TestClass","Constructor Param", "TestMethod", "Method Param", "Expected", "Result", "Success"};


	private int consCount=0;
	private int metsCount=0;
	private FileOutputStream fileoutputstream= null;
	private XSSFWorkbook workbook = null;
	private String fileName= null, containerName=null;
	private HashMap<String, ClassInfo> classinfos=null;
	private File existingExcel= null;



	public ExcelCreator(String fileName,String containerName ,HashMap<String, ClassInfo> classinfos){
		this.fileName=fileName;
		this.containerName= containerName;
		this.classinfos= classinfos;

		consCount=getMaxParamCount(CONSTRUCTOR,classinfos);
		metsCount=getMaxParamCount(METHOD,classinfos);
	}

	public void initWorkSheets(XSSFWorkbook workbook){
		//Init All names. 
		List<XSSFName> names= workbook.getAllNames();
		String[] rmvName= new String[names.size()];
		int rm_Nameindex= 0;
		for(int i =0; i<names.size(); i++){
			rmvName[rm_Nameindex++]=  names.get(i).getNameName();
		}
		for(int j=0; j< rm_Nameindex; j++){
			if(rmvName[j] !=null){
				Name name=workbook.getName(rmvName[j]);
				workbook.removeName(name);
			}else break;
		}

		String[] rmvSheet= new String[workbook.getNumberOfSheets()];
		int rm_Sheetindex=0;

		for(int sheet_index=0;sheet_index<workbook.getNumberOfSheets();sheet_index++){
			if(workbook.isSheetHidden(sheet_index)){//Init hidden Sheets
				rmvSheet[rm_Sheetindex++]= workbook.getSheetName(sheet_index); //Save Delete Sheet's name
			}else {
				XSSFSheet sheet = workbook.getSheetAt(sheet_index);
				XSSFRow firstRow = sheet.getRow(0);

				//init DataValidations.
				unsetValidation(sheet);

				if(firstRow != null){
					//counting Constructor param Number and Method param Number.
					int old_conNum=0;
					int old_metNum=0;
					for(int cell_index =0; cell_index< firstRow.getPhysicalNumberOfCells(); cell_index++){
						XSSFCell cell=null;
						cell=firstRow.getCell(cell_index);
						if(cell.getStringCellValue().contains("Constructor Param")){
							old_conNum++;
						}
						if(cell.getStringCellValue().contains("Method Param")){
							old_metNum++;
						}
					}

					//칼럼수 조절.
					int con_change= consCount-old_conNum;
					int met_change = metsCount- old_metNum;
					if(con_change>0){//차이만큼 칼럼 추가.
						for(int i=0;i< con_change; i++){
							shiftColumn(sheet, 1+old_conNum, con_change);
						}
					}else if(con_change<0){//차이만큼 삭제
						for(int i=0;i> con_change; i--){
							removeColumn(sheet, 1+old_conNum, -con_change);
						}
					}

					if(met_change>0){//차이만큼 칼럼 추가.
						for(int i=0;i< met_change; i++){
							shiftColumn(sheet, 2+consCount, met_change);
						}
					}else if(met_change<0){//차이만큼 삭제
						for(int i=0;i> met_change; i--){
							removeColumn(sheet, 2+consCount, -met_change);
						}
					}
				}
				sheet.removeRow(sheet.getRow(0));//remove 첫번째 줄은 삭제할것.
			}
		}

		for(int i=0; i<rm_Sheetindex; i++){
			if(rmvSheet[i] !=null){
				int where = workbook.getSheetIndex(rmvSheet[i]);
				workbook.removeSheetAt(where);
			}else break;
		}

	}

	private void shiftColumn(XSSFSheet sheet, int from, int offset){
		XSSFRow currentRow =null;
		CellCopyPolicy ccp = new CellCopyPolicy();

		if(sheet !=null){
			for(int i=0; i<sheet.getPhysicalNumberOfRows(); i++){
				currentRow =sheet.getRow(i);
				XSSFCell currentCell =null;
				for(int j=currentRow.getPhysicalNumberOfCells()-1; j>=from;j--){
					currentCell = currentRow.getCell(j);
					if(currentCell !=null)
						currentRow.createCell(j+offset).copyCellFrom(currentCell, ccp);
				}
				for(int k=from; k< from+offset ; k++){
					currentCell = currentRow.getCell(k);
					if(currentCell !=null){
						if(currentCell.getCellComment() !=null)
							currentCell.removeCellComment();
						currentRow.removeCell(currentCell);
					}
				}
			}
		}
	}

	//셀인덱스를 기준으로.
	private void removeColumn(XSSFSheet sheet, int from, int offset){
		XSSFRow currentRow =null;
		CellCopyPolicy ccp = new CellCopyPolicy();
		if(sheet !=null){
			int colNum =sheet.getRow(0).getPhysicalNumberOfCells();
			for(int i=0; i<sheet.getPhysicalNumberOfRows(); i++){ 
				currentRow =sheet.getRow(i);
				XSSFCell currentCell =null;

				for(int j=from+offset-1; j<colNum;j++){
					currentCell = currentRow.getCell(j);
					if(currentCell !=null)
						currentRow.getCell(j-offset).copyCellFrom(currentCell, ccp);
				}
				for(int k=colNum-offset; k< colNum ; k++){
					currentCell = currentRow.getCell(k);
					if(currentCell !=null){
						if(currentCell.getCellComment() !=null)
							currentCell.removeCellComment();
						currentRow.removeCell(currentCell);
					}
				}

			}
		}
	}


	private File getExistingExcel(){
		boolean will_create=true;
		File root = new File(containerName);
		File xlsx= null;
		File[] filelist=  root.listFiles();
		for(File f: filelist){
			//			System.out.println(f.getName());
			if(f.getName().equals(fileName+".xlsx")){
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				will_create= MessageDialog.openQuestion(
						window.getShell(),
						"Do you want to Overwrite it? Yes :Overwrite No : Delete and Create it",
						"WARNING : If you overwrite it, you can lose its data");
				if(will_create)xlsx=f;
				else f.delete();
				break;
			}
		}
		return xlsx;
	}


	//create xlsx for Testcases.
	/*
	 * 테스트 이름, 클래스, 생성자파라미터, 메소드, 메소드파라미터, 리턴, 결과
	 * 1. 다중 파라미터를 어떻게 ? 파라미터 개수륿 분석해서 칼럼 수 조정할것 DONE
	 * 2. 모크객체의 이용방법은 어떻게? 모크객체 이름으로 접근하도록을 기본. 모크객체 생성도 엑셀로 지원 가능? DONE
	 * 3. 테스트방식 : 시나리오 or 독립. => 시트를 슈트단위로 하여 DONE.
	 * 4. 생성파일 네이밍. => 9.
	 * 5. 중복파일 처리. => DONE
	 * 	5-1. UPDATE 방식으로 유효성,네임 초기화 및 재설정. 칼럼 조정.
	 * 6. SUCCESS 처리=> TODO 엑셀에 셋팅.  컬러 조정.
	 * 7. 로그처리=>TODO Stack ListView로 제공. 로그파일 생성.
	 * 8. src 폴더의 경로 => 9.
	 * 
	 * 9. Extension Point  수정 : 버튼 + new. => 새 파일 Wizard 마법사로 슈트 클래스와 xlsx 파일 모두를 생성하게 만들자.
	 *  9-1. src, targetproject 모두 위저드에서 선택받도록. => 4, 8 동시해결  DONE
	 * */
	public void createXlsx() throws IOException{
		//Check ".xlxs" file is exist.
		existingExcel=getExistingExcel();

		//Create or Update
		XSSFSheet xssfSheet = null;
		XSSFRow row =null;
		XSSFCell cell =null;
		FileInputStream inputstream=null;
		try{
			if(existingExcel!=null){
				inputstream = new FileInputStream(existingExcel.getCanonicalPath());
				workbook= new XSSFWorkbook(inputstream);
				initWorkSheets(workbook);
				//Update.
			}else{
				workbook=new XSSFWorkbook();
				xssfSheet= workbook.createSheet("TestSuite 1");	
			}
			//make hidden sheet
			hiddensheet(workbook,classinfos);

			for(int sheet_index=0; sheet_index<workbook.getNumberOfSheets(); sheet_index++){
				if( !workbook.isSheetHidden(sheet_index) && !workbook.isSheetVeryHidden(sheet_index)){
					xssfSheet=workbook.getSheetAt(sheet_index);
					row=xssfSheet.createRow(0);//info

					int cellvalindex=0;
					int totalCellCount= TESTDATASET.length + consCount + metsCount-2;
					for(int i =0; i<totalCellCount; i++){
						String val=TESTDATASET[cellvalindex]; //Column List.
						if(val.equals("Constructor Param")){					
							//Set Param Validation Type
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
				}
			}
			//save xlsx
			fileoutputstream=new FileOutputStream(containerName+"/"+ fileName+".xlsx");
			workbook.write(fileoutputstream);

			System.out.println("Created");
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			//필수로 닫아주어야함
			if(workbook!=null) workbook.close();
			if( fileoutputstream!=null)
				fileoutputstream.close();
			if(workbook!=null) workbook.close();
			if( fileoutputstream!=null)
				fileoutputstream.close();
			if(inputstream !=null) inputstream.close();
		}
	}


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
//		Drawing drawing = class_method_sheet.createDrawingPatriarch();//to Create Cell Comment
//		CreationHelper factory =workbook.getCreationHelper();
		int clz_met_col_index=0;
		int cons_total=0, mets_total=0;
		//Class Loop Start
		for(String key : keys){
			info =classinfos.get(key);
			//클래스
			Class clz= info.getClz();

			//클래스 -메소드 설정
			XSSFCell infocell=clz_met_firstrow.createCell(clz_met_col_index);
			infocell.setCellValue(info.getClz().getName());

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
		if(!workbook.isSheetHidden(1)) workbook.setSheetHidden(1, true);
		if(!workbook.isSheetHidden(2)) workbook.setSheetHidden(2, true);
		if(!workbook.isSheetHidden(3)) workbook.setSheetHidden(3, true);
	}

	private void unsetValidation(XSSFSheet xssfSheet){
		CTWorksheet ctsheet = xssfSheet.getCTWorksheet();
		ctsheet.unsetDataValidations();
		ctsheet.setDataValidations(null);
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
