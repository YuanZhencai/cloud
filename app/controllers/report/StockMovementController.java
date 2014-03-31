package controllers.report;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.avaje.ebean.ExpressionList;

import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import models.Batch;
import models.Bin;
import models.Code;
import models.Order;
import models.StockTransaction;
import models.printVo.Cell;
import models.printVo.StockCount.StockCountDateByBatch;
import models.printVo.StockCount.StockCountDateByLocation;
import models.printVo.StockCount.StockCountReport;
import models.printVo.stockMovement.StockMovementDate;
import models.printVo.stockMovement.StockMovementReport;
import models.vo.Result.ResultVo;
import models.vo.query.BatchVo;
import models.vo.query.BinVo;
import models.vo.query.StockCountVo;
import models.vo.query.StockVo;
import models.vo.query.TransTypeVo;
import models.vo.query.TransactionQueryVo;
import models.vo.report.searchVo;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import play.mvc.Http.RequestBody;
import security.NoUserDeadboltHandler;
import utils.BatchSearchUtil;
import utils.CodeUtil;
import utils.DateUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import utils.Excel.ExcelObj;
import utils.Excel.ReadExcel;
@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class StockMovementController extends Controller {
	//static String warehouseId = "fe640b63-5501-44b0-9fb1-6bbd93c5e8e5";
	static String SourceTypeOfCargo="T003";
	static String orderType="T001";
	 public static Result index() {
		 return ok(views.html.report.stockMovement.render(""));
	 }
	
	 public static Result list(){
		 System.out.println("============list=====================");
		 searchVo searchVo=new searchVo();
		 RequestBody body = request().body();
		 if(body.asJson()!=null)
		 searchVo = Json.fromJson(body.asJson(), searchVo.class);
		 //System.out.println(searchVo.fromDate+":"+searchVo.toDate);
		 //List<StockTransaction> stockTransactions = StockTransaction.find().all();
		 if (searchVo.fromDate != null && searchVo.toDate != null) {
				if (searchVo.fromDate.after(searchVo.toDate)) {
					return ok(play.libs.Json.toJson(new ResultVo("error","fromDate can not be greater than toDate,Please correct!")));
				}
			}
		 List<StockMovementDate> transactionList = getTransactionList(searchVo);
		 System.out.println(transactionList.size());
		 return ok(play.libs.Json.toJson(new ResultVo(transactionList)));
	 }
	 public static List<StockMovementDate> getTransactionList(searchVo searchVo){
		 System.out.println(play.libs.Json.toJson(searchVo));
		 List<Batch> batchs = BatchSearchUtil.getlikeBatch(searchVo.piNo,searchVo.batchNo);
		 List<String> batchIds=new ArrayList<String>();
		 for (Batch batch : batchs) {
			 batchIds.add(batch.id.toString());
		}
		 System.out.println("BATCHSIZE"+batchs.size());
		List<StockTransaction> stockTransactions = StockTransaction.find()
				.fetch("stock")
				.fetch("stock.area.storageType")
				.fetch("stock.batch")
				.fetch("stock.material")
				.where().in("stock.batch.id",batchIds)
				//.like("transactionCode","T2%" )
				.between("transactionAt",DateUtil.strToDate((searchVo.fromDate==null||searchVo.fromDate.equals(""))?"2000-1-1 00:00:00":DateUtil.dateToStrShort(searchVo.fromDate)+" 00:00:00","yyyy-MM-dd HH:mm:ss"), DateUtil.strToDate((searchVo.toDate==null||searchVo.toDate.equals(""))?"2100-1-1 23:59:59":DateUtil.dateToStrShort(searchVo.toDate)+" 23:59:59","yyyy-MM-dd HH:mm:ss"))
				.eq("warehouse.id", SessionSearchUtil.searchWarehouse().id.toString())
				.eq("deleted", false).
				findList();
		 System.out.println(Json.toJson(stockTransactions.size()));
		 List<StockMovementDate> transactionDates=new ArrayList<StockMovementDate>();
		 for (StockTransaction stockTransaction : stockTransactions) {
				StockMovementDate transactionDate = new StockMovementDate();
				transactionDate.inTransaction(stockTransaction);
				boolean isAdd=true;
				if(searchVo.palletNo!=null&&!"".equals(searchVo.palletNo)){
					if(!searchVo.palletNo.equals(transactionDate.stockNo)){
						isAdd=false;
					}
				}
				for (StockMovementDate stockMovement : transactionDates) {
					if(isAdd==true&&stockMovement.isSame(transactionDate)){
						isAdd=false;
					}
				}
				if(isAdd)
				transactionDates.add(transactionDate);
		 }
			 return transactionDates;
	 }
	 
	 
	 static Form<searchVo> batchVoForm = Form.form(searchVo.class);
	 public static Result report(){
		 searchVo searchVo=null;
		 if(batchVoForm.hasErrors()) {
			 searchVo=new searchVo();
			} else {
			searchVo = batchVoForm.bindFromRequest().get();
			}
		  System.out.println(play.libs.Json.toJson(searchVo));
			 if (searchVo.fromDate != null && searchVo.toDate != null) {
					if (searchVo.fromDate.after(searchVo.toDate)) {
						return ok(play.libs.Json.toJson(new ResultVo("error","fromDate can not be greater than toDate,Please correct!")));
					}
				}
			 List<StockMovementDate> transactionDates = getTransactionList(searchVo);
			 List<StockMovementReport> reports = new ArrayList<StockMovementReport>();
			 StockMovementReport createReport = CreateReport(transactionDates,searchVo);
			  reports.add(createReport);
			  
			  File file = ReadExcel.outExcel(reports, createReport.getIntProdNo()+".xlsx");
		      response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");//可选择不同类型
		      response().setHeader("Content-Disposition", "attachment; filename=" + "Transaction-Report"+".xlsx");
		      return ok(file);
	 }
	 public static StockMovementReport  CreateReport( List<? extends ExcelObj> ReportDates,searchVo searchVo){
		 StockMovementReport transactionReport = new StockMovementReport();
		 /*List<ExcelObj> arrayList = new ArrayList<ExcelObj>();
         Cell cell0 = new Cell("PI NO",1,0);
         arrayList.add(cell0);
         Cell cell1 = new Cell(searchVo.piNo!=null&&!searchVo.piNo.equals("")?searchVo.piNo:"All",1,1);
         arrayList.add(cell1);
         Cell cell2 = new Cell("Report Date",1,7);
         arrayList.add(cell2);
         Cell cell3 = new Cell(DateUtil.dateToStrShort(new Date()),1,8);
         arrayList.add(cell3);
         Cell cell4=new Cell("Transaction Type",2,0);
         arrayList.add(cell4);
         arrayList.add(cell5);
         Cell cell6 = new Cell("Generate By ",2,7);
         arrayList.add(cell6);
         Cell cell7 = new Cell(SessionSearchUtil.searchUser().name,2,8);
         arrayList.add(cell7);
        // List<ExcelObj> arrayList1 = new ArrayList<ExcelObj>();
        // arrayList1.add(cell2);
         //stuffingReport.setFoot(arrayList1);
         transactionReport.setHead(arrayList);
		 transactionReport.setSheetName("sheet1");
		    //List<? extends ExcelObj> ReportDates=null;*/
		 transactionReport.setDates(ReportDates);
		 return transactionReport;
		}
}
