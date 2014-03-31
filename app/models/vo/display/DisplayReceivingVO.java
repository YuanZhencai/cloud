/** * serchVo.java 
* Created on 2013-4-22 上午11:15:59 
*/

package models.vo.display;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import models.Execution;
import models.PlanItemDetail;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.DateUtil;
import utils.ExtUtil;
import utils.UnitConversion;

/** 
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: DisplayReceivingVO.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">ChenAi hong</a>
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class DisplayReceivingVO {
	public String prodLine;
	public String piNo;
	public double netWeight;
	public double piQty;
	public double planQty;
	public double receivedQty;
	public String materialCode;
	public double receivePallet;
	public double totalPallet;
	public Date LastUpdate;
	@JsonIgnore
	public List<String> PlanItemIds=new ArrayList<String>();
	public void inPlanItemDetail(PlanItemDetail planItemDetail){
		if(planItemDetail!=null){
			if(planItemDetail.updatedAt!=null){
				this.LastUpdate=new Date(planItemDetail.updatedAt.getTime());
			}
			this.totalPallet=1;
			if(planItemDetail.planItem!=null&&planItemDetail.planItem.orderItem!=null&&planItemDetail.planItem.orderItem.order!=null){
				this.piNo=planItemDetail.planItem.orderItem.order.internalOrderNo;
				if(planItemDetail.planItem.orderItem.settlementQty!=null)
					this.piQty=planItemDetail.planItem.orderItem.settlementQty.doubleValue();
			}
			if(planItemDetail.planItem!=null&&planItemDetail.planItem.id!=null)
				this.PlanItemIds.add(planItemDetail.planItem.id.toString());
			if(planItemDetail.planItem!=null&&planItemDetail.planItem.palnnedQty!=null)
				this.planQty=planItemDetail.planItem.palnnedQty.doubleValue();
			List<Execution> findList = Execution.find().where().eq("deleted", false).eq("planItemDetail.id", planItemDetail.id).findList();
			if(findList.size()>0){
				this.receivePallet=1;
				receivedQty+=planItemDetail.palnnedQty.doubleValue();
			}
			if(planItemDetail.planItem!=null&&planItemDetail.planItem.material!=null){
				this.materialCode=planItemDetail.planItem.material.materialName;
				if(planItemDetail.planItem.fromMaterialUom!=null&&planItemDetail.planItem.toMaterialUom!=null)
				this.netWeight=UnitConversion.returnComparing(planItemDetail.planItem.fromMaterialUom.uomCode, planItemDetail.planItem.toMaterialUom.uomCode, planItemDetail.planItem.material.id.toString());
			}
			if (planItemDetail.fromBin != null) {
				prodLine = planItemDetail.fromBin.nameKey;
			}
		}
	}
	public boolean addDisplayReceivingVo(DisplayReceivingVO displayReceivingVO){
		if(displayReceivingVO.piNo!=null&&displayReceivingVO.piNo.equals(this.piNo)){
			if(this.LastUpdate!=null){
				if(this.LastUpdate.before(displayReceivingVO.LastUpdate))
					this.LastUpdate=displayReceivingVO.LastUpdate;
			}
			this.totalPallet++;
			if(displayReceivingVO.PlanItemIds.size()>0&&!displayReceivingVO.PlanItemIds.contains(displayReceivingVO.PlanItemIds.get(0))){
				this.planQty+=displayReceivingVO.planQty;
			}
			this.receivePallet+=displayReceivingVO.receivePallet;
			this.receivedQty+=displayReceivingVO.receivedQty;
			return true;
		}else{
			return false;
		}
	}
}
