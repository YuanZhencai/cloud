package controllers.setup;

import models.Business;
import models.Execution;
import models.Material;
import models.MaterialUom;
import models.Order;
import models.OrderItem;
import models.Plan;
import models.PlanItem;
import models.PlanItemDetail;
import models.StockTransaction;
import play.Logger;
import play.data.Form;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.With;
import static play.mvc.Http.RequestBody;
import play.mvc.Result;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import utils.Service.DataExchangePlatform;
import views.html.setup.business;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import action.Menus;

import Service.DateService;
import Service.OrderService;
import Service.TranactionService;
import Service.Vo.SOrder;
import Service.Vo.SOrderItemVo;
import Service.Vo.STransactionVo;
@With(Menus.class)
public class BusinessController extends Controller {

    //static Logger logger = new Logger();

    static Form<Business> businessForm = Form.form(Business.class);

    public static Result index() {
//        Business b1 = new Business();
//        b1.nameKey = "WILMAR";
//        b1.businessCode = "W";
//        b1.save();
//        Business b2 = new Business();
//        b2.nameKey = "KERRY";
//        b2.businessCode = "K";
//        b2.save();
        return ok(business.render("ODC Engineer"));
    }

    public static Result get(String id) {
        return ok(play.libs.Json.toJson(Business.find().byId(id)));
    }

    public static Result list() {
    	
    	 /* List<SOrder> ordersbyWH = OrderService.getOrdersbyWH(SessionSearchUtil.searchWarehouse().warehouseCode,1);
    	  for (SOrder sOrder : ordersbyWH) {
			sOrder.status=0;
			SOrder putOrder = OrderService.putOrder(sOrder);
		}*/
    	//boolean materials = DataExchangePlatform.getMaterials();
        return ok(play.libs.Json.toJson("Success Reset Status"));
    }

    public static Result save() {
    	//Form<Business> bindFromRequest = businessForm.bindFromRequest();
        //Logger.info(request().toString());
        //Business business = bindFromRequest.get();
        //List<Business> businesses = Json.fromJson(Json.parse(request().toString()), new java.util.ArrayList<Business>().getClass());;
        //Logger.info(Json.toJson(business).toString());
        RequestBody body = request().body();
        Business biz = Json.fromJson(body.asJson(), Business.class);
        Logger.info(">>>> " + biz);
        Logger.info(">>>> " + biz.id);
        Logger.info(">>>> " + biz.updatedAt);
        Logger.info(">>>> " + body.asJson());
        return ok();
    }
    @Transactional
    public static Result deletePlan(String pi,String type){
    	List<PlanItem> findList = PlanItem.find().where().eq("deleted", false).eq("orderItem.order.deleted", false).eq("orderItem.order.internalOrderNo", pi).eq("planType", type).findList();
    	for (PlanItem planItem : findList) {
    		List<PlanItemDetail> findList2 = PlanItemDetail.find().where().eq("deleted", false).eq("planItem.id", planItem.id.toString()).findList();
    		if(findList2.size()>0){
    			for (PlanItemDetail planItemDetail : findList2) {
    				List<Execution> findList3 = Execution.find().where().eq("deleted", false).eq("planItemDetail.id", planItemDetail.id.toString()).findList();
    				if(findList3.size()>0) return ok("This plan had execution"); 
					planItemDetail.deleted=true;
					planItemDetail.update();
				}
    			planItem.deleted=true;
    			planItem.update();
    		}
    		else{
    			planItem.deleted=true;
    			planItem.update();
    		}
		}
    	List<Plan> findList2 = Plan.find().where().eq("deleted", false).eq("order.internalOrderNo", pi).eq("planType", type).findList();
    	for (Plan plan : findList2) {
			plan.deleted=true;
			plan.update();
			plan.order.orderStatus="S000";
			plan.order.update();
		}
    	return ok("Deleted Success");
    }
    
    @Transactional
    public static Result updatePi(){
             List<SOrder> ordersbyWH = OrderService.getOrdersbyWH(SessionSearchUtil.searchWarehouse().warehouseCode,0);
             if(ordersbyWH!=null&&ordersbyWH.size()>0){
            	 System.out.println(ordersbyWH.size()+"===============DEP====================");
             for (SOrder sOrder : ordersbyWH) {
            	 System.out.println(sOrder.orderNo);
                List<Order> orders = Order.find().where().eq("deleted",false).eq("internalOrderNo", sOrder.orderNo)/*.eq("sourceType", "T002")*/.eq("orderType","T001" ).findList();
                if(orders.size()>0){
                	System.out.println("+++++++++++++catch PI+++++++++++++++++"+orders.get(0).internalOrderNo);
                	Order order = orders.get(0);
                	  //order.id=UUID.fromString(sOrder.id);
                    //order.internalOrderNo=sOrder.orderNo;
                    order.contractNo=sOrder.contractNo;
                   //order.orderType=sOrder.orderType;
                    order.sourceType="T001";
                    //order.externalOrderNo=sOrder.contractNo;
                    order.orderTimestamp=new Timestamp(Long.parseLong(sOrder.ts));
                    order.warehouse=SessionSearchUtil.searchWarehouse();
                    sOrder.ext.put("guid", sOrder.id);
                    sOrder.ext.put("remarks",sOrder.remarks);
                    order.ext=ExtUtil.updateExt(order.ext, sOrder.ext);
                    order.update();
                    List<OrderItem> orderItems = OrderItem.find().where().eq("deleted", false).eq("order.warehouse.deleted",false).eq("order",order).eq("order.warehouse.warehouseCode", SessionSearchUtil.searchWarehouse().warehouseCode).findList();
                    for (SOrderItemVo sorderItemVo : sOrder.items) {
                            OrderItem orderItem = orderItems.get(0);
                            String uomCode = ExtUtil.unapply(order.ext).get("uom");
                            System.out.println("=================================================uomCode===================================="+uomCode);
                            orderItem = sorderItemVo.getOrderItem(orderItem);
                         /*   List<Material> materials = Material.find().where().eq("deleted", false).eq("materialCode",sorderItemVo.material).findList();
                             if(materials.size()>0)
                            	orderItem.material=materials.get(0);*/
                           /* else{
                            	if(DataExchangePlatform.updateMaterial(sorderItemVo.material,sOrder.owner)!=null)
                            	orderItem.material=DataExchangePlatform.updateMaterial(sorderItemVo.material,sOrder.owner);
                            }*/
                            orderItem.order=order;
                            String setExt = setExt(ExtUtil.unapply(order.ext),orderItem.material);
                            orderItem.ext=orderItem.ext+","+setExt;
                            if(uomCode!=null&&!uomCode.equals("")){
                                List<MaterialUom> uoms = MaterialUom.find().where().eq("deleted", false).eq("material.materialCode",orderItem.material.materialCode).eq("uomCode",uomCode).findList();
                                if(uoms.size()>0)
                                orderItem.settlementUom=uoms.get(0);
                                }
                            orderItem.update();
                           // order.items.add(orderItem);
                        }
                    
             	   }
             	   
                }
             }else{
            	 return ok("Dep can't find Data");
             }
    	return ok("Update Pi Success");
    }
    public static String  setExt(HashMap<String,String> ext,Material material){
    	if(material!=null){
        resetUom("qtyToBeShipUom", material,ext);
        resetUom("netWeightUom", material,ext);
       // resetUom("qtyPerPalletUom", material,ext);
        resetUom("salesContractQtyUom",material,ext);
       // ext.put("sgMaterialCode", ext.get(""));
    	}
        return ExtUtil.apply(ext);
    }
	
 public static void resetUom(String key,Material material,Map<String,String> ext){
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
