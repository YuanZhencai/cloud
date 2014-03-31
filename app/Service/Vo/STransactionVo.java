package Service.Vo;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.DateUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;

import models.Area;
import models.StockTransaction;
@JsonIgnoreProperties(ignoreUnknown = true)
public class STransactionVo {
    public String ts;
    public String id="";
    public String warehouse="KTMNA";
    public String owner="KTMNA";
    public String material="";
    public String uom="";
    public Double qty;
    public String txnCode="T001";
    public String remarks="";
    public Map<String,String> ext;
    public int status;
    public void inTransaction(StockTransaction transaction){
        id=transaction.id.toString();
        ts=String.valueOf(transaction.transactionAt.getTime());
        //ts=DateUtil.dateToStrShort(DateUtil.timestampToDate(transaction.transactionAt));
        if(transaction.warehouse!=null)
        warehouse=transaction.warehouse.nameKey;
        owner=transaction.stock.material.owner.ownerName;
        material= transaction.stock.material.materialCode;
        uom=transaction.stock.materialUom.uomCode;
        if(transaction.oldQty==null){
            qty=transaction.newQty.doubleValue();
        }else if(transaction.newQty!=null){
            qty=transaction.oldQty.doubleValue()-transaction.newQty.doubleValue();
        }
        //txnCode=transaction.transactionCode;
        if(transaction.remarks!=null)
        remarks=transaction.remarks;
        HashMap<String,String> unapply = ExtUtil.unapply(transaction.stock.batch.ext);
        
        ext=new HashMap<String,String>();
        ext.put("pi", unapply.get("pi"));
        ext.put("lotno",unapply.get("lot"));
        ext.put("line",unapply.get("line"));
        ext.put("compangy", SessionSearchUtil.searchWarehouse().company.companyCode);
        Area area = Area.find().where().eq("id",transaction.oldAreaId).findUnique();
        ext.put("area",area!=null?area.nameKey:"");
        status=0;
    }
}
