package controllers.report;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Bin;
import models.PlanItem;
import models.printVo.Cell;
import models.printVo.productionSchedule.ProductionScheduleData;
import models.printVo.productionSchedule.ProductionScheduleRePort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import play.mvc.With;
import utils.ChainList;
import utils.DateUtil;
import utils.Excel.ExcelObj;
import utils.Excel.ReadExcel;
import utils.enums.CodeKey;
import views.html.report.productionSchedule;
import action.Menus;

import com.avaje.ebean.Ebean;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: ProductionSchedule.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
@With(Menus.class)
public class ProductionScheduleController extends Controller {
	
 
	private static final String STORAGE_TYPE = "Receiving";
	private static final Logger logger =LoggerFactory.getLogger(TemperingRoomController.class);
	/**
	 * 页面初始化
	 * @return
	 */
	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M");
	public static Result index() {
		return ok(productionSchedule.render(""));
	}
	
	/**
	 * 
	 * @return
	 */
	public static Result list() {
		logger.info("^^^^^^^^^^^ you have in list method  ^^^^^^^^^^^^^^^^^^^^^^^^^^");
		RequestBody body = request().body();
		if (body.asJson() != null) {
			//logger.info("^^^^^^^^^^^^ " + body.asJson());
			Map<String,Object> map = Json.fromJson(body.asJson(), Map.class);
			List<ProductionScheduleData> piSchedules = listPISchedule(map);
			//logger.info("^^^^^^^^^^^^^^   ProductionScheduleDate  ^^^^^^^^^^^^^^^^^^"+Json.toJson(piSchedules));
			return ok(Json.toJson(piSchedules));
		} else {
			return badRequest("your request information is wrong");
	}
	}
	/**
	 * 
	 * @return
	 */
	public static Result download(Long executeDate) {
		logger.info("^^^^^^^^^^^ you have in download method  ^^^^^^^^^^^^^^^^^^^^^^^^^^"+executeDate);
		ArrayList<ProductionScheduleRePort> psReorts = new ArrayList<ProductionScheduleRePort>();
		List<ExcelObj> piDates = null;
		ProductionScheduleRePort psReort = null;
	   
		Map<String, Object> map = new HashMap<String, Object>();
		 map.put("executeDate", executeDate);
			Timestamp ts = new Timestamp((Long)executeDate);
			Calendar current = Calendar.getInstance();
			Calendar execute = Calendar.getInstance();
			execute.setTimeInMillis(ts.getTime());
			boolean monthFlag = false;
			boolean dateFlag = true;
			//判断当前月份是否小于执行月份
		    if(execute.get(Calendar.YEAR) <current.get(Calendar.YEAR)
		    	||(execute.get(Calendar.YEAR) ==current.get(Calendar.YEAR) && execute.get(Calendar.MONTH) ==current.get(Calendar.MONTH))){
		    	monthFlag = true;
		    }
		 
	    List<ProductionScheduleData> piSchedules = listPISchedule(map);
	   List<Bin> bins = Ebean.find(Bin.class).fetch("area").fetch("area.storageType").where().eq("deleted", false).eq("area.storageType.nameKey", STORAGE_TYPE).findList();
	   for(Bin bin :bins){//每个生产线生成对应的sheet
		   piDates = new ArrayList<ExcelObj>();
		   for(int i=1;i<=execute.getActualMaximum(Calendar.DAY_OF_MONTH);i++){
			   dateFlag = true;
			   for (ProductionScheduleData piSchedule : piSchedules) {
				   if(piSchedule.line.equals(bin.nameKey)&&piSchedule.date==i){
					   piDates.add(piSchedule); 
					   dateFlag = false;
				   }
			   }
			   if(dateFlag){
				   piDates.add(new ProductionScheduleData((double) i));  
			   }
		   }
		   psReort = createReport("F-MNA-LOG-00-001", new Date(),bin.nameKey);
		   psReort.setDates(piDates);
		   psReorts.add(psReort); 
	   }
		File file = ReadExcel.outExcel(psReorts, psReort.getIntProdNo() + ".xlsx");
		response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");// 可选择不同类型
		response().setHeader("Content-Disposition", "attachment; filename=" + psReort.getIntProdNo() + ".xlsx");
		return ok(file);
	}
	
	/*
	 * 样式规划
	 */
	public static ProductionScheduleRePort createReport(String intProdNo, Date reportDate,String sheetName ) {
		ProductionScheduleRePort piSchedule = new ProductionScheduleRePort();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d");
		piSchedule.setIntProdNo(intProdNo);
		piSchedule.setReportDate(reportDate);
		piSchedule.setSheetName(sheetName);
		Calendar ca = Calendar.getInstance();
		ca.setTime(reportDate);
		// head
		ChainList<ExcelObj> headList = new ChainList<ExcelObj>();
		Cell cell00 = new Cell("Texturing Plant", 0, 0);
		Cell cell018 = new Cell(piSchedule.getIntProdNo(), 0, 18);
		Cell cell10 = new Cell("PRODUCTION SCHEDULING ", 1, 0);
		Cell cell13 = new Cell("Date", 1, 3);
		Cell cell14 = new Cell(sdf.format(reportDate), 1, 4);
		Cell cell20 = new Cell("LINE "+sheetName, 2, 0);
		Cell cell30 = new Cell("Month "+(ca.get(Calendar.MONTH)+1)+" "+ca.get(Calendar.YEAR), 3, 0);
		Cell cell40 = new Cell("Date", 4, 0);
		Cell cell41 = new Cell(sdf.format(reportDate), 4, 0);
	 
		headList.chainAdd(cell00).chainAdd(cell018).chainAdd(cell10).chainAdd(cell13).
		chainAdd(cell14).chainAdd(cell20).chainAdd(cell30).chainAdd(cell40).chainAdd(cell41);
		// foot
		ChainList<ExcelObj> footList = new ChainList<ExcelObj>();
		Cell cellf12 = new Cell("NB : - Untuk No.PI yang berwarna BLUE carton sdh ready ", 1, 2);
		Cell cellf22 = new Cell("      - Tulisan tipis sdh di produksi  ", 2, 2);
		Cell cellf32 = new Cell("         - Tulisan Tebal  belum di produksi ", 3, 2);
		Cell cellf52 = new Cell("             Prepared by,", 5, 2);
		Cell cellf510 = new Cell("             Acknowledged by,", 5, 10);
		Cell cellf62 = new Cell("             Edi suanto ", 6, 2);
		Cell cellf610 = new Cell("             Oki.W", 6, 10);
		footList.chainAdd(cellf12).chainAdd(cellf22).chainAdd(cellf32).chainAdd(cellf52).chainAdd(cellf510).chainAdd(cellf62).chainAdd(cellf610);
		piSchedule.setFoot(footList);
		piSchedule.setHead(headList);
		return piSchedule;
	}
	
	/**
	 * 
	 * @return  ProductionScheduleData的数组
	 */
	public static List<ProductionScheduleData> listPISchedule(Map<String,Object> map ){
		List<ProductionScheduleData> piSchedules = new ArrayList<ProductionScheduleData>();
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Object executeDate = map.get("executeDate");
		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();
		from.setTimeInMillis((Long)executeDate);
		to.setTimeInMillis((Long)executeDate);
		to.add(Calendar.MONTH, 1);
		List<PlanItem> planItems = Ebean.find(PlanItem.class).fetch("order").fetch("orderItem").where().eq("deleted", false).
			eq("planType",CodeKey.PLAN_TYPE_RECEIVE.toString()).eq("order.orderType", CodeKey.ORDER_TYPE_PRODUCE.toString()).
			ge("createdAt", DateUtil.strToDate(from.get(Calendar.YEAR)+"-"+(from.get(Calendar.MONTH)+1) + "-01 00:00:00", "yyyy-M-dd HH:mm:ss")). 
			le("createdAt", DateUtil.strToDate(to.get(Calendar.YEAR)+"-"+(to.get(Calendar.MONTH)+1) + "-01 00:00:00", "yyyy-MM-dd HH:mm:ss"))
			.order("createdAt ascending").
			findList();
		if(planItems!=null&& !planItems.isEmpty()){
			for(PlanItem planItem : planItems){
				ProductionScheduleData prDate = ProductionScheduleData.setProductionScheduleData(planItem);
				piSchedules.add(prDate);
			}
		}
		return piSchedules;
	}
}
