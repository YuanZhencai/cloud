package models.vo.outbound;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.DateUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;

import models.MaterialUom;
import models.Order;
import models.OrderItem;
import models.Plan;
import models.PlanItem;
import models.vo.inbound.UOMVo;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TempStuffingVo {
	static String orderTypeOfPi = "T001";
	static String orderTypeOfCargo = "T004";
	static String orderSourceTypeOfCargo = "T003";
	static String orderSuperTypeOfCargo = "T003.999";
	static String NEW = "S000";
	static String CONFIRMED = "S001";
	static String EXECUTE = "S002";
	static String COMPLETE = "S003";
	static String CANCEL = "S999";
	public TempContainerVo ContainVo;
	public String batch;
	public double Qty;
	public String id;

	public void initVo(String batch, double Qty, String id) {
		this.batch = batch;
		this.Qty = Qty;
		this.id = id;
	}

	public PlanItem outPlanItem(PlanItem planItem) {
		planItem.palnnedQty = new BigDecimal(this.Qty);
		if(this.ContainVo.TruckVo.StuffingDate!=null)
		planItem.plannedExecutionAt = new Timestamp(
				this.ContainVo.TruckVo.StuffingDate.getTime());
		planItem.ext = this.setExt(planItem.ext);
		planItem = this.setMaterial(planItem);
		return planItem;
	}

	public String setExt(String Ext) {
		HashMap<String, String> map=null;
		if(Ext==null){
			map=new HashMap<String,String>();
		}else{
			map = ExtUtil.unapply(Ext);
		}
		map.put("lot", batch != null ? batch : "");
		map.put("containerNo",
				ContainVo.container != null ? ContainVo.container : "");
		map.put("seal_No", ContainVo.SealNo != null ? ContainVo.SealNo : "");
		map.put("Truck", String.valueOf(ContainVo.TruckVo.Truck));
		return ExtUtil.apply(map);
	}

	public PlanItem setMaterial(PlanItem planItem) {
		List<OrderItem> orderItems = OrderItem.find().fetch("settlementUom")
				.where().eq("order.id", this.ContainVo.TruckVo.orderId)
				.eq("deleted", false).eq("order.deleted", false).findList();
		if (orderItems.size() > 0) {
			OrderItem orderItem = orderItems.get(0);
			planItem.material = orderItem.material;
			// ArrayList<DaseData> daseDate = new ArrayList<DaseData>();
			planItem.fromMaterialUom = orderItem.settlementUom;
			HashMap<String, String> Ext = ExtUtil.unapply(orderItem.ext);
			String uomcode = Ext.get("qtyPerPalletUom");
			System.out.println(uomcode);
			if (uomcode != null) {
				List<MaterialUom> materialUoms = MaterialUom.find().where()
						.eq("material", planItem.material).eq("deleted", false)
						.eq("id", uomcode).findList();
				if (materialUoms.size() > 0) {
					planItem.toMaterialUom = materialUoms.get(0);
				}
			}
		}
		return planItem;
	}
}
