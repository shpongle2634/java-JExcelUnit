package jexcelunit.excel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelResultSaver {
	FileInputStream inputstream;
	FileOutputStream fileoutputstream;
	String jexcelPath, logPath;
	XSSFWorkbook workbook;
	File jexcelFile,logRoot;
	FileReader fileReader;
	BufferedReader bufferedReader;
	Map<String,List<String>> testLogMap;

	public ExcelResultSaver(String jexcelPath, String logPath){
		this.logPath= logPath;
		this.jexcelPath=jexcelPath;
		this.jexcelFile = new File(jexcelPath);
		this.logRoot= new File(logPath);
		this.testLogMap= new HashMap<String,List<String>>();
		try {
			this.workbook=getWorkbook();
			initTestLogs();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public XSSFWorkbook getWorkbook() throws IOException{
		//open Excel File.
		if(jexcelFile.exists()){
			inputstream = new FileInputStream(jexcelFile);
			return new XSSFWorkbook(inputstream);
		}
		return null;
	}

	private int findIndex(XSSFRow firstRow, String charSequence){
		Iterator<Cell> it= firstRow.cellIterator();
		DataFormatter formatter=new DataFormatter();
		XSSFCell currentCell;
		while(it.hasNext()){
			currentCell= (XSSFCell) it.next();
			String value=formatter.formatCellValue(currentCell);
			if(value.contains(charSequence)) return currentCell.getColumnIndex();
		}
		return -1;
	}

	private void initTestLogs() throws IOException{
		File testLog = new File(logRoot+"/test.log");

		if(testLog.exists()){
			fileReader = new FileReader(testLog);
			bufferedReader = new BufferedReader(fileReader);
		}
		getTestLogs();

		fileReader.close();
		bufferedReader.close();
	}

	private void getTestLogs() throws IOException{

		List<String> logs=null;
		StringBuffer stringBuffer = new StringBuffer();
		String line, sheetName=null;
		//Until EOF
		while ((line = bufferedReader.readLine()) != null) {
			String flagString=null;
			if(line.lastIndexOf(']')+2 <line.length()){
				flagString = line.substring(line.lastIndexOf(']')+2, line.length());
			}
			else{
				flagString = "";
			}

			//Start Sheet
			if(flagString.equals("Suite is Started")){
				logs=new ArrayList<>();
				sheetName=line.substring(line.lastIndexOf('[')+1, line.lastIndexOf(']'));
			}
			//End Sheet
			else if(flagString.equals("Suite is Finished")){
				if(sheetName !=null && logs!=null){
					testLogMap.put(sheetName,logs);
					sheetName=null;
				}
			}
			//Test Log
			else {//gather Test Logs
				if(line.length()>0){
					stringBuffer.append(line);
					stringBuffer.append("\n");	
				}
				//Test Case
				if((flagString.equals("Test is Finished"))){
					logs.add(stringBuffer.toString());
					stringBuffer=new StringBuffer();
				}			
			}
		}
	}

	public void writeTestLog(String sheetName,int rowSize) throws IOException{

		XSSFSheet sheet=workbook.getSheet(sheetName);
		int successIndex=findIndex(sheet.getRow(1), "Success");
		List<String> sheetLogs= this.testLogMap.get(sheetName);
		if(sheetLogs!=null){
			XSSFRow currentRow; XSSFCell cell;
			CreationHelper helper= workbook.getCreationHelper();
			Drawing drawing = sheet.createDrawingPatriarch();

			for(int row=2; row<rowSize+2; row++){
				currentRow= sheet.getRow(row);
				cell = currentRow.getCell(successIndex);
				if(cell.getCellComment()!=null)
					cell.removeCellComment();
				String log= sheetLogs.get(row-2);
								System.out.println(log);

				ClientAnchor anchor = helper.createClientAnchor();
				anchor.setCol1(cell.getColumnIndex()+1);
				anchor.setCol2(cell.getColumnIndex()+10);
				anchor.setRow1(cell.getRowIndex());
				anchor.setRow2(cell.getRowIndex()+8);
				Comment comment= drawing.createCellComment(anchor);
				RichTextString str = helper.createRichTextString(log);
				comment.setString(str);
				cell.setCellComment(comment);
				cell =null;
			}	
		}

	}

	public void writeResults(String sheetName, int totalRow, String[] result, boolean[] success) throws IOException{
		if(workbook==null) workbook = getWorkbook();
		XSSFSheet sheet=workbook.getSheet(sheetName);
		int resultIndex=findIndex(sheet.getRow(1), "Result");
		int successIndex=resultIndex+1;
		XSSFRow currentRow= null;
		XSSFCell resultCell, successCell;

		int successCount=0,failCount=0;

		XSSFCellStyle successStyle=workbook.createCellStyle();
		XSSFCellStyle failStyle=workbook.createCellStyle();
		successStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		failStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
		successStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		failStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		int caseIndex=2;
		for(int i = caseIndex; i<totalRow+caseIndex; i++){
			currentRow= sheet.getRow(i);

			resultCell= currentRow.getCell(resultIndex);
			if(resultCell ==null) resultCell=currentRow.createCell(resultIndex);
			resultCell.setCellValue(result[i-caseIndex]);

			successCell = currentRow.getCell(successIndex);
			if(successCell==null) successCell=currentRow.createCell(successIndex);
			if(success[i-caseIndex]){
				successCell.setCellValue("SUCCESS");
				successCell.setCellStyle(successStyle);
				successCount++;
			}else{
				successCell.setCellValue("FAIL");	
				successCell.setCellStyle(failStyle);
				failCount++;
			}
		}

		//Save Total Statistics

	}
	public void write() throws IOException{
		if(jexcelFile.exists()&& fileoutputstream ==null)
			fileoutputstream=new FileOutputStream(jexcelFile);
		if(workbook!=null){
			workbook.write(fileoutputstream);
		}
	}

	public void close() throws IOException{
		if(inputstream !=null) inputstream.close();
		if(fileoutputstream !=null) fileoutputstream.close();
		if(workbook!=null){
			workbook.close();
		}
	}

}
