package models.printVo.Transaction;

import java.util.Date;
import java.util.UUID;

import action.Cell;

import models.Area;
import models.Order;
import models.StockTransaction;
import utils.CodeUtil;
import utils.DateUtil;
import utils.ExtUtil;
import utils.Excel.ExcelObj;

public class TransactionDate extends ExcelObj{
	@Cell(name="TransactionType")
	public String transaction_Type;
	@Cell(name="TransactionDate")
	public Date transaction_Date;
	@Cell(name="PI NO")
	public String pI_No;
	@Cell(name="Batch No")
	public String batch_No;
	@Cell(name="Sales Contract No")
	public String sales_Contract_No;
	@Cell(name="Stock_No",Type="deleted")
	public String StockNo;
	@Cell(name="Material Code")
	public String materialCode;
	@Cell(name="From Storage Type")
	public String from_storage_type;
	@Cell(name="To Storage Type")
	public String to_storage_Type;
	@Cell(name="Transaction Quantity")
	public double Qty;
	@Cell(name="UOM")
	public String UOM;
	@Cell(name="Remarks")
	public String Remarks;
	//public String last_Update_Person;
	public void inTransaction(StockTransaction transaction){
		transaction_Date=DateUtil.timestampToDate(transaction.transactionAt);
		transaction_Type=CodeUtil.getTransActionTypeByCode(transaction.transactionCode);
		UOM=transaction.stock.materialUom.uomCode;
		batch_No=ExtUtil.unapply(transaction.stock.batch.ext).get("lot");
		materialCode=transaction.stock.material.materialCode;
		from_storage_type=getStorageType(transaction.oldAreaId);
		to_storage_Type=getStorageType(transaction.newAreaId);
		if(transaction.oldQty==null){
		    Qty=transaction.newQty.doubleValue();
		}else{
		    if(transaction.newQty.doubleValue()!=transaction.oldQty.doubleValue()){
		        Qty=transaction.newQty.doubleValue()-transaction.oldQty.doubleValue();
		        Qty= Math.abs(Qty);
		    }else
		        Qty=transaction.newQty.doubleValue();
		}
		StockNo=ExtUtil.unapply(transaction.stock.ext).get("stockNo");
		Remarks=transaction.remarks;
	}
	public void inOrder(Order order){
		sales_Contract_No=order.contractNo;
		pI_No=order.internalOrderNo;
	}
	
	public String getStorageType(UUID Areaid){
		if(Areaid==null) return "";
		Area area = Area.find().where().eq("id", Areaid).findUnique();
		if(area!=null)
			return area.storageType.nameKey;
		else
			return "";
	}
	public String getTransaction_Type() {
		return transaction_Type;
	}
	public void setTransaction_Type(String transaction_Type) {
		this.transaction_Type = transaction_Type;
	}
	public Date getTransaction_Date() {
		return transaction_Date;
	}
	public void setTransaction_Date(Date transaction_Date) {
		this.transaction_Date = transaction_Date;
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
	public String getSales_Contract_No() {
		return sales_Contract_No;
	}
	public void setSales_Contract_No(String sales_Contract_No) {
		this.sales_Contract_No = sales_Contract_No;
	}
	public String getMaterialCode() {
		return materialCode;
	}
	public void setMaterialCode(String materialCode) {
		this.materialCode = materialCode;
	}
	public String getFrom_storage_type() {
		return from_storage_type;
	}
	public void setFrom_storage_type(String from_storage_type) {
		this.from_storage_type = from_storage_type;
	}
	public String getTo_storage_Type() {
		return to_storage_Type;
	}
	public void setTo_storage_Type(String to_storage_Type) {
		this.to_storage_Type = to_storage_Type;
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
	public double getQty() {
		return Qty;
	}
	public void setQty(double qty) {
		Qty = qty;
	}
	public String getStockNo() {
		return StockNo;
	}
	public void setStockNo(String stockNo) {
		StockNo = stockNo;
	}
	
}
