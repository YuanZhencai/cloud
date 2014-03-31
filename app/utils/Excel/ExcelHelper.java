package utils.Excel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import play.mvc.Http.MultipartFormData.FilePart;

public class ExcelHelper {

	static Logger logger = Logger.getLogger(ExcelHelper.class.getName());

	private static String XLS = "xls";
	private static String XLSX = "xlsx";
	private static String SHEET_HEADER = "sheet1_header_";
	private static String COLUMN = "column";
	private static Workbook wb;
	private static Sheet sheet;
	private static List<String> header;

	/**
	 * Create Workbook
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public Workbook createWorkbook(String filename) throws Exception {
		String suffix = getSuffix(filename);
		if (XLS.equals(suffix)) {
			wb = new HSSFWorkbook();
		} else if (XLSX.equals(suffix)) {
			wb = new XSSFWorkbook();
		}
		return wb;
	}

	/**
	 * Create Sheet
	 * @param sheetname
	 * @return
	 * @throws Exception
	 */
	public Sheet createSheet(String sheetname) throws Exception {
		String safeName = WorkbookUtil.createSafeSheetName(sheetname);
		sheet = wb.createSheet(safeName);
		return sheet;
	}

	/**
	 * Create Header
	 * @throws Exception
	 */
	/*
	 * public void createHeader() throws Exception { Map<String, String> header
	 * = ConfigManager.getConfigValueMapByFilter(SHEET_HEADER); List<String>
	 * columnIndex = genColumnIndex(header.size()); Row row =
	 * sheet.createRow((short)0); for (int i=0; i<columnIndex.size(); i++) { for
	 * (String colindex:header.keySet()) { if
	 * (colindex.equals(columnIndex.get(i))) {
	 * row.createCell(i).setCellValue(header.get(colindex)); break; } } } }
	 */

	/**
	 * Get Workbook by file path
	 * @param filepath
	 * @return
	 * @throws Exception
	 */
	public static Workbook getWorkbook(FilePart filePart) throws Exception {
		String suffix = getSuffix(filePart.getFilename());
		if (XLS.equals(suffix)) {
			wb = new HSSFWorkbook(new FileInputStream(filePart.getFile()));
		} else if (XLSX.equals(suffix)) {
			wb = new XSSFWorkbook(new FileInputStream(filePart.getFile()));
		}
		return wb;
	}

	public static Workbook getWorkbook(String filename) throws Exception {
		String suffix = getSuffix(filename);
		if (XLS.equals(suffix)) {
			wb = new HSSFWorkbook(new FileInputStream(filename));
		} else if (XLSX.equals(suffix)) {
			wb = new XSSFWorkbook(new FileInputStream(filename));
		}
		return wb;
	}

	private static Sheet getSheet(String sheetname) throws Exception {
		sheet = wb.getSheet(sheetname);
		return sheet;
	}

	/**
	 * Get Resource From Excel
	 * @return
	 * @throws Exception
	 */
	public static Map<String, List<Map<String, Object>>> exportDataFromExcel(FilePart filePart, String sheetname) throws Exception {
		Map<String, List<Map<String, Object>>> dataInSheet = new HashMap<String, List<Map<String, Object>>>();
		Map<String, Object> rowdata = null;
		boolean isData = false;
		boolean isEmptyRow = true;
		Workbook wb = getWorkbook(filePart);
		int sheetSize = wb.getNumberOfSheets();
		List<Sheet> sheets = new ArrayList<Sheet>();
		if (sheetname != null || !"".equals(sheetname)) {
			for (int i = 0; i < sheetSize; i++) {
				sheets.add(wb.getSheetAt(i));
				logger.log(Level.INFO, "Sheet name is: {0}", wb.getSheetAt(i).getSheetName());
			}
			logger.log(Level.INFO, "Sheet size is: {0}", sheetSize);
		} else {
			sheets.add(wb.getSheet(sheetname));
		}

		for (Sheet sheet : sheets) {
			List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
			header = new ArrayList<String>();
			isData = false;
			int t = 1;
			for (Row row : sheet) {
				rowdata = new HashMap<String, Object>();
				int idx = 0;
				isEmptyRow = true;
				for (Cell cell : row) {
					CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());
					if (!isData && header.size() < cell.getColumnIndex()) {
						int size = header.size();
						for (int i = 0; i < cell.getColumnIndex() - size; i++) {
							header.add(String.valueOf("BLANK" + idx));
							idx = idx + 1;
						}
					}
					String coordinate = cellRef.formatAsString();
					if (coordinate.endsWith("1") && coordinate.length() <= 3 && !isData) {
						if (coordinate.length() == 3 && ((int) coordinate.toCharArray()[1] < 65 || (int) coordinate.toCharArray()[1] > 90)) {
							continue;
						}
						header.add(cell.getRichStringCellValue().getString());
						continue;
					}

					isData = true;
					if (idx++ < header.size()) {
						switch (cell.getCellType()) {
						case Cell.CELL_TYPE_STRING:
							rowdata.put(header.get(cell.getColumnIndex()), cell.getRichStringCellValue().getString());
							isEmptyRow = false;
							break;
						case Cell.CELL_TYPE_NUMERIC:
							if (DateUtil.isCellDateFormatted(cell)) {
								logger.log(Level.INFO, "Getting a date value... " + cell.getDateCellValue());
								rowdata.put(header.get(cell.getColumnIndex()), cell.getDateCellValue());
							} else {
								logger.log(Level.INFO, "Getting a numic value... " + cell.getDateCellValue());
								rowdata.put(header.get(cell.getColumnIndex()), cell.getNumericCellValue());
							}
							isEmptyRow = false;
							break;
						case Cell.CELL_TYPE_BOOLEAN:
							rowdata.put(header.get(cell.getColumnIndex()), cell.getBooleanCellValue());
							isEmptyRow = false;
							break;
						case Cell.CELL_TYPE_FORMULA:
							// 读取带公式的数值
							rowdata.put(header.get(cell.getColumnIndex()), cell.getNumericCellValue());
							isEmptyRow = false;
							break;
						case Cell.CELL_TYPE_BLANK:
							rowdata.put(header.get(cell.getColumnIndex()), "");
						default:
							rowdata.put(header.get(cell.getColumnIndex()), "");
						}
					}
				}
				if (isData && !rowdata.isEmpty() && !isEmptyRow)
					data.add(rowdata);
			}
			dataInSheet.put(sheet.getSheetName(), data);
		}
		return dataInSheet;
	}

	/**
	 * 复制文件
	 * 		输入输出流获得其管道,然后分批次的从f1的管道中像f2的管道中输入数据每次输入的数据最大为2MB
	 * @param oldPath
	 * @param newPath
	 * @throws Exception
	 */
	public static String copyPrototypeToTemp(String oldPath, String newPath, String filename) throws Exception {
		int length = 2097152;
		String[] file = filename.split("[.]");
		String uuid = UUID.randomUUID().toString();
		FileInputStream in = new FileInputStream(oldPath + file[0] + "." + file[1]);
		FileOutputStream out = new FileOutputStream(newPath + file[0] + uuid + "." + file[1]);
		FileChannel inC = in.getChannel();
		FileChannel outC = out.getChannel();
		int i = 0;
		while (true) {
			if (inC.position() == inC.size()) {
				inC.close();
				outC.close();
				return uuid;
			}
			if ((inC.size() - inC.position()) < 20971520)
				length = (int) (inC.size() - inC.position());
			else
				length = 20971520;
			inC.transferTo(inC.position(), length, outC);
			inC.position(inC.position() + length);
			i++;
		}
	}

	/**
	 * Input Data to Excel
	 * @param data
	 * @throws Exception
	 */
	public static void importDataToExcel(List<Map<String, String>> data, String filename, String sheetname) throws Exception {
		int rows = data.size();
		int cols = data.get(0).size();
		System.out.println("int cols = data.get(0).size()+:" + cols);
		List<String> columnIndex = genColumnIndex(cols);
		getWorkbook(filename);
		getSheet(sheetname);
		// createHeader();
		CellStyle cs = wb.createCellStyle();
		cs.setLocked(false);
		for (int i = 1; i <= rows; i++) {
			Row row = null;
			/*
			 * if (i == 1) { row = sheet.getRow((short) i); } else {
			 */
			row = sheet.createRow((short) i);
			for (int j = 0; j < columnIndex.size(); j++) {
				for (String colindex : data.get(i - 1).keySet()) {
					if (colindex.equals(columnIndex.get(j))) {
						Cell cell = row.createCell(j);
						System.out.println("data.get(i-1).get(colindex)   " + data.get(i - 1).get(colindex));
						cell.setCellValue(data.get(i - 1).get(colindex));
						cell.setCellType(Cell.CELL_TYPE_STRING);
						cell.setCellStyle(cs);
						break;
					}
				}
			}
			// }
		}
		FileOutputStream fileOut = new FileOutputStream(filename);
		wb.write(fileOut);
		fileOut.close();
	}

	/**
	 * 获得某个单元格的值
	 * @param data
	 * @param rowIndex
	 * 		从1开始
	 * @param columnName
	 * @return
	 */
	public Object getCellData(List<Map<String, Object>> data, int rowIndex, String columnName) {
		return data.get(rowIndex - 1).get(columnName);
	}

	/**
	 * Get All column names
	 * @return
	 */
	public List<String> getHeader() {
		return header;
	}

	/**
	 * Generate Column Index List, such as column1 column2 column3...
	 * @param size
	 * @return
	 * @throws Exception
	 */
	private static List<String> genColumnIndex(int size) throws Exception {
		List<String> columnIndex = new ArrayList<String>();
		for (int i = 1; i <= size; i++) {
			columnIndex.add(COLUMN + i);
		}
		return columnIndex;
	}

	/**
	 * Get suffix
	 * @param filename
	 * @return
	 */
	private static String getSuffix(String filename) {
		return filename.split("[.]")[filename.split("[.]").length - 1];
	}

}
