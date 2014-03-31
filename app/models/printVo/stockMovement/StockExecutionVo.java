package models.printVo.stockMovement;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import utils.DateUtil;
import utils.ExtUtil;

import models.Bin;
import models.StockTransaction;

public class StockExecutionVo {
	public String palletNo;
	public Date productionDate;
	public Map<String,String> Ext=new HashMap<String, String>();
	public Date executionDate;
	public void inTransaction(StockTransaction transaction){
		if(transaction.stock!=null){
			this.palletNo=ExtUtil.unapply(transaction.stock.ext).get("stockNo");
			if(transaction.stock.batch!=null){
				String temp = ExtUtil.unapply(transaction.stock.batch.ext).get("date");
				if(StringUtils.isNotEmpty(temp))
				productionDate=DateUtil.strShortToDate(temp);
			}
		}
		if(transaction.transactionAt!=null)
			executionDate= new Date(transaction.transactionAt.getTime());
		if(transaction.transactionCode.indexOf("T1")>-1){
			if(transaction.stock!=null&&transaction.stock.batch!=null){
				System.out.println(transaction.stock.batch.ext);
				Ext.put("prodLine", ExtUtil.unapply(transaction.stock.batch.ext).get("line")!=null?ExtUtil.unapply(transaction.stock.batch.ext).get("line"):"");
			}
			if(transaction.execution!=null){
				Ext.put("paWork", ExtUtil.unapply(transaction.execution.ext).get("paworker")!=null?ExtUtil.unapply(transaction.execution.ext).get("paworker"):"");
				Ext.put("leader", ExtUtil.unapply(transaction.execution.ext).get("leader")!=null?ExtUtil.unapply(transaction.execution.ext).get("leader"):"");
				Ext.put("driver", ExtUtil.unapply(transaction.execution.ext).get("driver")!=null?ExtUtil.unapply(transaction.execution.ext).get("driver"):"");
				if(transaction.execution.planItemDetail!=null)
				Ext.put("blendingTank", ExtUtil.unapply(transaction.execution.planItemDetail.ext).get("blendingTank")!=null?ExtUtil.unapply(transaction.execution.planItemDetail.ext).get("blendingTank"):"");
				
			}
		}else if(transaction.transactionCode.indexOf("T2")>-1){
			if(transaction.oldBinId!=null){
				Bin bin = Bin.find().where().eq("id",transaction.oldBinId ).findUnique();
				if(bin!=null)
					Ext.put("from",bin.nameKey);
			}
			if(transaction.newBinId!=null){
				Bin bin = Bin.find().where().eq("id",transaction.newBinId ).findUnique();
				if(bin!=null)
					Ext.put("to",bin.nameKey);
			}
		}else if(transaction.transactionCode.indexOf("T3")>-1){
			if(transaction.execution!=null){
				Ext.put("containerNo", ExtUtil.unapply(transaction.execution.ext).get("containerNo"));
			}
		}
	}
}
