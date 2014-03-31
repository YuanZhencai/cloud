package utils.Excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;

public class ExcelData {
	private static final Object[] String = null;
	/**列名*/
	// private LinkedHashMap<String,String> colnumName;
	private String[] Name;
	/**列类型*/
	// private Integer[] colnumType;
	/**代码KV*/
	private LinkedHashMap<String, Field> Cells;
	private static Workbook wbs;
	private static Sheet sheet;
	private HashMap<String, Object> codeType;
	/**写入到的Object类型,该方法必须继承ExcelObj才可以使用*/
	private ExcelObj DTO;

	public ExcelObj getDTO() {
		return DTO;
	}

	public void setDTO(ExcelObj dto) {
		DTO = dto;
	}

	public LinkedHashMap<String, Field> getCells() {
		return Cells;
	}

	public void setCells(LinkedHashMap<String, Field> cells) {
		Cells = cells;
		if (cells != null)
			this.Name = cells.keySet().toArray(new String[cells.size()]);
		else
			this.Name = new String[0];
	}

	/*
	 * public LinkedHashMap<String,String> getColnumName() { return colnumName;
	 * } public void setColnumName(LinkedHashMap<String,String> colnumName) {
	 * this.colnumName = colnumName; Set<String> keySet = colnumName.keySet();
	 * for (String string : keySet) { System.out.println(string); }
	 * this.Name=colnumName.keySet().toArray(new String[colnumName.size()]); for
	 * (int i = 0; i < this.Name.length; i++) {
	 * System.out.println(this.Name[i]); } } public Integer[] getColnumType() {
	 * return colnumType; }
	 * 
	 * public void setColnumType(Integer[] colnumType) { this.colnumType =
	 * colnumType; }
	 */
	public HashMap<String, Object> getCodeType() {
		return codeType;
	}

	public void setCodeType(HashMap<String, Object> codeType) {
		this.codeType = codeType;
	}

	private static String XLS = "xls";
	private static String XLSX = "xlsx";

	/**
	 * 获取excel到一个List<List<HashMap<String,Object>>>，从第一个sheet页的第一行开始读取
	 * 从外到内依次为：sheet页,行,单元格
	 * @param file 要读取的文件
	 * @return 返回一个List<List<HashMap<String,Object>>>对象
	 * @throws Exception
	 */
	public List<List<HashMap<String, Object>>> getExcel(File file, String fileName) throws Exception {
		return getExcel(file, 0, 0, fileName);
	}

	public static Workbook getWorkbook(File file, String fileName) throws Exception {
		Workbook wbs = null;
		// System.out.println("11312"+fileName);
		String suffix = getSuffix(fileName);
		if (XLS.equals(suffix)) {
			wbs = new HSSFWorkbook(new FileInputStream(file));
		} else if (XLSX.equals(suffix)) {
			wbs = new XSSFWorkbook(new FileInputStream(file));
		}
		return wbs;
	}

	public static Workbook getWorkbook(File file) throws Exception {
		Workbook wbs = null;
		// System.out.println("12312"+file.getName());
		String suffix = getSuffix(file.getName());
		try {
			if (XLS.equals(suffix)) {
				wbs = new HSSFWorkbook(new FileInputStream(file));
			} else if (XLSX.equals(suffix)) {
				wbs = new XSSFWorkbook(new FileInputStream(file));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wbs;
	}

	/**
	 * 获取excel到一个List<List<HashMap<String,Object>>>
	 * 从外到内依次为：sheet页,行,单元格
	 * @param file 要读取的文件
	 * @param snum 读取那个sheet页,从0算起
	 * @param rnum 从那行开始读取,从0算起
	 * @return 返回一个List<List<HashMap<String,Object>>>对象
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public List<List<HashMap<String, Object>>> getExcel(File file, int snum, int rnum, String fileName) throws Exception {
		List<List<HashMap<String, Object>>> list = new ArrayList<List<HashMap<String, Object>>>();
		Object localObject = null;
		// 传入路径
		FileInputStream is = new FileInputStream(file);
		Workbook wbs = getWorkbook(file, fileName);
		// System.out.println(wbs.getNumberOfSheets());
		Sheet childSheet = wbs.getSheetAt(snum);
		for (int j = rnum; j <= childSheet.getLastRowNum(); j++) {
			// 读取行元素
			List<HashMap<String, Object>> listrow = new ArrayList<HashMap<String, Object>>();
			Row row = childSheet.getRow(j);
			if (null != row) {
				HashMap<String, Object> cellv = null;
				for (int k = 0; k < row.getLastCellNum(); k++) {
					// 读取单元格
					Cell cell = row.getCell((short) k);
					cellv = new HashMap<String, Object>();
					if (cell == null) {
						cellv.put(Name[k], null);
						listrow.add(cellv);
						continue;
					} else {
						// 判断获取类型
						// System.out.println("CellType+++++++++++++++++++++++++++++++++++++++++++"+cell.getCellType());
						switch (cell.getCellType()) {
						case Cell.CELL_TYPE_NUMERIC:
							if (DateUtil.isCellDateFormatted(cell)) {
								localObject = cell.getDateCellValue().getTime();
								// System.out.println("date::::::::::::::::::::::"+localObject);
							} else
								localObject = cell.getNumericCellValue();
							break;
						case Cell.CELL_TYPE_STRING:
							localObject = cell.getStringCellValue();
							break;
						case Cell.CELL_TYPE_BOOLEAN:
							localObject = new Boolean(cell.getBooleanCellValue());
							break;
						case Cell.CELL_TYPE_BLANK:
							localObject = "";
							break;
						case Cell.CELL_TYPE_FORMULA:
							int a = (cell.getCellFormula().indexOf("+") + 1) + (cell.getCellFormula().indexOf('/') + 1)
									+ (cell.getCellFormula().indexOf('*') + 1) + (cell.getCellFormula().indexOf('-') + 1);
							if (a <= 0) {
								localObject = cell.getCellFormula();
							} else if (a > 0) {
								localObject = cell.getNumericCellValue();
							}
							break;
						case Cell.CELL_TYPE_ERROR:
							localObject = new Byte(cell.getErrorCellValue());
							break;
						default:
							System.out.println("未知类型");
							break;
						}
						try {
							// 限制类型的时候,做下面的类型强制转换
							if (Cells != null) {
								localObject = getRightTypeValue(localObject, k);
								// System.out.println(localObject);
							}
						} catch (Exception e) {
							cellv.put("ErrMsg", e.getMessage());
							listrow.add(cellv);
							continue;
						}
						cellv.put(Name[k], localObject);
					}
					listrow.add(cellv);
				}
				list.add(listrow);
			}
		}
		return list;
	}

	/**
	 * 获取excel到一个List<List<HashMap<String,Object>>>
	 * @param filepath Excel文件路径
	 * @return 返回一个List<List<HashMap<String,Object>>>对象
	 * @throws Exception
	 */
	public List<List<HashMap<String, Object>>> getExcel(String filepath) throws Exception {
		File file = new File(filepath);
		return getExcel(file, 0, 0, null);
	}

	/**
	 * 获取excel到一个List<List<HashMap<String,Object>>>
	 * @param filepath Excel文件路径
	 * @param snum 读取那个sheet页,从0算起
	 * @param rnum 从那行开始读取,从0算起
	 * @return 返回一个List<List<HashMap<String,Object>>>对象
	 * @throws Exception
	 */
	public List<List<HashMap<String, Object>>> getExcel(String filepath, int snum, int rnum) throws Exception {
		File file = new File(filepath);
		return getExcel(file, snum, rnum, null);
	}

	/**
	 * 读取到excel到List<Object>从第一个sheet页的第一行开始读取
	 * @param file 要读取的文件
	 * @return 返回一个List<Object>对象
	 * @throws Exception
	 */
	public List<Object> getExcelObj(File file, String fileName) throws Exception {
		return getExcelObj(file, 0, 0, fileName);
	}

	/**
	 * 读取到excel到List<Object>
	 * @param file 要读取的文件
	 * @param snum 读取那个sheet页,从0算起
	 * @param runm 从那行开始读取,从0算起
	 * @return 返回一个List<Object>对象
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getExcelObj(File file, int snum, int runm, String fileName) throws Exception {
		List<Object> list = new ArrayList<Object>();
		Class c = DTO.getClass();
		ExcelObj dto = null;
		List<List<HashMap<String, Object>>> exceldata = getExcel(file, snum, runm, fileName);
		if (exceldata != null) {
			// row
			for (List<HashMap<String, Object>> lista : exceldata) {
				dto = (ExcelObj) c.newInstance();
				dto.putValue("errMsg", "");
				// 列
				for (int iii = 0; iii < lista.size(); iii++) {
					HashMap<String, Object> hm = lista.get(iii);
					if (hm.get(Name[iii]) != null) {
						dto.putValue(Name[iii], hm.get(Name[iii]));
					} else {
						dto.putValue("errMsg", dto.outValue("errMsg") + "第" + (iii + 1) + "列存在为空的值;");
						continue;
					}
				}
				list.add(dto);
			}
		}
		return list;
	}

	/**
	 * 读取到excel到List<Object>
	 * @param filepath Excel文件路径
	 * @return 返回一个List<Object>对象
	 * @throws Exception
	 */
	public List<Object> getExcelObj(String filepath, String fileName) throws Exception {
		File file = new File(filepath);
		return getExcelObj(file, 0, 0, fileName);
	}

	/**
	 * 读取到excel到List<Object>
	 * @param filepath Excel文件路径
	 * @param snum
	 * @param runm
	 * @return 返回一个List<Object>对象
	 * @throws Exception
	 */
	public List<Object> getExcelObj(String filepath, int snum, int runm, String fileName) throws Exception {
		File file = new File(filepath);
		return getExcelObj(file, snum, runm, fileName);
	}

	/**
	 * 这里获取的值是输入正确,但是单元格属性设置错误导致类型错误,需要矫正的值
	 * @param localObject
	 * @param k
	 * @return 经过类型矫正的值
	 * @throws Exception
	 */
	private Object getRightTypeValue(Object localObject, int k) throws Exception {
		Field field = Cells.get(Name[k]);
		switch (ColT.getType(field.getGenericType().toString())) {
		case ColT.NO:// 不做任何转换
			break;
		case ColT.cCode:
			// 需要根据KV转换规则进行转换
			Object obj = codeType.get(localObject.toString());
			if (obj == null) {
				throw new Exception("找不到对应的代码值,请确认代码值列是否为文本类型!");
			} else {
				localObject = obj;
			}
			break;
		case ColT.cDATE:
			if (!(localObject instanceof Date)) {
				if (localObject instanceof Long) {
					localObject = (new DateTime((Long) localObject)).toDate();
					// System.out.println("11111"+localObject.toString());
				} else if (localObject instanceof String) {
					// 按照格式yyyy-MM-dd转换
					DateTime parse = DateTime.parse(localObject.toString());
					localObject = parse.toDate();
					/*
					 * SimpleDateFormat sdf = new
					 * SimpleDateFormat("yyyy/MM/dd"); localObject =
					 * sdf.parse(localObject.toString());
					 */
				}
				/*
				 * else if(localObject instanceof Double){
				 * System.out.println("Date:::::::::::::::::::::::"
				 * +localObject); int day = ((Double)localObject).intValue();
				 * Calendar c=Calendar.getInstance(); c.set(1900, 0, 1);
				 * c.add(Calendar.DAY_OF_YEAR, day); localObject = c.getTime();
				 * }
				 */
				else {
					throw new Exception("日期格式错误");
				}
			}
			break;
		case ColT.cDOUBLE:
			if (!(localObject instanceof Double)) {
				if (localObject instanceof String) {
					localObject = Double.parseDouble(localObject.toString());
				}
			}
			break;
		case ColT.cFlOAT:
			if (localObject instanceof Double) {
				localObject = ((Double) localObject).floatValue();
			}
			break;
		case ColT.cINT:
			if (localObject instanceof Double) {
				localObject = ((Double) localObject).intValue();
			}
			break;
		case ColT.cLONG:
			if (localObject instanceof Double) {
				localObject = ((Double) localObject).longValue();
			}
			break;
		case ColT.cSTRING:
			if (localObject instanceof Double) {
				// System.out.println(new BigDecimal((Double)localObject));
				localObject = (new BigDecimal((Double) localObject)).toString();
			} else {
				localObject = localObject.toString();
			}
			// System.out.println("abc:"+localObject);
			break;
		}
		return localObject;
	}

	private static String getSuffix(String filename) {
		return filename.split("[.]")[filename.split("[.]").length - 1];
	}

	public void importDataToExcel(PrintObj obj, File file) throws Exception {
		String[] columnIndex = Name;
		LinkedHashMap<String, Field> assemblys = new LinkedHashMap<String, Field>();
		Field[] declaredFields = obj.getClass().getDeclaredFields();
		for (Field field : declaredFields) {
			if (field.isAnnotationPresent(action.Cell.class)) {
				action.Cell annotation = field.getAnnotation(action.Cell.class);
				assemblys.put(annotation.name(), field);
			}
		}
		// System.out.println("importDataExcel");
		int rows = assemblys.get("head").getAnnotation(action.Cell.class).Size() + obj.getDates().size()
				+ assemblys.get("foot").getAnnotation(action.Cell.class).Size();
		int cols = columnIndex.length;
		// if(obj.getHead()!=null)
		// System.out.println("++++++++++++++++++++++++++++++++++++++++"+obj.getHead().size());
		HashMap<Integer, HashMap<Integer, java.lang.String[]>> head = getStringList(obj.getHead(),
				assemblys.get("head").getAnnotation(action.Cell.class).Size(), cols);
		HashMap<Integer, HashMap<Integer, java.lang.String[]>> foot = getStringList(obj.getFoot(),
				assemblys.get("foot").getAnnotation(action.Cell.class).Size(), cols);

		// System.out.println("int cols = data.get(0).size()+:" +
		// cols+":"+rows);
		wbs = getWorkbook(file);
		if (wbs == null)
			wbs = createWorkbook(file.getName());
		sheet = createSheet(wbs, obj.getSheetName());
		if (sheet == null)
			sheet = createSheet(wbs, obj.getSheetName());
		// createHeader();
		CellStyle cs = wbs.createCellStyle();
		// DataFormat dataFormat = wbs.createDataFormat();
		cs.setLocked(false);
		cs.setAlignment((short) 0);
		// System.out.println("head is null:"+head);
		cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("dd/MMM/yyyy"));
		for (int i = 0; i <= rows; i++) {
			Row row = null;
			if (head != null && i < assemblys.get("head").getAnnotation(action.Cell.class).Size()) {
				// System.out.println("head::::::::::::::::"+i);
				if (sheet.getRow((short) i) == null) {
					row = sheet.createRow(i);
				} else {
					row = sheet.getRow((short) i);
				}
				for (int j = 0; j < cols; j++) {
					// System.out.println(head.get(i));
					if (head.get(i) != null && head.get(i).get(j) != null) {
						// System.out.println(head.get(i).get(j));
						Cell cell = row.createCell(j);
						// System.out.println(head.get(i).get(j)[1]);
						cell.setCellValue(head.get(i).get(j)[1]);
						// System.out.println(i+":"+j+":"+head.get(i).get(j)[1]);
						cell.setCellType(Cell.CELL_TYPE_STRING);
						if (head.get(i).get(j)[0] != null && head.get(i).get(j)[0].equals("title")) {
							cell.setCellStyle(getNameStyle());
						} else {
							cell.setCellStyle(getFirstStyle());
						}
					}
				}
			} else if (foot != null && i >= (assemblys.get("head").getAnnotation(action.Cell.class).Size() + obj.getDates().size() + 1)) {
				// System.out.println("foot");
				if (sheet.getRow((short) i) == null) {
					row = sheet.createRow(i);
				} else {
					row = sheet.getRow((short) i);
				}
				for (int j = 0; j < cols; j++) {
					if (foot.get(i - 1 - assemblys.get("head").getAnnotation(action.Cell.class).Size() - obj.getDates().size()) != null
							&& foot.get(i - 1 - assemblys.get("head").getAnnotation(action.Cell.class).Size() - obj.getDates().size()).get(j) != null) {
						System.out.println(foot.get(i - 1 - assemblys.get("head").getAnnotation(action.Cell.class).Size() - obj.getDates().size())
								.get(j));
						Cell cell = row.createCell(j);
						if (foot.get(i - 1 - assemblys.get("head").getAnnotation(action.Cell.class).Size() - obj.getDates().size()).get(j)[0]
								.equals("line")) {
							cell.setCellValue(foot.get(i - 1 - assemblys.get("head").getAnnotation(action.Cell.class).Size() - obj.getDates().size())
									.get(j)[1]);
							cell.setCellType(Cell.CELL_TYPE_STRING);
							cell.setCellStyle(getLineStyle());
						} else {
							cell.setCellValue(foot.get(i - 1 - assemblys.get("head").getAnnotation(action.Cell.class).Size() - obj.getDates().size())
									.get(j)[1]);
							cell.setCellType(Cell.CELL_TYPE_STRING);
							cell.setCellStyle(getFootStyle());
						}
					}
				}
			} else if (i >= assemblys.get("head").getAnnotation(action.Cell.class).Size() && obj.getDates().size() > 0) {
				if (sheet.getRow((short) i) == null) {
					row = sheet.createRow(i);
				} else {
					row = sheet.getRow((short) i);
				}
				for (int j = 0; j < cols; j++) {
					Cell cell = row.createCell(j);
					// System.out.println(i+":"+j);
					if (i == assemblys.get("head").getAnnotation(action.Cell.class).Size()) {
						// System.out.println(columnIndex[j]);
						cell.setCellType(Cell.CELL_TYPE_STRING);
						if (Cells.get(columnIndex[j]).isAnnotationPresent(action.Cell.class)) {
							action.Cell annotation = Cells.get(columnIndex[j]).getAnnotation(action.Cell.class);
							cell.setCellValue(annotation.name());
						} else {
							cell.setCellValue(Cells.get(columnIndex[j]).getName());
						}
						cell.setCellStyle(getContentStyle());
					} else {
						if (i - assemblys.get("head").getAnnotation(action.Cell.class).Size() - obj.getDates().size() > 0)
							break;
						// System.out.println("data.get(i-1).get(colindex)" +
						// obj.getDates().get(i-assemblys.get("head").getAnnotation(action.Cell.class).Size()-
						// 1).outValue(columnIndex[j]));
						Object localObject = obj.getDates().get(i - assemblys.get("head").getAnnotation(action.Cell.class).Size() - 1)
								.outValue(columnIndex[j]);
						if (localObject != null && !localObject.equals("")) {
							Field field = Cells.get(columnIndex[j]);
							// System.out.println(localObject+"=========="+field.getGenericType().toString()+"+++++++++++++++"+ColT.getType(field.getGenericType().toString()));
							switch (ColT.getType(field.getGenericType().toString())) {
							case ColT.NO:// 不做任何转换
								break;
							case ColT.cCode:
								// 需要根据KV转换规则进行转换
								cell.setCellValue((String) codeType.get(localObject.toString()));
								break;
							case ColT.cDATE:
								if (!(localObject instanceof Date)) {
									if (localObject instanceof String) {
										// 按照格式yyyy-MM-dd转换
										// System.out.println(localObject);
										SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
										cell.setCellValue(sdf.parse(localObject.toString()));
									} else if (localObject instanceof Double) {
										int day = ((Double) localObject).intValue();
										Calendar c = Calendar.getInstance();
										c.set(1900, 0, 1);
										c.add(Calendar.DAY_OF_YEAR, day);
										cell.setCellValue(c.getTime());
									} else {
										throw new Exception("日期格式错误");
									}
									cell.setCellType(Cell.CELL_TYPE_STRING);
								} else {
									SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
									if (Cells.get(columnIndex[j]).isAnnotationPresent(action.Cell.class)) {
										action.Cell annotation = Cells.get(columnIndex[j]).getAnnotation(action.Cell.class);
										if (annotation.Type().equals("time"))
											sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
									}
									cell.setCellValue(sdf.format(localObject));

									// System.out.println(localObject);

									cell.setCellType(Cell.CELL_TYPE_STRING);
								}
								break;
							case ColT.cDOUBLE:
								// System.out.println("double");
								if (!(localObject instanceof Double)) {
									if (localObject instanceof String) {
										cell.setCellValue(Double.parseDouble(localObject.toString()));
									}
								} else {
									// System.out.println("+++++++++++++++++++++++++++++++++++"+localObject);
									if (Cells.get(columnIndex[j]).isAnnotationPresent(action.Cell.class)) {
										action.Cell annotation = Cells.get(columnIndex[j]).getAnnotation(action.Cell.class);
										if (annotation.Type().equals("NotZero")){
											if((Double) localObject != 0)
											cell.setCellValue((Double) localObject);
										}else{
											cell.setCellValue((Double) localObject);
										}
									}else{
										cell.setCellValue((Double) localObject);
									}
								}
								cell.setCellType(Cell.CELL_TYPE_NUMERIC);
								break;
							case ColT.cFlOAT:
								if (localObject instanceof Double) {
									cell.setCellValue(((Double) localObject).floatValue());
								} else {
									if (((Float) localObject).floatValue() != 0)
										cell.setCellValue(((Float) localObject).floatValue());
								}
								cell.setCellType(Cell.CELL_TYPE_NUMERIC);
								break;
							case ColT.cINT:
								if (localObject instanceof Double) {
									localObject = ((Double) localObject).intValue();
								} else {
									if (Cells.get(columnIndex[j]).isAnnotationPresent(action.Cell.class)) {
										action.Cell annotation = Cells.get(columnIndex[j]).getAnnotation(action.Cell.class);
										if (annotation.Type().equals("NotZero")){
											if(((Integer) localObject).floatValue() != 0)
												cell.setCellValue(((Integer) localObject).floatValue());
										}else{
											cell.setCellValue(((Integer) localObject).floatValue());
										}
									}else{
										cell.setCellValue(((Integer) localObject).floatValue());
									}
								}
								cell.setCellType(Cell.CELL_TYPE_NUMERIC);
								break;
							case ColT.cLONG:
								if (localObject instanceof Double) {
									cell.setCellValue(((Double) localObject).longValue());
								} else {
									if (((Long) localObject).floatValue() != 0)
										cell.setCellValue(((Long) localObject).floatValue());
								}
								cell.setCellType(Cell.CELL_TYPE_NUMERIC);
								break;
							case ColT.cSTRING:
								cell.setCellValue(localObject.toString());
								cell.setCellType(Cell.CELL_TYPE_STRING);
								break;
							case ColT.cBoolean:
								cell.setCellValue((Boolean) localObject);
								cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
							}
						} else {
							cell.setCellValue("");
						}
						cell.setCellStyle(getTableStyle());
					}

				}
			} else {
				row = sheet.createRow(i);
				continue;
			}
			// }
		}
		FileOutputStream fileOut = new FileOutputStream(file);
		wbs.write(fileOut);
		fileOut.close();
	}

	public Workbook createWorkbook(String filename) throws Exception {
		Workbook wbs = null;
		String suffix = getSuffix(filename);
		if (XLS.equals(suffix)) {
			System.out.println("xls");
			wbs = new HSSFWorkbook();
		} else if (XLSX.equals(suffix)) {
			System.out.println("xlsx");
			wbs = new XSSFWorkbook();
		}
		return wbs;
	}

	/**
	 * Create Sheet
	 * @param sheetname
	 * @return
	 * @throws Exception
	 */
	public Sheet createSheet(Workbook wbs, String sheetname) throws Exception {
		Sheet sheet = null;
		String safeName = WorkbookUtil.createSafeSheetName(sheetname);
		sheet = wbs.createSheet(safeName);
		return sheet;
	}

	private static Sheet getSheet(Workbook wbs, String sheetname) throws Exception {
		Sheet sheet = wbs.getSheet(sheetname);
		return sheet;
	}

	public CellStyle getFirstStyle() {

		CellStyle style = wbs.createCellStyle();
		Font font = wbs.createFont();
		font.setFontName("Times New Roman");
		font.setFontHeightInPoints((short) 9);// 设置字体大小
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setAlignment(CellStyle.ALIGN_LEFT);// 左右居中
		style.setFont(font);
		return style;
	}

	public CellStyle getNameStyle() {

		CellStyle style = wbs.createCellStyle();
		Font font = wbs.createFont();
		font.setFontName("Times New Roman");
		font.setFontHeightInPoints((short) 10);// 设置字体大小
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setAlignment(CellStyle.ALIGN_CENTER);// 左右居中
		style.setFont(font);
		return style;
	}

	/** 
	 * 标题行样式 
	 *  
	 * @return 
	 */
	public CellStyle getTitleStyle() {

		CellStyle style = wbs.createCellStyle();
		Font font = wbs.createFont();
		font.setFontName("Times New Roman");
		font.setFontHeightInPoints((short) 8);// 设置字体大小
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setAlignment(CellStyle.ALIGN_CENTER);// 左右居中
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);// 上线居中
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setWrapText(true);
		style.setFont(font);
		return style;
	}

	/** 
	 * 蓝色样式 
	 *  
	 * @return 
	 */
	public CellStyle getTitleStyle(short index) {

		CellStyle style = wbs.createCellStyle();
		Font font = wbs.createFont();
		font.setFontName("Times New Roman");
		font.setFontHeightInPoints((short) 8);// 设置字体大小
		font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
		style.setAlignment(CellStyle.ALIGN_CENTER);// 左右居中
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);// 上线居中
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setWrapText(true);
		style.setFont(font);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setFillForegroundColor(index);
		return style;
	}

	/** 
	 * 表格样式 
	 *  
	 * @return 
	 */
	public CellStyle getTableStyle() {

		CellStyle style = wbs.createCellStyle();
		Font font = wbs.createFont();
		font.setFontName("Times New Roman");
		font.setFontHeightInPoints((short) 8);// 设置字体大小
		font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
		style.setAlignment(CellStyle.ALIGN_CENTER);// 左右居中
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);// 上线居中
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setWrapText(false);
		style.setFont(font);
		return style;
	}

	/** 
	 * 文本样式 
	 *  
	 * @return 
	 */
	public CellStyle getContentStyle() {
		CellStyle style = wbs.createCellStyle();
		Font font = wbs.createFont();
		font.setFontName("Times New Roman");
		font.setFontHeightInPoints((short) 8);
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setAlignment(CellStyle.ALIGN_CENTER);// 左右居中
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);// 上线居中
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setWrapText(false);
		style.setFont(font);
		return style;
	}

	/** 
	 * 底部，签名样式 
	 *  
	 * @return 
	 */
	public CellStyle getFootStyle() {

		CellStyle style = wbs.createCellStyle();
		Font font = wbs.createFont();
		font.setFontName("Arial");
		font.setFontHeightInPoints((short) 8);// 设置字体大小
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setAlignment(CellStyle.ALIGN_RIGHT);
		style.setFont(font);
		return style;
	}

	public CellStyle getLineStyle() {
		CellStyle style = wbs.createCellStyle();
		style.setBorderBottom(CellStyle.BORDER_THIN);
		return style;
	}

	public HashMap<Integer, HashMap<Integer, String[]>> getStringList(List<? extends ExcelObj> list, int row, int cell) {
		HashMap<Integer, HashMap<Integer, String[]>> result = null;
		if (list == null)
			return result;
		for (ExcelObj excelObj : list) {
			// System.out.println("start");
			String[] s = new String[2];
			action.Cell annotation = excelObj.getClass().getAnnotation(action.Cell.class);
			String name = "";
			if (annotation != null)
				name = annotation.name();
			String outValue = (String) excelObj.outValue("value");
			boolean isValue = (Boolean) excelObj.outValue("isValue");
			int x = (Integer) excelObj.outValue("x");
			int y = (Integer) excelObj.outValue("y");
			s[0] = name;
			if (!isValue)
				s[1] = outValue;
			if (result == null) {
				result = new HashMap<Integer, HashMap<Integer, String[]>>();
			}
			if (result.get(x) == null) {
				HashMap<Integer, String[]> hashMap = new HashMap<Integer, String[]>();
				hashMap.put(y, s);
				result.put(x, hashMap);
			} else {
				HashMap<Integer, String[]> hashMap = result.get(x);
				hashMap.put(y, s);
			}
		}
		return result;
	}
}
