package jexcelunit.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	String filePath,rootpath;
	XSSFWorkbook workbook;
	File file;
	public ExcelResultSaver(String filePath){
		this.filePath=filePath;
		this.file = new File(filePath);
	}

	public XSSFWorkbook getWorkbook() throws IOException{
		//open Excel File.
		if(file.exists()){
			inputstream = new FileInputStream(file);
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
	
	private List<String> getTestLog(){
		List<String> list = new ArrayList<String>();
		
		return null;
	}
	
	public void writeTestLog(String sheetName,int testIndex) throws IOException{
		if(workbook==null) workbook = getWorkbook();
		XSSFSheet sheet=workbook.getSheet(sheetName);
		int resultIndex=findIndex(sheet.getRow(1), "Result");
		int successIndex=resultIndex+1;
		XSSFRow currentRow= sheet.getRow(testIndex);
		XSSFCell cell = currentRow.getCell(successIndex);
		
		
		CreationHelper helper= workbook.getCreationHelper();
		Drawing drawing = sheet.createDrawingPatriarch();
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
	}
	
	public void writeResults(String sheetName, int totalRow, String[] result, boolean[] success) throws IOException{
		if(workbook==null) workbook = getWorkbook();
		XSSFSheet sheet=workbook.getSheet(sheetName);
		int resultIndex=findIndex(sheet.getRow(1), "Result");
		int successIndex=resultIndex+1;
		XSSFRow currentRow= null;
		XSSFCell resultCell, successCell;
		
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
			successCell.setCellValue(success[i-caseIndex]?"SUCCESS":"FAIL");
			
			if(success[i-caseIndex])successCell.setCellStyle(successStyle);
			else successCell.setCellStyle(failStyle);
		}

	}
	public void write() throws IOException{
		if(file.exists()&& fileoutputstream !=null)
			fileoutputstream=new FileOutputStream(file);
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
