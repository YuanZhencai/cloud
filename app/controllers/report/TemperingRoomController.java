/** * TemperingRoomReportController.java 
* Created on 2013-5-30 上午9:21:46 
*/

package controllers.report;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.StockTransaction;
import models.Warehouse;
import models.printVo.Cell;
import models.printVo.Line;
import models.printVo.Title;
import models.printVo.temperingReport.TemperingDate;
import models.printVo.temperingReport.TemperingOutData;
import models.printVo.temperingReport.TemperingReport;
import models.vo.report.TemperingRoomVo;

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
import utils.DateUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import utils.Excel.ExcelObj;
import utils.Excel.ReadExcel;
import views.html.report.temperingRoom;
import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: TemperingRoomReportController.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class TemperingRoomController extends Controller {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static final String STORAGE_TYPE_TEMPER = "Tempering";
	private static final Logger logger =LoggerFactory.getLogger(TemperingRoomController.class);

	/**
	 * 显示主页面
	 * @return
	 */
	public static Result index() {
		return ok(temperingRoom.render(""));

	}

	/**
	 * 查询除满足条件的数据
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result list() {
		logger.info("^^^^^^^^^^^ you have in list method  ^^^^^^^^^^^^^^^^^^^^^^^^^^");
		RequestBody body = request().body();
		if (body.asJson() != null) {
			//logger.info("^^^^^^^^^^^^ " + body.asJson());
			TemperingRoomVo trVo = Json.fromJson(body.asJson(), TemperingRoomVo.class);
			List<? extends ExcelObj> gatherStocks = listSlock(trVo, false);
			//logger.info("^^^^^^^^^^^^^^   gatherStocks  ^^^^^^^^^^^^^^^^^^" + gatherStocks);
			return ok(Json.toJson(gatherStocks));
		} else {
			return badRequest("your request information is wrong");
		}
	}

	/**
	 *  下载模板
	 * @return
	 */
	@Transactional
	public static Result download(String inOrOut, Long executeDate) {
		logger.info("^^^^^^^^^^^ you have in download method  ^^^^^^^^^^^^^^^^^^^^^^^^^^");
		ArrayList<TemperingReport> trs = new ArrayList<TemperingReport>();
		Map<String, Object> map = new HashMap<String, Object>();
		TemperingRoomVo trVo = new TemperingRoomVo();
		trVo.inOrOut = inOrOut;
		trVo.executeDate = new Timestamp(executeDate);
		List<? extends ExcelObj> listSlock = listSlock(trVo, true);
		TemperingReport tr = createReport("F/MNA-WHSF-00-027", new Date(), listSlock.size(), inOrOut);
		tr.setDates(listSlock);
		trs.add(tr);
		File file = ReadExcel.outExcel(trs, tr.getIntProdNo() + ".xlsx");
		response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");// 可选择不同类型
		response().setHeader("Content-Disposition", "attachment; filename=" + tr.getIntProdNo() + ".xlsx");
		return ok(file);
	}

	/*
	 * 样式规划
	 */
	public static TemperingReport createReport(String IntProdNo, Date reportDate, int dataSize, String sheetName) {
		TemperingReport tr = new TemperingReport();
		tr.setIntProdNo(IntProdNo);
		tr.setReportDate(reportDate);
		logger.info(tr.getStyle());
		tr.setSheetName(sheetName);
		// head
		List<ExcelObj> headList = new ArrayList<ExcelObj>();
		Cell cell00 = new Cell("PT.MULTIMAS NABATI ASAHAN", 0, 0);
		Cell cell01 = new Cell(tr.getIntProdNo(), 0, 9);
		Cell cell10 = new Cell("Ware House Speciality Fats", 1, 9);
		Title title = new Title("TEMPERING ROOM RECORD   " + DateUtil.dateToStrShortEn(tr.getReportDate()), 3, 5);
		Cell cell40 = new Cell("Date", 4, 0);
		Cell cell50 = new Cell("Time", 5, 0);
		Cell cell60 = new Cell("Shift/Group", 6, 0);
		Cell cell80 = new Cell("Product In Tempering Room", 8, 0);
		headList.add(cell00);
		headList.add(cell01);
		headList.add(cell10);
		headList.add(title);
		headList.add(cell40);
		headList.add(cell50);
		headList.add(cell60);
		headList.add(cell80);
		// foot
		List<ExcelObj> footList = new ArrayList<ExcelObj>();
		int footStrart = 8 + dataSize;
		// foot row1
		Cell cellf00 = new Cell("Temperatur Tempering 1", 1, 0);
		Line linef01 = new Line("C", 1, 1, true);
		Cell cellf03 = new Cell("Temperatur Ware House 1", 1, 3);
		Line linef04 = new Line("", 1, 4, false);
		Cell cellf06 = new Cell("Total Product Tempering 1", 1, 6);
		Line linef07 = new Line("", 1, 7, false);
		// foot row2
		Cell cellf10 = new Cell("Temperatur Tempering 2", 2, 0);
		Line linef11 = new Line("", 2, 1, false);
		Cell cellf13 = new Cell("Temperatur Ware House 2", 2, 3);
		Line linef14 = new Line("", 2, 4, false);
		Cell cellf16 = new Cell("Total Product Tempering 2", 2, 6);
		Line linef17 = new Line("", 2, 7, false);
		// foot row3
		Cell cellf20 = new Cell("Temperatur Tempering 3", 3, 0);
		Line linef21 = new Line("", 3, 1, false);
		Cell cellf23 = new Cell("Temperatur Ware House 3", 3, 3);
		Line linef24 = new Line("", 3, 4, false);
		Cell cellf26 = new Cell("Total Product Tempering 3", 3, 6);
		Line linef27 = new Line("", 3, 7, false);
		// foot row4
		Cell cellf42 = new Cell("Remaks :", 5, 2);
		Line linef43 = new Line("", 5, 3, false);
		Line linef44 = new Line("", 5, 4, false);
		Line linef45 = new Line("", 5, 5, false);
		// foot row5
		Line linef53 = new Line("", 6, 3, false);
		Line linef54 = new Line("", 6, 4, false);
		Line linef55 = new Line("", 6, 5, false);
		Line linef63 = new Line("", 7, 3, false);
		Line linef64 = new Line("", 7, 4, false);
		Line linef65 = new Line("", 7, 5, false);
		Cell cellf73 = new Cell("Prepared By:", 8, 3);
		Cell cellf76 = new Cell("Checked  By:", 8, 6);

		footList.add(cellf00);
		footList.add(linef01);
		footList.add(cellf03);
		footList.add(linef04);
		footList.add(cellf06);
		footList.add(linef07);
		footList.add(cellf10);
		footList.add(linef11);
		footList.add(cellf13);
		footList.add(linef14);
		footList.add(cellf16);
		footList.add(linef17);
		footList.add(cellf20);
		footList.add(linef21);
		footList.add(cellf23);
		footList.add(linef24);
		footList.add(cellf26);
		footList.add(linef27);
		footList.add(cellf42);
		footList.add(linef43);
		footList.add(linef44);
		footList.add(linef45);
		footList.add(linef53);
		footList.add(linef54);
		footList.add(linef55);
		footList.add(linef63);
		footList.add(linef64);
		footList.add(linef65);
		footList.add(cellf73);
		footList.add(cellf76);
		tr.setFoot(footList);
		tr.setHead(headList);
		return tr;
	}

	/**
	 * 根据inOrOut字段对查询当天进(IN)出(OUT)冷库的stocks,且根据 pi 和 material 进行汇聚
	 * @param map
	 * @return
	 */
	public static List<? extends ExcelObj> listSlock(TemperingRoomVo trVo, boolean isDownload) {
		List<? extends ExcelObj> returnList = null;
		final String TRANSACTION_CODE = "T101";
		List<StockTransaction> stTransfer = null;
		List<TemperingDate> gatherInStocks = new ArrayList<TemperingDate>();
		List<TemperingOutData> gatherOutStocks = new ArrayList<TemperingOutData>();
		String executeDate = sdf.format(trVo.executeDate);
		Warehouse wh = SessionSearchUtil.searchWarehouse();
		Boolean inOrOut = true;
		ExpressionList<StockTransaction> el = Ebean.find(StockTransaction.class).fetch("stock").fetch("execution").where().eq("deleted", false)
			.eq("warehouse", wh).ge("transactionAt", DateUtil.strToDate(executeDate + " 00:00:00", "yyyy-MM-dd HH:mm:ss"))
			.le("transactionAt", DateUtil.strToDate(executeDate + " 23:59:59", "yyyy-MM-dd HH:mm:ss"));

		if ("IN".equals(trVo.inOrOut)) {
			// 收货时的入冷库
			el.eq("execution.toArea.storageType.nameKey", STORAGE_TYPE_TEMPER).eq("transactionCode", TRANSACTION_CODE);
			// Transfer 进入冷库
			stTransfer = Ebean.find(StockTransaction.class).fetch("stock").fetch("execution").where().eq("deleted", false).eq("warehouse", wh)
				.ge("transactionAt", DateUtil.strToDate(executeDate + " 00:00:00", "yyyy-MM-dd HH:mm:ss"))
				.le("transactionAt", DateUtil.strToDate(executeDate + " 23:59:59", "yyyy-MM-dd HH:mm:ss"))
				.ne("execution.fromArea.storageType.nameKey", STORAGE_TYPE_TEMPER).eq("execution.toArea.storageType.nameKey", STORAGE_TYPE_TEMPER)
				.findList();
			inOrOut = true;
		} else {
			el.eq("execution.fromArea.storageType.nameKey", STORAGE_TYPE_TEMPER).ne("execution.toArea.storageType.nameKey", STORAGE_TYPE_TEMPER);
			inOrOut = false;
		}
		List<StockTransaction> stockTransactions = el.findList();
		if (stockTransactions != null && !stockTransactions.isEmpty() && stTransfer != null && !stTransfer.isEmpty()) {
			stockTransactions.addAll(stTransfer);
		} else if ((stockTransactions == null || stockTransactions.isEmpty()) && stTransfer != null && !stTransfer.isEmpty()) {
			stockTransactions = stTransfer;
		}
		//logger.info("stockTransactionsstockTransactionsstockTransactions" + stockTransactions);

		if (!stockTransactions.isEmpty()) {
			for (int k = 0; k < stockTransactions.size(); k++) {
				boolean flag = true;
				// 进入冷库
				if (inOrOut) {
					if (!gatherInStocks.isEmpty()) {
						for (TemperingDate td : gatherInStocks) {
							if (td.piNoAdnContract.equals(ExtUtil.unapply(stockTransactions.get(k).stock.batch.ext).get("pi"))
								&& td.desc.equals(stockTransactions.get(k).stock.material.materialName)) {
								flag = false;
								td.palletNo++;
								td.totalBox = td.totalBox + stockTransactions.get(k).newQty.doubleValue();
							}
						}
					}
					if (flag) {
						TemperingDate temperingDate = TemperingDate.getTemperingDate(stockTransactions.get(k));
						temperingDate.no = gatherInStocks.size() + 1;
						gatherInStocks.add(temperingDate);
					}
				} else {// 出冷库
					if (!gatherOutStocks.isEmpty()) {
						for (TemperingOutData td : gatherOutStocks) {
							if (td.piNoAdnContract.equals(ExtUtil.unapply(stockTransactions.get(k).stock.batch.ext).get("pi"))
								&& td.desc.equals(stockTransactions.get(k).stock.material.materialName)) {
								flag = false;
								td.palletNo++;
								td.totalBox = td.totalBox + stockTransactions.get(k).newQty.doubleValue();
							}
						}
					}
					if (flag) {
						TemperingOutData temperingDate = TemperingOutData.getTemperingOutData(stockTransactions.get(k));
						temperingDate.no = gatherInStocks.size() + 1;
						gatherOutStocks.add(temperingDate);

					}
				}
			}

		}
		if (inOrOut) {
			if (gatherInStocks == null || gatherInStocks.isEmpty()) {
				gatherInStocks = new ArrayList<TemperingDate>();
				if (isDownload) {
					gatherInStocks.add(new TemperingDate());
				}
			}
			returnList = gatherInStocks;
		} else {
			if (gatherOutStocks == null || gatherOutStocks.isEmpty()) {
				gatherOutStocks = new ArrayList<TemperingOutData>();
				if (isDownload) {
					gatherOutStocks.add(new TemperingOutData());
				}
			}
			returnList = gatherOutStocks;
		}
		return returnList;
	}

}
