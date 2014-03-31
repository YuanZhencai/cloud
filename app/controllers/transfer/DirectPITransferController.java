/** * DirectPITransfer.java 
* Created on 2013-7-10 下午1:40:29 
*/

package controllers.transfer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import models.Execution;
import models.OrderItem;
import models.Stock;
import models.StockTransaction;
import models.vo.transfer.DirectTransferVo;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.With;
import security.NoUserDeadboltHandler;
import utils.BatchSearchUtil;
import utils.CrudUtil;
import utils.EmptyUtil;
import utils.ExtUtil;
import utils.UnitConversion;
import utils.enums.CodeKey;
import views.html.transfer.directPITransfer;
import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: DirectPITransfer.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public class DirectPITransferController extends Controller {
	private static final String ERROR = "Error ";
	private static final String DELETED = "deleted";
	// 允许从前台传到后台的最大数据量
	private static final int MAX_LENGTH = 5 * 1024 * 1024;
	private static final Logger logger = LoggerFactory.getLogger(DirectPITransferController.class);

	/**
	 * 
	 * @return
	 */
	@SubjectPresent(handler = NoUserDeadboltHandler.class)
	@With(Menus.class)
	public static Result index() {
		return ok(directPITransfer.render(""));
	}

	/**
	 * 
	 *  
	 * @return
	 */
	public static Result viaPIGetQty() {
		logger.info("^^^^^^^^^^^^^^you have in viaPIGetQty method^^^^^^^^^^^^^^");
		RequestBody body = request().body();
		if (body.asJson() != null) {
			BigDecimal rate = null;
			HashMap<String, Object> map = new HashMap<String, Object>();
			String piNo = Json.fromJson(body.asJson(), String.class);
			List<Stock> stocks = BatchSearchUtil.viaPIGetStock(piNo);
			BigDecimal totalQty = new BigDecimal(0);
			for (Stock stock : stocks) {
				totalQty = totalQty.add(stock.qty);
			}
			// orderItem.settlementUom.id
			List<OrderItem> orderItems = Ebean.find(OrderItem.class).where().eq(DELETED, false).eq("order.internalOrderNo", piNo).findList();
			if (EmptyUtil.isNotEmptyList(orderItems)) {
				OrderItem orderItem = orderItems.get(0);
				HashMap<String, String> ext = ExtUtil.unapply(orderItem.ext);
				String skuUomId = ext.get("qtyPerPalletUom");
				map.put("qtyPerPallet", ext.get("qtyPerPallet"));
				map.put("mat", orderItem.material.materialName);
				rate = UnitConversion.SkuToQuantity(orderItem.settlementUom.id.toString(), skuUomId);
				logger.info("rate is " + rate);
			}
			totalQty = totalQty.multiply(rate);
			map.put("totalQty", totalQty);
			map.put("rate", rate);
			return ok(Json.toJson(map));
		} else {
			return ok(ERROR + "System Error,the system does not get reception parameters");
		}
	}

	/**
	 * 	
	 * @return
	 */
	public static Result print() {
		return ok();
	}

	/**
	 * 
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_LENGTH)
	public static Result executeAll() {
		logger.info("^^^^^^^^ you have in executeAll method ^^^^^^^^^^^^^^");
		RequestBody body = request().body();
		List<DirectTransferVo> dts = null;
		if (body.asJson() != null) {
			//logger.info("*******PalletDetail  body.asJson() ********" + body.asJson());
			ObjectMapper mapper = new ObjectMapper();
			try {
				dts = mapper.readValue(body.asJson(), new TypeReference<List<DirectTransferVo>>() {
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (dts != null && dts.size() > 2) {
				// 把from pi 中 相应数量的stock 删除或者修改
				HashMap<String, String> issue = new HashMap<String, String>();
				issue.put("transactionCode", CodeKey.TRANSACTION_CODE_DIRECTTRANSFER.toString());
				DirectTransferVo tempfrom = dts.get(dts.size() - 1);
				DirectTransferVo tempto = dts.get(dts.size() - 2);
				List<Stock> stocks = BatchSearchUtil.viaPIGetStock(tempfrom.piNo);
				BigDecimal skuTotal = tempfrom.qty.divide(tempfrom.rate, RoundingMode.HALF_UP);
				if (EmptyUtil.isNotEmptyList(stocks)) {
					for (Stock stock : stocks) {
						issue.put("toPI",tempto.piNo);
						if (skuTotal.compareTo(new BigDecimal(0)) > 0) {
							if (stock.qty.compareTo(skuTotal) <= 0) {
								skuTotal = skuTotal.subtract(stock.qty);
								CrudUtil.delete(stock);
								Execution execution = setExecution(stock, stock.qty, false, true);
								setStockTransaction(execution, issue, stock, stock.qty, false, true);
							} else {
								stock.qty = stock.qty.subtract(skuTotal);
								CrudUtil.update(stock);
								Execution execution = setExecution(stock, skuTotal, false, false);
								setStockTransaction(execution, issue, stock, skuTotal, false, false);
								skuTotal = skuTotal.subtract(stock.qty);
								break;
							}
						}
					}
				}
				// 给to pi 中新建stock
				HashMap<String, String> receive = new HashMap<String, String>();
				receive.put("transactionCode", CodeKey.TRANSACTION_CODE_DIRECTTRANSFER.toString());
				HashMap<String, String> map = new HashMap<String, String>();
				for (int i = 0, len = dts.size() - 1; i < len; i++) {
					Stock stock = dts.get(i).setStock();
					CrudUtil.save(stock);
					receive.put("fromPI", tempfrom.piNo);
					Execution execution = setExecution(stock, stock.qty, true, false);
					setStockTransaction(execution, receive, stock, stock.qty, true, false);
				}

			}
		}
		return ok("The operation was successful");
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
		HashMap<String, String> extMap = new HashMap<String,String>();
		extMap.put("fromPI", EmptyUtil.nullToEmpty(map.get("fromPI")));
		extMap.put("toPI", EmptyUtil.nullToEmpty(map.get("toPI")));
		stockTransaction.ext = ExtUtil.apply(extMap);
		CrudUtil.save(stockTransaction);
		// DataExchangePlatform.setTransaction(stockTransaction);
		return stockTransaction;
	}
	 
}
