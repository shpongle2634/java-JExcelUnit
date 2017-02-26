package jexcelunit.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
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

	/*Excel Reading Issue
	 * 
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
			DataFormatter formatter=new DataFormatter();//change all values to String
			//Read first line and set vo info.
			XSSFRow firstrow = xssfsheet.getRow(0);
			XSSFCell infocell = null;
			int colSize= firstrow.getPhysicalNumberOfCells();
			int[] voOption = new int[colSize];
			for(int i=0; i < colSize; i++){
				infocell= firstrow.getCell(i);

				for(int j =0; j < TESTDATASET.length; j++){
					if(infocell.getStringCellValue().contains(TESTDATASET[j])){						
						System.out.println(infocell.getStringCellValue() + " / " +TESTDATASET[j] + " : " +infocell.getStringCellValue().contains(TESTDATASET[j]));
						//remember index and after reading, Have to use this info	
						voOption[i]=j;
						break;
					}	
				}
			}
			for( int i=0; i<voOption.length; i++){
				System.out.println( i + " : " + voOption[i]);
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
							//							System.out.println((char)('A'+j)+Integer.toString(i) + " : " +currentCell.getStringCellValue());		

							setVOvalue(vo,j,formatter.formatCellValue(currentCell));
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
	private void setVOvalue(TestcaseVO vo , int OPTION, Object data){
		switch(OPTION){
		case TESTNAME:
			vo.setTestname((String) data);
			break;
		case TESTCLASS:
			vo.setTestclass((String) data);
			break;
		case CONSTPARAM:
			//
			vo.addConstructorParam((String)data);
			break;
		case METHOD:
			vo.setTestmethod((String) data);
			break;
		case METHODPARAM:
			//
			vo.addMethodParam((String)data);
			break;
		case EXPECTED:
			vo.setExpect((String) data);
			break;
		case RESULT:
			vo.setResult((String) data);
			break;
		case SUCCESS:

			break;
		}
	}
}
