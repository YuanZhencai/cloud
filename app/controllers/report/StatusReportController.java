package controllers.report;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Execution;

import org.apache.commons.lang3.StringUtils;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import play.mvc.With;
import security.NoUserDeadboltHandler;
import utils.DateUtil;
import utils.Excel.ExcelUtil;
import views.html.report.statusReport;
import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;


/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: controllers.report.StatusReportController.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:cuichunsheng@wcs-global.com">Cui Chunsheng</a>
 */
@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class StatusReportController extends Controller{
	
	private static List<Map> cachedResult = null;//缓存数据
	
	public static Result index(){
		cachedResult = null;
		return ok(statusReport.render(""));
	}
	
	/**
	 * 报表数据查询
	 * */
	@SuppressWarnings("unchecked")
	public static Result search(){
		RequestBody body = request().body();
		List<Map> result = null;
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
			
//			List<Order> orders = Order.find().where().eq("deleted",false).between("createdAt",dateFrom,dateTo).order().desc("orderStatus").findList();
			
			StringBuilder sb = new StringBuilder("SELECT t_o.internal_order_no AS piNo,t_m.material_name AS materialDesc,t_c.name_key AS statusType,"
					+" t_oi.ext::hstore->'piQty' AS piQty,t_oi.ext::hstore->'closed_date' AS closedDate,t_oi.ext::hstore->'closed_time' AS closedTime"
					+" FROM t_order t_o "
					+" INNER JOIN t_order_item t_oi ON t_oi.order_id = t_o.id "
					+" INNER JOIN t_material t_m ON t_oi.material_id = t_m.id ");
			sb.append(" LEFT JOIN t_code t_c ON t_c.code_key = t_o.order_status ");
			sb.append(" INNER JOIN t_code_cat t_cc ON t_cc.id = t_c.code_cat_id ");
			sb.append(" WHERE t_o.deleted=false ");//AND t_o.order_type='T001' 
			sb.append(" AND t_c.deleted=false AND t_cc.deleted=false AND t_cc.cat_key='ORDER_STATUS'");
			sb.append(" AND (t_o.created_at BETWEEN '"+dateFrom+"' AND '"+dateTo+"') ");
			sb.append(" ORDER BY t_o.order_status ");
			
			List<SqlRow> orders = Ebean.createSqlQuery(sb.toString()).findList();
			result = new ArrayList<Map>(orders.size());
			
			for(SqlRow order:orders){
				
				order.put("closeddatetime","");
				
				if(StringUtils.isNotBlank(order.getString("closeddate"))){
					order.put("closeddatetime",DateUtil.dateToStringEn2(new Date(order.getLong("closeddate"))));
				}
				
				if(StringUtils.isNotBlank(order.getString("closedtime"))){
					order.put("closeddatetime",order.get("closeddatetime")+" "+DateUtil.dateToShortTime(new Date(order.getLong("closedtime"))));
				}
				
				
				/*sb = new StringBuilder("SELECT t_o.internal_order_no,t_e.execution_type AS exeType,sum(t_e.executed_qty) AS qty FROM t_execution t_e "
						+" LEFT JOIN t_plan_item t_pi ON t_pi.id=t_e.plan_item_id "
						+" INNER JOIN t_order t_o ON t_pi.order_id=t_o.id "
						+" WHERE (t_e.execution_type='"+Execution.EXECUTION_TYPE_RECEIVE+"'"
							+" OR t_e.execution_type='"+Execution.EXECUTION_TYPE_ISSUE+"')");
				sb.append(" AND t_o.internal_order_no = '"+order.getString("piNo")+"'");
				sb.append(" GROUP BY t_o.internal_order_no,t_e.execution_type ");*/
				
				
				
				sb = new StringBuilder("SELECT t_b.ext::hstore->'pi' AS pino,t_st.transaction_code AS transcode," +
						" sum(t_st.new_qty) AS newqty,sum(t_st.old_qty) AS oldqty FROM t_stock_transaction t_st");
				sb.append(" INNER JOIN t_stock t_s ON t_s.id=t_st.stock_id ");
				sb.append(" INNER JOIN t_batch t_b ON t_b.id=t_s.batch_id ");
				sb.append(" WHERE t_b.ext::hstore->'pi' = '"+order.getString("piNo")+"'");
				sb.append(" AND t_st.transaction_code IN ('T101','T102','T103','T301','T302','T303','T304')");
				sb.append(" GROUP BY pino,transcode");
				
				order.put("issueQty","0");
				order.put("receiveQty","0");
				order.put("remainingQty","0");
				List<SqlRow> rows = Ebean.createSqlQuery(sb.toString()).findList();//Receive/Issue
				if(null!=rows&&rows.size()>0){
					for(SqlRow row:rows){
						String exeType = row.getString("transcode");
						double oldqty = StringUtils.isNotBlank(row.getString("oldqty"))?row.getDouble("oldqty"):0;
						double newqty = StringUtils.isNotBlank(row.getString("newqty"))?row.getDouble("newqty"):0;
						if(exeType.indexOf("T10")!=-1){
							order.put("receiveQty",newqty-oldqty);//StringUtils.isNotBlank(row.getString("qty"))?row.getString("qty"):"0"
						}else if(exeType.indexOf("T30")!=-1){
							order.put("issueQty",oldqty-newqty);
						}
					}
					order.put("remainingQty",order.getDouble("receiveQty")-order.getDouble("issueQty"));
				}
				result.add(order);
			}
		}
		List<Map> mergeResult = mergeRepeatPiNo(result);
		cachedResult = mergeResult;
		return ok(Json.toJson(mergeResult));
	}
	
	//合并重复PI No
	private static List<Map> mergeRepeatPiNo(List<Map> res){
		
		Map<String,RepeatPiBean> piNoMap = new HashMap<String,RepeatPiBean>(); 
		
		List<Map> result = new ArrayList<Map>();
		
		int c = 0;
		for(int i=0,len=res.size();i<len;i++){
			
			SqlRow order = (SqlRow) res.get(i);
			
			RepeatPiBean bean = piNoMap.get(order.getString("piNo"));
			if(null==bean||!bean.isExisted){
				RepeatPiBean tmpBean = RepeatPiBean.getRepeatPiBean();
				tmpBean.index = c;
				tmpBean.statusType =  order.getString("statustype");
				tmpBean.closeddatetime = order.getString("closeddatetime");
				tmpBean.isExisted = true;
				piNoMap.put(order.getString("piNo"),tmpBean);
				result.add(order);
				c++;
			}else{
				SqlRow previousOrder = (SqlRow) result.get(bean.index);
				if((previousOrder.getString("statustype")).indexOf(order.getString("statustype"))==-1){
					previousOrder.put("statustype",previousOrder.getString("statustype")
							+","+order.getString("statustype"));
				}
				
				if(StringUtils.isNotBlank(order.getString("closeddatetime"))){
					previousOrder.put("closeddatetime",order.getString("closeddatetime"));
				}
			}
		}
		return result;
	}
	
	private static final class RepeatPiBean{
		
		private RepeatPiBean(){}
		
		private static RepeatPiBean bean = new RepeatPiBean();
		
		public int index;//集合列
		public String statusType;
		public String closeddatetime;
		public boolean isExisted = false;
		
		public static RepeatPiBean getRepeatPiBean(){
			if(bean==null){
				bean = new RepeatPiBean();
			}
			return bean;
		}
	}
	
	/**
	 * 报表导出
	 * */
	public static Result report(){
		String[] headers = new String[]{"","PI No","Material Desc",
				"Receive","Issue","Remaining","PI Qty","Closing Date/Time"};
		
		String[] keys = new String[]{"statustype","pino","materialdesc",
				"receiveqty","issueqty","remainingqty","piqty","closeddatetime"};
		
 		File file = ExcelUtil.exportExcel(null,"piStatusReport.xlsx",headers,keys,cachedResult);
		response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");// 可选择不同类型
		response().setHeader("Content-Disposition", "attachment; filename=piStatusReport.xlsx");
		return ok(file);
		
	}
	
	
}
