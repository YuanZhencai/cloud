package models.printVo.stockMovement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.time.DateUtils;

import action.Cell;

import models.Area;
import models.Batch;
import models.Bin;
import models.Order;
import models.Stock;
import models.StockTransaction;
import models.User;
import utils.CodeUtil;
import utils.DateUtil;
import utils.ExtUtil;
import utils.Excel.ExcelObj;

public class StockMovementDate extends ExcelObj{
	@Cell(name="PI NO")
	public String pI_No;
	@Cell(name="Batch No")
	public String batch_No;
	@Cell(name="Pallet Count")
	public Double palletCount=new Double(1);
	@Cell(name="Movement Type")
	public String movement_Type;
	@Cell(name="SKU qty")
	public double Qty;
	@Cell(name="UOM")
	public String UOM;
	@Cell(name="Remarks")
	public String Remarks;
	@Cell(name="From Bin")
	public String from_bin;
	@Cell(name="To Bin")
	public String to_bin;
	@Cell(name="Execute By")
	public String executeBy;
	@Cell(name="Execute DateTime")
	public Date Execute_Date;
	@Cell(name="stockNo",Type="deleted")
	public String stockNo;
	@Cell(name="executions",Type="deleted")
	public List<StockExecutionVo> executions=new ArrayList<StockExecutionVo>();
	@Cell(name="Type",Type="deleted")
	public int type;
	//public String last_Update_Person;
	public void inTransaction(StockTransaction transaction){
		Execute_Date=DateUtil.timestampToDate(transaction.transactionAt);
		movement_Type=CodeUtil.getTransActionTypeByCode(transaction.transactionCode);
		if(transaction.transactionCode.indexOf("T1")>-1){
			type=1;
		}else if(transaction.transactionCode.indexOf("T2")>-1){
			type=2;
		}else if(transaction.transactionCode.indexOf("T3")>-1){
			type=3;
		}else{
			type=0;
		}
		UOM=transaction.stock.materialUom.uomCode;
		from_bin=getStorageType(transaction.oldBinId);
		to_bin=getStorageType(transaction.newBinId);
		if(transaction.oldQty==null){
		    Qty=transaction.newQty.doubleValue();
		}else{
		    if(transaction.newQty.doubleValue()!=transaction.oldQty.doubleValue()){
		        Qty=transaction.newQty.doubleValue()-transaction.oldQty.doubleValue();
		        Qty= Math.abs(Qty);
		    }else
		        Qty=transaction.newQty.doubleValue();
		}
		Remarks=transaction.remarks;
		executeBy=getCreateName(transaction.createdBy);
		inStock(transaction.stock);
		StockExecutionVo stockExecutionVo = new StockExecutionVo();
		stockExecutionVo.inTransaction(transaction);
		executions.add(stockExecutionVo);
	}
	public void inBatch(Batch batch){
		if(batch!=null){
		pI_No=ExtUtil.unapply(batch.ext).get("pi");
		batch_No=ExtUtil.unapply(batch.ext).get("lot");
		}
	}
	public void inStock(Stock stock){
		if(stock!=null){
		stockNo=ExtUtil.unapply(stock.ext).get("stockNo");
		inBatch(stock.batch);
		}
	}
	public boolean isSame(StockMovementDate stockMovementDate){
		if(this.pI_No!=null&&this.pI_No.equals(stockMovementDate.pI_No)
		 &&this.batch_No!=null&&this.batch_No.equals(stockMovementDate.batch_No)
		 &&isSameDate(Execute_Date, stockMovementDate.Execute_Date)
		 &&this.movement_Type!=null&&this.movement_Type.equals(stockMovementDate.movement_Type)
		 &&this.from_bin!=null&&this.from_bin.equals(stockMovementDate.from_bin)
		 &&this.to_bin!=null&&this.to_bin.equals(stockMovementDate.to_bin)){
			this.Qty+=stockMovementDate.Qty;
			this.palletCount++;
			this.executions.addAll(stockMovementDate.executions);
			return true;
		}else{
			return false;
		}
	}
	public boolean isSameDate(Date date1,Date date2){
		if(date1==null||date2==null)
			return false;
		long deff=date1.getTime()-date2.getTime();
		if(deff/1000/60<10)
			return true;
		else
			return false;
	}
	public String getStorageType(UUID binId){
		if(binId==null) return "";
		Bin bin = Bin.find().where().eq("id", binId).findUnique();
		if(bin!=null)
			return bin.nameKey;
		else
			return "";
	}
	public String getCreateName(UUID nameId){
		if(nameId==null) return "";
		User findUnique = User.find().where().eq("id",nameId).findUnique();
		if(findUnique!=null)
			return findUnique.name;
		else
			return "";
	}
	public String getpI_No() {
		return pI_No;
	}
	public void setpI_No(String pI_No) {
		this.pI_No = pI_No;
	}
	public String getBatch_No() {
		return batch_No;
	}
	public void setBatch_No(String batch_No) {
		this.batch_No = batch_No;
	}
	public Double getPalletCount() {
		return palletCount;
	}
	public void setPalletCount(Double palletCount) {
		this.palletCount = palletCount;
	}
	public String getMovement_Type() {
		return movement_Type;
	}
	public void setMovement_Type(String movement_Type) {
		this.movement_Type = movement_Type;
	}
	public double getQty() {
		return Qty;
	}
	public void setQty(double qty) {
		Qty = qty;
	}
	public String getUOM() {
		return UOM;
	}
	public void setUOM(String uOM) {
		UOM = uOM;
	}
	public String getRemarks() {
		return Remarks;
	}
	public void setRemarks(String remarks) {
		Remarks = remarks;
	}
	public String getFrom_bin() {
		return from_bin;
	}
	public void setFrom_bin(String from_bin) {
		this.from_bin = from_bin;
	}
	public String getTo_bin() {
		return to_bin;
	}
	public void setTo_bin(String to_bin) {
		this.to_bin = to_bin;
	}
	public String getExecuteBy() {
		return executeBy;
	}
	public void setExecuteBy(String executeBy) {
		this.executeBy = executeBy;
	}
	public Date getExecute_Date() {
		return Execute_Date;
	}
	public void setExecute_Date(Date execute_Date) {
		Execute_Date = execute_Date;
	}
	
}
