package models.vo.inbound;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.ExtUtil;
import utils.UnitConversion;
import models.Execution;
import models.PlanItem;
import models.PlanItemDetail;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: PlanItemVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanItemVo {
	public boolean ifSelect = false;
	public String planItemId;
	public Timestamp productionDate;
	public String productionLineId;
	public String productionLine;
	public String piNo;
	// public String batchNo;
	public String materialDescription;
	public BigDecimal productionqty;
	public String productionuom;
	public String productionuomId;
	public BigDecimal piSKU;
	public String skuUOM;
	public String skuUOMId;
	public boolean ifExecution;
	// 只为add detail所用
	public BigDecimal qtyPerPallet;
	public int palletCount;
	// Plan status
	public BigDecimal receivedQty = new BigDecimal(0);
	public String styleClass;
	// PI status
	public BigDecimal piReceivedQty = new BigDecimal(0);
	public BigDecimal piqyt;
	public String piStyleClass;
	// 换算关系
	public BigDecimal conversion;
	// receive from return使用
	public String referenceNo;

	public void filledVo(PlanItem planItem, boolean ifExecute) {
		planItemId = planItem.id.toString();
		productionDate = planItem.plan.plannedTimestamp;
		productionLineId = planItem.toBin.id.toString();
		productionLine = planItem.toBin.nameKey;
		piNo = planItem.order.internalOrderNo;
		// batchNo = ExtUtil.unapply(planItem.ext).get("lot");
		materialDescription = planItem.material.materialName;
		productionuom = planItem.fromMaterialUom.uomCode;
		productionuomId = planItem.fromMaterialUom.id.toString();
		piSKU = planItem.palnnedQty;
		skuUOM = planItem.toMaterialUom.uomCode;
		skuUOMId = planItem.toMaterialUom.id.toString();
		// 换算比例
		conversion = UnitConversion.SkuToQuantity(planItem.orderItem.settlementUom.id.toString(), planItem.materialUom.id.toString());
		// 自动计算出来的
		productionqty = piSKU.multiply(conversion);
		ifExecution = ifExecute;
		HashMap<String, String> orderItemExt = ExtUtil.unapply(planItem.orderItem.ext);
		if (orderItemExt.get("qtyPerPallet") != null) {
			qtyPerPallet = new BigDecimal(orderItemExt.get("qtyPerPallet"));
		} else {
			qtyPerPallet = new BigDecimal(0);
		}
		List<PlanItemDetail> PlanItemDetailList = PlanItemDetail.find().where().eq("deleted", false).eq("planItem.id", planItem.id)
				.isNotNull("assignedTo").findList();
		for (PlanItemDetail planItemDetail : PlanItemDetailList) {
			HashMap<String, String> map = ExtUtil.unapply(planItemDetail.ext);
			if (StringUtils.isNotEmpty(map.get("leaderId")) && StringUtils.isNotEmpty(map.get("paworkerId")) && planItemDetail.assignedTo != null) {
				receivedQty = receivedQty.add(planItemDetail.palnnedQty);
			}
		}
		receivedQty = receivedQty.multiply(conversion);
		if (receivedQty.doubleValue() > productionqty.doubleValue()) {
			styleClass = "errorNum";
		} else if (receivedQty.doubleValue() == productionqty.doubleValue()) {
			styleClass = "";
		} else {
			styleClass = "blueNum";
		}
		List<Execution> executionList = Execution.find().where().eq("deleted", false).eq("planItem.planType", "T001")
				.eq("planItem.orderItem.id", planItem.orderItem.id).findList();
		for (Execution execution : executionList) {
			piReceivedQty = piReceivedQty.add(execution.executedQty);
		}
		piReceivedQty = piReceivedQty.multiply(conversion);
		piqyt = planItem.orderItem.settlementQty;
		if (piReceivedQty.doubleValue() > piqyt.doubleValue()) {
			piStyleClass = "errorNum";
		} else if (piReceivedQty.doubleValue() == piqyt.doubleValue()) {
			piStyleClass = "";
		} else {
			piStyleClass = "blueNum";
		}
		if (orderItemExt.get("referenceNo") != null) {
			referenceNo = orderItemExt.get("referenceNo");
		}
	}
}
