package controllers.adjustment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.ExpressionList;

import models.Area;
import models.Batch;
import models.Bin;
import models.Execution;
import models.Stock;
import models.StockTransaction;
import models.StorageType;
import models.Warehouse;
import models.vo.Result.ResultVo;
import models.vo.adjustment.AreaVo;
import models.vo.adjustment.BinVo;
import models.vo.adjustment.StockCollectionVo;
import models.vo.adjustment.StockSearchVo;
import models.vo.adjustment.StockVo;
import models.vo.adjustment.StorageTypeVo;
import models.vo.inbound.WarehouseVo;
import security.NoUserDeadboltHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.BatchSearchUtil;
import utils.CrudUtil;
import utils.DateUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import utils.Service.DataExchangePlatform;
import utils.exception.CustomException;
import views.html.adjustment.stockAdjustment;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.*;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: StockAdjustController.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
@SubjectPresent(handler = NoUserDeadboltHandler.class)
public class StockAdjustController extends Controller {

	private static final Logger logger = LoggerFactory.getLogger(StockAdjustController.class);
	static String CHECKTYPEONE = "location";// 按照location groupBy
	static String CHECKTYPETWO = "batch";// 按照batch groupBy

	// **********code + status************
	static String EXECUTION_TYPE_4 = "T004";// 内部调整
	static String EXECUTION_SUBTYPE_5 = "T004.999";// 其他调整
	static String TRANSACTION_CODE__6 = "T501";// adjust
	// 允许从前台传到后台的最大数据量
	private static final int MAX_LENGTH = 10 * 1024 * 1024;

	/**
	 * 页面初始化
	 * @return
	 */
	@With(Menus.class)
	public static Result index() {
		return ok(stockAdjustment.render(""));
	}

	/**
	 * 初始化页面查询warehouse
	 * @return
	 */
	public static Result initWarehouse() {
		List<WarehouseVo> warehouseVoList = new ArrayList<WarehouseVo>();
		if (SessionSearchUtil.searchWarehouse() == null) {
			logger.error(">>>>>>>>>> initWarehouse>>>> session is null");
		} else {
			String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
			List<Warehouse> warehouseList = Warehouse.find().where().eq("deleted", false).eq("id", warehouseId).findList();
			if (warehouseList.size() > 0) {
				for (Warehouse warehouse : warehouseList) {
					WarehouseVo warehouseVo = new WarehouseVo();
					warehouseVo.id = warehouse.id.toString();
					warehouseVo.nameKey = warehouse.nameKey;
					warehouseVoList.add(warehouseVo);
				}
			} else {
				return ok(play.libs.Json.toJson(new ResultVo("error", "Can not get the warehouse!")));
			}
		}
		return ok(play.libs.Json.toJson(new ResultVo(warehouseVoList)));
	}

	/**
	 * 查询StorageType
	 * @return
	 */
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result changeStorageType() {
		JsonNode body = request().body().asJson();
		if (body != null) {
			List<StorageTypeVo> storageTypeVoList = new ArrayList<StorageTypeVo>();
			if (SessionSearchUtil.searchWarehouse() == null) {
				logger.info(">>>>>>>>> initWarehouse>>>> session is null");
			} else {
				String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
				StockSearchVo stockSearchVo = Json.fromJson(body, StockSearchVo.class);
				if (!warehouseId.equals(stockSearchVo.warehouse)) {
					return ok(play.libs.Json
							.toJson(new ResultVo("error", "Error : Warehouse information does not match with the current warehouse!")));
				} else {
					List<StorageType> storageTypeList = StorageType.find().where().eq("deleted", false).eq("warehouse.deleted", false)
							.eq("warehouse.id", stockSearchVo.warehouse).findList();
					for (StorageType storageType : storageTypeList) {
						StorageTypeVo storageTypeVo = new StorageTypeVo();
						storageTypeVo.id = storageType.id.toString();
						storageTypeVo.nameKey = storageType.nameKey;
						storageTypeVoList.add(storageTypeVo);
					}
				}
			}
			return ok(play.libs.Json.toJson(new ResultVo(storageTypeVoList)));
		} else {
			logger.error("+++++++Stock Adjust Qty +++++++searchStorageType++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo("error", "Can not find the storage type in the warehouse!")));
		}
	}

	/**
	 * 初始化头查询的area
	 * @return
	 */
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result changeArea() {
		JsonNode body = request().body().asJson();
		if (body != null) {
			List<AreaVo> areaVoList = new ArrayList<AreaVo>();
			StockSearchVo stockSearchVo = Json.fromJson(body, StockSearchVo.class);
			if (SessionSearchUtil.searchWarehouse() == null) {
				logger.info(">>>>>>>>>>> initWarehouse>>>> session is null");
			} else {
				String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
				if (!warehouseId.equals(stockSearchVo.warehouse)) {
					return ok(play.libs.Json
							.toJson(new ResultVo("error", "Error : Warehouse information does not match with the current warehouse!")));
				} else {
					List<Area> areaList = Area.find().where().eq("deleted", false).eq("storageType.id", stockSearchVo.storageType)
							.eq("storageType.warehouse.id", stockSearchVo.warehouse).eq("storageType.warehouse.deleted", false).orderBy("nameKey")
							.findList();
					for (Area area : areaList) {
						AreaVo areaVo = new AreaVo();
						areaVo.id = area.id.toString();
						areaVo.nameKey = area.nameKey;
						areaVoList.add(areaVo);
					}
				}
			}
			return ok(play.libs.Json.toJson(new ResultVo(areaVoList)));
		} else {
			logger.error("+++++++Stock Adjust Qty +++++++searchArea++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo("error", "Can not find the area in the warehouse!")));
		}
	}

	/**
	 * 初始化头查询的bin
	 * @return
	 */
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result changeBin() {
		JsonNode body = request().body().asJson();
		if (body != null) {
			List<BinVo> binVoList = new ArrayList<BinVo>();
			if (SessionSearchUtil.searchWarehouse() == null) {
				logger.info(">>>>>>>>>>>> initWarehouse>>>> session is null");
			} else {
				String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
				StockSearchVo stockSearchVo = Json.fromJson(body, StockSearchVo.class);
				if (!warehouseId.equals(stockSearchVo.warehouse)) {
					return ok(play.libs.Json.toJson(new ResultVo("error", "Error : Warehouse information does not match with the current warehouse")));
				} else {
					List<Bin> binList = Bin.find().where().eq("deleted", false).eq("area.storageType.warehouse.deleted", false)
							.eq("area.storageType.warehouse.id", stockSearchVo.warehouse).eq("area.deleted", false).eq("area.id", stockSearchVo.area)
							.orderBy("nameKey").findList();
					for (Bin bin : binList) {
						BinVo binVo = new BinVo();
						binVo.id = bin.id.toString();
						binVo.nameKey = bin.nameKey;
						binVoList.add(binVo);
					}
				}
			}
			return ok(play.libs.Json.toJson(new ResultVo(binVoList)));
		} else {
			logger.error("+++++++Stock Adjust Qty +++++++searchBin++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo("error", "Can not find the bin in the warehouse!")));
		}
	}

	/**
	 * 头查询ByLocation
	 * @return
	 */
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result searchStockCollectByLocation() {
		logger.info("+++Stock Adjust Qty+++++++searchStockCollectByLocation++++start++++");
		JsonNode body = request().body().asJson();
		if (body != null) {
			List<StockCollectionVo> stockCollectList = new ArrayList<StockCollectionVo>();
			if (SessionSearchUtil.searchWarehouse() == null) {
				logger.info(">>>>>>>>>>> initWarehouse>>>> session is null");
			} else {
				String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
				StockSearchVo stockSearchVo = Json.fromJson(body, StockSearchVo.class);
				if (!warehouseId.equals(stockSearchVo.warehouse)) {
					return ok(play.libs.Json.toJson(new ResultVo("error", "Error : Warehouse information does not match with the current warehouse")));
				} else {
					if (CHECKTYPEONE.equals(stockSearchVo.checkType)) {
						// 按照location排序
						ExpressionList<Stock> searchStock = Stock.find().where().eq("deleted", false).eq("bin.deleted", false)
								.eq("area.deleted", false).eq("warehouse.deleted", false).eq("warehouse.id", warehouseId)
								.eq("area.storageType.deleted", false).eq("area.storageType.warehouse.deleted", false);
						if (StringUtils.isNotEmpty(stockSearchVo.storageType)) {
							searchStock = searchStock.eq("area.storageType.id", stockSearchVo.storageType);
						}
						if (StringUtils.isNotEmpty(stockSearchVo.area)) {
							searchStock = searchStock.eq("area.id", stockSearchVo.area);
						}
						if (StringUtils.isNotEmpty(stockSearchVo.bin)) {
							searchStock = searchStock.eq("bin.id", stockSearchVo.bin);
						}
						List<Stock> stockList = searchStock.findList();
						for (int i = 0; i < stockList.size(); i++) {
							if (stockCollectList.size() < 1) {
								StockCollectionVo stockCollectionVo = new StockCollectionVo();
								stockCollectionVo.filledVoLoc(stockList.get(i));
								stockCollectList.add(stockCollectionVo);
							} else {
								boolean asure = false;
								for (int j = 0; j < stockCollectList.size(); j++) {
									if (stockList.get(i).material.id.toString().equals(stockCollectList.get(j).materialId)
											&& stockList.get(i).bin.id.toString().equals(stockCollectList.get(j).binId)
											&& stockList.get(i).materialUom.id.toString().equals(stockCollectList.get(j).quantityUomId)
											&& ExtUtil.unapply(stockList.get(i).batch.ext).get("pi").equals(stockCollectList.get(j).pino)) {
										stockCollectList.get(j).systemQuantity = stockCollectList.get(j).systemQuantity.add(stockList.get(i).qty);
										stockCollectList.get(j).adjusterQuantity = null;
										asure = true;
									}
								}
								if (asure) {
									continue;
								} else {
									StockCollectionVo stockCollectionVo = new StockCollectionVo();
									stockCollectionVo.filledVoLoc(stockList.get(i));
									stockCollectList.add(stockCollectionVo);
								}
							}
						}
					}
				}
			}
			logger.info("+++Stock Adjust Qty+++++++searchStockCollectByLocation++++end++++");
			return ok(play.libs.Json.toJson(new ResultVo(stockCollectList)));
		} else {
			logger.error("+++++++Stock Adjust Qty +++++++searchStockCollectByLocation++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo("error", "Can't get the query conditions!")));
		}
	}

	/**
	 * 头查询ByBatch
	 * @return
	 */
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result searchStockCollectByBatch() {
		logger.info("+++Stock Adjust Qty+++++++searchStockCollectByBatch++++start++++");
		JsonNode body = request().body().asJson();
		List<StockCollectionVo> stockCollectList = new ArrayList<StockCollectionVo>();
		if (body != null) {
			if (SessionSearchUtil.searchWarehouse() == null) {
				logger.info(">>>>>>>>>> initWarehouse>>>> session is null");
			} else {
				String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
				StockSearchVo stockSearchVo = Json.fromJson(body, StockSearchVo.class);
				if (!warehouseId.equals(stockSearchVo.warehouse)) {
					return ok(play.libs.Json.toJson(new ResultVo("error", "Error : Warehouse information does not match with the current warehouse")));
				} else {
					if (CHECKTYPETWO.equals(stockSearchVo.checkType) || stockSearchVo.checkType != null) {
						// 按照location排序
						ExpressionList<Stock> searchStock = Stock.find().where().eq("deleted", false).eq("bin.deleted", false)
								.eq("area.deleted", false).eq("warehouse.deleted", false).eq("warehouse.id", warehouseId)
								.eq("area.storageType.deleted", false).eq("area.storageType.warehouse.deleted", false);
						if (StringUtils.isNotEmpty(stockSearchVo.storageType)) {
							searchStock = searchStock.eq("area.storageType.id", stockSearchVo.storageType);
						}
						if (StringUtils.isNotEmpty(stockSearchVo.area)) {
							searchStock = searchStock.eq("area.id", stockSearchVo.area);
						}
						if (StringUtils.isNotEmpty(stockSearchVo.bin)) {
							searchStock = searchStock.eq("bin.id", stockSearchVo.bin);
						}
						List<Stock> stockList = searchStock.findList();
						for (int i = 0; i < stockList.size(); i++) {
							if (stockCollectList.size() < 1) {
								StockCollectionVo stockCollectionVo = new StockCollectionVo();
								stockCollectionVo.filledVoBat(stockList.get(i));
								stockCollectList.add(stockCollectionVo);
							} else {
								boolean asure = false;
								for (int j = 0; j < stockCollectList.size(); j++) {
									if (stockList.get(i).material.id.toString().equals(stockCollectList.get(j).materialId)
											&& stockList.get(i).materialUom.id.toString().equals(stockCollectList.get(j).quantityUomId)
											&& ExtUtil.unapply(stockList.get(i).batch.ext).get("lot").equals(stockCollectList.get(j).batchNo)
											&& ExtUtil.unapply(stockList.get(i).batch.ext).get("pi").equals(stockCollectList.get(j).pino)) {
										stockCollectList.get(j).systemQuantity = stockCollectList.get(j).systemQuantity.add(stockList.get(i).qty);
										stockCollectList.get(j).adjusterQuantity = null;
										asure = true;
									}
								}
								if (asure) {
									continue;
								} else {
									StockCollectionVo stockCollectionVo = new StockCollectionVo();
									stockCollectionVo.filledVoBat(stockList.get(i));
									stockCollectList.add(stockCollectionVo);
								}
							}
						}
					}
				}
			}
			logger.info("+++Stock Adjust Qty+++++++searchStockCollectByBatch++++end++++");
			return ok(play.libs.Json.toJson(new ResultVo(stockCollectList)));
		} else {
			logger.error("+++++++Stock Adjust Qty +++++++searchStockCollectByBatch++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo("error", "Can't get the query conditions!")));
		}
	}

	/**
	 * 查询StockList by location
	 * @return
	 */
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result getStockList() {
		logger.info("+++Stock Adjust Qty+++++++getStockListByLocation++++start++++");
		JsonNode body = request().body().asJson();
		if (body != null) {
			List<StockVo> stockVoList = new ArrayList<StockVo>();
			if (SessionSearchUtil.searchWarehouse() == null) {
				logger.info(">>>>>>>>>>> initWarehouse>>>> session is null");
			} else {
				String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
				StockCollectionVo stockCollectionVo = Json.fromJson(body, StockCollectionVo.class);
				if (!warehouseId.equals(stockCollectionVo.warehouseId)) {
					return ok(play.libs.Json.toJson(new ResultVo("error", "Error : Warehouse information does not match with the current warehouse")));
				} else {
					if (StringUtils.isNotEmpty(stockCollectionVo.areaId) && StringUtils.isNotEmpty(stockCollectionVo.binId)
							&& StringUtils.isNotEmpty(stockCollectionVo.materialId) && StringUtils.isNotEmpty(stockCollectionVo.quantityUomId)
							&& StringUtils.isNotEmpty(stockCollectionVo.pino)) {
						List<Batch> batchList = BatchSearchUtil.getBatch(stockCollectionVo.pino);
						List<Stock> stockList = Stock.find().where().eq("deleted", false).eq("warehouse.deleted", false)
								.eq("warehouse.id", warehouseId).eq("area.deleted", false).eq("area.id", stockCollectionVo.areaId)
								.eq("bin.deleted", false).eq("bin.id", stockCollectionVo.binId).eq("material.deleted", false)
								.eq("material.id", stockCollectionVo.materialId).eq("materialUom.deleted", false).in("batch", batchList)
								.eq("materialUom.id", stockCollectionVo.quantityUomId).findList();
						for (Stock stock : stockList) {
							StockVo stockVo = new StockVo();
							stockVo.fillVo(stock);
							stockVoList.add(stockVo);
						}
					} else {
						return ok(play.libs.Json.toJson(new ResultVo("error", "Error data!")));
					}
				}
			}
			logger.info("+++Stock Adjust Qty+++++++getStockListByLocation++++end++++");
			return ok(play.libs.Json.toJson(new ResultVo(stockVoList)));
		} else {
			logger.error("+++++++Stock Adjust Qty +++++++getStockListByLocation++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo("error", "Can't get the query conditions!")));
		}
	}

	/**
	 * 查询StockList by batch
	 * @return
	 */
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result getStockListBt() {
		logger.info("+++Stock Adjust Qty+++++++getStockListByBatch++++start++++");
		JsonNode body = request().body().asJson();
		if (body != null) {
			List<StockVo> stockVoList = new ArrayList<StockVo>();
			if (SessionSearchUtil.searchWarehouse() == null) {
				logger.info(">>>>>>>>>>> initWarehouse>>>> session is null");
			} else {
				String warehouseId = SessionSearchUtil.searchWarehouse().id.toString();
				StockCollectionVo stockCollectionVo = Json.fromJson(body, StockCollectionVo.class);
				if (!warehouseId.equals(stockCollectionVo.warehouseId)) {
					return ok(play.libs.Json.toJson(new ResultVo("error", "Error : Warehouse information does not match with the current warehouse")));
				} else {
					if (StringUtils.isNotEmpty(stockCollectionVo.materialId) && StringUtils.isNotEmpty(stockCollectionVo.quantityUomId)
							&& StringUtils.isNotEmpty(stockCollectionVo.pino) && StringUtils.isNotEmpty(stockCollectionVo.batchNo)) {
						List<Batch> batchList = BatchSearchUtil.getBatch(stockCollectionVo.pino, stockCollectionVo.batchNo);
						List<Stock> stockList = Stock.find().where().eq("deleted", false).eq("warehouse.deleted", false)
								.eq("warehouse.id", warehouseId).eq("batch.deleted", false).in("batch", batchList).eq("material.deleted", false)
								.eq("material.id", stockCollectionVo.materialId).eq("materialUom.deleted", false)
								.eq("materialUom.id", stockCollectionVo.quantityUomId).findList();
						for (Stock stock : stockList) {
							StockVo stockVo = new StockVo();
							stockVo.fillVo(stock);
							stockVoList.add(stockVo);
						}
					} else {
						return ok(play.libs.Json.toJson(new ResultVo("error", "Error data!")));
					}
				}
			}
			logger.info("+++Stock Adjust Qty+++++++getStockListByBatch++++end++++");
			return ok(play.libs.Json.toJson(new ResultVo(stockVoList)));
		} else {
			logger.error("+++++++Stock Adjust Qty +++++++getStockListByBatch++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo("error", "Can't get the query conditions!")));
		}
	}

	/**
	 * 执行调整
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_LENGTH)
	public static Result execution() {
		logger.info("+++Stock Adjust Qty+++++++execution++++start++++");
		JsonNode body = request().body().asJson();
		if (body != null) {
			ObjectMapper mapper = new ObjectMapper();
			List<StockVo> stockVoList = new ArrayList<StockVo>();
			try {
				stockVoList = mapper.readValue(body, new TypeReference<List<StockVo>>() {
				});
			} catch (Exception e) {
				e.printStackTrace();
				return ok(play.libs.Json.toJson(new ResultVo("error", "Data anomalies")));
			}
			if (stockVoList.size() > 0) {
				for (StockVo stockVo : stockVoList) {
					try {
						Stock stock = Stock.find().where().eq("deleted", false).eq("id", stockVo.id).findUnique();
						isExcepitonNull(stock, "Can not find the stock in the database!");
						isExcepitonNull(stockVo.qty, "Adjust quantity is null!");
						if (stockVo.id.equals(stock.id.toString()) && stockVo.adjustQty.compareTo(stock.qty) != 0) {
							// 只调整数量改变的
							StockTransaction stockTransaction = new StockTransaction();
							stockTransaction.oldArrivedAt = stock.arrivedAt;
							stockTransaction.oldStatus = stock.stockStatus;
							isExcepitonNull(stockVo.adjustQty, "Can not get the adjustQty data!");
							isExcepitonNull(stock.area, "Can not get the area data in the stock!");
							isExcepitonNull(stock.bin, "Can not get the bin data in the stock!");
							isExcepitonNull(stock.material, "Can not get the material data in the stock!");
							isExcepitonNull(stock.materialUom, "Can not get the materialUom data in the stock!");
							isExcepitonNull(stock.warehouse, "Can not get the warehouse in the stock!");
							if (stockVo.reason.trim() == null || "".equals(stockVo.reason.trim())) {
								return ok(play.libs.Json.toJson(new ResultVo("error", "The adjust reason is null!")));
							}
							stock.qty = stockVo.adjustQty;
							CrudUtil.update(stock);
							Execution execution = new Execution();
							execution.material = stock.material;
							execution.fromMaterialUom = stock.materialUom;
							execution.toMaterialUom = stock.materialUom;
							execution.executedQty = stockVo.adjustQty;
							// 暂时未有pallet信息，先不加验证
							if (stock.palletType != null) {
								execution.fromPalletType = stock.palletType;
								execution.toPalletType = stock.palletType;
							}
							if (stock.pallet != null) {
								execution.fromPallet = stock.pallet;
								execution.toPallet = stock.pallet;
							}
							// *********************************
							execution.fromArea = stock.area;
							execution.toArea = stock.area;
							execution.fromBin = stock.bin;
							execution.toBin = stock.bin;
							execution.executionType = EXECUTION_TYPE_4;
							execution.executionSubtype = EXECUTION_SUBTYPE_5;
							execution.executedAt = DateUtil.currentTimestamp();
							CrudUtil.save(execution);
							if (SessionSearchUtil.searchWarehouse() != null) {
								stockTransaction.warehouse = SessionSearchUtil.searchWarehouse();
							}
							stockTransaction.newArrivedAt = DateUtil.currentTimestamp();
							stockTransaction.newStatus = stock.stockStatus;
							stockTransaction.stock = stock;
							stockTransaction.execution = execution;
							stockTransaction.warehouse = stock.warehouse;
							stockTransaction.oldUomId = stock.materialUom.id;
							stockTransaction.newUomId = stock.materialUom.id;
							stockTransaction.oldAreaId = stock.area.id;
							stockTransaction.newAreaId = stock.area.id;
							stockTransaction.oldBinId = stock.bin.id;
							stockTransaction.newBinId = stock.bin.id;
							stockTransaction.oldQty = stockVo.qty;
							stockTransaction.newQty = stockVo.adjustQty;
							stockTransaction.remarks = stockVo.reason.trim();
							stockTransaction.transactionCode = TRANSACTION_CODE__6;
							stockTransaction.transactionAt = DateUtil.currentTimestamp();
							// 与SAP的数据交互
							DataExchangePlatform.setTransaction(stockTransaction);
							CrudUtil.save(stockTransaction);
						}
					} catch (CustomException e) {
						e.printStackTrace();
						return ok(play.libs.Json.toJson(new ResultVo("error", e.getMessage())));
					}
				}
			} else {
				logger.error("++++Stock Adjust Qty+++++++execution++++Can not get the Stocks data++++++++");
				return ok(play.libs.Json.toJson(new ResultVo("error", "Can not get the stocks data from the page!")));
			}
			logger.info("+++Stock Adjust Qty+++++++execution++++end++++");
			return ok(play.libs.Json.toJson(new ResultVo("Congratulations : Stocks adjust successfully!")));
		} else {
			logger.error("++++Stock Adjust Qty+++++++execution++++josn body in null++++++++");
			return ok(play.libs.Json.toJson(new ResultVo("error", "Can not get the stocks data from the page!")));
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
}
