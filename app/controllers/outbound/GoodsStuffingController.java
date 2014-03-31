package controllers.outbound;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import models.Batch;
import models.Execution;
import models.OrderItem;
import models.PlanItem;
import models.PlanItemDetail;
import models.Stock;
import models.StockTransaction;
import models.Warehouse;
import models.vo.Result.ResultVo;
import models.vo.outbound.GetStocksVo;
import models.vo.outbound.GoodsStuffingVo;
import models.vo.outbound.SelectBatch;
import models.vo.outbound.searchVo;
import models.vo.outbound.stockVo;
import models.vo.outbound.stuffingDetial;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
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
import utils.BatchSearchUtil;
import utils.CheckUtil;
import utils.DateUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import utils.UnitConversion;
import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.ExpressionList;

/**
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: GoodsStuffingController.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */
@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class GoodsStuffingController extends Controller {
	
	private static final Logger logger = LoggerFactory.getLogger(GoodsStuffingController.class);
	
    //public static Form<searchVo> goodsStuffingForm = form(searchVo.class);
    //public static List<GoodsStuffingVo> goodsStuffingVos = null;
    //public static List<stuffingDetial> stuffingDetials = null;
    //static String  SessionSearchUtil.searchWarehouse().id.toString() = "fe640b63-5501-44b0-9fb1-6bbd93c5e8e5";
   // static String SourceTypeOfCargo="T003";
    static String orderTypeOfCargo = "T004";
    static String orderSourceTypeOfCargo = "T003";
    static String orderSuperTypeOfCargo = "T003.999";
    static String NEW="S000";
    static String CONFIRMED="S001";
    static String EXECUTE="S002";
    static String COMPLETE="S003";
    static String CANCEL="S999";
    static String CargoTranactionCode="T301";
    static String CargoExecutionType="T003";
    static String CargoExecutionSuperType="T003.999";
    /*
     * 载入页面
     */
    public static Result index() {
        return ok(views.html.outbound.goodsStuffing.render(""));
    }
    /*
     * 根据查询条件，查询数据
     */
    public static Result list() {
    	
        List<GoodsStuffingVo> goodsStuffingVos=new ArrayList<GoodsStuffingVo>();
         searchVo vo=new searchVo();
         RequestBody body = request().body();
         ExpressionList<PlanItem> searchPlanItem = PlanItem.find().fetch("orderItem")
        		 .fetch("material").fetch("fromMaterialUom")
        		 .fetch("toMaterialUom").where()
        		 .eq("orderItem.deleted",false).eq("material.deleted",false)
        		 .eq("fromMaterialUom.deleted",false).eq("toMaterialUom.deleted",false)
        		 .eq("planType", orderSourceTypeOfCargo).eq("deleted",false)
        		 .eq("order.warehouse.id",  SessionSearchUtil.searchWarehouse().id.toString())
        		 .eq("order.deleted", false).eq("orderItem.deleted",false);
        
//         StringBuffer sb = new StringBuffer("select t_o.internal_order_no," +
//         		"t_pi.ext::hstore->'containerNo',t_pi.id,t_pi.plan_type," +
//         		"t_pi.ext::hstore->'lot' as batchNo|| from t_plan_item t_pi ");
         
         if(body.asJson()!=null){
        	 vo = Json.fromJson(body.asJson(), searchVo.class);
        	 
        	 if (vo.fromDate != null && vo.toDate != null) {
       			if (vo.fromDate.after(vo.toDate)) {
       				return ok(play.libs.Json.toJson(new ResultVo("error","fromDate can not be greater than toDate,Please correct!")));
       			}
       		 }
        	 
//        	 sb.append(" inner join  t_order 		t_o 	on t_o.id 	= t_pi.order_id");
//             sb.append(" inner join  t_warehouse 	t_w 	on t_w.id 	= t_o.warehouse_id");
//             sb.append(" inner join  t_order_item 	t_oi 	on t_oi.id 	= t_pi.order_item_id ");
//             sb.append(" inner join  t_plan 		t_p 	on t_p.id 	= t_pi.plan_id ");
//             sb.append(" inner join  t_material 	t_m 	on t_m.id 	= t_pi.material_id ");
//             sb.append(" inner join  t_material_uom t_fmu 	on t_fmu.id = t_pi.from_uom_id ");
//             sb.append(" inner join  t_material_uom t_tmu 	on t_tmu.id = t_pi.to_uom_id ");
//             sb.append(" where 1=1 and t_oi.deleted=false and t_m.deleted=false and t_fmu.deleted=false");
//             sb.append(" and t_tmu.deleted=false and t_pi.play_type='"+orderSourceTypeOfCargo+"'");
//             sb.append(" and t_pi.deleted=false and t_w.id='"+SessionSearchUtil.searchWarehouse().id+"'");
//             sb.append(" and t_o.deleted=false and t_oi.deleted=false ");
        	 
             if(StringUtils.isNotBlank(vo.piNo)){
                 searchPlanItem= searchPlanItem.like("plan.order.internalOrderNo","%"+vo.piNo+"%");
//            	 sb.append(" and t_o.internal_order_no like '%"+vo.piNo+"%'");
             }
//             if(vo.piStatus!=null&&!vo.piStatus.endsWith("")){
//                 searchPlanItem= searchPlanItem.like("plan.order.orderStatus","%"+vo.piStatus+"%");
            	 
//             }
             
         }
         
//         sb.append(" and t_pi.planned_execution_at (between '"+DateUtil.strToDate((vo.fromDate==null||vo.fromDate.equals(""))?"2000-1-1 00:00:00":DateUtil.dateToStrShort(vo.fromDate)+" 00:00:00","yyyy-MM-dd HH:mm:ss")+"' and '"+DateUtil.strToDate((vo.toDate==null||vo.toDate.equals(""))?"2100-1-1 23:59:59":DateUtil.dateToStrShort(vo.toDate)+" 23:59:59","yyyy-MM-dd HH:mm:ss")+"')");
//        
//         sb.append(" group by t_o.internal_order_no,t_pi.ext::hstore->'containerNo' ");
         
        //List<PlanItem> list = PlanItem.find().where().eq("planType", "T003").findList();
        //List<PlanItem> list = PlanItem.find().fetch("batch").fetch("material").fetch("materialUom").where().like("plan.order.externalOrderNo",vo.piNo==null?"%":"%"+vo.piNo+"%").like("plan.order.contractNo", vo.sgPiNo==null?"%":"%"+vo.sgPiNo+"%").like("plan.order.orderStatus",vo.piStatus==null?"%":"%"+vo.piStatus+"%").between("plannedExecutionAt",DateUtil.StringToDate(vo.fromDate==null?"2000-1-1 00:00:00":vo.fromDate+" 00:00:00"), DateUtil.StringToDate(vo.toDate==null?"2100-1-1 23:59:59":vo.toDate+" 23:59:59")).eq("planType", orderSourceTypeOfCargo).eq("plan.deleted",false).eq("order.warehouse.id",  SessionSearchUtil.searchWarehouse().id.toString()).findList();
         /*.El("plan.planStatus", "S002")*/

         List<PlanItem> list = searchPlanItem
                 .between("plannedExecutionAt",DateUtil.strToDate((vo.fromDate==null||vo.fromDate.equals(""))?"2000-1-1 00:00:00":DateUtil.dateToStrShort(vo.fromDate)+" 00:00:00","yyyy-MM-dd HH:mm:ss"), DateUtil.strToDate((vo.toDate==null||vo.toDate.equals(""))?"2100-1-1 23:59:59":DateUtil.dateToStrShort(vo.toDate)+" 23:59:59","yyyy-MM-dd HH:mm:ss"))
                 .findList();
        /* Map<String,String> mp = new HashMap<String,String>(); 
         Map<String,GoodsStuffingVo> groupMp = new HashMap<String,GoodsStuffingVo>();*/
         System.out.println("planItem.size:"+list.size());
         for (PlanItem planItem : list) {
        	if(CheckUtil.TransfCanCreateCheck(planItem)){
	            GoodsStuffingVo goodsStuffingVo = new GoodsStuffingVo();
	            goodsStuffingVo.inplanItem(planItem);
	            boolean isContainer=true;
	            if(vo.sgPiNo!=null&&!vo.sgPiNo.equals("")){
	                int indexOf = goodsStuffingVo.containerNo.indexOf(vo.sgPiNo);
//	            	 sb.append(" and t_o.contract_no like '%"+vo.sgPiNo+"%'");
	                if(indexOf<0){
	                	isContainer=false;
	                }
	             }
	         /*   String key = goodsStuffingVo.piNo+"-"+goodsStuffingVo.batchNo;
	            String groupKey = goodsStuffingVo.piNo+"-"+goodsStuffingVo.containerNo;
	            if(!mp.containsKey(key)){
	            	List<Batch> batches = BatchSearchUtil.getBatch(goodsStuffingVo.piNo,goodsStuffingVo.batchNo);
	            	List<Stock> stocks = Stock.find().fetch("warehouse").fetch("area").fetch("bin")
	    	        		.where().eq("warehouse.deleted",false).eq("area.deleted",false)
	    	        		.eq("bin.deleted",false).in("batch",batches).eq("warehouse.deleted",false)
	    	        		.eq("area.deleted", false).eq("bin.deleted", false).eq("batch.deleted", false)
	    	        		.eq("area.storageType.nameKey","Loading Bay").eq("deleted", false).findList();
	            	System.out.println("stock.seize:"+stocks.size());
	            	mp.put(key,stocks.size()>0?"Y":"N");
	            }
	            goodsStuffingVo.loadingBayStatus = mp.get(key);*/
	            boolean isSame=false;
	            for (GoodsStuffingVo TempgoodsStuffingVo : goodsStuffingVos) {
					if(TempgoodsStuffingVo.addStuffingVo(goodsStuffingVo))
						isSame=true;
						
				}
	            if(!isSame&&isContainer){
	            	goodsStuffingVos.add(goodsStuffingVo);
	            	//goodsStuffingVos.add(goodsStuffingVo);
	            }/*else{
	            	GoodsStuffingVo tmpGoodsStuffingVo = groupMp.get(groupKey);
	            	tmpGoodsStuffingVo.addPlanItem(planItem);
	            	//tmpGoodsStuffingVo.batchNo+=("<br/>"+goodsStuffingVo.batchNo);
//	            	tmpGoodsStuffingVo.stuffingQuantity+="<br/>"+goodsStuffingVo.stuffingQuantity;
//	            	tmpGoodsStuffingVo.remarks
	            	int index = 0;
	            	for(int i=0,len=goodsStuffingVos.size();i<len;i++){
	            		GoodsStuffingVo stuffingVo = goodsStuffingVos.get(i);
	            		if((stuffingVo.piNo).equals(goodsStuffingVo.piNo)&&(stuffingVo.containerNo).equals(goodsStuffingVo.containerNo)){
	            			index = i;
	            			break;
	            		}
	            	}
	            	goodsStuffingVos.set(index, tmpGoodsStuffingVo);
	            }*/
	            
//	            goodsStuffingVo.stuffingQuantity=/*returnComparing(planItem.toMaterialUom.uomCode, planItem.fromMaterialUom.uomCode, planItem.material.id.toString())**/(planItem.palnnedQty.doubleValue());
        	}
         }
         return ok(play.libs.Json.toJson(new ResultVo(goodsStuffingVos)));
    }
    /*
     * 根据PlanItem.id查询已经EXECUTION数据
     */
    public static Result exectionDatial(){
    	JsonNode body = request().body().asJson();
    	if(body!=null){
    		GetStocksVo vo = Json.fromJson(body, GetStocksVo.class);
    		List<stuffingDetial> stuffingDetials=new ArrayList<stuffingDetial>();
    		for(String key :vo.batchs.keySet())	{
    			PlanItem planItem = PlanItem.find().where()
    					.eq("deleted",false)
    					.eq("plan.deleted",false)
    					.eq("planType", orderSourceTypeOfCargo)
    					.eq("order.warehouse.id",  SessionSearchUtil.searchWarehouse().id.toString())
    					.eq("order.deleted", false)
    					.eq("id", vo.batchs.get(key).id)
    					.findUnique();
    			if(planItem!=null) {
    				/*if(planItem.plan.planStatus.equals(COMPLETE)){
    					List<StockTransaction> stockTransactions = StockTransaction.find().where().eq("deleted", false).eq("execution.deleted",false).eq("execution.planItem", planItem).findList();
    					for (StockTransaction stockTransaction : stockTransactions) {
    						stuffingDetial stuffingDetial = new stuffingDetial();
    						stuffingDetial.inTransaction(stockTransaction);
    						stuffingDetials.add(stuffingDetial);
    					}
    				}*/
    				List<PlanItemDetail> planItemDetails = PlanItemDetail.find().where()
    				.eq("deleted", false)
    				.eq("planItem.id", planItem.id)
//    				.order().asc("stock.batch.ext－>'lot'").order().asc("stock.ext->'stockNo'")
    				.findList();
    				
    				/*String sql = "select t_pid.id from t_plan_item_detail t_pid "
    	    				+" inner join t_stock t_s on t_pid.stock_id=t_s.id "
    	    				+" inner join t_batch t_b on t_b.id = t_s.batch_id "
    	    				+" inner join t_plan_item t_pi on t_pi.id = t_pid.plan_item_id"
    	    				+" where 1=1 and t_pid.deleted=false "
    	    				+" and t_pi.id='"+planItem.id+"'"
    	    				+" order by t_b.ext::hstore->'lot' asc,t_s.ext::hstore->'stockNo' asc";
    				
    				RawSql rawSql = RawSqlBuilder.parse(sql).create();
    				Query<PlanItemDetail> query = Ebean.find(PlanItemDetail.class);
    				List<PlanItemDetail> planItemIds = query.setRawSql(rawSql).findList();*/
    				
    				
    				for (PlanItemDetail planItemDetail: planItemDetails) {//排序
//    					planItemDetail = PlanItemDetail.find().byId(String.valueOf(planItemDetail.id));
    					stuffingDetial stuffingDetial = new stuffingDetial();
						stuffingDetial.inPlanItemDetial(planItemDetail);
						List<Execution> findList = Execution.find().where().eq("deleted", false).eq("planItemDetail.id", planItemDetail.id).findList();
						if(findList.size()>0)
							stuffingDetial.isExecuted=true;
						stuffingDetials.add(stuffingDetial);
					}
    			}
    		}
    		return ok(play.libs.Json.toJson(new ResultVo(sortList(stuffingDetials))));
    	}else{
    		return ok(play.libs.Json.toJson(new ResultVo("error","can't find Data!")));
    	}
    }
    
    
	private static final Pattern pd = Pattern.compile("\\d+");
	private static final Pattern pw = Pattern.compile("\\D+");

	public static <T extends stuffingDetial> List<T> sortList(List<T> ts){
		List<T> afterSortList = new ArrayList<T>();
		if(null!=ts&&ts.size()>0){
			while(ts.size()>0){
				int minIndex = 0;
				int minPalletNo = changeStrToInt(ts.get(0).batchNo+ts.get(0).No);
				System.out.println(ts.get(0).batchNo+ts.get(0).No);
				for (int i = 0; i < ts.size(); i++) {
					int palletNo = changeStrToInt(ts.get(i).batchNo+ts.get(i).No);
					System.out.println(palletNo +">>><<<"+ minPalletNo);
					if (palletNo < minPalletNo) {
						minPalletNo = palletNo;
						minIndex = i;
					}
				}
				afterSortList.add(ts.get(minIndex));
				ts.remove(minIndex);
			}
		}
		return	afterSortList;
	}

	/**
	 * 把字符串转换为数字
	 * @param str
	 * @return
	 */
	private static int changeStrToInt(String str) {
		int number = 0;
		String[] ints = pw.split(str);
		String[] chars = pd.split(str);
		if (chars != null && chars.length > 0) {
			for (int i = 0; i < chars.length; i++) {
				if(StringUtils.isNotEmpty(chars[i]))
				number += number + chars[i].charAt(0);
			}
			number=number*number;
		}
		if (ints != null&&ints.length>0) {
			for (int i = 0; i < ints.length; i++) {
				if(StringUtils.isNotEmpty(ints[i]))
				number = number + Integer.parseInt(ints[i]);
			}
		}
		return number;
	}
    
	public static Result out(){
		JsonNode body = request().body().asJson();
    	if(body!=null){
    		GoodsStuffingVo vo = Json.fromJson(body, GoodsStuffingVo.class);
    		String result = vo.setPlanOut();
    		ResultVo resultVo = new ResultVo("success",result);
    		resultVo.Data=vo;
    		return ok(play.libs.Json.toJson(resultVo));
    	}else{
    		return ok(play.libs.Json.toJson(new ResultVo("error","Can't find Data")));
    	}
	}
    
    
    /*
     * 查询符合PlanItem的Stocks
     */
    public static Result detailsList(){
    	JsonNode body = request().body().asJson();
    	if(body!=null){
    		GetStocksVo vo = Json.fromJson(body, GetStocksVo.class);
    		 List<stuffingDetial> stuffingDetials=new ArrayList<stuffingDetial>();
    		for(String key :vo.batchs.keySet())	{
    		List<PlanItem> planItems = PlanItem.find().where().eq("deleted",false).eq("planType", orderSourceTypeOfCargo)
        		.eq("order.warehouse.id",  SessionSearchUtil.searchWarehouse().id.toString())
        		.eq("plan.deleted",false).eq("order.deleted", false).eq("id", vo.batchs.get(key).id).findList();
    		System.out.println("planItem.size:"+planItems.size());
    		if(planItems.size()>0) {
    			PlanItem planItem = planItems.get(0);
    			HashMap<String, String> map = ExtUtil.unapply(planItem.ext);
    			List<Batch> batchs = BatchSearchUtil.getBatch(vo.piNo, vo.batchs.get(key).batch);
	        
    			Map<String,String> mp = new HashMap<String,String>();
    			mp.put("pi",vo.piNo);
    			//List<SelectBatch> batchVos = getBatchList(mp);
	        
    			List<Stock> stocks = Stock.find().fetch("warehouse").fetch("area").fetch("bin")
	        		.where().eq("warehouse.deleted",false).eq("area.deleted",false)
	        		.eq("bin.deleted",false).in("batch",batchs).eq("warehouse.deleted",false)
	        		.eq("area.deleted", false).eq("bin.deleted", false).eq("deleted", false).eq("batch.deleted", false)
	        		.eq("area.storageType.nameKey","Loading Bay").findList();
	        
    			for (Stock stock : stocks) {
    				stuffingDetial stuffingDetial = new stuffingDetial();
    				stuffingDetial.planItemId=vo.batchs.get(key).id;
    				stuffingDetial.setExt(planItem.ext);
    				stuffingDetial.inStock(stock);
    				//stuffingDetial.batchNoList = batchVos;
               // stuffingDetial.palletNoList = getStockList(stuffingDetial.piNo,stuffingDetial.batchNo);
    				stuffingDetials.add(stuffingDetial);
    			}
    		}
        }
    		System.out.println("stuffingDetials.size:"+stuffingDetials.size());
    		return ok(play.libs.Json.toJson(new ResultVo(sortList(stuffingDetials))));
    		
    	}else{
    		return ok(play.libs.Json.toJson(new ResultVo("error","can't find Data!")));
    	}
    }
    
    /*
     * 查询符合当前PI,BatchNo的Stock集合
     * 
     */
    public static Result findStockList(){
    	JsonNode body = request().body().asJson();
    	//HashMap<String,stuffingDetial> stockNoList = new HashMap<String,stuffingDetial>();
    	List<SelectBatch> selectBatchs=new ArrayList<SelectBatch>();
        if(body!=null){
        	GetStocksVo vo = Json.fromJson(body, GetStocksVo.class);
        	for(String key :vo.batchs.keySet()){
        		SelectBatch stockList = getStockList(vo.piNo, vo.batchs.get(key).batch);
        		if(stockList!=null)
        		selectBatchs.add(stockList) ;
        	}
        }
       
        return ok(play.libs.Json.toJson(new ResultVo(selectBatchs)));
    }
    
    private static SelectBatch getStockList(String piNo,String batchNo){
    	
    	//List<SelectBatch> batchVos = new ArrayList<SelectBatch>();
    	SelectBatch bv =null;
    	bv=new SelectBatch();
		bv.batchNo=batchNo;
//    	List<PlanItem> planItems = PlanItem.find().where().eq("deleted",false).eq("planType", orderSourceTypeOfCargo)
//        		.eq("order.warehouse.id",  SessionSearchUtil.searchWarehouse().id.toString())
//        		.eq("plan.deleted",false).eq("order.deleted", false).eq("id", id).findList();
    	List<Batch> batchs = BatchSearchUtil.getBatch(piNo,batchNo);
    	for (Batch batch2 : batchs) {
    		List<Stock> stocks = Stock.find()
        		.where().eq("warehouse.deleted",false).eq("area.deleted",false)
        		.eq("bin.deleted",false).eq("batch.id",batch2.id.toString()).eq("warehouse.deleted",false)
        		.eq("area.deleted", false).eq("area.storageType.nameKey","Loading Bay").eq("bin.deleted", false).eq("batch.deleted", false)
        		.eq("deleted", false).findList();
    		System.out.println("stock.size"+stocks.size());
    		for(Stock stock:stocks){
    			stockVo stockVo = new stockVo(stock);
    			if(bv.stockNos==null){
    				bv.stockNos=new ArrayList<stockVo>();
    			}
    			bv.stockNos.add(stockVo);
    		}
    	}
    	return bv;
    }
    
    /**
     * 获取PI获取loading bay的batch列表
     * */
    public static Result findBatchList(){
    	System.out.println("start");
    	JsonNode body = request().body().asJson();
    	List<SelectBatch> stockNoList = null;
        if(body!=null){
        	Map<String,String> mp = Json.fromJson(body, HashMap.class);
        	System.out.println(play.libs.Json.toJson(mp));
        	stockNoList = getBatchList(mp);
        }
        System.out.println("end");
        System.out.println(play.libs.Json.toJson(stockNoList));
        return ok(play.libs.Json.toJson(new ResultVo(stockNoList)));
    }
    
    /**
     * 根据参数值，获取batch no列表
     * */
    private static List<SelectBatch> getBatchList(Map<String,String> mp){
    	
    	List<SelectBatch> batchVos = new ArrayList<SelectBatch>();
        
    	List<Batch> batches = BatchSearchUtil.getBatch(mp.get("pi"));
    	for(Batch batch:batches){
    		SelectBatch bv = new SelectBatch();
    		bv.batchNo=batch.batchNo;
    	List<Stock> stocks = Stock.find().fetch("warehouse").fetch("area").fetch("bin")
        		.where().eq("warehouse.deleted",false).eq("area.deleted",false)
        		.eq("bin.deleted",false).eq("batch.id",batch.id.toString()).eq("warehouse.deleted",false)
        		.eq("area.deleted", false).eq("bin.deleted", false).eq("batch.deleted", false)
        		.eq("area.storageType.nameKey","Loading Bay").eq("deleted", false).findList();
    	
        	for(Stock stock:stocks){
        		//SelectBatch bv = new SelectBatch(batch.batchNo,stock);
        		/*System.out.println(play.libs.Json.toJson(bv));
        		boolean isSame=false;
        		for (SelectBatch selectBatch : batchVos) {
        			if(selectBatch.isSame(bv))
        				isSame=true;
				}
        		if(!isSame){
        			batchVos.add(bv);
        		}*/
        		stockVo stockVo = new stockVo(stock);
        		if(bv.stockNos==null){
        			bv.stockNos=new ArrayList<stockVo>();
        		}
        		bv.stockNos.add(stockVo);
        	}
        	batchVos.add(bv);
    	}
        return batchVos;
    }
    
    @Transactional
    @BodyParser.Of(value = BodyParser.Json.class, maxLength = 10 * 1024 * 1024)
    public static Result SaveStuffing(){
    	System.out.println("save");
    	JsonNode body = request().body().asJson();
    	ResultVo resultVo = new ResultVo("success", "save success");
          if(body!=null){
              List<stuffingDetial> stuffingDetials = null;
              try {
                  stuffingDetials = new ObjectMapper().readValue(body, new TypeReference<List<stuffingDetial>>() { });
              } catch (Exception e) {
                  return ok(play.libs.Json.toJson(new ResultVo("error","data error")));
              }
              System.out.println("size"+stuffingDetials.size());
          if(stuffingDetials!=null&&stuffingDetials.size()>0){
        	  for (stuffingDetial stuffingDetial : stuffingDetials) {
        		  PlanItemDetail outPlanItemDetial = stuffingDetial.OutPlanItemDetial();
        		  stuffingDetial.inPlanItemDetial(outPlanItemDetial);
			}
          
          }
          resultVo.Data=stuffingDetials;
        }
    	return ok(play.libs.Json.toJson(resultVo));
    }
    
    public static Result Simplesave(){
        JsonNode body = request().body().asJson();
        if(body!=null){
            stuffingDetial stuffingDetial = null;
            try {
            	stuffingDetial=	Json.fromJson(body, stuffingDetial.class);
            } catch (Exception e) {
                return ok(play.libs.Json.toJson(new ResultVo("error","data error")));
            }
           // double sum=0;
            
        /*    List<PlanItem> planItems = PlanItem.find().where().eq("fromMaterialUom.deleted",false).eq("planType", orderSourceTypeOfCargo).eq("order.warehouse.id",  SessionSearchUtil.searchWarehouse().id.toString()).eq("toMaterialUom.deleted",false).eq("material.deleted",false).eq("plan.planStatus", "S003").eq("id",stuffingDetial.planItemId).eq("deleted", false).findList();
            if(planItems.size()>0){
            	for (PlanItem planItem : planItems) {
					Execution execution = Execution.find().where().eq("deleted", false).eq("planItem.id", planItem.id).findUnique();
					if(execution!=null)
					sum+=execution.executedQty.doubleValue();
				}
                PlanItem planItem=planItems.get(0);
                double percent=sum*UnitConversion.returnComparing(planItem.fromMaterialUom.uomCode,planItem.toMaterialUom.uomCode,planItem.material.id.toString())/planItem.orderItem.settlementQty.doubleValue();
                    if(planItem.minPercent!=null&&planItem.maxPercent!=null&&(planItem.minPercent.doubleValue()<=percent)&&(planItem.maxPercent.doubleValue()>=percent)){
                        return ok(play.libs.Json.toJson(new ResultVo("error","Split Failed,Qty exceed!")));
                    }
            	}*/
                Stock stock = Stock.find().where().eq("id", stuffingDetial.stockId).eq("deleted", false).eq("area.deleted", false).eq("bin.deleted", false).findUnique();
                try{
                	eachSave(stuffingDetial.planItemId,stock,stuffingDetial);
                }catch(Exception e){
                	e.printStackTrace();
                	return ok(play.libs.Json.toJson(new ResultVo("error",e.getMessage())));
                }
                ResultVo resultVo = new ResultVo("success","Save Success");
                resultVo.Data=stuffingDetial;
            return ok(play.libs.Json.toJson(resultVo));
        }
        return ok(play.libs.Json.toJson(new ResultVo("error","Can not Find Data")));
    }
    /*
     * execution 保存到execution表中
     */
    @Transactional
    @BodyParser.Of(value = BodyParser.Json.class, maxLength = 10 * 1024 * 1024)
    public static Result save(){
        JsonNode body = request().body().asJson();
        if(body!=null){
            List<stuffingDetial> stuffingDetials = null;
            try {
                stuffingDetials = new ObjectMapper().readValue(body, new TypeReference<List<stuffingDetial>>() { });
            } catch (Exception e) {
                return ok(play.libs.Json.toJson(new ResultVo("error","data error")));
            }
            if(stuffingDetials.size()<1) return ok(play.libs.Json.toJson(new ResultVo("error","No Data Need Save")));
            double sum=0;
            String Result=null;
            HashMap<String,stuffingDetial> StockIds=new HashMap<String,stuffingDetial>();
            for (stuffingDetial stuffingDetial : stuffingDetials) {
            	if(StockIds.containsKey(stuffingDetial.stockId)){
            		if(Result==null){
            			Result=",But Stock : "+stuffingDetial.No;
            		}else{
            			Result+= ","+stuffingDetial.No ;
            		}
            		//stuffingDetials.remove(stuffingDetial);
            	}else{
            		stuffingDetial.isExecuted=true;
            		StockIds.put(stuffingDetial.stockId,stuffingDetial);
            	}
                sum=sum+stuffingDetial.stuffingQuantity;
            }
            if(Result!=null)
            	Result+="been selected many times!";
            List<PlanItem> planItems = PlanItem.find().where().eq("fromMaterialUom.deleted",false).eq("planType", orderSourceTypeOfCargo).eq("order.warehouse.id",  SessionSearchUtil.searchWarehouse().id.toString()).eq("toMaterialUom.deleted",false).eq("material.deleted",false).eq("plan.planStatus", "S003").eq("id",stuffingDetials.get(0).planItemId).eq("deleted", false).findList();
            if(planItems.size()>0){
            	for (PlanItem planItem : planItems) {
					List<Execution> executions = Execution.find().where().eq("deleted", false).eq("planItem.id", planItem.id).findList();
					for (Execution execution : executions) {
						if(execution.executedQty!=null)
						sum+=execution.executedQty.doubleValue();
					}
				}
                PlanItem planItem=planItems.get(0);
                double percent=sum*UnitConversion.returnComparing(planItem.fromMaterialUom.uomCode,planItem.toMaterialUom.uomCode,planItem.material.id.toString())/planItem.orderItem.settlementQty.doubleValue();
                    if(planItem.minPercent!=null&&planItem.maxPercent!=null&&(planItem.minPercent.doubleValue()<=percent)&&(planItem.maxPercent.doubleValue()>=percent)){
                        return ok(play.libs.Json.toJson(new ResultVo("error","Split Failed,Qty exceed!")));
                    }
            }
            for(String key :StockIds.keySet()){
                Stock stock = Stock.find().fetch("area").fetch("bin").where().eq("id", StockIds.get(key).stockId).eq("deleted", false).eq("area.deleted", false).eq("bin.deleted", false).findUnique();
                try{
                	eachSave(StockIds.get(key).planItemId,stock,StockIds.get(key));
                }catch(Exception e){
                	e.printStackTrace();
                	return ok(play.libs.Json.toJson(new ResultVo("error","StockNo:"+StockIds.get(key).No+","+e.getMessage())));
                }
            }
            ResultVo resultVo = new ResultVo("success","Save Success"+(Result!=null?Result:""));
            resultVo.Data=stuffingDetials;
            return ok(play.libs.Json.toJson(resultVo));
        }
        return ok(play.libs.Json.toJson(new ResultVo("error","Can not Find Data")));
    }
    /*
     * stock状态修改，并且保存到Stocktransatcion
     */
    @Transactional
    public static void eachSave(String id,Stock stock,stuffingDetial stuffingDetial) throws Exception{
        PlanItem planItem = PlanItem.find().fetch("plan").fetch("order").fetch("material").fetch("fromMaterialUom").where().eq("plan.deleted",false).eq("order.deleted",false).eq("planType", orderSourceTypeOfCargo).eq("order.warehouse.id",  SessionSearchUtil.searchWarehouse().id.toString()).eq("id", id).eq("deleted", false).eq("material.deleted", false).eq("fromMaterialUom.deleted",false).findUnique();
        if(planItem==null) throw new Exception("Data Error");
        else{
           if( isComplete(planItem))
               planItem.order.orderStatus=COMPLETE;
           else
               planItem.order.orderStatus=EXECUTE;  
        planItem.order.updatedAt=DateUtil.currentTimestamp();
        planItem.order.updatedBy=SessionSearchUtil.searchUser().id;
        planItem.order.update();
        planItem.plan.planStatus=COMPLETE;
        planItem.plan.updatedAt=DateUtil.currentTimestamp();
        planItem.plan.updatedBy=SessionSearchUtil.searchUser().id;
        planItem.plan.update();
       // planItem.itemStatus=COMPLETE;
        Execution execution = new Execution();
        execution.planItem=planItem;
        PlanItemDetail planItemDetail=PlanItemDetail.find().byId(stuffingDetial.PlanDetialId);
        if(planItemDetail!=null){
        	planItemDetail.detailStatus=COMPLETE;
        	execution.planItemDetail=planItemDetail;
        }
       // System.out.println("12314513423");
        //System.out.println("============planItem.material=================="+play.libs.Json.toJson(planItem));
        execution.material=planItem.material;
        execution.executionType=CargoExecutionType;
        execution.executionSubtype=CargoExecutionSuperType;
        execution.executedAt=DateUtil.currentTimestamp();
        execution.fromMaterialUom=planItem.fromMaterialUom;
        if(stock==null) throw new Exception("Incomplete data");
        execution.executedQty=new BigDecimal(stuffingDetial.stuffingQuantity);
        execution.toMaterialUom=planItem.toMaterialUom;
        execution.fromArea=stock.area;
        execution.fromBin=stock.bin;
        execution.ext=stuffingDetial.returnExt();
        execution.createdAt=DateUtil.currentTimestamp();
        execution.createdBy=SessionSearchUtil.searchUser().id;
        execution.save();
        StockTransaction stockTransaction = new StockTransaction();
        stockTransaction.warehouse=Warehouse.find().where().eq("deleted", false).eq("id", SessionSearchUtil.searchWarehouse().id.toString()).findUnique();
        stockTransaction.stock=stock;
        stockTransaction.execution=execution;
        stockTransaction.transactionCode=CargoTranactionCode;
        stockTransaction.oldUomId=stock.materialUom.id;
        stockTransaction.oldQty=stock.qty;
        stockTransaction.oldAreaId=stock.area.id;
        stockTransaction.oldBinId=stock.bin.id;
        stockTransaction.oldArrivedAt=stock.arrivedAt;
        stockTransaction.oldStatus=stock.stockStatus;
        stockTransaction.oldTracingId=stock.tracingId;
        stockTransaction.newQty=new BigDecimal(stock.qty.doubleValue()-stuffingDetial.stuffingQuantity);
        stockTransaction.transactionAt=DateUtil.currentTimestamp();
        stockTransaction.createdAt=DateUtil.currentTimestamp();
        stockTransaction.createdBy=SessionSearchUtil.searchUser().id;
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
        // DataExchangePlatform.setTransaction(stockTransaction);
        }
    }
     
    public static boolean isComplete(PlanItem planItem){
        List<OrderItem> orderItems = OrderItem.find().where().eq("deleted", false).eq("order", planItem.order).eq("order.warehouse.id",  SessionSearchUtil.searchWarehouse().id.toString()).findList();
        if(orderItems.size()>0){
        double Sum=0;
        for (OrderItem orderItem : orderItems) {
            Sum= Sum+orderItem.settlementQty.doubleValue();
        }
        List<PlanItem> planItems = PlanItem.find().where().eq("toMaterialUom.deleted", false).eq("fromMaterialUom.deleted", false).eq("material.deleted", false).eq("deleted", false).eq("order.warehouse.id",  SessionSearchUtil.searchWarehouse().id.toString()).eq("order", orderItems.get(0).order).findList();
        double SumQty=0;
        for (PlanItem planItem2 : planItems) {
           // UnitConversion.returnComparing(uom, sku, materialId)
        	if(isPlanExecuted(planItem2.id.toString(),planItem2.palnnedQty))
            SumQty= SumQty+planItem2.palnnedQty.doubleValue()*UnitConversion.returnComparing(planItem2.fromMaterialUom.uomCode, planItem2.toMaterialUom.uomCode, planItem2.material.id.toString());
        }
        System.out.println("PlanItem qty"+SumQty+":"+Sum);
        if(SumQty>=Sum){
            /*for (OrderItem orderItem : orderItems) {
                orderItem.order.orderStatus=COMPLETE;
                orderItem.order.updatedAt=DateUtil.currentTimestamp();
                orderItem.order.update();
            }*/
            return true;
            }
        }
        return false;
    }
    public static boolean isPlanExecuted(String id,BigDecimal plannedQty){
    	List<PlanItemDetail> findList = PlanItemDetail.find().where().eq("deleted", false).eq("planItem.id", id).findList();
    	boolean isAllExecuted=true;
    	BigDecimal totalQty = new BigDecimal(0);
    	for (PlanItemDetail planItemDetail : findList) {
    		List<Execution> findList2 = Execution.find().where().eq("deleted", false).eq("planItemDetail.id",planItemDetail.id).findList();
    		if(findList2.size()<1)
    			isAllExecuted=false;
    		else{
    			if(findList2.get(0)!=null&&findList2.get(0).executedQty!=null)
    				totalQty=totalQty.add(findList2.get(0).executedQty);
    		}
		}
    	if(isAllExecuted&&plannedQty!=null&&plannedQty.compareTo(totalQty)<1)
    		return true;
    	else 
    		return false;
    }
    @Transactional
    public static Result deleteDetial(){
    	System.out.println("delete");
    	JsonNode body = request().body().asJson();
        if(body!=null){
        	List<String> mp = Json.fromJson(body, List.class);
        	for(String id:mp){
        		PlanItemDetail planItemDetail = PlanItemDetail.find().byId(id);
        		if(planItemDetail.deleted==true) return ok(play.libs.Json.toJson(new ResultVo("error","This Detial had deleted!")));
        		List<Execution> findList = Execution.find().where().eq("deleted", false).eq("planItemDetail.id", planItemDetail.id).findList();
        		if(findList.size()>0)
        			return ok(play.libs.Json.toJson(new ResultVo("error","This Detial had Executioned!")));
        		planItemDetail.deleted=true;
        		planItemDetail.updatedAt=DateUtil.currentTimestamp();
        		planItemDetail.updatedBy=SessionSearchUtil.searchUser().id;
        		planItemDetail.update();
        	}
        	System.out.println("end");
        	return ok(play.libs.Json.toJson(new ResultVo("success","Delete Success")));	
        }
        return ok(play.libs.Json.toJson(new ResultVo("error","Can't Catch Data")));	
    }
     /*
      * execution的数据生成表格
      */
    
}
