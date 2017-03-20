package jexcelunit.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
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

	public void writeResults(int suite, int totalRow, String[] result, boolean[] success) throws IOException{
		workbook = getWorkbook();
		XSSFSheet sheet=workbook.getSheetAt(suite);
		int resultIndex=findIndex(sheet.getRow(0), "Result");
		int successIndex=resultIndex+1;
		XSSFRow currentRow= null;
		XSSFCell resultCell, successCell;
		
		XSSFCellStyle successStyle=workbook.createCellStyle();
		XSSFCellStyle failStyle=workbook.createCellStyle();
		successStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		failStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
		successStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		failStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		for(int i = 1; i<=totalRow; i++){
			currentRow= sheet.getRow(i);

			resultCell= currentRow.getCell(resultIndex);
			if(resultCell ==null) resultCell=currentRow.createCell(resultIndex);
			resultCell.setCellValue(result[i-1]);

			successCell = currentRow.getCell(successIndex);
			if(successCell==null) successCell=currentRow.createCell(successIndex);
			successCell.setCellValue(success[i-1]);
			
			if(success[i-1])successCell.setCellStyle(successStyle);
			else successCell.setCellStyle(failStyle);
		}

	}
	public void write() throws IOException{
		if(file.exists())
			fileoutputstream=new FileOutputStream(file);
		if(workbook!=null){
			workbook.write(fileoutputstream);
			workbook.close();
		}
	}

	public void close() throws IOException{
		if(inputstream !=null) inputstream.close();
		if(fileoutputstream !=null) fileoutputstream.close();
	}

}
