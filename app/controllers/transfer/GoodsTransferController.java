/** * GoodstransferController.java 
* Created on 2013-3-27 上午11:37:20 
*/

package controllers.transfer;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Area;
import models.Bin;
import models.Execution;
import models.PlanItem;
import models.PlanItemDetail;
import models.Stock;
import models.StockTransaction;
import models.vo.transfer.PalletDetail;
import models.vo.transfer.SortUtil;
import models.vo.transfer.TPResult;
import models.vo.transfer.TPSearch;

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
import utils.CrudUtil;
import utils.EmptyUtil;
import utils.ExtUtil;
import utils.enums.CodeKey;
import utils.exception.CustomException;
import views.html.transfer.goodsTransfer;
import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: GoodstransferController.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@SubjectPresent(handler = NoUserDeadboltHandler.class)
public class GoodsTransferController extends Controller {

	private static final Logger logger = LoggerFactory.getLogger(TransferPlanController.class);
	private static final String ERROR = "Error";
	// 允许从前台传到后台的最大数据量
	private static final int MAX_LENGTH = 5 * 1024 * 1024;

	/**
	 * show goods transfer main view 
	 * @return
	 */
	@With(Menus.class)
	public static Result index() {
		return ok(goodsTransfer.render(""));
	}

	/**
	 * list tp that Meet the conditions
	 * @return
	 */
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result list() {
		logger.info("************* you have in list method ****************");
		List<TPResult> tpResults = new ArrayList();
		RequestBody body = request().body();
		TPSearch tps = null;
		if (body.asJson() != null) {
			logger.info("^^^^^^^^^list condition^^^^^^^" + body.asJson());
			tps = Json.fromJson(body.asJson(), TPSearch.class);
		}
		if (tps == null) {
			tps = new TPSearch();
		}
		if (tps.transferFromDate != null && tps.transferToDate != null && tps.transferFromDate.compareTo(tps.transferToDate) > 0) {
			return ok("fromDate can not be greater than toDate,Please correct!");
		}
		tpResults = PlanItem.list(tps, tpResults);
		//logger.info("**********play.libs.Json.toJson**tpResults.size****   " + tpResults.size() + "   *******" + play.libs.Json.toJson(tpResults));
		return ok(play.libs.Json.toJson(tpResults));
	}

	/**
	 * 
	 * @param id is PlanItem id base this retrieval  corresponing planItemDetail 
	 * @return  show planItemDetail  on view
	 */
	@Transactional
	public static Result listpallets(String id) {
		logger.info("**********  listPallets  ****************" + id);

		List<PalletDetail> pallets = new ArrayList<PalletDetail>();
		int i = 0;
		PlanItem planItem = PlanItem.find().where().eq("deleted", false).eq("id", id).findUnique();
		BigDecimal total = new BigDecimal(0);
		if (planItem != null) {
			List<PlanItemDetail> pids = Ebean.find(PlanItemDetail.class).where().join("stock", "qty").where().join("stock.area", "nameKey").where()
				.join("stock.bin", "nameKey").where().join("stock.materialUom").where().eq("deleted", false)
				.eq("detailStatus", CodeKey.PLAN_DETAIL_STATUS_NEW.toString()).eq("planItem", planItem).order("updatedAt ascending").findList();
			List<Execution> executions = Ebean.find(Execution.class).where().eq("deleted", false).eq("planItem", planItem)
				.order("updatedAt ascending").findList();

			if (executions != null && !executions.isEmpty()) {
				for (Execution execution : executions) {
					PalletDetail pd = PalletDetail.getPalletDetail(execution);
					total = total.add(pd.qty);
					pallets.add(pd);
				}
			}
			if (EmptyUtil.isNotEmptyList(pids)) {
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
	 * this function relation to execution model is one to many
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_LENGTH)
	public static Result execute() {
		// assume already have stocks and Forklift Driver(execution_executedBy)
		logger.info("^^^^^^^^ you have in execute method ^^^^^^^^^^^^^^");
		StringBuffer sb = new StringBuffer("  The plan detail corresponding pallet no ");
		boolean stockChange = false;
		RequestBody body = request().body();
		List<PalletDetail> pds = null;
		List<String> palletNos = new ArrayList<String>();
		Map<String, Object> reMap = new HashMap<String, Object>();
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
				HashMap<String, String> map = ExtUtil.unapply(planItem.ext);
				Map<String, Stock> stocks = new HashMap<String, Stock>();
				BigDecimal totalQty = new BigDecimal(map.get("executeQty"));
				for (PalletDetail pd : pds) {
					Stock stock = Ebean.find(Stock.class).where().eq("deleted", false).eq("id", pd.stockId).findUnique();
					if (stock == null) {
						stockChange = true;
						sb.append(pd.palletNo + ", ");
						pd.notMatch = true;
						palletNos.add(pd.palletNo);
					}
					if (stock != null) {
						totalQty = totalQty.add(stock.qty);
						stocks.put(pd.stockId, stock);
					}
				}

				if (!EmptyUtil.isNotEmtpyString(pds.get(0).reason) && totalQty.compareTo(planItem.palnnedQty) > 0) {
					reMap.put("message", ERROR + "Execution  qty " + totalQty + "  more than plan qty " + planItem.palnnedQty + ", must be filled  reason ");
					return ok(Json.toJson(reMap));
				}
				totalQty = new BigDecimal(map.get("executeQty"));
				for (PalletDetail pd : pds) {
					if (!pd.notMatch) {
						//palletNos.add(pd.palletNo);
						Stock stock = stocks.get(pd.stockId);
						List<Area> areas = Ebean.find(Area.class).where().eq("deleted", false).eq("nameKey", pd.areaTo).findList();
						List<Bin> bins = Ebean.find(Bin.class).where().eq("deleted", false).eq("nameKey", pd.binTo).findList();
						PlanItemDetail pid = PlanItemDetail.find().byId(pd.id);
						if (CodeKey.PLAN_DETAIL_STATUS_NEW.getValue().equals(pid.detailStatus)) {
							// 生成相应的 execution
							Execution execution = generateExecution(planItem, stock, pd, areas, bins);
							// 生成相应的 StockTransaction
							generateStockTransaction(planItem, execution, stock);
							// 更新stock的位置
							stock.area = areas.get(0);
							stock.bin = bins.get(0);
							CrudUtil.update(stock);
							totalQty = totalQty.add(stock.qty);
							// 如果这个palletDetail
							// 是从planitemdetail带出则更新planitemdetail
							if (pid != null) {
								if (totalQty.compareTo(planItem.palnnedQty) > 0) {
									HashMap<String, String> pidMap = ExtUtil.unapply(pid.ext);
									addMapPorperty(pidMap, pd);
									pid.ext = ExtUtil.apply(pidMap);
								}
								pid.detailStatus = CodeKey.PLAN_DETAIL_STATUS_EXECUTED.toString();
								CrudUtil.update(pid);
							}
						}
					}
				}
				// 更新planItem的状态
				map.put("executeQty", String.valueOf(totalQty));
				if (totalQty.compareTo(planItem.palnnedQty) >= 0) {
					planItem.itemStatus = CodeKey.PLAN_STATUS_EXECUTED.toString();
				} else {
					planItem.itemStatus = CodeKey.PLAN_STATUS_EXECUTEING.toString();
				}
				planItem.ext = ExtUtil.apply(map);
				CrudUtil.update(planItem);
			}
			// 如果plan detai 对应的stock已经被修改则返回相应的错误
			reMap.put("palletNos", palletNos);
			if (stockChange) {
				reMap.put("message", sb.append(" does not exist,The system ignores these data!").toString());
			} else {
				reMap.put("message", "The operation was successful");
			}
			return ok(Json.toJson(reMap));
		}
		reMap.put("message", ERROR+"System error!");
		return ok(Json.toJson(reMap));
	}

	/**
	 * 
	 * @param planItem
	 * @param stock
	 * @param pd
	 * @return
	 */
	private static Execution generateExecution(PlanItem planItem, Stock stock, PalletDetail pd, List<Area> tas, List<Bin> tbs) {
		Execution execution = new Execution();
		HashMap<String, String> map = new HashMap<String, String>();
		execution.planItem = planItem;
		if (pd.id != null && !"".equals(pd.id)) {
			execution.planItemDetail = PlanItemDetail.find().byId(pd.id);
		}
		execution.executionType = planItem.planType;
		execution.executionSubtype = planItem.planSubtype;
		execution.reverse = false;
		execution.reversed = false;
		execution.refExecution = null;
		execution.executedBy = null;
		execution.executedQty = stock.qty;
		execution.material = planItem.material;
		execution.fromMaterialUom = stock.materialUom;
		execution.fromArea = stock.area;
		execution.fromBin = stock.bin;
		execution.fromPalletType = null;
		// execution.fromPallet = Pallet.find().byId(pd.palletId);
		execution.toMaterialUom = stock.materialUom;
		execution.toArea = tas.get(0);
		execution.toBin = tbs.get(0);
		execution.toPalletType = null;
		execution.toPallet = null;
		// execution.seqNo
		execution.remarks = null;
		execution.deleted = false;
		addMapPorperty(map, pd);
		map.put("FD", pd.forkLiftDriver);
		execution.ext = ExtUtil.apply(map);
		CrudUtil.save(execution);
		return execution;
	}

	/**
	 * 
	 * @param planItem
	 * @param execution
	 * @param stock
	 * @param pd
	 */
	private static void generateStockTransaction(PlanItem planItem, Execution execution, Stock stock) {
		StockTransaction stockTransaction = new StockTransaction();
		stockTransaction.warehouse = stock.warehouse;
		stockTransaction.stock = stock;
		stockTransaction.execution = execution;
		stockTransaction.transactionCode = CodeKey.TRANSACTION_CODE_TRANSFER.toString();
		stockTransaction.oldUomId = execution.fromMaterialUom.id;
		stockTransaction.oldQty = stock.qty;
		stockTransaction.oldAreaId = execution.fromArea.id;
		stockTransaction.oldBinId = execution.fromBin.id;
		stockTransaction.oldArrivedAt = stock.arrivedAt;
		stockTransaction.oldStatus = stock.stockStatus;
		// stockTransaction.oldPalletTypeId = stock.palletType.id;
		// stockTransaction.oldPalletId = stock.pallet.id;
		// stockTransaction.oldTracingId = stock.tracingId;
		stockTransaction.newUomId = execution.toMaterialUom.id;
		stockTransaction.newQty = stock.qty;
		stockTransaction.newAreaId = execution.toArea.id;
		stockTransaction.newBinId = execution.toBin.id;
		stockTransaction.newArrivedAt = execution.executedAt;
		stockTransaction.newStatus = stock.stockStatus;
		stockTransaction.transactionAt = new Timestamp(System.currentTimeMillis());
		// stockTransaction.newPalletTypeId = stock.palletType.id;
		// stockTransaction.newPalletId = stock.pallet.id;
		// stockTransaction.newTracingId = stock.tracingId;
		stockTransaction.seqNo = execution.seqNo;
		CrudUtil.save(stockTransaction);
		// DataExchangePlatform.setTransaction(stockTransaction);
	}

	private static void addMapPorperty(HashMap<String, String> map, PalletDetail pd) {
		map.put("reason", EmptyUtil.nullToEmpty(pd.reason));
		map.put("shift", EmptyUtil.nullToEmpty(pd.shift));
		map.put("remarks", EmptyUtil.nullToEmpty(pd.remarks));
	}

}
