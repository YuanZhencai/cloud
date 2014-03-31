package controllers.report;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Execution;

import org.apache.commons.lang3.StringUtils;

import play.cache.Cache;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import play.mvc.With;
import security.NoUserDeadboltHandler;
import utils.DateUtil;
import utils.UnitConversion;
import utils.Excel.ExcelUtil;
import views.html.report.receiveDetailsReport;
import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: controllers.report.ReceiveDetailsReportController.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:cuichunsheng@wcs-global.com">Cui Chunsheng</a>
 */
@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class ReceiveDetailsReportController  extends Controller{
	
	private static List cachedResult = new ArrayList(0);//缓存数据
	
	public static Result index(){
		cachedResult = new ArrayList(0);;
//		Cache.set("cached_receivedetails",new ArrayList(0));
		return ok(receiveDetailsReport.render(""));
	}
	
	/**
	 * 报表数据查询
	 * */
	@SuppressWarnings("unchecked")
	public static Result search(){
		RequestBody body = request().body();
		List<Map> result = new ArrayList<Map>(0);
		if(null!=body.asJson()){
			Map<String,String> mp = Json.fromJson(body.asJson(), HashMap.class);
			
			String dateFrom = null;
			if(StringUtils.isBlank(mp.get("dateTimeFrom"))){
				dateFrom = "2010-1-1 00:00:00";
			}else{
				dateFrom = mp.get("dateTimeFrom").replace("T"," ");
			}
			
			String dateTo = null;
			if(StringUtils.isBlank(mp.get("dateTimeTo"))){
				dateTo = DateUtil.dateToStrLong(new Date());
			}else{
				dateTo = mp.get("dateTimeTo").replace("T"," ");
			}
			
			StringBuilder sb = new StringBuilder("SELECT to_char(t_p.planned_timestamp,'DD/MM/YYYY') as ProductionDate,"//t_pid.ext::hstore->'productionDate'
					+" t_b.name_key as PrdLine,t_o.internal_order_no as PiNo,t_pi.palnned_qty as planQty," 
					+" sum(t_e.executed_qty) as ReceiveQty,t_m.material_name as materialdesc,"
					+" t_oi.settlement_qty as piQty,t_mu_from.uom_code as prodUom,"
					+" t_pi.palnned_qty as piSKU,t_mu_to.uom_code as UOM,"
					+" t_oi.settlement_uom_id as oiUomId,t_pi.material_uom_id as piUomId,to_char(t_e.executed_at,'DD/MM/YYYY') as executedTime"
					+" FROM t_execution t_e "
//					+" INNER JOIN t_plan_item_detail t_pid ON t_pid.id = t_e.plan_item_detail_id"
					+" INNER JOIN t_plan_item t_pi ON t_e.plan_item_id = t_pi.id "
					+" INNER JOin t_plan t_p ON t_p.id = t_pi.plan_id"
					+" INNER JOIN t_material t_m on t_m.id = t_pi.material_id "
					+" INNER JOIN t_material_uom t_mu_to ON t_mu_to.id = t_pi.to_uom_id "
					+" INNER JOIN t_material_uom t_mu_from ON t_mu_from.id=t_pi.from_uom_id");
			sb.append(" INNER JOIN t_bin t_b ON t_pi.to_bin_id = t_b.id ");
			sb.append(" INNER JOIN t_order t_o 	ON t_o.id = t_pi.order_id ");
			sb.append(" INNER JOIN t_order_item t_oi ON t_oi.order_id=t_o.id ");
//			sb.append(" INNER JOIN t_material_uom t_oi_mu ON t_oi_mu.id=t_oi.settlement_uom");
			sb.append(" WHERE t_pi.deleted=false ");
			sb.append(" AND t_e.deleted=false AND t_e.execution_type = '"+Execution.EXECUTION_TYPE_RECEIVE+"'");
//			sb.append(" AND t_pid.detail_status='S001' AND t_pi.plan_type = 'T001' ");//RECEIVE
			/*sb.append(" AND t_pid.id IN ("//NOT
				+"SELECT plan_item_detail_id FROM where plan_item_detail_id IS NOT NULL"
				+")");*/
			sb.append(" AND (t_e.created_at BETWEEN '"+dateFrom+"' AND '"+dateTo+"') ");
			sb.append(" GROUP BY ProductionDate,PrdLine,PiNo,materialdesc,prodUom,piQty,piSKU,UOM,oiUomId,piUomId,executedTime,planQty");
			
			List<SqlRow> orders = Ebean.createSqlQuery(sb.toString()).findList();
			result = new ArrayList<Map>(orders.size());
			
			Map<String,BigDecimal> piQtyMap = new HashMap<String,BigDecimal>();
			
			for(SqlRow order:orders){
				
				BigDecimal conversion = UnitConversion.SkuToQuantity(order.getString("oiUomId"),order.getString("piUomId"));
				
				order.put("productionqty", order.getBigDecimal("piSKU").multiply(conversion));
				order.put("planreceiveqty",order.getBigDecimal("receiveqty").multiply(conversion));
				order.put("planstatus",order.get("planreceiveqty")+"/"+order.get("productionqty"));
				//Execution.find().where().eq("deleted", false).eq("planItem.planType", "T001")
//				.eq("planItem.orderItem.id", planItem.orderItem.id).findList()
				sb = new StringBuilder("select sum(t_e.executed_qty) as qty from t_execution t_e"
						+" INNER JOIN t_plan_item t_pi ON t_pi.id=t_e.plan_item_id "
						+" INNER JOIN t_order t_o ON t_pi.order_id=t_o.id "
						+" WHERE t_e.deleted=false AND t_e.execution_type='"+Execution.EXECUTION_TYPE_RECEIVE+"'");
				sb.append(" AND t_o.internal_order_no = '"+order.getString("pino")+"' AND t_e.plan_item_id IS NOT NULL ");
				
				if(!piQtyMap.containsKey(order.getString("pino"))){
					List<SqlRow> rows = Ebean.createSqlQuery(sb.toString()).findList();//Receive/Issue
					if(null!=rows&&rows.size()>0){
						for(SqlRow row:rows){
							piQtyMap.put(order.getString("pino"),(row.getBigDecimal("qty")).multiply(conversion));
						}
					}
				}
				
				order.put("pireceivedqty",piQtyMap.get(order.getString("pino")));
				order.put("pistatus",order.get("pireceivedqty")+"/"+order.get("piqty"));
				result.add(order);
			}
		}
		List<Map> mergeResult = result;//mergeRepeatPiNo(
		cachedResult = mergeResult;
//		Cache.set("cached_receivedetails", mergeResult);
		return ok(Json.toJson(mergeResult));
	}
	
	/**
	 * 报表导出
	 * */
	public static Result report(){
		
		String[] headers = new String[]{"Planned Prod Date","Prod Line","PI No","Material Description",
				"Transaction Date-time","Received Qty","UOM",
				"Prod Qty","UOM","PI SKU","UOM","Plan Status","PI Status"};
		
		String[] keys = new String[]{"productiondate","prdline","pino","materialdesc","executedtime",
				"receiveqty","uom","productionqty","produom","pisku","uom","planstatus","pistatus"};
		
 		File file = ExcelUtil.exportExcel(null,"goodsReceiveDetailsReport.xlsx",headers,keys,cachedResult);//(List<Map>)Cache.get("cached_receivedetails")
		response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");// 可选择不同类型
		response().setHeader("Content-Disposition", "attachment; filename=goodsReceiveDetailsReport"+DateUtil.dateToExcelPattern(new Date())+".xlsx");
		return ok(file);
		
	}
}
