package jexcelunit.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFName;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheetConditionalFormatting;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

import jexcelunit.classmodule.ClassInfo;
import jexcelunit.classmodule.PrimitiveChecker;


/*
 * Created : 2017.02.23
 * Vendor  : Taehoon Seo
 * Description : create excel file including classes, methods and constructor informations.
 * 
 * '17 /04/ 21 TODO
 *  1. 모크객체 = >결국 코딩해야 하는데 뭐가 편할까   모크생성 시트를 만들까?!
 *   모크 객체에 필요한 것들 => 모크 이름. 모크 클래스. 생성자 파라미터, 호출 후 수행할 함수 , 파라미터들이 필요..
 *  2. 형상관리처럼 시각화 -> 분석한 모듈과 파라미터 타입에 따라서 재귀적으로 접근하도록 해야함.
 *  UML 테스트 는 어떨라나 ??!!! Star UML처럼 분석하여 시각화 하고, 파라미터 및 테스트를 드래그앤 드롭식으로 테스트 가능하게?! + 엑셀같은 데이터 작성 툴 제공?!!!
 *  3. 시나리오 테스트/ 독립테스트 설정 필드.첫줄에 여러 테스트 모드를 지원할 것. - DONE
 * */
@SuppressWarnings("rawtypes")
public class ExcelCreator{
	public final Attribute[] TESTDATASET = Attribute.values();

	private int fieldsCount=0;
	private int consCount=0;
	private int metsCount=0;
	private FileOutputStream fileoutputstream= null;
	private XSSFWorkbook workbook = null;
	private String fileName= null, containerPath=null;
	private HashMap<String, ClassInfo> classinfos=null;
	private File existingExcel= null;


	public ExcelCreator(String fileName,String containerPath ,HashMap<String, ClassInfo> classinfos){
		this.fileName=fileName;
		this.containerPath= containerPath;
		this.classinfos= classinfos;

		consCount=getMaxParamCount(Attribute.TestClass,classinfos);
		metsCount=getMaxParamCount(Attribute.TestMethod,classinfos);
		fieldsCount=getMaxFieldCount(classinfos);
	}


	private int getMaxFieldCount(HashMap<String, ClassInfo> classinfos) {
		// TODO Auto-generated method stub
		int max=0;
		for(ClassInfo classinfo : classinfos.values()){
			if(max<classinfo.getFields().length)
				max=classinfo.getFields().length;
		}
		return max;
	}


	/*
	 * 1. 모든 NamedName을 삭제.
	 * 2. HiddenSheet 삭제.
	 * 3. Mock Sheet 조정 
	 * 4. Test Sheet Column 조정
	 * */
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

		for(int sheetIndex=0;sheetIndex<workbook.getNumberOfSheets();sheetIndex++){
			if(!isTestSheet(workbook, sheetIndex)){//Init hidden Sheets
				if(!workbook.getSheetName(sheetIndex).contains("Mock"))
					rmvSheet[rm_Sheetindex++]= workbook.getSheetName(sheetIndex); //Save Delete Sheet's name
			}else {

				XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
				XSSFRow firstRow = sheet.getRow(1);

				//init DataValidations.
				unsetAllValidation(sheet);

				if(firstRow != null){
					//counting Constructor param Number and Method param Number.
					int old_conNum=0;
					int old_metNum=0;
					for(int cell_index =0; cell_index< firstRow.getPhysicalNumberOfCells(); cell_index++){
						XSSFCell cell=null;
						cell=firstRow.getCell(cell_index);
						if(cell.getStringCellValue().contains("ConsParam")){
							old_conNum++;
						}
						if(cell.getStringCellValue().contains("MetParam")){
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
				sheet.removeRow(sheet.getRow(1));//remove 첫번째 줄은 삭제할것.
				sheet.removeRow(sheet.getRow(0));
			}
		}

		//remove Hidden Sheet.
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

	/*
	 * 
	 * 
	 * */
	private File getExistingExcel(){
		boolean will_create=true;
		File root = new File(containerPath);
		File xlsx= null;
		File[] filelist=  root.listFiles();
		if(filelist !=null)
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



	private XSSFCellStyle getBorderStyle (XSSFCellStyle cs, short color){
		cs.setFillForegroundColor(color);
		cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		cs.setAlignment(HorizontalAlignment.CENTER);
		cs.setBorderTop(BorderStyle.MEDIUM);
		cs.setBorderBottom(BorderStyle.MEDIUM);
		cs.setBorderLeft(BorderStyle.MEDIUM);
		cs.setBorderRight(BorderStyle.MEDIUM);
		return cs;
	}
	/*
	 * 1. Scenario mode. 
	 * 2. Unit Tests
	 * */
	private void makeSetTestModeRow(XSSFSheet sheet, int rowIndex){
		XSSFRow row= CheckingUtil.createRowIfNotExist(sheet, rowIndex);

		XSSFCellStyle cs=getBorderStyle(workbook.createCellStyle(), IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());

		//Field Name;
		XSSFCell cell = CheckingUtil.createCellIfNotExist(row, 0);
		cell.setCellValue("Test MODE");
		cell.setCellStyle(cs);

		//validation Name
		cell= CheckingUtil.createCellIfNotExist(row, 1);;
		DataValidationHelper dvh= new XSSFDataValidationHelper(sheet);
		CellRangeAddressList addr= new CellRangeAddressList(row.getRowNum(),row.getRowNum(), 1, 1);
		DataValidationConstraint dvConstraint=dvh.createExplicitListConstraint(new String[]{"Scenario", "Units"});
		DataValidation dv= dvh.createValidation(dvConstraint, addr);
		sheet.addValidationData(dv);
		cell.setCellStyle(cs);
	}

	//create xlsx for Testcases.
	/*
	 * 테스트 이름, 클래스, 생성자파라미터, 메소드, 메소드파라미터, 리턴, 결과
	 * 1. 다중 파라미터를 어떻게 ? 파라미터 개수륿 분석해서 칼럼 수 조정할것 DONE
	 * 2. 모크객체의 이용방법은 어떻게? 모크객체 이름으로 접근하도록을 기본. 모크객체 생성도 엑셀로 지원 가능? DONE
	 * 3. 테스트방식 : 시나리오 or 독립. => 시트를 슈트단위로 하여 DONE.
	 * 4. 생성파일 네이밍. => 9. DONE
	 * 5. 중복파일 처리. => DONE
	 * 	5-1. UPDATE 방식으로 유효성,네임 초기화 및 재설정 DONE. 칼럼 조정 DONE.
	 * 6. SUCCESS 처리=> DONE.  컬러 조정 DONE.
	 * 7. 로그처리=>TODO Stack ListView로 제공. 로그파일 생성.
	 * 8. src 폴더의 경로 => 9. DONE
	 * 9. Extension Point  수정 : 버튼 + new. => 새 파일 Wizard 마법사로 슈트 클래스와 xlsx 파일 모두를 생성하게 만들자. DONE
	 *  9-1. src, targetproject 모두 위저드에서 선택받도록. => 4, 8 동시해결  DONE
	 * */
	public boolean createXlsx() throws IOException{
		//Check ".xlxs" file is exist.
		existingExcel=getExistingExcel();

		//Create or Update
		XSSFSheet xssfSheet = null, mockSheet=null;
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
				mockSheet= workbook.createSheet("Mock Sheet");
				//make Mock Sheet
				makeMockSheet(workbook, mockSheet);

				xssfSheet= workbook.createSheet("TestSuite 1");	
			}
			//make hidden sheet
			hiddensheet(workbook,classinfos);



			XSSFCellStyle cs=getBorderStyle(workbook.createCellStyle(), IndexedColors.LIGHT_YELLOW.getIndex());


			for(int sheetIndex=0; sheetIndex<workbook.getNumberOfSheets(); sheetIndex++){
				if( isTestSheet(workbook, sheetIndex)){
					//				if( workbook.getSheetAt(sheet_index).getSheetName().equals("TestSuite 1")){
					xssfSheet=workbook.getSheetAt(sheetIndex);
					makeSetTestModeRow(xssfSheet, 0); //Selection Test Mode Row
					row=CheckingUtil.createRowIfNotExist(xssfSheet, 1);

					int cellvalindex=0;
					int totalCellCount= Attribute.values().length + consCount + metsCount-2;


					for(int i =0; i<totalCellCount; i++){
						Attribute val=TESTDATASET[cellvalindex]; //Column List.
						if(val.equals(Attribute.ConsParam)){					
							//Set Param Validation Type
							for(int k=0; k<consCount; k++){
								cell=CheckingUtil.createCellIfNotExist(row, i+k);
								xssfSheet.setColumnWidth(i+k, 2700);
								cell.setCellValue(val.toString()+(k+1));
								cell.setCellStyle(cs);
							}
							i+=consCount-1;
							cellvalindex++;
						}
						else if(val.equals(Attribute.TestMethod)){
							xssfSheet.setColumnWidth(i, 3000);
							setValidation("INDIRECT(LEFT($B3,FIND(\"(\",$B3)-1))", xssfSheet,new CellRangeAddressList(2, 500, i, i));
							cell=CheckingUtil.createCellIfNotExist(row, i);
							cell.setCellValue(val.toString());
							cellvalindex++;
						}
						else if(val.equals(Attribute.MetParam)){

							for(int k=0; k<metsCount; k++){
								cell=CheckingUtil.createCellIfNotExist(row, i+k);
								xssfSheet.setColumnWidth(i+k, 2700);
								cell.setCellValue(val.toString()+(k+1));
								cell.setCellStyle(cs);
							}
							i+=metsCount-1;
							cellvalindex++;
						}
						else{
							xssfSheet.setColumnWidth(i, 3000);
							cell=CheckingUtil.createCellIfNotExist(row, i);
							cell.setCellValue(val.toString());
							cellvalindex++;
						}
						//set Cell Style.
						if(cell !=null){
							cell.setCellStyle(cs);
						}

					}
					setValidation("Class", xssfSheet, new CellRangeAddressList(2, 500, 1, 1));
					char colIndex=(char)('A'+totalCellCount-1);

					XSSFSheetConditionalFormatting cf=xssfSheet.getSheetConditionalFormatting();
					XSSFConditionalFormattingRule rule1 =cf.createConditionalFormattingRule("INDIRECT(ADDRESS(ROW(),"+totalCellCount+"))=\"SUCCESS\"");
					XSSFConditionalFormattingRule rule2 =cf.createConditionalFormattingRule("INDIRECT(ADDRESS(ROW(),"+totalCellCount+"))=\"FAIL\"");
					rule1.createPatternFormatting().setFillBackgroundColor(HSSFColor.LIGHT_GREEN.index);
					rule2.createPatternFormatting().setFillBackgroundColor(HSSFColor.ROSE.index);

					XSSFConditionalFormattingRule[] rules ={ rule1, rule2};
					CellRangeAddress[] range = { CellRangeAddress.valueOf("A3:"+colIndex+"500")};
					cf.addConditionalFormatting(range, rules);
				}
			}
			//save xlsx
			fileoutputstream=new FileOutputStream(containerPath+"/"+ fileName+".xlsx");
			workbook.write(fileoutputstream);

			System.out.println("Created");
			return true;
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
		return false;
	}

	//return if The Sheet in workbook at SheetIndex is Test Sheet or not.
	private boolean isTestSheet(XSSFWorkbook workbook, int sheetIndex){
		return  !workbook.isSheetHidden(sheetIndex) && !workbook.isSheetVeryHidden(sheetIndex) && !workbook.getSheetName(sheetIndex).contains("Mock")?true:false;
	}

	//Create Mock Sheet Page
	private void makeMockSheet(XSSFWorkbook workbook, XSSFSheet mockSheet){
		/*
		 * 모크 이슈
		 * 1. 모크가 사용자 정의 객체인경우
		 * 	1.1 모크 생성자
		 * 	1.2 필드
		 * 		1.2.1필드가 사용자 정의 객체 -> 다른 모크로 사용하도록
		 * 		1.3  클래스 - 필드 페이지 만들어야. -> Hidden Sheet.
		 * 	1.3 세터
		 * 	1.4 함수
		 * 2. 모크가 Primitive 타입 => 그럼 바로입력해야지.
		 * 3. 모크가 사용자정의 객체도아닌 JDBC 같은 객체인경우. **
		 * 4. 배열
		 * 5. 컬렉션
		 * */
		XSSFRow firstRow = CheckingUtil.createRowIfNotExist(mockSheet, 0);
		XSSFCellStyle cs=getBorderStyle(workbook.createCellStyle(), IndexedColors.LIGHT_YELLOW.getIndex());


		XSSFCell cell =CheckingUtil.createCellIfNotExist(firstRow, 0);
		cell.setCellValue("MockList");
		cell.setCellStyle(cs);

		//Make Help Comment.
		cell =null;
		cell= CheckingUtil.createCellIfNotExist(firstRow,2);
		cell.setCellValue("Help");
		cell.setCellStyle(cs);

		CreationHelper helper= workbook.getCreationHelper();
		Drawing drawing = mockSheet.createDrawingPatriarch();
		ClientAnchor anchor = helper.createClientAnchor();
		anchor.setCol1(cell.getColumnIndex());
		anchor.setCol2(cell.getColumnIndex()+8);
		anchor.setRow1(cell.getRowIndex());
		anchor.setRow2(cell.getRowIndex()+1);
		Comment comment= drawing.createCellComment(anchor);
		RichTextString str = helper.createRichTextString("Hello, World!");
		comment.setString(str);
		cell.setCellComment(comment);
		cell =null;

		//Make Index Row
		XSSFRow IndexRow = CheckingUtil.createRowIfNotExist(mockSheet, 1);
		XSSFCell indexCell = CheckingUtil.createCellIfNotExist(IndexRow, 0);
		indexCell.setCellStyle(cs);
		for(int i=1; i<= 10; i++){
			indexCell = CheckingUtil.createCellIfNotExist(IndexRow, i);
			indexCell.setCellValue(i);
			indexCell.setCellStyle(cs);
		}

		//Make Fields
		int rowIndex= 2, colIndex=0;
		XSSFRow categoryRow = CheckingUtil.createRowIfNotExist(mockSheet, rowIndex++);
		cell = CheckingUtil.createCellIfNotExist(categoryRow, colIndex);
		cell.setCellValue("MockName");
		mockSheet.setColumnWidth(0, 3500);
		cell.setCellStyle(cs);

		categoryRow = CheckingUtil.createRowIfNotExist(mockSheet, rowIndex++);
		cell = CheckingUtil.createCellIfNotExist(categoryRow, colIndex);
		cell.setCellValue("MockClass");	
		cell.setCellStyle(cs);

		//Set validation
		setValidation("Class", mockSheet, new CellRangeAddressList(rowIndex-1,rowIndex-1,1,500));

		int max=rowIndex + consCount;
		for(int i = 1; rowIndex < max;i++){
			categoryRow= CheckingUtil.createRowIfNotExist(mockSheet, rowIndex++);
			cell = CheckingUtil.createCellIfNotExist(categoryRow, colIndex);
			cell.setCellValue("ConsParam"+i);
			cell.setCellStyle(cs);
		}
		cell= null;

		//Field & Value
		max= rowIndex+(fieldsCount*2);
		for(int i =1; rowIndex< max; i++){
			categoryRow= CheckingUtil.createRowIfNotExist(mockSheet, rowIndex++);
			cell = CheckingUtil.createCellIfNotExist(categoryRow, colIndex);
			cell.setCellValue("Field"+i);
			//=INDIRECT(CONCATENATE("FID",LEFT($B4,FIND("(",$B4)-1)))
			String validationStr ="=INDIRECT(CONCATENATE(\"FID\",LEFT(INDIRECT(ADDRESS(4,COLUMN())),FIND(\"(\",INDIRECT(ADDRESS(4,COLUMN())))-1)))";
			setValidation(validationStr, mockSheet, new CellRangeAddressList(rowIndex-1,rowIndex-1,1,500));

			cell.setCellStyle(cs);
			categoryRow= CheckingUtil.createRowIfNotExist(mockSheet, rowIndex++);
			cell = CheckingUtil.createCellIfNotExist(categoryRow, colIndex);
			cell.setCellValue("Value"+i);
			cell.setCellStyle(cs);
		}

	}

	private String makeNameString(Class clz){
		//Check Class Name, and convert Array. Or Collection.
		String className=clz.getSimpleName();
		if(className == null|| className.length()==0)
		{
			className= clz.getName().substring(clz.getName().lastIndexOf("."), clz.getName().length());
		}
		if(className.contains("["))
			className=className.substring(0, className.indexOf('['))+"Array";
		else if(className.contains("<"))
			className=className.substring(0, className.indexOf('<'))+"T";

		return className;
	}

	// Create Hidden sheet for DataValidation List.
	private void hiddensheet(XSSFWorkbook workbook, HashMap<String,ClassInfo> classinfos){
		//생성자리스트 및 생성자 파라미터 유효성 -완료
		XSSFSheet cons_param_sheet = workbook.createSheet("ConstructorParamhidden");
		//메소드-파라미터 유효성
		XSSFSheet method_param_sheet = workbook.createSheet("MethodParamhidden");
		//클래스리스트 및 메소드 이름 유효성-완료
		XSSFSheet class_method_sheet = workbook.createSheet("ClassMethodhidden");

		//클래스-필드 유효성
		XSSFSheet class_field_sheet= workbook.createSheet("ClassFieldhidden");

		Set<String> keys = classinfos.keySet();
		ClassInfo classInfo =null;
		XSSFRow clz_met_firstrow=class_method_sheet.createRow(0);
		XSSFRow met_par_firstrow =method_param_sheet.createRow(0);
		XSSFRow cons_par_firstrow = cons_param_sheet.createRow(0);
		XSSFRow clz_fld_firstrow = class_field_sheet.createRow(0);

		//		Drawing drawing = class_method_sheet.createDrawingPatriarch();//to Create Cell Comment
		//		CreationHelper factory =workbook.getCreationHelper();
		int clz_met_col_index=0,clz_fld_col_index=0;
		int mets_total=0, cons_total=0;


		//Class Loop Start
		for(String key : keys){
			classInfo =classinfos.get(key);
			//클래스
			Class clz= classInfo.getClz();


			//클래스-필드 설정
			Field[] fields= classInfo.getFields();
			XSSFCell classNamecell=clz_fld_firstrow.createCell(clz_fld_col_index);
			classNamecell.setCellValue(classInfo.getClz().getName());
			if(fields.length>0){
				XSSFCell clz_fld_cell= null;
				int fieldRow =1;
				for(Field field : fields){

					//make Field's Cell
					XSSFRow clz_fld_row = CheckingUtil.createRowIfNotExist(class_field_sheet, fieldRow);
					clz_fld_cell = CheckingUtil.createCellIfNotExist(clz_fld_row, clz_fld_col_index);

					String fieldStr =field.getType().getSimpleName()+' '+field.getName();
					clz_fld_cell.setCellValue(fieldStr);
					fieldRow++;
				}
				//Make name
				String cell=cellIndex(clz_fld_col_index+1);
				String formula= "ClassFieldhidden!$"+cell+"$2:$"+cell+"$" + (fields.length+1);
				setNamedName(workbook, "FID"+makeNameString(clz), formula);


			}


			//클래스 -메소드 설정
			classNamecell=clz_met_firstrow.createCell(clz_met_col_index);
			classNamecell.setCellValue(clz.getName());
			//Method loop 
			Method[] mets = classInfo.getMethods();
			if(mets.length >0){
				XSSFCell clz_met_cell= null;
				XSSFCell met_par_cell=null;
				int methodRow =1;
				for(Method met : mets){
					if(!met.isSynthetic()){

						//클래스 -메소드 부분
						XSSFRow clz_met_row=  CheckingUtil.createRowIfNotExist(class_method_sheet, methodRow);

						Parameter[] params= met.getParameters();
						//Search method Params
						String methodStr =makeNameString(met.getReturnType())+" "+met.getName() + "(";

						String methodNamedStr="MET";
						methodNamedStr+=makeNameString(clz);
						methodNamedStr+=makeNameString(met.getReturnType());
						methodNamedStr+=met.getName();

						Parameter param= null;
						for(int param_index=0; param_index<params.length; param_index++){

							param= params[param_index];
							String paramType=makeNameString(param.getType());

							String paramStr = paramType+ " " + param.getName();
							methodStr+=paramStr;
							if(param_index!=params.length-1)
								methodStr+=',';

							//METHOD-PARAM SET
							methodNamedStr+= paramType;
							XSSFRow met_par_row= CheckingUtil.createRowIfNotExist(method_param_sheet,param_index+1); //2번째 줄에서부터 생성할것.
							XSSFCell paramTypeCell = met_par_row.createCell(mets_total); //파라미터 타입 리스트 생성.
							paramTypeCell.setCellValue(paramType);	

						}
						methodStr+=')';
						//						System.out.println(methodStr);

						//ClassMethod sheet
						clz_met_cell= clz_met_row.createCell(clz_met_col_index);
						clz_met_cell.setCellValue(methodStr);

						//MethodParam Sheet
						met_par_cell = met_par_firstrow.createCell(mets_total);
						met_par_cell.setCellValue(methodStr);

						if(params.length>0){
							//Method Parameter List Name.
							String cell=cellIndex(mets_total+1);
							String formula= "MethodParamhidden!$"+cell+"$2:$"+cell+"$" + (params.length+1);
							if(workbook.getName(methodNamedStr) !=null)
								setNamedName(workbook, methodNamedStr, formula);
						}

						methodRow++; 
						mets_total++;
					}
				}//Method loop End

				//Set Class-Method Data ReferenceList
				String currentCol=cellIndex(clz_met_col_index+1);
				String formula= "ClassMethodhidden!$"+currentCol+"$2:$"+currentCol+"$" + (mets.length+1);
				setNamedName(workbook, makeNameString(clz), formula);

			}


			//생성자 리스트 및 생성자 파라미터
			if(PrimitiveChecker.isUserClass(clz)){
				XSSFRow con_par_row=null;
				Constructor[] conset= clz.getDeclaredConstructors();
				Constructor con =null;
				for(int con_index=0; con_index< conset.length; con_index++){ 
					con= conset[con_index];

					//다른이름이지만, 같은 유효성을 가리키는 이름 생성. INDIRECT를 위해서 생성함..
					String consName=classInfo.getClz().getSimpleName()+"(";
					String consParamNamed="CON"+classInfo.getClz().getSimpleName();
					Parameter[] params= con.getParameters();

					for(int param_index =0; param_index< params.length; param_index++ ){
						//생성자 풀스트링 설정
						Parameter param = params[param_index];
						consName+=param.getType().getSimpleName()+" "+param.getName();
						if(param_index != params.length-1) consName+=',';

						//생성자+파라미터 타입으로  네이밍스트링 만듬
						consParamNamed+=makeNameString(param.getType());

						//파라미터 타입 셀생성
						con_par_row=CheckingUtil.createRowIfNotExist(cons_param_sheet, param_index+1);
						XSSFCell paramcell=CheckingUtil.createCellIfNotExist(con_par_row, cons_total);
						paramcell.setCellValue(param.getType().getSimpleName());
					}
					consName+=')';
					System.out.println(consName);

					//생성자 셀 생성. .
					XSSFCell consCell = CheckingUtil.createCellIfNotExist(cons_par_firstrow,cons_total);
					consCell.setCellValue(consName);

					if(params.length>0){
						//생성자 네임생성
						String cell=cellIndex(cons_total+1);
						String formula="ConstructorParamhidden!$"+cell+"$2:$"+cell+"$"+(params.length+1);
						setNamedName(workbook, consParamNamed, formula);
					}
					cons_total++;
				}//Constructor Loop End
			}

			clz_met_col_index++;
			clz_fld_col_index++;
		}//Class loop End.

		//Set Class Data ReferenceList.
		String cell=cons_total>0?cellIndex(cons_total):"A";
		String formula= "ConstructorParamhidden!$A$1:$"+cell+"$1";
		setNamedName(workbook, "Class", formula);
		System.out.println("total : " + cons_total);

		//Set hidden Sheet if true=  hidden.
		int consParamSheet=workbook.getSheetIndex("ConstructorParamhidden");
		int metParamSheet= workbook.getSheetIndex("MethodParamhidden");
		int clzMetSheet=workbook.getSheetIndex("ClassMethodhidden");
		int clzFieldSheet=workbook.getSheetIndex("ClassFieldhidden");


		if(!workbook.isSheetHidden(consParamSheet)) workbook.setSheetHidden(consParamSheet, true);
		if(!workbook.isSheetHidden(metParamSheet)) workbook.setSheetHidden(metParamSheet, true);
		if(!workbook.isSheetHidden(clzMetSheet)) workbook.setSheetHidden(clzMetSheet, true);
		if(!workbook.isSheetHidden(clzFieldSheet)) workbook.setSheetHidden(clzFieldSheet, true);
	}

	private void setNamedName(XSSFWorkbook workbook, String name, String formula){
		XSSFName namedcell =workbook.createName();
		namedcell.setNameName(name); //Naming이 중요.
		namedcell.setRefersToFormula(formula);
	}

	private void unsetAllValidation(XSSFSheet xssfSheet){
		CTWorksheet ctsheet = xssfSheet.getCTWorksheet();
		if(ctsheet.isSetDataValidations()){
			ctsheet.unsetDataValidations();
			ctsheet.setDataValidations(null);
		}
	}

	// 해당 열 데이터 유효성 
	private void setValidation(String namedCell, XSSFSheet xssfSheet,CellRangeAddressList addresslist){
		DataValidation dataValidation = null;
		DataValidationConstraint constraint = null;
		DataValidationHelper validationHelper = null;
		validationHelper = new XSSFDataValidationHelper(xssfSheet);

		constraint= validationHelper.createFormulaListConstraint(namedCell);
		if(constraint !=null){
			dataValidation= validationHelper.createValidation(constraint, addresslist);
			dataValidation.setSuppressDropDownArrow(true);
			dataValidation.setShowErrorBox(true);
			dataValidation.createErrorBox("Wrong Input", "You must input Right Type.");
			xssfSheet.addValidationData(dataValidation);
		}		
	}

	private static String cellIndex(int offset){
		char a= 'A'-1;
		int q=Math.floorDiv(offset, 26),r=offset%26;
		if(q==0) {
			if(r==0)return new Character((char) (a+r+1)).toString();
			return new Character((char) (a+r)).toString();
		}
		if(r==0)return new Character((char) (a+26)).toString();
		return cellIndex(q)+(char)(a+r);
	}
	private int getMaxParamCount(Attribute testclass,HashMap<String, ClassInfo> classinfos){
		int max= 0;
		Set<String> keys= classinfos.keySet();
		for(String key : keys){
			ClassInfo ci = classinfos.get(key);

			if(testclass == Attribute.TestClass){
				for(Constructor con : ci.getConstructors()){
					if( max < con.getParameterCount())
						max = con.getParameterCount();
				}	
			}
			else if(testclass == Attribute.TestMethod){
				for(Method met : ci.getMethods()){
					if( max < met.getParameterCount())
						max = met.getParameterCount();
				}
			}
		}
		return max;
	}
}
