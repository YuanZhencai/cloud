package Service.Vo;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.ExtUtil;
import utils.Service.DataExchangePlatform;

import models.Material;
import models.MaterialUom;
import models.OrderItem;
@JsonIgnoreProperties(ignoreUnknown = true)
public class SOrderItemVo {
    public int sortNo;
    public String material;
    public String uom;
    public double qty;
    public double minPercent;
    public double maxPercent;
    public Map<String, String> ext=new HashMap<String, String>();
    public OrderItem getOrderItem(OrderItem orderItem){
        orderItem.maxPercent=BigDecimal.valueOf(this.maxPercent);
        orderItem.minPercent=BigDecimal.valueOf(this.minPercent);
       /* List<MaterialUom> uoms = MaterialUom.find().where().eq("deleted", false).eq("material",orderItem.material).eq("uomCode", this.uom).findList();
        if(uoms.size()>0)
        orderItem.settlementUom=uoms.get(0);*/
        orderItem.settlementQty=BigDecimal.valueOf(this.qty);
        //ext.put("sgMaterialCode", material);
        this.ext.put("piQty", String.valueOf(qty));
        orderItem.ext=ExtUtil.updateExt(orderItem.ext,this.ext);
        return orderItem;
    }
}
