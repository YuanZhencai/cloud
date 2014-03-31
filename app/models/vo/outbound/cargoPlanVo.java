package models.vo.outbound;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonIgnoreType;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonValue;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonFilter;
import org.codehaus.jackson.map.annotate.JsonValueInstantiator;

import action.Cell;

import models.Batch;
import models.MaterialUom;
import models.Order;
import models.OrderItem;
import models.PlanItem;
import models.Stock;

import scala.Array;
import utils.BatchSearchUtil;
import utils.CodeUtil;
import utils.DateUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import utils.UnitConversion;
import utils.Excel.ExcelObj;
@JsonIgnoreProperties(ignoreUnknown = true)
public class cargoPlanVo extends ExcelObj{
	/**
	 * 
	 */
	public String piNo;
	public String splitShipment;
	public String loc;
	public String refNo;
	public String fcl;
	public String feet;
	public String thistype;
	public String dest;
	public String agent;
	public String vessel;
	public Date closedDate;
	public Date closedTime;
	public String transp;
	@Cell(Type="deleted",name="batchVos")
	public HashMap<String,batchVo> batchVos=new HashMap<String,batchVo>();
	public String remarks;
	@Cell(Type="deleted",name="Skuuom")
	public String Skuuom;
	@Cell(Type="deleted",name="id")
	public String id;
	@Cell(Type="deleted",name="planEdit")
	public String planEdit;
	@Cell(Type="deleted",name="Commodity")
	public String Commodity;
	@Cell(Type="deleted",name="netWeight")
	public double netWeight;
	@Cell(Type="deleted",name="netWeightUOM")
	public String netWeightUOM;
	@Cell(Type="deleted",name="qtyPerFcl")
	public String qtyPerFcl;
	@Cell(Type="deleted",name="IPBnumber")
	public String IPBnumber;
	@Cell(Type="deleted",name="containerNo")
	public double containerNo;
	@Cell(Type="deleted",name="Trucks")
	public HashMap<String,TruckVo> Trucks=new HashMap<String,TruckVo>();
	@Cell(Type="deleted",name="palletiZed")
	public boolean palletiZed;
	@Cell(Type="deleted",name="Out")
	public Date Out;
	@Cell(Type="deleted",name="status")
	public String status;
	@Cell(Type="deleted",name="CreateTime")
	public Date createTime;
	@Cell(Type="deleted",name="updateTime")
	public Date updateTime;
	@Cell(Type="deleted",name="fumigation")
	public boolean fumigation;
	
	public String getSplitShipment() {
		return splitShipment;
	}
	public void setSplitShipment(String splitShipment) {
		this.splitShipment = splitShipment;
	}
	public String getPiNo() {
		return piNo;
	}
	public void setPiNo(String piNo) {
		this.piNo = piNo;
	}
	public String getLoc() {
		return loc;
	}
	public void setLoc(String loc) {
		this.loc = loc;
	}
	public String getFeet() {
		return feet;
	}
	public void setFeet(String feet) {
		this.feet = feet;
	}
	public Date getClosedDate() {
		return closedDate;
	}
	public void setClosedDate(Date closedDate) {
		this.closedDate = closedDate;
	}
	public String getRefNo() {
		return refNo;
	}
	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}
	public String getFcl() {
		return fcl;
	}
	@JsonProperty("fcl")
	public void setJsonFcl(String fcl){
		this.fcl=fcl;
	}
	public void setFcl(String fcl) {
		this.fcl = fcl;
	}
	public String getThistype() {
		return thistype;
	}
	public void setThistype(String thistype) {
		this.thistype = thistype;
	}
	public Date getClosedTime() {
		return closedTime;
	}
    @JsonProperty("closedTime")
    public void setStringTime(String closedTime){
    	if(closedTime!=null&&!closedTime.equals("NaN:NaN"))
        this.closedTime=DateUtil.strToDate(closedTime, "HH:mm");
    }
	public void setClosedTime(Date closedTime) {
		String dateToShortTime = DateUtil.dateToShortTime(closedTime);
		this.setStringTime(dateToShortTime);
	}
	public String getAgent() {
		return agent;
	}
	public void setAgent(String agent) {
		this.agent = agent;
	}
	
	public String getVessel() {
		return vessel;
	}
	public void setVessel(String vessel) {
		this.vessel = vessel;
	}
	public String getTransp() {
		return transp;
	}
	public void setTransp(String transp) {
		this.transp = transp;
	}
	public String getDest() {
		return dest;
	}
	public void setDest(String dest) {
		this.dest = dest;
	}
	
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public void setExt(String ext){
		HashMap<String, String> Ext = ExtUtil.unapply(ext);
		loc=Ext.get("loc");
		splitShipment=Ext.get("splitShipment");
		remarks=Ext.get("cargoRemarks");
		if(Ext.get("palletiZed")!=null&&!Ext.get("palletiZed").equals(""))
			palletiZed=Boolean.valueOf(Ext.get("palletiZed"));
		IPBnumber=Ext.get("ipbNumber");
		if(Ext.get("qtyPerFcl")!=null&&!Ext.get("qtyPerFcl").equals(""))
		qtyPerFcl=Ext.get("qtyPerFcl");
		feet=Ext.get("feet");
		if(Ext.get("closed_date")!=null&&!Ext.get("closed_date").equals(""))
		closedDate=new Date(Long.valueOf(Ext.get("closed_date")));
		if(Ext.get("Out")!=null&&!Ext.get("Out").equals(""))
			Out=new Date(Long.valueOf(Ext.get("Out")));
		fcl=Ext.get("fcl");
		thistype=Ext.get("type");
		if(Ext.get("closed_time")!=null&&!Ext.get("closed_time").equals(""))
		closedTime=new Date(Long.valueOf(Ext.get("closed_time")));
		agent=Ext.get("agent");
		vessel=Ext.get("feeder_vessel");
		transp=Ext.get("transp");
		dest=Ext.get("destination");
		netWeight=Ext.get("netWeight")!=null&&!Ext.get("netWeight").equals("")?Double.valueOf(Ext.get("netWeight")):0;
		Skuuom = Ext.get("qtyPerPalletUom");
		if(StringUtils.isNotEmpty(Ext.get("fumigation")))
			this.fumigation=Boolean.valueOf(Ext.get("fumigation"));
		//System.out.println(Skuuom);
	}
	public String returnExt(HashMap<String, String> Ext){
		Ext.put("loc", loc!=null?loc:"");
		Ext.put("Out",Out!=null?String.valueOf(Out.getTime()):"");
		Ext.put("palletiZed",String.valueOf(palletiZed));
		Ext.put("ipbNumber",IPBnumber!=null?IPBnumber:"");
		Ext.put("cargoRemarks", remarks!=null?remarks:"");
		Ext.put("splitShipment", splitShipment!=null?splitShipment:"");
		Ext.put("feet",feet !=null?feet:"");
		Ext.put("closed_date", closedDate!=null?String.valueOf(closedDate.getTime()):"");
		Ext.put("fcl", String.valueOf(fcl));
		Ext.put("qtyPerFcl",String.valueOf(qtyPerFcl));
		Ext.put("type",thistype!=null?thistype:"" );
		Ext.put("closed_time", closedTime!=null?String.valueOf(closedTime.getTime()):"");
		Ext.put("agent", agent!=null?agent:"");
		Ext.put("feeder_vessel",vessel!=null?vessel:"" );
		Ext.put("transp", transp!=null?transp:"");
		Ext.put("destination",dest!=null?dest:"");
		Ext.put("fumigation", String.valueOf(fumigation));
		return ExtUtil.apply(Ext);
	}
	public String UploadreturnExt(HashMap<String, String> Ext){
		if(loc!=null&&!loc.equals(""))
		Ext.put("loc", loc);
		if(Out!=null&&!Out.equals(""))
		Ext.put("Out",String.valueOf(Out.getTime()));
		Ext.put("palletiZed",String.valueOf(palletiZed));
		if(IPBnumber!=null&&!IPBnumber.equals(""))
		Ext.put("ipbNumber",IPBnumber);
		if(remarks!=null&&!remarks.equals(""))
		Ext.put("cargoRemarks", remarks);
		if(splitShipment!=null&&!splitShipment.equals(""))
		Ext.put("splitShipment", splitShipment);
		if(feet!=null&&!feet.equals(""))
		Ext.put("feet",feet);
		if(closedDate!=null&&!closedDate.equals(""))
		Ext.put("closed_date", String.valueOf(closedDate.getTime()));
		Ext.put("fcl", String.valueOf(fcl));
		Ext.put("qtyPerFcl",String.valueOf(qtyPerFcl));
		if(thistype!=null&&!thistype.equals(""))
		Ext.put("type",thistype );
		if(closedTime!=null&&!closedTime.equals(""))
		Ext.put("closed_time", String.valueOf(closedTime.getTime()));
		if(agent!=null&&!agent.equals(""))
		Ext.put("agent", agent);
		if(vessel!=null&&!vessel.equals(""))
		Ext.put("feeder_vessel",vessel);
		if(transp!=null&&!transp.equals(""))
		Ext.put("transp", transp);
		if(dest!=null&&!dest.equals(""))
		Ext.put("destination",dest);
		return ExtUtil.apply(Ext);
	}
	public void inOrder(Order order){
		//if(!order.orderStatus.equals("S000"))
		if(order!=null){
		planEdit=order.orderStatus;
		//System.out.println(planEdit);
		id=order.id.toString();
		piNo=order.internalOrderNo;
		refNo=order.contractNo;
		this.status=CodeUtil.getOrderStatus(order.orderStatus);
		}
	}
	public Order outOrder(Order order){
		order.externalOrderNo=piNo;
		order.internalOrderNo=piNo;
		order.contractNo=refNo;
		return order;
	}
	public void inOrderItem(OrderItem orderItem){
		if(orderItem.settlementUom!=null)
			this.netWeightUOM=orderItem.settlementUom.uomCode;
		//System.out.println(this.netWeightUOM);
		this.setExt(orderItem.ext);
		if(orderItem!=null&&orderItem.material!=null){
			this.Commodity=orderItem.material.materialName;
			MaterialUom materialUom =null;
			if(Skuuom!=null)
				materialUom = MaterialUom.find().byId(Skuuom);
			if(materialUom!=null)
			this.netWeight=UnitConversion.returnComparing(this.netWeightUOM, materialUom.uomCode, orderItem.material.id.toString());
			}
		this.createTime=new Date(orderItem.createdAt.getTime());
		if(orderItem.updatedAt!=null)
		this.updateTime=new Date(orderItem.updatedAt.getTime());
	}
	public OrderItem outUploadOrderItem(OrderItem orderItem,OrderItem orderItemOld){
		if(orderItemOld==null){
			orderItem.ext=UploadreturnExt(new HashMap<String,String>());
		}else
			orderItem.ext=UploadreturnExt(ExtUtil.unapply(orderItemOld.ext));
		return orderItem;
	}
	public OrderItem outOrderItem(OrderItem orderItem,OrderItem orderItemOld){
		if(orderItemOld==null){
			orderItem.ext=returnExt(new HashMap<String,String>());
		}else
			orderItem.ext=returnExt(ExtUtil.unapply(orderItemOld.ext));
		return orderItem;
	}
	public void inPlanItem(List<PlanItem> planItems){
		this.containerNo=planItems.size();
		if(planItems.size()>0){
			for (PlanItem planItem : planItems) {
				TruckVo truckVo = new TruckVo();
				truckVo.inPlanItem(planItem);
				if(Trucks.containsKey(String.valueOf(truckVo.Truck))){
					Trucks.get(String.valueOf(truckVo.Truck)).addTruck(truckVo);
				}else{
					Trucks.put(String.valueOf(truckVo.Truck), truckVo);
				}
			}
		}
	}
	public void getbatchs(String id){
		//System.out.println("getPI batchs");
		Order order = Order.find().where().eq("deleted", false).eq("id", id)
				.eq("warehouse.id", SessionSearchUtil.searchWarehouse().id)
			.findUnique();
			
		String pi = order.internalOrderNo;
		List<PlanItem> planItems = PlanItem
				.find()
				.where()
				.eq("orderItem.order.internalOrderNo", pi)
				.eq("deleted", false)
				.eq("orderItem.order.deleted", false)
				.eq("orderItem.order.warehouse.deleted", false)
				.eq("planType", "T003")
				.eq("orderItem.order.warehouse.id",
						SessionSearchUtil.searchWarehouse().id.toString())
				.findList();
		List<Batch> batchs = BatchSearchUtil.getBatch(pi);
		for (Batch batch : batchs) {
			batchVo batchVo = null;
			boolean add=true;
			if(batchVos.containsKey(batch.batchNo)){
				batchVo=batchVos.get(batch.batchNo);
				add=false;
			}else{
			 batchVo = new batchVo();
			 batchVo.inBatch(batch);
			}
			List<Stock> stocks = Stock.find().where().eq("deleted", false)
					.eq("warehouse.id", SessionSearchUtil.searchWarehouse().id)
					.eq("batch.id", batch.id).findList();
			for (Stock stock : stocks) {
				batchVo.SumQty = batchVo.SumQty + stock.qty.doubleValue();
			}
			if(add){
				for (PlanItem planItem : planItems) {
					String lotNo = ExtUtil.unapply(planItem.ext).get("lot");
					if (lotNo != null && lotNo.equals(batchVo.batchNo)) {
						batchVo.reserveQty += planItem.palnnedQty.doubleValue();
					}
				}
			}
			if(StringUtils.isNotEmpty(batchVo.batchNo))
			batchVos.put(batchVo.batchNo,batchVo);
		}
	}
}
