package models.vo.outbound;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import utils.DateUtil;
import utils.ExtUtil;

import models.PlanItem;

public class ContainerTranforVo {
	static String orderTypeOfCargo = "T004";
	public Map<String, Object> map;
	public BigDecimal qty;
	public String Key;
	public String batchNo;
	public Date StuffingDate;
	public ContainerTranforVo(){
		
	}
	public ContainerTranforVo(PlanItem planItem){
		this.qty=planItem.palnnedQty;
		this.StuffingDate=DateUtil.timestampToDate(planItem.plannedExecutionAt);
		map = new HashMap<String, Object>();
		map.put("pi", planItem.orderItem.order.internalOrderNo);
		List<String> batchs = new ArrayList<String>();
		batchNo=ExtUtil.unapply(planItem.ext).get("lot");
		batchs.add(batchNo);
		map.put("lot", batchs);
		map.put("orderType", orderTypeOfCargo);
		map.put("planItemId", planItem.id.toString());
		HashMap<String, String> PlanItemExt = ExtUtil.unapply(planItem.ext);
		this.Key=PlanItemExt.get("containerNo");
		map.put("containerNo",PlanItemExt.get("containerNo"));
		HashMap<String, String> orderItemExt = ExtUtil.unapply(planItem.orderItem.ext);
		map.put("palletized", orderItemExt.get("palletiZed")==null?"false":orderItemExt.get("palletiZed"));
		Long CloseTime;
		if(StringUtils.isNotEmpty(orderItemExt.get("closed_date")))
			CloseTime=Long.valueOf(orderItemExt.get("closed_date"));
		else
			CloseTime=(long) 0;
		if(StringUtils.isNotEmpty(orderItemExt.get("closed_time")))
			CloseTime+=Long.valueOf(orderItemExt.get("closed_time"));
		else
			CloseTime+=(long) 0;
		map.put("closeDateTime",CloseTime>0?DateUtil.dateToStrLong(new Date(CloseTime)):"");
		map.put("remaks", orderItemExt.get("cargoRemarks")==null?"":orderItemExt.get("cargoRemarks"));
	}
	public boolean isAdd(ContainerTranforVo containerTranforVo){
		if(this.Key.equals(containerTranforVo.Key)){
			this.qty=this.qty.add(containerTranforVo.qty);
			System.out.println(qty);
			if(this.map.get("lot") instanceof List){
				List<String> object = (List<String>) map.get("lot");
				if(!object.contains(containerTranforVo.batchNo)){
					object.add(containerTranforVo.batchNo);
				}
			 this.map.put("lot", object);
			}else{
				List<String> batchs = new ArrayList<String>();
				batchs.add(containerTranforVo.batchNo);
				map.put("lot", batchs);
			}
			return true;
		}else{
			return false;
		}
	}
}
