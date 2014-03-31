package models.vo.query;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.Batch;
import models.Bin;
import models.OrderItem;
import models.Stock;
import models.Warehouse;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.avaje.ebean.Ebean;
 

import utils.DateUtil;
import utils.EmptyUtil;
import utils.ExtUtil;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: WarehouseVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockCountVo {
	public String id;
	public String binId;
	public String piNo;
	public String materialCode;
	public String materialDescription;
	public String batchNo;
	public String warehouse;
	public String storageType;
	public String storageArea;
	public String storageBin;
	public String prodLine;
	public String prodDate;
	public double systemQty = 0;
	public String palletQty;
	public String QtyUom;
	public String stockNo;
	public int stockCount;
	private Set<String> set = new HashSet<String>();

	public void inBin(Bin bin) {
		this.binId=bin.id.toString();
		storageBin = bin.nameKey;
		storageArea = bin.area.nameKey;
		storageType = bin.area.storageType.nameKey;
		warehouse = bin.area.storageType.warehouse.nameKey;
		// this.systemQty=stock.qty.doubleValue();
	}

	public void inStock(Stock stock) {
		if (stock.materialUom != null)
			QtyUom = stock.materialUom.uomCode;
		if (stock.material != null) {
			materialCode = stock.material.materialCode;
			materialDescription = stock.material.materialName;
		}
	}

	public void inStockVo(StockVo stockVo) {
		this.warehouse = stockVo.warehouse;
		this.storageType = stockVo.storageType;
		this.storageArea = stockVo.storageArea;
		this.storageBin = stockVo.storageBin;
		this.systemQty = stockVo.quantityPallet;
		stockNo = stockVo.No;
	}

	public void addQty(double qty) {
		this.systemQty = systemQty + qty;
		++stockCount;
	}

	public void inBatch(Batch batch) {
		id = batch.id.toString();
		if (batch.material != null) {
			materialCode = batch.material.materialCode;
			materialDescription = batch.material.materialName;
		}
		this.setExt(batch.ext);
	}

	public void setExt(String ext) {
		HashMap<String, String> Ext = ExtUtil.unapply(ext);
		piNo = Ext.get("pi");
		batchNo = Ext.get("lot");
		List<OrderItem> orderItems = Ebean.find(OrderItem.class).where().eq("deleted", false).eq("order.internalOrderNo", piNo).findList();
		if (EmptyUtil.isNotEmptyList(orderItems)) {
			palletQty = ExtUtil.unapply(orderItems.get(0).ext).get("qtyPerPallet");
		}
		String line = Ext.get("line");
		if (line != null) {
			Bin bin = Bin.find().where().eq("deleted", false).eq("id", line).findUnique();
			if (bin != null)
				prodLine = bin.nameKey;
		}
		setMultProDate(Ext);
	}

	public void setMultProDate(HashMap<String, String> Ext) {
		String date = Ext.get("date");
		if (date != null&&prodDate==null) {
			set.add(date);
			try {
				prodDate = DateUtil.dateToStringEn2(DateUtil.strToDate(date, "yyyy-MM-dd"));
			} catch (Exception e) {
				prodDate = DateUtil.dateToStringEn2(new Date(Long.valueOf(date)));
			}
		}else if(date != null&&prodDate!=null&&!set.contains(date)){
			set.add(date);
			try {
				prodDate = prodDate+","+DateUtil.dateToStringEn2(DateUtil.strToDate(date, "yyyy-MM-dd"));
			} catch (Exception e) {
				prodDate = prodDate+","+DateUtil.dateToStringEn2(new Date(Long.valueOf(date)));
			}
		}
	}
	public void appendStockNo(String stockNo){
		if(this.stockNo==null){
			this.stockNo =  stockNo;
			stockCount = 1;
		}else{
			this.stockNo = this.stockNo +","+stockNo;
			stockCount++;
		}
	}
}
