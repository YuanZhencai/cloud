package models.vo.inbound;

import java.math.BigDecimal;
import java.util.HashMap;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.CrudUtil;
import utils.ExtUtil;
import models.Execution;
import models.PlanItem;
import models.PlanItemDetail;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: ExecutionVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecutionVo {
	public int palletNumber;
	public String planItemId;
	public String planItemDetailId;
	public String executionId;
	public String warehouseId;
	public String warehouse;
	public String areaId;
	public String area;
	public String binId;
	public String bin;
	public String driverId;
	public String driver;
	public String paworkerId;
	public String paworker;
	public BigDecimal palletQty;
	public String status;
	public boolean ifExecution;
	public boolean ifSelect = false;

	// 填充detailVo
	public void filledVo(Execution execution, boolean isexection) {
		HashMap<String, String> ext = new HashMap<String, String>();
		ext = ExtUtil.unapply(execution.ext);
		planItemId = execution.planItem.id.toString();
		executionId = execution.id.toString();
		warehouseId = execution.planItem.order.warehouse.id.toString();
		warehouse = execution.planItem.order.warehouse.nameKey;
		if (execution.toArea != null) {
			areaId = execution.toArea.id.toString();
			area = execution.toArea.nameKey;
		}
		if (execution.toBin != null) {
			binId = execution.toBin.id.toString();
			bin = execution.toBin.nameKey;
		}
		driverId = ext.get("driverId");
		driver = ext.get("driver");
		paworkerId = ext.get("paworkerId");
		paworker = ext.get("paworker");
		palletQty = execution.executedQty;
		ifExecution = isexection;
		status = "Y";// 已经被execution
	}

	// 填充detailVo
	public void filledVo(PlanItemDetail planItemDetail, boolean isExection) {
		// EXT字段中获得paworker的name
		HashMap<String, String> ext = new HashMap<String, String>();
		ext = ExtUtil.unapply(planItemDetail.ext);
		planItemId = planItemDetail.planItem.id.toString();
		planItemDetailId = planItemDetail.id.toString();
		warehouseId = planItemDetail.planItem.order.warehouse.id.toString();
		warehouse = planItemDetail.planItem.order.warehouse.nameKey;
		if (planItemDetail.toArea != null) {
			areaId = planItemDetail.toArea.id.toString();
			area = planItemDetail.toArea.nameKey;
		}
		if (planItemDetail.toBin != null) {
			binId = planItemDetail.toBin.id.toString();
			bin = planItemDetail.toBin.nameKey;
		}
		if (planItemDetail.assignedTo != null) {
			driverId = planItemDetail.assignedTo.id.toString();
			driver = planItemDetail.assignedTo.employeeName;
		}
		paworkerId = ext.get("paworkerId");
		paworker = ext.get("paworker");
		palletQty = planItemDetail.palnnedQty;
		ifExecution = isExection;
		status = "N";// 未被执行execution
	}

	// 生成一条新的只有头的detail
	public String addDetail(PlanItemDetailVo planItemDetailVo, PlanItem planItem) {
		PlanItemDetail planItemDetail = new PlanItemDetail();
		planItemDetail.planItem = planItem;
		planItemDetail.fromMaterialUom = planItem.fromMaterialUom;
		planItemDetail.fromBin = planItem.toBin;
		planItemDetail.toMaterialUom = planItem.toMaterialUom;
		planItemDetail.palnnedQty = planItemDetailVo.palletQty;
		planItemDetail.detailStatus = "N";
		CrudUtil.save(planItemDetail);
		return planItemDetail.id.toString();
	}
}
