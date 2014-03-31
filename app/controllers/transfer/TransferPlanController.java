package controllers.transfer;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import models.Area;
import models.Batch;
import models.Bin;
import models.EmployeeWarehouse;
import models.MaterialUom;
import models.PlanItem;
import models.PlanItemDetail;
import models.Stock;
import models.vo.transfer.MapVo;
import models.vo.transfer.PalletDetail;
import models.vo.transfer.SortUtil;
import models.vo.transfer.StockVo;
import models.vo.transfer.TPResult;
import models.vo.transfer.TPSearch;
import models.vo.transfer.TransferPlanVo;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import play.mvc.With;
import security.NoUserDeadboltHandler;
import utils.BatchSearchUtil;
import utils.CrudUtil;
import utils.EmptyUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import utils.enums.CodeKey;
import utils.exception.CustomException;
import views.html.transfer.transferPlan;
import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: OrderItemController.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@SubjectPresent(handler = NoUserDeadboltHandler.class)
public class TransferPlanController extends Controller {
	private static final Logger logger = LoggerFactory.getLogger(TransferPlanController.class);
	private static List<TPResult> tpResults = new ArrayList();
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static final String DELETED = "deleted";
	private static final String ERROR = "Error";
	// 允许从前台传到后台的最大数据量
	private static final int MAX_LENGTH = 5 * 1024 * 1024;

	/**
	 * skip to tp main view
	 * @return
	 */
	@With(Menus.class)
	public static Result index() {
		return ok(transferPlan.render(""));
	}

	/**
	 * retrieval tp that Meet the conditions
	 * @return
	 */
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result list() {
		logger.info("^^^^^^^^^^^^^^^you hava in  list method() ^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		RequestBody body = request().body();
		TPSearch tps = null;
		if (body.asJson() != null) {
			logger.info("^^^^^^^^^retrieve condition^^^^^^^" + body.asJson());
			tps = Json.fromJson(body.asJson(), TPSearch.class);
		}
		if (tps == null) {
			tps = new TPSearch();
		}
		tpResults.clear();
		if (tps.transferFromDate != null && tps.transferToDate != null && tps.transferFromDate.compareTo(tps.transferToDate) > 0) {
			return ok("fromDate can not be greater than toDate,Please correct!");
		}
		tpResults = PlanItem.list(tps, tpResults);
		//logger.info("**********play.libs.Json.toJson(retrtprs)******   " + tpResults.size() + "   *******" + play.libs.Json.toJson(tpResults));
		return ok(play.libs.Json.toJson(tpResults));
	}

	/**
	 * persist  tansport paln to db;
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result save() {
		logger.info("^^^^^^^^^^^have in saveOrUpdate method^^^^^^^^^");
		RequestBody body = request().body();
		TransferPlanVo tpVo = new TransferPlanVo();
		if (body.asJson() != null) {
			logger.info("************ body.asJson() *************" + body.asJson());
			Map<String, Object> map = new HashMap<String, Object>();
			tpVo = Json.fromJson(body.asJson(), TransferPlanVo.class);
			if (!EmptyUtil.isNotEmptyList(tpVo.batchId)) {
				return ok("batchNo is null!");
			} else if (tpVo.transferQty == null) {
				return ok("Transfer Quantity is null!");
			} else if (tpVo.transferDate == null) {
				return ok("Transfer Date is null!");
			}
			map.put("id", tpVo.id);
			map.put("pi", tpVo.pino);
			map.put("lot", tpVo.batchId);
			map.put("date", tpVo.proDate);
			//map.put("date", tpVo.proDate);
			map.put("orderType", CodeKey.ORDER_TYPE_PRODUCE.toString());
			map.put("manual", "Manual");
			try {
				PlanItem.saveOrUpdate(map, tpVo.transferQty, tpVo.transferDate);
			} catch (CustomException e) {
				e.printStackTrace();
				return ok(ERROR + e.getMessage());
			}
			logger.info("The operation was successful");
			return ok("The operation was successful");
		} else {

			return ok(ERROR + " Syestem Error");
		}

	}

	/**
	 * basic id delete corrdsponding PlanItem
	 * @param id
	 * @return
	 */
	public static Result delete(String id) {
		logger.info("^^^^^^^^^^^   have in save delete method   ^^^^^parma id is :^^^^" + id);
		PlanItem planItem = Ebean.find(PlanItem.class, id);
		CrudUtil.delete(planItem);
		return ok("you have deleted success");
	}

	/**
	 * 
	 * @return
	 */
	@Transactional
	public static Result update() {
		return TODO;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public static Result listPallets(String id) {
		logger.info("**********  listPallets  ****************" + id);
		List<PalletDetail> pallets = new ArrayList<PalletDetail>();
		int i = 0;
		PlanItem planItem = PlanItem.find().where().eq("deleted", false).eq("id", id).findUnique();
		if (planItem != null) {
			List<PlanItemDetail> pids = Ebean.find(PlanItemDetail.class)
				.where().join("stock", "qty")
				.where().join("stock.area", "nameKey")
				.where().join("stock.bin", "nameKey")
				.where().join("stock.materialUom")
				.where().eq("deleted", false).eq("planItem", planItem)
				.order("updatedAt ascending").findList();
			if (pids != null && !pids.isEmpty()) {
				for (PlanItemDetail pid : pids) {
					PalletDetail pd = PalletDetail.getPalletDetail(pid);
					pallets.add(pd);
				}
			}
		}
		try {
			pallets = SortUtil.sortList(pallets);
		} catch (CustomException e) {
			e.printStackTrace();
		}
		//logger.info("^^^^^^^PalletDetail  toJson^^^^^^^" + play.libs.Json.toJson(pallets));
		return ok(play.libs.Json.toJson(pallets));

	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	@Transactional
	public static Result listStocks(String id) {
		List<StockVo> stockVos = new ArrayList<StockVo>();
		logger.info("**********  listStocks  ****************" + id);
		PlanItem planItem = PlanItem.find().where().eq("deleted", false).eq("id", id).findUnique();
		HashMap<String, String> unapply = ExtUtil.unapply(planItem.ext);
		List<Batch> batchs  = new ArrayList<Batch>();
		HashMap<String,String> map = ExtUtil.unapply(planItem.ext);
		String lot = map.get("lot");
		String[] lots = lot.split("[,]");
	   for(int i =0;i<lots.length;i++){
		   map.put("lot", lots[i]);
		   List<Batch> tempBatchs = BatchSearchUtil.serchBatch(map);
		   if(EmptyUtil.isNotEmptyList(tempBatchs))batchs.addAll(tempBatchs); 
	   }
		if (batchs != null && !batchs.isEmpty()) {
			for (Batch batch : batchs) {
				List<Stock> stocks = Ebean.find(Stock.class).where().eq("deleted", false).eq("batch.id", batch.id).order("updatedAt ascending")
					.findList();
				if (stocks != null && !stocks.isEmpty()) {
					for (Stock stock : stocks) {
						StockVo stockVo = StockVo.getStockVo(stock);
						stockVos.add(stockVo);
					}
				}
			}
			//logger.info("^^^^^^^PalletDetail  toJson^^^^^^^" + play.libs.Json.toJson(stockVos));
			try {
				stockVos = SortUtil.sortList(stockVos);
			} catch (CustomException e) {
				e.printStackTrace();
			}
			return ok(play.libs.Json.toJson(stockVos));
		} else {
			return ok("this plan no stock");
		}

	}

	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_LENGTH)
	public static Result savePallets() {
		logger.info("^^^^^^^^ you have in savePallets method ^^^^^^^^^^^^^^");
		// 这个状态表明有detail生成
		boolean stockChange = false;
		StringBuffer sb = new StringBuffer("The plan detail");
		RequestBody body = request().body();
		List<PalletDetail> pds = null;
		if (body.asJson() != null) {
			logger.info("*******PalletDetail  body.asJson() ********" + body.asJson());
			ObjectMapper mapper = new ObjectMapper();
			try {
				pds = mapper.readValue(body.asJson(), new TypeReference<List<PalletDetail>>() {
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (pds != null && !pds.isEmpty()) {
				PlanItem planItem = Ebean.find(PlanItem.class).where().eq("id", pds.get(0).planItemId).findUnique();
				planItem.itemStatus = CodeKey.PLAN_STATUS_CONFIRM.toString();
				CrudUtil.update(planItem);
				for (PalletDetail pd : pds) {
					try {
						PalletDetail.getPlanItemDetail(pd, planItem);
					} catch (Exception e) {
						stockChange = true;
						sb.append(e.getMessage());
					}
				}
				if (stockChange) {
					return ok(sb.append("  not mapping stock!").toString());
				}
			}

		}
		return ok("The operation was successful");

	}

	// ------------------------------------------------------util
	// function--------------------------------------

	/**
	 * 
	 * @param batchId
	 * @return
	 */
	public static Result getFromAreas(String batchId) {
		List<Stock> stocks = Ebean.find(Stock.class).fetch("batch", "id").where().eq("deleted", false).eq("batch.id", batchId).findList();
		List<MapVo> areas = new ArrayList<MapVo>();
		if (stocks != null && !stocks.isEmpty()) {
			Set<UUID> areaIds = new HashSet<UUID>();
			for (Stock stock : stocks) {
				int i = areaIds.size();
				areaIds.add(stock.area.id);
				if (areaIds.size() > i) {
					MapVo area = new MapVo();
					area.key = String.valueOf(stock.area.id);
					area.descripton = stock.area.nameKey;
					areas.add(area);
				}
			}
		}
		return ok(play.libs.Json.toJson(areas));
	}

	/**
	 * 
	 * @param whNameKey
	 * @return
	 */
	public static Result getToAreas() {
		List<Area> areaEntitys = Ebean.find(Area.class).fetch("warehouse", "nameKey").where().eq("deleted", false).eq("warehouse", SessionSearchUtil.searchWarehouse())
			.order("nameKey ascending").findList();
		List<MapVo> areas = new ArrayList<MapVo>();
		if (areaEntitys != null && !areaEntitys.isEmpty()) {
			for (Area areaEntity : areaEntitys) {
				MapVo area = new MapVo();
				area.key = String.valueOf(areaEntity.id);
				area.descripton = areaEntity.nameKey;
				areas.add(area);
			}
		}
		return ok(play.libs.Json.toJson(areas));
	}

	/**
	 * 
	 * @param whNameKey
	 * @return
	 */
	public static Result getEmployee() {
		List<EmployeeWarehouse> employWhs = Ebean.find(EmployeeWarehouse.class).fetch("employee").fetch("employee.employeeType")
			.fetch("warehouse", "nameKey").where().where().eq("deleted", false).eq("warehouse", SessionSearchUtil.searchWarehouse())
			.eq("employee.employeeType.typeCode", CodeKey.EMPLOY_TYPE_FORKLIFTDRIVE.toString()).eq("employee.deleted", false)
			.order("employee.employeeName ascending").findList();
		List<MapVo> areas = new ArrayList<MapVo>();
		if (employWhs != null && !employWhs.isEmpty()) {
			for (EmployeeWarehouse ew : employWhs) {
				MapVo area = new MapVo();
				area.key = String.valueOf(ew.employee.id);
				area.descripton = ew.employee.employeeName;
				areas.add(area);
			}
		}
		return ok(play.libs.Json.toJson(areas));
	}

	/**
	 * 
	 * @param areaNameKey
	 * @return
	 */
	public static Result getFromBins(String areaNameKey) {
		logger.info("^^^^^^^^^^^^you have in getBins method^^^^^^^^^^^" + areaNameKey);
		String[] str = areaNameKey.split("&");
		List<Stock> stocks = Ebean.find(Stock.class).fetch("area").fetch("batch").where().eq("deleted", false).eq("area.nameKey", str[0])
			.eq("batch.id", str[1]).order("nameKey ascending").findList();
		Set<UUID> binIds = new HashSet<UUID>();
		List<MapVo> bins = new ArrayList<MapVo>();
		if (stocks != null && !stocks.isEmpty()) {
			for (Stock stock : stocks) {
				int i = binIds.size();
				binIds.add(stock.bin.id);
				if (binIds.size() > i) {
					MapVo bin = new MapVo();
					bin.key = String.valueOf(stock.bin.id);
					bin.descripton = stock.bin.nameKey;
					bins.add(bin);
				}
			}
		}
		//logger.info("^^^^^^^^^^^^frombins messages^^^^^^  " + play.libs.Json.toJson(bins));
		return ok(play.libs.Json.toJson(bins));
	}

	/**
	 * 
	 * @param areaNameKey
	 * @return
	 */
	public static Result getTomBins(String areaNameKey) {
		logger.info("^^^^^^^^^^^^you have in getBins method^^^^^^^^^^^" + areaNameKey);

		List<Bin> binEntitys = Ebean.find(Bin.class).fetch("area").where().eq("deleted", false).eq("area.nameKey", areaNameKey).findList();
		List<MapVo> bins = new ArrayList<MapVo>();
		if (binEntitys != null && !binEntitys.isEmpty()) {
			for (Bin binEntity : binEntitys) {
				MapVo bin = new MapVo();
				bin.key = String.valueOf(binEntity.id);
				bin.descripton = binEntity.nameKey;
				bins.add(bin);

			}
		}
		//logger.info("^^^^^^^^^^^^tobins messages^^^^^^  " + play.libs.Json.toJson(bins));
		return ok(play.libs.Json.toJson(bins));
	}

	/**
	 * 
	 * @param binNameKey
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result getStocks() {
		logger.info("^^^^^^^^^^^^you have in getStocks method^^^^^^^^^^^^^^^^^param you transfer is  ^^^^^^^");
		RequestBody body = request().body();
		@SuppressWarnings("rawtypes")
		// 这个list存放前台中已有pallet的qty 和 binNameKey
		List qtysAndBinNameKey;
		Set<BigDecimal> qtys = new HashSet<BigDecimal>();
		if (body.asJson() != null) {
			logger.info("^^^^^^^^^^^^^^qtysAndBinNameKey^^^:" + body.asJson());
			qtysAndBinNameKey = Json.fromJson(body.asJson(), List.class);
			if (qtysAndBinNameKey != null && !qtysAndBinNameKey.isEmpty()) {
				List<Stock> stockEntitys = Ebean.find(Stock.class).fetch("bin").where().eq("deleted", false)
					.eq("bin.nameKey", qtysAndBinNameKey.get(qtysAndBinNameKey.size() - 3))
					.eq("batch.id", qtysAndBinNameKey.get(qtysAndBinNameKey.size() - 2)).findList();
				List<MapVo> stocks = new ArrayList<MapVo>();
				if (stockEntitys != null && !stockEntitys.isEmpty()) {
					// pattets 中已有的stock不让再选 ,，如果是初始化则不要次操作（init）
					Boolean init = false;
					if (qtysAndBinNameKey.get(qtysAndBinNameKey.size() - 1) instanceof Boolean) {
						init = (Boolean) qtysAndBinNameKey.get(qtysAndBinNameKey.size() - 1);
					}
					if (qtysAndBinNameKey.size() > 3 && init) {
						for (int i = 0; i < qtysAndBinNameKey.size() - 1; i++) {
							for (int j = 0; j < stockEntitys.size(); j++) {
								if (qtysAndBinNameKey.get(i) instanceof Number
									&& new BigDecimal(String.valueOf(qtysAndBinNameKey.get(i))).compareTo(stockEntitys.get(j).qty) == 0) {
									stockEntitys.remove(j);
									break;
								}
							}
						}
					}
					for (Stock stockEntity : stockEntitys) {
						int k = qtys.size();
						qtys.add(stockEntity.qty);
						// 确保 展现再前台的list 中不会出现相同的数量 比如 40 40
						if (qtys.size() > k) {
							MapVo stock = new MapVo();
							stock.key = String.valueOf(stockEntity.id);
							stock.qty = stockEntity.qty;
							stocks.add(stock);
						}
					}
				}
				logger.info("^^^^^^^^^^^^stocks messages^^^^^^  " + play.libs.Json.toJson(stocks));
				if (stocks.size() > 0) {
					return ok(play.libs.Json.toJson(stocks));
				} else {
					return ok("There is no pallet you can use in this bin");
				}
			} else {
				return ok("system error you not select BinNameKey");
			}
		} else {
			return ok("system error!");
		}
	}

	/**
	 * 
	 * @return
	 */
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result getBatchsByPiNo() {
		List<MapVo> batchList = getPiByBatch(false);
		if (batchList != null && !batchList.isEmpty()) {
			//logger.info("*******batchList******" + play.libs.Json.toJson(batchList));
			return ok(play.libs.Json.toJson(batchList));
		} else {
			return ok("No corresponding stock");
		}

	}
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result getBatchsByPiNoForEdit() {
		List<MapVo> batchList = getPiByBatch(true);
		if (batchList != null && !batchList.isEmpty()) {
			//logger.info("*******batchList******" + play.libs.Json.toJson(batchList));
			return ok(play.libs.Json.toJson(batchList));
		} else {
			return ok("No corresponding stock");
		}

	}

	private static List<MapVo> getPiByBatch(boolean flag) {
		List<MapVo> batchList = new ArrayList<MapVo>();
		Set<String> batchNo = new HashSet<String>();
		String piNo = null;
		RequestBody body = request().body();
		if (body.asJson() != null) {
			logger.info("^^^^^^^piNo^^^^^^^" + body.asJson());
			piNo = Json.fromJson(body.asJson(), String.class);
			List<Batch> batchs = BatchSearchUtil.getlikeBatch(piNo);
			if (batchs != null && !batchs.isEmpty()) {
				for (Batch batch : batchs) {
					List<Stock> stocks = null;
					if(flag){
						stocks = Ebean.find(Stock.class).where().eq(DELETED,flag).eq("batch.id", batch.id).findList();	
					}else{
						stocks = Ebean.find(Stock.class).where().eq(DELETED,flag).eq(DELETED, false).eq("batch.id", batch.id).findList();	
					}
					if (stocks != null && !stocks.isEmpty()) {
						if (!batchNo.contains(batch.batchNo)) {
							MapVo map = new MapVo();
							map.key = batch.id.toString();
							map.descripton = batch.batchNo;
							batchList.add(map);
						}
						batchNo.add(batch.batchNo);
					}
				}

			}
		}
		return batchList;
	}

	/**
	 * 
	 * @param lot
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result getBatchDetail() {
		logger.info("+++++++++++++++have in method getBatchDetail++++++++++++++++++++++");
		TPResult result = new TPResult();
		RequestBody body = request().body();
		Stock tempStock = null;
		boolean flag = true;
		Map<String, Object> map = null;
		if (body.asJson() != null) {
			logger.info("^^^^^^^^^retrieve condition^^^^^^^" + body.asJson());
			map = Json.fromJson(body.asJson(), Map.class);
		}
		BigDecimal totalQty = new BigDecimal(0);
		String tempDate = "0";
		List<String> lotNos  = (List<String>) map.get("batchId");
		List<SqlRow> rows =new ArrayList<SqlRow>();
		for(String lot :lotNos){
			List<SqlRow> temprows = BatchSearchUtil.getBatchDetail(map.get("pino").toString().trim(),lot.trim());
			if(EmptyUtil.isNotEmptyList(temprows)){
				rows.addAll(temprows);
			}
		}
		// id date matCode
		if (EmptyUtil.isNotEmptyList(rows)) {
			for (SqlRow row : rows) {
				List<Stock> stocks = Ebean.find(Stock.class).where().eq(DELETED, false).eq("batch.id", row.getString("id")).findList();
				if (EmptyUtil.isNotEmptyList(stocks)) {
					if (flag) {
						tempStock = stocks.get(0);
						flag = false;
					}
					for (Stock stock : stocks) {
						totalQty = totalQty.add(stock.qty);
					}

				}
				if ((tempDate.compareTo(row.getString("date"))) < 0) {
					tempDate = row.getString("date");
				}
			}
			SqlRow row = rows.get(0);
			result.mat = row.getString("matCode");
			result.proQty = totalQty;
			result.matUom = tempStock.materialUom.uomCode;
			result.proDate = tempDate;
			//logger.info("+++++getBatchDetail++ json +++ :" + play.libs.Json.toJson(result));
			return ok(play.libs.Json.toJson(result));
		} else {
			return ok("this batch not receive");
		}

	}

	/**
	 * 
	 * @return
	 */
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result print() {
		logger.info("^^^^^^^^^^^^^^^you hava in  print method() ^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		RequestBody body = request().body();
		PalletDetail pd = null;
		if (body.asJson() != null) {
			logger.info("^^^^^^^^^print condition^^^^^^^" + body.asJson());
			pd = Json.fromJson(body.asJson(), PalletDetail.class);
			PalletDetail palletDetail = PalletDetail.getPalletDetail(pd);
			//logger.info("**********play.libs.Json.toJson(retrtprs)******   " + tpResults.size() + "   *******" + play.libs.Json.toJson(palletDetail));
			return ok(play.libs.Json.toJson(palletDetail));
		} else {
			return ok(" request is wrong");
		}
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public static Result deletePallet() {
		logger.info("^^^^^^^^^^^^you have in deletePallet method ^^^^^^ ^^^^^");
		RequestBody body = request().body();
		PlanItemDetail tempPd = null;
		boolean flag = true;
		List<String> ids = null;
		if (body.asJson() != null) {
			logger.info("*******ids ********" + body.asJson());
			ObjectMapper mapper = new ObjectMapper();
			try {
				ids = mapper.readValue(body.asJson(), new TypeReference<List<String>>(){});
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (EmptyUtil.isNotEmptyList(ids)) {
				for (String id : ids) {
					PlanItemDetail pid = Ebean.find(PlanItemDetail.class).where().eq("id", id).findUnique();
					if(CodeKey.PLAN_DETAIL_STATUS_NEW.getValue().equals(pid.detailStatus)){
						CrudUtil.delete(pid);
					}
					if (flag) {
						tempPd = pid;
					}

				}
			}
			// 如果 这个pid 对应的PlanItem没有detail 则改变其状态
			List<PlanItemDetail> pids = Ebean.find(PlanItemDetail.class).where().eq("deleted", false).eq("planItem", tempPd.planItem).findList();
			if (pids == null || pids.isEmpty()) {
				tempPd.planItem.itemStatus = CodeKey.PLAN_STATUS_NEW.toString();
				CrudUtil.update(tempPd.planItem);
			}
		}
		return ok("Delete Success!");
	}

	/**
	 * 
	 * @param fromUom
	 * @param toUOm
	 * @return
	 */
	public static BigDecimal conversionUom(MaterialUom fromUom, MaterialUom toUom) {
		BigDecimal result = new BigDecimal(0);
		if (fromUom != null && toUom != null) {
			result = ((toUom.qtyOfBaseDenom).multiply(fromUom.qtyOfBaseNum)).divide(((fromUom.qtyOfBaseDenom).multiply(toUom.qtyOfBaseNum)), 2,
				BigDecimal.ROUND_CEILING);
		}
		return result;
	}

	/**
	 * 
	 * @param whNameKey
	 * @return
	 */
	@Transactional
	public static Result getAreaAndBin(String whNameKey) {
		List<Area> areaEntitys = Ebean.find(Area.class).fetch("warehouse", "nameKey").where().eq("deleted", false).eq("warehouse.nameKey", whNameKey)
			.findList();
		List<MapVo> areas = new ArrayList<MapVo>();
		List<MapVo> bins = new ArrayList<MapVo>();
		Map<String, List<MapVo>> map = new HashMap<String, List<MapVo>>();
		if (areaEntitys != null && !areaEntitys.isEmpty()) {
			for (Area areaEntity : areaEntitys) {
				MapVo area = new MapVo();
				area.key = String.valueOf(areaEntity.id);
				area.descripton = areaEntity.nameKey;
				areas.add(area);
				List<Bin> binEntitys = Ebean.find(Bin.class).where().eq("deleted", false).eq("area", areaEntity).findList();
				if (binEntitys != null && !binEntitys.isEmpty()) {
					for (Bin binEntity : binEntitys) {
						MapVo bin = new MapVo();
						bin.key = String.valueOf(binEntity.id);
						bin.descripton = binEntity.nameKey;
						bins.add(area);
					}
				}
			}
		}
		map.put("areas", areas);
		map.put("bins", bins);
		//logger.info("^^^^^^^^^^^^^^^^^^^^^^^" + play.libs.Json.toJson(map));
		return ok(play.libs.Json.toJson(map));
	}
}
