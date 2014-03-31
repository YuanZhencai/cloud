//PlanItem.java
package models;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import play.libs.Json;

import models.vo.transfer.TPResult;
import models.vo.transfer.TPSearch;
import utils.BatchSearchUtil;
import utils.CodeUtil;
import utils.CrudUtil;
import utils.DateUtil;
import utils.EmptyUtil;
import utils.ExtUtil;
import utils.enums.CodeKey;
import utils.exception.CustomException;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.FetchConfig;
import com.avaje.ebean.Query;

@Entity
@Table(name = "t_plan_item")
public class PlanItem extends BaseModel {

	@ManyToOne
	public Plan plan;

	@Column(length = 40)
	public String planType;
	@Column(length = 40)
	public String planSubtype;

	@ManyToOne
	public Order order;
	@ManyToOne
	public OrderItem orderItem;
	@ManyToOne
	public Material material;
	@ManyToOne
	public MaterialUom materialUom;
	@ManyToOne
	public Batch batch;

	public Timestamp plannedExecutionAt;
	@Column(precision = 18, scale = 4)
	public BigDecimal palnnedQty;
	@Column(precision = 18, scale = 4)
	public BigDecimal minPercent;
	@Column(precision = 18, scale = 4)
	public BigDecimal maxPercent;

	@ManyToOne
	@JoinColumn(name = "from_uom_id")
	public MaterialUom fromMaterialUom;
	@ManyToOne
	@JoinColumn(name = "from_area_id")
	public Area fromArea;
	@ManyToOne
	@JoinColumn(name = "from_bin_id")
	public Bin fromBin;

	@ManyToOne
	@JoinColumn(name = "to_uom_id")
	public MaterialUom toMaterialUom;
	@ManyToOne
	@JoinColumn(name = "to_area_id")
	public Area toArea;
	@ManyToOne
	@JoinColumn(name = "to_bin_id")
	public Bin toBin;

	public long seqNo;
	public int sortNo;

	@ManyToOne
	@JoinColumn(name = "assigned_to")
	public Employee assignedTo;

	@Column(length = 40)
	public String itemStatus;

	@OneToMany(cascade = CascadeType.ALL)
	public List<PlanItemDetail> details = new ArrayList<PlanItemDetail>();

	public static Finder<String, PlanItem> find() {
		return new Finder<String, PlanItem>(String.class, PlanItem.class);
	}

	/**
	 *  pi /  lot /orderType   可选项  planItemId  id  line / date /
	 * @param map
	 * @param quantity
	 * @param executeDate
	 * @throws CustomException 
	 */

	public static void saveOrUpdate(Map<String, Object> map, BigDecimal quantity, Date executeDate) throws CustomException {
		System.out.println("^^^^^planItem^^^^^^^^^^^you hava  calling saveOrUpdate method^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		boolean flagUpdate = true;
		boolean flagStock = true;
		Stock tempStock = null;
		BigDecimal totalQty = new BigDecimal("0");
		// 从map中获取真正的参数
		String piNo = EmptyUtil.nullToEmpty(String.valueOf(map.get("pi")));
		List<String> lots = (List<String>) map.get("lot");
		String orderType = EmptyUtil.nullToEmpty(String.valueOf(map.get("orderType")));
		// 参数可能有也可能没有
		String line = EmptyUtil.nullToEmpty(String.valueOf(map.get("line")));
		String date = EmptyUtil.nullToEmpty(String.valueOf(map.get("date")));
		String containNo = EmptyUtil.nullToEmpty(String.valueOf(map.get("containerNo")));
		String palletized = EmptyUtil.nullToEmpty(String.valueOf(map.get("palletized")));
		String closeTime = EmptyUtil.nullToEmpty(String.valueOf(map.get("closeDateTime")));
		String remaks = EmptyUtil.nullToEmpty(String.valueOf(map.get("remaks")));
		String planItemId = EmptyUtil.nullToEmpty(String.valueOf(map.get("planItemId")));
		String id = EmptyUtil.nullToEmpty(String.valueOf(map.get("id")));
		System.out.println("parame pi : " + piNo + "  lot: " + Json.toJson(lots) + " orderType:" + orderType + " qty: " + quantity
			+ "  executeDate: " + executeDate + "  date: " + date + " line: " + line);
		HashMap<String, String> extMap = new HashMap<String, String>();
		List<Batch> batchs = new ArrayList<Batch>();
		extMap.put("pi", piNo);
		// extMap.put("line", line);
		extMap.put("date", date);
		StringBuffer lotsStr = new StringBuffer();
		for (String lot : lots) {
			extMap.put("lot", lot);
			List<Batch> tempBatchs = BatchSearchUtil.serchBatch(extMap);
			if (EmptyUtil.isNotEmptyList(tempBatchs)) {
				batchs.addAll(tempBatchs);
			}
			lotsStr.append(lot + ",");
		}
		if (lotsStr.toString().endsWith(","))
			lotsStr.deleteCharAt(lotsStr.length() - 1);
		extMap.put("lot", lotsStr.toString());
		extMap.put("transferType", "Stuffing");
		extMap.put("containerNo", containNo);
		extMap.put("palletized", palletized);
		extMap.put("closeDateTime", closeTime);
		extMap.put("remaks", remaks);
		if (EmptyUtil.isNotEmptyList(batchs)) {
			for (Batch batch : batchs) {
				List<Stock> tempStcoks = Ebean.find(Stock.class).where().eq("deleted", false).eq("batch.id", batch.id).findList();
				if (EmptyUtil.isNotEmptyList(tempStcoks)) {
					for (Stock stock : tempStcoks) {
						totalQty = totalQty.add(stock.qty);
					}
					if (flagStock) {
						tempStock = tempStcoks.get(0);
						flagStock = false;
					}
				}
			}
		} else {
			throw new CustomException(" No corresponding Batch");
		}
		if (totalQty.equals(new BigDecimal(0))) {
			throw new CustomException(" No corresponding Stock");
		}

		extMap.put("executeQty", "0");
		// 如果这个planitem（cargo）生成过transferplan 则更新
		PlanItem havaPlanItemIdPlan = null;
		if (EmptyUtil.isNotEmtpyString(planItemId)) {
			havaPlanItemIdPlan= BatchSearchUtil.serchPlanItem(line, date, piNo, planItemId);
		}
		if (orderType.trim().equals(CodeKey.ORDER_TYPE_PRODUCE.toString())) {
			havaPlanItemIdPlan = BatchSearchUtil.searchPlanItem(piNo, lots.get(0), executeDate);
		}
		List<OrderItem> orderItems = Ebean.find(OrderItem.class).fetch("order").where().eq("deleted", false).eq("order.internalOrderNo", piNo)
			.eq("order.orderType", orderType).findList();
		if (!EmptyUtil.isNotEmptyList(orderItems)) {
			throw new CustomException(" System Error : No parent document");
		}
		OrderItem pi = orderItems.get(0);
		HashMap<String, String> planItemExt = new HashMap<String, String>();
		Plan tPlan = new Plan();
		PlanItem tpItem = new PlanItem();
		if (!id.isEmpty()) {
			tpItem = Ebean.find(PlanItem.class).where().eq("deleted", false).eq("id", id).findUnique();
			tPlan = tpItem.plan;
			flagUpdate = false;
		} else if (havaPlanItemIdPlan != null) {
			tpItem = havaPlanItemIdPlan;
			tPlan = tpItem.plan;
			flagUpdate = false;
		}
	
		if (flagUpdate) {// 新增
			if (map.get("manual") != null && !String.valueOf(map.get("manual")).trim().equals("")) {
				extMap.put("transferType", String.valueOf(map.get("manual")).trim());
			} else if (orderType.trim().equals(CodeKey.ORDER_TYPE_PRODUCE.toString())) {
				extMap.put("transferType", "Tempering");
			} else if (orderType.trim().equals(CodeKey.ORDER_TYPE_CARGO.toString())) {
				extMap.put("transferType", "Stuffing");
			}
		} else {
			extMap.put("transferType", ExtUtil.unapply(tpItem.ext).get("transferType"));
			extMap.put("containerNo", ExtUtil.unapply(tpItem.ext).get("containerNo"));
			extMap.put("palletized", ExtUtil.unapply(tpItem.ext).get("palletized"));
			extMap.put("closeDateTime", ExtUtil.unapply(tpItem.ext).get("closeDateTime"));
			extMap.put("remaks", ExtUtil.unapply(tpItem.ext).get("remaks"));
		}
		planItemExt.putAll(extMap);
		planItemExt.put("proQuantity", String.valueOf(totalQty));
		// planItemExt.put("proUOM", pi.settlementUom.uomCode);
		planItemExt.put("planItemId", planItemId);
		// assign vlue to plan properties
		tPlan.warehouse = pi.order.warehouse;
		tPlan.planType = CodeKey.PLAN_TYPE_TRANSFER.toString();
		tPlan.planSubtype = CodeKey.PLAN_TYPE_TRANSFER.toString();
		tPlan.order = pi.order;
		// tPlan.seqNo = null;
		tPlan.assignedTo = null;
		
		// assign vlue to planItem properties
		tpItem.ext = ExtUtil.apply(planItemExt);
		tpItem.planType = CodeKey.PLAN_TYPE_TRANSFER.toString();
		tpItem.planSubtype = CodeKey.PLAN_TYPE_TRANSFER.toString();
		tpItem.order = pi.order;
		tpItem.orderItem = pi;
		tpItem.material = pi.material;
		tpItem.materialUom = tempStock.materialUom;
		// tpItem.batch = batch;

		tpItem.palnnedQty = quantity;
		// tpItem.minPercent = null;
		// tpItem.maxPercent = null;
		tpItem.fromMaterialUom = tempStock.materialUom;
		tpItem.toMaterialUom = null;
		tpItem.toArea = null;
		tpItem.toBin = null;
		tpItem.assignedTo = null;
		tpItem.plannedExecutionAt = new Timestamp(executeDate.getTime());
		// tpItem.seqNo = null;
		// tpItem.sortNo= null;
		if (flagUpdate) {
			tPlan.planStatus = CodeKey.PLAN_STATUS_NEW.toString();
			tpItem.itemStatus = CodeKey.PLAN_STATUS_NEW.toString();
			tpItem.plan = tPlan;
			CrudUtil.save(tPlan);
			CrudUtil.save(tpItem);
			System.out.println("^^^^^^^^^^^^^^^^save success^^^^^^^^^^^^");
		} else {
			CrudUtil.update(tPlan);
			CrudUtil.update(tpItem);
			System.out.println("^^^^^^^^^^^^^^^^update success^^^^^^^^^^^^");
		}

	}

	/**
	 * 
	 * @param tps
	 * @param tpResults
	 * @return
	 */
	public static List<TPResult> list(TPSearch tps, List<TPResult> tpResults) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		List<PlanItem> planItems = null;
		boolean flagTransferType = false;
		List<PlanItem> pis = Ebean.find(PlanItem.class).where().eq("deleted", false).findList();
		ExpressionList<PlanItem> el = Ebean
			.find(PlanItem.class)
			.where().join("order", "internalOrderNo").where().join("order.warehouse", "nameKey").where().join("materialUom", "uomCode").where()
			.join("fromMaterialUom", "uomCode").where().join("material", "materialCode").where().eq("deleted", false)
			.eq("planType", CodeKey.PLAN_TYPE_TRANSFER.getValue());
		if (tps.planItemId != null && !"".equals(tps.planItemId.trim())) {
			el.contains("id", tps.planItemId);
		}
		if (tps.piNO != null && !"".equals(tps.piNO.trim())) {
			el.contains("order.externalOrderNo", tps.piNO);
		}
		if (tps.sgPiNO != null && !"".equals(tps.sgPiNO.trim())) {
			el.contains("order.contractNo", tps.sgPiNO);
		}
		if (tps.piStatus != null && !"".equals(tps.piStatus.trim())) {
			el.contains("order.orderStatus", tps.piStatus);
		}
		if (tps.planStatus != null && !"".equals(tps.planStatus.trim())) {
			el.contains("itemStatus", tps.planStatus);
		}
		if (tps.transferFromDate != null) {
			el.ge("plannedExecutionAt", DateUtil.strToDate(sdf.format(tps.transferFromDate) + " 00:00:00", "yyyy-MM-dd HH:mm:ss"));
		}
		if (tps.transferToDate != null) {
			el.le("plannedExecutionAt", DateUtil.strToDate(sdf.format(tps.transferToDate) + " 23:59:59", "yyyy-MM-dd HH:mm:ss"));
		}
		if (EmptyUtil.isNotEmtpyString(tps.transferType)) {
			flagTransferType = true;
		}
		planItems = el.order("updatedAt descending").findList();
		if (planItems != null && !planItems.isEmpty()) {
			for (PlanItem planItem : planItems) {
				if (flagTransferType) {
					String transferType = ExtUtil.unapply(planItem.ext).get("transferType").trim();
					if (tps.transferType.equals(transferType)) {
						tpResults.add(TPResult.getTPResult(planItem));
					}
				} else {
					tpResults.add(TPResult.getTPResult(planItem));
				}

			}
		}
		return tpResults;
	}

}
