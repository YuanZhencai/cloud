package models.printVo.cargoPlanReport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import models.MaterialUom;
import models.OrderItem;
import models.PlanItem;

import action.Cell;
import utils.DateUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import utils.UnitConversion;
import utils.Excel.ExcelObj;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: CargoPlanDateVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
public class CargoPlanDateVo extends ExcelObj {
	@Cell(name = "NO.")
	public String piNo;
	@Cell(name = "Split Shipment")
	public String splitShipment;
	@Cell(name = "Description")
	public String description;
	@Cell(name = "Net Weight")
	public String netWeight;
	@Cell(name = "Container FCL")
	public int containerFcl;
	@Cell(name = "Feet")
	public String feet;
	@Cell(name = "Type")
	public String type;
	@Cell(name = "Qty/Fcl")
	public double qtyFcl;
	@Cell(name = "IPB Number")
	public String ipbNumber;
	@Cell(name = "")
	public String columnOne;
	@Cell(name = "")
	public String columnTwo;
	@Cell(name = "Schedule")
	public String schedule;
	@Cell(name = "")
	public String columnThr;
	@Cell(name = "")
	public String columnFor;
	@Cell(name = "Truck", Type = "deleted")
	public String truck;
	@Cell(name = "Stuffing Date", Type = "deleted")
	public Date stuffingDate;
	@Cell(name = "Container No./Seal No", Type = "deleted")
	public String containerNo;
	@Cell(name = "batch", Type = "deleted")
	public String batch;
	@Cell(name = "Qty", Type = "deleted")
	public double qty;
	@Cell(name = "Palletized")
	public String palletized;
	@Cell(name = "Fumigation")
	public String fumigation;
	@Cell(name = "Out")
	public String out;
	@Cell(name = "Closing Date")
	public String closingDate;
	@Cell(name = "Closing Time")
	public String closingTime;
	@Cell(name = "Remarks")
	public String remarks;
	@Cell(name = "Transport")
	public String transport;
	@Cell(name = "Vessel")
	public String vessel;
	@Cell(name = "Destination")
	public String destination;

	public CargoPlanDateVo setCargoPlanDateVo(OrderItem orderitem, int containerNo, double qty) {
		OrderItem orderItem = OrderItem.find().where().eq("id", orderitem.id).findUnique();
		CargoPlanDateVo cargoPlanDateVo = new CargoPlanDateVo();
		HashMap<String, String> orderItemExtMap = ExtUtil.unapply(orderItem.ext);
		if (orderItem.order != null) {
			cargoPlanDateVo.piNo = orderItem.order.internalOrderNo;
		}
		cargoPlanDateVo.splitShipment = orderItemExtMap.get("splitShipment");
		if (orderItem.material != null) {
			cargoPlanDateVo.description = orderItem.material.materialName;
		}
		String skuuom = orderItemExtMap.get("qtyPerPalletUom");
		double netweigth = 0;
		if (skuuom != null) {
			MaterialUom materialUom = MaterialUom.find().byId(skuuom);
			if (materialUom != null && orderItem.settlementUom != null && orderItem.material != null) {
				netweigth = UnitConversion.returnComparing(orderItem.settlementUom.uomCode, materialUom.uomCode, orderItem.material.id.toString());
				cargoPlanDateVo.netWeight = netweigth + orderItem.settlementUom.uomCode;
			}
		}
		if (StringUtils.isNotEmpty(orderItemExtMap.get("fcl"))) {
			String string = orderItemExtMap.get("fcl");
			System.out.println(string);
			String[] split = string.replace("|", "%%%").split("%%%");
			System.out.println(play.libs.Json.toJson(split));
			if(split.length>0){
				for (String string2 : split) {
					System.out.println(string2);
					if(StringUtils.isNotEmpty(string2))
					cargoPlanDateVo.containerFcl += Double.valueOf(string2);
				}
			}else{
				cargoPlanDateVo.containerFcl = 0;
			}
		} else {
			cargoPlanDateVo.containerFcl = 0;
		}
		cargoPlanDateVo.feet = orderItemExtMap.get("feet");
		cargoPlanDateVo.type = orderItemExtMap.get("type");
		Double TotalQtyFcl=new Double(0);
		if (StringUtils.isNotEmpty(orderItemExtMap.get("qtyPerFcl"))) {
			String string = orderItemExtMap.get("fcl");
			String string3 = orderItemExtMap.get("qtyPerFcl");
			String[] split2 = string3.replace("|", "%%%").split("%%%");
			String[] split = string.replace("|", "%%%").split("%%%");
			if(split.length==split2.length){
				for (int i=0;i<split.length;i++) {
					if(StringUtils.isNotEmpty(split[i])&&StringUtils.isNotEmpty(split2[i]))
					TotalQtyFcl=Double.valueOf(split[i])*Double.valueOf(split2[i]);
				}
			}
		}
		cargoPlanDateVo.ipbNumber = orderItemExtMap.get("ipbNumber");
		cargoPlanDateVo.columnOne = null;
		cargoPlanDateVo.columnTwo = null;
		cargoPlanDateVo.schedule = "Container:" + containerNo + "/" + (cargoPlanDateVo.containerFcl - containerNo) + "\r\n" + "Stuffing Qty-SKU("
				+ qty + "/" +( netweigth>0?(TotalQtyFcl / netweigth):TotalQtyFcl) + ")" + "\r\n" + "Stuffing Qty-KG("
				+ (qty * netweigth) + "/" + (TotalQtyFcl) + ")";
		cargoPlanDateVo.columnThr = null;
		cargoPlanDateVo.columnFor = null;
		if (StringUtils.isNotEmpty(orderItemExtMap.get("palletiZed"))) {
			if (Boolean.valueOf(orderItemExtMap.get("palletiZed")) == true) {
				cargoPlanDateVo.palletized = "Y";
			} else {
				cargoPlanDateVo.palletized = "N";
			}
		}
		if (StringUtils.isNotEmpty(orderItemExtMap.get("Out"))) {
			cargoPlanDateVo.out = DateUtil.dateToStringEn2(new Date(Long.valueOf(orderItemExtMap.get("Out"))));
		}
		if (StringUtils.isNotEmpty(orderItemExtMap.get("closed_date"))) {
			cargoPlanDateVo.closingDate = DateUtil.dateToStringEn2(new Date(Long.valueOf(orderItemExtMap.get("closed_date"))));
		}
		if (StringUtils.isNotEmpty(orderItemExtMap.get("closed_time"))) {
			cargoPlanDateVo.closingTime = DateUtil.dateToShortTime(new Date(Long.valueOf(orderItemExtMap.get("closed_time"))));
		}
		cargoPlanDateVo.remarks = orderItemExtMap.get("cargoRemarks");
		cargoPlanDateVo.transport = orderItemExtMap.get("transp");
		cargoPlanDateVo.destination = orderItemExtMap.get("destination");
		cargoPlanDateVo.vessel = orderItemExtMap.get("feeder_vessel");
		return cargoPlanDateVo;
	}

	public static List<CargoPlanDateVo> initTruck(OrderItem orderItem) {
		List<PlanItem> planItemList = PlanItem.find().where().eq("planType", "T003").eq("orderItem.id", orderItem.id).eq("deleted", false)
				.eq("order.warehouse.deleted", false).eq("order.warehouse.id", SessionSearchUtil.searchWarehouse().id).eq("order.deleted", false)
				.eq("orderItem.deleted", false).findList();
		List<CargoPlanDateVo> cargoPlanDateVoList = new ArrayList<CargoPlanDateVo>();
		CargoPlanDateVo Vo1 = new CargoPlanDateVo();
		int containerNo = 0;
		double qty = 0;
		List<String> stringList = new ArrayList<String>();
		for (PlanItem planItem : planItemList) {
			HashMap<String, String> ext = ExtUtil.unapply(planItem.ext);
			if (StringUtils.isNotEmpty(ext.get("containerNo"))) {
				if (!stringList.contains(ext.get("containerNo"))) {
					stringList.add(ext.get("containerNo"));
				}
			}
			qty = qty + planItem.palnnedQty.doubleValue();
		}
		containerNo = stringList.size();
		Vo1 = Vo1.setCargoPlanDateVo(orderItem, containerNo, qty);
		cargoPlanDateVoList.add(Vo1);
		CargoPlanDateVo Vo2 = new CargoPlanDateVo();
		Vo2.columnOne = "Truck";
		Vo2.columnTwo = "Stuffing Date";
		Vo2.schedule = "Container No./Seal No";
		Vo2.columnThr = "batch";
		Vo2.columnFor = "Qty";
		cargoPlanDateVoList.add(Vo2);
		if (planItemList.size() > 0) {
			planItemList = sortPlanItem(planItemList);
			for (int i = 0; i < planItemList.size(); i++) {
				CargoPlanDateVo cargoPlanDateVo = new CargoPlanDateVo();
				if (i == 0) {
					HashMap<String, String> planItemMap = ExtUtil.unapply(planItemList.get(i).ext);
					cargoPlanDateVo.columnOne = planItemMap.get("Truck");
					if (planItemList.get(i).plannedExecutionAt != null) {
						cargoPlanDateVo.columnTwo = DateUtil.dateToStringEn2(new Date(planItemList.get(i).plannedExecutionAt.getTime()));
					}
					if (StringUtils.isNotEmpty(planItemMap.get("containerNo")) && StringUtils.isNotEmpty(planItemMap.get("seal_No"))) {
						cargoPlanDateVo.schedule = planItemMap.get("containerNo") + " [" + planItemMap.get("seal_No") + "]";
					}
					cargoPlanDateVo.columnThr = planItemMap.get("lot");
					if (planItemList.get(i).palnnedQty != null) {
						cargoPlanDateVo.columnFor = setBigDecimalToString(planItemList.get(i).palnnedQty);
					}
					cargoPlanDateVoList.add(cargoPlanDateVo);
				} else if (i > 0) {
					HashMap<String, String> planItemMap1 = ExtUtil.unapply(planItemList.get(i - 1).ext);
					HashMap<String, String> planItemMap2 = ExtUtil.unapply(planItemList.get(i).ext);
					if (StringUtils.isNotEmpty(planItemMap1.get("Truck")) && StringUtils.isNotEmpty(planItemMap2.get("Truck"))
							&& planItemMap1.get("Truck").equals(planItemMap2.get("Truck"))) {
						if (StringUtils.isNotEmpty(planItemMap1.get("containerNo")) && StringUtils.isNotEmpty(planItemMap2.get("containerNo"))
								&& planItemMap1.get("containerNo").equals(planItemMap2.get("containerNo"))) {
							cargoPlanDateVo.columnThr = planItemMap2.get("lot");
							if (planItemList.get(i).palnnedQty != null) {
								cargoPlanDateVo.columnFor = setBigDecimalToString(planItemList.get(i).palnnedQty);
							}
							cargoPlanDateVoList.add(cargoPlanDateVo);
						} else {
							if (StringUtils.isNotEmpty(planItemMap2.get("containerNo")) && StringUtils.isNotEmpty(planItemMap2.get("seal_No"))) {
								cargoPlanDateVo.schedule = planItemMap2.get("containerNo") + " [" + planItemMap2.get("seal_No") + "]";
							}
							cargoPlanDateVo.columnThr = planItemMap2.get("lot");
							if (planItemList.get(i).palnnedQty != null) {
								cargoPlanDateVo.columnFor = setBigDecimalToString(planItemList.get(i).palnnedQty);
							}
							cargoPlanDateVoList.add(cargoPlanDateVo);
						}
					} else {
						cargoPlanDateVo.columnOne = planItemMap2.get("Truck");
						if (planItemList.get(i).plannedExecutionAt != null) {
							cargoPlanDateVo.columnTwo = DateUtil.dateToStringEn2(new Date(planItemList.get(i).plannedExecutionAt.getTime()));
						}
						if (StringUtils.isNotEmpty(planItemMap2.get("containerNo")) && StringUtils.isNotEmpty(planItemMap2.get("seal_No"))) {
							cargoPlanDateVo.schedule = planItemMap2.get("containerNo") + " [" + planItemMap2.get("seal_No") + "]";
						}
						cargoPlanDateVo.columnThr = planItemMap2.get("lot");
						if (planItemList.get(i).palnnedQty != null) {
							cargoPlanDateVo.columnFor = setBigDecimalToString(planItemList.get(i).palnnedQty);
						}
						cargoPlanDateVoList.add(cargoPlanDateVo);
					}
				}
			}
		}
		return cargoPlanDateVoList;
	}

	/**
	 * 对PlanItem按truck和containerNo进行排序
	 * @param planItemList
	 * @return
	 */
	public static List<PlanItem> sortPlanItem(List<PlanItem> planItemList) {
		if (planItemList.size() > 1) {
			Collections.sort(planItemList, new Comparator<PlanItem>() {
				@Override
				public int compare(PlanItem o1, PlanItem o2) {
					HashMap<String, String> extMap1 = ExtUtil.unapply(o1.ext);
					HashMap<String, String> extMap2 = ExtUtil.unapply(o2.ext);
					if (StringUtils.isNotEmpty(extMap1.get("Truck")) && StringUtils.isNotEmpty(extMap2.get("Truck"))) {
						if ((extMap1.get("Truck")).compareTo(extMap2.get("Truck")) == 0) {
							if ((extMap1.get("containerNo")).compareTo(extMap2.get("containerNo")) == 0) {
								return 0;
							} else if ((extMap1.get("containerNo")).compareTo(extMap2.get("containerNo")) > 0) {
								return 1;
							} else {
								return -1;
							}
						} else if ((extMap1.get("Truck")).compareTo(extMap2.get("Truck")) > 0) {
							return 1;
						} else {
							return -1;
						}
					} else {
						return -1;
					}
				}
			});
		}
		return planItemList;
	}

	public static String setBigDecimalToString(BigDecimal qty) {
		double amount = (double) (Math.floor(qty.doubleValue() * 100 + 0.5) / 100);
		return String.valueOf(amount);
	}

	public String getPiNo() {
		return piNo;
	}

	public void setPiNo(String piNo) {
		this.piNo = piNo;
	}

	public String getSplitShipment() {
		return splitShipment;
	}

	public void setSplitShipment(String splitShipment) {
		this.splitShipment = splitShipment;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNetWeight() {
		return netWeight;
	}

	public void setNetWeight(String netWeight) {
		this.netWeight = netWeight;
	}

	public int getContainerFcl() {
		return containerFcl;
	}

	public void setContainerFcl(int containerFcl) {
		this.containerFcl = containerFcl;
	}

	public String getFeet() {
		return feet;
	}

	public void setFeet(String feet) {
		this.feet = feet;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double getQtyFcl() {
		return qtyFcl;
	}

	public void setQtyFcl(double qtyFcl) {
		this.qtyFcl = qtyFcl;
	}

	public String getIpbNumber() {
		return ipbNumber;
	}

	public void setIpbNumber(String ipbNumber) {
		this.ipbNumber = ipbNumber;
	}

	public String getTruck() {
		return truck;
	}

	public void setTruck(String truck) {
		this.truck = truck;
	}

	public String getContainerNo() {
		return containerNo;
	}

	public void setContainerNo(String containerNo) {
		this.containerNo = containerNo;
	}

	public String getBatch() {
		return batch;
	}

	public void setBatch(String batch) {
		this.batch = batch;
	}

	public double getQty() {
		return qty;
	}

	public void setQty(double qty) {
		this.qty = qty;
	}

	public Date getStuffingDate() {
		return stuffingDate;
	}

	public void setStuffingDate(Date stuffingDate) {
		this.stuffingDate = stuffingDate;
	}

	public String getPalletized() {
		return palletized;
	}

	public void setPalletized(String palletized) {
		this.palletized = palletized;
	}

	public String getOut() {
		return out;
	}

	public void setOut(String out) {
		this.out = out;
	}

	public String getClosingDate() {
		return closingDate;
	}

	public void setClosingDate(String closingDate) {
		this.closingDate = closingDate;
	}

	public String getClosingTime() {
		return closingTime;
	}

	public void setClosingTime(String closingTime) {
		this.closingTime = closingTime;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getTransport() {
		return transport;
	}

	public void setTransport(String transport) {
		this.transport = transport;
	}

	public String getVessel() {
		return vessel;
	}

	public void setVessel(String vessel) {
		this.vessel = vessel;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getFumigation() {
		return fumigation;
	}

	public void setFumigation(String fumigation) {
		this.fumigation = fumigation;
	}

	public String getColumnOne() {
		return columnOne;
	}

	public void setColumnOne(String columnOne) {
		this.columnOne = columnOne;
	}

	public String getColumnTwo() {
		return columnTwo;
	}

	public void setColumnTwo(String columnTwo) {
		this.columnTwo = columnTwo;
	}

	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public String getColumnThr() {
		return columnThr;
	}

	public void setColumnThr(String columnThr) {
		this.columnThr = columnThr;
	}

	public String getColumnFor() {
		return columnFor;
	}

	public void setColumnFor(String columnFor) {
		this.columnFor = columnFor;
	}

}
