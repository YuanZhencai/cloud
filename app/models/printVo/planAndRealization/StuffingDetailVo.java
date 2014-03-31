package models.printVo.planAndRealization;

import java.util.Date;
import java.util.HashMap;

import models.OrderItem;
import models.PlanItem;
import models.vo.outbound.cargoPlanSplitVo;
import models.vo.outbound.cargoPlanVo;
import utils.ExtUtil;
import utils.UnitConversion;
import action.Cell;

public class StuffingDetailVo {
	public String IPBNUMBER;
	public String PI;
	public String Referency;
	public String Commodity;
	public double Kg;
	public String QtyperFcl;
	public String ContainerNumber;
	public String SealNumber;
	public String Fcl;
	public double StuffingQuantity;
	public String Destination;
	public String Vessel;
	public String BatchNo;
	public Date StuffingDate;
	public Date Out;
	public Date Closing;
	public String Transport;
	public String Remaks;
	
	public void inCargoPlanVo(cargoPlanVo cargoPlanVo){
		Remaks=cargoPlanVo.remarks;
		IPBNUMBER=cargoPlanVo.IPBnumber;
		Out=cargoPlanVo.Out;
		PI=cargoPlanVo.piNo;
		Referency=cargoPlanVo.refNo;
		//if(cargoPlanVo.fcl!=null&&!cargoPlanVo.fcl.equals(""))
		Fcl=cargoPlanVo.fcl;
		Transport=cargoPlanVo.transp;
		Vessel=cargoPlanVo.vessel;
		Destination=cargoPlanVo.getDest();
		System.out.println(cargoPlanVo.closedDate);
		if(cargoPlanVo.closedDate!=null&&cargoPlanVo.closedTime!=null){
		   //Closing=DateUtil.strToDate(DateUtil.dateToStrShort(cargoPlanVo.closedDate)+" "+DateUtil.dateToTime(DateUtil.dateToDateTime(cargoPlanVo.closedTime)), "yyyy-MM-dd HH:mm:ss");
		Closing=new Date(cargoPlanVo.closedDate.getTime()+cargoPlanVo.closedTime.getTime());
		System.out.println("Date================================"+Closing);
		}
	}
	public void inCargeplanSplitVo(cargoPlanSplitVo cargoPlanSplitVo){
		//BatchNo=cargoPlanSplitVo.batch_No;
		//IPBNUMBER=cargoPlanSplitVo.ipb_No;
		ContainerNumber=cargoPlanSplitVo.containerNo;
		SealNumber=cargoPlanSplitVo.seal_No;
		if(cargoPlanSplitVo.stuffing_Date!=null)
		StuffingDate=cargoPlanSplitVo.stuffing_Date;
		StuffingQuantity=cargoPlanSplitVo.stuffing_Quantity;
		BatchNo=cargoPlanSplitVo.batch_No;
	//	kg=UnitConversion.returnComparing(cargoPlanSplitVo.sku_UOM.uomCode,cargoPlanSplitVo.stuffing_UOM.uomCode,cargoPlanSplitVo.material.id.toString());
	}
	public void inPlanItem(PlanItem planItem){
	    cargoPlanVo cargoPlanVo = new cargoPlanVo();
        cargoPlanVo.inOrder(planItem.order);
        System.out.println(planItem.orderItem+"++++++========orderItem=========+++++++++++++++++++");
        cargoPlanVo.inOrderItem(planItem.orderItem);
        //cargoPlanVo.setExt(planItem.orderItem.ext);
        System.out.println(cargoPlanVo.refNo+"===========================");
        cargoPlanSplitVo cargoPlanSplitVo = new cargoPlanSplitVo();
        cargoPlanSplitVo.inPlanItem(planItem);
        inCargoPlanVo(cargoPlanVo);
        inCargeplanSplitVo(cargoPlanSplitVo);
        inOrderItem(planItem.orderItem);
        StuffingQuantity=cargoPlanSplitVo.pi_SKU*UnitConversion.returnComparing(cargoPlanSplitVo.stuffing_UOM.uomCode, cargoPlanSplitVo.sku_UOM.uomCode,planItem.material.id.toString());
        Kg=UnitConversion.returnComparing(cargoPlanSplitVo.stuffing_UOM.uomCode, cargoPlanSplitVo.sku_UOM.uomCode,planItem.material.id.toString());
        System.out.println(StuffingQuantity);
        if(planItem!=null&&planItem.orderItem!=null)
            QtyperFcl=ExtUtil.unapply(planItem.orderItem.ext).get("qtyPerFcl");
	}
	public void inOrderItem(OrderItem orderItem){
	    Commodity=orderItem.material.materialName;
	    HashMap<String, String> EXT = ExtUtil.unapply(orderItem.ext);
	    Destination=EXT.get("destination");
	    //SealNumber=EXT.get("");
	   // Remaks=EXT.get("remarking");
	   // System.out.println(orderItem.remarks+"============             =============  f");
	}
}
