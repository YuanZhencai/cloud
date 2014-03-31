package utils.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controllers.setup.MaterialMasterController;

import play.db.ebean.Transactional;

import utils.DateUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import utils.exception.CustomException;

import models.Material;
import models.MaterialUom;
import models.Order;
import models.OrderItem;
import models.StockTransaction;
import models.UomCapacityPoint;
import models.vo.Result.ResultVo;

import Service.MaterialService;
import Service.OrderService;
import Service.TranactionService;
import Service.Vo.SMaterial;
import Service.Vo.SOrder;
import Service.Vo.SOrderItemVo;
import Service.Vo.STransactionVo;

public class DataExchangePlatform {
        @Transactional
        public static String getOrders(){
            String result="can be updated,";
            List<String> orderList=new ArrayList<String>();
            int i=0;
            List<SOrder> ordersbyWH = OrderService.getOrdersbyWH(SessionSearchUtil.searchWarehouse().warehouseCode,0);
            if(ordersbyWH!=null&&ordersbyWH.size()>0){
            for (SOrder sOrder : ordersbyWH) {
            	
               List<Order> orders = Order.find().where().eq("deleted",false).eq("internalOrderNo", sOrder.orderNo).eq("orderType",sOrder.orderType ).findList();
               if(orders.size()>0){
                  /* Order order = orders.get(0);
                   sOrder.outOrder(order);
                   order.updatedAt=DateUtil.currentTimestamp();
                   order.updatedBy=SessionSearchUtil.searchUser().id;
                   order.update();
                   for(OrderItem orderItem:order.items){
                       orderItem.createdAt=DateUtil.currentTimestamp();
                       orderItem.createdBy=SessionSearchUtil.searchUser().id;
                       orderItem.save();
                   }
                   sOrder.status=1;
                   sOrder.id=order.id.toString();
                   SOrder putOrder = OrderService.putOrder(sOrder);
                   if(putOrder==null)*/
            	   Order order = orders.get(0);
            	   List<OrderItem> orderItems = OrderItem.find().where().eq("deleted", false).eq("order.id", order.id).eq("order.deleted",false).findList();
            	   if(orderItems.size()>0&&sOrder.items!=null&&sOrder.items.size()>0){
            		   OrderItem orderItem = orderItems.get(0);
            		   /*String doubleValue = ExtUtil.unapply(orderItem.ext).get("piQty");
            		   Double valueOf = Double.valueOf(doubleValue);*/
            		   double doubleValue=0;
            		   if(orderItem.settlementQty!=null)
            		    doubleValue = orderItem.settlementQty.doubleValue();
            		   //double doubleValue = orderItems.get(0).qty.doubleValue();
            		if(doubleValue== sOrder.items.get(0).qty){
            			updateOrder(sOrder,order);
            			orderList.add("PI ["+order.internalOrderNo+"] downloaded contains production schedule on date ["+DateUtil.dateToStrShort(DateUtil.timestampToDate(order.orderTimestamp))+"]");
            		}else{
            			updateOrder(sOrder,order);
            			orderList.add("PI ["+order.internalOrderNo+"] downloaded contains production schedule on date ["+DateUtil.dateToStrShort(DateUtil.timestampToDate(order.orderTimestamp))+"] and PI quantity has been updated Please verify above PI Production Schedule.");
            		}
            	   }
            	   
               }else{
                   Order order = new Order();
                   sOrder.outOrder(order);
                   order.createdAt=DateUtil.currentTimestamp();
                   order.createdBy=SessionSearchUtil.searchUser().id;
                   order.updatedAt=DateUtil.currentTimestamp();
                   order.updatedBy=SessionSearchUtil.searchUser().id;
                   order.save();
                   for(OrderItem orderItem:order.items){
                       orderItem.createdAt=DateUtil.currentTimestamp();
                       orderItem.createdBy=SessionSearchUtil.searchUser().id;
                       orderItem.updatedAt=DateUtil.currentTimestamp();
                       orderItem.updatedBy=SessionSearchUtil.searchUser().id;
                       orderItem.save();
                   }
                   sOrder.status=1;
                  // sOrder.id=order.id.toString();
                  // System.out.println(play.libs.Json.toJson(sOrder));
                   SOrder putOrder = OrderService.putOrder(sOrder);
                   if(putOrder!=null){
                       //result="Order Upload Success;";
                	   i++;
                   }
               }
            }
            if(orderList.size()!=0){
            	result=" And ";
            	for (String msg : orderList) {
            		result=msg+";<br>"+result;
				}
            }else{
            	result="";
            }
                return "Total "+i+" orders Download Success;<br>"+result;
            }else{
                return "No order needs to be download;";
            }
        }
        public static void updateOrder(SOrder sOrder,Order order){
        	System.out.println("+++++++++++++catch PI+++++++++++++++++"+order.internalOrderNo);
        ///	Order order = orders.get(0);
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
        public static String deleteOrder(Order order){
            String id = ExtUtil.unapply(order.ext).get("guid");
            if(id!=null&&!id.equals("")){
            SOrder sorder = OrderService.getOrder(id);
            sorder.status=0;
            SOrder putOrder = OrderService.putOrder(sorder);
            if(putOrder!=null)
                return "Order of SAP reset Success";
            else
                return "Order of SAP had been deleted";
            }else{
                return "Order isn't download from SAP";
            }
        }
        @Transactional
        public static String getMaterials(String Owner){
            String result="";
            int i=0;
            List<SMaterial> materialbyowner = MaterialService.getMaterialbyowner(Owner);
            System.out.println(play.libs.Json.toJson(materialbyowner));
            if(materialbyowner!=null){
            for (SMaterial sMaterial : materialbyowner) {
                List<Material> materials = Material.find().where().eq("deleted", false).eq("materialCode", sMaterial.material).eq("owner.ownerName", sMaterial.owner).findList();
                if(materials.size()>0){
                   /* Material material = materials.get(0);
                    sMaterial.setValueMaterial(material);
                    material.updatedAt=DateUtil.currentTimestamp();
                    material.updatedBy=SessionSearchUtil.searchUser().id;
                    material.update();
                    for (MaterialUom materialUom : material.items) {
                        materialUom.createdAt=DateUtil.currentTimestamp();
                        materialUom.createdBy=SessionSearchUtil.searchUser().id;
                        materialUom.save();
                    }*/
                    //result=result+ "Material:"+sMaterial.material+" had been save;";
                }else{
                    Material material=new Material();
                    sMaterial.setValueMaterial(material);
                    
                    try {
						MaterialMasterController.saveMatAndUom(material);
					} catch (CustomException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    i++;
               /*     if(material!=null){
                    material.createdAt=DateUtil.currentTimestamp();
                    material.createdBy=SessionSearchUtil.searchUser().id;
                    material.updatedAt=DateUtil.currentTimestamp();
                    material.updatedBy=SessionSearchUtil.searchUser().id;
                    material.save();
                    for (MaterialUom materialUom : material.items) {
                        materialUom.createdAt=DateUtil.currentTimestamp();
                        materialUom.createdBy=SessionSearchUtil.searchUser().id;
                        materialUom.updatedAt=DateUtil.currentTimestamp();
                        materialUom.updatedBy=SessionSearchUtil.searchUser().id;
                        materialUom.save();
                       UomCapacityPoint uomCapacityPoint = new  UomCapacityPoint();
                       uomCapacityPoint.capacityType="Kg";
                       uomCapacityPoint.capacityPoint=new BigDecimal(materialUom.qtyOfBaseNum.doubleValue()/materialUom.qtyOfBaseDenom.doubleValue());
                       uomCapacityPoint.materialUom=materialUom;
                       uomCapacityPoint.createdAt=DateUtil.currentTimestamp();
                       uomCapacityPoint.createdBy=SessionSearchUtil.searchUser().id;
                       uomCapacityPoint.updatedAt=DateUtil.currentTimestamp();
                       uomCapacityPoint.updatedBy=SessionSearchUtil.searchUser().id;
                       uomCapacityPoint.save();
                     }
                    //result=result+":"+material.materialCode+"Materials Download Successfully;";
                    }*/
                }
            }
            
                return "Total "+i+" Materials Download Success";
            }else{
                return "No Material need Upload";
            }
        }
        @Transactional
        public static Material updateMaterial(String materialName,String Owner){
            SMaterial materialbyCode = MaterialService.getMaterialbyCode(materialName,Owner);
            if(materialbyCode!=null){
                List<Material> materials = Material.find().where().eq("deleted", false).eq("materialCode", materialbyCode.material).eq("owner.ownerName", materialbyCode.owner).findList();
                if(materials.size()>0){
                    Material material = materials.get(0);
                 /*   materialbyCode.setValueMaterial(material);
                    material.updatedAt=DateUtil.currentTimestamp();
                    material.updatedBy=SessionSearchUtil.searchUser().id;
                    material.update();
                    for (MaterialUom materialUom : material.items) {
                        materialUom.createdAt=DateUtil.currentTimestamp();
                        materialUom.createdBy=SessionSearchUtil.searchUser().id;
                        materialUom.save();
                    }*/
                    return material;
                }else{
                	 Material material=new Material();
                	 materialbyCode.setValueMaterial(material);
                     if(material!=null){
                     material.createdAt=DateUtil.currentTimestamp();
                     material.createdBy=SessionSearchUtil.searchUser().id;
                     material.updatedAt=DateUtil.currentTimestamp();
                     material.updatedBy=SessionSearchUtil.searchUser().id;
                     material.save();
                     for (MaterialUom materialUom : material.items) {
                         materialUom.createdAt=DateUtil.currentTimestamp();
                         materialUom.createdBy=SessionSearchUtil.searchUser().id;
                         materialUom.updatedAt=DateUtil.currentTimestamp();
                         materialUom.updatedBy=SessionSearchUtil.searchUser().id;
                         materialUom.save();
                        UomCapacityPoint uomCapacityPoint = new  UomCapacityPoint();
                        uomCapacityPoint.capacityType="Kg";
                        uomCapacityPoint.capacityPoint=new BigDecimal(materialUom.qtyOfBaseNum.doubleValue()/materialUom.qtyOfBaseDenom.doubleValue());
                        uomCapacityPoint.materialUom=materialUom;
                        uomCapacityPoint.createdAt=DateUtil.currentTimestamp();
                        uomCapacityPoint.createdBy=SessionSearchUtil.searchUser().id;
                        uomCapacityPoint.updatedAt=DateUtil.currentTimestamp();
                        uomCapacityPoint.updatedBy=SessionSearchUtil.searchUser().id;
                        uomCapacityPoint.save();
                      }
                     //result=result+":"+material.materialCode+"Materials Download Successfully;";
                     }
                    return material;
                }
            }else{
                return null;
            }
        }
        public static boolean setTransaction(StockTransaction Transaction){
            STransactionVo sTransactionVo = new STransactionVo();
            sTransactionVo.inTransaction(Transaction);
            STransactionVo result = TranactionService.putTransaction(sTransactionVo);
            if(result!=null) return true;
            else return false;
        }
        
}
