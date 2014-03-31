/** * ProductionScheduleData.java 
* Created on 2013-6-7 上午10:38:00 
*/

package models.printVo.goodsTransferDetails;

import java.util.Date;
import java.util.HashMap;

import models.StockTransaction;
import utils.ExtUtil;
import utils.Excel.ExcelObj;
import utils.exception.CustomException;
import action.Cell;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: ProductionScheduleData.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public class GoodsTransferDetailsData extends ExcelObj {

	@Cell(name = "Pi No")
	public String piNo;
	@Cell(name = "Batch No")
	public String batchNo;
	@Cell(name = "Material Desc")
	public String matDesc;
	@Cell(name = "Pallet No")
	public String palletNo;
	@Cell(name = "From storage bin")
	public String from;
	@Cell(name = "To storage bin")
	public String to;
	@Cell(name = "qty/pallet")
	public Double qty;
	@Cell(name = "UOM")
	public String uom;
	@Cell(name = "Execution date")
	public Date exeDate;
	public void setGoodsTransferDetailsData(StockTransaction st) throws CustomException {
		HashMap<String, String> batchMap;
		try {
			batchMap = ExtUtil.unapply(st.stock.batch.ext);
		} catch (Exception e) {
			 throw new CustomException();
		}
		HashMap<String, String> stockhMap = ExtUtil.unapply(st.stock.ext);
		this.piNo = batchMap.get("pi");
		this.batchNo = batchMap.get("lot");
		this.palletNo = stockhMap.get("stockNo");
		this.matDesc = st.execution.material.materialName;
		this.from = st.execution.fromArea.areaCode + " ~ " + st.execution.fromBin.binCode;
		this.to = st.execution.toArea.areaCode + " ~ " + st.execution.toBin.binCode;
		this.qty =  st.execution.executedQty.doubleValue();
		this.uom = st.execution.fromMaterialUom.uomCode;
		this.exeDate = st.createdAt;
	}
 
	
	public String getPiNo() {
		return piNo;
	}

	public void setPiNo(String piNo) {
		this.piNo = piNo;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public String getMatDesc() {
		return matDesc;
	}

	public void setMatDesc(String matDesc) {
		this.matDesc = matDesc;
	}

	public String getPalletNo() {
		return palletNo;
	}

	public void setPalletNo(String palletNo) {
		this.palletNo = palletNo;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	 

	public Double getQty() {
		return qty;
	}


	public void setQty(Double qty) {
		this.qty = qty;
	}


	public String getUom() {
		return uom;
	}

	public void setUom(String uom) {
		this.uom = uom;
	}

}
