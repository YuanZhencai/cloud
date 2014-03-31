/** * PackageProductStockController.java 
* Created on 2013-6-5 下午4:16:01 
*/

package controllers.report;

import static utils.DateUtil.DATE_PATTERN;
import static utils.DateUtil.dateToStrShort;
import static utils.DateUtil.minusOneDay;
import static utils.DateUtil.strToDate;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import models.Batch;
import models.Execution;
import models.Material;
import models.MaterialUom;
import models.Order;
import models.OrderItem;
import models.PlanItem;
import models.Stock;
import models.StockTransaction;
import models.TimingPolicy;
import models.printVo.Cell;
import models.printVo.Title;
import models.printVo.planAndRealization.PrintDetial;
import models.printVo.planAndRealization.SearchVo;
import models.printVo.planAndRealization.StuffingDate;
import models.printVo.planAndRealization.StuffingDetailVo;
import models.printVo.planAndRealization.StuffingReport;
import models.printVopackProductStockReport.PackProductStockDate;
import models.printVopackProductStockReport.PackProductStockReport;
import models.vo.Result.ResultVo;
import models.vo.outbound.searchVo;
import models.vo.query.BatchVo;
import models.vo.report.PackProductStockSearchVo;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import play.mvc.With;
import utils.ChainList;
import utils.CheckUtil;
import utils.DateUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import utils.Excel.ExcelObj;
import utils.Excel.ReadExcel;
import views.html.report.stuffingForPlanning;
import action.Menus;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.SqlRow;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: PackageProductStockController.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:chenaihong@wcs-global.com">ChenAi hong</a>
 */
@With(Menus.class)
public class StuffingForPlanningController extends Controller {
	
	public static String orderSourceTypeOfCargo = "T003";
	public static final String PLAN_IN_TYPE 	= "T001";
	public static final String PLAN_SHIP_TYPE	= "T003";
	
	public static final String TRANSACTION_IN_TYPE 	= "T101";
	public static final String TRANSACTION_SHIP_TYPE	= "T301";
	
	public static final String STORAGE_TYPE_AGEING 		= "Ageing";
	public static final String STORAGE_TYPE_TEMPERING 	= "Tempering";
	
	public static Map<String,Boolean> uniqueMp = null;//判断统计的material唯一性

	public static Result index() {
		return ok(stuffingForPlanning.render(""));
	}

	public static Result list(){
		PrintDetial printDetial=new PrintDetial();
        RequestBody body = request().body();
        if(body.asJson()!=null)
        	printDetial = Json.fromJson(body.asJson(), PrintDetial.class);
		 //PrintDetial printDetial=searchVo.getVo();
        DateTime dateToDateTimeFrom=null;
        if(printDetial.dateFrom!=null&&!printDetial.dateFrom.equals(""))
        	dateToDateTimeFrom= DateUtil.dateToDateTime(printDetial.dateFrom);
        else
        	dateToDateTimeFrom=DateUtil.dateToDateTime(DateUtil.strToDate("1990-1-1", "yyyy-MM-dd"));
        DateTime dateToDateTimeTo=null;
        if(printDetial.dateTo!=null&&!printDetial.dateTo.equals(""))
        	dateToDateTimeTo = DateUtil.dateToDateTime(printDetial.dateTo);
        else
        	dateToDateTimeTo=DateUtil.dateToDateTime(DateUtil.strToDate("2020-1-1", "yyyy-MM-dd"));
        if (printDetial.dateFrom != null && printDetial.dateTo != null) {
			if (printDetial.dateFrom.after(printDetial.dateTo)) {
				return ok(play.libs.Json.toJson(new ResultVo("error","fromDate can not be greater than toDate,Please correct!")));
			}
		}        
        // System.out.println(dateToDateTimeFrom.getDayOfMonth());
            // System.out.println(DateUtil.setDay(stuffingReport.getReportDate(), j));
             List<PlanItem> planItems = PlanItem.find().fetch("material").fetch("plan").fetch("orderItem.order").fetch("orderItem").fetch("material").fetch("fromMaterialUom").fetch("toMaterialUom").where().eq("orderItem.deleted",false).eq("material.deleted",false).eq("fromMaterialUom.deleted",false).eq("toMaterialUom.deleted",false).eq("planType", orderSourceTypeOfCargo).eq("deleted",false)/*.El("plan.planStatus", "S002")*/.eq("order.warehouse.id",  SessionSearchUtil.searchWarehouse().id.toString()).eq("order.deleted", false).eq("orderItem.deleted",false ).between("plannedExecutionAt",dateToDateTimeFrom,dateToDateTimeTo.plus(1)).findList();;
             System.out.println(planItems.size());
             List<StuffingDetailVo> stuffingDates=new ArrayList<StuffingDetailVo>();
             for (PlanItem planItem : planItems) {
                 System.out.println(planItem.order.internalOrderNo+":"+planItem.order.contractNo+"++++++++++++++++++++++++13246548976541321654+++++++++++++++++++++++++++++++++++++++++");
              /*  List<Execution> executions = Execution.find().where().eq("deleted", false).eq("planItem", planItem).findList();
                Timestamp OUT = null;
                if(executions!=null){
                    for (Execution execution : executions) {
                        if(OUT!=null&&OUT.before(execution.executedAt)){
                            OUT=execution.executedAt;
                        }else{
                            OUT=execution.executedAt;
                        }
                    }
                }*/
                StuffingDetailVo stuffingDate = new StuffingDetailVo();
                stuffingDate.inPlanItem(planItem);
                if(printDetial.style!=null&&printDetial.style.equals("Product Exprot")){
                	if(CheckUtil.exportCheckByPI(stuffingDate.PI)){
                		stuffingDates.add(stuffingDate);
                	}
                }else if(printDetial.style!=null&&printDetial.style.equals("Product LoKal")){
                	if(!CheckUtil.exportCheckByPI(stuffingDate.PI)){
                		stuffingDates.add(stuffingDate);
                	}
                }else{
                	return ok(play.libs.Json.toJson(new ResultVo("error","Please fill the PrintStyle")));
                }
            }
         return ok(play.libs.Json.toJson(new ResultVo(stuffingDates)));
	}

	
	/**
	 *  下载模板
	 * @return
	 */
	static Form<SearchVo> batchVoForm = Form.form(SearchVo.class);
	 public static Result print(){
		 SearchVo searchVo=new SearchVo();
		 System.out.println(play.libs.Json.toJson(request().body().asFormUrlEncoded()));
		 if(!batchVoForm.hasErrors()) {
			// System.out.println("1111111111111");
			 searchVo = batchVoForm.bindFromRequest().get();
			// System.out.println(play.libs.Json.toJson(searchVo));
			}
		 PrintDetial printDetial=searchVo.getVo();
         ArrayList<StuffingReport> StuffingReports = new ArrayList<StuffingReport>();
         DateTime dateToDateTimeFrom=null;
         if(printDetial.dateFrom!=null&&!printDetial.dateFrom.equals(""))
        	 dateToDateTimeFrom = DateUtil.dateToDateTime(printDetial.dateFrom);
         else
        	 dateToDateTimeFrom =new DateTime();
         DateTime dateToDateTimeTo=null;
         if(printDetial.dateTo!=null&&!printDetial.dateTo.equals(""))
        	 dateToDateTimeTo= DateUtil.dateToDateTime(printDetial.dateTo);
         else 
        	 dateToDateTimeTo=new DateTime();
         System.out.println(dateToDateTimeFrom.getDayOfMonth());
         for (DateTime j = dateToDateTimeFrom; j.isBefore(dateToDateTimeTo.plus(1)) ; j=j.plusDays(1)) {
        DateTime TempDate=j;
             StuffingReport stuffingReport = CreateReport(printDetial.intProdNo,DateUtil.dateToStrShort(printDetial.printDate),printDetial.style,String.valueOf(j.getMonthOfYear())+"."+String.valueOf(j.getDayOfMonth()));
            // System.out.println(DateUtil.setDay(stuffingReport.getReportDate(), j));
             List<PlanItem> planItems = PlanItem.find().fetch("material").fetch("plan").fetch("orderItem.order").fetch("orderItem").fetch("material").fetch("fromMaterialUom").fetch("toMaterialUom").where().eq("orderItem.deleted",false).eq("material.deleted",false).eq("fromMaterialUom.deleted",false).eq("toMaterialUom.deleted",false).eq("planType", orderSourceTypeOfCargo).eq("deleted",false)/*.El("plan.planStatus", "S002")*/.eq("order.warehouse.id",  SessionSearchUtil.searchWarehouse().id.toString()).eq("order.deleted", false).eq("orderItem.deleted",false ).between("plannedExecutionAt",TempDate.plus(-1),TempDate).findList();;
             System.out.println(planItems.size());
             List<ExcelObj> stuffingDates=new ArrayList<ExcelObj>();
             boolean isadd=true;
             for (PlanItem planItem : planItems) {
                 System.out.println(planItem.order.internalOrderNo+":"+planItem.order.contractNo+"++++++++++++++++++++++++13246548976541321654+++++++++++++++++++++++++++++++++++++++++");
                List<Execution> executions = Execution.find().where().eq("deleted", false).eq("planItem", planItem).findList();
                Timestamp OUT = null;
                if(executions!=null){
                    for (Execution execution : executions) {
                        if(OUT!=null&&OUT.before(execution.executedAt)){
                            OUT=execution.executedAt;
                        }else{
                            OUT=execution.executedAt;
                        }
                    }
                }
                System.out.println("out::::::::"+OUT);
                StuffingDate stuffingDate = new StuffingDate();
                stuffingDate.inPlanItem(planItem);
                if(OUT!=null)
                stuffingDate.Out=DateUtil.timestampToDate(OUT);
                if(stuffingReport.getStyle().equals("Product Exprot")){
                	if(!stuffingDate.PI.equals(stuffingDate.Referency)){
                		isadd=false;
                		stuffingDates.add(stuffingDate);
                	}
                }else if(stuffingReport.getStyle().equals("Product LoKal")){
                	if(stuffingDate.PI.equals(stuffingDate.Referency)){
                		isadd=false;
                		stuffingDates.add(stuffingDate);
                	}
                }
            }
             if(isadd){
                 stuffingDates.add( new StuffingDate() );
             }
             stuffingReport.setDates(stuffingDates);
             System.out.println(stuffingReport.getDates().size());
             StuffingReports.add(stuffingReport);
         }
         File file = ReadExcel.outExcel(StuffingReports,printDetial.intProdNo+".xlsx");
         response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");//可选择不同类型
         response().setHeader("Content-Disposition", "attachment; filename=" + printDetial.intProdNo+".xlsx");
         return ok(file);
     }
     /*
      * 样式规划
      */
     public static StuffingReport CreateReport(String IntProdNo,String reportDate,String isloacle,String sheel){
         StuffingReport stuffingReport=new StuffingReport();
         stuffingReport.setIntProdNo(IntProdNo);
         stuffingReport.setReportDate(DateUtil.strToDate(reportDate,"yyyy-MM-dd"));
         stuffingReport.setStyle(isloacle);
         System.out.println(stuffingReport.getStyle());
         stuffingReport.setSheetName(sheel);
         List<ExcelObj> arrayList = new ArrayList<ExcelObj>();
         Cell cell0 = new Cell("PT.MULTIMAS NABATI ASAHAN",0,0);
         arrayList.add(cell0);
         Cell cell1 = new Cell(stuffingReport.getIntProdNo(),0,17);
         arrayList.add(cell1);
         Cell cell2 = new Cell("Warehouse - Speciality Fats",1,0);
         arrayList.add(cell2);
         Cell cell3 = new Cell("Revisi 3",1,17);
         arrayList.add(cell3);
         Title title=new Title("RENCANA TRANSPORT CARGO   "+DateUtil.dateToStrShortEn(stuffingReport.getReportDate()),2,8);
         arrayList.add(title);
         Cell cell4 = new Cell(stuffingReport.getStyle(),3,0);
         arrayList.add(cell4);
         List<ExcelObj> arrayList1 = new ArrayList<ExcelObj>();
         arrayList1.add(cell2);
         //stuffingReport.setFoot(arrayList1);
         stuffingReport.setHead(arrayList);
         return stuffingReport;
     }

}
