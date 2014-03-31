/** * PalletArrangementController.java 
* Created on 2013-4-22 上午10:52:32 
*/

package controllers.arrangement;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Area;
import models.Bin;
import models.Execution;
import models.Order;
import models.OrderItem;
import models.Stock;
import models.StockTransaction;
import models.vo.arrangement.BatchStockVo;
import models.vo.arrangement.SerchVo;
import models.vo.arrangement.StockVo;
import models.vo.transfer.SortUtil;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
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
import utils.UnitConversion;
import utils.enums.CodeKey;
import utils.exception.CustomException;
import views.html.arrangement.palletArrangement;
import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.SqlRow;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: PalletArrangementController.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class PalletArrangementController extends Controller {
	private static final String EXECUTION_TYPE = "T004";
	

	private static final String DELETED = "deleted";
	private static final String ERROR = "Error ";


	// 允许从前台传到后台的最大数据量
	private static final int MAX_LENGTH = 5 * 1024 * 1024;

	private static final Logger logger = LoggerFactory.getLogger(PalletArrangementController.class);

	/**
	 * 
	 * @return
	 */
	public static Result index() {

		return ok(palletArrangement.render(""));
	}

	/**
	 * 
	 * @return
	 */
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result list() {
		logger.info("^^^^^^^^^^^^^^^^^ you have in PalletArrangementController list() method^^^^^^^^^^^^^ ");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		boolean flag = false;
		boolean flagEqual = false;
		List<BatchStockVo> batchStocks = new ArrayList<BatchStockVo>();
		RequestBody body = request().body();
		SerchVo serchVo = null;
		HashMap<String, String> batchMap = new HashMap<String, String>();

		if (body.asJson() != null) {
			//logger.info("search vo josn modal:  " + body.asJson());
			serchVo = Json.fromJson(body.asJson(), SerchVo.class);
		}
		if (serchVo == null) {
			serchVo = new SerchVo();
		}
		if (serchVo.beginDate != null && serchVo.endDate != null && serchVo.beginDate.compareTo(serchVo.endDate) > 0) {
			return ok("fromDate can not be greater than toDate,Please correct!");
		}
		batchMap.put("pi", EmptyUtil.nullToEmpty(serchVo.piNo));
		if (serchVo.beginDate != null) {
			batchMap.put("datefrom", sdf.format(serchVo.beginDate));
		}
		if (serchVo.endDate != null) {
			batchMap.put("dateto", sdf.format(serchVo.endDate));
		}
		List<SqlRow> rows = BatchSearchUtil.batchdetails(batchMap);
		ExpressionList<Order> el = Ebean.find(Order.class).where().eq(DELETED, false).eq("orderType", CodeKey.ORDER_TYPE_PRODUCE.getValue());
		if (StringUtils.isNotEmpty(serchVo.sgPiNo)) {
			el.like("contractNo", "%" + serchVo.sgPiNo + "%");
			flag = true;
		}
		if (StringUtils.isNotEmpty(serchVo.piStatus)) {
			el.like("orderStatus", "%" + serchVo.piStatus + "%");
			flag = true;
		}
		List<Order> orders = el.findList();
		if (EmptyUtil.isNotEmptyList(rows)) {
			if (flag && EmptyUtil.isNotEmptyList(orders)) {
				flagEqual = false;
				for (SqlRow row : rows) {
					for (Order order : orders) {
						if (order.internalOrderNo.equals(row.getString("pi"))) {
							flagEqual = true;
							break;
						}
					}
					if (flagEqual) {
						List<Stock> stocks = Ebean.find(Stock.class).where().eq(DELETED, false).eq("batch.id", row.getString("id")).findList();
						if (EmptyUtil.isNotEmptyList(stocks)) {
							batchStocks.add(BatchStockVo.getBatchStockVo(stocks));
						}
					}
				}
			} else if(!flag){
				for (SqlRow row : rows) {
					List<Stock> stocks = Ebean.find(Stock.class).where().eq(DELETED, false).eq("batch.id", row.getString("id")).findList();
					if (EmptyUtil.isNotEmptyList(stocks)) {
						batchStocks.add(BatchStockVo.getBatchStockVo(stocks));
					}
				}

			}
		}
		//logger.info("^^^^^batchStocks^^^^^" + play.libs.Json.toJson(batchStocks));
		return ok(play.libs.Json.toJson(batchStocks));
	}

	/**
	 * 
	 * @return合并
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_LENGTH)
	public static Result combine() {
		logger.info("^^^^^you have in PalletArrangementController combine() method^^^");
		RequestBody body = request().body();
		ObjectMapper mapper = new ObjectMapper();
		BigDecimal executeQty = null;
		BigDecimal oldQty = null;
		if (body.asJson() != null) {
			//logger.info("^^^^List<StockVo> asJson^^^^^^" + body.asJson());
			HashMap<String, String> exeMap = new HashMap<String, String>();
			exeMap.put("transactionCode", CodeKey.TRANSACTION_CODE_COMBIN.toString());
			List<StockVo> stockVos = new ArrayList<StockVo>();
			try {
				stockVos = mapper.readValue(body.asJson(), new TypeReference<List<StockVo>>() {
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			StockVo mainStockVo = null;
			StockVo generateStockVo = null;
			if (stockVos != null && !stockVos.isEmpty()) {
				for (StockVo stockVo : stockVos) {
					// 如果有stock id 说明是被合并的stock,如果main==true更新 否则 删除此stock
					if (stockVo.stockId != null && !"".equals(stockVo.stockId)) {
						if (!stockVo.main) {
							Stock stock = Ebean.find(Stock.class).where().eq(DELETED, false).eq("id", stockVo.stockId).findUnique();
							//logger.info("^^^^^^^^^^^" + stock.id);
							CrudUtil.delete(stock);
							executeQty = stock.qty;
							// 删除
							Execution execution = setExecution(stock, executeQty, false, true);
							setStockTransaction(execution, exeMap, stock, stock.qty, false, true);
						} else {
							mainStockVo = stockVo;
						}
					} else {
						generateStockVo = stockVo;
					}

				}
				Stock mainStock = Ebean.find(Stock.class).where().eq(DELETED, false).eq("id", mainStockVo.stockId).findUnique();
				executeQty = generateStockVo.qty.subtract(mainStock.qty).abs();
				oldQty = mainStock.qty;
				mainStock.qty = generateStockVo.qty;
				CrudUtil.update(mainStock);
				Execution execution = setExecution(mainStock, executeQty, false, false);
				setStockTransaction(execution, exeMap, mainStock, oldQty, false, false);
			}

		}
		return ok("you have combine success");
	}

	/**
	 * 拆分
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_LENGTH)
	public static Result dismantle() {
		logger.info("you have in PalletArrangementController dismantle() method");
		RequestBody body = request().body();
		ObjectMapper mapper = new ObjectMapper();
		List<StockVo> stockVos = new ArrayList<StockVo>();
		boolean flag = false;
		boolean flagFist = true;
		String stockNo = null;
		BigDecimal executeQty = null;
		BigDecimal oldQty = null;
		HashMap<String, String> extMap = new HashMap<String, String>();
		if (body.asJson() != null) {
			//logger.info("^^^^List<StockVo> asJson^^^^^^" + body.asJson());
			HashMap<String, String> exeMap = new HashMap<String, String>();
			exeMap.put("Arrangement", CodeKey.TRANSACTION_CODE_COMBINEREVERSED.toString());
			try {
				stockVos = mapper.readValue(body.asJson(), new TypeReference<List<StockVo>>() {
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (stockVos != null && !stockVos.isEmpty()) {
				Stock mainStock = null;
				for (StockVo stockVo : stockVos) {
					if (stockVo.main == true) {
						mainStock = Ebean.find(Stock.class).where().eq(DELETED, false).eq("id", stockVo.stockId).findUnique();
						flag = true;
						break;
					}
				}
				for (StockVo stockVo : stockVos) {
					if (flag && stockVo.main == false) {
						if (flagFist) {
							oldQty = mainStock.qty;
							executeQty = mainStock.qty.subtract(stockVo.qty).abs();
							mainStock.qty = stockVo.qty;
							stockNo = ExtUtil.unapply(mainStock.ext).get("stockNo");
							extMap.put("stockNo", stockNo + "-1");
							mainStock.ext = ExtUtil.apply(extMap);
							CrudUtil.update(mainStock);
							flagFist = false;
							// 更新stock
							Execution execution = setExecution(mainStock, executeQty, false, false);
							setStockTransaction(execution, exeMap, mainStock, oldQty, false, false);
						} else {
							extMap.put("stockNo", stockNo + "-2");
							Stock dismantleStock = StockVo.getStock(stockVo, mainStock, ExtUtil.apply(extMap));
							// 新建stock
							//logger.info("dismantleStock.qtydismantleStock.qty" + dismantleStock.qty);
							Execution execution = setExecution(dismantleStock, dismantleStock.qty, true, false);
							setStockTransaction(execution, exeMap, dismantleStock, dismantleStock.qty, true, false);
						}
					}

				}

			}

		}
		return ok("you have dismantle success");
	}

	public static Result caculateNewStock(){
		logger.info("you have in caculateNewStock mehtod");
		RequestBody body = request().body();
		if(body.asJson()!=null){
			Map<String,Integer> map =null;
			List<StockVo> vos = new ArrayList<StockVo>();
			final BigDecimal 	ZERO = new BigDecimal(0);
			//logger.info("json content is :%%%%%%% "+body.asJson());
			map = Json.fromJson(body.asJson(), Map.class);
			BigDecimal 	totalQty = new BigDecimal(map.get("totalQty"));
			BigDecimal 	newQty = new BigDecimal(map.get("newQty"));
			while(totalQty.compareTo(ZERO)>0){
				StockVo vo = new StockVo();
               if(totalQty.compareTo(newQty)>0){
					vo.qty = newQty;
				} else {
					vo.qty = totalQty;
				}
				totalQty = totalQty.subtract(newQty);
				vo.main = true;
				vo.todo = false;
				vo.isPrint = "N";
				vos.add(vo);
			}
			return ok(Json.toJson(vos));
		}
		return ok("System erro!");
	}
	/**
	 * 
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_LENGTH)
	public static Result doExecution() {
		logger.info("^^^^^^^^^^^^^you have in doExecution  method ^^^^^^^^^^^^^");
		RequestBody body = request().body();
		ObjectMapper mapper = new ObjectMapper();
		List<StockVo> stockVos = null;
		Stock tempStock = null;
		final BigDecimal ZERO = new BigDecimal(0);
		HashMap<String, String> extMap = new HashMap<String, String>();
		if (body.asJson() != null) {
			//logger.info("^^^^List<StockVo> asJson^^^^^^" + body.asJson());
			extMap.put("transactionCode", CodeKey.TRANSACTION_CODE_COMBIN.toString());
			try {

				stockVos = mapper.readValue(body.asJson(), new TypeReference<List<StockVo>>() {
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (EmptyUtil.isNotEmptyList(stockVos)) {
				for (StockVo vo : stockVos) {
					if (!vo.main) {// 新生成的stock
						Stock stock = Ebean.find(Stock.class).where().eq(DELETED, false).eq("id", vo.stockId).findUnique();
						if (tempStock == null) {
							tempStock = stock;
						}
						if(vo.qty.compareTo(stock.qty)!=0){
							return ok(ERROR+"pallet "+vo.palletNo+" qty have changed!");
						}
						CrudUtil.delete(stock);
						// 删除
						Execution execution = setExecution(stock, stock.qty, false, true);
						setStockTransaction(execution, extMap, stock, stock.qty, false, true);

					}
				}
				for (StockVo vo : stockVos) {
					if (vo.main) {// 新生成的stock
						Stock stock = new Stock();
						Area area = Ebean.find(Area.class).where().eq(DELETED, false).eq("id", vo.area.key).findUnique();
						Bin bin = Ebean.find(Bin.class).where().eq(DELETED, false).eq("id", vo.bin.key).findUnique();
						setStock(stock, tempStock, area, bin, vo.qty, vo.palletNo);
						Execution execution = setExecution(stock, stock.qty, true, false);
						setStockTransaction(execution, extMap, stock, stock.qty, true, false);

					}
				}
			}
			return ok("you have arrangement success");
		}else{
		  return ok(ERROR+"System error!");
		}
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public static Result listPallets(String id) {
		logger.info("you have in PalletArrangementController listPallets() method and parma 'id'is :" + id);
		List<StockVo> stockVos = new ArrayList<StockVo>();
		List<Stock> stocks = Ebean.find(Stock.class).where().eq(DELETED, false).eq("batch.id", id).order("updatedAt ascending").findList();
		if (stocks != null && !stocks.isEmpty()) {
			for (Stock stock : stocks) {
				StockVo stockVo = StockVo.getStockVo(stock);
				stockVo.stockLineId = id;
				stockVos.add(stockVo);
			}
		}
		try {
			stockVos = SortUtil.sortList(stockVos);
		} catch (CustomException e) {
			e.printStackTrace();
		}
		//logger.info("stockVos json mattern:" + play.libs.Json.toJson(stockVos));
		return ok(play.libs.Json.toJson(stockVos));
	}
	/**
	 * 
	 * @param id 为batch id
	 * @return
	 */
	public static Result print(String piNo){
		logger.info("you have in print method and parma 'piNo' :" + piNo);
		 Map<String,Object> map = new HashMap<String,Object>();
		 List<OrderItem> orderItems = Ebean.find(OrderItem.class)
		 .where().join("material","materialName")
		 .where().eq("order.internalOrderNo", piNo).eq(DELETED, false).findList();
		   OrderItem orderItem = orderItems.get(0);
			HashMap<String, String> ext = ExtUtil.unapply(orderItem.ext);
			String skuUomId = ext.get("qtyPerPalletUom");
		  BigDecimal pack = UnitConversion.SkuToQuantity(orderItem.settlementUom.id.toString(), skuUomId);
		 
		  map.put("mat", orderItem.material.materialName);
		  map.put("pack", pack);
		  
		return ok(Json.toJson(map));
	}

	/**
	 * 
	 * @param planItem
	 * @param planItemDetail
	 * @param map 往ext字段中放
	 * @param stocks 至少两个 stocks[0]代表old stocks[1]代表new
	 * @return
	 */
	private static Execution setExecution(Stock stock, BigDecimal executedQty, boolean isNew, boolean isDeleted) {

		Execution execution = new Execution();
		execution.planItem = null;
		execution.executionType = EXECUTION_TYPE;
		execution.executionSubtype = EXECUTION_TYPE;
		execution.planItemDetail = null;
		execution.reverse = false;
		execution.reversed = false;
		execution.refExecution = null;
		execution.executedBy = null;
		execution.executedQty = executedQty;
		execution.material = stock.material;
		// 说明这 isNew true 说明这个stock本来就存在 ，反之亦然
		if (!isNew) {
			execution.fromMaterialUom = stock.materialUom;
			execution.fromArea = stock.area;
			execution.fromBin = stock.bin;
			execution.fromPalletType = null;
			execution.fromPallet = null;
		}
		execution.executedAt = new Timestamp(System.currentTimeMillis());
		// 是否被删除了
		if (!isDeleted) {
			execution.toMaterialUom = stock.materialUom;
			execution.toArea = stock.area;
			execution.toBin = stock.bin;
			execution.toPalletType = null;
			execution.toPallet = null;
		}
		// execution.seqNo
		execution.remarks = null;
		execution.deleted = false;

		CrudUtil.save(execution);
		return execution;
	}

	/**
	 * 
	 * @param execution
	 * @param map key: transactionCode
	 * @param stocks
	 * @return
	 */
	private static StockTransaction setStockTransaction(Execution execution, HashMap<String, String> map, Stock stock, BigDecimal oldQty,
		boolean isNew, boolean isDeleted) {
		StockTransaction stockTransaction = new StockTransaction();
		stockTransaction.warehouse = stock.warehouse;
		stockTransaction.stock = stock;
		stockTransaction.execution = execution;
		if (map != null) {  						 
			stockTransaction.transactionCode = map.get("transactionCode");
		}
		if (!isNew) {
			stockTransaction.oldUomId = execution.fromMaterialUom.id;
			stockTransaction.oldQty = oldQty;
			stockTransaction.oldAreaId = execution.fromArea.id;
			stockTransaction.oldBinId = execution.fromBin.id;
			stockTransaction.oldArrivedAt = stock.arrivedAt;
			stockTransaction.oldStatus = stock.stockStatus;
			// stockTransaction.oldPalletTypeId = stock.palletType.id;
			// stockTransaction.oldPalletId = stock.pallet.id;
			// stockTransaction.oldTracingId = stock.tracingId;
		} else {
			stockTransaction.oldQty = new BigDecimal(0);
		}
		if (!isDeleted) {
			stockTransaction.newUomId = execution.toMaterialUom.id;
			stockTransaction.newQty = stock.qty;
			stockTransaction.newAreaId = execution.toArea.id;
			stockTransaction.newBinId = execution.toBin.id;
			stockTransaction.newArrivedAt = stock.arrivedAt;
			stockTransaction.newStatus = stock.stockStatus;
			// stockTransaction.newPalletTypeId = stock.palletType.id;
			// stockTransaction.newPalletId = stock.pallet.id;
			// stockTransaction.newTracingId = stock.tracingId;
		} else {
			stockTransaction.newQty = new BigDecimal(0);
		}
		stockTransaction.transactionAt = new Timestamp(System.currentTimeMillis());
		stockTransaction.seqNo = execution.seqNo;
		CrudUtil.save(stockTransaction);
		// DataExchangePlatform.setTransaction(stockTransaction);
		return stockTransaction;
	}

	private static void setStock(Stock newStock, Stock oldStock, Area area, Bin bin, BigDecimal qty, String stockNo) {
		newStock.warehouse = oldStock.warehouse;
		newStock.material = oldStock.material;
		newStock.materialUom = oldStock.materialUom;
		newStock.batch = oldStock.batch;
		newStock.palletType = oldStock.palletType;
		newStock.pallet = oldStock.pallet;
		newStock.receivedAt = oldStock.receivedAt;
		newStock.arrivedAt = oldStock.arrivedAt;
		newStock.stockStatus = oldStock.stockStatus;
		newStock.area = area;
		newStock.bin = bin;
		newStock.qty = qty;
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("stockNo", stockNo);
		newStock.ext = ExtUtil.apply(map);
		CrudUtil.save(newStock);
	}

	 

}
