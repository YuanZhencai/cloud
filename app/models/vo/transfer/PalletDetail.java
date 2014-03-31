/** * PalletDetail.java 
* Created on 2013-3-21 下午5:11:43 
*/

package models.vo.transfer;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import models.Area;
import models.Bin;
import models.Employee;
import models.Execution;
import models.MaterialUom;
import models.PlanItem;
import models.PlanItemDetail;
import models.Stock;
import models.StockTransaction;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.CrudUtil;
import utils.ExtUtil;
import utils.UnitConversion;
import utils.enums.CodeKey;

import com.avaje.ebean.Ebean;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: PalletDetail.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class PalletDetail extends PalletNoVo{
	// 是否编辑
	public Boolean show = true;
	// todo 代表这个stockVo是否被选中做操作
	public Boolean todo = false;
	public Boolean execute = false;
	//是否 被计划
	public Boolean plan = true;
	public Boolean notMatch = false;
	public String planItemId;
	//planItemDetail id
	public String id;
	public String stockId;
	public String warehouse;
	public String areaFrom;
	public String binFrom;
	public BigDecimal qty;
	public String areaTo;
	public String binTo;
	public String forkLiftDriver;
	public String status;
	public String shift;
	public String remarks;
	public String reason;
	public String batchNo;
	
	//print
	public String descritpion;
	public BigDecimal pack;

	public PalletDetail() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @param pid
	 * @param stcok
	 * @return
	 */
	public static PalletDetail getPalletDetail(PlanItemDetail pid) {
		List<MaterialUom> baseUoms = Ebean.find(MaterialUom.class).fetch("material").where().eq("deleted", false).eq("material", pid.stock.material).eq("baseUom", true).findList();
		PalletDetail pd = new PalletDetail();
		pd.planItemId = String.valueOf(pid.planItem.id);
		pd.id = String.valueOf(pid.id);
		pd.palletNo = ExtUtil.unapply(pid.stock.ext).get("stockNo");
		pd.stockId = pid.stock.id.toString();
		pd.qty = pid.stock.qty;
		pd.status = pid.detailStatus;
		if(CodeKey.PLAN_DETAIL_STATUS_EXECUTED.getValue().equals(pd.status)){
			pd.execute = true;
		}
		pd.batchNo=ExtUtil.unapply(pid.ext).get("lot");
		pd.areaFrom = pid.stock.area.nameKey;
		pd.binFrom = pid.stock.bin.nameKey;
		pd.areaTo = pid.toArea.nameKey;
		pd.binTo = pid.toBin.nameKey;
		pd.forkLiftDriver = pid.assignedTo.employeeName;
		pd.descritpion= pid.planItem.material.materialName;
		pd.pack = UnitConversion.SkuToQuantity(baseUoms.get(0),pid.stock.materialUom);
//		if(pid.detailStatus.equals("S000")&&(pid.stock.deleted||pid.stock.qty.compareTo(pid.palnnedQty)!=0||!pid.fromArea.nameKey.equals(pid.stock.area.nameKey)||!pid.fromBin.nameKey.equals(pid.stock.bin.nameKey))){
//			pd.notMatch =true;
//		}
		return pd;
	}
	public static PalletDetail getPalletDetail(PalletDetail parmPd) {
		 List<PlanItemDetail> pids = Ebean.find(PlanItemDetail.class).where().eq("deleted", false).eq("stock.id", parmPd.stockId).eq("planItem.id", parmPd.planItemId).findList();
		 return getPalletDetail(pids.get(0));
	}
	/**
	 * 
	 * @param pid
	 * @param stcok
	 * @return
	 */
	public static PalletDetail getPalletDetail(Execution execution) {
		List<StockTransaction> sts = Ebean.find(StockTransaction.class).where().eq("deleted", false).eq("execution", execution).findList();
		PalletDetail pd = new PalletDetail();
		pd.execute = true;
		pd.planItemId = String.valueOf(execution.planItem.id);
		if(execution.planItemDetail!=null){
			pd.id = String.valueOf(execution.planItemDetail.id);
		}
		pd.palletNo=ExtUtil.unapply(execution.planItemDetail.stock.ext).get("stockNo");
		pd.stockId = String.valueOf(sts.get(0).stock.id);
		pd.qty = execution.executedQty;
		pd.areaFrom = execution.fromArea.nameKey;
		pd.binFrom = execution.fromBin.nameKey;
		pd.areaTo = execution.toArea.nameKey;
		pd.binTo = execution.toBin.nameKey;
		HashMap<String,String> map = ExtUtil.unapply(execution.ext);
		pd.forkLiftDriver = map.get("FD");
		return pd;
	}

	public static PlanItemDetail getPlanItemDetail(PalletDetail pd, PlanItem pItem) throws Exception {
		//新建
		final String PIDSTATUSS000="S000";
		Boolean flag = false;
		Stock stock = null;
		PlanItemDetail pid = new PlanItemDetail();
		try{ 
         stock = Ebean.find(Stock.class).where().eq("deleted", false).eq("id", pd.stockId).findUnique();
	     }catch(Exception e){
	    	 throw new Exception(" "+pd.palletNo.toString());
	     }
		if(stock==null||stock.qty.compareTo(pd.qty)!=0||!stock.area.nameKey.equals(pd.areaFrom)||!stock.bin.nameKey.equals(pd.binFrom)){
			 throw new Exception(" "+pd.palletNo.toString());
		}
		List<Area> toAreas = Ebean.find(Area.class).where().eq("deleted", false).eq("nameKey", pd.areaTo).findList();
		List<Bin> toBins = Ebean.find(Bin.class).where().eq("deleted", false).eq("nameKey", pd.binTo).findList();
		List<Employee> employees = Employee.find().where().eq("employeeName", pd.forkLiftDriver).findList();
		List<PlanItemDetail> pids = Ebean.find(PlanItemDetail.class).where().eq("deleted", false).eq("stock", stock).eq("planItem", pItem).findList();
		if(pids!=null&&!pids.isEmpty()){
			pid = pids.get(0);
			flag = true;
		}
		pid.detailStatus = PIDSTATUSS000;
		pid.planItem = pItem;
		pid.stock = stock;
		HashMap<String, String> extMap = new HashMap<String, String>();
		extMap.put("Wearehouse", pd.warehouse);
		extMap.put("lot", pd.batchNo);
		pid.ext = ExtUtil.apply(extMap);
		// pid.fromMaterialUom = pItem.fromMaterialUom;
		// pid.palnnedQty = pd.palletQty;
		// pid.toMaterialUom = pItem.toMaterialUom;
		pid.fromArea = stock.area;
		pid.fromBin = stock.bin;
		pid.palnnedQty = stock.qty;
		pid.toArea = toAreas.get(0);
		pid.toBin = toBins.get(0);
		pid.assignedTo = employees.get(0);
		pid.sortNo = pItem.sortNo;
		if(flag){
			CrudUtil.update(pid);
		}else{
			CrudUtil.save(pid);
		}
		return pid;
	}

}
