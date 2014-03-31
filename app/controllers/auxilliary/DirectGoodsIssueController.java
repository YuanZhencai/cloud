package controllers.auxilliary;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import models.Batch;
import models.Stock;
import models.StockTransaction;
import models.Warehouse;
import models.vo.auxilliary.ExectionVo;
import models.vo.auxilliary.Request;
import models.vo.auxilliary.StockVo;
import models.vo.query.StockCountVo;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import play.mvc.Http.RequestBody;
import security.NoUserDeadboltHandler;
import utils.BatchSearchUtil;
import utils.DateUtil;
import utils.SessionSearchUtil;

@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class DirectGoodsIssueController extends Controller {
	//static String warehouseId = "fe640b63-5501-44b0-9fb1-6bbd93c5e8e5";
	static String countType="T002";
	static String NEW="S000";
	public static Result index() {
		return ok(views.html.Auxiliary.DirectGoodsIssue.render(""));
	}

	public static Result list() {
		System.out.println("=================list==================");
		RequestBody body = request().body();
		List<StockCountVo> StockCountVos;
			Request batchVo = new Request();
			if (body.asJson() != null)
				batchVo = Json.fromJson(body.asJson(), Request.class);
			StockCountVos = getStockCountByBatch(batchVo);
		//System.out.println(play.libs.Json.toJson(StockCountVos));
		return ok(play.libs.Json.toJson(StockCountVos));
	}

	public static List<StockCountVo> getStockCountByBatch(Request batchVo) {
		HashMap<String,StockCountVo> stockCountVos = new HashMap<String,StockCountVo>();
		List<Batch> searchBatch = BatchSearchUtil.serchlikeBatch(batchVo
				.getHashMap());
		for (Batch batch : searchBatch) {
			//
			batch = Batch.find().where().eq("id", batch.id)
					.eq("deleted", false).findUnique();
	/*		System.out.println(batchVo.piNo+":"+batchVo.lotNo);
			String pi = ExtUtil.unapply(batch.ext).get("pi");
			ExpressionList<Order> expressionList = Order.find().where().eq("warehouse", SessionSearchUtil.searchWarehouse()).eq("deleted", false);
		    if (batchVo.piNo != null && !batchVo.piNo.equals("")) {
		    	expressionList = expressionList.like("internalOrderNo", "%" + batchVo.piNo + "%");
	        }
	        if (batchVo.lotNo != null && !batchVo.lotNo.equals("")) {
	        	expressionList = expressionList.like("contractNo", "%" + batchVo.lotNo + "%");
	        }
	        List<Order> findList = expressionList.findList();
			System.out.println(findList.size());
			if(findList.size()<1) break;*/
//			StockCountVo stockCountVo = new StockCountVo();
//			stockCountVo.inBatch(batch);
			//System.out.println(play.libs.Json.toJson(batchVo));
			List<Stock> stocks = Stock.find().where().like("bin.nameKey",(batchVo.bin!=null&&!batchVo.bin.equals(""))?"%"+batchVo.bin+"%":"%%" ).like("area.nameKey", (batchVo.area!=null&&!batchVo.area.equals(""))?"%"+batchVo.area+"%":"%%").eq("deleted", false)
					.eq("batch", batch).findList();
			System.out.println("stocksize::::::::::::"+stocks.size());
			for (Stock stock : stocks) {
				if(stockCountVos.containsKey(batch.id.toString())){
					StockCountVo stockCountVo=stockCountVos.get(batch.id.toString());
					stockCountVo.addQty(stock.qty.doubleValue());
					//stockCountVo.inStock(stock);
				}else{
					StockCountVo stockCountVoTemp=new StockCountVo();
					stockCountVoTemp.inBatch(batch);
					stockCountVoTemp.addQty(stock.qty.doubleValue());
					stockCountVoTemp.inStock(stock);
					stockCountVoTemp.inBin(stock.bin);
					stockCountVos.put(batch.id.toString(),stockCountVoTemp);
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

	public static Result getStocks(){
		RequestBody body = request().body();
		Request request = new Request();
		if (body.asJson() != null)
			request = Json.fromJson(body.asJson(), Request.class);
		System.out.println("stock start");
		List<StockVo> searchStocks = searchStocks(request);
		return ok(play.libs.Json.toJson(searchStocks));
	}
	public static List<StockVo> searchStocks(Request request){
		List<StockVo> stockVos=new ArrayList<StockVo>();
		Batch batch = Batch.find().where().eq("deleted", false).eq("id", request.id).findUnique();
		List<Stock> stocks = Stock.find().where().like("bin.nameKey",(request.bin!=null&&!request.bin.equals(""))?"%"+request.bin+"%":"%%" ).like("area.nameKey", (request.area!=null&&!request.area.equals(""))?"%"+request.area+"%":"%%").eq("deleted", false)
				.eq("batch", batch).orderBy("createdAt").findList();
		for (Stock stock : stocks) {
			StockVo stockVo = new StockVo();
			stockVo.inStock(stock);
			stockVos.add(stockVo);
		}
		return stockVos;
	}
	public static Result getIssueType(){
		//CodeUtil.get
		return null;
	}
	public static Stock getStockById(String id){
		Stock stock = Stock.find().where().eq("id", id)
				.eq("warehouse.id", SessionSearchUtil.searchWarehouse().id.toString())
				.eq("area.deleted", false)
				.eq("bin.deleted",false)
				.eq("material.deleted", false)
				.eq("materialUom.deleted",false )
				.findUnique();
		return stock;
	}
	@Transactional
    @BodyParser.Of(value = BodyParser.Json.class, maxLength = 10 * 1024 * 1024)
	public static Result exection(){
		RequestBody body = request().body();
		ExectionVo exectionVo = new ExectionVo();
		if (body.asJson() != null){
			exectionVo = Json.fromJson(body.asJson(), ExectionVo.class);
			if(exectionVo.type==0){
				for (StockVo stockVo : exectionVo.stocks) {
					Stock stock = getStockById(stockVo.id);
					exectionVo.Qty=stock.qty.doubleValue();
					eachTransaction(stock, exectionVo);
				}
			}else{
				if(exectionVo.stocks!=null&&exectionVo.stocks.size()>0){
					StockVo stockVo = exectionVo.stocks.get(0);
					Stock stock = getStockById(stockVo.id);
					eachTransaction(stock, exectionVo);
				}
			}
		//System.out.println(play.libs.Json.toJson(exectionVo));
			return ok("Goods Issue Success");
		}else{
			return ok("Please Enter some Detail");
		}
	}
	public static void eachTransaction(Stock stock,ExectionVo executionVo){
	    StockTransaction stockTransaction = new StockTransaction();
        stockTransaction.warehouse=Warehouse.find().where().eq("deleted", false).eq("id", SessionSearchUtil.searchWarehouse().id.toString()).findUnique();
        stockTransaction.stock=stock;
        stockTransaction.transactionCode=executionVo.issueType;
        stockTransaction.oldUomId=stock.materialUom.id;
        stockTransaction.oldQty=stock.qty;
        stockTransaction.oldAreaId=stock.area.id;
        stockTransaction.oldBinId=stock.bin.id;
        stockTransaction.oldArrivedAt=stock.arrivedAt;
        stockTransaction.oldStatus=stock.stockStatus;
        stockTransaction.oldTracingId=stock.tracingId;
        stockTransaction.newQty=new BigDecimal(stock.qty.doubleValue()-executionVo.Qty);
        stockTransaction.transactionAt=DateUtil.currentTimestamp();
        stockTransaction.createdAt=DateUtil.currentTimestamp();
        stockTransaction.createdBy=SessionSearchUtil.searchUser().id;
        if(executionVo.remarks!=null)
        stockTransaction.remarks=executionVo.remarks;
        stockTransaction.save();
            if(stockTransaction.newQty.doubleValue()==0){
                stock.deleted=true;
                stock.issuedAt=DateUtil.currentTimestamp();
                stock.stockStatus="";
                stock.updatedAt=DateUtil.currentTimestamp();
                stock.updatedBy=SessionSearchUtil.searchUser().id;
                stock.update();
            }else{
                stock.qty=stockTransaction.newQty;
                stock.updatedAt=DateUtil.currentTimestamp();
                stock.updatedBy=SessionSearchUtil.searchUser().id;
                stock.update();
            }
	}
}
