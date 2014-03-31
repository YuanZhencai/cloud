package controllers.inbound;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Area;
import models.Execution;
import models.Stock;
import models.StockTransaction;
import models.vo.Result.ResultVo;
import models.vo.inbound.AreaVo;
import models.vo.transfer.DirectGoodReceiveVo;
import models.vo.transfer.DirectTransferVo;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import play.mvc.Http.RequestBody;
import security.NoUserDeadboltHandler;
import utils.BatchSearchUtil;
import utils.CrudUtil;
import utils.EmptyUtil;
import utils.SessionSearchUtil;
import utils.UnitConversion;
import utils.enums.CodeKey;
import views.html.inbound.directGoodsReceive;
import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: controllers.inbound.DirectGoodsReceiveController.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:cuichunsheng@wcs-global.com">Cui Chunsheng</a>
 */
@SubjectPresent(handler = NoUserDeadboltHandler.class)
public class DirectGoodsReceiveController extends Controller {
	
	private static final Logger logger = LoggerFactory.getLogger(DirectGoodsReceiveController.class);
	
	static String TEMPERING = "Tempering";// 硬编码
	static String AGEING = "Ageing";// 硬编码
	private static final int MAX_LENGTH = 5 * 1024 * 1024;
	
	@With(Menus.class)
	public static Result index() {
		return ok(directGoodsReceive.render(""));
	}
	
	/**
	 * 初始化添加area
	 * @return
	 */
	public static Result initArea() {
		logger.info("++++Goods receive++++++initArea+++++start++++++");
		List<AreaVo> areaVoList = new ArrayList<AreaVo>();
		JsonNode body = request().body().asJson();
		if (body != null) {
			if (SessionSearchUtil.searchWarehouse() == null) {
				return ok(play.libs.Json.toJson(new ResultVo(areaVoList)));
			} else {
				String warehouseId = String.valueOf(SessionSearchUtil.searchWarehouse().id);
				// 用户未填写生成时间策略，则默认放到常温库
				List<Area> areaList = Area.find().fetch("warehouse").fetch("storageType").where().eq("deleted", false)
						.eq("warehouse.deleted", false).eq("warehouse.id", warehouseId).eq("storageType.nameKey", AGEING).orderBy("nameKey")
						.findList();
				for (Area area : areaList) {
					AreaVo areaVo = new AreaVo();
					areaVo.id = String.valueOf(area.id);
					areaVo.nameKey = area.nameKey;
					areaVoList.add(areaVo);
				}
				return ok(play.libs.Json.toJson(new ResultVo(areaVoList)));
			}
		} else {
			return ok(play.libs.Json.toJson(new ResultVo(areaVoList)));
		}
	}
	
	
	
	//单位换算
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result uomConvertRate(){
		JsonNode body = request().body().asJson();
		if (body != null) {
			Map<String,String> skuVo = Json.fromJson(body, HashMap.class);
			return ok(play.libs.Json.toJson(new ResultVo(UnitConversion.SkuToQuantity(skuVo.get("qtyUomId"),skuVo.get("palletUomId")))));
		} else {
			logger.error("+++++++Goods receive+++++++getPiSku++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo("error", "Can not get data from page!")));
		}
	}
	
	/**
	 * 保存order/orderitem
	 * execution
	 * batch
	 * stock
	 * stocktransaction
	 * */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_LENGTH)
	public static Result executeGoodReceive(){
		RequestBody body = request().body();
		List<DirectGoodReceiveVo> dts = null;
		if (body.asJson() != null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				dts = mapper.readValue(body.asJson(), new TypeReference<List<DirectGoodReceiveVo>>() {
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (null!=dts && dts.size() > 0) {
				HashMap<String, String> receive = new HashMap<String, String>();
				receive.put("transactionCode", CodeKey.TRANSACTION_CODE_DIRECT_RECEIPT.toString());
				for (int i = 0, len = dts.size(); i < len; i++) {
					Stock stock = dts.get(i).setStock();
					CrudUtil.save(stock);
					Execution execution = setExecution(stock, stock.qty, true, false);
					setStockTransaction(execution, receive, stock, stock.qty, true, false);
				}
			}
		}
		return ok(play.libs.Json.toJson(new ResultVo("The operation was successful!")));
	}
	
	/**
	 * 
	 * @param planItem
	 * @param planItemDetail
	 * @param map 往ext字段中放
	 * @param stocks 至少两个 stocks[0]代表old stocks[1]代表new
	 * @return
	 */
	public static Execution setExecution(Stock stock, BigDecimal executedQty, boolean isNew, boolean isDeleted) {
		Execution execution = new Execution();
		execution.planItem = null;
		execution.executionType = CodeKey.EXECUTION_TYPE_RECEIPT.getValue();
		execution.executionSubtype = CodeKey.EXECUTION_TYPE_RECEIPT.getValue();
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
	public static StockTransaction setStockTransaction(Execution execution, HashMap<String, String> map, Stock stock, BigDecimal oldQty,
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
}
