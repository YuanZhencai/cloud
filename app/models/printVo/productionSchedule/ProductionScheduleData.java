/** * ProductionScheduleData.java 
* Created on 2013-6-7 上午10:38:00 
*/

package models.printVo.productionSchedule;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.avaje.ebean.Ebean;

import models.Batch;
import models.Execution;
import models.MaterialUom;
import models.OrderItem;
import models.PlanItem;
import models.PlanItemDetail;
import models.Stock;
import utils.BatchSearchUtil;
import utils.CodeUtil;
import utils.DateUtil;
import utils.EmptyUtil;
import utils.ExtUtil;
import utils.UnitConversion;
import utils.Excel.ExcelObj;
import action.Cell;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: ProductionScheduleData.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public class ProductionScheduleData extends ExcelObj {

	@Cell(name = "Date")
	public Double date;
	@Cell(name = "Prs (MT) /DAY")
	public Double prs;
	@Cell(name = "Index")
	public String index;
	@Cell(name = "Buyer")
	public String buyer;
	@Cell(name = "Reference No./Dest.")
	public String refeandDest;
	@Cell(name = "No.PI")
	public String piNo;
	@Cell(name = "Brand")
	public String brand;
	@Cell(name = "Mp")
	public String mp;
	@Cell(name = "@ kg")
	public String kg;
	@Cell(name = "N2")
	public String n2;
	@Cell(name = "Shipment")
	public String shipment;
	@Cell(name = "Remark")
	public String remark;
	@Cell(name = "MARKING")
	public String marking;
	@Cell(name = "Clog date")
	public Date clogDate;
	@Cell(name = "Time")
	public String time;
	@Cell(name = "planItem.orderItem REC")
	public Date pirec;
	@Cell(name = "ctn ready")
	public String ctnReady;
	@Cell(name = "LINE")
	public String line;
	@Cell(name = "Trader")
	public String trader;

	// 19 cloumn

	public static ProductionScheduleData setProductionScheduleData(PlanItem planItem) {
		ProductionScheduleData piVo = new ProductionScheduleData();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		HashMap<String, String> piExt = ExtUtil.unapply(planItem.orderItem.ext);
		HashMap<String, String> cargoExt = null;
		boolean flag = false;
		// boolean stockFlag = true;
		// closedDate closedTime
		List<OrderItem> cargos = Ebean.find(OrderItem.class).fetch("order").where().eq("deleted", false)
			.eq("order.orderType", CodeUtil.getOrderType("Cargo")).eq("order.externalOrderNo", planItem.order.externalOrderNo).findList();
		if (cargos != null && !cargos.isEmpty()) {
			OrderItem cargo = cargos.get(0);
			cargoExt = ExtUtil.unapply(cargo.ext);
			flag = true;
		}
		// 当天生产量 prs
		List<Execution> exes = Ebean.find(Execution.class).fetch("planItemDetail")
			.fetch("planItemDetail.planItem").where().eq("deleted", false).eq("planItemDetail.deleted", false)
			.eq("planItemDetail.planItem.id", planItem.id).findList();
		BigDecimal totalQty = new BigDecimal(0);
		if (EmptyUtil.isNotEmptyList(exes)) {
			for (Execution exe : exes) {
				String productionDate = ExtUtil.unapply(exe.planItemDetail.ext).get("productionDate");
				String date = ExtUtil.unapply(exe.planItemDetail.planItem.ext).get("date");
				if ((EmptyUtil.nullToEmpty(productionDate)).equals(date))
				totalQty = totalQty.add(exe.planItemDetail.palnnedQty);
			}
		}
		BigDecimal skuToQuantity = UnitConversion.SkuToQuantity(planItem.fromMaterialUom, planItem.toMaterialUom);
		totalQty = totalQty.multiply(skuToQuantity);
		piVo.kg = planItem.palnnedQty.multiply(skuToQuantity).setScale(2,BigDecimal.ROUND_HALF_UP).toString();
		Calendar ca = Calendar.getInstance();
		ca.setTime(planItem.createdAt);
		piVo.date = (double) ca.get(Calendar.DAY_OF_MONTH);
		piVo.prs = totalQty.doubleValue() / 1000;
		piVo.index = piExt.get("packingIndex");
		piVo.buyer = piExt.get("buyer");
		if (piExt.get("destination") != null && !"".equals(piExt.get("destination").trim()) && !"null".equals(piExt.get("destination").trim())) {
			piVo.refeandDest = planItem.orderItem.order.contractNo + "/" + piExt.get("destination");
		} else {
			piVo.refeandDest = planItem.orderItem.order.contractNo;
		}
		piVo.piNo = planItem.orderItem.order.externalOrderNo;
		piVo.brand = null;
		if (piExt.get("meltingPointfrom") != null && !"".equals(piExt.get("meltingPointfrom")) && piExt.get("meltingPointTo") != null
			&& !"".equals(piExt.get("meltingPointTo"))) {
			piVo.mp = piExt.get("meltingPointfrom") + "-" + piExt.get("meltingPointTo");
		}
		piVo.n2 = null;
		piVo.shipment = piExt.get("reqShipmentDate");
		piVo.remark = planItem.orderItem.remarks;
		piVo.marking = piExt.get("marking");
		if (flag) {
			if (cargoExt.get("closed_date") != null && !"".equals(cargoExt.get("closed_date").trim())
				&& !"null".equals(cargoExt.get("closed_date").trim())) {
				piVo.clogDate = new Date(Long.parseLong(cargoExt.get("closed_date")));
			}
			if (cargoExt.get("closed_time") != null && !"".equals(cargoExt.get("closed_time").trim())
				&& !"null".equals(cargoExt.get("closed_time").trim())) {
				piVo.time = sdf.format(new Date(Long.parseLong(cargoExt.get("closed_time"))));
			}
		}
		piVo.pirec = new Date(planItem.orderItem.createdAt.getTime());
		piVo.ctnReady = null;
		piVo.line = planItem.toBin.nameKey;
		return piVo;
	}

	public ProductionScheduleData() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ProductionScheduleData(Double date) {
		super();
		this.date = date;
	}

	public Double getDate() {
		return date;
	}

	public void setDate(Double date) {
		this.date = date;
	}

	public Double getPrs() {
		return prs;
	}

	public void setPrs(Double prs) {
		this.prs = prs;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getBuyer() {
		return buyer;
	}

	public void setBuyer(String buyer) {
		this.buyer = buyer;
	}

	public String getRefeandDest() {
		return refeandDest;
	}

	public void setRefeandDest(String refeandDest) {
		this.refeandDest = refeandDest;
	}

	public String getPiNo() {
		return piNo;
	}

	public void setPiNo(String piNo) {
		this.piNo = piNo;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getMp() {
		return mp;
	}

	public void setMp(String mp) {
		this.mp = mp;
	}

	public String getKg() {
		return kg;
	}

	public void setKg(String kg) {
		this.kg = kg;
	}

	public String getN2() {
		return n2;
	}

	public void setN2(String n2) {
		this.n2 = n2;
	}

	public String getShipment() {
		return shipment;
	}

	public void setShipment(String shipment) {
		this.shipment = shipment;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getMarking() {
		return marking;
	}

	public void setMarking(String marking) {
		this.marking = marking;
	}

	public Date getClogDate() {
		return clogDate;
	}

	public void setClogDate(Date clogDate) {
		this.clogDate = clogDate;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public Date getPirec() {
		return pirec;
	}

	public void setPirec(Date pirec) {
		this.pirec = pirec;
	}

	public String getCtnReady() {
		return ctnReady;
	}

	public void setCtnReady(String ctnReady) {
		this.ctnReady = ctnReady;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public String getTrader() {
		return trader;
	}

	public void setTrader(String trader) {
		this.trader = trader;
	}

}
