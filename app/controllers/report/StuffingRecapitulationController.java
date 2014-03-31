/** * StuffingRecapitulationReport.java 
* Created on 2013-6-4 下午3:34:15 
*/

package controllers.report;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import models.Execution;
import models.Warehouse;
import models.printVo.Cell;
import models.printVo.Title;
import models.printVo.stuffingRecapitulation.StuffingRecapitulationDate;
import models.printVo.stuffingRecapitulation.StuffingRecapitulationReport;
import models.vo.report.TemperingRoomVo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import play.mvc.With;
import security.NoUserDeadboltHandler;
import utils.ChainList;
import utils.DateUtil;
import utils.SessionSearchUtil;
import utils.Excel.ExcelObj;
import utils.Excel.ReadExcel;
import views.html.report.stuffingRecapitulationReport;
import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;

/** 
 * <p>Project: cloudwms</p> 
 * <p>Title: StuffingRecapitulationReport.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class StuffingRecapitulationController extends Controller {
	private static final String PLANITEM_TYPE_CARGO = "T003";
	private static final String PLANITEM_STATUS = "S003";
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static final Logger logger =LoggerFactory.getLogger(TemperingRoomController.class);
	/**
	 * 显示主页面
	 * @return
	 */
	public static Result index() {
		return ok(stuffingRecapitulationReport.render(""));
	}

	/**
	 * 查询除满足条件的数据
	 * @return
	 */
	public static Result list() {
		logger.info("^^^^^^^^^^^ you have in list method  ^^^^^^^^^^^^^^^^^^^^^^^^^^");
		RequestBody body = request().body();
		if (body.asJson() != null) {
			//logger.info("^^^^^^^^^^^^ " + body.asJson());
			TemperingRoomVo trVo = Json.fromJson(body.asJson(), TemperingRoomVo.class);
			List<StuffingRecapitulationDate> srr = (List<StuffingRecapitulationDate>) listExecutedStuffingPlan(trVo).get("srDates");
			//logger.info("^^^^^^^^^^^^^^   StuffingRecapitulationDate  ^^^^^^^^^^^^^^^^^^" + srr);
			return ok(Json.toJson(srr));
		} else {
			return badRequest("your request information is wrong");
		}
	}

	/**
	 *  下载模板
	 * @return
	 */
	public static Result download(String inOrOut, Long executeDate) {
		logger.info("^^^^^^^^^^^ you have in download method  ^^^^^^^^^^^^^^^^^^^^^^^^^^");
		ArrayList<StuffingRecapitulationReport> srs = new ArrayList<StuffingRecapitulationReport>();
		List<ExcelObj> srDates = new ArrayList<ExcelObj>();

		Map<String, Object> map = new HashMap<String, Object>();
		TemperingRoomVo trVo = new TemperingRoomVo();
		trVo.inOrOut = inOrOut;
		trVo.executeDate = new Timestamp(executeDate);
		Map<String, Object> listMap = listExecutedStuffingPlan(trVo);
		List<StuffingRecapitulationDate> srDateList = (List<StuffingRecapitulationDate>) listMap.get("srDates");
		if (srDateList != null && !srDateList.isEmpty()) {
			for (StuffingRecapitulationDate sr : srDateList) {
				srDates.add(sr);
			}
		}
		if(srDates.isEmpty()){
			srDates.add(new StuffingRecapitulationDate());
		}
		StuffingRecapitulationReport sr = createReport("F/MNA-WHSF-00-019", new Date(), trVo.inOrOut, (Double) listMap.get("totalQty"),inOrOut);
		sr.setDates(srDates);
		srs.add(sr);
		File file = ReadExcel.outExcel(srs, sr.getIntProdNo() + ".xlsx");
		response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");// 可选择不同类型
		response().setHeader("Content-Disposition", "attachment; filename=" + sr.getIntProdNo() + ".xlsx");
		return ok(file);
	}

	/*
	 * 样式规划
	 */
	public static StuffingRecapitulationReport createReport(String intProdNo, Date reportDate, String isloacle, Double totalQty,String sheetName) {
		StuffingRecapitulationReport sr = new StuffingRecapitulationReport();
		sr.setIntProdNo(intProdNo);
		sr.setReportDate(reportDate);
		sr.setStyle(isloacle);
		sr.setSheetName(sheetName);
		// head
		ChainList<ExcelObj> headList = new ChainList<ExcelObj>();
		Cell cell00 = new Cell("PT. MULTIMAS NABATI ASAHAN", 0, 0);
		Cell cell07 = new Cell(sr.getIntProdNo(), 0, 7);
		Cell cell17 = new Cell("WARE HOUSE SPECIALITY FAT", 2, 7);
		Cell cell27 = new Cell("Revisi    1", 1, 7);
		Title title43 = new Title("STUFFING RECAPITULATION REPORT", 4, 3);
		Title title53 = new Title("LO", 5, 3);
		Cell cell60 = new Cell(isloacle + ":", 6, 0);
		headList.chainAdd(cell00).chainAdd(cell00).chainAdd(cell07).chainAdd(cell17).chainAdd(cell27).chainAdd(title43).chainAdd(title53)
			.chainAdd(cell60);
		// foot
		ChainList<ExcelObj> footList = new ChainList<ExcelObj>();
		Cell cellf06 = new Cell("Total:", 0, 6);
		Cell cellf07 = new Cell(String.valueOf(totalQty), 0, 7);
		footList.chainAdd(cellf06).chainAdd(cellf07);
		sr.setFoot(footList);
		sr.setHead(headList);
		return sr;
	}

	/**
	 * 根据inOrOut字段对查询当天本地(Lokal)出口(Export)冷库的stocks,且根据 pi 和 material 进行汇聚
	 * @param map 
	 * totalQty 是为Double(0)
	 * @return
	 */
	public static Map<String, Object> listExecutedStuffingPlan(TemperingRoomVo trVo) {
		
		Double totalQty = new Double(0);
		Map<String, Object> map = new HashMap<String, Object>();
		List<StuffingRecapitulationDate> srDates = new ArrayList<StuffingRecapitulationDate>();
		Set<UUID> planItemIds = new HashSet<UUID>();
		String executeDate = sdf.format(trVo.executeDate);
		Warehouse wh = SessionSearchUtil.searchWarehouse();
 
	  ExpressionList<Execution> el = Ebean.find(Execution.class).fetch("planItem").fetch("planItem.order").fetch("planItem.order.warehouse").where()
			.eq("deleted", false).eq("planItem.deleted", false).eq("planItem.planType", PLANITEM_TYPE_CARGO)
			.eq("planItem.order.warehouse", wh).ge("executedAt", DateUtil.strToDate(executeDate + " 00:00:00", "yyyy-MM-dd HH:mm:ss"))
			.le("executedAt", DateUtil.strToDate(executeDate + " 23:59:59", "yyyy-MM-dd HH:mm:ss"));
	  if(trVo.inOrOut.trim().equals("Local")){
		  el.eq("planItem.order.externalOrderNo", "planItem.order.contractNo");
	  }else{
		  el.ne("planItem.order.externalOrderNo", "planItem.order.contractNo");
	  }
	  List<Execution> executions = el.order().desc("executedAt").findList();
		if (executions != null && !executions.isEmpty()) {
			for (Execution execution : executions) {
				if (!planItemIds.contains(execution.planItem.id)) {
						StuffingRecapitulationDate sr = StuffingRecapitulationDate.getSR(execution.planItem);
						sr.no = planItemIds.size() + 1;
						srDates.add(sr);
						totalQty += sr.qty;
				 
				}
				planItemIds.add(execution.planItem.id);
			}
		}
		map.put("srDates", srDates);
		map.put("totalQty", totalQty);
		return map;
		//order.externalOrderNo=piNo;
		//order.internalOrderNo=piNo;
		//order.contractNo=refNo;
	}
}
