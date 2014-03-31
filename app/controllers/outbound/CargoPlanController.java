package controllers.outbound;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.LoggerFactory;

import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;

import models.Batch;
import models.Code;
import models.Execution;
import models.MaterialUom;
import models.Order;
import models.OrderItem;
import models.Plan;
import models.PlanItem;
import models.PlanItemDetail;
import models.Stock;
import models.printVo.cargoPlanReport.CargoPlanDateVo;
import models.printVo.cargoPlanReport.CargoPlanReportVo;
import models.printVo.mould.CargoPlanDate;
import models.printVo.mould.CargoPlanReport;
import models.vo.Result.ResultVo;
import models.vo.inbound.SKUUOMVo;
import models.vo.inbound.UOMVo;
import models.vo.outbound.ContainerTranforVo;
import models.vo.outbound.DaseData;
import models.vo.outbound.OrderStatus;
import models.vo.outbound.TempContainerVo;
import models.vo.outbound.TempStuffingVo;
import models.vo.outbound.TempTruckVo;
import models.vo.outbound.TruckVo;
import models.vo.outbound.batchVo;
import models.vo.outbound.cargoPlanSplitVo;
import models.vo.outbound.cargoPlanVo;
import models.vo.outbound.cargoSearchVo;
import play.data.Form;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.With;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import security.NoUserDeadboltHandler;
import utils.BatchSearchUtil;
import utils.CheckUtil;
import utils.CodeUtil;
import utils.DateUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import utils.UnitConversion;
import utils.Excel.ExcelObj;
import utils.Excel.ReadExcel;
import utils.exception.CustomException;

/*
 * cargoplanManagement
 */
@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class CargoPlanController extends Controller {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CargoPlanController.class);
	static Form<cargoSearchVo> cargoSearchForm = Form.form(cargoSearchVo.class);
	static List<Order> cargoPlans = null;
	static List<cargoPlanVo> cargoPlanVos = null;
	static List<cargoPlanSplitVo> cargoPlanSpliteVos = null;
	// static String SessionSearchUtil.searchWarehouse().id.toString() =
	// "fe640b63-5501-44b0-9fb1-6bbd93c5e8e5";
	static String orderTypeOfPi = "T001";
	static String orderTypeOfCargo = "T004";
	static String orderSourceTypeOfCargo = "T003";
	static String orderSuperTypeOfCargo = "T003.999";
	static String NEW = "S000";
	static String CONFIRMED = "S001";
	static String EXECUTE = "S002";
	static String COMPLETE = "S003";
	static String CANCEL = "S999";

	/*
	 * 载入cargoplanManagement主页
	 */
	public static Result index() {
		System.out.println("index");
		return ok(views.html.outbound.cargoPlanManagement.render(""));
	}

	public static Result StatusList() {
		List<OrderStatus> orderStatuses = new ArrayList<OrderStatus>();
		List<Code> orderStatus = CodeUtil.getOrderStatus();
		for (Code code : orderStatus) {
			OrderStatus OrderStatus = new OrderStatus();
			OrderStatus.inCode(code);
			orderStatuses.add(OrderStatus);
		}
		return ok(play.libs.Json.toJson(new ResultVo(orderStatuses)));
	}

	/*
	 * 载入cargoplanManagement数据
	 */
	public static Result list() {
		//System.out.println("============list====================");
		cargoPlanVos = new ArrayList<cargoPlanVo>();
		cargoSearchVo searchVo = new cargoSearchVo();
		RequestBody body = request().body();
		if (body.asJson() != null)
			searchVo = Json.fromJson(body.asJson(), cargoSearchVo.class);
		// searchVo searchVo = search.bindFromRequest().get();
		//System.out.println(Json.toJson(searchVo));
		List<OrderItem> orderItems = new ArrayList<OrderItem>();
		try {
			orderItems = findOrderItemList(searchVo);
		} catch (Exception e) {
			return ok(play.libs.Json.toJson(new ResultVo("error", e.getMessage())));
			// e.printStackTrace();
		}
		for (OrderItem orderItem : orderItems) {
			boolean ifAdd = true;
			if (searchVo.fromCloseDate != null && searchVo.toCloseDate != null) {
				String string = ExtUtil.unapply(orderItem.ext).get("closed_date");
				if (StringUtils.isNotEmpty(string)) {
					Date closeDate = new Date(Long.valueOf(string));
					if (searchVo.fromCloseDate.before(DateUtil.addOneDay(closeDate)) && searchVo.toCloseDate.after(DateUtil.minusOneDay(closeDate))) {
						ifAdd = true;
					} else {
						ifAdd = false;
					}
				} else {
					ifAdd = false;
				}
			}
			if (ifAdd) {
				getPIDate(orderItem);
				cargoPlanVos.add(eachOrderToCargo(orderItem));
			}
		}
		//System.out.println(cargoPlanVos.size());
		return ok(play.libs.Json.toJson(new ResultVo(cargoPlanVos)));
	}

	public static List<OrderItem> findOrderItemList(cargoSearchVo searchVo) throws Exception {
		ExpressionList<OrderItem> searchOrderItem = OrderItem.find().where().eq("deleted", false).eq("order.deleted", false)
				.eq("order.warehouse.deleted", false).eq("order.orderType", orderTypeOfCargo)
				.eq("order.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString());
		if (searchVo.piNo != null && !searchVo.piNo.equals("")) {
			searchOrderItem = searchOrderItem.like("order.internalOrderNo", "%" + searchVo.piNo + "%");
		}
		/*
		 * if (searchVo.sgPiNo != null && !searchVo.sgPiNo.equals("")) {
		 * searchOrderItem = searchOrderItem.like("order.contractNo", "%" +
		 * searchVo.sgPiNo + "%"); } if (searchVo.piStatus != null &&
		 * !searchVo.piStatus.equals("")) { searchOrderItem =
		 * searchOrderItem.like("order.orderStatus", "%" + searchVo.piStatus +
		 * "%"); }
		 */
		if (searchVo.fromCloseDate != null && searchVo.toCloseDate != null) {
			if (searchVo.fromCloseDate.after(searchVo.toCloseDate)) {
				throw new Exception("fromCloseDate can not be greater than toCloseDate,Please correct!");
			}
		} else {
			if (searchVo.fromCloseDate != null || searchVo.toCloseDate != null) {
				if (searchVo.fromCloseDate == null)
					searchVo.fromCloseDate = new Date();
				if (searchVo.toCloseDate == null)
					searchVo.toCloseDate = new Date();
			}
		}
		if (searchVo.fromDate != null && searchVo.toDate != null) {
			if (searchVo.fromDate.after(searchVo.toDate)) {
				throw new Exception("fromDate can not be greater than toDate,Please correct!");
			}
		}
		if (searchVo.fromCreateDate != null && searchVo.toCreateDate != null) {
			if (searchVo.fromCreateDate.after(searchVo.toCreateDate)) {
				throw new Exception("fromCreateDate can not be greater than toCreateDate,Please correct!");
			}
		}
		if (searchVo.fromCreateDate != null || searchVo.toCreateDate != null) {
			searchOrderItem = searchOrderItem.between("createdAt", searchVo.fromCreateDate != null ? searchVo.fromCreateDate : new Date(0),
					searchVo.toCreateDate != null ? searchVo.toCreateDate : new Date());
		}
		if (searchVo.fromUpdateDate != null && searchVo.toUpdateDate != null) {
			if (searchVo.fromUpdateDate.after(searchVo.toUpdateDate)) {
				throw new Exception("fromUpdateDate can not be greater than toUpdateDate,Please correct!");
			}
		}
		if (searchVo.fromUpdateDate != null || searchVo.toUpdateDate != null) {
			searchOrderItem = searchOrderItem.between("updatedAt", searchVo.fromUpdateDate != null ? searchVo.fromUpdateDate : new Date(0),
					searchVo.toUpdateDate != null ? searchVo.toUpdateDate : new Date());
		}
		return searchOrderItem.between(
				"order.orderTimestamp",
				DateUtil.strToDate(
						(searchVo.fromDate == null || searchVo.fromDate.equals("")) ? "2000-1-1 00:00:00" : DateUtil
								.dateToStrShort(searchVo.fromDate) + " 00:00:00", "yyyy-MM-dd HH:mm:ss"),
				DateUtil.strToDate(
						(searchVo.toDate == null || searchVo.toDate.equals("")) ? "2100-1-1 23:59:59" : DateUtil.dateToStrShort(searchVo.toDate)
								+ " 23:59:59", "yyyy-MM-dd HH:mm:ss")).findList();
	}

	public static cargoPlanVo eachOrderToCargo(OrderItem orderItem) {
		if (orderItem != null) {
			List<PlanItem> planItemTemps = PlanItem.find().fetch("orderItem")
			/*
			 * .fetch("material").fetch("fromMaterialUom")
			 * .fetch("toMaterialUom")
			 */.where().eq("planType", orderSourceTypeOfCargo)
			/*
			 * .eq("material.deleted", false) .eq("fromMaterialUom.deleted",
			 * false).eq("toMaterialUom.deleted", false)
			 */.eq("orderItem.id", orderItem.id.toString()).eq("deleted", false).eq("order.warehouse.deleted", false)
					.eq("order.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).eq("order.deleted", false)
					.eq("orderItem.deleted", false).findList();
			cargoPlanVo cargoPlanVo = new cargoPlanVo();
			cargoPlanVo.inOrder(orderItem.order);
			cargoPlanVo.inOrderItem(orderItem);
			cargoPlanVo.inPlanItem(planItemTemps);
			cargoPlanVo.getbatchs(orderItem.order.id.toString());
			return cargoPlanVo;
		} else
			return null;
	}

	/*
	 * 载入已存在订单号
	 */
	public static Result listPI() {
		List<String> piNames = new ArrayList<String>();
		List<OrderItem> orderItems = OrderItem.find().where().eq("deleted", false).eq("order.deleted", false).eq("order.orderType", orderTypeOfPi)
				.eq("order.warehouse.deleted", false).eq("order.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).findList();
		for (OrderItem orderItem : orderItems) {
			List<Order> orders = Order.find().where().eq("deleted", false).eq("id", orderItem.order.id).eq("orderType", orderTypeOfCargo).findList();
			if (orders.size() < 1)
				piNames.add(orderItem.order.internalOrderNo);
		}
		//System.out.println("piNames:" + piNames.size());
		return ok(play.libs.Json.toJson(new ResultVo(piNames)));
	}

	/*
	 * 载入单据对应物料
	 */
	public static Result listUOM(String id) {
		//System.out.println(id + "+++++++++++++++++++++++++++++++++++++++++++++++++");
		List<OrderItem> orderItems = OrderItem.find().fetch("material").where().eq("order.id", id).eq("deleted", false).eq("material.deleted", false)
				.eq("order.deleted", false).findList();
		ArrayList<UOMVo> UOMs = new ArrayList<UOMVo>();
		for (OrderItem orderItem : orderItems) {
			List<MaterialUom> materialUoms = MaterialUom.find().where().eq("material.id", orderItem.material.id).eq("material.deleted", false)
					.eq("warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).eq("warehouse.deleted", false).eq("deleted", false)
					.findList();
			for (MaterialUom materialUom : materialUoms) {
				UOMVo uomVo = new UOMVo();
				uomVo.inMaterialUom(materialUom);
				UOMs.add(uomVo);
			}
		}
		//System.out.println("=====================UOM================" + UOMs.size());
		return ok(play.libs.Json.toJson(new ResultVo(UOMs)));
	}

	public static Result getBatchQty(String id, String Batch) {
		Order order = Order.find().where().eq("deleted", false).eq("id", id).eq("warehouse.id", SessionSearchUtil.searchWarehouse().id).findUnique();
		String pi = order.internalOrderNo;
		Map<String, Double> result = new HashMap<String, Double>();
		result.put("TotalQty", Double.valueOf(0));
		result.put("reserveQty", Double.valueOf(0));
		//System.out.println("PI:" + pi);
		List<PlanItem> planItems = PlanItem.find().where().eq("orderItem.order.internalOrderNo", pi).eq("deleted", false)
				.eq("orderItem.order.deleted", false).eq("orderItem.order.warehouse.deleted", false).eq("planType", "T003")
				.eq("orderItem.order.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).findList();
		//System.out.println("orderItems.size():" + planItems.size());
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("pi", pi);
		map.put("lot", Batch);
		List<Batch> batchs = BatchSearchUtil.serchBatch(map);
		for (Batch batch : batchs) {
			double Sum = 0;
			List<Stock> stocks = Stock.find().where().eq("deleted", false).eq("warehouse.id", SessionSearchUtil.searchWarehouse().id)
					.eq("batch.id", batch.id).findList();
			for (Stock stock : stocks) {
				Sum = Sum + stock.qty.doubleValue();
			}
			result.put("TotalQty", Double.valueOf(result.get("TotalQty").doubleValue() + Sum));
		}
		double Sum = 0;
		for (PlanItem planItem : planItems) {
			String lotNo = ExtUtil.unapply(planItem.ext).get("lot");
			if (lotNo != null && lotNo.equals(Batch)) {
				Sum += planItem.palnnedQty.doubleValue();
			}
		}
		result.put("reserveQty", Double.valueOf(result.get("reserveQty").doubleValue() + Sum));
		return ok(play.libs.Json.toJson(new ResultVo(result)));
	}

	public static batchVo getBatchSum(String id, batchVo Batch) {
		Order order = Order.find().where().eq("deleted", false).eq("id", id).eq("warehouse.id", SessionSearchUtil.searchWarehouse().id).findUnique();
		String pi = order.internalOrderNo;
		Map<String, Double> result = new HashMap<String, Double>();
		result.put("TotalQty", Double.valueOf(0));
		result.put("reserveQty", Double.valueOf(0));
		//System.out.println("PI:" + pi);
		List<PlanItem> planItems = PlanItem.find().where().eq("orderItem.order.internalOrderNo", pi).ne("orderItem.order.id", id)
				.eq("deleted", false).eq("orderItem.order.deleted", false).eq("orderItem.order.warehouse.deleted", false).eq("planType", "T003")
				.eq("orderItem.order.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).findList();
		//System.out.println("orderItems.size():" + planItems.size());
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("pi", pi);
		map.put("lot", Batch.batchNo);
		List<Batch> batchs = BatchSearchUtil.serchBatch(map);
		for (Batch batch : batchs) {
			double Sum = 0;
			List<Stock> stocks = Stock.find().where().eq("deleted", false).eq("warehouse.id", SessionSearchUtil.searchWarehouse().id)
					.eq("batch.id", batch.id).findList();
			for (Stock stock : stocks) {
				Sum = Sum + stock.qty.doubleValue();
			}
			result.put("TotalQty", Double.valueOf(result.get("TotalQty").doubleValue() + Sum));
		}
		double Sum = 0;
		for (PlanItem planItem : planItems) {
			List<Execution> findList = Execution.find().where().eq("planItem.id", planItem.id.toString()).eq("deleted", false).findList();
			if (findList.size() < 1) {
				String lotNo = ExtUtil.unapply(planItem.ext).get("lot");
				if (lotNo != null && lotNo.equals(Batch.batchNo)) {
					Sum += planItem.palnnedQty.doubleValue();
				}
			}
		}
		result.put("reserveQty", Double.valueOf(result.get("reserveQty").doubleValue() + Sum));
		Batch.reserveQty = Sum;
		Batch.SumQty = result.get("TotalQty");
		return Batch;
	}

	/*
	 * 载入Order对应BatchNo
	 */
	public static Result listBatch(String id) {
		//System.out.println(id + "+++++++++++++++++++++++++++++++++++++++++++++++++");
		List<OrderItem> orderItems = OrderItem.find().where().eq("order.id", id).eq("deleted", false).eq("order.deleted", false)
				.eq("order.warehouse.deleted", false).eq("order.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).findList();
		//System.out.println("orderItems.size():" + orderItems.size());
		List<batchVo> Result = new ArrayList<batchVo>();
		HashMap<String, batchVo> batchVos = new HashMap<String, batchVo>();
		for (OrderItem orderItem : orderItems) {
			// List<Batch> batchs =
			// Batch.find().where().eq("material",orderItem.material
			// ).eq("material.owner.warehouse.id",
			// SessionSearchUtil.searchWarehouse().id.toString()).eq("material.owner.warehouse.deleted",
			// false).like("parent.batchNo",
			// orderItem.order.internalOrderNo).eq("deleted",false).findList();
			List<Batch> batchs = BatchSearchUtil.getBatch(orderItem.order.internalOrderNo);
			//System.out.println("batch.size" + batchs.size());
			// System.out.println(batchs.get(0).ext);
			for (Batch batch : batchs) {
				if (!batchVos.containsKey(batch.batchNo)) {
					batchVo batchVo = new batchVo();
					batchVo.batchNo = batch.batchNo;
					batchVos.put(batch.batchNo, batchVo);
				}
			}
		}
		//System.out.println("=====================batchVos================" + batchVos.size());
		Set<String> keySet = batchVos.keySet();
		for (String string : keySet) {
			batchVo batchVo = batchVos.get(string);
			batchVo = getBatchSum(id, batchVo);
			Result.add(batchVo);
		}
		return ok(play.libs.Json.toJson(new ResultVo(Result)));
	}

	/*
	 * 载入Order对应SKUUOM
	 */
	public static Result listSKUUOM(String id) {
		List<OrderItem> orderItems = OrderItem.find().fetch("material").where().eq("order.id", id).findList();
		ArrayList<SKUUOMVo> UOMVos = new ArrayList<SKUUOMVo>();
		// System.out.println(orderItems.size());
		for (OrderItem orderItem : orderItems) {
			List<MaterialUom> materialUoms = MaterialUom.find().where().eq("material", orderItem.material)
					.eq("warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).eq("deleted", false).eq("material.deleted", false)
					.eq("warehouse.deleted", false).findList();
			for (MaterialUom materialUom : materialUoms) {
				SKUUOMVo skuuomVo = new SKUUOMVo();
				skuuomVo.inMaterialUom(materialUom);
				UOMVos.add(skuuomVo);
			}
		}
		//System.out.println("===============SKUUOM======================" + UOMVos.size());
		return ok(play.libs.Json.toJson(new ResultVo(UOMVos)));
	}

	/*
	 * 载入order对应数据的 总数量、物料单位、containerNo
	 */
	public static Result listBaseData(String id) {
		DaseData daseData = new DaseData();
		//System.out.println("===============baseDate===============================");
		List<OrderItem> orderItems = OrderItem.find().fetch("settlementUom").where().eq("order.id", id).eq("deleted", false)
				.eq("order.deleted", false).findList();
		if (orderItems.size() > 0) {
			OrderItem orderItem = orderItems.get(0);
			orderItem = getPIDate(orderItem);
			if (orderItem != null) {
				daseData = new DaseData();
				daseData.material = orderItem.material;
				// ArrayList<DaseData> daseDate = new ArrayList<DaseData>();
				daseData.qty = orderItem.settlementQty.doubleValue();
				UOMVo uomVo = new UOMVo();
				uomVo.inMaterialUom(orderItem.settlementUom);
				daseData.setExt(orderItem.ext);
				daseData.UOM = uomVo;
				//System.out.println(uomVo.uomCode);
			}
		}
		// System.out.println("===============SKUUOM======================"+daseDate.size());
		return ok(play.libs.Json.toJson(new ResultVo(daseData)));

	}

	/*
	 * lotNo的情况下，此代码不需要
	 */
	public static Result sumStockQty(String id) {
		//System.out.println("===============stockQty========================");
		List<Stock> stocks = Stock.find().where().eq("deleted", false).eq("batch.id", id).eq("batch.deleted", false).findList();
		double sumStockQty = 0;
		for (Stock stock : stocks) {
			sumStockQty = sumStockQty + stock.qty.doubleValue();
		}
		//System.out.println("============sumStockQty==============" + sumStockQty);
		return ok(play.libs.Json.toJson(new ResultVo(sumStockQty)));
	}

	public static String isLive(Map<String, String> map) {
		List<OrderItem> orderCargo = OrderItem.find().where().eq("order.internalOrderNo", map.get("PI"))
				.eq("order.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).eq("order.warehouse.deleted", false)
				.eq("order.orderType", orderTypeOfCargo).eq("deleted", false).findList();
		for (OrderItem orderItem : orderCargo) {
			String string = ExtUtil.unapply(orderItem.ext).get("splitShipment");
			if (string != null && string.equals(map.get("splitShipment")))
				return orderItem.order.id.toString();
			else {
				if ((StringUtils.isEmpty(string)) && (StringUtils.isEmpty(map.get("splitShipment"))))
					return orderItem.order.id.toString();
			}
		}
		return null;
	}

	/*
	 * 新增order action
	 */
	@Transactional
	public static Result save() {
		//System.out.println("==================save===================");
		RequestBody body = request().body();
		if (body.asJson() != null) {
			cargoPlanVo cargoPlanVo = Json.fromJson(body.asJson(), cargoPlanVo.class);
			// 提交正常，执行添加操作
			// System.out.println(play.libs.Json.toJson(cargoPlanVo));
			try {
				Map<String, String> map = new HashMap<String, String>();
				map.put("PI", cargoPlanVo.piNo);
				map.put("splitShipment", cargoPlanVo.splitShipment);
				if (isLive(map) != null) {
					return ok(play.libs.Json.toJson(new ResultVo("error", "This cargoPlan  exit")));
				} else {
					cargoPlanVo = eachSave(cargoPlanVo);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return ok(play.libs.Json.toJson(new ResultVo("error", e.getMessage())));
			}
			// System.out.println("123");
			return ok(play.libs.Json.toJson(new ResultVo(cargoPlanVo)));
		}
		return ok(play.libs.Json.toJson(new ResultVo("error", "can not find data")));
	}

	@Transactional
	public static OrderItem getPIDate(OrderItem orderItem) {
		// System.out.println(orderItem.settlementQty);
		if (orderItem.settlementQty != null && orderItem.settlementQty.doubleValue() != 0)
			return orderItem;
		// System.out.println("223");
		List<Order> orderList = Order.find().where().eq("internalOrderNo", orderItem.order.internalOrderNo)
				.eq("warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).eq("warehouse.deleted", false).eq("orderType", orderTypeOfPi)
				.eq("deleted", false).findList();
		// System.out.println(orderList.get(0).id);
		if (orderList.size() < 1)
			return null;
		else {
			List<OrderItem> orderItems = OrderItem.find().fetch("material").where().eq("order.warehouse.deleted", false)
					.eq("material.deleted", false).eq("order.deleted", false)
					.eq("order.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).eq("order.id", orderList.get(0).id.toString())
					.findList();
			// System.out.println(orderItem.settlementQty+"=============================================");
			for (OrderItem temp : orderItems) {
				//System.out.println("begin update orderItme");
				orderItem.material = temp.material;
				// orderItem.materialUom = temp.materialUom;
				// orderItem.qty = temp.qty;
				orderItem.settlementQty = temp.settlementQty;
				orderItem.remarks = temp.remarks;
				// System.out.println(temp.settlementQty+"##############################################");
				orderItem.settlementUom = temp.settlementUom;
				orderItem.maxPercent = temp.maxPercent;
				orderItem.minPercent = temp.minPercent;
				HashMap<String, String> unapply = ExtUtil.unapply(temp.ext);
				HashMap<String, String> map = ExtUtil.unapply(orderItem.ext);
				Set<String> keySet = unapply.keySet();
				for (String string : keySet) {
					map.put(string, unapply.get(string));
				}
				orderItem.ext = ExtUtil.apply(map);
				orderItem.updatedAt = DateUtil.currentTimestamp();
				orderItem.updatedBy = SessionSearchUtil.searchUser().id;
				// orderItem.order = temp.order;
			}
			// System.out.println(orderItem.settlementQty+"++++++++++++++++++++++++++++++++++++++++++++++++++");
			orderItem.update();
			//System.out.println("orderItem save");
			return orderItem;
		}
	}

	/*
	 * 新增order 调用数据库
	 */
	@Transactional
	public static cargoPlanVo eachSave(cargoPlanVo cargoPlanVo) throws Exception {
		cargoPlanVo result = null;
		/*
		 * List<Order> orderList = Order.find().where().eq("internalOrderNo",
		 * cargoPlanVo.piNo) .eq("warehouse.id",
		 * SessionSearchUtil.searchWarehouse
		 * ().id.toString()).eq("warehouse.deleted", false).eq("orderType",
		 * orderTypeOfPi) .eq("deleted", false).findList(); if (orderList.size()
		 * < 1) throw new Exception("piNo can't find");
		 */
		/*
		 * List<Order> orderCargo = Order.find().where().eq("internalOrderNo",
		 * cargoPlanVo.piNo) .eq("warehouse.id",
		 * SessionSearchUtil.searchWarehouse
		 * ().id.toString()).eq("warehouse.deleted", false).eq("orderType",
		 * orderTypeOfCargo) .eq("deleted", false).findList(); if
		 * (orderCargo.size() > 0) throw new Exception("piNo has exit");
		 */
		// System.out.println(orderList.size());
		// for (Order order : orderList) {
		Order orderSave = new Order();
		orderSave.contractNo = cargoPlanVo.refNo;
		// if (order.externalOrderNo != null) orderSave.externalOrderNo =
		// order.externalOrderNo;
		orderSave.internalOrderNo = cargoPlanVo.piNo;
		orderSave.warehouse = SessionSearchUtil.searchWarehouse();
		orderSave = cargoPlanVo.outOrder(orderSave);
		orderSave.orderStatus = NEW;
		orderSave.orderType = orderTypeOfCargo;
		orderSave.sourceType = orderSourceTypeOfCargo;
		orderSave.createdAt = DateUtil.currentTimestamp();
		orderSave.createdBy = SessionSearchUtil.searchUser().id;
		orderSave.updatedAt = DateUtil.currentTimestamp();
		orderSave.updatedBy = SessionSearchUtil.searchUser().id;
		orderSave.orderTimestamp = DateUtil.currentTimestamp();
		// orderSave.updatedAt=DateUtil.currentTimestamp();
		orderSave.save();
		cargoPlanVo.inOrder(orderSave);
		/*
		 * OrderItem orderItemOld =
		 * OrderItem.find().fetch("material").fetch("materialUom").where()
		 * .eq("order.warehouse.deleted", false).eq("material.deleted",
		 * false).eq("materialUom.deleted", false) .eq("order.deleted",
		 * false).eq("order.warehouse.id",
		 * SessionSearchUtil.searchWarehouse().id.toString()).eq("order.id",
		 * orderList.get(0).id) .eq("deleted", false).findUnique();
		 */
		OrderItem orderItemSave = new OrderItem();
		/*
		 * orderItemSave.material = orderItemOld.material;
		 * orderItemSave.materialUom = orderItemOld.materialUom;
		 * orderItemSave.qty = orderItemOld.qty; orderItemSave.settlementQty =
		 * orderItemOld.settlementQty; orderItemSave.settlementUom =
		 * orderItemOld.settlementUom; orderItemSave.maxPercent =
		 * orderItemOld.maxPercent; orderItemSave.minPercent =
		 * orderItemOld.minPercent; orderItemSave.ext = orderItemOld.ext;
		 */
		orderItemSave.order = orderSave;
		orderItemSave = cargoPlanVo.outOrderItem(orderItemSave, null);
		orderItemSave.createdAt = DateUtil.currentTimestamp();
		orderItemSave.createdBy = SessionSearchUtil.searchUser().id;
		orderItemSave.updatedAt = DateUtil.currentTimestamp();
		orderItemSave.updatedBy = SessionSearchUtil.searchUser().id;
		orderItemSave.save();
		getPIDate(orderItemSave);
		cargoPlanVo.inOrderItem(orderItemSave);
		cargoPlanVo.getbatchs(orderItemSave.order.id.toString());
		result = cargoPlanVo;
		return result;
	}

	/*
	 * 删除order
	 */
	@Transactional
	public static Result delete(String id) {
		/*
		 * List<Plan> plans = Plan.find().where().eq("order.id",
		 * id).eq("order.warehouse.deleted", false).eq("order.warehouse.id",
		 * SessionSearchUtil.searchWarehouse().id.toString()).eq("deleted",
		 * false).eq("order.deleted", false).findList(); if(plans!=null){
		 */
		List<OrderItem> orderItems = OrderItem.find().where().eq("order.warehouse.deleted", false)
				.eq("order.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).eq("order.id", id).eq("deleted", false)
				.eq("order.deleted", false)
				/* .eq("order.orderStatus", NEW) */.findList();
		if (orderItems.size() < 1)
			return ok(play.libs.Json.toJson(new ResultVo("error", "data had been deleted")));
		for (OrderItem orderItem : orderItems) {
			if (!orderItem.order.orderStatus.equals(NEW))
				return ok(play.libs.Json.toJson(new ResultVo("error", "This one had create plan")));
			orderItem.deleted = true;
			orderItem.updatedAt = DateUtil.currentTimestamp();
			orderItem.updatedBy = SessionSearchUtil.searchUser().id;
			orderItem.update();
		}
		List<Order> orders = Order.find().where().eq("id", id).eq("warehouse.id", SessionSearchUtil.searchWarehouse().id.toString())
				.eq("deleted", false).eq("orderStatus", NEW).findList();
		if (orders.size() < 1)
			return ok(play.libs.Json.toJson(new ResultVo("error", "data had been deleted")));
		for (Order order : orders) {
			order.deleted = true;
			order.updatedAt = DateUtil.currentTimestamp();
			order.updatedBy = SessionSearchUtil.searchUser().id;
			order.update();
		}
		return ok(play.libs.Json.toJson(new ResultVo("success", "data had deleted")));
		/*
		 * }else{ return badRequest("data had been deleted"); }
		 */

	}

	/*
	 * 数据批量上传
	 */
	@Transactional
	public static Result upload() {
		List<cargoPlanVo> result = new ArrayList<cargoPlanVo>();
		MultipartFormData body = request().body().asMultipartFormData();
		List<FilePart> files = body.getFiles();
		// FilePart file = body.getFile("file");
		// System.out.println(files.get(0).getFilename());
		// int fileNumber=0;
		for (FilePart file : files) {
			// fileNumber++;
			//System.out.println("\\\\\\\\\\\\\\\\\\\\\file\\\\\\\\\\\\\\\\\\\\\\\\\\");
			if (file != null) {
				String fileName = file.getFilename();
				// String contentType = file.getContentType();
				File excel = file.getFile();
				List<cargoPlanVo> readExcels = (List<cargoPlanVo>) ReadExcel.readExcel(new cargoPlanVo(), excel, fileName);
				// System.out.println("cargoPlanVos.size"+readExcels.size());
				int i = 0;
				// System.out.println(play.libs.Json.toJson(readExcels));
				if (readExcels == null)
					return ok(play.libs.Json.toJson(new ResultVo("error", "File:" + fileName + ",Data Type Error")));
				for (cargoPlanVo cargoPlanVo : readExcels) {
					i++;
					try {
						//System.out.println(cargoPlanVo.piNo.equals(""));
						if (cargoPlanVo.piNo != null && !cargoPlanVo.piNo.equals("")&& StringUtils.isNotEmpty(cargoPlanVo.piNo)) {
							//System.out.println("start save");
							Map<String, String> map = new HashMap<String, String>();
							map.put("PI", cargoPlanVo.piNo);
							map.put("splitShipment", cargoPlanVo.splitShipment);
							cargoPlanVo eachSave = null;
							String id = null;
							if ((id = isLive(map)) != null) {
								cargoPlanVo.id = id;
								eachSave = UploadUpdate(cargoPlanVo);
							} else {
								eachSave = eachSave(cargoPlanVo);
							}
							// System.out.println("closedTiem ::+++::"+cargoPlanVo.closedTime);
							if (eachSave == null)
								return ok(play.libs.Json.toJson(new ResultVo("error", "File:" + fileName + ",No." + i + " Data Error!")));
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return ok(play.libs.Json.toJson(new ResultVo("error", "File:" + fileName + ",No." + i + " " + e.getMessage())));
					}
					// System.out.println(play.libs.Json.toJson(cargoPlanVo));
				}
				result.addAll(readExcels);
			}
		}
		if (files.size() == 0)
			return ok(play.libs.Json.toJson(new ResultVo("error", "Please select a file")));
		else {
			return ok(play.libs.Json.toJson(new ResultVo(result)));
		}
	}

	/*
	 * 数据更新
	 */
	@Transactional
	public static Result update() {
		//System.out.println("=====================update================");
		RequestBody body = request().body();
		//System.out.println(body);
		ResultVo resultVo = new ResultVo("success", "Update Success");
		if (body.asJson() != null) {
			cargoPlanVo cargoPlanVo = Json.fromJson(body.asJson(), cargoPlanVo.class);
			//System.out.println(cargoPlanVo.fcl);
			/*
			 * List<Order> orderCargo =
			 * Order.find().where().eq("internalOrderNo", cargoPlanVo.piNo)
			 * .eq("warehouse.id",
			 * SessionSearchUtil.searchWarehouse().id.toString
			 * ()).eq("warehouse.deleted", false).eq("orderType",
			 * orderTypeOfCargo) .eq("deleted", false).findList(); if
			 * (orderCargo.size() >
			 * 0&&!orderCargo.get(0).id.toString().equals(cargoPlanVo.id))
			 * return ok(play.libs.Json.toJson(new
			 * ResultVo("error","piNo has exit")));
			 */
			try {
				resultVo.Data = eachUpdate(cargoPlanVo);
			} catch (Exception e) {
				return ok(play.libs.Json.toJson(new ResultVo("error", e.getMessage())));
			}
			return ok(play.libs.Json.toJson(resultVo));
		}
		// return
		// redirect(controllers.outbound.routes.CargoPlanControllers.list());
		return ok(play.libs.Json.toJson(new ResultVo("error", "Can't Find Data")));
	}

	@Transactional
	public static cargoPlanVo eachUpdate(cargoPlanVo cargoPlanVo) throws Exception {
		List<OrderItem> OrderItemList = OrderItem.find().where().eq("order.id", cargoPlanVo.id).eq("deleted", false)
				.eq("order.warehouse.deleted", false).eq("order.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString())
				.eq("order.deleted", false).findList();
		if (OrderItemList.size() < 1)
			throw new Exception("pino can't find");
		for (OrderItem orderItem : OrderItemList) {
			orderItem = cargoPlanVo.outOrderItem(orderItem, orderItem);
			Order orderTemp = cargoPlanVo.outOrder(orderItem.order);
			orderTemp.updatedAt = DateUtil.currentTimestamp();
			orderTemp.updatedBy = SessionSearchUtil.searchUser().id;
			if(NEW.equals(orderTemp.orderStatus)){
				orderTemp.orderStatus=CONFIRMED;
			}
			orderTemp.update();
			orderItem.updatedAt = DateUtil.currentTimestamp();
			orderItem.updatedBy = SessionSearchUtil.searchUser().id;
			orderItem.update();
			getPIDate(orderItem);
			cargoPlanVo.inOrder(orderTemp);
			cargoPlanVo.inOrderItem(orderItem);
			cargoPlanVo.getbatchs(orderItem.order.id.toString());
		}
		return cargoPlanVo;
	}

	@Transactional
	public static cargoPlanVo UploadUpdate(cargoPlanVo cargoPlanVo) throws Exception {
		List<OrderItem> OrderItemList = OrderItem.find().where().eq("order.id", cargoPlanVo.id).eq("deleted", false)
				.eq("order.warehouse.deleted", false).eq("order.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString())
				.eq("order.deleted", false).findList();
		if (OrderItemList.size() < 1)
			throw new Exception("pino can't find");
		for (OrderItem orderItem : OrderItemList) {
			orderItem = cargoPlanVo.outUploadOrderItem(orderItem, orderItem);
			Order orderTemp = cargoPlanVo.outOrder(orderItem.order);
			orderTemp.updatedAt = DateUtil.currentTimestamp();
			orderTemp.updatedBy = SessionSearchUtil.searchUser().id;
			orderTemp.update();
			orderItem.updatedAt = DateUtil.currentTimestamp();
			orderItem.updatedBy = SessionSearchUtil.searchUser().id;
			orderItem.update();
			getPIDate(orderItem);
			cargoPlanVo.inOrderItem(orderItem);
			cargoPlanVo.getbatchs(orderItem.order.id.toString());
		}
		return cargoPlanVo;
	}

	/*
	 * ====================================================数据拆分页面================
	 * =============================================
	 */
	/*
	 * 数据拆分,获取已经存在的Plan
	 */
	public static Result getPlan(String id) {
		// String id=null;
		// cargoPlanSpliteVos = new ArrayList<cargoPlanSplitVo>();
		LinkedHashMap<String, TruckVo> Trucks = new LinkedHashMap<String, TruckVo>();
		List<PlanItem> planItemTemps = PlanItem.find().fetch("orderItem")
		/*
		 * .fetch("material").fetch("fromMaterialUom") .fetch("toMaterialUom")
		 */.where().eq("planType", orderSourceTypeOfCargo)
		/*
		 * .eq("material.deleted", false) .eq("fromMaterialUom.deleted",
		 * false).eq("toMaterialUom.deleted", false)
		 */.eq("order.id", id).eq("deleted", false).eq("planType", "T003").eq("order.warehouse.deleted", false)
				.eq("order.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).eq("order.deleted", false)
				.eq("orderItem.deleted", false).order().asc("createdAt").findList();
		for (PlanItem planItemTemp : planItemTemps) {
			// planItemTemp.planType
			/*
			 * cargoPlanSplitVo cargoPlanSplitVo = new cargoPlanSplitVo();
			 * cargoPlanSplitVo.inOrder(planItemTemp.plan.order);
			 * cargoPlanSplitVo.inPlanItem(planItemTemp);
			 * if(cargoPlanSplitVo.pi_SKU
			 * !=0&&cargoPlanSplitVo.stuffing_UOM.uomCode
			 * !=null&&cargoPlanSplitVo.sku_UOM.uomCode!=null&&
			 * planItemTemp.material!=null) cargoPlanSplitVo.stuffing_Quantity =
			 * cargoPlanSplitVo.pi_SKU
			 * UnitConversion.returnComparing(cargoPlanSplitVo
			 * .stuffing_UOM.uomCode, cargoPlanSplitVo.sku_UOM.uomCode,
			 * planItemTemp.material.id.toString());
			 * cargoPlanSpliteVos.add(cargoPlanSplitVo);
			 */
			TruckVo truckVo = new TruckVo();
			truckVo.inPlanItem(planItemTemp);
			if (Trucks.containsKey(String.valueOf(truckVo.Truck))) {
				Trucks.get(String.valueOf(truckVo.Truck)).addTruck(truckVo);
			} else {
				Trucks.put(String.valueOf(truckVo.Truck), truckVo);
			}
		}
		List<TempTruckVo> tempTruckVos = new ArrayList<TempTruckVo>();
		for (String key : Trucks.keySet()) {
			TempTruckVo tempTruckVo = new TempTruckVo();
			tempTruckVo.initTruck(Trucks.get(key).Truck, Trucks.get(key).StuffingDate, Trucks.get(key).Canedit, Trucks.get(key).orderId);
			for (String key2 : Trucks.get(key).containers.keySet()) {
				TempContainerVo tempContainerVo = new TempContainerVo();
				tempContainerVo.initContainer(Trucks.get(key).containers.get(key2).container, Trucks.get(key).containers.get(key2).SealNo);
				for (String key3 : Trucks.get(key).containers.get(key2).batchs.keySet()) {
					TempStuffingVo tempStuffingVo = new TempStuffingVo();
					tempStuffingVo.initVo(Trucks.get(key).containers.get(key2).batchs.get(key3).batch,
							Trucks.get(key).containers.get(key2).batchs.get(key3).Qty, Trucks.get(key).containers.get(key2).batchs.get(key3).id);
					tempContainerVo.batchs.add(tempStuffingVo);
				}
				tempTruckVo.containers.add(tempContainerVo);
			}
			tempTruckVos.add(tempTruckVo);
		}
		//System.out.println("cargoPlanSpliteVos:" + tempTruckVos.size());
		return ok(play.libs.Json.toJson(new ResultVo(tempTruckVos)));
	}

	/*
	 * 删除已经保存的Plan
	 */
	@Transactional
	public static void deleteSplit(String id) throws Exception {
		PlanItem planItem = PlanItem.find().where().eq("deleted", false).eq("id", id).findUnique();
		if (planItem==null)
			throw new Exception("Delete Failed");
		
			List<PlanItemDetail> planItemDetails = PlanItemDetail.find().where().eq("planItem.id", planItem.id.toString())
					.eq("planItem.order.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).eq("planItem.deleted", false)
					.eq("planItem.order.warehouse.deleted", false).eq("deleted", false).findList();
			for (PlanItemDetail planItemDetail : planItemDetails) {
				planItemDetail.deleted = true;
				planItemDetail.updatedAt = DateUtil.currentTimestamp();
				planItemDetail.updatedBy = SessionSearchUtil.searchUser().id;
				planItemDetail.update();
			}
			planItem.deleted = true;
			planItem.updatedAt = DateUtil.currentTimestamp();
			planItem.updatedBy = SessionSearchUtil.searchUser().id;
			planItem.update();
			planItem.plan.deleted=true;
			planItem.plan.updatedAt=DateUtil.currentTimestamp();
			planItem.plan.updatedBy=SessionSearchUtil.searchUser().id;
			planItem.plan.update();
		List<PlanItem> planItems = PlanItem.find().where().eq("deleted", false).eq("orderItem.id", planItem.orderItem.id.toString()).findList();
		if (planItems.size() == 0) {
			planItem.order.orderStatus = NEW;
			planItem.order.updatedAt = DateUtil.currentTimestamp();
			planItem.order.updatedBy = SessionSearchUtil.searchUser().id;
			planItem.order.update();
		}
	}

	public static OrderItem getOrderBy(String id) {
		List<OrderItem> orderItem = OrderItem.find().where().eq("deleted", false).eq("order.deleted", false).eq("order.id", id).findList();
		if (orderItem.size() > 0)
			return orderItem.get(0);
		else
			return null;
	}

	/*
	 * 保存拆分之后的数据
	 */
	@Transactional
	public static Result savePlans(String id) {
		ResultVo result = new ResultVo("success", "Save Success");
		//System.out.println("=========saveplans==========");
		JsonNode body = request().body().asJson();
		if (body != null) {
			List<TempTruckVo> TempTruckVos = null;
			try {
				TempTruckVos = new ObjectMapper().readValue(body, new TypeReference<List<TempTruckVo>>() {
				});
			} catch (Exception e) {
				e.printStackTrace();
				return ok(play.libs.Json.toJson(new ResultVo("error", "data error")));
			}
			if (TempTruckVos.size() < 1) {
				//System.out.println("Deleting all");
				List<PlanItem> planItems = PlanItem.find().where().eq("deleted", false).eq("order.warehouse.deleted", false)
						.eq("order.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).eq("planType", "T003")
						.eq("orderItem.order.id", id).findList();
				for (PlanItem planItem : planItems) {
					try {
						deleteSplit(planItem.id.toString());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				ResultVo resultVo = new ResultVo("success", "Deleted All Success");
				resultVo.Data = eachOrderToCargo(getOrderBy(id));
				//System.out.println("Delelted");
				return ok(play.libs.Json.toJson(resultVo));
			}
			/*
			 * double Sum = 0; for (cargoPlanSplitVo cargoPlanSplitVo :
			 * cargoPlanSplitVos) { Sum += cargoPlanSplitVo.stuffing_Quantity; }
			 * Order byId = Order.find().byId(cargoPlanSplitVos.get(0).orderId);
			 * 
			 * List<OrderItem> orderItems =
			 * OrderItem.find().where().eq("order.internalOrderNo",
			 * byId.internalOrderNo) .eq("order.deleted",
			 * false).eq("order.orderType",orderTypeOfCargo).eq("deleted",
			 * false).findList(); //System.out.println("orderItems.size():" +
			 * orderItems.size()); double plansum = 0; for(OrderItem
			 * orderItem:orderItems){
			 * //System.out.println(orderItem.order.id.toString
			 * ().equals(cargoPlanSplitVos.get(0).orderId)); //
			 * if(orderItem.order
			 * .id.toString().equals(cargoPlanSplitVos.get(0).orderId)) break;
			 * //OrderItem orderItem = orderItems.get(0); List<PlanItem>
			 * planItems =
			 * PlanItem.find().fetch("fromMaterialUom").fetch("toMaterialUom"
			 * ).fetch("material").where().eq("deleted",
			 * false).eq("order.warehouse.deleted", false)
			 * .eq("order.warehouse.id",
			 * SessionSearchUtil.searchWarehouse().id.toString
			 * ()).eq("planType","T003").eq("orderItem.id",
			 * orderItem.id).findList(); for (PlanItem planItem : planItems) {
			 * if(!planItem.orderItem.order.id.equals(byId.id)) plansum =
			 * plansum +
			 * planItem.palnnedQty.doubleValue()*UnitConversion.returnComparing
			 * (planItem.fromMaterialUom.uomCode,
			 * planItem.toMaterialUom.uomCode, planItem.material.id.toString());
			 * }
			 * 
			 * }
			 * //System.out.println(orderItems.get(0).settlementQty.doubleValue()
			 * ); //System.out.println(Sum); //System.out.println(plansum); if
			 * (orderItems.get(0).settlementQty.doubleValue() - Sum - plansum <
			 * 0) { return ok(play.libs.Json.toJson(new
			 * ResultVo("error","Split Failed,Qty exceed!"))); }
			 */
			// ?存在transfor情况下，还可以删除么？
			// //System.out.println(play.libs.Json.toJson(TempTruckVos));
			List<TempStuffingVo> StuffingVos = new ArrayList<TempStuffingVo>();
			List<String> keys = new ArrayList<String>();
			for (TempTruckVo tempTruckVo : TempTruckVos) {
				if (tempTruckVo.containers.size() > 0) {
					for (TempContainerVo tempContainerVo : tempTruckVo.containers) {
						tempContainerVo.TruckVo = tempTruckVo;
						if (tempContainerVo.batchs.size() > 0) {
							HashMap<String, TempStuffingVo> tempbatchs = new HashMap<String, TempStuffingVo>();
							for (TempStuffingVo stuffingVo : tempContainerVo.batchs) {
								stuffingVo.ContainVo = tempContainerVo;
								if (!tempbatchs.containsKey(stuffingVo.batch)) {
									StuffingVos.add(stuffingVo);
									tempbatchs.put(stuffingVo.batch, stuffingVo);
									if (stuffingVo.id != null)
										keys.add(stuffingVo.id);
								} else {
									// StuffingVos.indexOf(tempbatchs.get(stuffingVo.batch))
									tempbatchs.get(stuffingVo.batch).Qty += stuffingVo.Qty;
								}
							}
						} else {
							TempStuffingVo tempStuffingVo = new TempStuffingVo();
							tempStuffingVo.ContainVo = tempContainerVo;
						}
					}
				} else {
					TempStuffingVo tempStuffingVo = new TempStuffingVo();
					tempStuffingVo.ContainVo = new TempContainerVo();
					tempStuffingVo.ContainVo.TruckVo = tempTruckVo;
				}
			}
			if (StuffingVos.size() > 0) {
				List<PlanItem> planItems = PlanItem.find().where().eq("deleted", false).eq("order.warehouse.deleted", false).eq("planType", "T003")
						.eq("order.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString())
						.eq("orderItem.order.id", StuffingVos.get(0).ContainVo.TruckVo.orderId).findList();
				//System.out.println("planItemSize:" + planItems.size());
				for (PlanItem planItem : planItems) {
					if (!keys.contains(planItem.id.toString())) {
						try {
							deleteSplit(planItem.id.toString());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}
			}
			HashMap<String, ContainerTranforVo> tranforVos = new HashMap<String, ContainerTranforVo>();
			for (TempStuffingVo tempStuffingVo : StuffingVos) {
				PlanItem TempplanItem = null;
				if (tempStuffingVo.id != null) {
					TempplanItem = updatePlanSplit(tempStuffingVo);
				} else {
					TempplanItem = addCargoPlanSplitVo(tempStuffingVo);
					logger.info("CargoPlan been Created");
				}
				if (TempplanItem != null && CheckUtil.TransfCanCreateCheck(TempplanItem)) {
					//System.out.println("TransFerPlan been Created");
					ContainerTranforVo containerTranforVo = new ContainerTranforVo(TempplanItem);
					if (tranforVos.containsKey(containerTranforVo.Key)) {
						tranforVos.get(containerTranforVo.Key).isAdd(containerTranforVo);
						//System.out.println("add:" + tranforVos.get(containerTranforVo.Key).qty);
					} else {
						tranforVos.put(containerTranforVo.Key, containerTranforVo);
						//System.out.println("new:" + containerTranforVo.qty);
					}
				}
				//System.out.println(TempplanItem.ext);
			}

			for (String key : tranforVos.keySet()) {
				ContainerTranforVo containerTranforVo = tranforVos.get(key);
				//System.out.println(play.libs.Json.toJson(containerTranforVo));
				result = CallTranforPlan(containerTranforVo);
			}
			result.Data = play.libs.Json.toJson(eachOrderToCargo(getOrderBy(id)));
			// System.out.println(TempplanItem.ext);
			return ok(play.libs.Json.toJson(result));
		}
		return ok(play.libs.Json.toJson(new ResultVo("error", "can not be null")));
	}

	@Transactional
	public static PlanItem updatePlanSplit(TempStuffingVo tempStuffingVo) {
		PlanItem result = null;
		List<PlanItem> planItems = PlanItem.find().where().eq("deleted", false).eq("orderItem.order.warehouse.deleted", false)
				.eq("orderItem.order.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).eq("planType", "T003")
				.eq("id", tempStuffingVo.id).findList();
		for (PlanItem planItem : planItems) {
			/*
			 * List<PlanItemDetail> planItemDetails = PlanItemDetail.
			 * find().where().eq("planItem.order.warehouse.id",
			 * SessionSearchUtil
			 * .searchWarehouse().id.toString()).eq("planItem.id", planItem
			 * .id.toString()).eq("deleted",false).findList(); for
			 * (PlanItemDetail planItemDetail : planItemDetails) {
			 * planItemDetail.deleted=true;
			 * planItemDetail.updatedAt=DateUtil.currentTimestamp();
			 * planItemDetail.update(); }
			 */
			planItem.palnnedQty = new BigDecimal(tempStuffingVo.Qty);
			planItem = tempStuffingVo.outPlanItem(planItem);
			planItem.updatedAt = DateUtil.currentTimestamp();
			planItem.updatedBy = SessionSearchUtil.searchUser().id;
			// planItem.update();
			// planItem.update();
			Ebean.update(planItem);
			// System.out.println(play.libs.Json.toJson(planItem.ext));
			//System.out.println("CargoPlan been Updated");
			result = planItem;
		}
		return result;
	}

	/*
	 * 拆分数据保存，生成对应数据
	 */
	@Transactional
	public static PlanItem addCargoPlanSplitVo(TempStuffingVo tempStuffingVo) {
		List<OrderItem> orderItemTemps = OrderItem.find().where().eq("order.id", tempStuffingVo.ContainVo.TruckVo.orderId)
				.eq("order.warehouse.deleted", false).eq("order.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString())
				.eq("deleted", false).eq("order.deleted", false).findList();
		PlanItem Result = null;
		for (OrderItem orderItem : orderItemTemps) {
			if(orderItem.order.orderStatus!=null&&orderItem.order.orderStatus.equals(NEW)){
				orderItem.order.orderStatus = CONFIRMED;
			}else if(orderItem.order.orderStatus==null){
				orderItem.order.orderStatus = CONFIRMED;
			}
			orderItem.order.updatedAt = DateUtil.currentTimestamp();
			orderItem.order.updatedBy = SessionSearchUtil.searchUser().id;
			orderItem.order.update();
			// if(Ebean.beginTransaction().isActive())
			// System.out.println(Ebean.beginTransaction().isActive());
			Plan planTemp = new Plan();
			planTemp.order = orderItem.order;
			planTemp.plannedTimestamp = DateUtil.currentTimestamp();
			planTemp.planStatus = NEW;
			planTemp.planType = orderSourceTypeOfCargo;
			planTemp.createdAt = DateUtil.currentTimestamp();
			planTemp.createdBy = SessionSearchUtil.searchUser().id;
			PlanItem planItemTemp = new PlanItem();
			planTemp.save();
			// planTemp.save();
			planItemTemp.plan = planTemp;
			planItemTemp.planType = orderSourceTypeOfCargo;
			planItemTemp.itemStatus = NEW;
			planItemTemp.maxPercent = orderItem.maxPercent;
			planItemTemp.minPercent = orderItem.minPercent;
			// planItemTemp.plannedExecutionAt = DateUtil.currentTimestamp();
			planItemTemp.planSubtype = orderSuperTypeOfCargo;
			planItemTemp.material = orderItem.material;
			planItemTemp.createdAt = DateUtil.currentTimestamp();
			planItemTemp.orderItem = orderItem;
			planItemTemp.order = orderItem.order;
			planItemTemp.palnnedQty = new BigDecimal(tempStuffingVo.Qty);
			planItemTemp = tempStuffingVo.outPlanItem(planItemTemp);
			planItemTemp.createdAt = DateUtil.currentTimestamp();
			planItemTemp.createdBy = SessionSearchUtil.searchUser().id;
			// planItemTemp.save();
			planItemTemp.save();
			Result = planItemTemp;
			// CallTranforPlan(planItemTemp);
			// orderItem.order.orderStatus = CONFIRMED;
			// orderItem.order.updatedAt = DateUtil.currentTimestamp();

		}
		return Result;
	}

	public static ResultVo CallTranforPlan(ContainerTranforVo containerTranforVo) {
		// PlanItem.saveOrUpdate(planItem.batch, planItem.palnnedQty,
		// planItem.plannedExecutionAt, orderTypeOfCargo,
		// null,planItem.id.toString());
		// 调用transfor方法
		try {
			PlanItem.saveOrUpdate(containerTranforVo.map, containerTranforVo.qty, containerTranforVo.StuffingDate);
		} catch (CustomException e) {
			// TODO Auto-generated catch block
			return new ResultVo("error", e.getMessage());
			// e.printStackTrace();
		}
		return new ResultVo("success", "TransferPlan success Create!");
	}

	/*
	 * 查询数据,计算单位之间换算action
	 */
	public static Result compute() {
		//System.out.println("Compute");
		RequestBody body = request().body();
		cargoPlanSplitVo vo = new cargoPlanSplitVo();
		if (body.asJson() != null) {
			vo = Json.fromJson(body.asJson(), cargoPlanSplitVo.class);
		}
		//System.out.println("123123" + play.libs.Json.toJson(vo));
		if (vo.stuffing_UOM != null && vo.sku_UOM != null) {
			OrderItem orderItem = OrderItem.find().fetch("material").where().eq("order.warehouse.deleted", false).eq("material.deleted", false)
					.eq("order.deleted", false).eq("order.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString())
					.eq("order.id", vo.orderId).findUnique();
			if (orderItem.material == null) {
				return ok(play.libs.Json.toJson(new ResultVo("error", "Can not find material")));
			}
			return ok(play.libs.Json.toJson(new ResultVo(UnitConversion.returnComparing(vo.stuffing_UOM.uomCode, vo.sku_UOM.uomCode,
					orderItem.material.id.toString()))));
		} else {
			return ok(play.libs.Json.toJson(new ResultVo("error", "failed!")));
		}
	}

	public static Result Mould() {
		ArrayList<CargoPlanReport> StuffingReports = new ArrayList<CargoPlanReport>();
		CargoPlanReport stuffingReport = new CargoPlanReport();
		stuffingReport.setSheetName("sheet1");
		List<ExcelObj> stuffingDates = new ArrayList<ExcelObj>();
		stuffingDates.add(new CargoPlanDate());
		stuffingReport.setDates(stuffingDates);
		StuffingReports.add(stuffingReport);

		File file = ReadExcel.outExcel(StuffingReports, "CARGO PLAN REPORT.xlsx");
		response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");// 可选择不同类型
		response().setHeader("Content-Disposition", "attachment; filename= CARGO PLAN REPORT.xlsx");
		return ok(file);
	}

	/**
	 * 导出报表
	 * @param type
	 * @return
	 */
	public static Result report(String type) {
		//System.out.println("reprot start");
		List<CargoPlanReportVo> cargoPlanReportVoList = new ArrayList<CargoPlanReportVo>();
		if ("1".equals(type)) {
			cargoSearchVo vo = null;
			if (cargoSearchForm.hasErrors()) {
				vo = new cargoSearchVo();
			} else {
				vo = cargoSearchForm.bindFromRequest().get();
			}
			List<OrderItem> orderItems = null;
			try {
				orderItems = findOrderItemList(vo);
			} catch (Exception e) {
				return ok(play.libs.Json.toJson(new ResultVo("error", e.getMessage())));
			}
			List<CargoPlanDateVo> cargoPlanDateVoList = new ArrayList<CargoPlanDateVo>();
			for (OrderItem orderItem : orderItems) {
				boolean ifAdd = true;
				if (vo.fromCloseDate != null && vo.toCloseDate != null) {
					String string = ExtUtil.unapply(orderItem.ext).get("closed_date");
					if (StringUtils.isNotEmpty(string)) {
						Date closeDate = new Date(Long.valueOf(string));
						if (vo.fromCloseDate.before(DateUtil.addOneDay(closeDate)) && vo.toCloseDate.after(DateUtil.minusOneDay(closeDate))) {
							ifAdd = true;
						} else {
							ifAdd = false;
						}
					} else {
						ifAdd = false;
					}
				}
				if (ifAdd) {
					cargoPlanDateVoList.addAll(CargoPlanDateVo.initTruck(orderItem));
				}
			}
			CargoPlanReportVo cargoPlanReportVo = new CargoPlanReportVo();
			cargoPlanReportVo.setDates(cargoPlanDateVoList);
			cargoPlanReportVo.setSheetName("sheet1");
			cargoPlanReportVoList.add(cargoPlanReportVo);
		}
		File file = ReadExcel.outExcel(cargoPlanReportVoList, "CARGO PLAN REPORT.xlsx");
		response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");// 可选择不同类型
		response().setHeader("Content-Disposition", "attachment; filename= CARGO PLAN REPORT.xlsx");
		return ok(file);
	}
}
