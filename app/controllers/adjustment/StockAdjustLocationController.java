/** * StockAdjustLocationController.java 
* Created on 2013-6-22 上午9:10:02 
*/

package controllers.adjustment;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;

import models.Area;
import models.Batch;
import models.Bin;
import models.EmployeeWarehouse;
import models.Execution;
import models.PlanItem;
import models.PlanItemDetail;
import models.Stock;
import models.StockTransaction;
import models.StorageType;
import models.vo.adjustment.SearchVo;
import models.vo.adjustment.StockAdjustLocationVo;
import models.vo.arrangement.StockVo;
import models.vo.transfer.MapVo;
import models.vo.transfer.PalletDetail;
import models.vo.transfer.SortUtil;
import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import play.Logger;

import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.With;
import security.NoUserDeadboltHandler;
import utils.BatchSearchUtil;
import utils.CodeUtil;
import utils.CrudUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import utils.TypeAndStatusUtil;
import utils.enums.CodeKey;
import utils.exception.CustomException;
import views.html.adjustment.stockAdjustLocation;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: StockAdjustLocationController.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

@SubjectPresent(handler = NoUserDeadboltHandler.class)
public class StockAdjustLocationController extends Controller {

	private static final String DELETED = "deleted";
	
	/**
	 * 
	 * @return
	 */
	@With(Menus.class)
	public static Result index() {
		return ok(stockAdjustLocation.render(""));
	}

	/**
	 * 
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result list() {
		Logger.info("^^^^^^^^^^^^^^^^ you have in list method ^^^^^^^^^^^^^^^^");
		RequestBody body = request().body();
		SearchVo searchVo= null;
		if (body.asJson()!= null) {
			Logger.info("^^^^^^^body.asJson()!=null^^^^^^^");
			searchVo = Json.fromJson(body.asJson(), SearchVo.class);
		} else {
			Logger.info("^^^^^^^body.asJson()==null^^^^^^^");
			searchVo = new SearchVo();
		}
			//Logger.info("Json.toJson  : " + Json.toJson(searchVo));
			List<StockAdjustLocationVo> vos = new ArrayList<StockAdjustLocationVo>();
			List<Stock> stocks = new ArrayList<Stock>();
			boolean piAndBatch = true;
//			ExpressionList<Stock> el = null;
			/*// 只有piNo
			if ((searchVo.piNo != null && !"".equals(searchVo.piNo)) && (searchVo.batchNo == null || "".equals(searchVo.batchNo))) {
				batchs = BatchSearchUtil.getlikeBatch(searchVo.piNo);
				piAndBatch = false;
			}
			// piNo batchNo两个都有
			if ((searchVo.piNo != null || !"".equals(searchVo.piNo)) && (searchVo.batchNo != null && !"".equals(searchVo.batchNo))) {
				batchs = BatchSearchUtil.getBatch(searchVo.piNo, searchVo.batchNo);
				piAndBatch = false;
			}
			if (piAndBatch) {
				el = getEl(searchVo);
			}*/
			Map<String,String> params = new HashMap<String,String>();
			params.put("pi_like",searchVo.piNo);
			params.put("lot_like",searchVo.batchNo);
			params.put("date", searchVo.date);
			List<Batch> batchs = BatchSearchUtil.getBatchsByCondition(params);
			if (batchs != null && !batchs.isEmpty()) {// 查询到相关batch
				Logger.info("batchs.size  : " + batchs.size() );
				for (Batch batch : batchs) {
					ExpressionList<Stock> elPiAndBatch = getEl(searchVo);
					List<Stock> tempStocks = elPiAndBatch.eq("batch.id", batch.id).findList();
					if (tempStocks != null && !tempStocks.isEmpty()) {
						stocks.addAll(tempStocks);
					}
				}
			} 

			if (stocks != null && !stocks.isEmpty()) {
				for (Stock stock : stocks) {
					StockAdjustLocationVo vo = new StockAdjustLocationVo();
					vo.setStockAdjustLocationVo(stock);
					vos.add(vo);
				}
			}
			try {
				vos = SortUtil.sortList(vos);
			} catch (CustomException e) {
				e.printStackTrace();
			}
			//Logger.info(" return vos ------------" + Json.toJson(vos));
			return ok(Json.toJson(vos));
		
		 
	}

	private static ExpressionList<Stock> getEl(SearchVo searchVo) {
		ExpressionList<Stock> el = Ebean.find(Stock.class).fetch("batch").fetch("material").fetch("materialUom").fetch("area")
			.fetch("area.storageType").fetch("bin").where().eq(DELETED, false);
		if (searchVo.storageType != null && !"".equals(searchVo.storageType)) {
			el.eq("area.storageType.id", searchVo.storageType);
		}
		if (searchVo.area != null && !"".equals(searchVo.area)) {
			el.eq("area.id", searchVo.area);
		}
		if (searchVo.bin != null && !"".equals(searchVo.bin)) {
			el.eq("bin.id", searchVo.bin);
		}
		return el;
	}
   
	public static Result getStoryType(){
		Logger.info("you have in getStoryType method");
		Ebean.find(StorageType.class).where().eq(DELETED, false).eq("warehouse", SessionSearchUtil.searchWarehouse());
		return ok();
	}
	/**
	 * 
	 * @param whNameKey
	 * @return
	 */
	@Transactional
	public static Result getToAreas(String id) {
		Logger.info("you have in getToAreas method");
		List<Area> areaEntitys = Ebean.find(Area.class).fetch("warehouse", "nameKey").where().eq("deleted", false)
			.eq("warehouse", SessionSearchUtil.searchWarehouse()).eq("storageType.id", id).order("nameKey ascending").findList();
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
	 * @param areaNameKey
	 * @return
	 */
	public static Result getToBins(String id) {
		System.out.println("^^^^^^^^^^^^you have in getBins method^^^^^^^^^^^" + id);

		List<Bin> binEntitys = Ebean.find(Bin.class).fetch("area").where().eq("deleted", false).eq("area.id", id).findList();
		List<MapVo> bins = new ArrayList<MapVo>();
		if (binEntitys != null && !binEntitys.isEmpty()) {
			for (Bin binEntity : binEntitys) {
				MapVo bin = new MapVo();
				bin.key = String.valueOf(binEntity.id);
				bin.descripton = binEntity.nameKey;
				bins.add(bin);

			}
		}
		//System.out.println("^^^^^^^^^^^^tobins messages^^^^^^  " + play.libs.Json.toJson(bins));
		return ok(play.libs.Json.toJson(bins));
	}

	/**
	 * 
	 * @param whNameKey
	 * @return
	 */
	@Transactional
	public static Result getEmployee() {
		List<EmployeeWarehouse> employWhs = Ebean.find(EmployeeWarehouse.class).fetch("employee").fetch("employee.employeeType")
			.fetch("warehouse", "nameKey").where().where().eq("deleted", false).eq("warehouse", SessionSearchUtil.searchWarehouse())
			.eq("employee.employeeType.typeCode", CodeKey.EMPLOY_TYPE_FORKLIFTDRIVE.toString()).eq("employee.deleted", false).order("employee.employeeName ascending")
			.findList();
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
	 * @return
	 */
	@Transactional
	public static Result transferStock() {
		Logger.info("^^^^^^^^^^^^^^^^ you have in transferStock method ^^^^^^^^^^^^^^^^");
		ObjectMapper mapper = new ObjectMapper();
		RequestBody body = request().body();
		Stock tempStock = null;
		List<StockAdjustLocationVo> stockVos = null;
		if (body.asJson() != null) {
			//Logger.info("^^^^List<StockVo> asJson^^^^^^" + body.asJson());
			try {
				stockVos = mapper.readValue(body.asJson(), new TypeReference<List<StockAdjustLocationVo>>() {
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 根据stock直接execution
			if (stockVos != null && !stockVos.isEmpty()) {
				for (StockAdjustLocationVo vo : stockVos) {
					tempStock = Ebean.find(Stock.class).where().eq(DELETED, false).eq("id", vo.id).findUnique();
					Area area = Ebean.find(Area.class).where().eq(DELETED, false).eq("id", vo.areaTo).findUnique();
					Bin bin = Ebean.find(Bin.class).where().eq(DELETED, false).eq("id", vo.binTo).findUnique();
					Execution execution = generateExecution(tempStock, area, bin, vo.forkLiftDriver);
					generateStockTransaction(execution, tempStock);
					tempStock.area = area;
					tempStock.bin = bin;
					CrudUtil.update(tempStock);
				}
			}
			return ok("The operation was successful");
		} else {
			return ok("System Error");
		}
	}

	/**
	 * 
	 * @param planItem
	 * @param stock
	 * @param pd  
	 * @return
	 */
	public static Execution generateExecution(Stock stock, Area area, Bin bin, String forkLiftDriver) {
		Execution execution = new Execution();
		HashMap<String, String> map = new HashMap<String, String>();
		execution.executionType = CodeKey.EXECUTION_TYPE_TRANSFER.toString();
		execution.executionSubtype = CodeKey.EXECUTION_TYPE_TRANSFER.toString();
		execution.reverse = false;
		execution.reversed = false;
		execution.refExecution = null;
		execution.executedBy = null;
		execution.executedQty = stock.qty;
		execution.material = stock.material;
		execution.fromMaterialUom = stock.materialUom;
		execution.fromArea = stock.area;
		execution.fromBin = stock.bin;
		execution.fromPalletType = null;
		// execution.fromPallet = Pallet.find().byId(pd.palletId);
		execution.toMaterialUom = stock.materialUom;
		execution.toArea = area;
		execution.toBin = bin;
		execution.toPalletType = null;
		execution.toPallet = null;
		// execution.seqNo
		execution.remarks = null;
		execution.deleted = false;
		map.put("FD", forkLiftDriver);
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
	public static void generateStockTransaction(Execution execution, Stock stock) {
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
}
