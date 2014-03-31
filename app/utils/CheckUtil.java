package utils;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import models.OrderItem;
import models.PlanItem;

public class CheckUtil {
	public static boolean TransfCanCreateCheck(PlanItem planItem) {
		boolean result = false;
		HashMap<String, String> unapply = ExtUtil.unapply(planItem.ext);
		if (planItem != null && planItem.plannedExecutionAt != null
				&& (planItem.palnnedQty.doubleValue() > 0)
				&& StringUtils.isNotEmpty(unapply.get("lot"))
				&& StringUtils.isNotEmpty(unapply.get("containerNo")))
			result = true;
		return result;
	}
	public static boolean exportCheck(String pi){
		if(StringUtils.isNotEmpty(pi)){
			pi = pi.toUpperCase();
			if(pi.indexOf("SPI")>-1||pi.indexOf("AN")>-1||pi.indexOf("AND")>-1){
				return true;
			}
		}
		return false;
	}
	public static boolean exportCheckByPI(String pi){
		List<OrderItem> orderItems = OrderItem.find().where().eq("deleted", false).eq("order.warehouse.id", SessionSearchUtil.searchWarehouse().id).eq("order.orderType", "T001").eq("order.internalOrderNo", pi).findList();
		if(orderItems.size()>0){
			OrderItem orderItem = orderItems.get(0);
			String piType = ExtUtil.unapply(orderItem.ext).get("piType");
			if("Local".equals(piType))
				return false;
			if("Export".equals(piType))
				return true;
			else{
				return exportCheck(pi);
			}
		}else{
			return exportCheck(pi);
		}
	}
}
