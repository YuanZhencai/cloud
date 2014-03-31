package models.vo.outbound;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.DateUtil;
import utils.ExtUtil;
import models.MaterialUom;
import models.Order;
import models.OrderItem;
import models.Plan;
import models.PlanItem;
import models.vo.inbound.SKUUOMVo;
import models.vo.inbound.UOMVo;
@JsonIgnoreProperties(ignoreUnknown = true)
public class cargoPlanSplitVo {
	public String id;
	public String orderId;
	public String orderItemId;
	public Date stuffing_Date;
	public String ipb_No;
	public String containerNo;
	public String seal_No;
	public String batch_No;
	public double stuffing_Quantity;
	public UOMVo stuffing_UOM;
	public double pi_SKU;
	public SKUUOMVo sku_UOM;
	public boolean show=true;
	public boolean haslive=true;
	public boolean Canedit=true;
	public void inOrder(Order order){
		orderId=order.id.toString();
		//container_No=order.contractNo;
	}
	public Order outOrder(Order order){
		id=order.id.toString();
		//order.contractNo=container_No;
		return order;
	}
	public void inOrderItem(OrderItem orderItem){
		orderItemId=orderItem.id.toString();
	}
	public OrderItem outOrderItem(OrderItem orderItem){
		return orderItem;
	}
	public void inPlan(Plan plan){
		
	}
	public Plan outPlan(Plan plan){
		return plan;
	}
	public void setExt(String ext){
		HashMap<String, String> Ext = ExtUtil.unapply(ext);
		ipb_No=Ext.get("ipb_No");
		seal_No=Ext.get("seal_No");
		containerNo=Ext.get("containerNo");
		batch_No= Ext.get("lot");
	}
	public String returnExt(HashMap<String, String> Ext){
		Ext.put("ipb_No", ipb_No!=null?ipb_No:"");
		Order order = Order.find().where().eq("deleted", false).eq("id", orderId).findUnique();
		if(order!=null)
		Ext.put("pi", order.internalOrderNo);
		Ext.put("lot", batch_No!=null?batch_No:"");
		Ext.put("seal_No",seal_No!=null?seal_No:"");
		Ext.put("containerNo", containerNo!=null?containerNo:"");
		return ExtUtil.apply(Ext);
	}
	public void inPlanItem(PlanItem planItem){
			if(!planItem.plan.planStatus.equals("S000"))
			Canedit=false;
			id=planItem.id.toString();
			if(planItem.palnnedQty!=null)
			pi_SKU=planItem.palnnedQty.intValue();
			if(planItem.plannedExecutionAt!=null){
			long TempDate=planItem.plannedExecutionAt.getTime();
			stuffing_Date=new Date(TempDate);
			}
			System.out.println(stuffing_Date+":::::::::::::::::::::::::::::");
			/*batchVo batchVo = new batchVo();
			if(planItem.batch!=null){
			batchVo.inBatch(planItem.batch);
			batch_No=batchVo;
			}*/
			if(planItem.ext!=null)
			setExt(planItem.ext);
			UOMVo uomVo = new UOMVo();
			if(planItem.fromMaterialUom!=null)
			uomVo.inMaterialUom(planItem.fromMaterialUom);
			stuffing_UOM=uomVo;
			SKUUOMVo skuuomVo = new SKUUOMVo();
			if(planItem.toMaterialUom!=null)
			skuuomVo.inMaterialUom(planItem.toMaterialUom);
			sku_UOM=skuuomVo;
	} 
	public PlanItem outPlanItem(PlanItem planItem){
		planItem.palnnedQty=new BigDecimal(pi_SKU);
		if(stuffing_UOM!=null){
		MaterialUom frommaterialUom = MaterialUom.find().where().eq("id",stuffing_UOM.id).findUnique();
		planItem.materialUom=frommaterialUom;
		planItem.fromMaterialUom=frommaterialUom;
		}
		if(sku_UOM!=null){
		MaterialUom tomaterialUom = MaterialUom.find().where().eq("id",sku_UOM.id).findUnique();
		planItem.toMaterialUom=tomaterialUom;
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try {
			if(stuffing_Date!=null)
				planItem.plannedExecutionAt=new Timestamp(stuffing_Date.getTime());
			else
				planItem.plannedExecutionAt=null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//planItem.
		planItem.ext=returnExt(new HashMap<String,String>());
		/*if(batch_No!=null){
		Batch batch = Batch.find().byId(batch_No.id);
		planItem.batch=batch;
		}*/
		
		return planItem;
	}

}
