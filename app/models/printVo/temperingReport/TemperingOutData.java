/** * TemperingOutData.java 
* Created on 2013-6-8 上午11:21:36 
*/

package models.printVo.temperingReport;

import models.StockTransaction;
import utils.ExtUtil;
import utils.Excel.ExcelObj;
import action.Cell;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: TemperingOutData.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public class TemperingOutData extends ExcelObj {

	   @Cell(name="No")
		public Integer no;
	    @Cell(name="Texture Condition(OK/Not Ok)")
		public String textureCondition;
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
		public static TemperingOutData getTemperingOutData(StockTransaction stockTransaction){
			TemperingOutData td = new TemperingOutData();
			td.textureCondition = null;
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

		public String getTextureCondition() {
			return textureCondition;
		}

		public void setTextureCondition(String textureCondition) {
			this.textureCondition = textureCondition;
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
