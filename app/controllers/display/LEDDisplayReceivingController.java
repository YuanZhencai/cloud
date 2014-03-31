/** * LEDDisplayStuffingController.java 
 * Created on 2013-5-30 下午4:03:00 
 */

package controllers.display;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Execution;
import models.PlanItem;
import models.PlanItemDetail;
import models.Warehouse;
import models.vo.display.DisplayReceivingVO;
import models.vo.display.LastVo;
import models.vo.display.ReceivingVo;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.NoUserDeadboltHandler;
import utils.DateUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import views.html.display.LEDDisplayReceiving;
import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

/**
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: LEDDisplayReceivingController.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">ChenAi hong</a>
 */

@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class LEDDisplayReceivingController extends Controller {

	static String receivingPlan = "T001"; // T001表示收货的plan
	static String hasExcution = "S003";// 表示该plan已经被执行
	static Warehouse warehouse = SessionSearchUtil.searchWarehouse();
	public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

	public static Result index() {
		return ok(LEDDisplayReceiving.render(""));
	}

	/**
	 * @return
	 */
	public static Result list() {
		List<ReceivingVo> displayReceivingVos = new ArrayList<ReceivingVo>();
		Date today = new Date();
		List<PlanItemDetail> planItemDetailList = PlanItemDetail.find()
				.where().eq("planItem.planType", receivingPlan).eq("planItem.order.warehouse", SessionSearchUtil.searchWarehouse())
				.le("planItem.plan.plannedTimestamp", DateUtil.strToDate(DateUtil.dateToStrShort(today) + " 23:59:59", DATE_PATTERN))
				.eq("deleted", false).findList();
		LastVo lastVo=null;
		for (PlanItemDetail planItemDetail : planItemDetailList) {
			LastVo temp = new LastVo();
			if(temp.inPlanItemDetail(planItemDetail)){
				if(lastVo!=null){
					if(!lastVo.isLaster(temp))
						lastVo=temp;
				}else{
					lastVo=temp;
				}
			}
			DisplayReceivingVO displayReceivingVO = new DisplayReceivingVO();
			displayReceivingVO.inPlanItemDetail(planItemDetail);
			boolean isHave=false;
			for(ReceivingVo receivingVo:displayReceivingVos){
				if(receivingVo.productLine!=null&&receivingVo.productLine.equals(displayReceivingVO.prodLine)){
					isHave=true;
					List<DisplayReceivingVO> list = receivingVo.DisplayReceivingVOs;
					boolean isAdd=false;
					for (DisplayReceivingVO displayReceivingVO2 : list) {
						if( displayReceivingVO2.addDisplayReceivingVo(displayReceivingVO)){
							isAdd=true;
						}
					}
					if(!isAdd){
						list.add(displayReceivingVO);
					}
				}
			}
			if(!isHave){
				ReceivingVo receivingVo = new ReceivingVo();
				receivingVo.productLine=displayReceivingVO.prodLine;
				receivingVo.DisplayReceivingVOs.add(displayReceivingVO);
				displayReceivingVos.add(receivingVo);
			}
		}
		List<ReceivingVo> Result=new ArrayList<ReceivingVo>();
		for (ReceivingVo receivingVo : displayReceivingVos) {
			List<DisplayReceivingVO> displayReceivingVOs2 = receivingVo.DisplayReceivingVOs;
			List<DisplayReceivingVO> Temp = new ArrayList<DisplayReceivingVO>();
			for (int i=0;i<displayReceivingVOs2.size();i++) {
				if(!displayReceivingVOs2.get(i).LastUpdate.before(DateUtil.addHoursDate(new Date(), -2))){
					Temp.add(displayReceivingVOs2.get(i));
				}
			}
			if(Temp.size()!=0){
				ReceivingVo receivingVo2 = new ReceivingVo();
				receivingVo2.productLine=receivingVo.productLine;
				receivingVo2.DisplayReceivingVOs=Temp;
				Result.add(receivingVo2);
			}
		}
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("List", Result);
		hashMap.put("last", lastVo);
		return ok(play.libs.Json.toJson(hashMap));
	}
}
