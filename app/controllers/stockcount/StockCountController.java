package controllers.stockcount;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.ExpressionList;

import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import models.Area;
import models.Batch;
import models.Bin;
import models.BinCapacity;
import models.Material;
import models.MaterialUom;
import models.Order;
import models.Stock;
import models.StockCount;
import models.StockCountBin;
import models.StockCountItem;
import models.StorageType;
import models.Warehouse;
import models.printVo.Cell;
import models.printVo.Title;
import models.printVo.StockCount.StockCountDateByBatch;
import models.printVo.StockCount.StockCountDateByLocation;
import models.printVo.StockCount.StockCountReport;
import models.vo.outbound.batchVo;
import models.vo.query.AreaVo;
import models.vo.query.BatchVo;
import models.vo.query.BinVo;
import models.vo.query.ReportBinVo;
import models.vo.query.StockCountVo;
import models.vo.query.StockVo;
import models.vo.query.StorageTypeVo;
import models.vo.query.WarehouseQueryVo;
import models.vo.query.WarehouseVo;
import play.data.Form;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;
import play.mvc.With;
import play.mvc.Http.RequestBody;
import security.NoUserDeadboltHandler;
import utils.BatchSearchUtil;
import utils.DateUtil;
import utils.EmptyUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import utils.Excel.ExcelObj;
import utils.Excel.ReadExcel;

@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class StockCountController extends Controller {
	// static String warehouseId = "fe640b63-5501-44b0-9fb1-6bbd93c5e8e5";
	static String countType = "T002";
	static String NEW = "S000";
	private static final Logger logger = LoggerFactory.getLogger(StockCountController.class);

	public static Result index() {
		return ok(views.html.stockcount.StockCount.render(""));
	}

	public static Result queryIndex() {
		return ok(views.html.stockcount.StockQuery.render(""));
	}

	public static Result initBin() {
		System.out.println("================init====================");
		List<BinVo> binVos = new ArrayList<BinVo>();
		List<AreaVo> areaVos = new ArrayList<AreaVo>();
		List<StorageTypeVo> storageTypeVos = new ArrayList<StorageTypeVo>();
		List<WarehouseVo> warehouseVos = new ArrayList<WarehouseVo>();
		// List<Bin> bins = Bin.find().where().eq("deleted",
		// false).eq("area.deleted", false).eq("area.storageType.deleted",
		// false).eq("area.storageType.warehouse.deleted",
		// false).eq("area.storageType.warehouse.id",
		// SessionSearchUtil.searchWarehouse().id.toString()).findList();
		List<Warehouse> warehouses = Warehouse.find().where().eq("deleted", false).eq("id", SessionSearchUtil.searchWarehouse().id.toString())
			.findList();
		for (Warehouse warehouse : warehouses) {
			WarehouseVo warehouseVo = new WarehouseVo();
			warehouseVo.inwarehouse(warehouse);
			warehouseVos.add(warehouseVo);
			List<StorageType> storageTypes = StorageType.find().where().eq("deleted", false).eq("warehouse", warehouse).findList();
			for (StorageType storageType : storageTypes) {
				StorageTypeVo storageTypeVo = new StorageTypeVo();
				storageTypeVo.inStorageTypeVo(storageType);
				storageTypeVo.warehouseVo = warehouseVo;
				storageTypeVos.add(storageTypeVo);
				List<Area> areas = Area.find().where().eq("deleted", false).eq("storageType", storageType).findList();
				for (Area area : areas) {
					AreaVo areaVo = new AreaVo();
					areaVo.inArea(area);
					areaVo.storageTypeVo = storageTypeVo;
					areaVos.add(areaVo);
					List<Bin> bins = Bin.find().where().eq("deleted", false).eq("area", area).findList();
					for (Bin bin : bins) {
						BinVo binVo = new BinVo();
						binVo.inBin(bin);
						binVo.areaVo = areaVo;
						binVos.add(binVo);
					}
				}
			}
		}

		/*
		 * for (Bin bin : bins) { BinVo binVo = new BinVo(); AreaVo areaVo = new
		 * AreaVo(); StorageTypeVo storageTypeVo = new StorageTypeVo();
		 * WarehouseVo warehouseVo = new WarehouseVo(); binVo.inBin(bin);
		 * areaVo.inArea(bin.area);
		 * storageTypeVo.inStorageTypeVo(bin.area.storageType);
		 * warehouseVo.inwarehouse(bin.area.storageType.warehouse);
		 * storageTypeVo.warehouseVo=warehouseVo;
		 * areaVo.storageTypeVo=storageTypeVo; binVo.areaVo=areaVo;
		 * binVos.add(binVo); }
		 */
		System.out.println("===========binVos============" + binVos.size());
		return ok(play.libs.Json.toJson(binVos));
	}

	public static Result initArea() {
		List<AreaVo> areaVos = new ArrayList<AreaVo>();
		List<StorageTypeVo> storageTypeVos = new ArrayList<StorageTypeVo>();
		List<WarehouseVo> warehouseVos = new ArrayList<WarehouseVo>();
		// List<Bin> bins = Bin.find().where().eq("deleted",
		// false).eq("area.deleted", false).eq("area.storageType.deleted",
		// false).eq("area.storageType.warehouse.deleted",
		// false).eq("area.storageType.warehouse.id",
		// SessionSearchUtil.searchWarehouse().id.toString()).findList();
		List<Warehouse> warehouses = Warehouse.find().where().eq("deleted", false).eq("id", SessionSearchUtil.searchWarehouse().id.toString())
			.findList();
		for (Warehouse warehouse : warehouses) {
			WarehouseVo warehouseVo = new WarehouseVo();
			warehouseVo.inwarehouse(warehouse);
			warehouseVos.add(warehouseVo);
			List<StorageType> storageTypes = StorageType.find().where().eq("deleted", false).eq("warehouse", warehouse).findList();
			for (StorageType storageType : storageTypes) {
				StorageTypeVo storageTypeVo = new StorageTypeVo();
				storageTypeVo.inStorageTypeVo(storageType);
				storageTypeVo.warehouseVo = warehouseVo;
				storageTypeVos.add(storageTypeVo);
				List<Area> areas = Area.find().where().eq("deleted", false).eq("storageType", storageType).findList();
				for (Area area : areas) {
					AreaVo areaVo = new AreaVo();
					areaVo.inArea(area);
					areaVo.storageTypeVo = storageTypeVo;
					areaVos.add(areaVo);
				}
			}
		}
		return ok(play.libs.Json.toJson(areaVos));
	}

	public static Result initStorageType() {
		List<StorageTypeVo> storageTypeVos = new ArrayList<StorageTypeVo>();
		List<Warehouse> warehouses = Warehouse.find().where().eq("deleted", false).eq("id", SessionSearchUtil.searchWarehouse().id.toString())
			.findList();
		for (Warehouse warehouse : warehouses) {
			WarehouseVo warehouseVo = new WarehouseVo();
			warehouseVo.inwarehouse(warehouse);
			List<StorageType> storageTypes = StorageType.find().where().eq("deleted", false).eq("warehouse", warehouse).findList();
			for (StorageType storageType : storageTypes) {
				StorageTypeVo storageTypeVo = new StorageTypeVo();
				storageTypeVo.inStorageTypeVo(storageType);
				storageTypeVo.warehouseVo = warehouseVo;
				storageTypeVos.add(storageTypeVo);
			}
		}
		return ok(play.libs.Json.toJson(storageTypeVos));
	}

	public static Result initWarehouse() {
		List<WarehouseVo> warehouseVos = new ArrayList<WarehouseVo>();
		// List<Bin> bins = Bin.find().where().eq("deleted",
		// false).eq("area.deleted", false).eq("area.storageType.deleted",
		// false).eq("area.storageType.warehouse.deleted",
		// false).eq("area.storageType.warehouse.id",
		// SessionSearchUtil.searchWarehouse().id.toString()).findList();
		List<Warehouse> warehouses = Warehouse.find().where().eq("deleted", false).eq("id", SessionSearchUtil.searchWarehouse().id.toString())
			.findList();
		for (Warehouse warehouse : warehouses) {
			WarehouseVo warehouseVo = new WarehouseVo();
			warehouseVo.inwarehouse(warehouse);
			warehouseVos.add(warehouseVo);
		}
		return ok(play.libs.Json.toJson(warehouseVos));
	}

	public static Result getLines() {
		List<BinVo> LineVos = new ArrayList<BinVo>();
		List<Bin> Lines = Bin.find().where().eq("deleted", false).eq("area.deleted", false).eq("area.storageType.deleted", false)
			.eq("area.storageType.nameKey", "Receiving").findList();
		for (Bin bin : Lines) {
			BinVo binVo = new BinVo();
			binVo.inBin(bin);
			LineVos.add(binVo);
		}
		return ok(play.libs.Json.toJson(LineVos));
	}

	public static Result list(String type) {
		System.out.println("=================list==================");
		/*
		 * BinVo binVo=new BinVo(); RequestBody body = request().body();
		 * if(body.asJson()!=null) binVo = Json.fromJson(body.asJson(),
		 * BinVo.class); System.out.println(play.libs.Json.toJson(binVo));
		 * HashMap<String,StockCountVo> StockCountVos=new
		 * HashMap<String,StockCountVo>(); List<Stock> stocks=null;
		 * if(binVo.areaVo==null){ //stocks = Stock.find().all(); stocks=
		 * Stock.find().where().eq("deleted",
		 * false).eq("batch.material.deleted", false).eq("batch.deleted",
		 * false).eq("area.deleted", false).eq("area.storageType.deleted",
		 * false).eq("warehouse.deleted", false).eq("warehouse.id",
		 * SessionSearchUtil.searchWarehouse().id.toString()).eq("bin.deleted",
		 * false).findList(); System.out.println(stocks.size()); }else{ stocks =
		 * Stock.find().where().eq("deleted",
		 * false).eq("batch.material.deleted", false).eq("batch.deleted",
		 * false).eq("bin.deleted",false).eq("area.deleted",
		 * false).eq("area.storageType.deleted", false).eq("warehouse.deleted",
		 * false).eq("warehouse.id",
		 * SessionSearchUtil.searchWarehouse().id.toString
		 * ()).like("warehouse.nameKey",binVo. areaVo.storageTypeVo.warehouseVo
		 * .nameKey.equals("")?"%":"%"+binVo.areaVo
		 * .storageTypeVo.warehouseVo.nameKey
		 * +"%").like("area.storageType.nameKey",
		 * binVo.areaVo.storageTypeVo.nameKey
		 * .equals("")?"%":"%"+binVo.areaVo.storageTypeVo
		 * .nameKey+"%").like("area.nameKey"
		 * ,binVo.areaVo.nameKey.equals("")?"%":
		 * "%"+binVo.areaVo.nameKey+"%").like
		 * ("bin.nameKey",binVo.nameKey.equals(
		 * "")?"%":"%"+binVo.nameKey+"%").findList(); }
		 */
		RequestBody body = request().body();
		List<StockCountVo> StockCountVos;
		logger.info("++++++=============: "+type);
		if (type.equals("0")) {
			BinVo binVo = new BinVo();
			if (body.asJson() != null)
				logger.info("body.asJson()+==============: " + body.asJson());
			binVo = Json.fromJson(body.asJson(), BinVo.class);

			StockCountVos = getStockCountByLocation(binVo);
		} else {
			BatchVo batchVo = new BatchVo();
			if (body.asJson() != null)
				batchVo = Json.fromJson(body.asJson(), BatchVo.class);
			StockCountVos = getStockCountByBatch(batchVo);
		}

		return ok(play.libs.Json.toJson(StockCountVos));
	}

	public static List<StockCountVo> getStockCountByBatch(BatchVo batchVo) {
		HashMap<String, StockCountVo> stockCountVos = new HashMap<String, StockCountVo>();
		List<Batch> searchBatch = BatchSearchUtil.serchlikeBatch(batchVo.getHashMap());
	 
		HashMap<String,String> batchMap = null;
		String key = null;
		for (Batch batch : searchBatch) {
			//
			batch = Batch.find().where().eq("id", batch.id).eq("deleted", false).findUnique();
			/*
			 * System.out.println(batchVo.piNo+":"+batchVo.lotNo); String pi =
			 * ExtUtil.unapply(batch.ext).get("pi"); ExpressionList<Order>
			 * expressionList = Order.find().where().eq("warehouse",
			 * SessionSearchUtil.searchWarehouse()).eq("deleted", false); if
			 * (batchVo.piNo != null && !batchVo.piNo.equals("")) {
			 * expressionList = expressionList.like("internalOrderNo", "%" +
			 * batchVo.piNo + "%"); } if (batchVo.lotNo != null &&
			 * !batchVo.lotNo.equals("")) { expressionList =
			 * expressionList.like("contractNo", "%" + batchVo.lotNo + "%"); }
			 * List<Order> findList = expressionList.findList();
			 * System.out.println(findList.size()); if(findList.size()<1) break;
			 */
			// StockCountVo stockCountVo = new StockCountVo();
			// stockCountVo.inBatch(batch);
			
			List<Stock> stocks = Stock.find().where().eq("deleted", false).eq("batch", batch).findList();
			boolean ishaveStock = false;
			for (Stock stock : stocks) {
				batchMap = ExtUtil.unapply(stock.batch.ext);
				key = batchMap.get("pi")+batchMap.get("lot");
				if (stockCountVos.containsKey(key)) {
					StockCountVo stockCountVo = stockCountVos.get(key);
					stockCountVo.addQty(stock.qty.doubleValue());
					stockCountVo.setMultProDate(ExtUtil.unapply(stock.batch.ext));
					//stockCountVo.appendStockNo(map.get("stockNo"));
				} else {
					StockCountVo stockCountVoTemp = new StockCountVo();
					stockCountVoTemp.inBatch(batch);
					stockCountVoTemp.addQty(stock.qty.doubleValue());
					stockCountVoTemp.inStock(stock);
					stockCountVoTemp.inBin(stock.bin);
					//stockCountVoTemp.appendStockNo(map.get("stockNo"));
					stockCountVos.put(key, stockCountVoTemp);
				}
			}
		}
		List<StockCountVo> arrayList = new ArrayList<StockCountVo>();
		Set<String> keySet = stockCountVos.keySet();
		for (String string : keySet) {
			arrayList.add(stockCountVos.get(string));
		}
		return arrayList;
	}

	public static List<StockCountVo> getStockCountByLocation(BinVo binVo) {
		// BinVo binVo = new BinVo();
		LinkedHashMap<String, StockCountVo> stockCountVos = new LinkedHashMap<String, StockCountVo>();
		HashMap<String, String> map = null;
		List<Bin> bins = new ArrayList<Bin>();
		if (binVo.areaVo == null) {
			bins = Bin.find().where().eq("deleted", false).eq("area.deleted", false).eq("area.storageType.deleted", false)
				.eq("area.storageType.warehouse.deleted", false)
				.eq("area.storageType.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).orderBy("nameKey").findList();
		} else {
			bins = Bin
				.find()
				.where()
				.eq("deleted", false)
				.eq("area.deleted", false)
				.eq("area.storageType.deleted", false)
				.eq("area.storageType.warehouse.deleted", false)
				.eq("area.storageType.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString())
				.like("area.storageType.warehouse.nameKey",
					binVo.areaVo.storageTypeVo.warehouseVo.nameKey.equals("") ? "%" : "%" + binVo.areaVo.storageTypeVo.warehouseVo.nameKey + "%")
				.like("area.storageType.nameKey",
					binVo.areaVo.storageTypeVo.nameKey.equals("") ? "%" : "%" + binVo.areaVo.storageTypeVo.nameKey + "%")
				.like("area.nameKey", binVo.areaVo.nameKey.equals("") ? "%" : "%" + binVo.areaVo.nameKey + "%")
				.like("nameKey", binVo.nameKey.equals("") ? "%" : "%" + binVo.nameKey + "%").orderBy("nameKey").findList();
		}
		for (Bin bin : bins) {
			List<Stock> Stocks = Stock.find().where().eq("batch.material.deleted", false).eq("batch.deleted", false).eq("deleted", false)
				.eq("bin", bin).findList();
			System.out.println(bin.nameKey);
			if (Stocks.size() < 1) {
				StockCountVo stockCountVo = new StockCountVo();
				stockCountVo.inBin(bin);
				stockCountVos.put(bin.id.toString(), stockCountVo);
			} else {
				for (Stock stock : Stocks) {
					map = ExtUtil.unapply(stock.batch.ext);
					if (stockCountVos.containsKey(map.get("pi") + map.get("lot") + stock.bin.id.toString()) == true) {
						System.out.println("");
						stockCountVos.get(map.get("pi") + map.get("lot") + stock.bin.id.toString()).addQty(stock.qty.doubleValue());
					} else {
						System.out.println("add stockCount");
						StockCountVo stockCountVo = new StockCountVo();
						stockCountVo.inBin(bin);
						stockCountVo.inStock(stock);
						stockCountVo.inBatch(stock.batch);
						stockCountVo.addQty(stock.qty.doubleValue());
						map = ExtUtil.unapply(stock.batch.ext);
						stockCountVos.put(map.get("pi") + map.get("lot") + stock.bin.id.toString(), stockCountVo);
					}
				}
			}
		}
		List<StockCountVo> arrayList = new ArrayList<StockCountVo>();
		Set<String> keySet = stockCountVos.keySet();
		for (String string : keySet) {
			StockCountVo vo  = stockCountVos.get(string);
			arrayList.add(vo);
		}
		return arrayList;
	}

	static Form<BatchVo> batchVoForm = Form.form(BatchVo.class);
	static Form<ReportBinVo> binVoForm = Form.form(ReportBinVo.class);

	public static Result report(String type) {
		Object obj = null;
		System.out.println("reprot start");
		if (type.equals("1")) {
			if (batchVoForm.hasErrors()) {
				obj = new BatchVo();
			} else {
				obj = batchVoForm.bindFromRequest().get();
			}
			//System.out.println(play.libs.Json.toJson(obj));
		} else {
			if (binVoForm.hasErrors()) {
				obj = new BinVo();
			} else {
				ReportBinVo reportBinVo = binVoForm.bindFromRequest().get();
				//System.out.println(play.libs.Json.toJson(reportBinVo));
				if (reportBinVo != null) {
					obj = reportBinVo.returnVo();
				}
			}
		}
		/*
		 * if(type.equals("1")){ BatchVo batchVo = new BatchVo();
		 * batchVo.piNo=(String)Form.get("piNo");
		 * batchVo.prodDateFrom=Form.get(); }
		 */
		List<StockCountReport> reports = new ArrayList<StockCountReport>();
		StockCountReport report = CreateReport(type, obj);
		reports.add(report);

		File file = ReadExcel.outExcel(reports, report.getIntProdNo() + ".xlsx");
		response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");// 可选择不同类型
		response().setHeader("Content-Disposition", "attachment; filename=" + report.getIntProdNo() + ".xlsx");
		return ok(file);
		// return null;
	}

	public static StockCountReport CreateReport(String type, Object obj) {
		StockCountReport stockCountReport = new StockCountReport();
		stockCountReport.setSheetName("sheet1");
		List<? extends ExcelObj> ReportDates = null;
		if (type.equals("0")) {
			BinVo binVo = (BinVo) obj;
			List<ExcelObj> arrayList = new ArrayList<ExcelObj>();
			Cell cell0 = new Cell("Warehouse", 1, 0);
			arrayList.add(cell0);
			Cell cell1 = new Cell(
				binVo != null && binVo.areaVo != null && binVo.areaVo.storageTypeVo != null && binVo.areaVo.storageTypeVo.warehouseVo != null
					&& !binVo.areaVo.storageTypeVo.warehouseVo.nameKey.equals("") ? binVo.areaVo.storageTypeVo.warehouseVo.nameKey : "All", 1, 1);
			arrayList.add(cell1);
			Cell cell2 = new Cell("Storage Type", 1, 6);
			arrayList.add(cell2);
			Cell cell3 = new Cell(binVo != null && binVo.areaVo != null && binVo.areaVo.storageTypeVo != null
				&& !binVo.areaVo.storageTypeVo.nameKey.equals("") ? binVo.areaVo.storageTypeVo.nameKey : "All", 1, 7);
			arrayList.add(cell3);
			Cell cell4 = new Cell("Storage Area", 2, 0);
			arrayList.add(cell4);
			// System.out.println(batchVo.prodLine);
			Cell cell5 = new Cell(binVo != null && binVo.areaVo != null && !binVo.areaVo.nameKey.equals("") ? binVo.areaVo.nameKey : "All", 2, 1);
			arrayList.add(cell5);
			Cell cell6 = new Cell("Storage Bin", 2, 6);
			arrayList.add(cell6);
			Cell cell7 = new Cell(binVo != null && !binVo.nameKey.equals("") ? binVo.nameKey : "All", 2, 7);
			arrayList.add(cell7);
			// List<ExcelObj> arrayList1 = new ArrayList<ExcelObj>();
			// arrayList1.add(cell2);
			// stuffingReport.setFoot(arrayList1);
			stockCountReport.setHead(arrayList);
			stockCountReport.setIntProdNo("Stock_CheckLocation");
			List<StockCountVo> stockCountByLocation = getStockCountByLocation((BinVo) obj);
			List<StockCountDateByLocation> Locations = new ArrayList<StockCountDateByLocation>();
			for (StockCountVo stockCountVo : stockCountByLocation) {
				StockCountDateByLocation stockCountDateByLocation = new StockCountDateByLocation(stockCountVo);
				Locations.add(stockCountDateByLocation);
			}
			ReportDates = Locations;
		} else {
			BatchVo batchVo = (BatchVo) obj;
			List<ExcelObj> arrayList = new ArrayList<ExcelObj>();
			Cell cell0 = new Cell("PI NO", 1, 0);
			arrayList.add(cell0);
			Cell cell1 = new Cell(batchVo.piNo != null && !batchVo.piNo.equals("") ? batchVo.piNo : "All", 1, 1);
			arrayList.add(cell1);
			Cell cell2 = new Cell("Report Date", 1, 7);
			arrayList.add(cell2);
			Cell cell3 = new Cell(DateUtil.dateToStrShort(new Date()), 1, 8);
			arrayList.add(cell3);
			Cell cell4 = new Cell("Product Line", 2, 0);
			arrayList.add(cell4);
			System.out.println(batchVo.prodLine);
			Cell cell5 = new Cell(batchVo.prodLine != null && !batchVo.prodLine.equals("") && Bin.find().byId(batchVo.prodLine) != null ? Bin.find()
				.byId(batchVo.prodLine).nameKey : "All", 2, 1);
			arrayList.add(cell5);
			Cell cell6 = new Cell("Generate By ", 2, 7);
			arrayList.add(cell6);
			Cell cell7 = new Cell(SessionSearchUtil.searchUser().name, 2, 8);
			arrayList.add(cell7);
			// List<ExcelObj> arrayList1 = new ArrayList<ExcelObj>();
			// arrayList1.add(cell2);
			// stuffingReport.setFoot(arrayList1);
			stockCountReport.setHead(arrayList);
			stockCountReport.setIntProdNo("Stock_CheckBatch");
			List<StockCountVo> stockCountByBatch = getStockCountByBatch(batchVo);
			// List<StockCountVo> stockCountByLocation =
			// getStockCountByLocation(request().body());
			List<StockCountDateByBatch> batchs = new ArrayList<StockCountDateByBatch>();
			Map<String,String> map = new HashMap<String, String>();
			for (StockCountVo stockCountVo : stockCountByBatch) {
				map.put("pi", stockCountVo.piNo);
				map.put("lot", stockCountVo.batchNo);
				List<StockVo> searchStocks = searchStocks(map);
				for (StockVo stockVo : searchStocks) {
					stockCountVo.inStockVo(stockVo);
					StockCountDateByBatch stockCountDateByBatch = new StockCountDateByBatch(stockCountVo);
					batchs.add(stockCountDateByBatch);
				}

			}
			ReportDates = batchs;
		}
		stockCountReport.setDates(ReportDates);
		return stockCountReport;
	}

	public static Result SaveStockCount(String type) {
		if (type.equals("0")) {
			try {
				RequestBody body = request().body();
				BinVo binVo = new BinVo();
				if (body.asJson() != null)
					binVo = Json.fromJson(body.asJson(), BinVo.class);
				System.out.println(body.asJson());
				CreateStockCount(getStockCountByLocation(binVo));
			} catch (Exception e) {
				e.printStackTrace();
				return badRequest("can't save");
			}
			return ok("success");
		}
		return badRequest("SotckChecType is not location");
	}

	@SuppressWarnings("unchecked")
	public static Result getStocks() {
		RequestBody body = request().body();
		Map<String,String> map = null;
		List<StockVo> searchStocks = null;
		if(body.asJson()!=null){
			logger.info("body.asJon+++++8*********** = "+body.asJson());
			map = Json.fromJson(body.asJson(), Map.class);
			  searchStocks = searchStocks(map);
		}
		return ok(play.libs.Json.toJson(searchStocks));
	}

	public static List<StockVo> searchStocks(Map<String,String> map) {
		List<StockVo> stockVos = new ArrayList<StockVo>();
		List<Batch> batchlist = BatchSearchUtil.serchBatch(map);
	   if(EmptyUtil.isNotEmptyList(batchlist)){
		   for(Batch batch :batchlist){
			   List<Stock> stocks = Stock.find().where().eq("deleted", false).eq("batch", batch).orderBy("createdAt").findList();
			   for (Stock stock : stocks) {
				   StockVo stockVo = new StockVo();
				   stockVo.inStock(stock);
				   stockVos.add(stockVo);
			   }  
		   }
	   }
		return stockVos;
	}

	@Transactional
	public static void CreateStockCount(List<StockCountVo> stockCountVos) {
		StockCount stockCount = new StockCount();
		stockCount.countAt = DateUtil.currentTimestamp();
		stockCount.createdAt = DateUtil.currentTimestamp();
		stockCount.createdBy = SessionSearchUtil.searchUser().id;
		stockCount.countType = countType;
		stockCount.countStatus = NEW;
		HashMap<String, StockCountBin> stockCoutBinVos = new HashMap<String, StockCountBin>();
		for (StockCountVo stockCountVo : stockCountVos) {
			StockCountBin stockCountBin = null;
			if (stockCoutBinVos.containsKey(stockCountVo.storageBin)) {
				stockCountBin = stockCoutBinVos.get(stockCountVo.storageBin);
			} else {
				stockCountBin = new StockCountBin();
			}
			List<Area> areas = Area.find().where().eq("nameKey", stockCountVo.storageArea).eq("deleted", false).findList();
			if (areas.size() > 0)
				stockCountBin.area = areas.get(0);
			List<Bin> bins = Bin.find().where().eq("nameKey", stockCountVo.storageBin).eq("deleted", false).findList();
			if (bins.size() > 0)
				stockCountBin.bin = bins.get(0);
			stockCountBin.countStatus = NEW;
			stockCountBin.createdAt = DateUtil.currentTimestamp();
			stockCountBin.createdBy = SessionSearchUtil.searchUser().id;
			if (stockCountVo.piNo != null) {
				StockCountItem stockCountItem = new StockCountItem();
				stockCountItem.expectedQty = BigDecimal.valueOf(stockCountVo.systemQty);
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("pi", stockCountVo.piNo);
				map.put("lot", stockCountVo.batchNo);
				List<Batch> batchs = BatchSearchUtil.serchBatch(map);
				if (batchs.size() > 0)
					stockCountItem.batch = batchs.get(0);
				List<Material> materials = Material.find().where().eq("deleted", false).eq("materialCode", stockCountVo.materialCode).findList();
				if (materials.size() > 0)
					stockCountItem.material = materials.get(0);
				List<MaterialUom> UOMS = MaterialUom.find().where().eq("deleted", false).eq("uomCode", stockCountVo.QtyUom).findList();
				if (UOMS.size() > 0)
					stockCountItem.materialUom = UOMS.get(0);
				stockCountItem.countedAt = DateUtil.currentTimestamp();
				stockCountItem.createdAt = DateUtil.currentTimestamp();
				stockCountItem.createdBy = SessionSearchUtil.searchUser().id;
				stockCountItem.save();
				stockCountBin.items.add(stockCountItem);
			}
			stockCountBin.save();
			stockCoutBinVos.put(stockCountVo.storageBin, stockCountBin);
		}
		Set<String> keySet = stockCoutBinVos.keySet();
		for (String string : keySet) {
			stockCount.items.add(stockCoutBinVos.get(string));
		}
		stockCount.save();
	}
}
