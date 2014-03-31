package controllers.report;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import models.Batch;
import models.Stock;
import models.printVo.temperingControllerReport.StockCountDateByBatch;
import models.printVo.temperingControllerReport.StockCountReport;
import models.vo.outbound.batchVo;
import models.vo.query.BatchVo;
import models.vo.query.ReportBinVo;
import models.vo.query.StockCountVo;
import models.vo.query.StockVo;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import play.mvc.Http.RequestBody;
import security.NoUserDeadboltHandler;
import utils.BatchSearchUtil;
import utils.EmptyUtil;
import utils.ExtUtil;
import utils.Excel.ExcelObj;
import utils.Excel.ReadExcel;

@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class TemperingReportController extends Controller {
	// static String warehouseId = "fe640b63-5501-44b0-9fb1-6bbd93c5e8e5";
	static String countType = "T002";
	static String NEW = "S000";
	private static final Logger logger = LoggerFactory.getLogger(TemperingReportController.class);

	public static Result index() {
		return ok(views.html.report.TemperingReport.render(""));
	}

	public static Result list() {
		System.out.println("=================list==================");
		RequestBody body = request().body();
		List<StockCountVo> StockCountVos;
		BatchVo batchVo = new BatchVo();
		if (body.asJson() != null)
			batchVo = Json.fromJson(body.asJson(), BatchVo.class);
		StockCountVos = getStockCountByBatch(batchVo);
		System.out.println(StockCountVos.size());
		return ok(play.libs.Json.toJson(StockCountVos));
	}

	public static List<StockCountVo> getStockCountByBatch(BatchVo batchVo) {
		HashMap<String, StockCountVo> stockCountVos = new HashMap<String, StockCountVo>();
		List<Batch> searchBatch = BatchSearchUtil.serchlikeBatch(batchVo.getHashMap());

		HashMap<String, String> batchMap = null;
		String key = null;
		for (Batch batch : searchBatch) {
			batch = Batch.find().where().eq("id", batch.id).eq("deleted", false).findUnique();
			List<Stock> stocks = Stock.find().where().eq("deleted", false).eq("area.storageType.nameKey", "Tempering").eq("batch", batch).findList();
			for (Stock stock : stocks) {
				batchMap = ExtUtil.unapply(stock.batch.ext);
				key = batchMap.get("pi") + batchMap.get("lot") + batchMap.get("date");
				if (stockCountVos.containsKey(key)) {
					StockCountVo stockCountVo = stockCountVos.get(key);
					if (stockCountVo.binId != null && stockCountVo.binId.equals(stock.bin.id.toString())) {
						stockCountVo.addQty(stock.qty.doubleValue());
						stockCountVo.setMultProDate(ExtUtil.unapply(stock.batch.ext));
					} else {
						StockCountVo stockCountVoTemp = new StockCountVo();
						stockCountVoTemp.inBatch(batch);
						stockCountVoTemp.addQty(stock.qty.doubleValue());
						stockCountVoTemp.inStock(stock);
						stockCountVoTemp.inBin(stock.bin);
						// stockCountVoTemp.appendStockNo(map.get("stockNo"));
						stockCountVos.put(key, stockCountVoTemp);
					}
					// stockCountVo.appendStockNo(map.get("stockNo"));
				} else {
					StockCountVo stockCountVoTemp = new StockCountVo();
					stockCountVoTemp.inBatch(batch);
					stockCountVoTemp.addQty(stock.qty.doubleValue());
					stockCountVoTemp.inStock(stock);
					stockCountVoTemp.inBin(stock.bin);
					// stockCountVoTemp.appendStockNo(map.get("stockNo"));
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

	static Form<BatchVo> batchVoForm = Form.form(BatchVo.class);
	static Form<ReportBinVo> binVoForm = Form.form(ReportBinVo.class);

	public static Result report() {
		Object obj = null;
		System.out.println("reprot start");
		if (batchVoForm.hasErrors()) {
			obj = new batchVo();
		} else {
			BatchVo batchVo = batchVoForm.bindFromRequest().get();
			// System.out.println(play.libs.Json.toJson(reportBinVo));
			if (batchVo != null) {
				obj = batchVo;
			}
		}
		/*
		 * if(type.equals("1")){ BatchVo batchVo = new BatchVo();
		 * batchVo.piNo=(String)Form.get("piNo");
		 * batchVo.prodDateFrom=Form.get(); }
		 */
		List<StockCountReport> reports = new ArrayList<StockCountReport>();
		StockCountReport report = CreateReport(obj);
		reports.add(report);

		File file = ReadExcel.outExcel(reports, report.getIntProdNo() + ".xlsx");
		response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");// 可选择不同类型
		response().setHeader("Content-Disposition", "attachment; filename=" + report.getIntProdNo() + ".xlsx");
		return ok(file);
		// return null;
	}

	public static StockCountReport CreateReport(Object obj) {
		StockCountReport stockCountReport = new StockCountReport();
		stockCountReport.setSheetName("sheet1");
		List<? extends ExcelObj> ReportDates = null;

		BatchVo batchVo = (BatchVo) obj;
		/*
		 * List<ExcelObj> arrayList = new ArrayList<ExcelObj>(); Cell cell0 =
		 * new Cell("PI NO", 1, 0); arrayList.add(cell0); Cell cell1 = new
		 * Cell(batchVo.piNo != null && !batchVo.piNo.equals("") ? batchVo.piNo
		 * : "All", 1, 1); arrayList.add(cell1); Cell cell2 = new
		 * Cell("Report Date", 1, 7); arrayList.add(cell2); Cell cell3 = new
		 * Cell(DateUtil.dateToStrShort(new Date()), 1, 8);
		 * arrayList.add(cell3); Cell cell4 = new Cell("Product Line", 2, 0);
		 * arrayList.add(cell4); System.out.println(batchVo.prodLine); Cell
		 * cell5 = new Cell(batchVo.prodLine != null &&
		 * !batchVo.prodLine.equals("") && Bin.find().byId(batchVo.prodLine) !=
		 * null ? Bin.find() .byId(batchVo.prodLine).nameKey : "All", 2, 1);
		 * arrayList.add(cell5); Cell cell6 = new Cell("Generate By ", 2, 7);
		 * arrayList.add(cell6); Cell cell7 = new
		 * Cell(SessionSearchUtil.searchUser().name, 2, 8);
		 * arrayList.add(cell7); // List<ExcelObj> arrayList1 = new
		 * ArrayList<ExcelObj>(); // arrayList1.add(cell2); //
		 * stuffingReport.setFoot(arrayList1);
		 * stockCountReport.setHead(arrayList);
		 */
		stockCountReport.setIntProdNo("Tempering Control Report");
		List<StockCountVo> stockCountByBatch = getStockCountByBatch(batchVo);
		// List<StockCountVo> stockCountByLocation =
		// getStockCountByLocation(request().body());
		List<StockCountDateByBatch> batchs = new ArrayList<StockCountDateByBatch>();
		Map<String, String> map = new HashMap<String, String>();
		for (StockCountVo stockCountVo : stockCountByBatch) {
			/*
			 * map.put("pi", stockCountVo.piNo); map.put("lot",
			 * stockCountVo.batchNo); List<StockVo> searchStocks =
			 * searchStocks(map); for (StockVo stockVo : searchStocks) {
			 * stockCountVo.inStockVo(stockVo);
			 */
			StockCountDateByBatch stockCountDateByBatch = new StockCountDateByBatch(stockCountVo);
			batchs.add(stockCountDateByBatch);
			/*
			 * }
			 */
		}
		ReportDates = batchs;
		stockCountReport.setDates(ReportDates);
		return stockCountReport;
	}

	@SuppressWarnings("unchecked")
	public static Result getStocks() {
		RequestBody body = request().body();
		Map<String, String> map = null;
		List<StockVo> searchStocks = null;
		if (body.asJson() != null) {
			// logger.info("body.asJon+++ = "+body.asJson());
			map = Json.fromJson(body.asJson(), Map.class);
			searchStocks = searchStocks(map);
		}
		return ok(play.libs.Json.toJson(searchStocks));
	}

	public static List<StockVo> searchStocks(Map<String, String> map) {
		List<StockVo> stockVos = new ArrayList<StockVo>();
		List<Batch> batchlist = BatchSearchUtil.serchBatch(map);
		if (EmptyUtil.isNotEmptyList(batchlist)) {
			for (Batch batch : batchlist) {
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
}
