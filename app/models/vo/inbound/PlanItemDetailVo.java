package models.vo.inbound;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

import utils.CrudUtil;
import utils.DateUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import models.PlanItem;
import models.PlanItemDetail;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: PlanItemDetailVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
/** 
* <p>Project: CloudWMS</p> 
* <p>Title: PlanItemDetailVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanItemDetailVo {
	public final String DETATILSTATUS_NEW = "S000";// 新建detail的状态
	public final String DETATILSTATUS_PRINTED = "S001";// 已经被打印

	public String stockno;
	public String planItemId;
	public String planItemDetailId;
	public Date productionDate;
	public String productionLineId;
	public String productionLine;
	public String batchNo;
	public String blendingTank;
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
	public String leaderId;
	public String leader;
	public BigDecimal palletQty;
	public String status;
	public String executionOrNo;
	public boolean ifExecution;
	public boolean ifSelect = false;
	public Date modifiedat;
	public String modifiedby;

	public String shift;
	public String remarks;
	public String reason;

	/**
	 * 填充detailVo
	 * @param planItemDetail
	 * @param isexection
	 */
	public void filledVo(PlanItemDetail planItemDetail, boolean isexection) {
		planItemId = planItemDetail.planItem.id.toString();
		planItemDetailId = planItemDetail.id.toString();
		warehouseId = planItemDetail.planItem.order.warehouse.id.toString();
		warehouse = planItemDetail.planItem.order.warehouse.nameKey;
		if (planItemDetail.toArea != null) {
			areaId = planItemDetail.toArea.id.toString();
			area = planItemDetail.toArea.nameKey;
		}
		if (planItemDetail.fromBin != null) {
			productionLineId = planItemDetail.fromBin.id.toString();
			productionLine = planItemDetail.fromBin.nameKey;
		}
		if (planItemDetail.toBin != null) {
			binId = planItemDetail.toBin.id.toString();
			bin = planItemDetail.toBin.nameKey;
		}
		if (planItemDetail.assignedTo != null) {
			driverId = planItemDetail.assignedTo.id.toString();
			driver = planItemDetail.assignedTo.employeeName;
		}
		// EXT字段中获得paworker的name
		HashMap<String, String> ext = new HashMap<String, String>();
		ext = ExtUtil.unapply(planItemDetail.ext);
		paworkerId = ext.get("paworkerId");
		paworker = ext.get("paworker");
		leaderId = ext.get("leaderId");
		leader = ext.get("leader");
		stockno = ext.get("stockno");
		batchNo = ext.get("batchNo");
		blendingTank = ext.get("blendingTank");
		if (StringUtils.isNotEmpty(ext.get("productionDate"))) {
			productionDate = DateUtil.strToDate(ext.get("productionDate"), "yyyy-MM-dd");
		}
		palletQty = planItemDetail.palnnedQty;
		executionOrNo = isexection ? "Y" : "N";
		ifExecution = isexection;
		if (planItemDetail.detailStatus.equals(DETATILSTATUS_NEW)) {
			status = "N";
		} else {
			status = "Y";
		}
		if (StringUtils.isNotEmpty(ext.get("modifiedat"))) {
			modifiedat = DateUtil.strToDate(ext.get("modifiedat"), "yyyy-MM-dd HH:mm:ss");
		}
		if (StringUtils.isNotEmpty(ext.get("modifiedby"))) {
			modifiedby = ext.get("modifiedby");
		}
	}

	/**
	 * 查询序列的方法（未用）
	 * @return
	 */
	public String selectNextVaL() {
		String sql = "SELECT NEXTVAL('stockno') as stockno;";
		SqlRow rows = Ebean.createSqlQuery(sql).findUnique();
		String value = "KT";
		return value + rows.getLong("stockno");
	}

	/**
	 * 数据库中生成一条新的只有头的detail
	 * @param planItemDetailVo
	 * @param planItem
	 * @param i
	 * @return
	 */
	public PlanItemDetail addDetail(PlanItemDetailVo planItemDetailVo, PlanItem planItem, int i) {
		PlanItemDetail planItemDetail = new PlanItemDetail();
		planItemDetail.planItem = planItem;
		planItemDetail.fromMaterialUom = planItem.fromMaterialUom;
		planItemDetail.fromBin = planItem.toBin;
		planItemDetail.toMaterialUom = planItem.toMaterialUom;
		planItemDetail.palnnedQty = planItemDetailVo.palletQty;
		planItemDetail.detailStatus = DETATILSTATUS_NEW;
		HashMap<String, String> ext = new HashMap<String, String>();
		ext.put("stockno", String.valueOf(i));
		ext.put("productionDate", DateUtil.dateToStrShort(planItemDetailVo.productionDate));
		ext.put("batchNo", "");
		ext.put("modifiedby", SessionSearchUtil.searchUser().name);
		ext.put("modifiedat", DateUtil.dateToStrLong(DateUtil.timestampToDate(DateUtil.currentTimestamp())));
		planItemDetail.ext = ExtUtil.apply(ext);
		CrudUtil.save(planItemDetail);
		return planItemDetail;
	}

	/**
	 * 生成一条新的只有头的detailVo
	 * @param stockno
	 * @param planItemId
	 * @param warehouseName
	 * @param warehouseId
	 * @param qty
	 * @return
	 */
	public PlanItemDetailVo addVo(String stockno, PlanItemVo planItemVo, String warehouseName, String warehouseId, BigDecimal qty) {
		PlanItemDetailVo planItemDetailVo = new PlanItemDetailVo();
		planItemDetailVo.stockno = stockno;
		planItemDetailVo.productionDate = DateUtil.timestampToDate(planItemVo.productionDate);
		planItemDetailVo.productionLineId = planItemVo.productionLineId;
		planItemDetailVo.productionLine = planItemVo.productionLine;
		planItemDetailVo.planItemId = planItemVo.planItemId;
		planItemDetailVo.warehouseId = warehouseId;
		planItemDetailVo.warehouse = warehouseName;
		planItemDetailVo.ifExecution = false;
		planItemDetailVo.palletQty = qty;
		planItemDetailVo.status = "N";
		planItemDetailVo.executionOrNo = "N";
		return planItemDetailVo;
	}

	/**
	 * 组装Vo剩余所需：id ,modifiedat,modifiedby
	 * @param planItemDetail
	 * @param planItemDetailVo
	 * @return
	 */
	public PlanItemDetailVo addVoExt(PlanItemDetail planItemDetail, PlanItemDetailVo planItemDetailVo) {
		HashMap<String, String> ext = ExtUtil.unapply(planItemDetail.ext);
		planItemDetailVo.planItemDetailId = planItemDetail.id.toString();
		if (StringUtils.isNotEmpty(ext.get("modifiedat"))) {
			planItemDetailVo.modifiedat = DateUtil.strToDate(ext.get("modifiedat"), "yyyy-MM-dd HH:mm:ss");
		}
		if (StringUtils.isNotEmpty(ext.get("modifiedby"))) {
			planItemDetailVo.modifiedby = ext.get("modifiedby");
		}
		return planItemDetailVo;
	}

	public String getStockno() {
		return stockno;
	}

	public void setStockno(String stockno) {
		this.stockno = stockno;
	}
}