package models.vo.adjustment;

import java.math.BigDecimal;
import java.util.HashMap;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.ExtUtil;

import models.Stock;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: StockVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockVo {
	public String id;
	public String stockNo;
	public String area;
	public String bin;
	public String batch;
	public BigDecimal qty;
	public String material;
	public String materialUOM;
	public BigDecimal adjustQty;
	public boolean ngshow = true;
	public String reason;

	public void fillVo(Stock stock) {
		HashMap<String, String> ext = ExtUtil.unapply(stock.batch.ext);
		HashMap<String, String> extStock = ExtUtil.unapply(stock.ext);
		stockNo = extStock.get("stockNo");
		id = stock.id.toString();
		area = stock.area.nameKey;
		bin = stock.bin.nameKey;
		batch = ext.get("lot");
		qty = stock.qty;
		material = stock.material.materialCode;
		materialUOM = stock.materialUom.uomCode;
		adjustQty = stock.qty;
	}
}
