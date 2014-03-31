package models.vo.query;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import models.Area;
import models.Order;
import models.StockTransaction;
import utils.CodeUtil;
import utils.DateUtil;
import utils.EmptyUtil;
import utils.ExtUtil;
import utils.enums.CodeKey;

public class TransactionQueryVo {
	public String transaction_Type;
	public Date transaction_Date;
	public String pI_No;
	public String batch_No;
	public String sales_Contract_No;
	public String materialCode;
	public String from_storage_type;
	public String to_storage_Type;
	public String UOM;
	public String remarks;
	//public String last_Update_Person;
	public double Qty;
	public void inTransaction(StockTransaction transaction){
		transaction_Date=DateUtil.timestampToDate(transaction.transactionAt);
		transaction_Type=CodeUtil.getTransActionTypeByCode(transaction.transactionCode);
		UOM=transaction.stock.materialUom.uomCode;
		/*if(transaction.updatedBy!=null){
		    User user = User.find().where().eq("deleted",false).eq("id",transaction.updatedBy).findUnique();
		last_Update_Person=user.name;
		}else if(transaction.createdBy!=null){
		    User user = User.find().where().eq("deleted",false).eq("id",transaction.createdBy).findUnique();
	        last_Update_Person=user.name;
		}*/
		//pI_No=ExtUtil.unapply(transaction.stock.batch.ext).get("pi");
		//sales_Contract_No=transaction.execution.planItem.plan.order.contractNo;
		batch_No=ExtUtil.unapply(transaction.stock.batch.ext).get("lot");
		materialCode=transaction.stock.material.materialCode;
		from_storage_type=getStorageType(transaction.oldAreaId);
		to_storage_Type=getStorageType(transaction.newAreaId);
		//storage_Type=transaction..area.storageType.nameKey;
		if(transaction.oldQty==null){
		    Qty=transaction.newQty.doubleValue();
		}else{
		    if(transaction.newQty.doubleValue()!=transaction.oldQty.doubleValue()){
		        Qty=transaction.newQty.doubleValue()-transaction.oldQty.doubleValue();
		        Qty= Math.abs(Qty);
		    }else
		        Qty=transaction.newQty.doubleValue();
		}
		if(CodeKey.TRANSACTION_CODE_DIRECTTRANSFER.toString().equals(transaction.transactionCode)){
			HashMap<String,String> map = ExtUtil.unapply(transaction.ext);
			if(EmptyUtil.isNotEmtpyString(map.get("fromPI"))){
				this.remarks = "This transfer from PI "+map.get("fromPI");
			}
			if(EmptyUtil.isNotEmtpyString(map.get("toPI"))){
				this.remarks = "This transfer to PI "+map.get("toPI");
			}
		}
		if(this.remarks==null)
			this.remarks="";
		if(transaction.remarks!=null)
		this.remarks+=transaction.remarks;
	}
	public void inOrder(Order order){
		sales_Contract_No=order.contractNo;
		pI_No=order.internalOrderNo;
	}
	public boolean compareClass(TransactionQueryVo transactionQueryVo){
	    //System.out.println(this.transaction_Date+"===========================");
	   /// System.out.println(transactionQueryVo.transaction_Date);
	    if(this.transaction_Type!=null&&this.transaction_Type.equals(transactionQueryVo.transaction_Type)
	        &&this.transaction_Date!=null&&(DateUtil.dateToStrShort(this.transaction_Date).equals(DateUtil.dateToStrShort(transactionQueryVo.transaction_Date)))
	        &&(this.pI_No!=null&&this.pI_No.equals(transactionQueryVo.pI_No))
	        &&(this.batch_No!=null&&this.batch_No.equals(transactionQueryVo.batch_No))
	       // &&(this.sales_Contract_No!=null&&this.sales_Contract_No.equals(transactionQueryVo.sales_Contract_No))
	        &&(this.materialCode!=null&&this.materialCode.equals(transactionQueryVo.materialCode))
	        &&(this.from_storage_type!=null&&this.from_storage_type.equals(transactionQueryVo.from_storage_type))
	         &&(this.to_storage_Type!=null&&this.to_storage_Type.equals(transactionQueryVo.to_storage_Type))
	       // &&(this.last_Update_Person!=null&&this.last_Update_Person.equals(transactionQueryVo.last_Update_Person))
	            ){
	        transactionQueryVo.Qty=this.Qty+transactionQueryVo.Qty;
	        //System.out.println(Qty);
	        return true;
	    }else{
	        return false;
	    }
	}
	public String getStorageType(UUID Areaid){
		if(Areaid==null) return "";
		Area area = Area.find().where().eq("id", Areaid).findUnique();
		if(area!=null)
			return area.storageType.nameKey;
		else
			return "";
	}
}
