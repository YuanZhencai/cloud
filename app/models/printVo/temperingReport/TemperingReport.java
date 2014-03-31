/** * TemperingReport.java 
* Created on 2013-5-30 下午2:22:18 
*/

package models.printVo.temperingReport;

import java.util.Date;
import java.util.List;

import action.Cell;

import utils.Excel.ExcelObj;
import utils.Excel.PrintObj;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: TemperingReport.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public class TemperingReport extends PrintObj{
	private Date   reportDate;
	private String intProdNo;
	private String style;
	private int type;
	private String sheet;
	@Cell(name="head",Size=9)
	private List<? extends ExcelObj> head;
	@Cell(name="foot",Size=10)
	private List<? extends ExcelObj> foot;
	@Cell(name="Dates")
	private List<? extends ExcelObj> Dates;

	
	
	
	
	@Override
	public Date getReportDate() {
		// TODO Auto-generated method stub
		return reportDate;
	}
	@Override
	public void setReportDate(Date reportDate) {
		// TODO Auto-generated method stub
		this.reportDate=reportDate;
	}
	@Override
	public String getIntProdNo() {
		// TODO Auto-generated method stub
		return intProdNo;
	}
	@Override
	public void setIntProdNo(String intProdNo) {
		// TODO Auto-generated method stub
		this.intProdNo=intProdNo;
	}
	@Override
	public String getStyle() {
		// TODO Auto-generated method stub
		return style;
	}
	@Override
	public void setStyle(String style) {
		// TODO Auto-generated method stub
		this.style=style;
	}
	
	@Override
	public String getSheetName() {
		// TODO Auto-generated method stub
		return sheet;
	}
	@Override
	public void setSheetName(String sheetName) {
		// TODO Auto-generated method stub
		this.sheet=sheetName;
	}
	@Override
	public List<? extends ExcelObj> getDates() {
		// TODO Auto-generated method stub
		return Dates;
	}
	@Override
	public void setDates(List<? extends ExcelObj> dates) {
		// TODO Auto-generated method stub
		this.Dates=dates;
	}
	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return type;
	}
	@Override
	public void setType(int type) {
		// TODO Auto-generated method stub
		this.type=type;
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
