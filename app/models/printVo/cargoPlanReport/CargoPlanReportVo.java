package models.printVo.cargoPlanReport;

import java.util.Date;
import java.util.List;

import action.Cell;
import utils.Excel.ExcelObj;
import utils.Excel.PrintObj;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: CargoPlanReportVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
public class CargoPlanReportVo extends PrintObj {
	private Date reportDate;
	private String intProdNo;
	private String style;
	private int type;
	private String sheet;
	@Cell(name = "head", Size = 0)
	private List<? extends ExcelObj> head;
	@Cell(name = "foot", Size = 0)
	private List<? extends ExcelObj> foot;
	@Cell(name = "Dates")
	private List<? extends ExcelObj> Dates;

	@Override
	public Date getReportDate() {
		return reportDate;
	}

	@Override
	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}

	@Override
	public String getIntProdNo() {
		return intProdNo;
	}

	@Override
	public void setIntProdNo(String intProdNo) {
		this.intProdNo = intProdNo;
	}

	@Override
	public String getStyle() {
		return style;
	}

	@Override
	public void setStyle(String style) {
		this.style = style;
	}

	@Override
	public String getSheetName() {
		return sheet;
	}

	@Override
	public void setSheetName(String sheetName) {
		this.sheet = sheetName;
	}

	@Override
	public List<? extends ExcelObj> getDates() {
		return Dates;
	}

	@Override
	public void setDates(List<? extends ExcelObj> dates) {
		this.Dates = dates;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public void setType(int type) {
		this.type = type;
	}

	public List<? extends ExcelObj> getHead() {
		return head;
	}

	public void setHead(List<? extends ExcelObj> head) {
		this.head = head;
	}

	public List<? extends ExcelObj> getFoot() {
		return foot;
	}

	public void setFoot(List<? extends ExcelObj> foot) {
		this.foot = foot;
	}
}
