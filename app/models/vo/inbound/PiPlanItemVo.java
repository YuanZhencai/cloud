package models.vo.inbound;

import java.util.Date;
import java.util.HashMap;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.DateUtil;
import utils.ExtUtil;

/**
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: PiPlanItemVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PiPlanItemVo {
	public String id;
	public String planId;
	public boolean haslive = false;
	public boolean hasPlan = false;
	public String piNo;
	public Date prodDate;
	public String splitProduction;
	public String prodLine;
	public String batchNo;
	public String uom;
	public double piSku;
	public String skuUom;
	public double prodQty;
	public BinVo binVo;
	public String piStatus;

	public String returnBatchExt() {
		HashMap<String, String> Ext = new HashMap<String, String>();
		Ext.put("date", prodDate != null ? DateUtil.dateToStrShort(prodDate) : "");
		Ext.put("line", binVo.id);
		Ext.put("pi", piNo);
		Ext.put("splitProduction",splitProduction!=null?splitProduction:"");
		Ext.put("lot", batchNo==null?"":batchNo);
		return ExtUtil.apply(Ext);
	}

	public void setBatchExt(String ext) {
		HashMap<String, String> Ext = ExtUtil.unapply(ext);
		if (Ext.get("date") != null && !"".equals(Ext.get("date")))
			prodDate = DateUtil.strToDate(Ext.get("date"), "yyyy-MM-dd");
		binVo.id = Ext.get("line");
		piNo = Ext.get("pi");
		splitProduction=Ext.get("splitProduction");
		batchNo = Ext.get("lot");
	}
}
