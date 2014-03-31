package models.vo.outbound;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import models.PlanItem;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.ExtUtil;
@JsonIgnoreProperties(ignoreUnknown = true)
public class TruckVo {
	public String orderId;
	public String Truck;
	public Date StuffingDate;
	public boolean show=true;
	public boolean haslive=true;
	public boolean Canedit=true;
	public HashMap<String,ContainerVo> containers=new HashMap<String,ContainerVo>();
	public void inPlanItem(PlanItem planItem){
		if(planItem!=null&&planItem.orderItem!=null&&planItem.orderItem.order!=null)
		orderId=planItem.orderItem.order.id.toString();
		if(!planItem.plan.planStatus.equals("S000"))
			Canedit=false;
		if(planItem!=null){
		HashMap<String, String> Ext = ExtUtil.unapply(planItem.ext);
		String temp= Ext.get("Truck");
		if(temp!=null&&!temp.equals(""))
			this.Truck=temp;
		else
			this.Truck=String.valueOf("Defaul");
		if(planItem.plannedExecutionAt!=null)
		StuffingDate=new Date(planItem.plannedExecutionAt.getTime());
		ContainerVo containerVo = new ContainerVo();
		containerVo.inPlanItem(planItem);
		containers.put(containerVo.container,containerVo);
		}
	}
	public void addTruck(TruckVo truckVo){
		for(String container:truckVo.containers.keySet()){
			if(this.containers.containsKey(container)){
				this.containers.get(container).inContainer(truckVo.containers.get(container));
			}else{
				this.containers.put(container, truckVo.containers.get(container));
			}
		}
	}
}	
