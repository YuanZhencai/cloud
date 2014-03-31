package utils.Excel;

import java.util.Date;
import java.util.List;

public abstract class PrintObj {
	public List<? extends ExcelObj> head;
	public List<? extends ExcelObj> foot;
	private List<? extends ExcelObj> Dates;

	public abstract Date getReportDate();

	public abstract void setReportDate(Date reportDate);

	public abstract String getIntProdNo();

	public abstract void setIntProdNo(String intProdNo);

	public abstract String getStyle();

	public abstract void setStyle(String style);

	public abstract String getSheetName();

	public abstract void setSheetName(String sheetName);

	public abstract List<? extends ExcelObj> getDates();

	public abstract void setDates(List<? extends ExcelObj> dates);

	public abstract int getType();

	public abstract void setType(int type);

	public abstract List<? extends ExcelObj> getHead();

	public abstract void setHead(List<? extends ExcelObj> head);

	public abstract List<? extends ExcelObj> getFoot();

	public abstract void setFoot(List<? extends ExcelObj> foot);
}
