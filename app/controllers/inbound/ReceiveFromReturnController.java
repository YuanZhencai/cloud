package controllers.inbound;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import models.Area;
import models.Batch;
import models.Bin;
import models.Code;
import models.Employee;
import models.EmployeeWarehouse;
import models.Execution;
import models.ItemPolicy;
import models.MaterialUom;
import models.OrderItem;
import models.Plan;
import models.PlanItem;
import models.PlanItemDetail;
import models.Stock;
import models.StockTransaction;
import models.TimingPolicy;
import models.Warehouse;
import models.vo.Result.ResultVo;
import models.vo.inbound.AreaVo;
import models.vo.inbound.BinVo;
import models.vo.inbound.EmployeeVo;
import models.vo.inbound.PiStatusVo;
import models.vo.inbound.PlanItemSearchVo;
import models.vo.inbound.PlanItemDetailVo;
import models.vo.inbound.PlanItemVo;
import models.vo.inbound.PrintVo;
import models.vo.inbound.WarehouseVo;
import security.NoUserDeadboltHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.ExpressionList;

import play.data.Form;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import utils.BatchSearchUtil;
import utils.CodeUtil;
import utils.CrudUtil;
import utils.DateUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import utils.UnitConversion;
import utils.Service.DataExchangePlatform;
import utils.exception.CustomException;
import views.html.inbound.receiveFromReturn;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: PlanItemController.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
@SubjectPresent(handler = NoUserDeadboltHandler.class)
public class ReceiveFromReturnController extends Controller {

	private static final Logger logger = LoggerFactory.getLogger(ReceiveFromReturnController.class);

	static Form<PlanItemDetailVo> planItemDetailForm = Form.form(PlanItemDetailVo.class);
	static Form<PlanItemVo> planItemForm = Form.form(PlanItemVo.class);
	static Form<PlanItemSearchVo> piSearchForm = Form.form(PlanItemSearchVo.class);
	static String TEMPERING = "Tempering";// 硬编码
	static String AGEING = "Ageing";// 硬编码
	static String PRODUCTIONLINE = "Receiving";// 硬编码
	static String PAWORKERCODE = "ET004";// 硬编码
	static String DRIVERCODE = "ET006";// 硬编码
	static String LEADERSHIFT = "ET005";// 硬编码
	// **********code and status************************
	static String PLANSTATUS_1 = "S000";// 新建
	static String PLANSTATUS_2 = "S001";// 确认，即有detail
	static String PLANITEM_PLANTYPE = "T001";// 收货
	static String PLANITEM_PLANSUBTYPE = "T001.001";// 生产入库
	static String DETATILSTATUS_NEW = "S000";// 新建detail的状态
	static String DETATILSTATUS_PRINTED = "S001";// 已经被打印
	static String ORDERSTATUS_3 = "S002";// 执行中
	static String ORDERSTATUS_4 = "S003";// 已完成
	static String PLANSTATUS_3 = "S002";// 执行中
	static String PLANSTATUS_4 = "S003";// 已完成
	static String STOCKSTATUS = "S001";// 在库
	static String EXECUTIONTYPE_1 = "T001";// 收货
	static String EXECUTIONSUBTYPE_1 = "T001.003";// 退货入库
	static String TRANSACTIONCODE_1 = "T101";// 收货
	// 允许从前台传到后台的最大数据量
	private static final int MAX_LENGTH = 10 * 1024 * 1024;

	/**
	 * 页面数据初始化
	 * @return
	 */
	@With(Menus.class)
	public static Result index() {
		String s = "abc";
		String b = "abcd";
		System.out.println(b.contains(s));
		return ok(receiveFromReturn.render(""));
	}

	/**
	 * 初始化头查询Pi status
	 * @return
	 */
	public static Result initPiStatus() {
		List<PiStatusVo> piStatusVos = new ArrayList<PiStatusVo>();
		if (SessionSearchUtil.searchWarehouse() == null) {
			logger.error(">>>> initWarehouse>>>> session is null");
			return ok(play.libs.Json.toJson(new ResultVo(piStatusVos)));
		} else {
			List<Code> codes = CodeUtil.getOrderStatus();
			for (Code code : codes) {
				PiStatusVo piStatusVo = new PiStatusVo();
				piStatusVo.initCode(code);
				piStatusVos.add(piStatusVo);
			}
			return ok(play.libs.Json.toJson(new ResultVo(piStatusVos)));
		}
	}

	/**
	 * 初始化头查询bin
	 * @return
	 */
	public static Result initBin() {
		List<BinVo> binVoList = new ArrayList<BinVo>();
		if (SessionSearchUtil.searchWarehouse() == null) {
			logger.error(">>>> initWarehouse>>>> session is null");
			return ok(play.libs.Json.toJson(new ResultVo(binVoList)));
		} else {
			String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
			List<Bin> bins = Bin.find().where().eq("area.warehouse.id", warehouseId).eq("area.warehouse.deleted", false)
					.eq("area.storageType.nameKey", PRODUCTIONLINE).eq("area.storageType.deleted", false).orderBy("nameKey").findList();
			for (Bin bin : bins) {
				BinVo binVo = new BinVo();
				binVo.id = bin.id.toString();
				binVo.nameKey = bin.binCode;
				binVoList.add(binVo);
			}
			return ok(play.libs.Json.toJson(new ResultVo(binVoList)));
		}
	}

	/**
	 * Plan_item表单查询(之前的页面载入就查询，暂时不用)
	 * @return
	 */
	public static Result list() {
		// 默认展示时不带出数据list
		List<PlanItemVo> planItemVoList = new ArrayList<PlanItemVo>();
		return ok(play.libs.Json.toJson(new ResultVo(planItemVoList)));
	}

	/**
	 * PlanItem的头查询
	 * @return
	 */
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result searchPiList() {
		JsonNode body = request().body().asJson();
		List<PlanItemVo> planItemVoList = new ArrayList<PlanItemVo>();
		if (body != null) {
			if (SessionSearchUtil.searchWarehouse() == null) {
				logger.error(">>>> initWarehouse>>>> session is null");
				return ok(play.libs.Json.toJson(new ResultVo(planItemVoList)));
			} else {
				List<PlanItem> planItemList = new ArrayList<PlanItem>();
				String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
				logger.info("+++++++Receive From Return+++++++searchlist++++start++++++++");
				boolean doNext = false;
				PlanItemSearchVo piSerchVo = Json.fromJson(body, PlanItemSearchVo.class);
				Date fdate = DateUtil.strToDate("1900-01-01", "yyyy-MM-dd");
				Date tdate = DateUtil.strToDate("2100-12-31", "yyyy-MM-dd");
				if (piSerchVo.fromDate != null) {
					fdate = DateUtil.strToDate(DateUtil.dateToStrShort(new Date(piSerchVo.fromDate.getTime())) + " 00:00:00", "yyyy-MM-dd HH:mm:ss");
				}
				if (piSerchVo.toDate != null) {
					tdate = DateUtil.strToDate(DateUtil.dateToStrShort(new Date(piSerchVo.toDate.getTime())) + " 23:59:59", "yyyy-MM-dd HH:mm:ss");
				}
				if (tdate.after(fdate)) {
					doNext = true;
				} else {
					if (tdate.equals(fdate)) {
						doNext = true;
					}
				}
				if (!doNext) {
					return ok(play.libs.Json.toJson(new ResultVo("error", "Production Date(To) can not be greater than Production Date(From)!")));
				} else {
					ExpressionList<PlanItem> searchPlanItem = PlanItem.find().where().eq("deleted", false).eq("planType", PLANITEM_PLANTYPE)
							.eq("orderItem.deleted", false).eq("orderItem.order.deleted", false).eq("plan.deleted", false)
							.eq("materialUom.deleted", false).eq("material.deleted", false).eq("orderItem.order.warehouse.deleted", false)
							.eq("orderItem.order.warehouse.id", warehouseId).eq("toArea.storageType.nameKey", PRODUCTIONLINE)
							.eq("toArea.storageType.deleted", false).eq("orderItem.order.sourceType", "T003");
					if (piSerchVo.piNo != null && !"".equals(piSerchVo.piNo)) {
						searchPlanItem = searchPlanItem.like("orderItem.order.externalOrderNo", "%" + piSerchVo.piNo + "%");
					}
					if (piSerchVo.sgPiNo != null && !"".equals(piSerchVo.sgPiNo)) {
						searchPlanItem = searchPlanItem.like("orderItem.order.contractNo", "%" + piSerchVo.sgPiNo + "%");
					}
					if (piSerchVo.piStatus != null && !"".equals(piSerchVo.piStatus)) {
						searchPlanItem = searchPlanItem.eq("orderItem.order.orderStatus", piSerchVo.piStatus);
					}
					if (piSerchVo.productionLine != null && !"".equals(piSerchVo.productionLine)) {
						searchPlanItem = searchPlanItem.eq("toBin.id", piSerchVo.productionLine);
					}
					searchPlanItem = searchPlanItem.between("plan.plannedTimestamp", fdate, tdate);
					planItemList = searchPlanItem.findList();
					for (PlanItem planItem : planItemList) {
						if (ExtUtil.unapply(planItem.orderItem.ext).get("referenceNo").contains(piSerchVo.referenceNo)) {
							boolean ifExecution = false;
							PlanItemVo planItemVo = new PlanItemVo();
							planItemVo.filledVo(planItem, ifExecution);
							planItemVoList.add(planItemVo);
						}
					}
					logger.info("+++++++Receive From Return+++++++searchlist++++end++++++++");
					return ok(play.libs.Json.toJson(new ResultVo(planItemVoList)));
				}
			}
		} else {
			logger.error("+++++++Receive From Return+++++++searchlist++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo(planItemVoList)));
		}
	}

	/**
	 * split Plan
	 * @return
	 */
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result saveSpiltPlan() {
		logger.info("++++++Receive From Return+++++++saveSpiltPlan+++++start++++++++");
		JsonNode body = request().body().asJson();
		if (body != null) {
			PlanItemVo planItemVo = Json.fromJson(body, PlanItemVo.class);
			PlanItem planitem = PlanItem.find().where().eq("id", planItemVo.planItemId).eq("deleted", false).findUnique();
			try {
				isExcepitonNull(planitem, "Can not find the planItem in the database!");
				isExcepitonNull(planItemVo.piSKU, "Can not find the new  Plan Prod Qty from the page!");
				isExcepitonNull(planItemVo.productionLineId, "Can not find the new  Plan Prod Line from the page!");
				isExcepitonNull(planitem.palnnedQty, "Can not get the plan qty who's id is" + planItemVo.planItemId);
				if (planItemVo.piSKU.doubleValue() > planitem.palnnedQty.doubleValue()) {
					return ok(play.libs.Json.toJson(new ResultVo("error", "The new Plan's Prod Qty is bigger than the old plan!")));
				} else {
					Plan plan = new Plan();
					plan = clonePlan(plan, planitem.plan);
					CrudUtil.save(plan);
					PlanItem planItem = new PlanItem();
					planItem = clonePlanItem(plan, planItem, planitem, planItemVo);
					CrudUtil.save(planItem);
					PlanItemVo itemVo = new PlanItemVo();
					itemVo.filledVo(planItem, false);
					logger.info("++++++Receive From Return+++++++saveSpiltPlan+++++end++++++++");
					return ok(play.libs.Json.toJson(new ResultVo(itemVo)));
				}
			} catch (CustomException e) {
				e.printStackTrace();
				return ok(play.libs.Json.toJson(new ResultVo("error", e.getMessage())));
			}
		} else {
			logger.error("+++++++Receive From Return+++++++saveSpiltPlan++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo("error", "Can not get data from page!")));
		}
	}

	/**
	 * 克隆plan
	 * @param plan
	 * @param oldPlan
	 * @return
	 * @throws CustomException
	 */
	public static Plan clonePlan(Plan plan, Plan oldPlan) throws CustomException {
		plan.remarks = oldPlan.remarks;
		plan.ext = oldPlan.remarks;
		plan.deleted = oldPlan.deleted;
		plan.schemaCode = oldPlan.schemaCode;
		plan.warehouse = oldPlan.warehouse;
		plan.planType = oldPlan.planType;
		plan.planSubtype = oldPlan.planSubtype;
		plan.order = oldPlan.order;
		plan.plannedTimestamp = oldPlan.plannedTimestamp;
		plan.seqNo = oldPlan.seqNo;
		plan.assignedTo = oldPlan.assignedTo;
		plan.planStatus = oldPlan.planStatus;
		plan.version = 1;
		return plan;
	}

	/**
	 * 克隆planItem
	 * @param planItem
	 * @param oldPlanItem
	 * @return
	 * @throws CustomException
	 */
	public static PlanItem clonePlanItem(Plan plan, PlanItem newPlanItem, PlanItem oldPlanItem, PlanItemVo planItemVo) throws CustomException {
		newPlanItem.remarks = oldPlanItem.remarks;
		newPlanItem.deleted = oldPlanItem.deleted;
		newPlanItem.schemaCode = oldPlanItem.schemaCode;
		newPlanItem.plan = plan;
		newPlanItem.planType = oldPlanItem.planType;
		newPlanItem.planSubtype = oldPlanItem.planSubtype;
		newPlanItem.order = oldPlanItem.order;
		newPlanItem.orderItem = oldPlanItem.orderItem;
		newPlanItem.material = oldPlanItem.material;
		newPlanItem.materialUom = oldPlanItem.materialUom;
		newPlanItem.batch = oldPlanItem.batch;
		newPlanItem.plannedExecutionAt = oldPlanItem.plannedExecutionAt;
		newPlanItem.palnnedQty = planItemVo.piSKU;
		newPlanItem.minPercent = oldPlanItem.minPercent;
		newPlanItem.maxPercent = oldPlanItem.maxPercent;
		newPlanItem.fromMaterialUom = oldPlanItem.fromMaterialUom;
		newPlanItem.fromArea = oldPlanItem.fromArea;
		newPlanItem.fromBin = oldPlanItem.fromBin;
		newPlanItem.toMaterialUom = oldPlanItem.toMaterialUom;
		newPlanItem.toArea = oldPlanItem.toArea;
		newPlanItem.toBin = oldPlanItem.toBin;
		newPlanItem.seqNo = oldPlanItem.seqNo;
		newPlanItem.assignedTo = oldPlanItem.assignedTo;
		newPlanItem.itemStatus = oldPlanItem.itemStatus;
		newPlanItem.version = 1;
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("line", planItemVo.productionLineId);
		newPlanItem.ext = ExtUtil.updateExt(oldPlanItem.ext, map);
		Bin bin = Bin.find().where().eq("deleted", false).eq("id", planItemVo.productionLineId).findUnique();
		if (bin != null) {
			newPlanItem.toBin = bin;
		}
		oldPlanItem.palnnedQty = oldPlanItem.palnnedQty.subtract(planItemVo.piSKU);
		CrudUtil.update(oldPlanItem);
		return newPlanItem;
	}

	/**
	 * split时返回换算比例
	 * @return
	 */
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result getPiSku() {
		logger.info("++++++Receive From Return+++++++getPiSku+++++start++++++++");
		JsonNode body = request().body().asJson();
		if (body != null) {
			PlanItemVo planItemVo = Json.fromJson(body, PlanItemVo.class);
			return ok(play.libs.Json.toJson(new ResultVo(returnComparing(planItemVo.productionuomId, planItemVo.skuUOMId))));
		} else {
			logger.error("+++++++Receive From Return+++++++getPiSku++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo("error", "Can not get data from page!")));
		}
	}

	/**
	 * 获得单位的换算关系
	 * @param uom
	 * @param sku
	 * @return
	 */
	@Transactional
	public static double returnComparing(String uom, String sku) {
		MaterialUom materialUom = MaterialUom.find().where().eq("id", uom).eq("deleted", false).findUnique();
		MaterialUom materialUomSku = MaterialUom.find().where().eq("id", sku).eq("deleted", false).findUnique();
		if (materialUom != null && materialUomSku != null) {
			double comparing = (materialUom.qtyOfBaseDenom.doubleValue() / materialUom.qtyOfBaseNum.doubleValue())
					/ (materialUomSku.qtyOfBaseDenom.doubleValue() / materialUomSku.qtyOfBaseNum.doubleValue());
			return comparing;
		} else {
			return 1;
		}
	}

	/**
	 * 根据Plan_Item_id查询plan_item_detail的内容
	 * @return
	 */
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result get() {
		logger.info("++++++Receive From Return+++++++detailList+++++start++++++++");
		JsonNode body = request().body().asJson();
		List<PlanItemDetailVo> piDetailVoList = new ArrayList<PlanItemDetailVo>();
		if (body != null) {
			if (SessionSearchUtil.searchWarehouse() == null) {
				logger.error(">>>> initWarehouse>>>> session is null");
				return ok(play.libs.Json.toJson(new ResultVo(piDetailVoList)));
			} else {
				String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
				PlanItemVo planItemVo = Json.fromJson(body, PlanItemVo.class);
				if (planItemVo.planItemId == null) {
					logger.error("+++++++Receive From Return+++++++detailList++++Can not find the PlanItem!++++++++");
					return ok(play.libs.Json.toJson(new ResultVo("error", "Can not find the PlanItem!")));
				}
				List<PlanItemDetail> planItemDetailList = PlanItemDetail.find().where().eq("deleted", false).eq("planItem.deleted", false)
						.eq("planItem.id", planItemVo.planItemId).eq("planItem.order.warehouse.deleted", false)
						.eq("planItem.order.warehouse.id", warehouseId).findList();
				if (planItemDetailList.size() > 0) {
					for (PlanItemDetail planItemDetail : planItemDetailList) {
						boolean isexection = Execution.find().where().eq("deleted", false).eq("planItemDetail.id", planItemDetail.id).findList()
								.size() > 0 ? true : false;
						PlanItemDetailVo planItemDetailVo = new PlanItemDetailVo();
						planItemDetailVo.filledVo(planItemDetail, isexection);
						piDetailVoList.add(planItemDetailVo);
					}
				}
				logger.info("+++++++Receive From Return++++++detailList(orderByStockNo)++++end++++++++");
				return ok(play.libs.Json.toJson(new ResultVo(orderByStockNo(piDetailVoList))));
			}
		} else {
			logger.error("+++++++Receive From Return+++++++detailList++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo(piDetailVoList)));
		}
	}

	/**
	 * 按stockno排序
	 * @param pivos
	 * @return
	 */
	public static List<PlanItemDetailVo> orderByStockNo(List<PlanItemDetailVo> pivos) {
		if (pivos.size() > 1) {
			Collections.sort(pivos, new Comparator<PlanItemDetailVo>() {
				@Override
				public int compare(PlanItemDetailVo o1, PlanItemDetailVo o2) {
					if (StringUtils.isNotEmpty(o1.getStockno()) && StringUtils.isNotEmpty(o2.getStockno())) {
						if (Integer.valueOf(o1.getStockno()) == Integer.valueOf(o2.getStockno())) {
							return 0;
						} else if (Integer.valueOf(o1.getStockno()) > Integer.valueOf(o2.getStockno())) {
							return 1;
						} else {
							return -1;
						}
					} else {
						return -1;
					}
				}
			});
		}
		return pivos;
	}

	/**
	 * 计算整个pi下收到的货物总量（执行过的才算）
	 * @return
	 */
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result sumPIQty() {
		logger.info("++++Receive From Return++++++sumPIQty+++++start++++++");
		JsonNode body = request().body().asJson();
		if (body != null) {
			if (SessionSearchUtil.searchWarehouse() == null) {
				logger.error(">>>> initWarehouse>>>> session is null");
				return ok(play.libs.Json.toJson(new ResultVo("error", "Can not get the loggin information, Please loggin again!")));
			} else {
				PlanItemVo planItemVo = Json.fromJson(body, PlanItemVo.class);
				try {
					BigDecimal piReceivedQty = new BigDecimal(0);
					BigDecimal conversion = new BigDecimal(0);
					isExcepitonNull(planItemVo, "Can not get the information from the page!");
					isExcepitonNull(planItemVo.planItemId, "Can not get the information from the page!!");
					PlanItem planitem = PlanItem.find().where().eq("id", planItemVo.planItemId).eq("deleted", false).findUnique();
					isExcepitonNull(planitem, "Can not find the PlanItem data in the database!!");
					List<Execution> executionList = Execution.find().where().eq("deleted", false).eq("planItem.orderItem.id", planitem.orderItem.id)
							.findList();
					for (Execution execution : executionList) {
						piReceivedQty = piReceivedQty.add(execution.executedQty);
					}
					conversion = UnitConversion.SkuToQuantity(planitem.orderItem.settlementUom.id.toString(), planitem.materialUom.id.toString());
					piReceivedQty = piReceivedQty.multiply(conversion);
					logger.info("++++Receive From Return++++++sumPIQty+++++end++++++");
					return ok(play.libs.Json.toJson(new ResultVo(piReceivedQty)));
				} catch (CustomException e) {
					e.printStackTrace();
					return ok(play.libs.Json.toJson(new ResultVo("error", e.getMessage())));
				}
			}
		} else {
			logger.error("++++Receive From Return++++++++sumPIQty+++Can not get the warehouse data!+++++");
			return ok(play.libs.Json.toJson(new ResultVo("error", "Can not get the warehouse data!")));
		}
	}

	/**
	 * 初始化编辑生产线bin
	 * @return
	 */
	public static Result initProductionLine() {
		logger.info("+++++Receive From Return+++++initProductionLine+++++++start++++");
		List<BinVo> binVoList = new ArrayList<BinVo>();
		if (SessionSearchUtil.searchWarehouse() == null) {
			logger.error(">>>> initWarehouse>>>> session is null");
		} else {
			String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
			List<Bin> binList = Bin.find().where().eq("deleted", false).eq("area.warehouse.id", warehouseId).eq("area.warehouse.deleted", false)
					.eq("area.storageType.nameKey", PRODUCTIONLINE).eq("area.storageType.deleted", false).orderBy("nameKey").findList();
			for (Bin bin : binList) {
				BinVo binVo = new BinVo();
				binVo.id = bin.id.toString();
				binVo.nameKey = bin.nameKey;
				binVoList.add(binVo);
			}
		}
		logger.info("+++++Receive From Return+++++initProductionLine+++++++end++++");
		return ok(play.libs.Json.toJson(new ResultVo(binVoList)));
	}

	/**
	 * 初始化添加warehouse
	 * @return
	 */
	public static Result initWarehouse() {
		logger.info("++++Receive From Return++++++++initWarehouse+++start+++++");
		List<WarehouseVo> warehouseVoList = new ArrayList<WarehouseVo>();
		if (SessionSearchUtil.searchWarehouse() == null) {
			logger.error(">>>> initWarehouse>>>> session is null");
			return ok(play.libs.Json.toJson(new ResultVo(warehouseVoList)));
		} else {
			Warehouse warehouse = Warehouse.find().where().eq("deleted", false).eq("id", SessionSearchUtil.searchWarehouse().id.toString())
					.findUnique();
			if (warehouse != null) {
				WarehouseVo warehouseVo = new WarehouseVo();
				warehouseVo.id = warehouse.id.toString();
				warehouseVo.nameKey = warehouse.nameKey;
				warehouseVoList.add(warehouseVo);
				logger.info("++++Receive From Return++++++++initWarehouse+++end+++++");
				return ok(play.libs.Json.toJson(new ResultVo(warehouseVoList)));
			} else {
				logger.error("++++Receive From Return++++++++initWarehouse+++Can not get the warehouse data!+++++");
				return ok(play.libs.Json.toJson(new ResultVo("error", "Can not get the warehouse data!")));
			}
		}
	}

	/**
	 * 初始化添加area
	 * @return
	 */
	public static Result initArea() {
		// 对应PlanItem如果有timePolicy，若有存冷库的时间策略，则选择冷库的area，若无，则选择常温库的
		logger.info("++++Receive From Return++++++initArea+++++start++++++");
		List<AreaVo> areaVoList = new ArrayList<AreaVo>();
		JsonNode body = request().body().asJson();
		if (body != null) {
			if (SessionSearchUtil.searchWarehouse() == null) {
				logger.error(">>>> initWarehouse>>>> session is null");
				return ok(play.libs.Json.toJson(new ResultVo(areaVoList)));
			} else {
				String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
				PlanItemDetailVo planItemDetailVo = Json.fromJson(body, PlanItemDetailVo.class);
				PlanItem planItem = PlanItem.find().where().eq("deleted", false).eq("orderItem.deleted", false)
						.eq("orderItem.order.warehouse.id", warehouseId).eq("orderItem.order.warehouse.deleted", false)
						.eq("id", planItemDetailVo.planItemId).findUnique();
				if (planItem != null) {
					List<ItemPolicy> itemPolicys = ItemPolicy.find().where().eq("deleted", false).eq("orderItem.id", planItem.orderItem.id)
							.eq("orderItem.deleted", false).eq("orderItem.order.warehouse.id", warehouseId)
							.eq("orderItem.order.warehouse.deleted", false).findList();
					if (itemPolicys.size() > 1) {
						logger.error("ItemPolicy error : There has more than one data!");
						return ok(play.libs.Json.toJson(new ResultVo("error", "ItemPolicy error : There has more than one data!")));
					} else if (itemPolicys.size() == 1) {
						List<Area> areaList = Area.find().fetch("storageType").fetch("storageType.warehouse").where().eq("deleted", false)
								.eq("warehouse.deleted", false).eq("storageType.warehouse.id", warehouseId)
								.eq("storageType.nameKey", itemPolicys.get(0).policyType).orderBy("nameKey").findList();
						for (Area area : areaList) {
							AreaVo areaVo = new AreaVo();
							areaVo.id = area.id.toString();
							areaVo.nameKey = area.nameKey;
							areaVoList.add(areaVo);
						}
					} else {
						// 用户未填写生成时间策略，则默认放到常温库
						List<Area> areaList = Area.find().fetch("warehouse").fetch("storageType").where().eq("deleted", false)
								.eq("warehouse.deleted", false).eq("warehouse.id", warehouseId).eq("storageType.nameKey", AGEING).orderBy("nameKey")
								.findList();
						for (Area area : areaList) {
							AreaVo areaVo = new AreaVo();
							areaVo.id = area.id.toString();
							areaVo.nameKey = area.nameKey;
							areaVoList.add(areaVo);
						}
					}
					logger.info("++++Receive From Return++++++initArea+++++end++++++");
					return ok(play.libs.Json.toJson(new ResultVo(areaVoList)));
				} else {
					logger.error("+++++Receive From Return++++++initArea++++++Can not find the planItem!++++");
					return ok(play.libs.Json.toJson(new ResultVo("error", "Can not find the planItem!")));
				}
			}
		} else {
			logger.error("+++++Receive From Return++++++initArea+++end++++josn body in null++++");
			return ok(play.libs.Json.toJson(new ResultVo(areaVoList)));
		}
	}

	/**
	 * 初始化添加bin
	 * @return
	 */
	public static Result changeBin() {
		logger.info("+++++Receive From Return+++++initBin+++++++start++++");
		List<BinVo> binVoList = new ArrayList<BinVo>();
		JsonNode body = request().body().asJson();
		if (body != null) {
			if (SessionSearchUtil.searchWarehouse() == null) {
				logger.error(">>>> initWarehouse>>>> session is null");
			} else {
				String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
				PlanItemDetailVo planItemDetailVo = Json.fromJson(body, PlanItemDetailVo.class);
				if (planItemDetailVo.areaId != null && !"".equals(planItemDetailVo.areaId)) {
					List<Bin> binList = Bin.find().fetch("area").where().eq("deleted", false).eq("area.deleted", false)
							.eq("area.id", planItemDetailVo.areaId).eq("area.warehouse.deleted", false).eq("area.warehouse.id", warehouseId)
							.orderBy("nameKey").findList();
					for (Bin bin : binList) {
						BinVo binVo = new BinVo();
						binVo.id = bin.id.toString();
						binVo.nameKey = bin.nameKey;
						binVoList.add(binVo);
					}
				}
			}
		}
		logger.info("+++++Receive From Return+++++initBin+++++++end++++");
		return ok(play.libs.Json.toJson(new ResultVo(binVoList)));
	}

	/**
	 * 初始化添加paworker
	 * @return
	 */
	public static Result initPAworker() {
		// employeeType表中的paworker的编码是ET002
		logger.info("++++++Receive From Return++initPAworker++++++start++++");
		List<EmployeeVo> paworkerVoList = new ArrayList<EmployeeVo>();
		if (SessionSearchUtil.searchWarehouse() == null) {
			logger.error(">>>>>>>>>ininPAworker>> session is null");
		} else {
			String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
			List<EmployeeWarehouse> employeeWarehouseList = EmployeeWarehouse.find().where().eq("deleted", false).eq("warehouse.deleted", false)
					.eq("warehouse.id", warehouseId).findList();
			for (EmployeeWarehouse ew : employeeWarehouseList) {
				Employee employee = Employee.find().fetch("employeeType").where().eq("deleted", false).eq("employeeType.deleted", false)
						.eq("employeeType.typeCode", PAWORKERCODE).eq("id", ew.employee.id).findUnique();
				EmployeeVo employeeVo = new EmployeeVo();
				if (employee != null) {
					employeeVo.id = employee.id.toString();
					employeeVo.nameKey = employee.employeeName;
					paworkerVoList.add(employeeVo);
				}
			}
		}
		logger.info("++++++Receive From Return++initPAworker++++++end++++");
		return ok(play.libs.Json.toJson(new ResultVo(paworkerVoList)));
	}

	/**
	 * 初始化添加driver
	 * @return
	 */
	public static Result initDriver() {
		// employeeType表中的driver的编码是ET001
		logger.info("++++++Receive From Return++initDriver++++++start++++");
		List<EmployeeVo> driverVoList = new ArrayList<EmployeeVo>();
		if (SessionSearchUtil.searchWarehouse() == null) {
			logger.error(">>>>>>>>>ininDriver>> session is null");
		} else {
			List<EmployeeWarehouse> employeeWarehouseList = EmployeeWarehouse.find().where().eq("deleted", false).eq("warehouse.deleted", false)
					.eq("warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).findList();
			for (EmployeeWarehouse ew : employeeWarehouseList) {
				Employee employee = Employee.find().fetch("employeeType").where().eq("deleted", false).eq("employeeType.deleted", false)
						.eq("employeeType.typeCode", DRIVERCODE).eq("id", ew.employee.id).findUnique();
				EmployeeVo employeeVo = new EmployeeVo();
				if (employee != null) {
					employeeVo.id = employee.id.toString();
					employeeVo.nameKey = employee.employeeName;
					driverVoList.add(employeeVo);
				}
			}
		}
		logger.info("++++++Receive From Return++initDriver++++++end++++");
		return ok(play.libs.Json.toJson(new ResultVo(driverVoList)));
	}

	/**
	 * 初始化添加leaderShift
	 * @return
	 */
	public static Result initLeader() {
		// employeeType表中的leaderShift的编码是ETOO5
		logger.info("++++++Receive From Return++initLeader++++++start++++");
		List<EmployeeVo> leaderShiftVoList = new ArrayList<EmployeeVo>();
		if (SessionSearchUtil.searchWarehouse() == null) {
			logger.error(">>>>>>>>>initLeader>> session is null");
		} else {
			List<EmployeeWarehouse> employeeWarehouseList = EmployeeWarehouse.find().where().eq("deleted", false).eq("warehouse.deleted", false)
					.eq("warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).findList();
			for (EmployeeWarehouse ew : employeeWarehouseList) {
				Employee employee = Employee.find().fetch("employeeType").where().eq("deleted", false).eq("employeeType.deleted", false)
						.eq("employeeType.typeCode", LEADERSHIFT).eq("id", ew.employee.id).findUnique();
				EmployeeVo employeeVo = new EmployeeVo();
				if (employee != null) {
					employeeVo.id = employee.id.toString();
					employeeVo.nameKey = employee.employeeName;
					leaderShiftVoList.add(employeeVo);
				}
			}
		}
		logger.info("++++++Receive From Return++initLeader++++++end++++");
		return ok(play.libs.Json.toJson(new ResultVo(leaderShiftVoList)));
	}

	/**
	 * 没有detail时默认添加
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result defaultAddDetail() {
		logger.info("+++++Receive From Return++++++++defaultAddDetail+++++start++++++++");
		JsonNode body = request().body().asJson();
		List<PlanItemDetailVo> piDetailVoList = new ArrayList<PlanItemDetailVo>();
		if (body != null) {
			if (SessionSearchUtil.searchWarehouse() == null) {
				logger.error(">>>>>>>>>>>defaultAddDetail session is null!");
				return ok(play.libs.Json.toJson(new ResultVo(piDetailVoList)));
			} else {
				String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
				String warehouseName = SessionSearchUtil.searchWarehouse().nameKey;
				PlanItemVo planItemVo = Json.fromJson(body, PlanItemVo.class);
				BigDecimal qtyPallet = new BigDecimal(0);// 生成策略
				BigDecimal remindPallert = new BigDecimal(0);// 需要用策略处理的量
				if (planItemVo.planItemId == null) {
					logger.error("+++++Receive From Return++++++++defaultAddDetail+++++Can not find the PlanItem!++++++++");
					return ok(play.libs.Json.toJson(new ResultVo("error", "Can not find the PlanItem!")));
				} else {
					List<PlanItemDetail> planItemDetailList = PlanItemDetail.find().where().eq("deleted", false).eq("planItem.deleted", false)
							.eq("planItem.id", planItemVo.planItemId).eq("planItem.order.warehouse.deleted", false)
							.eq("planItem.order.warehouse.id", warehouseId).findList();
					if (planItemDetailList.size() > 0) {
						return ok(play.libs.Json.toJson(new ResultVo("error", "This plan has benn operated, Please refresh the page!")));
					} else {
						// 直接按策略生成
						PlanItem planItemd = PlanItem.find().where().eq("deleted", false).eq("orderItem.deleted", false)
								.eq("orderItem.order.warehouse.id", warehouseId).eq("id", planItemVo.planItemId).findUnique();
						if (planItemd != null) {
							if (ExtUtil.unapply(planItemd.orderItem.ext).get("qtyPerPallet") != null) {
								qtyPallet = new BigDecimal(ExtUtil.unapply(planItemd.orderItem.ext).get("qtyPerPallet"));
							} else {
								logger.error("+++++Receive From Return++++++++addDetail+++qty/pallet of the PI in the PI Management page is null!");
								return ok(play.libs.Json.toJson(new ResultVo("error",
										"Please fill the qty/pallet for the PI in the PI Management page!")));
							}
							remindPallert = planItemVo.piSKU;
							int i = 1;
							while (remindPallert.doubleValue() > qtyPallet.doubleValue()) {
								PlanItemDetailVo planItemDetailVo = new PlanItemDetailVo();
								planItemDetailVo = planItemDetailVo.addVo(String.valueOf(i), planItemVo, warehouseName, warehouseId, qtyPallet);
								PlanItemDetail planItemDetail = planItemDetailVo.addDetail(planItemDetailVo, planItemd, i++);
								planItemDetailVo = planItemDetailVo.addVoExt(planItemDetail, planItemDetailVo);
								piDetailVoList.add(planItemDetailVo);
								remindPallert = remindPallert.subtract(qtyPallet);
							}
							if (remindPallert != new BigDecimal(0)) {
								PlanItemDetailVo planItemDetailVo = new PlanItemDetailVo();
								planItemDetailVo = planItemDetailVo.addVo(String.valueOf(i), planItemVo, warehouseName, warehouseId, remindPallert);
								PlanItemDetail planItemDetail = planItemDetailVo.addDetail(planItemDetailVo, planItemd, i);
								planItemDetailVo = planItemDetailVo.addVoExt(planItemDetail, planItemDetailVo);
								piDetailVoList.add(planItemDetailVo);
							}
							planItemd.itemStatus = PLANSTATUS_2;
							planItemd.plan.planStatus = PLANSTATUS_2;
							CrudUtil.update(planItemd);
							CrudUtil.update(planItemd.plan);
						} else {
							logger.error("+++++Receive From Return++++++++defaultAddDetail+++++Can not find the PlanItem in database++++++++");
							return ok(play.libs.Json.toJson(new ResultVo("error", "Can not find PlanItem!!")));
						}
					}
					logger.info("+++++Receive From Return++++++++defaultAddDetail(orderByStockNo)+++++end++++++++");
					return ok(play.libs.Json.toJson(new ResultVo(orderByStockNo(piDetailVoList))));
				}
			}
		} else {
			logger.error("+++++++Receive From Return+++++++defaultAddDetail++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo(piDetailVoList)));
		}
	}

	/**
	 * 页面做添加（detail表中有的数据查出，未达到数量的就先自动生成后带出头部分）
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result addDetail() {
		logger.info("+++++Receive From Return++++++++addDetail+++++start++++++++");
		JsonNode body = request().body().asJson();
		List<PlanItemDetailVo> piDetailVoList = new ArrayList<PlanItemDetailVo>();
		if (body != null) {
			if (SessionSearchUtil.searchWarehouse() == null) {
				logger.error(">>>>>>>>>>>addDetail session is null!");
				return ok(play.libs.Json.toJson(new ResultVo(piDetailVoList)));
			} else {
				String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
				String warehouseName = SessionSearchUtil.searchWarehouse().nameKey;
				PlanItemVo planItemVo = Json.fromJson(body, PlanItemVo.class);
				BigDecimal qtyPallet = new BigDecimal(0);// 生成策略
				if (planItemVo.planItemId == null) {
					logger.error("+++++Receive From Return++++++++addDetail+++++Can not find the PlanItem!++++++++");
					return ok(play.libs.Json.toJson(new ResultVo("error", "Can not find the PlanItem!")));
				} else {
					List<PlanItemDetail> planItemDetailList = PlanItemDetail.find().where().eq("deleted", false).eq("planItem.deleted", false)
							.eq("planItem.id", planItemVo.planItemId).eq("planItem.order.warehouse.deleted", false)
							.eq("planItem.order.warehouse.id", warehouseId).findList();
					if (planItemDetailList.size() > 0) {
						// 先组装已经保存detail的数据
						for (PlanItemDetail piDetail : planItemDetailList) {
							boolean isexection = Execution.find().where().eq("deleted", false).eq("planItemDetail.id", piDetail.id).findList().size() > 0 ? true
									: false;
							PlanItemDetailVo planItemDetailVo = new PlanItemDetailVo();
							planItemDetailVo.filledVo(piDetail, isexection);
							piDetailVoList.add(planItemDetailVo);
						}
						// 需要按生成策略来生成
						PlanItem planItemd = PlanItem.find().where().eq("deleted", false).eq("orderItem.deleted", false)
								.eq("id", planItemVo.planItemId).eq("orderItem.order.warehouse.id", warehouseId)
								.eq("orderItem.order.warehouse.deleted", false).findUnique();
						if (planItemd != null) {
							OrderItem orderItem = OrderItem.find().where().eq("deleted", false).eq("id", planItemd.orderItem.id.toString())
									.findUnique();
							if (orderItem != null) {
								if (ExtUtil.unapply(planItemd.orderItem.ext).get("qtyPerPallet") != null) {
									qtyPallet = new BigDecimal(ExtUtil.unapply(planItemd.orderItem.ext).get("qtyPerPallet"));
								} else {
									logger.error("+++++Receive From Return++++++++addDetail+++qty/pallet of the PI in the PI Management page is null!");
									return ok(play.libs.Json.toJson(new ResultVo("error",
											"Please fill the qty/pallet for the PI in the PI Management page!")));
								}
								int i = getNumber(planItemDetailList) + 1;
								for (int j = 0; j < planItemVo.palletCount; j++) {
									PlanItemDetailVo planItemDetailVo = new PlanItemDetailVo();
									planItemDetailVo = planItemDetailVo.addVo(String.valueOf(i), planItemVo, warehouseName, warehouseId, qtyPallet);
									PlanItemDetail planItemDetail = planItemDetailVo.addDetail(planItemDetailVo, planItemd, i++);
									planItemDetailVo = planItemDetailVo.addVoExt(planItemDetail, planItemDetailVo);
									piDetailVoList.add(planItemDetailVo);
								}
								planItemd.itemStatus = PLANSTATUS_2;
								planItemd.plan.planStatus = PLANSTATUS_2;
								CrudUtil.update(planItemd);
								CrudUtil.update(planItemd.plan);
							} else {
								logger.error("+++++Receive From Return++++++++addDetail+++++Cannot find OrderItem!++++++++");
								return ok(play.libs.Json.toJson(new ResultVo("error", "Cannot find OrderItem!")));
							}
						} else {
							logger.error("+++++Receive From Return++++++++addDetail+++++Can not find the PlanItem in database++++++++");
							return ok(play.libs.Json.toJson(new ResultVo("error", "Can not find PlanItem!!")));
						}
					} else {
						// 直接按策略生成
						PlanItem planItemd = PlanItem.find().where().eq("deleted", false).eq("orderItem.deleted", false)
								.eq("orderItem.order.warehouse.id", warehouseId).eq("id", planItemVo.planItemId).findUnique();
						if (planItemd != null) {
							if (ExtUtil.unapply(planItemd.orderItem.ext).get("qtyPerPallet") != null) {
								qtyPallet = new BigDecimal(ExtUtil.unapply(planItemd.orderItem.ext).get("qtyPerPallet"));
							} else {
								logger.error("+++++Receive From Return++++++++addDetail+++qty/pallet of the PI in the PI Management page is null!");
								return ok(play.libs.Json.toJson(new ResultVo("error",
										"Please fill the qty/pallet for the PI in the PI Management page!")));
							}
							int i = 1;
							for (int j = 0; j < planItemVo.palletCount; j++) {
								PlanItemDetailVo planItemDetailVo = new PlanItemDetailVo();
								planItemDetailVo = planItemDetailVo.addVo(String.valueOf(i), planItemVo, warehouseName, warehouseId, qtyPallet);
								PlanItemDetail planItemDetail = planItemDetailVo.addDetail(planItemDetailVo, planItemd, i++);
								planItemDetailVo = planItemDetailVo.addVoExt(planItemDetail, planItemDetailVo);
								piDetailVoList.add(planItemDetailVo);
							}
							planItemd.itemStatus = PLANSTATUS_2;
							planItemd.plan.planStatus = PLANSTATUS_2;
							CrudUtil.update(planItemd);
							CrudUtil.update(planItemd.plan);
						} else {
							logger.error("+++++Receive From Return++++++++addDetail+++++Can not find the PlanItem in database++++++++");
							return ok(play.libs.Json.toJson(new ResultVo("error", "Can not find PlanItem!!")));
						}
					}
					logger.info("+++++Receive From Return++++++++addDetail(orderByStockNo)+++++end++++++++");
					return ok(play.libs.Json.toJson(new ResultVo(orderByStockNo(piDetailVoList))));
				}
			}
		} else {
			logger.error("+++++++Receive From Return+++++++addDetail++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo(piDetailVoList)));
		}
	}

	/**
	 * 获得当前plan的最大的stockno
	 * @param planItemDetailList
	 * @return
	 */
	public static int getNumber(List<PlanItemDetail> planItemDetailList) {
		ArrayList<String> stocknos = new ArrayList<String>();
		for (PlanItemDetail planItemDetail : planItemDetailList) {
			String stockno = ExtUtil.unapply(planItemDetail.ext).get("stockno");
			stocknos.add(stockno);
		}
		int max = 0;
		for (int i = 0; i < stocknos.size(); i++) {
			if (i == 0) {
				max = Integer.valueOf(stocknos.get(0));
			} else if (max < Integer.valueOf(stocknos.get(i))) {
				max = Integer.valueOf(stocknos.get(i));
			}
		}
		return max;
	}

	/**
	 * 编辑修改Detial数据
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result updateDetail() {
		logger.info("+++++Receive From Return+++updateDetail+++++++start+++++++");
		JsonNode body = request().body().asJson();
		if (body != null) {
			if (SessionSearchUtil.searchWarehouse() == null) {
				logger.error(">>>>>>>>>>>addDetail session is null!");
				return ok(play.libs.Json.toJson(new ResultVo("error", "Please refresh page!")));
			} else {
				String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
				PlanItemDetailVo planItemDetailVo = Json.fromJson(body, PlanItemDetailVo.class);
				if (planItemDetailVo.planItemDetailId != null) {
					try {
						// 公共方法
						planItemDetailVo = planItemDetailVo.addVoExt(publicEdit(planItemDetailVo, warehouseId, true), planItemDetailVo);
						return ok(play.libs.Json.toJson(new ResultVo(planItemDetailVo)));
					} catch (CustomException e) {
						e.printStackTrace();
						return ok(play.libs.Json.toJson(new ResultVo("error", e.getMessage())));
					}
				} else {
					logger.error("+++++++Receive From Return+++++++updateDetail+++PlanItemDetail id is null");
					return ok(play.libs.Json.toJson(new ResultVo("error", "Can not get the detail's id from the page!")));
				}
			}
		} else {
			logger.error("+++++++Receive From Return+++++++updateDetail++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo("error", "Can not get the detail data from the page!")));
		}
	}

	/**
	 * 选中部分detail后default(无需页面返回)
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_LENGTH)
	public static Result defaultDetail() {
		logger.info("+++++++Receive From Return+++++++defaultDetail++++start++++++++");
		JsonNode body = request().body().asJson();
		List<PlanItemDetailVo> planItemDetailList = new ArrayList<PlanItemDetailVo>();
		if (body != null) {
			if (SessionSearchUtil.searchWarehouse() == null) {
				logger.error(">>>>>>>>>>>addDetail session is null!");
				return ok(play.libs.Json.toJson(new ResultVo("error", "Please refresh page!")));
			} else {
				String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
				ObjectMapper mapper = new ObjectMapper();
				try {
					planItemDetailList = mapper.readValue(body, new TypeReference<List<PlanItemDetailVo>>() {
					});
				} catch (Exception e) {
					e.printStackTrace();
					return ok(play.libs.Json.toJson(new ResultVo("error", "Data anomalies")));
				}
				if (planItemDetailList.size() > 0) {
					// 仅为返回前台一个更新时间
					PlanItemDetailVo foReturn = new PlanItemDetailVo();
					for (PlanItemDetailVo planItemDetailVo : planItemDetailList) {
						if (planItemDetailVo.planItemDetailId != null) {
							try {
								foReturn = foReturn.addVoExt(publicEdit(planItemDetailVo, warehouseId, false), planItemDetailVo);
							} catch (CustomException e) {
								e.printStackTrace();
								return ok(play.libs.Json.toJson(new ResultVo("error", e.getMessage())));
							}
						}
					}
					logger.info("+++++++Receive From Return+++++++defaultDetail++++end++++++++");
					return ok(play.libs.Json.toJson(new ResultVo(foReturn)));
				} else {
					logger.error("+++++++Receive From Return+++++++defaultDetail++++Can not get the details data from page!!++++++++");
					return ok(play.libs.Json.toJson(new ResultVo("error", "Can not get the details' data from the page!")));
				}
			}
		} else {
			logger.error("+++++++Receive From Return+++++++defaultDetail++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo("error", "Can not get the details data from the page!")));
		}
	}

	/**
	 * 公共Edit方法
	 * @param planItemDetailVo
	 * @param warehouseId
	 * @param type(单条执行为true)
	 * @throws CustomException
	 */
	public static PlanItemDetail publicEdit(PlanItemDetailVo planItemDetailVo, String warehouseId, boolean type) throws CustomException {
		PlanItemDetail planItemDetail = PlanItemDetail.find().where().eq("id", planItemDetailVo.planItemDetailId).eq("deleted", false).findUnique();
		isExcepitonNull(planItemDetail, "Save Failed ,Can not find the detail data in the database!!");
		List<Execution> executionList = Execution.find().where().eq("deleted", false).eq("planItemDetail.id", planItemDetail.id).findList();
		if (executionList.size() > 1) {
			throw new CustomException("Save Failed ,Detail data has been executed, please refresh page!!");
		}
		PlanItem planItem = PlanItem.find().where().eq("id", planItemDetailVo.planItemId).findUnique();
		Bin productionLine = Bin.find().where().eq("id", planItemDetailVo.productionLineId).eq("deleted", false).findUnique();
		Area area = Area.find().where().eq("id", planItemDetailVo.areaId).eq("deleted", false).findUnique();
		Bin bin = Bin.find().where().eq("id", planItemDetailVo.binId).eq("deleted", false).findUnique();
		Employee driver = Employee.find().where().eq("id", planItemDetailVo.driverId).eq("deleted", false).findUnique();
		Employee paworker = Employee.find().where().eq("id", planItemDetailVo.paworkerId).eq("deleted", false).findUnique();
		Employee leader = Employee.find().where().eq("id", planItemDetailVo.leaderId).eq("deleted", false).findUnique();
		HashMap<String, String> map = new HashMap<String, String>();
		isExcepitonNull(planItem, "Save Failed ,Did not get the PlanItem data!!");
		// isExcepitonNull(productionLine,
		// "Save Failed ,Please select the Production Line!!");
		// isExcepitonNull(area,
		// "Save Failed ,Please select the Area!!");
		// isExcepitonNull(bin,
		// "Save Failed ,Please select the Bin!!");
		// isExcepitonNull(driver,
		// "Save Failed ,Please select the Forklift Driver!!");
		// isExcepitonNull(paworker,
		// "Save Failed ,Please select the PA Worker!!");
		// isExcepitonNull(leader,
		// "Save Failed ,Please select the Leader Shift!!");
		// isExcepitonNull(planItemDetailVo.batchNo,
		// "Save Failed ,Can not find the Batch No!!");
		isExcepitonNull(planItemDetailVo.palletQty, "Save Failed ,Please fill the Quantity!!");
		isExcepitonZero(planItemDetailVo.palletQty, "Save Failed ,Must be a maximum  14-bit integers, two decimal places positive!!!");
		isExcepitonNull(planItemDetailVo.stockno, "Save Failed ,Can not find the Pallet No!!");
		planItemDetail.fromMaterialUom = planItem.fromMaterialUom;
		planItemDetail.toMaterialUom = planItem.toMaterialUom;
		if (productionLine != null) {
			planItemDetail.fromBin = productionLine;
		} else {
			planItemDetail.fromBin = null;
		}
		if (area != null) {
			planItemDetail.toArea = area;
		} else {
			planItemDetail.toArea = null;
		}
		if (bin != null) {
			planItemDetail.toBin = bin;
		} else {
			planItemDetail.toBin = null;
		}
		if (driver != null) {
			planItemDetail.assignedTo = driver;
		} else {
			planItemDetail.assignedTo = null;
		}
		planItemDetail.palnnedQty = planItemDetailVo.palletQty;
		if (paworker != null) {
			map.put("paworker", paworker.employeeName);
			map.put("paworkerId", paworker.id.toString());
		} else {
			map.put("paworker", "");
			map.put("paworkerId", "");
		}
		if (leader != null) {
			map.put("leaderId", leader.id.toString());
			map.put("leader", leader.employeeName);
		} else {
			map.put("leaderId", "");
			map.put("leader", "");
		}
		if (planItemDetailVo.productionDate != null) {
			map.put("productionDate", DateUtil.dateToStrShort(planItemDetailVo.productionDate));
		} else {
			map.put("productionDate", "");
		}
		if (planItemDetailVo.batchNo != null) {
			map.put("batchNo", planItemDetailVo.batchNo);
		} else {
			map.put("batchNo", "");
		}
		if (planItemDetailVo.blendingTank != null) {
			map.put("blendingTank", planItemDetailVo.blendingTank);
		} else {
			map.put("blendingTank", "");
		}
		if (type) {
			// 单条编辑保存时才查看stockno
			List<PlanItemDetail> planItemDetailList = PlanItemDetail.find().where().eq("deleted", false).eq("planItem.deleted", false)
					.eq("planItem.id", planItem.id).eq("planItem.order.warehouse.deleted", false).eq("planItem.order.warehouse.id", warehouseId)
					.findList();
			if (planItemDetailList.size() > 0) {
				for (PlanItemDetail Detail : planItemDetailList) {
					if (ExtUtil.unapply(Detail.ext).get("stockno").equals(planItemDetailVo.stockno)
							&& !Detail.id.toString().equals(planItemDetailVo.planItemDetailId)) {
						throw new CustomException("The pallet no has exist!");
					}
				}
			}
			map.put("stockno", planItemDetailVo.stockno);
		}
		map.put("modifiedby", SessionSearchUtil.searchUser().name);
		map.put("modifiedat", DateUtil.dateToStrLong(DateUtil.timestampToDate(DateUtil.currentTimestamp())));
		planItemDetail.detailStatus = DETATILSTATUS_NEW;
		planItemDetail.ext = ExtUtil.updateExt(planItemDetail.ext, map);
		CrudUtil.update(planItemDetail);
		return planItemDetail;
	}

	/**
	 * 删除一条Detail数据
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result deleteDetail() {
		logger.info("+++++++Receive From Return+++++++deleteDetail++++start++++++++");
		JsonNode body = request().body().asJson();
		if (body != null) {
			PlanItemDetailVo planItemdetailVo = Json.fromJson(body, PlanItemDetailVo.class);
			try {
				publicDelete(planItemdetailVo);
			} catch (CustomException e) {
				e.printStackTrace();
				return ok(play.libs.Json.toJson(new ResultVo(e.getMessage())));
			}
		} else {
			logger.error("+++++++Receive From Return+++++++deleteDetail++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo("error", "Can not get the detail data from the page!")));
		}
		logger.info("+++++++Receive From Return+++++++deleteDetail++++end++++++++");
		return ok(play.libs.Json.toJson(new ResultVo("success", "Congratulations! Delete successfully!")));
	}

	/**
	 * 批量删除
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_LENGTH)
	public static Result deleteAll() {
		logger.info("+++++++Receive From Return+++++++deleteAll++++start++++++++");
		JsonNode body = request().body().asJson();
		if (body != null) {
			ObjectMapper mapper = new ObjectMapper();
			List<PlanItemDetailVo> planItemDetailList = new ArrayList<PlanItemDetailVo>();
			try {
				planItemDetailList = mapper.readValue(body, new TypeReference<List<PlanItemDetailVo>>() {
				});
			} catch (Exception e) {
				e.printStackTrace();
				return ok(play.libs.Json.toJson(new ResultVo("error", "Data anomalies")));
			}
			if (planItemDetailList.size() > 0) {
				for (PlanItemDetailVo planItemDetailVo : planItemDetailList) {
					try {
						publicDelete(planItemDetailVo);
					} catch (CustomException e) {
						e.printStackTrace();
						return ok(play.libs.Json.toJson(new ResultVo(e.getMessage())));
					}
				}
			} else {
				logger.error("+++++++Receive From Return+++++++deleteAll++++Can not get the details data from page!!++++++++");
				return ok(play.libs.Json.toJson(new ResultVo("error", "Error : Can not get the details data!")));
			}
		} else {
			logger.error("+++++++Receive From Return+++++++deleteAll++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo("error", "Can not get the detail data from the page!")));
		}
		logger.info("+++++++Receive From Return+++++++deleteAll++++end++++++++");
		return ok(play.libs.Json.toJson(new ResultVo("success", "Congratulations! Delete successfully!")));
	}

	/**
	 * 删除操作的公共方法
	 * @param planItemdetailVo
	 * @throws CustomException
	 */
	public static void publicDelete(PlanItemDetailVo planItemdetailVo) throws CustomException {
		PlanItemDetail planItemDetail = PlanItemDetail.find().where().eq("deleted", false).eq("id", planItemdetailVo.planItemDetailId).findUnique();
		if (planItemDetail != null) {
			List<Execution> ExecutionList = Execution.find().where().eq("deleted", false).eq("planItemDetail.id", planItemDetail.id).findList();
			if (ExecutionList.size() > 0) {
				logger.error("+++++++Receive From Return+++++++deleteDetail++++the detail you delete has executed!+++");
				throw new CustomException("The detail you delete has executed!");
			}
			CrudUtil.delete(planItemDetail);
			if (planItemdetailVo.planItemId != null) {
				List<PlanItemDetail> planItemDetailList = PlanItemDetail.find().where().eq("deleted", false)
						.eq("planItem.id", planItemdetailVo.planItemId).findList();
				if (planItemDetailList.size() == 0) {
					PlanItem planItem = PlanItem.find().where().eq("id", planItemdetailVo.planItemId).findUnique();
					if (planItem != null) {
						planItem.itemStatus = PLANSTATUS_1;
						planItem.plan.planStatus = PLANSTATUS_1;
						CrudUtil.update(planItem);
						CrudUtil.update(planItem.plan);
					}
				}
			}
		} else {
			logger.error("+++++++Receive From Return+++++++deleteDetail++++this data cann't find in the database!--id is "
					+ planItemdetailVo.planItemDetailId);
			throw new CustomException("This data cann't find in the database!");
		}
	}

	/**
	 * 打印Detail
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result printDetail() {
		logger.info("+++++++Receive From Return+++++++printDetail++++start++++++++");
		JsonNode body = request().body().asJson();
		PrintVo printVo = new PrintVo();
		if (body != null) {
			PlanItemDetailVo planItemdetailVo = Json.fromJson(body, PlanItemDetailVo.class);
			if (planItemdetailVo.planItemDetailId != null) {
				PlanItem planItem = PlanItem.find().where().eq("deleted", false).eq("id", planItemdetailVo.planItemId).findUnique();
				try {
					printVo = publicPrint(planItem, planItemdetailVo);
				} catch (CustomException e) {
					e.printStackTrace();
					return ok(play.libs.Json.toJson(new ResultVo("error", e.getMessage())));
				}
			} else {
				logger.error("+++++Receive From Return+++++printDetail++++Can not find this detail data in database who's id is "
						+ planItemdetailVo.planItemDetailId);
				return ok(play.libs.Json.toJson(new ResultVo("error", "Failed : Can not find this detail data in database!")));
			}
			logger.info("+++++++Receive From Return+++++++printDetail++++end++++++++");
			return ok(play.libs.Json.toJson(new ResultVo(printVo)));
		} else {
			logger.error("+++++++Receive From Return+++++++printDetail++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo("error", "Can not get the detail data from the page!")));
		}
	}

	/**
	 * 选择性打印
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_LENGTH)
	public static Result printAllDetail() {
		logger.info("+++++++Receive From Return+++++++detailPrintall++++start++++++++");
		JsonNode body = request().body().asJson();
		List<PrintVo> printVoList = new ArrayList<PrintVo>();
		if (body != null) {
			ObjectMapper mapper = new ObjectMapper();
			List<PlanItemDetailVo> planItemDetailList = new ArrayList<PlanItemDetailVo>();
			try {
				planItemDetailList = mapper.readValue(body, new TypeReference<List<PlanItemDetailVo>>() {
				});
			} catch (Exception e) {
				e.printStackTrace();
				return ok(play.libs.Json.toJson(new ResultVo("error", "Data anomalies")));
			}
			if (planItemDetailList.size() > 0) {
				PlanItem planItem = PlanItem.find().where().eq("deleted", false).eq("id", planItemDetailList.get(0).planItemId).findUnique();
				for (PlanItemDetailVo planItemdetailVo : planItemDetailList) {
					try {
						printVoList.add(publicPrint(planItem, planItemdetailVo));
					} catch (CustomException e) {
						e.printStackTrace();
						return ok(play.libs.Json.toJson(new ResultVo("error", e.getMessage())));
					}
				}
			} else {
				logger.error("+++++++Receive From Return+++++++printAllDetail++++Can not get the details data from page!!++++++++");
				return ok(play.libs.Json.toJson(new ResultVo("error", "Can not get the details' data from the page!")));
			}
			logger.info("+++++++Receive From Return+++++++printAllDetail++++end++++++++");
			return ok(play.libs.Json.toJson(new ResultVo(printVoList)));
		} else {
			logger.error("+++++++Receive From Return+++++++printAllDetail++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo("error", "Can not get the details' data from the page!")));
		}
	}

	/**
	 * 打印公共方法
	 * @param planItem
	 * @param planItemdetailVo
	 * @return
	 * @throws CustomException
	 */
	public static PrintVo publicPrint(PlanItem planItem, PlanItemDetailVo planItemdetailVo) throws CustomException {
		PrintVo printVo = new PrintVo();
		PlanItemDetail planItemDetail = PlanItemDetail.find().where().eq("deleted", false).eq("id", planItemdetailVo.planItemDetailId).findUnique();
		if (planItemDetail != null && DETATILSTATUS_NEW.equals(planItemDetail.detailStatus)) {
			Area area = Area.find().where().eq("deleted", false).eq("id", planItemdetailVo.areaId).findUnique();
			Bin bin = Bin.find().where().eq("id", planItemdetailVo.binId).findUnique();
			Employee paworker = Employee.find().where().eq("deleted", false).eq("id", planItemdetailVo.paworkerId).findUnique();
			Employee driver = Employee.find().where().eq("deleted", false).eq("id", planItemdetailVo.driverId).findUnique();
			Employee leader = Employee.find().where().eq("deleted", false).eq("id", planItemdetailVo.leaderId).findUnique();
			isExcepitonNull(planItem, "Print Failed ,Can not find the PlanItem data in the database! Please check!");
			isExcepitonNull(area, "Print Failed ,Can not find the Area! Please check!");
			isExcepitonNull(bin, "Print Failed ,Can not find the Bin! Please check!");
			isExcepitonNull(driver, "Print Failed ,Can not find the Forklift Driver! Please check!");
			isExcepitonNull(paworker, "Print Failed ,Can not find the PA Worker! Please check!");
			isExcepitonNull(leader, "Print Failed ,Can not find the Leader Shift! Please check!");
			isExcepitonNull(planItemdetailVo.palletQty, "Print Failed ,Can not find the Quantity! Please check!");
			isExcepitonNull(planItemdetailVo.stockno, "Print Failed ,Can not find the Pallet No! Please check!");
			planItemDetail.detailStatus = DETATILSTATUS_PRINTED;
			CrudUtil.update(planItemDetail);
			// 先保存，后打印
			printVo.fillVo(planItem, planItemDetail, planItemdetailVo);
		} else if (planItemDetail != null && DETATILSTATUS_PRINTED.equals(planItemDetail.detailStatus)) {
			// 直接打印
			printVo.fillVo(planItem, planItemDetail, planItemdetailVo);
		}
		return printVo;
	}

	/**
	 * 单独执行某一条
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result execute() {
		logger.info("+++++++Receive Execution++++++execute++++start++++++++");
		JsonNode body = request().body().asJson();
		if (body != null) {
			if (SessionSearchUtil.searchWarehouse() == null) {
				logger.error(">>>>>>>>>>initWarehouse>>>> session is null");
			} else {
				String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
				PlanItemDetailVo planItemDetailVo = Json.fromJson(body, PlanItemDetailVo.class);
				if (planItemDetailVo.ifExecution == true) {
					return ok(play.libs.Json.toJson(new ResultVo("error", "Pallet [ " + planItemDetailVo.stockno + " ] has executed!")));
				} else if (StringUtils.isEmpty(planItemDetailVo.paworkerId) || StringUtils.isEmpty(planItemDetailVo.driverId)
						|| StringUtils.isEmpty(planItemDetailVo.leaderId)) {
					return ok(play.libs.Json.toJson(new ResultVo("error", "Please print Pallet [ " + planItemDetailVo.stockno
							+ " ] carton slip before execute!")));
				} else {
					PlanItem planItemMatch = PlanItem.find().where().eq("id", planItemDetailVo.planItemId).eq("deleted", false).findUnique();
					if (planItemMatch != null) {
						PlanItemDetail planItemDetail = PlanItemDetail.find().where().eq("deleted", false)
								.eq("id", planItemDetailVo.planItemDetailId).findUnique();
						if (planItemDetail != null) {
							// 换算比例（KG的数量）
							BigDecimal conversion = UnitConversion.SkuToQuantity(planItemMatch.orderItem.settlementUom.id.toString(),
									planItemMatch.materialUom.id.toString());
							BigDecimal planReceivedQty = new BigDecimal(0);// plan已执行的量（execute的才算）
							BigDecimal piReceivedQty = new BigDecimal(0);// PI已执行的量
							List<Execution> planExecutionList = Execution.find().where().eq("deleted", false).eq("planItem.id", planItemMatch.id)
									.findList();
							for (Execution execution : planExecutionList) {
								planReceivedQty = planReceivedQty.add(execution.executedQty);
							}
							planReceivedQty = planReceivedQty.add(planItemDetail.palnnedQty);// 已经execute+本次要execute
							planReceivedQty = planReceivedQty.multiply(conversion);// 已经execute,单位KG
							List<Execution> piExecutionList = Execution.find().where().eq("deleted", false)
									.eq("planItem.orderItem.id", planItemMatch.orderItem.id).eq("planItem.planType", PLANITEM_PLANTYPE).findList();
							for (Execution execution : piExecutionList) {
								piReceivedQty = piReceivedQty.add(execution.executedQty);
							}
							piReceivedQty = piReceivedQty.add(planItemDetail.palnnedQty);// 已经execute+本次要execute
							piReceivedQty = piReceivedQty.multiply(conversion);// 已经execute,单位KG
							BigDecimal maxSKU = calculateTolerance(planItemMatch).get("maxSKU").multiply(conversion);// 判断总量用最大容差
							BigDecimal minSKU = calculateTolerance(planItemMatch).get("minSKU").multiply(conversion);// 变更状态用最小容差
							BigDecimal planQty = planItemMatch.palnnedQty.multiply(conversion);// plan的计划量/单位KG
							if (planItemDetailVo.reason.trim().length() > 0
									|| (planItemDetailVo.reason.trim().length() < 1
											&& getDouble(piReceivedQty.doubleValue()) <= getDouble(maxSKU.doubleValue()) && getDouble(planReceivedQty
											.doubleValue()) <= getDouble(planQty.doubleValue()))) {
								try {
									updateDetail(planItemDetail, planItemDetailVo);
									Batch batch = new Batch();
									// 新生成batch或调用已有的batch
									batch = createBatch(planItemMatch, planItemDetail);
									publicExecution(planItemMatch, planItemDetail, batch, planItemDetailVo, warehouseId);
									logger.info(">>>>>>>>>>>>>>>>>>>>>Execute successfully!<<<<<<<<<<<<<<<<<<<<");
								} catch (CustomException e) {
									e.printStackTrace();
									return ok(play.libs.Json.toJson(new ResultVo("error", e.getMessage())));
								}
							} else if (getDouble(piReceivedQty.doubleValue()) > getDouble(maxSKU.doubleValue())
									&& planItemDetailVo.reason.trim().length() < 1) {
								BigDecimal productionQyt = planQty;
								return ok(play.libs.Json.toJson(new ResultVo("error", "PI Qty: [ " + getDouble(maxSKU.doubleValue()) + " ],\n"
										+ "Production Qty: [ " + getDouble(productionQyt.doubleValue()) + " ],\n" + "PI Received Qty: [ "
										+ getDouble(piReceivedQty.doubleValue()) + " ],\n"
										+ "PI Received Qty is bigger than PI Qty. Please state reason!")));
							} else if (getDouble(planReceivedQty.doubleValue()) > getDouble(planItemMatch.palnnedQty.multiply(conversion)
									.doubleValue()) && planItemDetailVo.reason.trim().length() < 1) {
								BigDecimal productionQyt = planQty;
								return ok(play.libs.Json.toJson(new ResultVo("error", "PI Qty: [" + getDouble(maxSKU.doubleValue()) + " ],\n"
										+ "Production Qty: [ " + getDouble(productionQyt.doubleValue()) + " ],\n" + "Plan Received Qty: [ "
										+ getDouble(planReceivedQty.doubleValue()) + " ],\n"
										+ "Plan Received Qty is bigger than Plan Production Qty. Please state reason!")));
							}
							publicChangeStatus(planItemMatch, getDouble(planQty.doubleValue()), getDouble(planReceivedQty.doubleValue()),
									getDouble(minSKU.doubleValue()), getDouble(piReceivedQty.doubleValue()));
						} else {
							logger.error("+++++Receive Execution++++++++execute+++++Can not find the PlanItemDetail in database++++++++");
							return ok(play.libs.Json.toJson(new ResultVo("error", "Can not find Detail data!!")));
						}
					} else {
						logger.error("+++++Receive Execution++++++++execute+++++Can not find the PlanItem in database++++++++");
						return ok(play.libs.Json.toJson(new ResultVo("error", "Can not find PlanItem!!")));
					}
				}
			}
		} else {
			logger.error("+++++++Receive Execution+++++++execute++++josn body in null++++++++");
		}
		logger.info("+++++++Receive Execution+++++++execute++++end++++++++");
		return ok(play.libs.Json.toJson(new ResultVo("success", "Congrations : Execute successfully!")));

	}

	/**
	 * 选择性执行Execution方法(条件为Plan_item_id)
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_LENGTH)
	public static Result executeAll() {
		logger.info("+++++Receive Execution++++++++executeAll+++++start++++++++");
		JsonNode body = request().body().asJson();
		ObjectMapper mapper = new ObjectMapper();
		List<PlanItemDetailVo> planItemDetailVoList = new ArrayList<PlanItemDetailVo>();
		if (body != null) {
			try {
				planItemDetailVoList = mapper.readValue(body, new TypeReference<List<PlanItemDetailVo>>() {
				});
			} catch (Exception e) {
				e.printStackTrace();
				return ok(play.libs.Json.toJson(new ResultVo("error", "Data anomalies")));
			}
			if (SessionSearchUtil.searchWarehouse() == null) {
				logger.error(">>>> initWarehouse>>>> session is null");
			} else {
				String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
				if (planItemDetailVoList.size() > 0) {
					PlanItem planItemMatch = PlanItem.find().where().eq("id", planItemDetailVoList.get(0).planItemId).eq("deleted", false)
							.findUnique();
					if (planItemMatch != null) {
						String number = "";
						boolean nullData = false;
						String exeNumber = "";
						boolean exeData = false;
						for (PlanItemDetailVo planItemDetailVo : planItemDetailVoList) {
							if ("N".equals(planItemDetailVo.status)) {
								nullData = true;
								number = number + planItemDetailVo.stockno + " ,";
							}
							if (planItemDetailVo.ifExecution == true && planItemDetailVo.ifSelect == true) {
								exeData = true;
								exeNumber = exeNumber + planItemDetailVo.stockno + " ,";
							}
						}
						if (nullData) {
							number = number.substring(0, number.length() - 1);
							return ok(play.libs.Json.toJson(new ResultVo("error", "Please print Pallet [ " + number
									+ " ] carton slip before execute!")));
						}
						if (exeData) {
							exeNumber = exeNumber.substring(0, exeNumber.length() - 1);
							return ok(play.libs.Json.toJson(new ResultVo("error", "Pallet [ " + exeNumber + " ] has executed!")));
						}
						// 换算比例（KG的数量）
						BigDecimal conversion = UnitConversion.SkuToQuantity(planItemMatch.orderItem.settlementUom.id.toString(),
								planItemMatch.materialUom.id.toString());
						BigDecimal planReceivedQty = new BigDecimal(0);// plan已执行的量
						BigDecimal piReceivedQty = new BigDecimal(0);// PI已执行的量
						BigDecimal currentSKU = new BigDecimal(0);// 本次执行量（SKU）
						for (PlanItemDetailVo planItemDetailVo : planItemDetailVoList) {
							PlanItemDetail planItemDetail = PlanItemDetail.find().where().eq("deleted", false)
									.eq("id", planItemDetailVo.planItemDetailId).findUnique();
							if (planItemDetail != null) {
								currentSKU = currentSKU.add(planItemDetail.palnnedQty);
							} else {
								return ok(play.libs.Json.toJson(new ResultVo("error",
										"Can not find the detail data in the database, please refresh the page!")));
							}
						}
						List<Execution> planExecutionList = Execution.find().where().eq("deleted", false).eq("planItem.id", planItemMatch.id)
								.findList();
						for (Execution execution : planExecutionList) {
							planReceivedQty = planReceivedQty.add(execution.executedQty);
						}
						planReceivedQty = planReceivedQty.add(currentSKU);// execute+本次执行的,单位：SKU
						planReceivedQty = planReceivedQty.multiply(conversion);// execute+本次执行的，单位：kg
						List<Execution> piExecutionList = Execution.find().where().eq("deleted", false)
								.eq("planItem.orderItem.id", planItemMatch.orderItem.id).eq("planItem.planType", PLANITEM_PLANTYPE).findList();
						for (Execution execution : piExecutionList) {
							piReceivedQty = piReceivedQty.add(execution.executedQty);
						}
						piReceivedQty = piReceivedQty.add(currentSKU);// execute+本次执行的，单位：SKU
						piReceivedQty = piReceivedQty.multiply(conversion);// execute+本次执行的，单位：kg
						BigDecimal maxSKU = calculateTolerance(planItemMatch).get("maxSKU").multiply(conversion);// 判断总量用最大容差/单位KG
						BigDecimal minSKU = calculateTolerance(planItemMatch).get("minSKU").multiply(conversion);// 变更状态用最小容差/单位KG
						BigDecimal planQty = planItemMatch.palnnedQty.multiply(conversion);// plan的计划量/单位KG
						// 页面中需要execution的和加上已经execution的和是否大于总的PlanItem的qty
						for (PlanItemDetailVo planItemDetailVo : planItemDetailVoList) {
							if (planItemDetailVo.reason.trim().length() > 0
									|| (planItemDetailVo.reason.trim().length() < 1
											&& getDouble(piReceivedQty.doubleValue()) <= getDouble(maxSKU.doubleValue()) && getDouble(planReceivedQty
											.doubleValue()) <= getDouble(planQty.doubleValue()))) {
								PlanItemDetail planItemDetail = PlanItemDetail.find().where().eq("deleted", false)
										.eq("id", planItemDetailVo.planItemDetailId).findUnique();
								try {
									updateDetail(planItemDetail, planItemDetailVo);
									Batch batch = new Batch();
									// 新生成batch或调用已有的batch
									batch = createBatch(planItemMatch, planItemDetail);
									publicExecution(planItemMatch, planItemDetail, batch, planItemDetailVo, warehouseId);
								} catch (CustomException e) {
									e.printStackTrace();
									return ok(play.libs.Json.toJson(new ResultVo("error", e.getMessage())));
								}
							} else if (getDouble(piReceivedQty.doubleValue()) > getDouble(maxSKU.doubleValue())
									&& planItemDetailVo.reason.trim().length() < 1) {
								BigDecimal productionQyt = planQty;
								return ok(play.libs.Json.toJson(new ResultVo("error", "PI Qty: [ " + getDouble(maxSKU.doubleValue()) + " ],\n"
										+ "Production Qty: [ " + getDouble(productionQyt.doubleValue()) + " ],\n" + "PI Received Qty: [ "
										+ getDouble(piReceivedQty.doubleValue()) + " ],\n"
										+ "PI Received Qty is bigger than PI Qty. Please state reason!")));
							} else if (getDouble(planReceivedQty.doubleValue()) > getDouble(planQty.doubleValue())
									&& planItemDetailVo.reason.trim().length() < 1) {
								BigDecimal productionQyt = planQty;
								return ok(play.libs.Json.toJson(new ResultVo("error", "PI Qty: [" + getDouble(maxSKU.doubleValue()) + " ],\n"
										+ "Production Qty: [ " + getDouble(productionQyt.doubleValue()) + " ],\n" + "Plan Received Qty: [ "
										+ getDouble(planReceivedQty.doubleValue()) + " ],\n"
										+ "Plan Received Qty is bigger than Plan Production Qty. Please state reason!")));
							}
						}
						publicChangeStatus(planItemMatch, getDouble(planQty.doubleValue()), getDouble(planReceivedQty.doubleValue()),
								getDouble(minSKU.doubleValue()), getDouble(piReceivedQty.doubleValue()));
						logger.info(">>>>>>>>>>>>>>>>>>>>>Execute successfully!<<<<<<<<<<<<<<<<<<<<");
					} else {
						logger.error("+++++Receive Execution++++++++executeAll+++++Can not find the PlanItem in database++++++++");
						return ok(play.libs.Json.toJson(new ResultVo("error", "Can not find PlanItem!!")));
					}
				} else {
					logger.error("+++++++Receive Execution+++++++executeAll++++Can not get the details data from page!!++++++++");
					return ok(play.libs.Json.toJson(new ResultVo("error", "Error : Can not get the details data!")));
				}
			}
		} else {
			logger.error("+++++++Receive Execution+++++++executeAll++++josn body in null++++++++");
		}
		logger.info("+++++++Receive Execution+++++++executeAll++++end++++++++");
		return ok(play.libs.Json.toJson(new ResultVo("success", "Congrations : Execute successfully!")));
	}

	/**
	 * double值取两位小数
	 * @param amount
	 * @return
	 */
	public static double getDouble(double amount) {
		return (double) (Math.floor(amount * 100 + 0.5) / 100);
	}

	/**
	 * 计算容差
	 * @param planItemMatch
	 * @return
	 */
	public static Map<String, BigDecimal> calculateTolerance(PlanItem planItemMatch) {
		Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
		// 容差
		if (planItemMatch.orderItem.maxPercent != null) {
			map.put("maxSKU",
					planItemMatch.orderItem.settlementQty.multiply(planItemMatch.orderItem.maxPercent.divide(new BigDecimal(100))).multiply(
							new BigDecimal(UnitConversion.quantityToSku(planItemMatch.materialUom.id.toString(),
									planItemMatch.orderItem.settlementUom.id.toString()))));
		} else {
			map.put("maxSKU",
					planItemMatch.orderItem.settlementQty.multiply(new BigDecimal(UnitConversion.quantityToSku(
							planItemMatch.materialUom.id.toString(), planItemMatch.orderItem.settlementUom.id.toString()))));
		}
		if (planItemMatch.orderItem.minPercent != null) {
			map.put("minSKU",
					planItemMatch.orderItem.settlementQty.multiply(planItemMatch.orderItem.minPercent.divide(new BigDecimal(100))).multiply(
							new BigDecimal(UnitConversion.quantityToSku(planItemMatch.materialUom.id.toString(),
									planItemMatch.orderItem.settlementUom.id.toString()))));
		} else {
			map.put("minSKU",
					planItemMatch.orderItem.settlementQty.multiply(new BigDecimal(UnitConversion.quantityToSku(
							planItemMatch.materialUom.id.toString(), planItemMatch.orderItem.settlementUom.id.toString()))));
		}
		return map;
	}

	/**
	 * 更改order orderItem plan planItem状态的方法
	 * @param planItemMatch
	 * @param planQyt plan的计划执行量
	 * @param planReceived plan的实际执行量
	 * @param piQty pi的计划执行量
	 * @param piReceived pi的实际执行量
	 */
	public static void publicChangeStatus(PlanItem planItemMatch, double planQyt, double planReceived, double piQty, double piReceived) {
		if (planReceived >= planQyt) {
			planItemMatch.itemStatus = PLANSTATUS_4;
			planItemMatch.plan.planStatus = PLANSTATUS_4;
		} else {
			planItemMatch.itemStatus = PLANSTATUS_3;
			planItemMatch.plan.planStatus = PLANSTATUS_3;
		}
		if (piReceived >= piQty) {
			// 已执行量+本次执行量之和若大于order的总量（最小容差）就算是已经执行结束了
			planItemMatch.orderItem.itemStatus = ORDERSTATUS_4;
			planItemMatch.orderItem.order.orderStatus = ORDERSTATUS_4;
		} else {
			planItemMatch.orderItem.itemStatus = ORDERSTATUS_3;
			planItemMatch.orderItem.order.orderStatus = ORDERSTATUS_3;
		}
		CrudUtil.update(planItemMatch);
		CrudUtil.update(planItemMatch.plan);
		CrudUtil.update(planItemMatch.orderItem);
		CrudUtil.update(planItemMatch.orderItem.order);
	}

	/**
	 * 提取的execution的公共方法
	 * @param planItemMatch
	 * @param planItemDetail
	 * @param batch
	 * @param planItemDetailVo
	 * @param warehouseId
	 * @throws CustomException
	 */
	public static void publicExecution(PlanItem planItemMatch, PlanItemDetail planItemDetail, Batch batch, PlanItemDetailVo planItemDetailVo,
			String warehouseId) throws CustomException {
		Warehouse warehouse = Warehouse.find().where().eq("id", planItemDetailVo.warehouseId).eq("deleted", false).findUnique();
		isExcepitonNull(warehouse, "Execute failed, Warehouse does not exist!");
		Area area = Area.find().where().eq("id", planItemDetailVo.areaId).eq("deleted", false).findUnique();
		isExcepitonNull(area, "Execute failed, Storage Area does not exist!");
		Bin bin = Bin.find().where().eq("id", planItemDetailVo.binId).eq("deleted", false).findUnique();
		isExcepitonNull(bin, "Execute failed, Storage Bin does not exist!");
		// 保存stock
		Stock stock = createStock(planItemMatch, planItemDetailVo, batch, warehouse, area, bin);
		isExcepitonNull(planItemDetail, "Execute failed, Can not find this detail data in database!");
		// 保存Execution表
		Execution execution = createExecution(planItemMatch, planItemDetail, planItemDetailVo, area, bin);
		// 保存transaction表
		StockTransaction stockTransaction = createStockTransaction(stock, execution, planItemMatch, SessionSearchUtil.searchWarehouse());
		// 与DEP的数据交互
		// DataExchangePlatform.setTransaction(stockTransaction);
		// 生成transferPlan
		createTransferPlan(planItemMatch, warehouseId, batch, planItemDetail);
	}

	/**
	 * 新生成或调用已有的batch
	 * @param planItemMatch
	 * @param planItemDetail
	 * @return
	 */
	public static Batch createBatch(PlanItem planItemMatch, PlanItemDetail planItemDetail) {
		Batch batch = new Batch();
		// 页面中需要execution的和加上已经execution的和是否大于总的PlanItem的qty
		HashMap<String, String> ext = ExtUtil.unapply(planItemMatch.ext);
		HashMap<String, String> detailExt = ExtUtil.unapply(planItemDetail.ext);
		String pi = ext.get("pi");
		String lot = detailExt.get("batchNo");
		String line = planItemDetail.fromBin.id.toString();
		String date = "";
		if (detailExt.get("productionDate") != null) {
			date = detailExt.get("productionDate");
		}
		List<PlanItemDetail> detailList = PlanItemDetail.find().where().eq("deleted", false).eq("planItem.orderItem.id", planItemMatch.orderItem.id)
				.isNotNull("assignedTo").findList();
		BigDecimal planReceivedQty = planItemDetail.palnnedQty;
		for (PlanItemDetail detail : detailList) {
			Execution execution = Execution.find().where().eq("deleted", false).eq("planItemDetail.id", detail.id).findUnique();
			if (execution != null && StringUtils.isNotEmpty(detailExt.get("batchNo")) && StringUtils.isNotEmpty(detailExt.get("productionDate"))
					&& detailExt.get("batchNo").equals(ExtUtil.unapply(detail.ext).get("batchNo"))
					&& detailExt.get("productionDate").equals(ExtUtil.unapply(detail.ext).get("productionDate"))) {
				planReceivedQty = planReceivedQty.add(detail.palnnedQty);
			}
		}
		List<Batch> batchList = BatchSearchUtil.serchBatch(lot, line, date, pi);
		if (batchList.size() == 0) {
			// 若在batch表中没有，则新建一个batch
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("lot", lot);
			map.put("line", line);
			map.put("date", date);
			map.put("pi", pi);
			map.put("qty", String.valueOf(planReceivedQty));
			batch.ext = ExtUtil.apply(map);
			batch.material = planItemMatch.material;
			CrudUtil.save(batch);
		} else if (batchList.size() >= 1) {
			if (batchList.size() > 1) {
				logger.error(">>>>>>There are more than one batch in the database, the batch id is " + batchList.get(0).id.toString());
			}
			batch = batchList.get(0);
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("qty", String.valueOf(planReceivedQty));
			batch.ext = ExtUtil.updateExt(batch.ext, map);
			CrudUtil.update(batch);
		}
		return batch;
	}

	/**
	 * 保存stock表
	 * @param planItemMatch
	 * @param planItemDetailVo
	 * @param batch
	 * @param warehouse
	 * @param area
	 * @param bin
	 * @return
	 * @throws CustomException
	 */
	public static Stock createStock(PlanItem planItemMatch, PlanItemDetailVo planItemDetailVo, Batch batch, Warehouse warehouse, Area area, Bin bin)
			throws CustomException {
		isExcepitonNull(planItemDetailVo.palletQty, "Execute failed, PalletQty does not exist!");
		isExcepitonZero(planItemDetailVo.palletQty, "Execute failed, PalletQty can not be zero!");
		Stock stock = new Stock();
		stock.warehouse = warehouse;
		stock.area = area;
		stock.bin = bin;
		stock.material = planItemMatch.material;
		stock.materialUom = planItemMatch.materialUom;
		stock.batch = batch;
		stock.qty = planItemDetailVo.palletQty;
		stock.stockStatus = STOCKSTATUS;
		stock.receivedAt = DateUtil.currentTimestamp();
		stock.arrivedAt = DateUtil.currentTimestamp();
		HashMap<String, String> extMap = new HashMap<String, String>();
		extMap.put("stockNo", planItemDetailVo.stockno);
		stock.ext = ExtUtil.apply(extMap);
		CrudUtil.save(stock);
		return stock;
	}

	/**
	 * 生成Execution
	 * @param planItemMatch
	 * @param planItemDetail
	 * @param planItemDetailVo
	 * @param area
	 * @param bin
	 * @return
	 * @throws CustomException
	 */
	public static Execution createExecution(PlanItem planItemMatch, PlanItemDetail planItemDetail, PlanItemDetailVo planItemDetailVo, Area area,
			Bin bin) throws CustomException {
		Employee driver = Employee.find().where().eq("id", planItemDetailVo.driverId).eq("deleted", false).findUnique();
		isExcepitonNull(driver, "Execute failed, Forklift Driver does not exist!");
		Employee paworker = Employee.find().where().eq("id", planItemDetailVo.paworkerId).eq("deleted", false).findUnique();
		isExcepitonNull(paworker, "Execute failed, PA Worker does not exist!");
		Employee leader = Employee.find().where().eq("id", planItemDetailVo.leaderId).eq("deleted", false).findUnique();
		isExcepitonNull(leader, "Execute failed, Leader Shift does not exist!");
		Execution execution = new Execution();
		HashMap<String, String> map = new HashMap<String, String>();
		execution.planItem = planItemMatch;
		execution.planItemDetail = planItemDetail;
		map.put("driver", driver.employeeName);
		map.put("driverId", driver.id.toString());
		map.put("paworker", paworker.employeeName);
		map.put("paworkerId", paworker.id.toString());
		map.put("leader", leader.employeeName);
		map.put("leaderId", leader.id.toString());
		execution.material = planItemMatch.material;
		execution.executedQty = planItemDetailVo.palletQty;
		execution.fromMaterialUom = planItemMatch.fromMaterialUom;
		execution.toMaterialUom = planItemMatch.toMaterialUom;
		execution.toArea = area;
		execution.toBin = bin;
		execution.executionType = EXECUTIONTYPE_1;
		execution.executionSubtype = EXECUTIONSUBTYPE_1;
		execution.executedAt = DateUtil.currentTimestamp();
		execution.ext = ExtUtil.apply(map);
		CrudUtil.save(execution);
		return execution;
	}

	/**
	 * 生成StockTransaction
	 * @param stock
	 * @param execution
	 * @param planItemMatch
	 * @param warehouse
	 * @return
	 */
	public static StockTransaction createStockTransaction(Stock stock, Execution execution, PlanItem planItemMatch, Warehouse warehouse) {
		StockTransaction stockTransaction = new StockTransaction();
		stockTransaction.warehouse = warehouse;
		stockTransaction.stock = stock;
		stockTransaction.execution = execution;
		stockTransaction.transactionCode = TRANSACTIONCODE_1;
		stockTransaction.oldAreaId = planItemMatch.toArea.id;
		stockTransaction.newUomId = planItemMatch.toMaterialUom.id;
		stockTransaction.newQty = stock.qty;
		stockTransaction.newAreaId = stock.area.id;
		stockTransaction.newBinId = stock.bin.id;
		stockTransaction.newArrivedAt = DateUtil.currentTimestamp();
		stockTransaction.newStatus = stock.stockStatus;
		stockTransaction.transactionAt = DateUtil.currentTimestamp();
		CrudUtil.save(stockTransaction);
		return stockTransaction;
	}

	/**
	 * 生成transferPlan
	 * @param planItemMatch
	 * @param warehouseId
	 */
	public static void createTransferPlan(PlanItem planItemMatch, String warehouseId, Batch batch, PlanItemDetail planItemDetail)
			throws CustomException {
		TimingPolicy timingPolicy = TimingPolicy.find().where().eq("deleted", false)
				.eq("itemPolicy.orderItem.id", planItemMatch.orderItem.id.toString()).eq("itemPolicy.orderItem.deleted", false)
				.eq("itemPolicy.orderItem.order.warehouse.id", warehouseId).findUnique();
		Map<String, Object> createMap = new HashMap<String, Object>();
		createMap.put("pi", planItemMatch.order.externalOrderNo);
		List<String> lotNoList = new ArrayList<String>();
		lotNoList.add(ExtUtil.unapply(batch.ext).get("lot"));
		createMap.put("lot", lotNoList);
		createMap.put("orderType", planItemMatch.order.orderType);
		createMap.put("line", ExtUtil.unapply(batch.ext).get("line"));
		createMap.put("date", ExtUtil.unapply(batch.ext).get("date"));
		int hour = 0;
		if (timingPolicy != null) {
			hour = timingPolicy.minHours;
			try {
				PlanItem.saveOrUpdate(createMap, new BigDecimal(ExtUtil.unapply(batch.ext).get("qty")),
						DateUtil.addHoursDate(DateUtil.strToDate(ExtUtil.unapply(planItemDetail.ext).get("productionDate"), "yyyy-MM-dd"), hour));
			} catch (CustomException e) {
				e.printStackTrace();
			}
			logger.info("There has timingPolicy, Automatically generated transferPlan!");
		} else {
			logger.info("There has no timingPolicy, does not generated transferPlan!");
		}
	}

	/**
	 * 更新detail
	 * @param planItemDetail
	 * @param planItemDetailVo
	 */
	public static void updateDetail(PlanItemDetail planItemDetail, PlanItemDetailVo planItemDetailVo) {
		HashMap<String, String> map = new HashMap<String, String>();
		if (planItemDetailVo.shift != null) {
			map.put("shift", planItemDetailVo.shift);
		} else {
			map.put("shift", "");
		}
		if (planItemDetailVo.remarks != null) {
			map.put("remarks", planItemDetailVo.remarks);
		} else {
			map.put("remarks", "");
		}
		if (planItemDetailVo.reason != null) {
			map.put("reason", planItemDetailVo.reason);
		} else {
			map.put("reason", "");
		}
		planItemDetail.ext = ExtUtil.updateExt(planItemDetail.ext, map);
		CrudUtil.update(planItemDetail);
	}

	/**
	 * 实体不为空异常
	 * @param object
	 * @param message
	 * @throws CustomException
	 */
	public static void isExcepitonNull(Object object, String message) throws CustomException {
		if (object == null) {
			throw new CustomException(message);
		}
	}

	/**
	 * qty不为零
	 * @param object
	 * @param message
	 * @throws CustomException
	 */
	public static void isExcepitonZero(BigDecimal object, String message) throws CustomException {
		if (object.compareTo(new BigDecimal(0)) == 0) {
			throw new CustomException(message);
		}
	}
}
