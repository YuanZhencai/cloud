/** * PackageProductStockController.java 
* Created on 2013-6-5 涓嬪崍4:16:01 
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
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import models.Batch;
import models.Execution;
import models.Material;
import models.MaterialUom;
import models.Order;
import models.OrderItem;
import models.Stock;
import models.StockTransaction;
import models.TimingPolicy;
import models.printVo.Cell;
import models.printVo.Title;
import models.printVopackProductStockReport.PackProductStockDate;
import models.printVopackProductStockReport.PackProductStockReport;
import models.vo.query.BatchVo;
import models.vo.report.PackProductStockSearchVo;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Http.Response;
import play.mvc.Result;
import play.mvc.With;
import utils.BatchSearchUtil;
import utils.ChainList;
import utils.CheckUtil;
import utils.DateUtil;
import utils.ExtUtil;
import utils.Excel.ExcelObj;
import utils.Excel.ReadExcel;
import views.html.pageNotFound;
import views.html.report.packProductStock;
import action.Menus;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExampleExpression;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.annotation.Where;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: PackageProductStockController.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:chenaihong@wcs-global.com">ChenAi hong</a>
 */
@With(Menus.class)
public class PackProductStockController extends Controller {
	
	
	public static final String PLAN_IN_TYPE 	= "T001";
	public static final String PLAN_SHIP_TYPE	= "T003";
	
	public static final String TRANSACTION_IN_TYPE 	= "T101";
	public static final String TRANSACTION_SHIP_TYPE	= "T301";
	
	public static final String STORAGE_TYPE_AGEING 		= "Ageing";
	public static final String STORAGE_TYPE_TEMPERING 	= "Tempering";
	
	public static Map<String,Boolean> uniqueMp = null;//鍒ゆ柇缁熻鐨刴aterial鍞竴鎬�
	
	public static List<PackProductStockDate> cachedSrDateList = new ArrayList<PackProductStockDate>(0);//缂撳瓨缁撴灉闆�

	public static Result index() {
		return ok(packProductStock.render(""));
	}

	@SuppressWarnings("unchecked")
	public static Result list(){
		response().setHeader(Response.CONNECTION, "1000000");
		RequestBody body = request().body();
		if (body.asJson() != null) {
			PackProductStockSearchVo trVo = Json.fromJson(body.asJson(), PackProductStockSearchVo.class);
			if(trVo.dateTimeFrom==null){
				trVo.dateTimeFrom=new Date(0);
			}
			if(trVo.dateTimeTo==null){
				trVo.dateTimeTo=new Date();
			}
			//trVo.exportFlag=false;
//			Map<String,Object> sParams = Json.fromJson(body.asJson(), HashMap.class);
//			System.out.println("sParams:"+sParams);
			List<PackProductStockDate> srr = (List<PackProductStockDate>) listExecutedStuffingPlan(trVo).get("srDates");
			cachedSrDateList = srr;
			//System.out.println("^^^^^^^^^^^^^^   StuffingRecapitulationDate  ^^^^^^^^^^^^^^^^^^" + srr);
			return ok(Json.toJson(srr));
		}else{
			cachedSrDateList = new ArrayList<PackProductStockDate>(0);
			return badRequest("wrong");
		}
	}
	
	
	/**
	 * @param packProductStockSearchVo
	 * @return
	 * 妯＄硦鏌ヨ鏁版嵁
	 */
	public static Map<String, Object> listExecutedStuffingPlan(PackProductStockSearchVo searchVo) {
		
		List<PackProductStockDate> srDates = new ArrayList<PackProductStockDate>();
		
		List<PackProductStockDate> srDatesExoprt = new ArrayList<PackProductStockDate>();
		List<PackProductStockDate> srDatesLocal = new ArrayList<PackProductStockDate>();
		List<PackProductStockDate> srDatesNca =null;
		
		List<Batch> getlikeBatch = BatchSearchUtil.getlikeBatch(searchVo.piNo);
		List<UUID> batchs=new ArrayList<UUID>();
		System.out.println(searchVo.piNo);
		for (Batch batch : getlikeBatch) {
			batchs.add(batch.id);
		}
		HashMap<String,PackProductStockDate> tempstocks=new HashMap<String,PackProductStockDate>();
		HashMap<String,PackProductStockDate> tempPIs=new HashMap<String,PackProductStockDate>();
		HashMap<String,PackProductStockDate> ncaStock=new HashMap<String,PackProductStockDate>();
		HashMap<String,PackProductStockDate> ncaPI=new HashMap<String,PackProductStockDate>();
		System.out.println("batchSize:"+batchs.size());
		//minusOneDay(
		List<Stock> stocks = Stock.find().where().in("batch.id", batchs)
		.disjunction().or(Expr.le("createdAt", searchVo.dateTimeTo),Expr.and(Expr.eq("deleted",true), Expr.ge("updatedAt",searchVo.dateTimeFrom)))
		.findList();
		//System.out.println("stock search END");
		for (Stock stock : stocks) {
			PackProductStockDate packProductStockDate = new PackProductStockDate();
			packProductStockDate.setStock(stock);
			boolean isAdd=false;
			List<StockTransaction> sts = StockTransaction.find().where()
					.between("transactionAt", searchVo.dateTimeFrom, searchVo.dateTimeTo)
					.eq("deleted", false)
					.eq("stock.id",stock.id)
					.order().desc("transactionAt")
					.findList();
			//System.out.println("transaction search end");
			if(sts.size()>0){
				for (StockTransaction stockTransaction : sts) {
						/*if(ncaStock.containsKey(packProductStockDate.StockId)){
							PackProductStockDate packProductStockDate2 = ncaStock.get(packProductStockDate.StockId);
							packProductStockDate2.inNacTransaction(stockTransaction);
						}else{
							PackProductStockDate packProductStockDateTemp = new PackProductStockDate();
							packProductStockDateTemp.setStock(stock);
							packProductStockDateTemp.inNacTransaction(stockTransaction);
							ncaStock.put(packProductStockDate.StockId, packProductStockDateTemp);
						}*/
						isAdd=true;
						if((!"T201".equals(stockTransaction.transactionCode))||((stock.deleted==false)||(stock.deleted==true&&stock.updatedAt!=null&&stock.updatedAt.after(searchVo.dateTimeTo))))
						packProductStockDate.inTransaction(stockTransaction);
				}
			}else{
				List<StockTransaction> trans = StockTransaction.find().where().le("transactionAt", searchVo.dateTimeFrom).eq("deleted", false)
						.eq("stock.id",stock.id)
						.order().desc("transactionAt")
						.findList();
				if(trans.size()>0){
					//System.out.println("Trans:"+trans.get(0).transactionAt);
						/*if(ncaStock.containsKey(packProductStockDate.StockId)){
							PackProductStockDate packProductStockDate2 = ncaStock.get(packProductStockDate.StockId);
							packProductStockDate2.initNacTransaction(trans.get(0));
						}else{
							PackProductStockDate packProductStockDateTemp = new PackProductStockDate();
							packProductStockDateTemp.setStock(stock);
							packProductStockDateTemp.initNacTransaction(trans.get(0));
							ncaStock.put(packProductStockDate.StockId, packProductStockDateTemp);
						}*/
						if(packProductStockDate.chechNewArea(trans.get(0))){
							packProductStockDate.initNacTransaction(trans.get(0));
						}else
							packProductStockDate.initTransaction(trans.get(0));
						isAdd=true;
				}
			}
			if(isAdd){
				tempstocks.put(packProductStockDate.StockId, packProductStockDate);
			}
		}
		/*List<StockTransaction> sts = StockTransaction.find().where()
				.in("stock.batch.id", batchs)
				.eq("deleted", false)
				.disjunction().or(Expr.le("stock.createdAt", searchVo.dateTimeTo),Expr.and(Expr.eq("deleted",true), Expr.ge("stock.updatedAt",searchVo.dateTimeFrom)))
				.findList();
		System.out.println("TransactionSize:"+sts.size());
		if (null!=sts && sts.size()>0) {
			srDates = new ArrayList<PackProductStockDate>(sts.size());
			for (StockTransaction st : sts) {
					PackProductStockDate packProductStockDate = new PackProductStockDate();
					packProductStockDate.inTransaction(st);
					if("Local".equals(searchVo.localOrExport)==packProductStockDate.isLoacl()){
						if(tempstocks.containsKey(packProductStockDate.StockId)){
							tempstocks.get(packProductStockDate.StockId).addPackProductStock(packProductStockDate);
						}else{
							tempstocks.put(packProductStockDate.StockId, packProductStockDate);
						}
					}
				}
			}*/
	/*	for(String key:ncaStock.keySet()){
			PackProductStockDate packProductStockDate = ncaStock.get(key);
			if(ncaPI.containsKey(packProductStockDate.pi)){
				ncaPI.get(packProductStockDate.pi).addPackProductPi(packProductStockDate);
			}else{
				ncaPI.put(packProductStockDate.pi, packProductStockDate);
			}
		}*/
		//System.out.println("ncastock:"+ncaStock.size());
		for (String key : tempstocks.keySet()) {
			PackProductStockDate packProductStockDate = tempstocks.get(key);
			if(tempPIs.containsKey(packProductStockDate.pi)){
				tempPIs.get(packProductStockDate.pi).addPackProductPi(packProductStockDate);
				if(packProductStockDate.isNca&&packProductStockDate.Nca==0){
					tempPIs.get(packProductStockDate.pi).Nca+=packProductStockDate.closing;
				}
			}else{
				tempPIs.put(packProductStockDate.pi, packProductStockDate);
				if(packProductStockDate.isNca&&packProductStockDate.Nca==0){
					tempPIs.get(packProductStockDate.pi).Nca+=packProductStockDate.closing;
				}
			}
		}
		//System.out.println(tempPIs.size());
		for (String pi : tempPIs.keySet()) {
			
			PackProductStockDate packProductStockDate = tempPIs.get(pi);
			if(packProductStockDate.Nca>0&&searchVo.isNca&&packProductStockDate.closing!=0){//NCA鏄惁杩囨护 add by cui
				try {
					ncaPI.put(packProductStockDate.pi, (PackProductStockDate) packProductStockDate.Clone());
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			packProductStockDate.Init();
			
			if(CheckUtil.exportCheckByPI(pi)){
				if(srDatesExoprt.size()==0) 
					tempPIs.get(pi).piType="Export";
				if(packProductStockDate.closing!=0){//add by cui
					srDatesExoprt.add(packProductStockDate);
				}
					
			}else{
				if(srDatesLocal.size()==0) 
					tempPIs.get(pi).piType="Local";
				if(packProductStockDate.closing!=0){//add by cui
					srDatesLocal.add(packProductStockDate);
				}
			}
		}
		
		if("Export".equals(searchVo.localOrExport)||StringUtils.isEmpty(searchVo.localOrExport)){
			srDates.addAll(srDatesExoprt);
		}
		if("Local".equals(searchVo.localOrExport)||StringUtils.isEmpty(searchVo.localOrExport)){
			srDates.addAll(srDatesLocal);
		}
		
		System.out.println("ncaPI:"+ncaPI.size());
		if(searchVo.isNca){//NCA鏄惁杩囨护 add by cui
			srDatesNca= new ArrayList<PackProductStockDate>(ncaPI.size());
			for(String pi:ncaPI.keySet()){
				if(srDatesNca.size()==0)
					ncaPI.get(pi).piType="NCA";
				PackProductStockDate packProductStockDate = ncaPI.get(pi);
				packProductStockDate.ncaInit();
				if(packProductStockDate.closing!=0){//add by cui
					if(CheckUtil.exportCheckByPI(pi))
						srDatesNca.add(packProductStockDate);
				}
			}
			for(String pi:ncaPI.keySet()){
				if(srDatesNca.size()==0)
					ncaPI.get(pi).piType="NCA";
				PackProductStockDate packProductStockDate = ncaPI.get(pi);
				packProductStockDate.ncaInit();
				if(packProductStockDate.closing!=0){//add by cui
					if(!CheckUtil.exportCheckByPI(pi))
						srDatesNca.add(packProductStockDate);
				}
			}
			srDates.addAll(srDatesNca);
		}

		
		/*ExpressionList<Execution> el = Execution.find().where().eq("deleted",false).between("executedAt",
				strToDate(dateToStrShort(packProductStockSearchVo.reportDate)+" 00:00:00",DATE_PATTERN),
				strToDate(dateToStrShort(packProductStockSearchVo.reportDate)+" 23:59:59",DATE_PATTERN));
		
		if("Local".equals(packProductStockSearchVo.localOrExport)){
			el.eq("planItem.order.internalOrderNo","planItem.order.contractNo");
		}else{
			el.ne("planItem.order.internalOrderNo","planItem.order.contractNo");
		}
		
		el.order().desc("executedAt");
		
		List<Execution> executionList = el.findList();
		
		if (null!=executionList && executionList.size()>0) {
			uniqueMp = new HashMap<String,Boolean>();
			srDates = new ArrayList<PackProductStockDate>(executionList.size());
			for (Execution execution : executionList) {	
				String uniqueKey = String.valueOf(execution.material.id)+execution.planItem.order.id;
				if(!uniqueMp.containsKey(uniqueKey)){
					uniqueMp.put(uniqueKey,Boolean.TRUE);
					PackProductStockDate sr = PackProductStockDate.getSR(execution);
					srDates.add(sr);
				}	
			}
		}else{
			if(packProductStockSearchVo.exportFlag){
				srDates.add(new PackProductStockDate());
			}
		}*/
		for (PackProductStockDate packProductStockDate : srDates) {
			packProductStockDate.Number();
		}
		Map<String, Object> map = new HashMap<String, Object>(1);
		map.put("srDates", srDates);
		return map;
	}
	
	/**
	 *  涓嬭浇妯℃澘
	 * @return
	 */
	static Form<PackProductStockSearchVo> VoForm = Form.form(PackProductStockSearchVo.class);
	public static Result download() {
		response().setHeader(Response.CONNECTION, "1000000");
		PackProductStockSearchVo obj = null;
		System.out.println("reprot start");
			if (VoForm.hasErrors()) {
				obj = new PackProductStockSearchVo();
			} else {
				obj = VoForm.bindFromRequest().get();
			}
			System.out.println(play.libs.Json.toJson(obj));
			if(obj.dateTimeFrom==null){
				obj.dateTimeFrom=new Date(0);
			}
			if(obj.dateTimeTo==null){
				obj.dateTimeTo=new Date();
			}
		//PackProductStockSearchVo trVo = new PackProductStockSearchVo();
//		Map<String, Object> listMap = listExecutedStuffingPlan(obj);
		//List<? extends ExcelObj> excelDates = new ArrayList<E extends ExcelObj>();
		List<PackProductStockDate> srDateList = cachedSrDateList;//(List<PackProductStockDate>) listMap.get("srDates");
		/*if (srDateList != null && !srDateList.isEmpty()) {
			excelDates=srDateList;
		}else{
			excelDates.add(e)(new PackProductStockDate());
		}*/
		
		ArrayList<PackProductStockReport> srs = new ArrayList<PackProductStockReport>();
		PackProductStockReport sr = createReport("F/MNA-WHSF-00-019", new Date(), StringUtils.isNotEmpty(obj.localOrExport)?obj.localOrExport:"All");
		sr.setDates(srDateList);
		srs.add(sr);
		
		File file = ReadExcel.outExcel(srs, sr.getIntProdNo() + ".xlsx");
		response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");// 鍙�鎷╀笉鍚岀被鍨�
		response().setHeader("Content-Disposition", "attachment; filename=" + "F_MNA_WHSF"+DateUtil.dateToExcelPattern(new Date()) + ".xlsx");
		return ok(file);
	}
	
	/*
	 * 鏍峰紡瑙勫垝
	 */
	public static PackProductStockReport createReport(String intProdNo, Date reportDate, String isloacle) {
		PackProductStockReport sr = new PackProductStockReport();
		sr.setIntProdNo(intProdNo);
		sr.setReportDate(reportDate);
		sr.setStyle(isloacle);
		sr.setSheetName("1");
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
		footList.chainAdd(cellf06);
		sr.setFoot(footList);
		sr.setHead(headList);
		return sr;
	}
	
	/**
	  * SR 鏄�StuffingRecapitulationDate鐨勭缉鍐�
	  * 浼犲叆PlanItem Execution 鐨勫疄渚嬶紝杈撳嚭StuffingRecapitulationDate鐨勫疄渚�
	  */
	/*@Deprecated
	public static PackProductStockDate getSR(Execution execution) {
		PackProductStockDate packDate = new PackProductStockDate();
		
		Material material = execution.material;
		
		packDate.commodity = material.materialName;//execution.planItem.material.materialName;
		
		MaterialUom mu2 = execution.planItem.materialUom;
		MaterialUom mu = MaterialUom.find().where().eq("material.id", execution.material.id).eq("baseUom",Boolean.TRUE).findUnique();
		packDate.kg = Double.valueOf((mu2.qtyOfBaseDenom.doubleValue() / mu2.qtyOfBaseNum.doubleValue()) / (mu.qtyOfBaseDenom.doubleValue() / mu.qtyOfBaseNum.doubleValue()));
		
		HashMap<String, String> ext = ExtUtil.unapply(execution.planItem.orderItem.order.ext);
		if (ext.get("destination") != null && !"".equals(ext.get("destination")))
			packDate.destination = ext.get("destination");
		
		packDate.openingStock = 0.0;//鏄ㄥぉ浠撳簱搴撳瓨
		
		packDate.in	= 0.0;
		packDate.ship = 0.0;
		List<SqlRow> lists  = Ebean.createSqlQuery("select t_e.material_id as materialId,sum(executed_qty) as quantity,t_e.execution_type as executionType"
				+" from t_execution t_e "
				+" inner join t_material t_m on t_m.id=t_e.material_id"
				+" inner join t_plan_item t_pi on t_pi.id=t_e.plan_item_id"
				+" inner join t_order t_o on t_o.id=t_pi.order_id"
				+" inner join t_material_uom t_mu on t_mu.id = t_e.to_uom_id"
				+" where t_e.material_id = '"+material.id+"' and t_pi.order_id='"+execution.planItem.orderItem.order.id+"'"
				+" group by t_e.material_id,t_e.execution_type ").findList();
		
		if(null!=lists&&lists.size()>0){
			for(SqlRow sqlRow:lists){
				if(PLAN_IN_TYPE.equals(sqlRow.getString("executionType"))){
					packDate.in = sqlRow.getDouble("quantity");
				}else if(PLAN_SHIP_TYPE.equals(sqlRow.getString("executionType"))){
					packDate.ship = sqlRow.getDouble("quantity");
				}
			}
		}
		
		//Sample	
		//Out To Lab	
		//Blend
		packDate.closing = packDate.openingStock+packDate.in-packDate.ship;
		packDate.tonnage = packDate.closing*packDate.kg/1000;
		
		List<TimingPolicy> timingPolicys = TimingPolicy.find().where().eq("material.id",material.id).findList();
		if(null!=timingPolicys&&timingPolicys.size()>0){
			TimingPolicy timingPolicy = timingPolicys.get(0);
			if(STORAGE_TYPE_AGEING.equals(timingPolicy.storageType.nameKey)){
				packDate.aging = timingPolicy.itemPolicy.orderItem.settlementQty.doubleValue();
			}else if(STORAGE_TYPE_TEMPERING.equals(timingPolicy.storageType.nameKey)){
				packDate.temp = timingPolicy.itemPolicy.orderItem.settlementQty.doubleValue();
			}
		}
		
		packDate.proDate = execution.planItem.plan.plannedTimestamp.toString();//鍙栬緝鏃╃殑
		
		packDate.pi = execution.planItem.orderItem.order.externalOrderNo;//PI 鍞竴
		packDate.refNo = execution.planItem.orderItem.order.contractNo;
		packDate.materialCode = execution.planItem.orderItem.material.materialCode;
		
		List<Batch> batchs = Batch.find().where().eq("material.id",material.id).orderBy("createdAt desc").findList();
		if(batchs.size()>0){
			packDate.productionCode = batchs.get(0).batchNo;
		}

		return null;
	}
	
	*//**
	 * 鑾峰彇stock product report
	 * *//*
	public static PackProductStockDate getPackReport(StockTransaction stockTransaction,
			PackProductStockSearchVo searchVo) {
		PackProductStockDate packDate = new PackProductStockDate();
		
		Stock stock = stockTransaction.stock;
		Material material = stock.material;
		String pi = ExtUtil.unapply(stock.batch.ext).get("pi");
		List<Order> orders = Order.find().where().eq("internalOrderNo",pi).findList();
		
		Order order = orders.get(0);
		packDate.commodity = material.materialName;//execution.planItem.material.materialName;
		MaterialUom mu2 = stock.materialUom;
		
		MaterialUom mu = MaterialUom.find().where().eq("material.id", material.id).eq("baseUom",Boolean.TRUE).findUnique();
		packDate.kg = Double.valueOf( (mu.qtyOfBaseDenom.doubleValue() / mu.qtyOfBaseNum.doubleValue())/(mu2.qtyOfBaseDenom.doubleValue() / mu2.qtyOfBaseNum.doubleValue()));
		
		List<OrderItem> orderItems = OrderItem.find().where().eq("order.id",order.id).findList();
		if(null!=orderItems&&orderItems.size()>0){
			OrderItem orderItem = orderItems.get(0);
			HashMap<String, String> ext = ExtUtil.unapply(orderItem.ext);
			if (ext.get("dest") != null && !"".equals(ext.get("dest")))
				packDate.destination = ext.get("dest");
		}
		
		
		packDate.openingStock = 0.0;//鏄ㄥぉ浠撳簱搴撳瓨
		List<SqlRow> yesterdayStocks  = Ebean.createSqlQuery("select t_sci.actual_qty as quantity from t_stock_count_item t_sci " +
				" where material_id= '"+material.id+"' and ext::hstore->'pi'='"+pi+"' "+
				" and (created_at between '"+searchVo.dateTimeFrom+
				"' and '"+searchVo.dateTimeTo+"' )").findList();
		if(null!=yesterdayStocks&&yesterdayStocks.size()>0){
			for(SqlRow sqlRow:yesterdayStocks){
				packDate.openingStock = sqlRow.getDouble("quantity");
			}
		}

		packDate.in	= 0.0;
		packDate.ship = 0.0;
		List<SqlRow> lists  = Ebean.createSqlQuery("select t_s.material_id as materialId,sum(new_qty) as quantity,t_st.transaction_code as executionType"
				+" from t_stock_transaction t_st "
				+" left join t_stock t_s on t_s.id=t_st.stock_id"
				+" left join t_batch t_b on t_b.id=t_s.batch_id"
				+" inner join t_material t_m on t_m.id=t_s.material_id"
				+" where t_s.material_id = '"+material.id+"' and t_b.ext::hstore->'pi'='"+pi+"'"
				+" group by t_s.material_id,t_st.transaction_code ").findList();
		
		if(null!=lists&&lists.size()>0){
			for(SqlRow sqlRow:lists){
				if(TRANSACTION_IN_TYPE.equals(sqlRow.getString("executionType"))){
					packDate.in = sqlRow.getDouble("quantity");
				}else if(TRANSACTION_SHIP_TYPE.equals(sqlRow.getString("executionType"))){
					packDate.ship = sqlRow.getDouble("quantity");
				}
			}
		}
		
		//Sample	
		//Out To Lab	
		//Blend
		
		packDate.closing = packDate.openingStock+packDate.in-packDate.ship;
		packDate.tonnage = packDate.closing*packDate.kg/1000;
		
		List<TimingPolicy> timingPolicys = TimingPolicy.find().where().eq("material.id",material.id).findList();
		if(null!=timingPolicys&&timingPolicys.size()>0){
			TimingPolicy timingPolicy = timingPolicys.get(0);
			if(STORAGE_TYPE_AGEING.equals(timingPolicy.storageType.nameKey)){
				packDate.aging = timingPolicy.itemPolicy.orderItem.settlementQty.doubleValue();
			}else if(STORAGE_TYPE_TEMPERING.equals(timingPolicy.storageType.nameKey)){
				packDate.temp = timingPolicy.itemPolicy.orderItem.settlementQty.doubleValue();
			}
		}
		
		packDate.pi = pi;
		packDate.refNo = order.internalOrderNo;
		packDate.materialCode = material.materialCode;
		
		List<Batch> batchs = Batch.find().where().eq("material.id",material.id).orderBy().desc("createdAt").findList();
		if(batchs.size()>0){
			packDate.productionCode = ExtUtil.unapply(batchs.get(0).ext).get("lot");//batchs.get(0).batchNo ext
			packDate.proDate = ExtUtil.unapply(batchs.get(0).ext).get("date");//鍙栬緝鏃╃殑(PI)
		}

		return packDate;
	}
*/
}
