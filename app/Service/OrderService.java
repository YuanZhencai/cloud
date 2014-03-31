package Service;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;

import Service.Vo.SOrder;

public class OrderService {
    public static String NEW="S000";
    public static List<SOrder> getOrdersbyWH(String Warehouse){
        List<SOrder> result = (List<SOrder>) DateService.sendHttp("/order/bywarehouse/"+Warehouse,"GET",null,new ArrayList<SOrder>(),new TypeReference<List<SOrder>>() {
        });
        return result;
    }
    
    public static List<SOrder> getOrdersbyWH(String Warehouse,int status){
        System.out.println("/order/bywarehousestatus/"+Warehouse+"/"+status);
        List<SOrder> result = (List<SOrder>) DateService.sendHttp("/order/bywarehousestatus/"+Warehouse+"/"+status,"GET",null,new ArrayList<SOrder>(),new TypeReference<List<SOrder>>() {
        });
        return result;
    }
    
    public static List<SOrder> getOrdersbyowner(String Owner){
        List<SOrder> result = (List<SOrder>) DateService.sendHttp("/order/byowner/"+Owner,"GET",null,new ArrayList<SOrder>(),new TypeReference<List<SOrder>>() {
        });
        return result;
    }
    
    public static List<SOrder> getOrdersbyowner(String Owner,int status){
        List<SOrder> result = (List<SOrder>) DateService.sendHttp("/order/byowner/"+Owner+"/"+status,"GET",null,new ArrayList<SOrder>(),new TypeReference<List<SOrder>>() {
        });
        return result;
    }
    public static SOrder getOrder(String id){
        SOrder result = (SOrder) DateService.sendHttp("/order/byid/"+id,"GET",null,new SOrder());
        return result;
    }
    
    public static SOrder putOrder(SOrder sOrderVo){
        SOrder result = (SOrder) DateService.sendHttp("/order/byid/"+sOrderVo.id,"PUT",sOrderVo,new SOrder());
        return result;
    }
    
    public static SOrder deleteOrder(String id){
        SOrder result = (SOrder) DateService.sendHttp("/order/byid/"+id,"DELETE",null,new SOrder());
        return result;
    }
    
}   
