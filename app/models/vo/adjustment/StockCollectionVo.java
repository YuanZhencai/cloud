package models.vo.adjustment;

import java.math.BigDecimal;
import java.util.HashMap;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.ExtUtil;

import models.Stock;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: StockSearchVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockCollectionVo {
	public String pino;
	public String warehouseId;
	public String warehouse;
	public String areaId;
	public String area;
	public String binId;// 特殊作用不展示
	public String bin;
	public String materialId;// 特殊作用不展示
	public String materialCode;
	public BigDecimal systemQuantity;
	public String quantityUomId;
	public String quantityUom;
	public String batchId;// 特殊作用不展示
	public String batchNo;
	public BigDecimal adjusterQuantity;
	public String reason;
	public boolean ifshow = true;

	public void filledVoLoc(Stock stock) {
		HashMap<String, String> ext = ExtUtil.unapply(stock.batch.ext);
		pino = ext.get("pi");
		warehouseId = stock.bin.area.storageType.warehouse.id.toString();
		warehouse = stock.bin.area.storageType.warehouse.nameKey;
		areaId = stock.bin.area.id.toString();
		area = stock.bin.area.nameKey;
		binId = stock.bin.id.toString();
		bin = stock.bin.nameKey;
		materialId = stock.material.id.toString();
		materialCode = stock.material.materialCode;
		systemQuantity = stock.qty;
		quantityUomId = stock.materialUom.id.toString();
		quantityUom = stock.materialUom.uomCode;
		adjusterQuantity = null;
	}

	public void filledVoBat(Stock stock) {
		HashMap<String, String> ext = ExtUtil.unapply(stock.batch.ext);
		pino = ext.get("pi");
		warehouseId = stock.bin.area.storageType.warehouse.id.toString();
		warehouse = stock.bin.area.storageType.warehouse.nameKey;
		materialId = stock.material.id.toString();
		materialCode = stock.material.materialCode;
		batchId = stock.batch.id.toString();
		batchNo = ext.get("lot");
		systemQuantity = stock.qty;
		quantityUomId = stock.materialUom.id.toString();
		quantityUom = stock.materialUom.uomCode;
		adjusterQuantity = null;
	}

	public String toString() {
		return "pino:" + pino + " ,warehouseId:" + warehouseId + " ,warehouse:" + warehouse + " ,areaId:" + areaId + " ,area:" + area + " ,binId:"
				+ binId + " ,bin:" + bin + " ,materialId:" + materialId + " ,materialCode:" + materialCode + " ,systemQuantity:" + systemQuantity
				+ " ,quantityUom:" + quantityUom + " ,adjusterQuantity:" + adjusterQuantity + " ,reason:" + reason;
	}
}
