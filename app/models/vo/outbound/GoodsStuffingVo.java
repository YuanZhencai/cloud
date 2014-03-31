package models.vo.outbound;


import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import controllers.outbound.GoodsStuffingController;

import utils.BatchSearchUtil;
import utils.CodeUtil;
import utils.DateUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import utils.UnitConversion;

import models.Batch;
import models.PlanItem;
import models.Stock;

/**
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: GoodsStuffingVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoodsStuffingVo {
	public String id;
	public String tarnsferType;
	public String piNo;
	@JsonIgnore
	public String lotNo;
	@JsonIgnore
	public BigDecimal batchQty;
	public double netWeight;
	public double productionQuantity;
	public String productionUom;
	public double stuffingQuantity;
	public String stuffingUom;
	public Date transferDateTime;
	public String containerNo;
	public boolean loadingBayStatus;
	public boolean palletized;
	public String remarks;
	public boolean Executed;
	public List<String> PlanItemIds=new ArrayList<String>();
	public Date out;
	public HashMap<String,StuffingVo> batchs=new HashMap<String,StuffingVo>();
	@JsonProperty("out")
	public void setOut(String out){
		this.out=getDate(out);
	}
	public Date getDate(String Time){
		if(StringUtils.isNotBlank(Time)){
			Time = Time.replaceAll("T"," ");
			return utils.DateUtil.strToDate(Time,"yyyy-MM-dd HH:mm");
		}else{
			return null;
		}
	}
	public void inplanItem(PlanItem planItem){
		if(planItem.fromMaterialUom!=null&&planItem.toMaterialUom!=null&&planItem.material!=null)
			this.netWeight=UnitConversion.returnComparing(planItem.fromMaterialUom.uomCode, planItem.toMaterialUom.uomCode, planItem.material.id.toString());
		this.id=planItem.id.toString();
		this.PlanItemIds.add(id);
		this.piNo=planItem.plan.order.internalOrderNo;
		this.tarnsferType=CodeUtil.getPlanType(planItem.planType);
		setExt(planItem.ext);
		System.out.println("isnull ?:"+planItem.orderItem!=null&&planItem.orderItem.order!=null);
		if(planItem.orderItem!=null)
		setOrderExt(planItem.orderItem.ext);
		this.productionQuantity=planItem.orderItem.settlementQty.doubleValue();
		this.productionUom=planItem.orderItem.settlementUom.uomCode;
		this.stuffingQuantity=planItem.palnnedQty.doubleValue();
		this.stuffingUom=planItem.toMaterialUom.uomCode;
		long TempDate=planItem.plannedExecutionAt.getTime();
		this.transferDateTime=new Date(TempDate);
		setinitBatch(planItem);
		List<Batch> batches = BatchSearchUtil.getBatch(this.piNo,this.lotNo);
    	List<Stock> stocks = Stock.find().fetch("warehouse").fetch("area").fetch("bin")
        		.where().eq("warehouse.deleted",false).eq("area.deleted",false)
        		.eq("bin.deleted",false).in("batch",batches).eq("warehouse.deleted",false)
        		.eq("area.deleted", false).eq("bin.deleted", false).eq("batch.deleted", false)
        		.eq("area.storageType.nameKey","Loading Bay").eq("deleted", false).findList();
    	System.out.println("stock.seize:"+stocks.size());
    	this.loadingBayStatus=stocks.size()>0;
    	this.batchQty=planItem.palnnedQty;
    	if(GoodsStuffingController.isPlanExecuted(planItem.id.toString(),batchQty)){
    		this.Executed=true;
    	}else{
    		this.Executed=false;
    	}
    	
	}
	public void setExt(String ext){
		HashMap<String, String> Ext = ExtUtil.unapply(ext);
		lotNo=Ext.get("lot");
		containerNo=Ext.get("containerNo");
		String tempOut=Ext.get("out");
		if(StringUtils.isNotEmpty(tempOut))
		this.out=new Date(Long.valueOf(tempOut));
		
	}
	public void setOrderExt(String ext){
		//System.out.println(ext);
		HashMap<String, String> Ext = ExtUtil.unapply(ext);
		palletized =Boolean.valueOf(Ext.get("palletiZed")) ;
		remarks = Ext.get("cargoRemarks");
	}
	public boolean addStuffingVo(GoodsStuffingVo goodsStuffingVo){
		/*System.out.println("piissame:"+this.piNo.equals(goodsStuffingVo.piNo));
		System.out.println(this.id);
		System.out.println("idSame:"+(this.id!=goodsStuffingVo.id));*/
		if(this.id!=goodsStuffingVo.id&&this.piNo!=null&&this.piNo.equals(goodsStuffingVo.piNo)&&this.containerNo!=null&&this.containerNo.equals(goodsStuffingVo.containerNo)){
			this.PlanItemIds.add(goodsStuffingVo.id);
			this.batchs.putAll(goodsStuffingVo.batchs);
			this.batchQty=this.batchQty.add(goodsStuffingVo.batchQty);
			if(GoodsStuffingController.isPlanExecuted(this.id.toString(),batchQty)){
	    		this.Executed=true;
	    	}else{
	    		this.Executed=false;
	    	}
			return true;
		}else{
			return false;
		}
	}
	
	public void setinitBatch(PlanItem planItem){
		StuffingVo stuffingVo = new StuffingVo();
		stuffingVo.inPlanItem(planItem);
		if(batchs.containsKey(stuffingVo.batch)){
			batchs.get(stuffingVo.batch).Qty+=stuffingVo.Qty;
		}else{
			batchs.put(stuffingVo.batch, stuffingVo);
		}
	}
	public String setPlanOut(){
		for (String id : PlanItemIds) {
			PlanItem planItem = PlanItem.find().where().eq("deleted", false).eq("id", id).findUnique();
			if(planItem==null) return "This Data can't find";
			HashMap<String,String> unapply = ExtUtil.unapply(planItem.ext);
			if(out!=null&&!out.equals("")){
				unapply.put("out",String.valueOf(out.getTime()));
			}else{
				unapply.put("out","");
			}
			planItem.ext=ExtUtil.apply(unapply);
			planItem.updatedAt=DateUtil.currentTimestamp();
			planItem.updatedBy=SessionSearchUtil.searchUser().id;
			planItem.update();
		}
		return "Success";
	}
}
