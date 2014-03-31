package models.vo.outbound;

import java.util.HashMap;
import java.util.List;

import models.PlanItem;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.ExtUtil;
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainerVo {
	public String container;
	public String SealNo;
	public HashMap<String,StuffingVo> batchs=new HashMap<String,StuffingVo>();
	public void inPlanItem(PlanItem planItem){
		if(planItem!=null){
		HashMap<String,String> Ext = ExtUtil.unapply(planItem.ext);
		container = Ext.get("containerNo");
		SealNo=Ext.get("seal_No");
		StuffingVo stuffingVo = new StuffingVo();
		stuffingVo.inPlanItem(planItem);
		batchs.put(stuffingVo.batch,stuffingVo);
		}
	}
	public void inContainer(ContainerVo containerVo){
		for(String batch:containerVo.batchs.keySet()){
			if(this.batchs.containsKey(batch)){
				this.batchs.get(batch).Qty+=containerVo.batchs.get(batch).Qty;
			}else{
				this.batchs.put(batch,containerVo.batchs.get(batch));
			}
		}
	}
}
