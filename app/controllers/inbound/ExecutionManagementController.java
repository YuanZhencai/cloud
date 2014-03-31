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

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

import models.Area;
import models.Batch;
import models.Bin;
import models.Code;
import models.Employee;
import models.Execution;
import models.PlanItem;
import models.PlanItemDetail;
import models.Stock;
import models.StockTransaction;
import models.TimingPolicy;
import models.Warehouse;
import models.vo.Result.ResultVo;
import models.vo.inbound.BinVo;
import models.vo.inbound.CreatTransferPlanVo;
import models.vo.inbound.PiStatusVo;
import models.vo.inbound.PlanItemDetailVo;
import models.vo.inbound.PlanItemSearchVo;
import models.vo.inbound.PlanItemVo;
import security.NoUserDeadboltHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import views.html.inbound.executionManagement;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: ExecutionManagementController.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
@SubjectPresent(handler = NoUserDeadboltHandler.class)
public class ExecutionManagementController extends Controller {

	private static final Logger logger = LoggerFactory.getLogger(ExecutionManagementController.class);

	static Form<PlanItemVo> planItemForm = Form.form(PlanItemVo.class);
	static Form<PlanItemSearchVo> piSearchForm = Form.form(PlanItemSearchVo.class);
	static String PRODUCTIONLINE = "Receiving";
	// *******************code and status************************
	static String PLANITEM_PLANTYPE = "T001";// 收货
	static String planItem_planSubtype = "T001.001";// 生产入库
	static String ORDERSTATUS_3 = "S002";// 执行中
	static String ORDERSTATUS_4 = "S003";// 已完成
	static String PLANSTATUS_3 = "S002";// 执行中
	static String PLANSTATUS_4 = "S003";// 已完成
	static String STOCKSTATUS = "S001";// 在库
	static String EXECUTIONTYPE_1 = "T001";// 收货
	static String EXECUTIONSUBTYPE_1 = "T001.001";// 生产入库
	static String TRANSACTIONCODE_1 = "T101";// 收货
	// 允许从前台传到后台的最大数据量
	private static final int MAX_LENGTH = 10 * 1024 * 1024;

	/**
	 * 页面数据初始化
	 * @return
	 */
	@With(Menus.class)
	public static Result index() {
		return ok(executionManagement.render(""));
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
				logger.info("+++++++Receive Execution+++++++searchlist++++start++++++++");
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
					List<String> sourceTypeList = new ArrayList<String>();
					sourceTypeList.add("T001");// SAP下载
					sourceTypeList.add("T002");// 手动生成
					ExpressionList<PlanItem> searchPlanItem = PlanItem.find().where().eq("deleted", false).eq("planType", PLANITEM_PLANTYPE)
							.eq("orderItem.deleted", false).eq("orderItem.order.deleted", false).eq("plan.deleted", false)
							.eq("materialUom.deleted", false).eq("material.deleted", false).eq("orderItem.order.warehouse.deleted", false)
							.eq("orderItem.order.warehouse.id", warehouseId).eq("toArea.storageType.nameKey", PRODUCTIONLINE)
							.eq("toArea.storageType.deleted", false).in("orderItem.order.sourceType", sourceTypeList);
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
						boolean ifExecution = false;
						PlanItemVo planItemVo = new PlanItemVo();
						planItemVo.filledVo(planItem, ifExecution);
						planItemVoList.add(planItemVo);
					}
					logger.info("+++++++Receive Execution+++++++searchlist++++end++++++++");
					return ok(play.libs.Json.toJson(new ResultVo(planItemVoList)));
				}
			}
		} else {
			logger.error("+++++++Receive Execution+++++++searchlist++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo(planItemVoList)));
		}
	}

	/**
	 * 根据Plan_Item_id查询plan_item_detail的内容
	 * @return
	 */
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result get() {
		logger.info("++++++Receive Execution+++++++detailList+++++start++++++++");
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
					logger.error("+++++++Receive Execution+++++++detailList++++Can not find the PlanItem!++++++++");
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
				logger.info("+++++++Receive Execution++++++detailList(orderByStockNo)++++end++++++++");
				return ok(play.libs.Json.toJson(new ResultVo(orderByStockNo(piDetailVoList))));
			}
		} else {
			logger.error("+++++++Receive Execution+++++++detailList++++josn body in null++++++++");
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
		logger.info("++++Receive Execution++++++sumPIQty+++++start++++++");
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
					logger.info("++++Receive Execution++++++sumPIQty+++++end++++++");
					return ok(play.libs.Json.toJson(new ResultVo(piReceivedQty)));
				} catch (CustomException e) {
					e.printStackTrace();
					return ok(play.libs.Json.toJson(new ResultVo("error", e.getMessage())));
				}
			}
		} else {
			logger.error("++++Receive Execution++++++++sumPIQty+++Can not get the warehouse data!+++++");
			return ok(play.libs.Json.toJson(new ResultVo("error", "Can not get the warehouse data!")));
		}
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
				} else if ("N".equals(planItemDetailVo.status)) {
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
		BigDecimal extra = new BigDecimal(0);
		Map<String, String> ext = ExtUtil.unapply(planItemMatch.orderItem.ext);
		String extr = ext.get("extraQty");
		if (StringUtils.isNotEmpty(extr)) {
			extra = new BigDecimal(extr);
		}
		// 容差
		if (planItemMatch.orderItem.maxPercent != null) {
			map.put("maxSKU", extra.add(
					planItemMatch.orderItem.settlementQty).multiply(planItemMatch.orderItem.maxPercent.divide(new BigDecimal(100))).multiply(
					new BigDecimal(UnitConversion.quantityToSku(planItemMatch.materialUom.id.toString(),
							planItemMatch.orderItem.settlementUom.id.toString()))));
		} else {
			map.put("maxSKU", extra.add(
					planItemMatch.orderItem.settlementQty).multiply(new BigDecimal(UnitConversion.quantityToSku(
					planItemMatch.materialUom.id.toString(), planItemMatch.orderItem.settlementUom.id.toString()))));
		}
		if (planItemMatch.orderItem.minPercent != null) {
			map.put("minSKU", extra.add(
					planItemMatch.orderItem.settlementQty).multiply(planItemMatch.orderItem.minPercent.divide(new BigDecimal(100))).multiply(
					new BigDecimal(UnitConversion.quantityToSku(planItemMatch.materialUom.id.toString(),
							planItemMatch.orderItem.settlementUom.id.toString()))));
		} else {
			map.put("minSKU", extra.add(
					planItemMatch.orderItem.settlementQty).multiply(new BigDecimal(UnitConversion.quantityToSku(
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
				throw new CustomException(e.getMessage());
			}
			logger.info("There has timingPolicy, Automatically generated transferPlan!");
		} else {
			logger.info("There has no timingPolicy, does not generated transferPlan!");
		}
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

	/**
	 * 默认当天晚上11点执行(不使用)
	 * @return
	 */
	public static Result createTransferPlan() {
		String sql = "SELECT E.plan_item_id, SUM(S.qty) as qtySum FROM t_stock S,t_execution E,t_stock_transaction R "
				+ " WHERE S.id=R.stock_id AND E.id=R.execution_id AND E.execution_type = 'receive'" + " AND S.created_at >= '"
				+ DateUtil.dateToStrShort(new Date()) + "' AND E.created_at >= '" + DateUtil.dateToStrShort(new Date())
				+ "' GROUP BY E.plan_item_id ORDER BY E.plan_item_id";
		RawSql rawSql = RawSqlBuilder.parse(sql).columnMapping("E.plan_item_id", "planItemId").create();
		Query<CreatTransferPlanVo> query = Ebean.find(CreatTransferPlanVo.class);
		query.setRawSql(rawSql);
		List<CreatTransferPlanVo> list = query.findList();
		for (CreatTransferPlanVo creatTransferPlanVo : list) {
			PlanItem planItem = PlanItem.find().where().eq("deleted", false).eq("id", creatTransferPlanVo.planItemId).findUnique();
			if (planItem != null) {
				if (SessionSearchUtil.searchWarehouse() != null) {
					String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
					TimingPolicy timingPolicy = TimingPolicy.find().where().eq("deleted", false)
							.eq("itemPolicy.orderItem.id", planItem.orderItem.id.toString()).eq("itemPolicy.orderItem.deleted", false)
							.eq("itemPolicy.orderItem.order.warehouse.id", warehouseId).findUnique();
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("pi", planItem.order.externalOrderNo);
					map.put("lot", ExtUtil.unapply(planItem.ext).get("lot"));
					map.put("orderType", planItem.order.orderType);
					map.put("line", ExtUtil.unapply(planItem.ext).get("line"));
					map.put("date", ExtUtil.unapply(planItem.ext).get("date"));
					map.put("batchDate", DateUtil.dateToStrShort(new Date()));
					try {
						PlanItem.saveOrUpdate(map, creatTransferPlanVo.qtySum, DateUtil.addHoursDate(new Date(), timingPolicy.minHours));
					} catch (CustomException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return ok(executionManagement.render(""));
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
}
