package jexcelunit.excel;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class CheckingUtil {

	public static boolean isNullOrEmpty(String value) {
		if(value == null) return true;
		else if(value.isEmpty()) return true;
		else return false;
	}
	public static XSSFRow createRowIfNotExist(XSSFSheet sheet, int rowIndex){
		XSSFRow row = sheet.getRow(rowIndex);
		if(row ==null) row= sheet.createRow(rowIndex);
		return row;
	}
	public static XSSFCell createCellIfNotExist(XSSFRow row, int cellIndex){
		XSSFCell cell = row.getCell(cellIndex);
		if(cell ==null) cell= row.createCell(cellIndex);
		return cell;
	}
}
