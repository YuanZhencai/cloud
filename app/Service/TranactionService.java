package Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import models.MaterialUom;
import models.Order;
import models.OrderItem;
import play.libs.Json;
import utils.DateUtil;
import utils.SessionSearchUtil;
import Service.Vo.SOrderItemVo;
import Service.Vo.SOrder;
import Service.Vo.STransactionVo;

public class TranactionService {
    public static String Warehouse="KTMNA";
    public static String Owner="KTMNA";
    public static String NEW="S000";
    public static List<STransactionVo> getTransactionsbyWH(){
        List<STransactionVo> result = (List<STransactionVo>) DateService.sendHttp("/transaction/bywarehouse/"+Warehouse,"GET",null,new ArrayList<STransactionVo>());
        return result;
    }
    
    public static List<STransactionVo> getTransactionsbyWH(int status){
        List<STransactionVo> result = (List<STransactionVo>) DateService.sendHttp("/transaction/bywarehousestatus/"+Warehouse+"/"+status,"GET",null,new ArrayList<STransactionVo>());
        return result;
    }
    
    public static List<STransactionVo> getTransactionsbyowner(){
        List<STransactionVo> result = (List<STransactionVo>) DateService.sendHttp("/transaction/byowner/"+Owner,"GET",null,new ArrayList<STransactionVo>());
        return result;
    }
    
    public static List<STransactionVo> getTransactionsbyowner(int status){
        List<STransactionVo> result = (List<STransactionVo>) DateService.sendHttp("/transaction/byowner/"+Owner+"/"+status,"GET",null,new ArrayList<STransactionVo>());
        return result;
    }
    public static STransactionVo getTransaction(String id){
        STransactionVo result = (STransactionVo) DateService.sendHttp("/transaction/byid/"+id,"GET",null,new STransactionVo());
        return result;
    }
    
    public static STransactionVo putTransaction(STransactionVo sTransactionVo){
        STransactionVo result = (STransactionVo) DateService.sendHttp("/transaction/byid/"+sTransactionVo.id,"PUT",sTransactionVo,new STransactionVo());
        return result;
    }
    
    public static STransactionVo deleteTransaction(String id){
        STransactionVo result = (STransactionVo) DateService.sendHttp("/transaction/byid/"+id,"DELETED",null,new STransactionVo());
        return result;
    }
    
    
}   
