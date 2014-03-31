package models.vo.outbound;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import com.ning.http.util.DateUtil;

import utils.ExtUtil;
import utils.SessionSearchUtil;

import models.Batch;
import models.PlanItem;
import models.PlanItemDetail;
import models.Stock;
import models.StockTransaction;
import models.vo.query.BatchVo;
@JsonIgnoreProperties(ignoreUnknown = true)
public class stuffingDetial {
	static  int x=0;
	public String Number;
	public String id;
	public String PlanDetialId;
	public String piNo;
	public String No;//stockNo
	public String batchNo;
	public String planItemId;
	public String stockId;
	public String warehouse;
	public String storageArea;
	public String storageBin;
	public double quantityPallet;
	public String containerNo;
	public double stuffingQuantity;
	public boolean show=true;
	public boolean isPlanned=false;
	public String cargoRemarks;
	public Date time;
	public boolean isExecuted;
	
	//public List<BatchVo>  batchNoList;
	//public List<stuffingDetial>  palletNoList;
	
	public String shift;
	
	@JsonProperty("time")
	public void setDateTime(String time) {
		if(StringUtils.isNotBlank(time)){
			time = time.replaceAll("T"," ");
			this.time = utils.DateUtil.strFormatDate(time);
		}
	}
	public String remarks;
	public String reason;
	
	public void inStock(Stock stock){
		if(stock!=null){
		this.stockId=stock.id.toString();
		this.warehouse=stock.warehouse.nameKey;
		this.storageArea=stock.area.nameKey;
		this.storageBin=stock.bin.nameKey;
		this.quantityPallet=stock.qty.doubleValue();
		this.batchNo = ExtUtil.unapply(stock.batch.ext).get("lot");
		this.setStockExt(stock.ext);
		isExecuted = false;
		isPlanned=isPlanned(this.stockId);
		}
	}
	public boolean isPlanned(String id){
		List<PlanItemDetail> findList = PlanItemDetail.find().where()
				.eq("deleted",false)
				.eq("planItem.deleted",false)
				.eq("planItem.planType", "T003")
				.eq("stock.id",id)
				.eq("planItem.order.warehouse.id",  SessionSearchUtil.searchWarehouse().id.toString())
				.eq("planItem.order.deleted", false)
				.findList();
		if(findList.size()>0) return true;
		else return false;
	}
	public void inTransaction(StockTransaction stockTransaction){
		id=stockTransaction.stock.id.toString();
		planItemId=stockTransaction.execution.planItem.id.toString();
		warehouse=stockTransaction.stock.warehouse.nameKey;
		storageArea=stockTransaction.execution.fromArea.nameKey;
		storageBin=stockTransaction.execution.fromBin.nameKey;
		quantityPallet=stockTransaction.oldQty.doubleValue();
		batchNo = stockTransaction.stock.batch.batchNo;
		setExt(stockTransaction.execution.ext);
		setStockExt(stockTransaction.stock.ext);
		stuffingQuantity=stockTransaction.oldQty.doubleValue()-stockTransaction.newQty.doubleValue();
		isExecuted = stockTransaction.execution.planItem.plan.planStatus.equals("S003");
	}
	public void inPlanItemDetial(PlanItemDetail planItemDetail){
		inStock(planItemDetail.stock);
		stuffingQuantity=planItemDetail.palnnedQty.doubleValue();
		this.PlanDetialId=planItemDetail.id.toString();
		this.planItemId=planItemDetail.planItem.id.toString();
		getDetialExt(planItemDetail.ext);
	}
	public PlanItemDetail OutPlanItemDetial(){
		boolean Updeate=true;
		PlanItemDetail planItemDetail=null;
		if(this.PlanDetialId!=null&&!this.PlanDetialId.equals("")){
			planItemDetail = PlanItemDetail.find().where().eq("deleted", false).eq("id",this.PlanDetialId).findUnique();
		}
		if(planItemDetail==null){
			Updeate=false;
		planItemDetail = new PlanItemDetail();
		planItemDetail.createdAt=utils.DateUtil.currentTimestamp();
		planItemDetail.createdBy=SessionSearchUtil.searchUser().id;
		}
		//System.out.println(PlanDetialId+"///"+planItemDetail.id);
		planItemDetail.planItem=PlanItem.find().byId(this.planItemId);
		planItemDetail.palnnedQty=new BigDecimal(this.stuffingQuantity);
		Stock stock = Stock.find().where().eq("id",this.stockId).findUnique();
		if(stock!=null){
		planItemDetail.stock=stock;
		planItemDetail.fromArea=stock.area;
		planItemDetail.fromBin=stock.bin;
		planItemDetail.fromMaterialUom=stock.materialUom;
		}
		HashMap<String,String> hashMap = new HashMap<String,String>();
		hashMap.put("cargoRemarks",this.cargoRemarks==null?"":this.cargoRemarks);
		hashMap.put("Time",this.time==null?"":String.valueOf(this.time.getTime()));
		planItemDetail.ext=ExtUtil.apply(hashMap);
		planItemDetail.updatedAt=utils.DateUtil.currentTimestamp();
		planItemDetail.updatedBy=SessionSearchUtil.searchUser().id;
		if(!Updeate){
			planItemDetail.save();
		}else{
			planItemDetail.update();
		}
		return planItemDetail;
		
	}
	public void getDetialExt(String ext){
		HashMap<String, String> hashMap = ExtUtil.unapply(ext);
		this.cargoRemarks=hashMap.get("cargoRemarks");
		if(StringUtils.isNotEmpty(hashMap.get("Time")))
			this.time=new Date(Long.valueOf(hashMap.get("Time")));
	}
	public void setExt(String ext){
		HashMap<String, String> Ext = ExtUtil.unapply(ext);
		containerNo=Ext.get("containerNo");
		piNo = Ext.get("pi");
	}
	public void setStockExt(String ext){
		HashMap<String, String> Ext = ExtUtil.unapply(ext);
		No=Ext.get("stockNo");
		//cargoRemarks = Ext.get("cargoRemarks");
	}
	public String returnExt(){
		HashMap<String, String> Ext = new HashMap<String, String>();
		Ext.put("containerNo", containerNo!=null?containerNo:"");
		Ext.put("cargoRemarks",cargoRemarks!=null?containerNo:"");
		Ext.put("shift",shift!=null?shift:"");
		Ext.put("remarks",remarks!=null?remarks:"");
		Ext.put("reason",reason!=null?reason:"");
		return ExtUtil.apply(Ext);
	}
}
