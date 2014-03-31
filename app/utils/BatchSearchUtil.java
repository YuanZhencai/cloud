package utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import utils.enums.CodeKey;

import models.Batch;
import models.PlanItem;
import models.Stock;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.SqlRow;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: BatchSearchVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
public class BatchSearchUtil {
	/**
	 * 注意：因为select的只有id，所以可用的也就只有id，其他字段如果想获取值，要在select后加字段名称，不然会报错
	 * @param lot
	 * @param line
	 * @param date
	 * @param pi
	 * @return
	 */
	public static List<Batch> serchBatch(String lot, String line, String date, String pi) {
		lot = ExtUtil.encodeSpecialChar(lot);
		line = ExtUtil.encodeSpecialChar(line);
		date = ExtUtil.encodeSpecialChar(date);
		pi = ExtUtil.encodeSpecialChar(pi);
		List<Batch> batchIdList = new ArrayList<Batch>();
		String sql = "select b.id from t_batch b where b.ext::hstore->'lot'='" + lot + "' and b.ext::hstore->'line'='" + line
				+ "' and b.ext::hstore->'pi'='" + pi + "' and b.ext::hstore->'date'='" + date + "'";
		RawSql rawSql = RawSqlBuilder.parse(sql).create();
		Query<Batch> query = Ebean.find(Batch.class);
		batchIdList = query.setRawSql(rawSql).findList();
		return batchIdList;
	}

	/**
	 * 
	 * @param map
	 * @return
	 */
	public static List<Batch> serchBatch(Map<String, String> map) {
		String pi = map.get("pi");
		String lot = map.get("lot");
		lot = ExtUtil.encodeSpecialChar(lot);
		pi = ExtUtil.encodeSpecialChar(pi);
		List<Batch> batchIdList = new ArrayList<Batch>();
		StringBuffer sb = new StringBuffer("select b.id,b.ext from t_batch b where b.deleted=false and b.ext::hstore->'pi'='" + pi + "'");

		if (EmptyUtil.isNotEmtpyString(lot)) {
			sb.append(" and b.ext::hstore->'lot'='" + lot + "'");
		}
		RawSql rawSql = RawSqlBuilder.parse(sb.toString()).create();
		Query<Batch> query = Ebean.find(Batch.class);
		batchIdList = query.setRawSql(rawSql).findList();
		return batchIdList;
	}

	public static List<Batch> serchlikeBatch(HashMap<String, String> map) {
		String pi = map.get("pi");
		String line = map.get("line");
		String dateFrom = map.get("datefrom");
		if (dateFrom == null || "".equals(dateFrom)) {
			dateFrom = "1990-1-1";
		}
		String dateTo = map.get("dateto");
		if (dateTo == null || "".equals(dateTo)) {
			dateTo = "2020-1-1";
		}
		String lot = map.get("lot");
		lot = ExtUtil.encodeSpecialChar(lot);
		line = ExtUtil.encodeSpecialChar(line);
		dateFrom = ExtUtil.encodeSpecialChar(dateFrom);
		dateTo = ExtUtil.encodeSpecialChar(dateTo);
		pi = ExtUtil.encodeSpecialChar(pi);
		List<Batch> batchIdList = new ArrayList<Batch>();
		// System.out.println("select id from t_batch b where b.deleted=false  and b.ext::hstore ->'pi' like'%"
		// + pi+ "%' and b.ext::hstore ->'line' like'%" + line +
		// "%' and b.ext::hstore ->'date' between '" + dateFrom+" 00:00:00"+
		// "' and '"+dateTo+" 23:59:59"+"' and b.ext::hstore ->'lot' like'%"
		// +lot + "%'");
		StringBuffer sb = new StringBuffer("select id from t_batch b where b.deleted=false  and b.ext::hstore ->'pi' like'%" + pi
				+ "%' and b.ext::hstore ->'line' like'%" + line + "%' and b.ext::hstore ->'date' between '" + dateFrom + "' and '"
				+ dateTo + " 23:59:59" + "' and b.ext::hstore ->'lot' like'%" + lot + "%'");
		RawSql rawSql = RawSqlBuilder.parse(sb.toString()).create();
		Query<Batch> query = Ebean.find(Batch.class);
		batchIdList = query.setRawSql(rawSql).findList();
		return batchIdList;
	}

	public static List<SqlRow> batchdetails(HashMap<String, String> map) {
		String pi = map.get("pi");
		String dateFrom = map.get("datefrom");
		if (dateFrom == null || "".equals(dateFrom)) {
			dateFrom = "1990-1-1";
		}
		String dateTo = map.get("dateto");
		if (dateTo == null || "".equals(dateTo)) {
			dateTo = "2020-1-1";
		}
		dateFrom = ExtUtil.encodeSpecialChar(dateFrom);
		dateTo = ExtUtil.encodeSpecialChar(dateTo);
		pi = ExtUtil.encodeSpecialChar(pi);
		StringBuffer sql = new StringBuffer(
				"select id, b.ext::hstore ->'pi' as pi  from t_batch b where b.deleted=false  and b.ext::hstore ->'pi' like'%" + pi
						+ "%'  and b.ext::hstore ->'date' between '" + dateFrom  + "' and '" + dateTo + " 23:59:59" + "'");
		List<SqlRow> batchDetails = Ebean.createSqlQuery(sql.toString()).findList();
		return batchDetails;
	}

	/**
	 * 
	 * @param lot
	 * @param line  option
	 * @param date  option
	 * @param pi
	 * @return
	 */
	public static PlanItem serchPlanItem(String line, String date, String pi, String planItemId) {
		line = ExtUtil.encodeSpecialChar(line);
		date = ExtUtil.encodeSpecialChar(date);
		pi = ExtUtil.encodeSpecialChar(pi);
		PlanItem planItem = null;
		List<PlanItem> pis = Ebean.find(PlanItem.class).where().eq("deleted", false).eq("planType", CodeKey.PLAN_TYPE_TRANSFER.getValue()).eq("order.internalOrderNo", pi).findList();
	/*	List<PlanItem> planItemIds = new ArrayList<PlanItem>();
		List<PlanItem> planItems = new ArrayList<PlanItem>();
		StringBuffer sb = new StringBuffer("select tpi.id from t_plan_item tpi where tpi.deleted=false and tpi.plan_type='"
				+ CodeKey.PLAN_TYPE_TRANSFER.toString() + "' and tpi.ext::hstore->'pi'='" + pi);
		if (EmptyUtil.isNotEmtpyString(date)) {
			sb.append("' and tpi.ext::hstore->'date'='" + date);
		}
		if (EmptyUtil.isNotEmtpyString(line)) {
			sb.append("' and tpi.ext::hstore->'line'='" + line);
		}
		if (EmptyUtil.isNotEmtpyString(planItemId)) {
			sb.append("' and tpi.ext::hstore->'planItemId'='" + planItemId);
		}
		sb.append("'");
		RawSql rawSql = RawSqlBuilder.parse(sb.toString()).create();
		Query<PlanItem> query = Ebean.find(PlanItem.class);
		planItemIds = query.setRawSql(rawSql).findList();
		if (planItemIds != null && !planItemIds.isEmpty()) {
			for (PlanItem planA : planItemIds) {
			  planItem = Ebean.find(PlanItem.class).where().eq("id", planA.id).findUnique();
			  break;
			 
			}
		}*/
		if(EmptyUtil.isNotEmptyList(pis)){
			for(PlanItem p :pis){
				if(planItemId.equals(ExtUtil.unapply(p.ext).get("planItemId"))){
					planItem = p;	
				}
			}
		}
		return planItem;
	}

	/**
	 * 仅限GoodReceive自动生成transferplan时判断是更新还是新建
	 * @param pi
	 * @param lotno
	 * @param executeDate
	 * @return
	 */
	public static PlanItem searchPlanItem(String pi, String lotno, Date executeDate) {
		List<PlanItem> planItemList = PlanItem.find().where().eq("deleted", false).eq("plannedExecutionAt", executeDate).eq("planType", "T002")
				.findList();
		PlanItem planItem = null;
		for (PlanItem item : planItemList) {
			HashMap<String, String> extMap = new HashMap<String, String>();
			extMap = ExtUtil.unapply(item.ext);
			if (StringUtils.isNotEmpty(pi) && StringUtils.isNotEmpty(lotno) && pi.equals(extMap.get("pi")) && lotno.equals(extMap.get("lot"))
					&& "Tempering".equals(extMap.get("transferType"))) {
				planItem = item;
				continue;
			}
		}
		return planItem;
	}

	public static List<Batch> getBatch(String piNo) {
		piNo = ExtUtil.encodeSpecialChar(piNo);
		List<Batch> batchList = new ArrayList<Batch>();
		String sql = "select id, ext::hstore->'lot' as batchNo  from t_batch  where ext::hstore ->'pi' ='" + piNo + "'";
		RawSql rawSql = RawSqlBuilder.parse(sql).create();
		batchList = Ebean.find(Batch.class).setRawSql(rawSql).findList();
		for (Batch batch : batchList) {
			batch.batchNo = ExtUtil.decodeSpecialChar(batch.batchNo);
		}
		return batchList;
	}

	public static List<Batch> getlikeBatch(String piNo) {
		System.out.println(piNo);
		if (piNo == null)
			piNo = "";
		else
			piNo = ExtUtil.encodeSpecialChar(piNo);
		List<Batch> batchList = new ArrayList<Batch>();
		String sql = "select id, ext::hstore->'lot' as batchNo  from t_batch  where ext::hstore ->'pi' like'%" + piNo + "%'";
		RawSql rawSql = RawSqlBuilder.parse(sql).create();
		batchList = Ebean.find(Batch.class).setRawSql(rawSql).findList();
		for (Batch batch : batchList) {
			batch.batchNo = ExtUtil.decodeSpecialChar(batch.batchNo);
		}
		return batchList;
	}
	public static List<Batch> getlikeBatch(String piNo,String lot) {
		System.out.println(piNo);
		if (piNo == null)
			piNo = "";
		else
			piNo = ExtUtil.encodeSpecialChar(piNo);
		if (lot == null)
			lot = "";
		else
			lot = ExtUtil.encodeSpecialChar(lot);
		List<Batch> batchList = new ArrayList<Batch>();
		String sql = "select id from t_batch b  where b.ext::hstore ->'pi' like '%" + piNo + "%'" +" and b.ext::hstore ->'lot' like '%" + lot + "%'";
		RawSql rawSql = RawSqlBuilder.parse(sql).create();
		batchList = Ebean.find(Batch.class).setRawSql(rawSql).findList();
		return batchList;
	}
	public static List<Batch> getBatch(String piNo, String lot) {
		lot = ExtUtil.encodeSpecialChar(lot);
		piNo = ExtUtil.encodeSpecialChar(piNo);
		List<Batch> batchList = new ArrayList<Batch>();
		String sql = "select id from t_batch  where ext::hstore ->'pi'='" + piNo + "' and ext::hstore ->'lot'='" + lot + "'";
		RawSql rawSql = RawSqlBuilder.parse(sql).create();
		batchList = Ebean.find(Batch.class).setRawSql(rawSql).findList();
		return batchList;
	}

	/**
	 *  根据piNo得到其所对应的stock
	 * @param piNo 
	 * @return
	 */
	public static List<Stock> viaPIGetStock(String piNo) {
		List<Stock> stocks = new ArrayList<Stock>();
		if (StringUtils.isEmpty(piNo)) {
			throw new NullPointerException("IN method viaPIGetStock,piNo =" + piNo + " is null,please check");
		}
		List<Batch> batchs = getBatch(piNo);
		if (EmptyUtil.isNotEmptyList(batchs)) {
			for (Batch batch : batchs) {
				List<Stock> tempStocks = Ebean.find(Stock.class).where().eq("deleted", false).eq("batch.id", batch.id).findList();
				if (EmptyUtil.isNotEmptyList(tempStocks)) {
					stocks.addAll(tempStocks);
				}
			}
		}
		return stocks;
	}

	/**
	 * 
	 * @param piNo
	 * @param lot
	 * @return  id date matCode
	 */
	public static List<SqlRow> getBatchDetail(String piNo, String lot) {
		lot = ExtUtil.encodeSpecialChar(lot);
		piNo = ExtUtil.encodeSpecialChar(piNo);
		String sql = "select b.id ,b.ext::hstore ->'date' as date ,m.material_code as matCode from t_batch b left outer join t_material m on b.material_id=m.id where b.ext::hstore ->'pi'='"
				+ piNo + "' and b.ext::hstore ->'lot'='" + lot + "'";
		List<SqlRow> batchDetails = Ebean.createSqlQuery(sql).findList();
		return batchDetails;
	}

	public static List<Batch> getBatch(String piNo, String lot, String line) {
		lot = ExtUtil.encodeSpecialChar(lot);
		line = ExtUtil.encodeSpecialChar(line);
		piNo = ExtUtil.encodeSpecialChar(piNo);
		List<Batch> batchList = new ArrayList<Batch>();
		String sql = "select id from t_batch  where ext::hstore ->'pi'='" + piNo + "' and ext::hstore ->'lot'='" + lot
				+ "' and ext::hstore ->'line'='" + line + "'";
		RawSql rawSql = RawSqlBuilder.parse(sql).create();
		batchList = Ebean.find(Batch.class).setRawSql(rawSql).findList();
		return batchList;
	}

	/**
	 * 
	 * @param piNo
	 * @return
	 */
	public static List<SqlRow> getLotNo(String piNo) {
		piNo = ExtUtil.encodeSpecialChar(piNo);
		String sql = "select id, ext::hstore->'lot' as lot  from t_plan_item  where plan_type='T001' and  deleted=false and ext::hstore ->'pi'='"
				+ piNo + "'";
		List<SqlRow> rows = Ebean.createSqlQuery(sql).findList();
		return rows;
	}
	
	
	/**
	 * 根据条件查询batch集合
	 * @param params 
	 * @return List<Batch>
	 * */
	public static List<Batch> getBatchsByCondition(Map<String,String> params) {
		
		StringBuffer sb = new StringBuffer("select b.id,b.ext from t_batch b where b.deleted=false");

		String pi = params.get("pi");
		if(StringUtils.isNotEmpty(pi)){
			pi = ExtUtil.encodeSpecialChar(pi);
			sb.append(" and b.ext::hstore->'pi'='" + pi + "'");
		}
		
		String pilike = params.get("pi_like");
		if(StringUtils.isNotEmpty(pilike)){
			pilike = ExtUtil.encodeSpecialChar(pilike);
			sb.append(" and b.ext::hstore->'pi' like '%" + pilike + "%'");
		}
		
		String lot = params.get("lot");		
		if (StringUtils.isNotEmpty(lot)) {//batchNo
			lot = ExtUtil.encodeSpecialChar(lot);
			sb.append(" and b.ext::hstore->'lot'='" + lot + "'");
		}
		
		String lotlike = params.get("lot_like");		
		if (StringUtils.isNotEmpty(lotlike)) {//batchNo
			lotlike = ExtUtil.encodeSpecialChar(lotlike);
			sb.append(" and b.ext::hstore->'lot' like '%" + lotlike + "%'");
		}
		
		String date = params.get("date");
		if (StringUtils.isNotEmpty(date)){
			date = ExtUtil.encodeSpecialChar(date);
			sb.append(" and b.ext::hstore->'date'='" + date + "'");
		}
		
		String line = params.get("line");
		if (StringUtils.isNotEmpty(line)){
			line = ExtUtil.encodeSpecialChar(line);
			sb.append(" and b.ext::hstore->'line'='" + line + "'");
		}
		
		RawSql rawSql = RawSqlBuilder.parse(sb.toString()).create();
		return Ebean.find(Batch.class).setRawSql(rawSql).findList();
	}
}
