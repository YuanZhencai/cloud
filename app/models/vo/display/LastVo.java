package models.vo.display;

import java.util.Date;
import java.util.List;

import models.Execution;
import models.PlanItemDetail;

import org.codehaus.jackson.annotate.JsonIgnore;

public class LastVo {
	public String piNo;
	public String From;
	public String To;
	public double Qty;
	@JsonIgnore
	public Date date;
	public boolean inPlanItemDetail(PlanItemDetail planItemDetail){
		if(planItemDetail!=null){
			List<Execution> findList = Execution.find().where().eq("deleted", false).eq("planItemDetail.id", planItemDetail.id).findList();
			if(findList.size()>0){
			if(findList.get(0).createdAt!=null)
				this.date =new Date(findList.get(0).createdAt.getTime());
			if(planItemDetail.fromBin!=null)
				this.From=planItemDetail.fromBin.nameKey;
			if(planItemDetail.toBin!=null)
				this.To=planItemDetail.toBin.nameKey;
			if(planItemDetail.palnnedQty!=null)
				this.Qty=planItemDetail.palnnedQty.doubleValue();
			if(planItemDetail.planItem!=null&&planItemDetail.planItem.order!=null)
				this.piNo=planItemDetail.planItem.order.internalOrderNo;
			return true;
			}
		}
		return false;
	}
	public boolean isLaster(LastVo lastVo){
		return this.date!=null&&this.date.after(lastVo.date);
	}
}
