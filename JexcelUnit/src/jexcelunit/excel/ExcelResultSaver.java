package jexcelunit.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelResultSaver {
	FileInputStream inputstream;
	FileOutputStream fileoutputstream;
	String fileName,rootpath;
	File file;
	public ExcelResultSaver(String fileName, String rootpath){
		this.fileName=fileName;
		this.rootpath= rootpath;
		this.file = new File(rootpath +'/'+fileName+".xlsx");
	}
	
	public XSSFWorkbook getWorkbook(String fileName, String rootpath) throws IOException{
		//open Excel File.
		inputstream = new FileInputStream(file);
		return new XSSFWorkbook(inputstream);
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
		XSSFWorkbook workbook = getWorkbook(fileName, rootpath);
		XSSFSheet sheet=workbook.getSheetAt(suite);
		int resultIndex=findIndex(sheet.getRow(0), "Result");
		int successIndex=resultIndex+1;
		XSSFRow currentRow= null;
		XSSFCell resultCell, successCell;
		for(int i = 1; i<totalRow; i++){
			currentRow= sheet.getRow(i);
			
			resultCell= currentRow.getCell(resultIndex);
			if(resultCell ==null) currentRow.createCell(resultIndex);
			resultCell.setCellValue(result[i-1]);
			
			successCell = currentRow.getCell(successIndex);
			if(successCell==null) currentRow.createCell(successIndex);
			successCell.setCellValue(success[i-1]);
		}
		fileoutputstream=new FileOutputStream(file);
		workbook.write(fileoutputstream);
		workbook.close();
		close();
		
	}
	
	private void close() throws IOException{
		if(inputstream !=null) inputstream.close();
		if(fileoutputstream !=null) fileoutputstream.close();
	}
	
}
