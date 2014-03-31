package models.vo.inbound;

import java.math.BigDecimal;
import java.util.HashMap;

import utils.DateUtil;
import utils.ExtUtil;
import utils.UnitConversion;
import models.PlanItem;
import models.PlanItemDetail;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: PrintVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
public class PrintVo {
	public String piNo;
	public String batchNo;
	public String palletNo;
	public String packing;
	public String qty;
	public String receivingDate;
	public String storageLocation;
	public String description;
	public String barCode;

	public void fillVo(PlanItem planItem, PlanItemDetail planItemDetail, PlanItemDetailVo planItemdetailVo) {
		HashMap<String, String> ext = ExtUtil.unapply(planItem.ext);
		HashMap<String, String> detailExt = ExtUtil.unapply(planItemDetail.ext);
		piNo = ext.get("pi");
		batchNo = detailExt.get("batchNo");
		packing = UnitConversion.SkuToQuantity(planItem.fromMaterialUom, planItem.toMaterialUom).setScale(0, BigDecimal.ROUND_HALF_UP).toString();
		qty = planItemDetail.palnnedQty.setScale(2).toString();
		if (detailExt.get("productionDate") != null) {
			receivingDate = DateUtil.dateToStrShortEn(DateUtil.strToDate(detailExt.get("productionDate"), "yyyy-MM-dd"));
		}
		palletNo = detailExt.get("stockno");
		if (planItemDetail.toArea != null && planItemDetail.toBin != null) {
			storageLocation = planItemDetail.toArea.nameKey + "-" + planItemDetail.toBin.nameKey;
		}
		if (planItem.material != null) {
			description = planItem.material.materialName;
		}
		// barCode组成：piNo-lotNo
		barCode = piNo + "-" + batchNo;
	}

	public String toString() {
		return "piNo:" + piNo + ", batchNo:" + ", Packing:" + packing + ", qty:" + qty + ", ReceivingDate:" + receivingDate + ", StorageLocation"
				+ storageLocation + ", Description" + ", barCode" + barCode;
	}
}
