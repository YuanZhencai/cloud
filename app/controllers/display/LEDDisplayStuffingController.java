/** * LEDDisplayStuffingController.java 
 * Created on 2013-5-30 下午4:03:00 
 */

package controllers.display;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import be.objectify.deadbolt.java.actions.SubjectPresent;

import models.Batch;
import models.Execution;
import models.PlanItem;
import models.Stock;
import models.Warehouse;
import models.vo.display.DisplayStuffingVo;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.NoUserDeadboltHandler;
import utils.BatchSearchUtil;
import utils.DateUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import views.html.display.LEDDisplayStuffing;
import action.Menus;

/**
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: LEDDisplayStuffingController.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">ChenAi hong</a>
 */
@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class LEDDisplayStuffingController extends Controller {

	static String SourceTypeOfCargo = "T003";// 表示对应的type是发货
	static String hasExcution = "S003";// 表示该plan已经被执行
	static Warehouse warehouse = SessionSearchUtil.searchWarehouse();
	public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

	public static Result index() {
		return ok(LEDDisplayStuffing.render(""));
	}

	/**
	 * 查询当前仓库下的所有待发货的planItem
	 * 
	 * @return
	 */
	public static Result list() {
		List<PlanItem> planItemList = new ArrayList<PlanItem>();
		Date today = new Date();
		planItemList = PlanItem.find().fetch("batch").where().eq("planType", SourceTypeOfCargo)
				.le("plan.plannedTimestamp", DateUtil.strToDate(DateUtil.dateToStrShort(today) + " 23:59:59", DATE_PATTERN))
				.ne("itemStatus", hasExcution).eq("order.warehouse", SessionSearchUtil.searchWarehouse()).eq("deleted", false).findList();
		List<DisplayStuffingVo> displayStuffingVos = new ArrayList<DisplayStuffingVo>();
		if (planItemList.size() > 0) {
			for (PlanItem planItem : planItemList) {
			    List<Execution> findList = Execution.find().where().eq("deleted",false).eq("planItem", planItem).findList();
			    if(findList.size()<1){ 
				DisplayStuffingVo displayStuffingVo= new DisplayStuffingVo();
			    displayStuffingVo.atLoadingBay = false;
				HashMap<String, String> ext = new HashMap<String, String>();
				ext = ExtUtil.unapply(planItem.ext);
				String piNo = ext.get("pi");
				String lot = ext.get("lot");
				List<Batch> batchs = BatchSearchUtil.getBatch(piNo, lot);
				if (batchs.size() > 0) {
					for (Batch batch : batchs) {
					    List<Stock> stocks = Stock.find().where().eq("batch.id", batch.id.toString()).eq("deleted", false).findList();
						for (Stock stock :stocks) {
                            if(stock.area.storageType.nameKey.equals("Loading Bay")){
                                displayStuffingVo.atLoadingBay=true;
                                displayStuffingVo.Qty= displayStuffingVo.Qty+stock.qty.doubleValue();
                                displayStuffingVo.loadingBay = stock.area.nameKey;
                            }
                        }
					}
				}
				 // displayStuffingVo 
                  displayStuffingVo.piNo = planItem.order.internalOrderNo;
                  if(planItem!=null&&planItem.orderItem!=null&&planItem.orderItem.material!=null)
                  displayStuffingVo.commodityName = planItem.orderItem.material.materialName;
                  if (planItem.orderItem.settlementQty != null){
                	  displayStuffingVo.piQty = planItem.orderItem.settlementQty.toString();// 包括容差
                  }
                  if (planItem.palnnedQty != null){
                	  displayStuffingVo.stuffingPlanQty = planItem.palnnedQty.toString();
                  }
                  displayStuffingVo.prodDate = planItem.plan.plannedTimestamp;
                  displayStuffingVo.remarks = planItem.orderItem.remarks;
                  HashMap<String, String> extPlan = ExtUtil.unapply(planItem.ext);
                  HashMap<String, String> extOrder = ExtUtil.unapply(planItem.orderItem.ext);
                  displayStuffingVo.containerNo = extPlan.get("containerNo");
                  displayStuffingVo.sealNo = extPlan.get("seal_No");
                  displayStuffingVo.sgRemarks = extOrder.get("remarks");
                  displayStuffingVo.transp = extOrder.get("transp");
                  displayStuffingVo.feeder = extOrder.get("feeder_vessel");
                  if (extOrder.get("closed_date") != null && !"".equals(extOrder.get("closed_date")) && extOrder.get("closed_time") != null
                          && !"".equals(extOrder.get("closed_time"))) {
                      displayStuffingVo.closedDate =new Date(Long.valueOf(extOrder.get("closed_date"))+Long.valueOf(extOrder.get("closed_time")) );
                  } else if (extOrder.get("closed_date") != null&&!extOrder.get("closed_date").equals("")) {
                      displayStuffingVo.closedDate = new Date(Long.valueOf(extOrder.get("closed_date")) );
                  }
                  displayStuffingVos.add(displayStuffingVo);
			    }
				/*if (stocks.size() > 0) {
					Set<String> areaIds = new HashSet<String>();
					List<Stock> stockTemps = new ArrayList<Stock>();
					for (int i = 0; i < stocks.size(); i++) {// 如果这同一个batch下的stock不在同一个位置，则显示多条
						areaIds.add(stocks.get(i).area.id.toString());
					}
					for (String str : areaIds) {
						stockTemps = Stock.find().where().eq("deleted", false).eq("area.id", str).findList();
						if (stockTemps.size() > 0) {
							Stock stock = stockTemps.get(0);
							displayStuffingVo = new DisplayStuffingVo();
							displayStuffingVo.atLoadingBay = false;
							displayStuffingVo.loadingBay = stock.area.nameKey;
							displayStuffingVo.piNo = planItem.order.internalOrderNo;
							displayStuffingVo.materialCode = planItem.orderItem.material.materialCode;
							if (planItem.orderItem.settlementQty != null)
								displayStuffingVo.piQty = planItem.orderItem.settlementQty.toString();// 包括容差
							if (planItem.palnnedQty != null)
								displayStuffingVo.stuffingPlanQty = planItem.palnnedQty.toString();
							displayStuffingVo.prodDate = planItem.plan.plannedTimestamp;
							displayStuffingVo.remarks = planItem.remarks;
							HashMap<String, String> extPlan = ExtUtil.unapply(planItem.ext);
							HashMap<String, String> extOrder = ExtUtil.unapply(planItem.orderItem.ext);
							displayStuffingVo.containerNo = extPlan.get("containerNo");
							displayStuffingVo.sealNo = extPlan.get("seal_No");
							if (extOrder.get("closed_date") != null && !"".equals(extOrder.get("closed_date")) && extOrder.get("closed_time") != null
									&& !"".equals(extOrder.get("closed_time"))) {
								displayStuffingVo.closedDate =new Date(Long.valueOf(extOrder.get("closed_date"))+Long.valueOf(extOrder.get("closed_time")) );
							} else if (extOrder.get("closed_date") != null&&!extOrder.get("closed_date").equals("")) {
								displayStuffingVo.closedDate = new Date(Long.valueOf(extOrder.get("closed_date")) );
							}
							if ((stock.area.storageType.id.toString()).equals(loadingBay)) {// 判断如果该Stock已经在loadingBay上时，该条数据显示为红色
								displayStuffingVo.atLoadingBay = true;
							}
							displayStuffingVos.add(displayStuffingVo);
						}
					}

				}*/
			}
		}
		return ok(play.libs.Json.toJson(displayStuffingVos));
	}
}
