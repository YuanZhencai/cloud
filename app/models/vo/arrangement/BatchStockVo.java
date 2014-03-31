/** * PlanItemVo.java 
* Created on 2013-4-22 下午12:57:24 
*/

package models.vo.arrangement;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.avaje.ebean.Ebean;

import models.Batch;
import models.Bin;
import models.MaterialUom;
import models.Stock;
import utils.EmptyUtil;
import utils.ExtUtil;
import utils.UnitConversion;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: PlanItemVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public class BatchStockVo {
	public String planItemId;
	public Date proDate;
	public String proLine;
	public String piNo;
	public String batchNo;
	public String materialCode;
	public BigDecimal proQty;
	public String ProUom;
	public BigDecimal piSKU;

	public BatchStockVo() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static BatchStockVo getBatchStockVo(List<Stock> stocks) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		BatchStockVo planItemVo = new BatchStockVo();
		Batch batch = stocks.get(0).batch;
		HashMap<String, String> ext = ExtUtil.unapply(batch.ext);
		BigDecimal total = new BigDecimal(0);
		List<MaterialUom> basuom = Ebean.find(MaterialUom.class).where().eq("deleted", false).eq("material.id", batch.material.id)
			.eq("baseUom", true).findList();
		for (Stock stock : stocks) {
			total = total.add(stock.qty);
		}
		if (EmptyUtil.isNotEmptyList(basuom)) {
			planItemVo.ProUom = basuom.get(0).uomCode;
			try {
				planItemVo.proQty = total.multiply(UnitConversion.SkuToQuantity(basuom.get(0),stocks.get(0).materialUom));
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		planItemVo.planItemId = String.valueOf(batch.id);
		try {
			planItemVo.proDate = sdf.parse(ext.get("date"));
		} catch (ParseException e) {
			planItemVo.proDate = new Date();
		}
		if(EmptyUtil.isNotEmtpyString(ext.get("line"))){
			Bin bin = Ebean.find(Bin.class).where().eq("deleted", false).eq("id", ext.get("line")).findUnique();
			planItemVo.proLine = bin.nameKey;
		}
		planItemVo.piNo = ext.get("pi");
		planItemVo.batchNo = ext.get("lot");
		// planItemVo.batchNo = planItem.batch.batchNo;
		planItemVo.materialCode = batch.material.materialCode;
		planItemVo.piSKU = total;
		return planItemVo;
	}
}
