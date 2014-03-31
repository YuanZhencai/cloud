package controllers.query;
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
import models.printVo.Transaction.TransactionDate;
import models.printVo.Transaction.TransactionReport;
import models.vo.Result.ResultVo;
import models.vo.outbound.searchVo;
import models.vo.query.BatchVo;
import models.vo.query.BinVo;
import models.vo.query.StockCountVo;
import models.vo.query.StockVo;
import models.vo.query.TransTypeVo;
import models.vo.query.TransactionQueryVo;
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
public class TransactionQueryController extends Controller {
	static String warehouseId = "fe640b63-5501-44b0-9fb1-6bbd93c5e8e5";
	static String SourceTypeOfCargo="T003";
	static String orderType="T001";
	 public static Result index() {
		 return ok(views.html.query.transactionQuery.render(""));
	 }
	 public static Result TypeList(){
		 List<TransTypeVo> transTypeVos=new ArrayList<TransTypeVo>();
		 List<Code> transActionTypes = CodeUtil.getTransActionTypes();
		 for (Code code : transActionTypes) {
			TransTypeVo transTypeVo = new TransTypeVo();
			transTypeVo.inCode(code);
			transTypeVos.add(transTypeVo);
		}
		 System.out.println(transTypeVos.size());
		 return ok(play.libs.Json.toJson(new ResultVo(transTypeVos)));
	 }
	 public static Result list(){
		 System.out.println("============list=====================");
		 List<TransactionQueryVo> TransactionQueryVos=new ArrayList<TransactionQueryVo>();
		 searchVo searchVo=new searchVo();
		 RequestBody body = request().body();
		 if(body.asJson()!=null)
		 searchVo = Json.fromJson(body.asJson(), searchVo.class);
		 TransactionQueryVos=new ArrayList<TransactionQueryVo>();
		 //System.out.println(searchVo.fromDate+":"+searchVo.toDate);
		 //List<StockTransaction> stockTransactions = StockTransaction.find().all();
		 List<Batch> batchs = BatchSearchUtil.getlikeBatch(searchVo.piNo);
		 System.out.println("BATCHSIZE"+batchs.size());
		 if (searchVo.fromDate != null && searchVo.toDate != null) {
				if (searchVo.fromDate.after(searchVo.toDate)) {
					return ok(play.libs.Json.toJson(new ResultVo("error","fromDate can not be greater than toDate,Please correct!")));
				}
			}
		List<StockTransaction> stockTransactions = StockTransaction.find()
				.fetch("stock")
				.fetch("stock.area.storageType")
				.fetch("stock.batch")
				.fetch("stock.material")
				.where().in("stock.batch",batchs)
				.like("transactionCode",searchVo.piStatus==null?"%":"%"+searchVo.piStatus+"%" )
				.between("transactionAt",DateUtil.strToDate((searchVo.fromDate==null||searchVo.fromDate.equals(""))?"2000-1-1 00:00:00":DateUtil.dateToStrShort(searchVo.fromDate)+" 00:00:00","yyyy-MM-dd HH:mm:ss"), DateUtil.strToDate((searchVo.toDate==null||searchVo.toDate.equals(""))?"2100-1-1 23:59:59":DateUtil.dateToStrShort(searchVo.toDate)+" 23:59:59","yyyy-MM-dd HH:mm:ss"))
				.eq("warehouse.id", SessionSearchUtil.searchWarehouse().id.toString())
				.eq("deleted", false).
				findList();
		 System.out.println(Json.toJson(stockTransactions.size()));
		 for (StockTransaction stockTransaction : stockTransactions) {
			 //System.out.println( stockTransaction.stock.batch.parent.batchNo);
			 ExpressionList<Order> expressionList = Order.find().where().eq("internalOrderNo", ExtUtil.unapply(stockTransaction.stock.batch.ext).get("pi"))
			 .eq("orderType", orderType).eq("deleted", false);
			 if(searchVo.sgPiNo!=null&&!searchVo.sgPiNo.equals(""))
				 expressionList.like("contractNo", "%"+searchVo.sgPiNo+"%");
			//System.out.println("ORDER:"+orders.size());
			 List<Order> orders =expressionList.findList();
			if(orders.size()>0){
			Order order = orders.get(0);
			//System.out.println(order.contractNo);
			TransactionQueryVo transactionQueryVo = new  TransactionQueryVo();
			transactionQueryVo.inTransaction(stockTransaction);
			transactionQueryVo.inOrder(order);
			boolean ishave=false;
			for (TransactionQueryVo transaction : TransactionQueryVos) {
			    ishave=ishave||transactionQueryVo.compareClass(transaction);
            }
			if(!ishave)
			TransactionQueryVos.add(transactionQueryVo);
			}
		 }
		 System.out.println(TransactionQueryVos.size());
		 return ok(play.libs.Json.toJson(new ResultVo(TransactionQueryVos)));
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
			 List<Batch> batchs = BatchSearchUtil.getlikeBatch(searchVo.piNo);
			 System.out.println("BATCHSIZE"+batchs.size());
			 if (searchVo.fromDate != null && searchVo.toDate != null) {
					if (searchVo.fromDate.after(searchVo.toDate)) {
						return ok(play.libs.Json.toJson(new ResultVo("error","fromDate can not be greater than toDate,Please correct!")));
					}
				}
			List<StockTransaction> stockTransactions = StockTransaction.find()
					.fetch("stock")
					.fetch("stock.area.storageType")
					.fetch("stock.batch")
					.fetch("stock.material")
					.where().in("stock.batch",batchs)
					.like("transactionCode",searchVo.piStatus==null?"%":"%"+searchVo.piStatus+"%" )
					.between("transactionAt",DateUtil.strToDate((searchVo.fromDate==null||searchVo.fromDate.equals(""))?"2000-1-1 00:00:00":DateUtil.dateToStrShort(searchVo.fromDate)+" 00:00:00","yyyy-MM-dd HH:mm:ss"), DateUtil.strToDate((searchVo.toDate==null||searchVo.toDate.equals(""))?"2100-1-1 23:59:59":DateUtil.dateToStrShort(searchVo.toDate)+" 23:59:59","yyyy-MM-dd HH:mm:ss"))
					.eq("warehouse.id", SessionSearchUtil.searchWarehouse().id.toString())
					.eq("deleted", false).
					findList();
			 System.out.println(Json.toJson(stockTransactions.size()));
			 List<TransactionDate> transactionDates=new ArrayList<TransactionDate>();
			 for (StockTransaction stockTransaction : stockTransactions) {
				 ExpressionList<Order> expressionList = Order.find().where().eq("internalOrderNo", ExtUtil.unapply(stockTransaction.stock.batch.ext).get("pi"))
						 .eq("orderType", orderType).eq("deleted", false);
						 if(searchVo.sgPiNo!=null&&!searchVo.sgPiNo.equals(""))
							 expressionList.like("contractNo", "%"+searchVo.sgPiNo+"%");
						//System.out.println("ORDER:"+orders.size());
						 List<Order> orders =expressionList.findList();
				 System.out.println("ORDER:"+orders.size());
					if(orders.size()>0){
					Order order = orders.get(0);
					TransactionDate transactionDate = new TransactionDate();
					transactionDate.inTransaction(stockTransaction);
					transactionDate.inOrder(order);
					transactionDates.add(transactionDate);
					}
			 }
			 List<TransactionReport> reports = new ArrayList<TransactionReport>();
			 TransactionReport createReport = CreateReport(transactionDates,searchVo);
			  reports.add(createReport);
			  
			  File file = ReadExcel.outExcel(reports, createReport.getIntProdNo()+".xlsx");
		      response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");//可选择不同类型
		      response().setHeader("Content-Disposition", "attachment; filename=" + "Transaction-Report"+".xlsx");
		      return ok(file);
	 }
	 public static TransactionReport  CreateReport( List<? extends ExcelObj> ReportDates,searchVo searchVo){
		 TransactionReport transactionReport = new TransactionReport();
		 List<ExcelObj> arrayList = new ArrayList<ExcelObj>();
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
         System.out.println(searchVo.piStatus);
         Cell cell5 = new Cell(searchVo.piStatus!=null&&!searchVo.piStatus.equals("")&&CodeUtil.getTransActionTypeByCode(searchVo.piStatus)!=null?CodeUtil.getTransActionTypeByCode(searchVo.piStatus):"All",2,1);
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
		    //List<? extends ExcelObj> ReportDates=null;
		 transactionReport.setDates(ReportDates);
		 return transactionReport;
		}
}
