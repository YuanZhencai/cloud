package Service.Vo;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import scala.Array;
import utils.CheckUtil;
import utils.DateUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import utils.Service.DataExchangePlatform;

import models.Material;
import models.MaterialUom;
import models.Order;
import models.OrderItem;
@JsonIgnoreProperties(ignoreUnknown = true)
public class SOrder {
    //public static String Warehouse="KTMNA";
    public static String NEW="S000";
    public String id;
    public String ts;
    public String owner;
    public String warehouse;
    public String orderType;
    public String orderNo;
    public String contractNo;
    public List<SOrderItemVo> items=new ArrayList<SOrderItemVo>();
    public String remarks;
    public HashMap<String,String> ext;
    public int status;
    public Order outOrder(Order order){
      //  Order order = orders.get(0);
        if(order.orderStatus==null){
            order.orderStatus=NEW;
        }
        if(order.orderStatus.equals(NEW)&&status==0){
        //order.id=UUID.fromString(this.id);
        order.internalOrderNo=this.orderNo;
        order.contractNo=this.contractNo;
        order.orderType=orderType;
        order.sourceType="T001";
        order.externalOrderNo=this.contractNo;
        order.orderTimestamp=new Timestamp(Long.parseLong(this.ts));
        order.warehouse=SessionSearchUtil.searchWarehouse();
        this.ext.put("guid", this.id);
        this.ext.put("remarks",this.remarks);
        this.ext.put("piType",CheckUtil.exportCheck(this.orderNo)?"Export":"Local");
        order.ext=ExtUtil.updateExt(order.ext, this.ext);
        List<OrderItem> orderItems = OrderItem.find().where().eq("deleted", false).eq("order.warehouse.deleted",false).eq("order",order).eq("order.warehouse.warehouseCode", SessionSearchUtil.searchWarehouse().warehouseCode).findList();
        for (OrderItem orderItem : orderItems) {
                orderItem.deleted=true;
                orderItem.updatedAt=DateUtil.currentTimestamp();
                orderItem.updatedBy=SessionSearchUtil.searchUser().id;
                orderItem.update();
        }
        for (SOrderItemVo sorderItemVo : this.items) {
                OrderItem orderItem = new OrderItem();
                String uomCode = ExtUtil.unapply(order.ext).get("uom");
                System.out.println("=================================================uomCode===================================="+uomCode);
                orderItem = sorderItemVo.getOrderItem(orderItem);
                List<Material> materials = Material.find().where().eq("deleted", false).eq("materialCode",sorderItemVo.material).findList();
                if(materials.size()>0)
                	orderItem.material=materials.get(0);
                else{
                	orderItem.material=DataExchangePlatform.updateMaterial(sorderItemVo.material,owner);
                }
                orderItem.order=order;
                
                String setExt = setExt(ExtUtil.unapply(order.ext),orderItem.material);
                orderItem.ext=orderItem.ext+","+setExt;
                if(uomCode!=null&&!uomCode.equals("")){
                    List<MaterialUom> uoms = MaterialUom.find().where().eq("deleted", false).eq("material.materialCode",orderItem.material.materialCode).eq("uomCode",uomCode).findList();
                    if(uoms.size()>0)
                    orderItem.settlementUom=uoms.get(0);
                    }
                order.items.add(orderItem);
            }
        }/*else if(order.orderStatus.equals(NEW)&&status==9){
            order.deleted=true;
        }*/
        return order;
    }
    public String  setExt(HashMap<String,String> ext,Material material){
        resetUom("qtyToBeShipUom", material,ext);
        resetUom("netWeightUom", material,ext);
        resetUom("qtyPerPalletUom", material,ext);
        resetUom("salesContractQtyUom",material,ext);
       // ext.put("sgMaterialCode", ext.get(""));
        return ExtUtil.apply(ext);
    }
    public void resetUom(String key,Material material,Map<String,String> ext){
        String name = ext.get(key);
        if(name!=null&&!name.equals("")){
            List<MaterialUom> uoms = MaterialUom.find().where().eq("deleted", false).eq("material",material).eq("uomCode",name).findList();
            if(uoms.size()>0)
                ext.put(key,uoms.get(0).id.toString());
        }else{
            List<MaterialUom> uoms = MaterialUom.find().where().eq("deleted", false).eq("material",material).eq("uomCode","KG").findList();
            if(uoms.size()>0)
                ext.put(key,uoms.get(0).id.toString());
        }
    }
}
