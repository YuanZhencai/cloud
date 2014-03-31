package models.vo.query;

import java.util.HashMap;

import utils.ExtUtil;
import models.Stock;

public class StockVo {
	public String No;
	public String warehouse;
	public String storageArea;
	public String storageBin;
	public String storageType;
	public double quantityPallet;
	public void inStock(Stock stock){
		this.setExt(stock.ext);
		storageType=stock.area.storageType.nameKey;
		warehouse=stock.warehouse.nameKey;
		storageArea=stock.area.nameKey;
		storageBin=stock.bin.nameKey;
		quantityPallet=stock.qty.doubleValue();
	}
	public void setExt(String ext){
		HashMap<String, String> Ext = ExtUtil.unapply(ext);
		this.No= Ext.get("stockNo");
	}
}
