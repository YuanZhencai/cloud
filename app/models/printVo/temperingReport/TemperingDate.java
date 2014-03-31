package models.printVo.temperingReport;
/** * TemperingDate.java 
* Created on 2013-5-30 下午2:22:58 
*/

 

import models.StockTransaction;
import utils.ExtUtil;
import utils.Excel.ExcelObj;
import action.Cell;

import com.avaje.ebean.annotation.Sql;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: TemperingDate.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@Sql
public class TemperingDate extends ExcelObj {
    @Cell(name="No")
	public Integer no;
    @Cell(name="Product company")
	public String prodCompay;
    @Cell(name="Description")
	public String desc;
    @Cell(name="PI No / Sales Contract")
	public String piNoAdnContract;
    @Cell(name="Palet Number")
	public Double palletNo;
    @Cell(name="Total Boxes")
	public Double totalBox;
   @Cell(name="Area")
	public String area; 
    @Cell(name="Bin")
	public String bin;
    @Cell(name="Remaks")
	public String remarks;
	
	/**
	 * 
	 * @param stock
	 * @return
	 */
	public static TemperingDate getTemperingDate(StockTransaction stockTransaction){
		TemperingDate td = new TemperingDate();
		td.prodCompay = stockTransaction.stock.warehouse.company.nameKey;
		td.desc=stockTransaction.stock.material.materialName;
		td.piNoAdnContract=ExtUtil.unapply(stockTransaction.stock.batch.ext).get("pi");
		td.palletNo = (double) 1;
		td.totalBox =stockTransaction.newQty.doubleValue();
		td.area = stockTransaction.stock.area.nameKey;
		td.bin=stockTransaction.stock.bin.nameKey ;
		td.remarks = null;
		return td;
	}

	
	public Integer getNo() {
		return no;
	}

	public void setNo(Integer no) {
		this.no = no;
	}

	public String getProdCompay() {
		return prodCompay;
	}

	public void setProdCompay(String prodCompay) {
		this.prodCompay = prodCompay;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getPiNoAdnContract() {
		return piNoAdnContract;
	}

	public void setPiNoAdnContract(String piNoAdnContract) {
		this.piNoAdnContract = piNoAdnContract;
	}

	public Double getPalletNo() {
		return palletNo;
	}

	public void setPalletNo(Double palletNo) {
		this.palletNo = palletNo;
	}

	public Double getTotalBox() {
		return totalBox;
	}

	public void setTotalBox(Double totalBox) {
		this.totalBox = totalBox;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getBin() {
		return bin;
	}

	public void setBin(String bin) {
		this.bin = bin;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	 

 













 
	
	 
	 
	
	
	
	
	
	
	
	
	
	


	
}
