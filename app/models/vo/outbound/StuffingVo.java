package models.vo.outbound;

import java.util.HashMap;

import models.PlanItem;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.ExtUtil;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StuffingVo {
	public String batch;
	public double Qty;
	public String id;
	public void inPlanItem(PlanItem planItem){
		if(planItem!=null){
		id=planItem.id.toString();
		if(planItem.palnnedQty!=null)
		Qty=planItem.palnnedQty.doubleValue();
		HashMap<String,String> Ext = ExtUtil.unapply(planItem.ext);
		batch=Ext.get("lot");
		}
	}
}
