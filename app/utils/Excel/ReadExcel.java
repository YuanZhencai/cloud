package utils.Excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import action.Cell;

public class ReadExcel {
	public static List<? extends ExcelObj> readExcel(ExcelObj obj, File file, String fileName) {
		LinkedHashMap<String, Field> Cells = new LinkedHashMap<String, Field>();

		HashMap<String, Object> code = new HashMap<String, Object>();
		ExcelData excel = new ExcelData();
		// 存入文件列名,必填项

		Field[] declaredFields = obj.getClass().getDeclaredFields();
		for (Field field : declaredFields) {
			if (field.isAnnotationPresent(Cell.class)) {
				Cell annotation = field.getAnnotation(Cell.class);
				if (!annotation.Type().equals("deleted")) {
					Cells.put(field.getName(), field);
				}
			} else {
				Cells.put(field.getName(), field);
			}
		}
		excel.setCells(Cells);
		// 文件列名对应类型,可选,不写可能会出现异常
		// 使用code类型时,必须设置codetype
		// 使用getExcelObj方法必须设置
		try {
			excel.setDTO(obj.getClass().newInstance());
		} catch (InstantiationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IllegalAccessException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		/*
		 * //使用第一种方法获取并输出 List<List<HashMap<String, Object>>> exceldata = null;
		 * try { exceldata = excel.getExcel(file,0,1,fileName); } catch
		 * (Exception e1) { // TODO Auto-generated catch block
		 * e1.printStackTrace(); } if(exceldata!=null){ for(List<HashMap<String,
		 * Object>> lista:exceldata){ for(int iii=0;iii<lista.size();iii++){
		 * HashMap<String, Object> hm = lista.get(iii);
		 * System.out.print(hm.get(name[iii])+"  "); } System.out.println(); } }
		 */
		// 使用第二种方法直接获取List<Object>
		List<Object> list = null;
		try {
			list = excel.getExcelObj(file, 0, 1, fileName);
			System.out.println("++++++++++++++++++++++++++++++++++list : " + list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<ExcelObj> objs = new ArrayList<ExcelObj>();
		System.out.println("++++++++++++++++++++++++++++++++++list : " + list);
		if (list == null)
			return null;
		for (Object o : list) {
			if (o == null || o.equals(""))
				break;
			objs.add((ExcelObj) o);
			// System.out.println(play.libs.Json.toJson(t));
		}
		return objs;
	}

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
				return file[0] + uuid + "." + file[1];
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

	private static String getSuffix(String filename) {
		return filename.split("[.]")[filename.split("[.]").length - 1];
	}

	private static String XLS = "xls";
	private static String XLSX = "xlsx";

	public static File outExcel(List<? extends PrintObj> printObjs, String fileName) {
		// fileName="MaterialMasterSetup_Final.xlsx";
		/*
		 * String url=""; try { url =
		 * copyPrototypeToTemp("public/files/","public/files/",fileName); }
		 * catch (Exception e1) { // TODO Auto-generated catch block
		 * e1.printStackTrace(); }
		 */
		File file = null;
		try {
			String suffix = getSuffix(fileName);
			if (XLS.equals(suffix)) {
				file = File.createTempFile("temp", ".xls");
			} else if (XLSX.equals(suffix)) {
				file = File.createTempFile("temp", ".xlsx");
			}
			// file = File.createTempFile("temp", ".xlsx");
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		ExcelData excel = new ExcelData();
		if (printObjs.size() < 1) {
			try {
				Workbook createWorkbook = excel.createWorkbook(fileName);
				Sheet createSheet = null;
				if (printObjs.size() > 0)
					createSheet = createWorkbook.createSheet(printObjs.get(0).getSheetName());
				else
					createSheet = createWorkbook.createSheet("sheet");
				createSheet.createRow(0);
				FileOutputStream fileOut = new FileOutputStream(file);
				createWorkbook.write(fileOut);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		for (PrintObj printObj : printObjs) {
			System.out.println("type" + printObj.getType());
			LinkedHashMap<String, Field> Cells = new LinkedHashMap<String, Field>();
			if (printObj.getDates().size() > 0) {
				Field[] declaredFields = printObj.getDates().get(0).getClass().getDeclaredFields();
				for (Field field : declaredFields) {
					if (field.isAnnotationPresent(Cell.class)) {
						Cell annotation = field.getAnnotation(Cell.class);
						if (!annotation.Type().equals("deleted")) {
							Cells.put(field.getName(), field);
						}
					} else {
						Cells.put(field.getName(), field);
					}
				}
				excel.setCells(Cells);
			} else {
				excel.setCells(null);
			}
			// printObj=ReportType.getType(printObj, Cells.size());
			try {
				// System.out.println(file.getName());
				// System.out.println("start inportExcel");
				excel.importDataToExcel(printObj, file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println(file.getAbsolutePath());
		return file;
	}
}
